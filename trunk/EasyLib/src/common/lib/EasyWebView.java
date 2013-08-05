package common.lib;

import easy.lib.R;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class EasyWebView extends MyWebView {

	public String m_ready = "";
	public EasyWebView(Context context, EasyApp appstate) {
		super(context, appstate);

		try {
			// hide scroll bar when not scroll. from API5, not work on cupcake.
			setScrollbarFadingEnabled.invoke(this, true);
		} catch (Exception e) {}

		webSettings.setDomStorageEnabled(true);// API7, key to enable gmail

		webSettings.setDefaultZoom(WrapWebSettings.ZoomDensity.MEDIUM);//start from API7

		// loads the WebView completely zoomed out. fit for hao123, but not fit for homepage. from API7
		webSettings.setLoadWithOverviewMode(mAppstate.overviewPage);
		webSettings.setForceUserScalable(true);

		addJavascriptInterface(new EasyJavaScriptInterface(this, appstate), "JSinterface");

		setWebChromeClient(new WebChromeClient() {
			boolean updated = false;
			
			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (progress == 100) mProgress = 0;
				else mProgress = progress;
				
				if (mAppstate.HOME_PAGE.equals(view.getUrl())) {
					getPageReadyState();
					// the progress is not continuous from 0, 1, 2... 100. it always looks like 10, 13, 15, 16, 100
					if ("".equals(m_ready)) //must update the page on after some progress(like 13), other wise it will not update success
						Log.d("=================", progress+"");
						mAppstate.updateHomePage();
				}

				if (isForeground) {
					mAppstate.loadProgress.setProgress(progress);
					if (progress == 100)
						mAppstate.loadProgress.setVisibility(View.GONE);
				}
			}

			// For Android 3.0+
			public void openFileChooser(ValueCallback<Uri> uploadMsg,
					String acceptType) {
				if (null == mAppstate.mUploadMessage)
					mAppstate.mUploadMessage = new WrapValueCallback();
				mAppstate.mUploadMessage.mInstance = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				mAppstate.mActivity.startActivityForResult(Intent.createChooser(i,
						mContext.getString(R.string.select_file)),
						mAppstate.FILECHOOSER_RESULTCODE);
			}

			// For Android < 3.0
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				openFileChooser(uploadMsg, "");
			}

			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog,
					boolean isUserGesture, android.os.Message resultMsg) {
				if (mAppstate.openNewPage(null, mAppstate.webIndex+1, true, true)) {// open new page success
					((WebView.WebViewTransport) resultMsg.obj)
							.setWebView(mAppstate.serverWebs.get(mAppstate.webIndex));
					resultMsg.sendToTarget();
					return true;
				} else return false;
			}
		});
	}
}
