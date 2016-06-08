var exec = require("cordova/exec");

function BluebirdBarcode() {
}

BluebirdBarcode.prototype.register = function (options, successCallback, errorCallback) {
	return exec(
		successCallback,
		errorCallback,
		"BluebirdBarcode",
		"register",
		[]
	);
};

BluebirdBarcode.prototype.unregister = function (options, successCallback, errorCallback) {
	return exec(
		successCallback,
		errorCallback,
		"BluebirdBarcode",
		"unregister",
		[]
	);
};

module.exports = new BluebirdBarcode();
