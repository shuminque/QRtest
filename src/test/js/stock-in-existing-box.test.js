const assert = require('assert');
const fs = require('fs');
const path = require('path');
const vm = require('vm');

const source = fs.readFileSync(path.resolve(__dirname,
    '../../main/resources/templates/pages/BearingStorage/BearingStorage-in.html'), 'utf8');
const start = source.indexOf("document.getElementById('stockInForm').addEventListener('submit'");
const end = source.indexOf('\n    function checkIfStocked', start);
assert(start !== -1 && end !== -1, 'Stock-in form handler must exist');

async function submit(boxText, boxNumber, { stocked = false, quantity = '3000' } = {}) {
    const generated = [];
    const stockIns = [];
    const errors = [];
    let handler;
    const fields = { boxText, boxNumber, quantity };
    const context = {
        document: {
            getElementById: id => id === 'stockInForm'
                ? { addEventListener: (name, callback) => { handler = callback; } }
                : { value: fields[id] }
        },
        depositoryId: 2,
        newiter: 1,
        latestBoxNumber: '001',
        checkIfStocked: async () => stocked,
        determineAndSaveProductId: async (...args) => { generated.push(args); },
        stockIn: async (...args) => { stockIns.push(args); },
        toastr: { error: message => errors.push(message) },
        console: { error: () => {} }
    };
    vm.runInNewContext(source.slice(start, end), context);
    handler({ preventDefault() {} });
    await new Promise(resolve => setImmediate(resolve));
    return { generated, stockIns, errors };
}

async function run() {
    for (const [boxText, boxNumber] of [['ZMNIA', '1001'], ['ZMNIB', '1002'], ['ZMNIA', '1999']]) {
        const result = await submit(boxText, boxNumber);
        assert.strictEqual(result.generated.length, 0, 'Existing zero box must not allocate another number');
        assert.deepStrictEqual(result.stockIns, [[boxText, boxNumber, '3000', '入库', 2, 1]]);
        assert.deepStrictEqual(result.errors, []);
    }

    const existingWhole = await submit('ZMNIA', '009');
    assert.strictEqual(existingWhole.generated.length, 0);
    assert.strictEqual(existingWhole.stockIns[0][1], '009');

    const nextWhole = await submit('ZMNIA', '001');
    assert.strictEqual(nextWhole.generated.length, 1, 'New whole box must still be created before stock-in');
    assert.strictEqual(nextWhole.generated[0][1], '001');
    assert.strictEqual(nextWhole.stockIns[0][1], '001');

    const stocked = await submit('ZMNIA', '1001', { stocked: true });
    assert.strictEqual(stocked.generated.length, 0);
    assert.strictEqual(stocked.stockIns.length, 0);
    assert(stocked.errors[0].includes('该箱号编号已入库'));

    const invalid = await submit('ZMNIA', '1001', { quantity: '0' });
    assert.strictEqual(invalid.generated.length, 0);
    assert.strictEqual(invalid.stockIns.length, 0);
    assert.strictEqual(invalid.errors.length, 1);
    console.log('Stock-in regression checks passed (7 cases).');
}

run().catch(error => { console.error(error); process.exitCode = 1; });
