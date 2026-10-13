// 整箱、零箱分别查询返库箱号及当前在库范围内的缺号；关闭提示后才打开预生成窗口。
function preGenerateWithReturnedBoxes(boxText, boxType, depositoryId, proceed) {
    var url = '/bearingRecords/reusable-records?boxText=' + encodeURIComponent(boxText)
        + '&boxType=' + encodeURIComponent(boxType) + '&depositoryId=' + depositoryId;
    var gapUrl = '/productIds/single-number-gaps?boxText=' + encodeURIComponent(boxText)
        + '&boxType=' + encodeURIComponent(boxType);
    return Promise.all([url, gapUrl].map(queryUrl => fetch(queryUrl).then(response => {
        if (!response.ok) {
            throw new Error('查询箱号提示失败，状态码：' + response.status);
        }
        return response.json();
    })))
        .then(results => {
            var records = results[0];
            var gaps = results[1];
            if (!Array.isArray(records) || !Array.isArray(gaps)) {
                throw new Error('箱号提示响应格式错误');
            }
            // 保留整箱箱号的前导零，合并不同轮次中重复显示的箱号。
            var numbers = Array.from(new Set(records.map(record => String(record.boxNumber))));
            if (numbers.some(number => !/^\d+$/.test(number))) {
                throw new Error('返库箱号格式错误');
            }
            if (gaps.some(gap => !/^\d+$/.test(String(gap.missingNumber))
                || !/^\d+$/.test(String(gap.beforeNumber))
                || !/^\d+$/.test(String(gap.afterNumber))
                || !Number.isInteger(Number(gap.iter)) || Number(gap.iter) < 1)) {
                throw new Error('历史缺号响应格式错误');
            }
            if (numbers.length === 0 && gaps.length === 0) {
                return proceed(boxText);
            }
            var boxLabel = boxType === 'zero' ? '零箱' : '整箱';
            var messages = [];
            if (numbers.length > 0) {
                messages.push('优先使用' + boxLabel + '返库箱号：' + numbers.join('、')
                    + '<br>请在预生成窗口手动修改箱号。');
            }
            if (gaps.length > 0) {
                var missingNumbers = Array.from(new Set(gaps.map(gap => String(gap.missingNumber))));
                messages.push('当前箱号存在' + boxLabel + '历史跳号：<br>'
                    + gaps.map(gap => '第' + Number(gap.iter) + '轮缺 '
                        + gap.missingNumber + '（' + gap.beforeNumber + ' → ' + gap.afterNumber + '）').join('<br>')
                    + '<br>请优先使用' + missingNumbers.join('、'));
            }
            return new Promise(resolve => {
                layer.alert(messages.join('<br><br>'), {
                    title: boxLabel + '箱号提示',
                    skin: 'qr-box-number-alert',
                    area: '560px',
                    btn: ['确定'],
                    end: resolve
                });
            }).then(() => proceed(boxText));
        })
        .catch(error => {
            console.error('查询箱号提示失败：', error);
            layer.alert('查询箱号提示失败，请重试。', {
                skin: 'qr-box-number-alert',
                area: '560px',
                btn: ['确定']
            });
        });
}
