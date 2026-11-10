const assert = require('assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const source = fs.readFileSync(path.resolve(__dirname,
    '../../main/resources/templates/pages/BearingStorage/BearingStorage-in.html'), 'utf8');
// Run the actual page scripts and HTTP helpers, with an in-memory HTTP backend.
function page(boxNumber, options = {}) {
    const requests = [], errors = [], successes = [];
    const rows = (options.rows || []).map(row => ({ ...row }));
    const fields = {};
    const button = { disabled: false };
    for (const id of ['stockInForm', 'boxText', 'boxNumber', 'quantity']) {
        fields[id] = { value: '', handlers: {}, addEventListener(name, callback) { this.handlers[name] = callback; } };
    }
    fields.boxText.value = options.boxText || 'ZMNIA';
    fields.boxNumber.value = boxNumber;
    fields.quantity.value = options.quantity === undefined ? '3000' : options.quantity;
    let stockFailures = options.stockFailures || 0;
    const response = (status, body) => ({ ok: status >= 200 && status < 300, status, json: async () => body });
    const document = {
        getElementById: id => fields[id],
        addEventListener() {},
        querySelector: () => button
    };
    const context = vm.createContext({
        document, URLSearchParams,
        $: () => ({ ready() {}, focus() {} }),
        console: { log() {}, error() {} },
        toastr: { error: message => errors.push(message), success: message => successes.push(message) },
        fetch: async (url, init = {}) => {
            const body = init.body ? JSON.parse(init.body) : undefined;
            requests.push({ url, body });
            const parsed = new URL(url, 'http://localhost');
            if (parsed.pathname === '/productIds/quantity') {
                if (options.lookupGate) await options.lookupGate;
                if (options.lookupStatus) return response(options.lookupStatus, {});
                const existing = rows.filter(row => row.boxNumber === parsed.searchParams.get('boxNumber'))
                    .sort((a, b) => b.iter - a.iter)[0];
                return existing ? response(200, { ...existing }) : response(404, {});
            }
            if (parsed.pathname === '/productIds/checkStocked') {
                if (options.checkStatus) return response(options.checkStatus, {});
                const existing = rows.find(row => row.boxNumber === parsed.searchParams.get('boxNumber')
                    && row.iter === Number(parsed.searchParams.get('iter')));
                return response(200, { isStocked: !!existing && existing.isStocked === 1 });
            }
            if (parsed.pathname.startsWith('/bearings/')) {
                if (options.generateStatus) return response(options.generateStatus, { message: '编号生成失败' });
                const generated = {
                    boxNumber: options.generatedNumber || boxNumber,
                    iter: options.generatedIter || 1, quantity: Number(body.quantity), isStocked: 0
                };
                rows.push(generated);
                return response(200, { ...generated });
            }
            if (parsed.pathname === '/bearingInventory/stockInWithRecord') {
                const existing = rows.find(row => row.boxNumber === body.boxNumber && row.iter === body.iter);
                assert(existing, 'Stock-in must refer to an existing or newly generated product');
                if (stockFailures-- > 0) return response(500, { message: '模拟入库失败，事务已回滚' });
                existing.isStocked = 1;
                return response(200, { message: 'ok' });
            }
            throw new Error('Unexpected request: ' + url);
        }
    });
    for (const script of source.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)) {
        if (script[1].trim()) vm.runInContext(script[1], context);
    }
    vm.runInContext(`depositoryId = 2; userDepositoryId = 2;
        latestBoxNumber = '001'; newiter = ${options.previewIter || 1};`, context);
    return {
        rows, requests, errors, successes, fields, button,
        submit: () => fields.stockInForm.handlers.submit({ preventDefault() {} }),
        generated: () => requests.filter(request => request.url.startsWith('/bearings/')),
        stockIns: () => requests.filter(request => request.url === '/bearingInventory/stockInWithRecord')
    };
}

const row = (boxNumber, iter = 1, isStocked = 0) => ({ boxNumber, iter, isStocked, quantity: 3000 });

async function run() {
    let cases = 0;
    for (const number of ['1001', '1002', '1999', '009', '001']) {
        const fixture = page(number, { rows: [row(number)] });
        await fixture.submit();
        assert.strictEqual(fixture.generated().length, 0, 'Existing boxes must not generate another number');
        assert.strictEqual(fixture.rows.length, 1);
        assert.strictEqual(fixture.stockIns()[0].body.boxNumber, number);
        assert.strictEqual(fixture.stockIns()[0].body.quantityInStock, '3000');
        assert.strictEqual(fixture.successes.length, 1);
        cases++;
    }

    for (const [input, generated, iter] of [['1001', '1001', 1], ['1003', '1003', 1],
        ['1005', '1003', 1], ['1001', '1001', 3], ['001', '001', 1], ['001', '002', 1]]) {
        const fixture = page(input, { generatedNumber: generated, generatedIter: iter });
        await fixture.submit();
        assert.strictEqual(fixture.generated().length, 1, 'Missing zero/previewed whole box must still be created');
        assert(fixture.generated()[0].url.includes(input.length === 4 ? '/bearings/zero/' : '/bearings/ZMNIA/'));
        assert.strictEqual(fixture.stockIns()[0].body.boxNumber, generated, 'Stock-in must use the actual generated number');
        assert.strictEqual(fixture.stockIns()[0].body.iter, iter, 'Stock-in must use the actual generated iteration');
        assert.strictEqual(fixture.rows[0].isStocked, 1);
        assert(fixture.successes[0].includes(generated));
        cases++;
    }

    const rollover = page('001', { rows: [row('001')], previewIter: 2, generatedIter: 2 });
    await rollover.submit();
    assert.strictEqual(rollover.generated().length, 1, 'Whole-box rollover must create the new iteration');
    assert.strictEqual(rollover.stockIns()[0].body.iter, 2);
    assert.strictEqual(rollover.rows[0].isStocked, 0, 'Historical iteration must remain unchanged');
    cases++;

    const existingIteration = page('1002', { rows: [row('1002', 4)] });
    await existingIteration.submit();
    assert.strictEqual(existingIteration.generated().length, 0);
    assert.strictEqual(existingIteration.stockIns()[0].body.iter, 4, 'Existing zero box uses its own iteration');
    cases++;

    for (const options of [{ rows: [row('1001', 1, 1)] }, { quantity: '0' }, { quantity: '' },
        { lookupStatus: 500 }, { rows: [row('1001')], checkStatus: 500 }, { generateStatus: 500 }]) {
        const fixture = page('1001', options);
        await fixture.submit();
        assert.strictEqual(fixture.stockIns().length, 0);
        assert.strictEqual(fixture.rows.filter(row => row.isStocked === 1).length, options.rows?.[0]?.isStocked || 0);
        if (!options.generateStatus) assert.strictEqual(fixture.generated().length, 0);
        assert(fixture.errors.length > 0);
        assert.strictEqual(fixture.button.disabled, false, 'Submission must be re-enabled after failure');
        cases++;
    }

    const missingWhole = page('009');
    await missingWhole.submit();
    assert.strictEqual(missingWhole.generated().length, 0);
    assert.strictEqual(missingWhole.stockIns().length, 0);
    cases++;

    const retry = page('1005', { generatedNumber: '1003', generatedIter: 2, stockFailures: 1 });
    await retry.submit();
    assert.strictEqual(retry.successes.length, 0);
    assert.strictEqual(retry.fields.boxNumber.value, '1003');
    await retry.submit();
    assert.strictEqual(retry.generated().length, 1, 'Retry must reuse the already generated number');
    assert.strictEqual(retry.stockIns().length, 2);
    assert.strictEqual(retry.successes.length, 1);
    cases++;

    const checkFailureOptions = { generatedNumber: '1003', generatedIter: 2, checkStatus: 500 };
    const checkRetry = page('1005', checkFailureOptions);
    await checkRetry.submit();
    assert.strictEqual(checkRetry.fields.boxNumber.value, '1003');
    assert.strictEqual(checkRetry.stockIns().length, 0);
    delete checkFailureOptions.checkStatus;
    await checkRetry.submit();
    assert.strictEqual(checkRetry.generated().length, 1, 'Status-query retry must reuse the generated number');
    assert.strictEqual(checkRetry.successes.length, 1);
    cases++;

    let release;
    const lookupGate = new Promise(resolve => { release = resolve; });
    const duplicate = page('1001', { lookupGate });
    const first = duplicate.submit();
    assert.strictEqual(duplicate.button.disabled, true);
    await duplicate.submit();
    assert.strictEqual(duplicate.requests.length, 1, 'Double-click must not start another submission');
    release();
    await first;
    assert.strictEqual(duplicate.generated().length, 1);
    assert.strictEqual(duplicate.stockIns().length, 1);
    assert.strictEqual(duplicate.button.disabled, false);
    cases++;

    console.log(`Stock-in page regression checks passed (${cases} scenarios).`);
}

run().catch(error => { console.error(error); process.exitCode = 1; });
