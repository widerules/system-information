package common.lib;

import android.os.Message;
import android.widget.Toast;

public class MyJavaScriptInterface {
	MyWebView mWebView;
	MyApp mAppstate;
	MyJavaScriptInterface(MyWebView webview, MyApp appstate) {
		mWebView = webview;
		mAppstate = appstate;
	}
	
	@SuppressWarnings("unused")
	public void processHTML(String html) {
		mWebView.pageSource = html;// to get page source, part 1
	}

	@SuppressWarnings("unused")
	public void showInterstitialAd() {
		if (mAppstate.interstitialAd.isReady()) mAppstate.interstitialAd.show();
		else {
			Toast.makeText(mAppstate.mContext, "Admob is loading", Toast.LENGTH_SHORT).show();
			mAppstate.interstitialAdClicked = true;
		}
		
		// try to load interstitialAd
		Message fail = mAppstate.mAppHandler.obtainMessage();
		fail.what = -3;
		mAppstate.mAppHandler.sendMessage(fail);
	}
}
