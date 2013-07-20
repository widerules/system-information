package common.lib;

import android.net.Uri;
import android.webkit.ValueCallback;

//for get webpage source on cupcake
public class WrapValueCallback {
	public ValueCallback<Uri> mInstance;

	WrapValueCallback() {}

	static {
		try {
			Class.forName("android.webkit.ValueCallback");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void checkAvailable() {}

	public void onReceiveValue(Uri value) {
		mInstance.onReceiveValue(value);
	}
}

