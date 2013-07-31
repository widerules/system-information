package common.lib;

import java.net.URLDecoder;
import java.util.Locale;

import easy.lib.MyListAdapter;
import easy.lib.R;
import base.lib.WrapAdView;
import base.lib.WrapInterstitialAd;
import base.lib.util;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebBackForwardList;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class HarleyApp extends MyApp {
	public View bookmarkDownloads;
	public int bookmarkWidth = LayoutParams.WRAP_CONTENT;
	public int menuWidth = LayoutParams.WRAP_CONTENT;
	public boolean menuOpened = true;
	public boolean bookmarkOpened = true;
	
	public GridView menuGrid = null;
	public LinearLayout bookmarkView;
	public ListView downloadsList;
	MyListAdapter bookmarkAdapter, historyAdapter, downloadsAdapter;
	int minWebControlWidth = 200;

	public void loadPage() {// load home page
		super.loadPage();
		if ((mBookMark.size() > 0) || (mHistory.size() > 0)) showBookmark();// show bookmark for load home page too slow
	}

	public void shareUrl(String title, String url) {
		super.shareUrl(title, url);

		if (shareMode != 1) scrollToMain();
	}
	
	void hideMenu() {
		menuGrid.getLayoutParams().width = 0;
		menuGrid.requestLayout();
		menuOpened = false;
	}
	
	void hideBookmark() {
		bookmarkDownloads.getLayoutParams().width = 0;
		bookmarkDownloads.requestLayout();
		bookmarkView.setVisibility(View.VISIBLE);
		if (downloadsList != null) downloadsList.setVisibility(View.GONE);
		bookmarkOpened = false;
	}

	void showBookmark() {
		bookmarkDownloads.getLayoutParams().width = bookmarkWidth;
		bookmarkDownloads.requestLayout();
		bookmarkOpened = true;
		if (dm.widthPixels-menuWidth-bookmarkWidth < minWebControlWidth) {
			webControl.setVisibility(View.GONE);
			hideMenu();
		}
		if (bookmarkAdapter == null) initBookmarks();
	}
	
	public void scrollToMain() {
		if (bookmarkOpened) hideBookmark();		
		if (menuOpened) hideMenu(); 
	}
	
	public void initBookmarks() {
		bookmarkAdapter = new MyListAdapter(mContext, mBookMark);
		bookmarkAdapter.type = 0;
		ListView bookmarkList = (ListView) findViewById(R.id.bookmark);
		bookmarkList.inflate(mContext, R.layout.web_list, null);
		bookmarkList.setAdapter(bookmarkAdapter);
		bookmarkList.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					ListView lv = (ListView) v;
					serverWebs.get(webIndex).loadUrl(mBookMark.get(lv.getSelectedItemPosition()).m_url);
					imgBookmark.performClick();
				}
				return false;
			}
		});

		historyAdapter = new MyListAdapter(mContext, mHistoryForAdapter);
		historyAdapter.type = 1;
		historyList = (ListView) findViewById(R.id.history);
		historyList.inflate(mContext, R.layout.web_list, null);
		historyList.setAdapter(historyAdapter);
		historyList.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					ListView lv = (ListView) v;
					serverWebs.get(webIndex).loadUrl(mHistory.get(mHistory.size() - 1 - lv.getSelectedItemPosition()).m_url);
					imgBookmark.performClick();
				}
				return false;
			}
		});
		
		updateHistory();
	}
	
	public void readPreference() {
		super.readPreference();
		
		if (Locale.CHINA.equals(mLocale)) 
			HOME_PAGE = "file:///android_asset/home-ch.html";
		
		m_homepage = sp.getString("homepage", null);
	}
	
	public void createAd() {
		if (adview != null) return;// only create ad for one time.
		
		if (mAdAvailable) {
			adview = new WrapAdView(mActivity, 0, "a1502880ce4208b", null);// AdSize.BANNER require 320*50
			if ((adview != null) && (adview.getInstance() != null)) {
				adContainer.addView(adview.getInstance());
				adview.loadAd();
			}
			
			adview2 = new WrapAdView(mActivity, 0, "a1517e34883f8ce", null);// AdSize.BANNER require 320*50
			if ((adview2 != null) && (adview2.getInstance() != null)) {
				adContainer2.addView(adview2.getInstance());
				adview2.loadAd();
			}
			
			interstitialAd = new WrapInterstitialAd(mActivity, "a14be3f4ec2bb11", mAppHandler);
		}
	}
	
	public void updateDownloads() {
		if (downloadsAdapter != null) {
			downloadsAdapter.notifyDataSetChanged();
		}
		downloadsChanged = true;
	}
	
	public void updateBookmark() {
		if (bookmarkAdapter != null) {
			bookmarkAdapter.notifyDataSetChanged();
			updateHistoryViewHeight();
		}
		bookmarkChanged = true;
	}
	
	public void updateHistory() {
		if (historyAdapter != null) {
			updateHistoryViewHeight();
			
			mHistoryForAdapter.clear();
			for (int i = mHistory.size()-1; i >= 0; i--)
				mHistoryForAdapter.add(mHistory.get(i));
			historyAdapter.notifyDataSetChanged();
		}
		historyChanged = true;
	}
	
	public boolean openNewPage(String url, int newIndex, boolean changeToNewPage, boolean closeIfCannotBack) {
		boolean weblink = super.openNewPage(url, newIndex, changeToNewPage, closeIfCannotBack);
		if (!weblink) return false;

		if (webAdapter.getCount() == 9) {// max pages is 9
			Toast.makeText(mContext, R.string.nomore_pages, Toast.LENGTH_LONG).show();
			return false; // not open new page if got max pages
		} else {
			webAdapter.insert(new HarleyWebView(mContext, this), newIndex);
			webAdapter.notifyDataSetInvalidated();
			webpages.addView(webAdapter.getItem(newIndex), newIndex);
			imgNew.setImageBitmap(util.generatorCountIcon(
					util.getResIcon(getResources(), R.drawable.newpage),
					webAdapter.getCount(), 
					2, 
					dm.density,
					mContext));
			if (changeToNewPage) changePage(newIndex);
			else serverWebs.get(newIndex).isForeground = false;
			serverWebs.get(newIndex).closeToBefore = changeToNewPage;
			serverWebs.get(newIndex).shouldCloseIfCannotBack = closeIfCannotBack;
		}

		if (url != null) {
			if ("".equals(url)) loadPage();
			// else if (url.endsWith(".pdf"))//can't open local pdf by google
			// doc
			// serverWebs.get(webIndex).loadUrl("http://docs.google.com/gview?embedded=true&url="
			// + url);
			else {
				try {url = URLDecoder.decode(url);} catch (Exception e) {}
				serverWebs.get(newIndex).loadUrl(url);
			}
		}

		return true;
	}
}
