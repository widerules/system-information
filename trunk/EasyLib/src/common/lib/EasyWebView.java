package common.lib;

import android.content.Context;

public class EasyWebView extends MyWebView {

	public EasyWebView(Context context, MyApp appstate) {
		super(context, appstate);
		
		addJavascriptInterface(new EasyJavaScriptInterface(this, appstate), "JSinterface");
	}

}
