package common.lib;

import android.net.Uri;
import android.webkit.ValueCallback;

class WrapValueCallback {
	ValueCallback<Uri> mInstance;

	WrapValueCallback() {}

	static {
		try {
			Class.forName("android.webkit.ValueCallback");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void checkAvailable() {}

	void onReceiveValue(Uri value) {
		mInstance.onReceiveValue(value);
	}
}

