// 整箱、零箱分别查询；关闭优先编号提示后才打开预生成窗口。
function preGenerateWithReturnedBoxes(boxText, boxType, depositoryId, proceed) {
    var url = '/bearingRecords/reusable-records?boxText=' + encodeURIComponent(boxText)
        + '&boxType=' + encodeURIComponent(boxType) + '&depositoryId=' + depositoryId;
    return fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error('查询返库编号失败，状态码：' + response.status);
            }
            return response.json();
        })
        .then(records => {
            if (!Array.isArray(records)) {
                throw new Error('返库编号响应格式错误');
            }
            // 保留整箱编号的前导零，合并不同轮次中重复显示的编号。
            var numbers = Array.from(new Set(records.map(record => String(record.boxNumber))));
            if (numbers.some(number => !/^\d+$/.test(number))) {
                throw new Error('返库编号格式错误');
            }
            if (numbers.length === 0) {
                return proceed(boxText);
            }
            var boxLabel = boxType === 'zero' ? '零箱' : '整箱';
            return new Promise(resolve => {
                layer.alert('优先使用' + boxLabel + '返库编号：' + numbers.join('、')
                    + '<br>请在预生成窗口手动修改编号。', {
                    title: boxLabel + '返库编号提示',
                    end: resolve
                });
            }).then(() => proceed(boxText));
        })
        .catch(error => {
            console.error('查询优先使用编号失败：', error);
            layer.alert('查询返库编号失败，请重试。');
        });
}
