package common.lib;

import android.os.Message;
import android.widget.Toast;

public class MyJavaScriptInterface {
	MyWebView mWebView;
	SimpleBrowser mActivity;
	MyJavaScriptInterface(MyWebView webview, SimpleBrowser activity) {
		mWebView = webview;
		mActivity = activity;
	}
	
	@SuppressWarnings("unused")
	public void processHTML(String html) {
		mWebView.pageSource = html;// to get page source, part 1
	}

	@SuppressWarnings("unused")
	public void showInterstitialAd() {
		if (mActivity.interstitialAd.isReady()) mActivity.interstitialAd.show();
		else {
			Toast.makeText(mActivity.mContext, "Admob is loading", Toast.LENGTH_SHORT).show();
			mActivity.interstitialAdClicked = true;
		}
		
		// try to load interstitialAd
		Message fail = mActivity.mAppHandler.obtainMessage();
		fail.what = -3;
		mActivity.mAppHandler.sendMessage(fail);
	}
}
