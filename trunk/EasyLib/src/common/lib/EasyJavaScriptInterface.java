package common.lib;

import common.lib.MyJavaScriptInterface;

public class EasyJavaScriptInterface extends MyJavaScriptInterface {
	EasyJavaScriptInterface(EasyWebView webview, EasyApp appstate) {
		super(webview, appstate);
	}

	@SuppressWarnings("unused")
	public void processReady(String ready) {
		mWebView.m_ready = ready;
	}
	
	@SuppressWarnings("unused")
	public void saveCollapseState(String item, boolean state) {
		if ("1".equals(item))
			((EasyApp)mAppstate).collapse1 = state;
		if ("2".equals(item))
			((EasyApp)mAppstate).collapse2 = state;
		if ("3".equals(item))
			((EasyApp)mAppstate).collapse3 = state;
	}
	
	@SuppressWarnings("unused")
	public void deleteItems(String bookmarks, String historys) {
		if (!"".equals(historys) && !",,,,".equals(historys)) {
			String[] tmp1 = historys.split(",,,,");
			for (int i = 0; i < tmp1.length; i++) {// the display of history is in revert order, so delete in revert order
				int index = mAppstate.mHistory.size() - 1 + i - (Integer.valueOf(tmp1[i]) - mAppstate.mBookMark.size());
				if ((index >= 0) && (index < mAppstate.mHistory.size()))// sometime the index is -1? 
					mAppstate.mHistory.remove(index);
			}
			mAppstate.updateHistory();
			mAppstate.historyChanged = true;
		}
		
		if (!"".equals(bookmarks) && !",,,,".equals(bookmarks)) {
			String[] tmp2 = bookmarks.split(",,,,");
			for (int i = tmp2.length-1; i >= 0; i--) 
				try{mAppstate.mBookMark.remove(Integer.valueOf(tmp2[i]) + 0);} catch(Exception e) {}// it will not treat as integer if not add 0
			mAppstate.updateBookmark();
			mAppstate.bookmarkChanged = true;
		}
	}
	
	@SuppressWarnings("unused")
	public void updateHome() {
		mAppstate.updateHomePage();
	}
}
