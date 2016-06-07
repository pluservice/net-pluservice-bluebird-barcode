package net.pluservice.bluebird;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.Exception;
import java.lang.IllegalArgumentException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BarcodeScanner extends CordovaPlugin {
	// Constants
	public static final String ACTION_BARCODE_CLOSE           = "kr.co.bluebird.android.bbapi.action.BARCODE_CLOSE";
	public static final String ACTION_BARCODE_DECODING_DATA   = "kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_DECODING_DATA";
	public static final String ACTION_BARCODE_OPEN            = "kr.co.bluebird.android.bbapi.action.BARCODE_OPEN";
	public static final String ACTION_BARCODE_REQUEST_FAILED  = "kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_REQUEST_FAILED";
	public static final String ACTION_BARCODE_REQUEST_SUCCESS = "kr.co.bluebird.android.bbapi.action.BARCODE_CALLBACK_REQUEST_SUCCESS";
	public static final String ACTION_BARCODE_SET_TRIGGER     = "kr.co.bluebird.android.bbapi.action.BARCODE_SET_TRIGGER";

	public static final String EXTRA_HANDLE        = "EXTRA_HANDLE";
	public static final String EXTRA_STR_DATA1     = "EXTRA_STR_DATA1";
	public static final String EXTRA_INT_DATA2     = "EXTRA_INT_DATA2";
	public static final String EXTRA_INT_DATA3     = "EXTRA_INT_DATA3";
	public static final String EXTRA_DECODING_DATA = "EXTRA_BARCODE_DECODING_DATA";

	public static final int ERROR_FAILED                       = -1;
	public static final int ERROR_NOT_SUPPORTED                = -2;
	public static final int ERROR_NO_RESPONSE                  = -4;
	public static final int ERROR_BATTERY_LOW                  = -5;
	public static final int ERROR_BARCODE_DECODING_TIMEOUT     = -6;
	public static final int ERROR_BARCODE_ERROR_USE_TIMEOUT    = -7;
	public static final int ERROR_BARCODE_ERROR_ALREADY_OPENED = -8;

	// Variables
	private CallbackContext barcodeContext;
	private int barcodeHandle;
	private boolean barcodeRegistered = false;

	// Functions
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
		// Execute
		try {
			// Context
			barcodeContext = callbackContext;

			// Result
			PluginResult result = null;

			// Intent
			Intent intent = new Intent();

			// Actions
			if (action.equals("register")) {
				result = register();
			}

			if (action.equals("unregister")) {
				result = unregister();
			}

			// Return
			callbackContext.sendPluginResult(result);
		}
		catch (Exception E) {
			// Error
			callbackContext.sendPluginResult(
				new PluginResult(
					PluginResult.Status.ERROR,
					E.getMessage()
				)
			);
		}

		// Return
		return true;
	}

	private PluginResult register() {
		// Register
		if (!barcodeRegistered) {
			// Filter
			IntentFilter filter = new IntentFilter();
			filter.addAction(BarcodeScanner.ACTION_BARCODE_DECODING_DATA);
			filter.addAction(BarcodeScanner.ACTION_BARCODE_REQUEST_SUCCESS);
			filter.addAction(BarcodeScanner.ACTION_BARCODE_REQUEST_FAILED);

			// Register
			webView.getContext().registerReceiver(barcodeReceiver, filter);
			barcodeRegistered = true;
		}

		// Result
		PluginResult result = new PluginResult(PluginResult.Status.OK);
		result.setKeepCallback(true);

		// Return
		return result;
	}
	private PluginResult unregister() {
		// Unregister
		if (barcodeRegistered) {
			webView.getContext().unregisterReceiver(barcodeReceiver);
			barcodeRegistered = false;
		}

		// Return
		return
			new PluginResult(
				PluginResult.Status.OK
			);
	}

	// Utility - Sender
	private void barcodeSender (String event, String data) {
		// Data
		JSONObject obj = new JSONObject();
		try {
			obj.put("action", event);
			obj.put("data", data);
		}
		catch (JSONException e) {
			return;
		}

		// Result
		PluginResult result = new PluginResult(PluginResult.Status.OK, obj);
		result.setKeepCallback(true);

		// Event
		if (barcodeContext != null) {
			barcodeContext.sendPluginResult(result);
		}
	}

	// Utility - Receiver
	private BroadcastReceiver barcodeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Parameters
			String action = intent.getAction();
			int seq = intent.getIntExtra(BarcodeScanner.EXTRA_INT_DATA3, 0);

			// Decode Data
			if (action.equals(BarcodeScanner.ACTION_BARCODE_DECODING_DATA)) {
				String event = null;
				String value = null;
				try {
					// Data
					byte[] data = intent.getByteArrayExtra(BarcodeScanner.EXTRA_DECODING_DATA);
					if (data == null) {
						throw new IllegalArgumentException();
					}

					// Value
					value = new String(data);
					event = "bluebird.barcodeScanned";
				}
				catch (Exception E) {
					// barcode
					value = E.getMessage();
					event = "bluebird.barcodeError";
				}

				// Event
				barcodeSender(event, value);
			}

			// Request Failed
			if (action.equals(BarcodeScanner.ACTION_BARCODE_REQUEST_FAILED)) {
				// Result
				int result = intent.getIntExtra(BarcodeScanner.EXTRA_INT_DATA2, 0);

				String value = null;
				if (result == BarcodeScanner.ERROR_BARCODE_DECODING_TIMEOUT) {
					value = "Decode Timeout";
				}
				else if (result == BarcodeScanner.ERROR_BARCODE_ERROR_ALREADY_OPENED) {
					value = "Already opened";
				}
				else if (result == BarcodeScanner.ERROR_BARCODE_ERROR_USE_TIMEOUT) {
					value = "Use Timeout";
				}
				else if (result == BarcodeScanner.ERROR_NOT_SUPPORTED) {
					value = "Not Supoorted";
				}
				else {
					value = String.valueOf(result);
				}
				// BarcodeScanner.ERROR_BATTERY_LOW
				// BarcodeScanner.ERROR_FAILED
				// BarcodeScanner.ERROR_NO_RESPONSE

				// Event
				barcodeSender("bluebird.barcodeError", value);
			}
		}
	};
}
