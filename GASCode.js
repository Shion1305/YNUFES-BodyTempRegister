/*
This code is used for Google App Engine in Google Spread Sheet
 */
var test1 = {
    "parameter": {
        "type": "check",
        "name": "市川詩恩"
    }
};
var test2 = {
    "parameter": {
        "type": "register",
        "name": "市川詩恩",
        "temp": "100.0"
    }
};

function doGet(e) {
    return process(e);
}
const c = SpreadsheetApp.getActive().getSheetByName("編集現役");
function process(e) {
    if (!checkQualification(e)) return respond({ code: 400 });
    const indexN = checkNameExistance(e.parameter.name);
    if (indexN === -1) return respond({ code: 404 });
    if (e.parameter.type === "check") return respond({ code: 200 });
    const d = new Date(new Date().toLocaleString('ja-JP', { timeZone: 'Asia/Tokyo' }));
    const indexD = searchRowForDate(d.getMonth() + 1, d.getDate());
    if (indexD === -1) return respond({ code: 500 });
    const cell = c.getRange(indexD, indexN + 1);
    const oldV = cell.getValue();
    cell.setValue(e.parameter.temp);
    return respond({ code: 202, oldValue: oldV });
}

function checkNameExistance(name) {
    const data = c.getDataRange().getValues();
    return data[3].findIndex((v) => v === name);
}


//Input format should be conventional number for each field.
function searchRowForDate(month, date) {
    month -= 1;
    const dates = c.getRange(1, 1, c.getLastRow()).getValues();
    for (var i = 2; i <= dates.length; i++) {
        try {
            const d = new Date(dates[i].toLocaleString('ja-JP', { timeZone: 'Asia/Tokyo' }));
            if (!d.getMonth() || !d.getDate()) continue;
            if (month == d.getMonth() && date == d.getDate()) return i + 1;
        }
        catch (e) { }
    } return -1;
}

function checkQualification(e) {
    if (!e.parameter.type) return false;
    if (e.parameter.type === "check") return e.parameter.name;
    if (e.parameter.type === "register") return e.parameter.name && e.parameter.temp;
    return false;
}

function respond(status) {
    var response = {};
    response.code = status.code;
    switch (status.code) {
        case 200:
            response.message = "FOUND";
            break;
        case 202:
            response.message = "SUCCESS,REQUEST HAS BEEN PROCESSED"
            break;
        case 400:
            response.message = "BAD REQUEST";
            break;
        case 404:
            response.message = "REQUESTED NAME NOT FOUND";
            break;
        case 500:
            response.message = "FAILED TO FIND TARGET DATE";
            break;
    }
    if (status.oldValue) respond.oldValue = status.oldValue;
    return ContentService.createTextOutput(JSON.stringify(response))
        .setMimeType(ContentService.MimeType.JSON);
}