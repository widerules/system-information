package common.lib;

import common.lib.MyJavaScriptInterface;
import easy.lib.EasyBrowser;

public class EasyJavaScriptInterface extends MyJavaScriptInterface {
	EasyBrowser mActivity;
	EasyJavaScriptInterface(EasyWebView webview, EasyBrowser activity) {
		super(webview, activity);
		
		mActivity = activity;
	}

	@SuppressWarnings("unused")
	public void processReady(String ready) {
		((EasyWebView)mWebView).m_ready = ready;
	}
	
	@SuppressWarnings("unused")
	public void saveCollapseState(String item, boolean state) {
		if ("1".equals(item))
			mActivity.collapse1 = state;
		if ("2".equals(item))
			mActivity.collapse2 = state;
		if ("3".equals(item))
			mActivity.collapse3 = state;
	}
	
	@SuppressWarnings("unused")
	public void deleteItems(String bookmarks, String historys) {
		if (!"".equals(historys) && !",,,,".equals(historys)) {
			String[] tmp1 = historys.split(",,,,");
			for (int i = 0; i < tmp1.length; i++) {// the display of history is in revert order, so delete in revert order
				int index = mActivity.mHistory.size() - 1 + i - (Integer.valueOf(tmp1[i]) - mActivity.mBookMark.size());
				if ((index >= 0) && (index < mActivity.mHistory.size()))// sometime the index is -1? 
					mActivity.mHistory.remove(index);
			}
			mActivity.updateHistory();
			mActivity.historyChanged = true;
		}
		
		if (!"".equals(bookmarks) && !",,,,".equals(bookmarks)) {
			String[] tmp2 = bookmarks.split(",,,,");
			for (int i = tmp2.length-1; i >= 0; i--) 
				try{mActivity.mBookMark.remove(Integer.valueOf(tmp2[i]) + 0);} catch(Exception e) {}// it will not treat as integer if not add 0
			mActivity.updateBookmark();
			mActivity.bookmarkChanged = true;
		}
	}
	
	@SuppressWarnings("unused")
	public void updateHome() {
		mActivity.updateHomePage();
	}
}
