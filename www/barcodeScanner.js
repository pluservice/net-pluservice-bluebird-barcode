var exec = require("cordova/exec");

function BarcodeScanner() {
}

BarcodeScanner.prototype.register = function (options, successCallback, errorCallback) {
	return exec(
		successCallback,
		errorCallback,
		"BarcodeScanner",
		"register",
		[]
	);
};

BarcodeScanner.prototype.unregister = function (options, successCallback, errorCallback) {
	return exec(
		successCallback,
		errorCallback,
		"BarcodeScanner",
		"unregister",
		[]
	);
};

module.exports = new BarcodeScanner();
