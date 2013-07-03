package common.lib;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import base.lib.BaseApp;
import base.lib.WebUtil;
import base.lib.util;
import base.lib.WrapAdView;
import base.lib.WrapInterstitialAd;

public class MyApp extends BaseApp {
	final String HOME_PAGE = "file:///android_asset/home.html";
	final String HOME_BLANK = "about:blank";
	String browserName;
	String m_homepage = null;

	// Ads
	public FrameLayout adContainer;
	public LinearLayout adContainer2;	
	public WrapAdView adview = null, adview2 = null;
	WrapInterstitialAd interstitialAd = null;
	boolean clicked = false;
	static boolean mAdAvailable;
	static {
		try {
			Class.forName("com.google.ads.AdView");
			mAdAvailable = true;
		} catch (Throwable t) {
			mAdAvailable = false;
		}
	}

	class AppHandler extends Handler {
		public void handleMessage(Message msg) {
			if (msg.what > 0) {
				clicked = true;
				removeAd();
			}
			else if (msg.what == -2) {
				Bundle data = msg.getData();
				String errorMsg = data.getString("msg");
				if (errorMsg != null) Toast.makeText(mContext, "Can't load AdMob. " + errorMsg, Toast.LENGTH_LONG).show();
			}
			else if (msg.what == -3) {
				interstitialAd.loadAd();
			}
		}
	}
	AppHandler mAppHandler = new AppHandler();
	public DisplayMetrics dm;

	// settings
	public SharedPreferences sp;
	public Editor sEdit;

	public boolean showUrl = true;
	boolean showControlBar = true;
	final int urlHeight = 40, barHeight = 40;
	boolean showStatusBar = true;
	int rotateMode = 1;
	boolean incognitoMode = false;
	boolean updownButton = true;
	boolean snapFullWeb = false;
	boolean blockImage = false;
	boolean cachePrefer = false;
	boolean blockPopup = false;
	boolean blockJs = false;
	TextSize textSize = TextSize.NORMAL;
	final int historyCount = 16;
	long html5cacheMaxSize = 1024 * 1024 * 8;
	int ua = 0;
	int searchEngine = 3;
	int shareMode = 2;
	private int SETTING_RESULTCODE = 1002;
	boolean enableProxy = false;
	int localPort;
	boolean overviewPage = false;
	Locale mLocale;

	// bookmark and history
	public boolean historyChanged = false, bookmarkChanged = false, downloadsChanged = false;
	public boolean noSdcard = false;

	Activity mActivity;// the main activity
	// download related
	public Context mContext;
	public String downloadPath;
	public NotificationManager nManager;
	public HashMap<String, Integer> downloadAppID = new HashMap<String, Integer>();
	public HashMap<String, DownloadTask> downloadState = new HashMap<String, DownloadTask>();

	// browser related
	public ArrayList<MyWebView> serverWebs = new ArrayList<MyWebView>();
	public int webIndex;
	public ArrayList<TitleUrl> mHistory = new ArrayList<TitleUrl>();
	public ArrayList<TitleUrl> mBookMark = new ArrayList<TitleUrl>();
	public ArrayList<TitleUrl> mSystemHistory = new ArrayList<TitleUrl>();
	public ArrayList<TitleUrl> mSystemBookMark = new ArrayList<TitleUrl>();
	public ArrayList<TitleUrl> mDownloads = new ArrayList<TitleUrl>();
	ArrayList<String> siteArray = new ArrayList<String>();
	ArrayAdapter<String> urlAdapter;
	AutoCompleteTextView webAddress;
	ProgressBar loadProgress;
	ImageView imgNext, imgPrev, imgHome, imgRefresh, imgNew;
	WebAdapter webAdapter;
	LinearLayout webTools, webControl, urlLine;
	MyViewFlipper webpages;

	WrapValueCallback mUploadMessage;
	final public int FILECHOOSER_RESULTCODE = 1001;

	InputMethodManager imm;

	int revertCount = 0;
	boolean needRevert = false;
	class WebAdapter extends ArrayAdapter<MyWebView> {
		ArrayList localWeblist;

		public WebAdapter(Context context, List<MyWebView> webs) {
			super(context, 0, webs);
			localWeblist = (ArrayList) webs;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final MyWebView wv = (MyWebView) localWeblist.get(position);

			final LayoutInflater inflater = mActivity.getLayoutInflater();
			if (needRevert) 
				convertView = inflater.inflate(R.layout.revert_web_list, parent, false);
			else
				convertView = inflater.inflate(R.layout.web_list, parent, false);

			if (position == webIndex)
				convertView.setBackgroundColor(0x80ffffff);
			else
				convertView.setBackgroundColor(0);

			final ImageView btnIcon = (ImageView) convertView
					.findViewById(R.id.webicon);
			try {
				btnIcon.setImageBitmap(wv.getFavicon());
			} catch (Exception e) {
			}// catch an null pointer exception on 1.6}

			final TextView webname = (TextView) convertView
					.findViewById(R.id.webname);
			if ((wv.getTitle() != null) && (!"".equals(wv.getTitle())))
				webname.setText(wv.getTitle());
			else
				webname.setText(wv.m_url);

			webname.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					webControl.setVisibility(View.INVISIBLE);
					changePage(position);
				}
			});
			webname.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					CharSequence operations[];
					if (mAdAvailable) operations = new CharSequence[8];
					else operations = new CharSequence[10];

					operations[0] = getString(R.string.open);
					operations[1] = getString(R.string.shareurl); 
					operations[2] = getString(R.string.save);
					operations[3] = getString(R.string.copy_url); 
					operations[4] = getString(R.string.bookmark); 
					operations[5] = getString(R.string.remove_history);
					if (mAdAvailable) {
						operations[6] = getString(R.string.close);
						operations[7] = getString(R.string.close_all);
					}
					else {
						operations[6] = getString(R.string.set_homepage);
						operations[7] = getString(R.string.add_shortcut);
						operations[8] = getString(R.string.close);
						operations[9] = getString(R.string.close_all);
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(webname.getText());
					builder.setItems(operations, new DialogInterface.OnClickListener() {
					    @Override
					    public void onClick(DialogInterface dialog, int which) {
					    	switch(which) {
					    	case 0:// open
					    		webname.performClick();
					    		break;
					    	case 1:// share url
					    		shareUrl("", wv.m_url);
					    		break;
					    	case 2:// save
					    		startDownload(wv.m_url, "", "no");
					    		break;
					    	case 3:// copy url
								ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
								ClipMan.setText(wv.m_url);
					    		break;
					    	case 4:// bookmark
								String url = wv.m_url;
								if (HOME_PAGE.equals(url)) return;// not add home page
					    		addRemoveFavo(url, webname.getText().toString());
					    		break;
					    	case 5:// remove history
					    		for (int i = mHistory.size() - 1; i >= 0; i--)
					    			if (mHistory.get(i).m_url.equals(wv.m_url)) {
					    				removeHistory(i);
					    				break;
					    			}
					    		break;
					    	case 6:// close or set homepage
					    		if (mAdAvailable) closePage(position, false);
					    		else {
									m_homepage = wv.m_url;
									sEdit.putString("homepage", wv.m_url);
									sEdit.commit();
					    		}
					    		break;
					    	case 7:// close all or add shortcut
					    		if (mAdAvailable) {
					    			while (serverWebs.size() > 1) closePage(0, false);
					    			loadPage();
					    		}
					    		else 
									createShortcut(wv.m_url, webname.getText().toString());
					    		break;
					    	case 8:// close
					    		closePage(position, false);
					    		break;
					    	case 9:// close all
					    		while (serverWebs.size() > 1) closePage(0, false);
					    		loadPage();
					    		break;
					    	}
					    }
					});
					builder.show();					
					return true;
				}
			});

			ImageView btnStop = (ImageView) convertView
					.findViewById(R.id.webclose);
			btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					closePage(position, false);
				}
			});

			return convertView;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public void updateBookmark(){}
	public void updateHistory() {}
	public void updateHomePage() {}
	
	void loadPage() {// load home page
		serverWebs.get(webIndex).getSettings().setJavaScriptEnabled(true);

		WebBackForwardList wbfl = serverWebs.get(webIndex).copyBackForwardList();
		if (wbfl != null) {
			int size = wbfl.getSize();
			int current = wbfl.getCurrentIndex();
			for (int i = 0; i < size; i++) {
				if (HOME_PAGE.equals(wbfl.getItemAtIndex(i).getUrl())) {
					serverWebs.get(webIndex).goBackOrForward(i - current);
					return;
				}
			}
		}
		
		serverWebs.get(webIndex).loadUrl(HOME_PAGE);
	}

	void changePage(int position) {//identical
		while (webpages.getDisplayedChild() != position)
			webpages.showNext();
		for (int i = 0; i < serverWebs.size(); i++) {
			serverWebs.get(i).isForeground = false;
			serverWebs.get(i).freeMemory(serverWebs.get(i));
		}
		serverWebs.get(position).isForeground = true;
		webIndex = position;
		String url = serverWebs.get(webIndex).m_url;
		if (url == null) url = "";
		else if (HOME_PAGE.equals(url)) url = HOME_BLANK;
		webAddress.setText(url);// refresh the display url

		// global settings
		WebSettings localSettings = serverWebs.get(webIndex).getSettings();
		// localSettings.setBuiltInZoomControls(showZoom);
		if (ua <= 1)
			localSettings.setUserAgent(ua);
		else
			localSettings.setUserAgentString(WebUtil.selectUA(ua));
		localSettings.setTextSize(textSize);
		localSettings.setBlockNetworkImage(blockImage);
		if (cachePrefer)
			localSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		else
			localSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		localSettings.setJavaScriptCanOpenWindowsAutomatically(blockPopup);
		// localSettings.setSupportMultipleWindows(true);
		localSettings.setJavaScriptEnabled(!blockJs);
		//localSettings.setUseWideViewPort(overviewPage);
		new WrapWebSettings(localSettings).setLoadWithOverviewMode(overviewPage);

		if (serverWebs.get(position).mProgress > 0) {
			imgRefresh.setImageResource(R.drawable.stop);
			loadProgress.setVisibility(View.VISIBLE);
			loadProgress.setProgress(serverWebs.get(position).mProgress);
		} else {
			imgRefresh.setImageResource(R.drawable.refresh);
			loadProgress.setVisibility(View.INVISIBLE);
		}
	}

	void closePage(int position, boolean clearData) {//identical
		if (position == webIndex) {
			// remove current page, so stop loading at first
			serverWebs.get(webIndex).stopLoading();
			if (clearData) {
				serverWebs.get(webIndex).clearCache(true);
				serverWebs.get(webIndex).clearSslPreferences();
				serverWebs.get(webIndex).clearFormData();
			}
		}

		if (webAdapter.getCount() > 1) {
			MyWebView tmp = (MyWebView) webpages.getChildAt(position);
			if (tmp == null) return;//sometime it is null if close page very quick
			boolean toBefore = tmp.closeToBefore;
			webAdapter.remove(tmp);
			webAdapter.notifyDataSetInvalidated();
			try {
				webpages.removeViewAt(position);
			} catch (Exception e) {
			}// null pointer reported by 3 user. really strange.
			tmp.destroy();
			System.gc();
			imgNew.setImageBitmap(util.generatorCountIcon(
					util.getResIcon(getResources(), R.drawable.newpage),
					webAdapter.getCount(), 
					2,
					dm.density,
					mContext));// show the changed page number
			if ((position == webIndex) && !toBefore) {// change to the page after current page
				if (webIndex == webAdapter.getCount()) webIndex -= 1;
			}
			else if ((webIndex >= position) && (webIndex > 0)) webIndex -= 1;// change to previous page by default
		} else {// return to home page if only one page when click close button
			webControl.setVisibility(View.INVISIBLE);
			loadPage();
			webIndex = 0;
			//serverWebs.get(webIndex).clearHistory();// is that necessary to clear history?
		}
		changePage(webIndex);
		
		recordPages();
	}

	void setUrlHeight(boolean showUrlNow) {
		LayoutParams lpUrl = urlLine.getLayoutParams();
		if (showUrlNow) 
			lpUrl.height = LayoutParams.WRAP_CONTENT;
		else lpUrl.height = 0;
		urlLine.requestLayout();		
	}
	
	void setBarHeight(boolean showBarNow) {
		LayoutParams lpBar = webTools.getLayoutParams();
		if (showBarNow) 
			lpBar.height = LayoutParams.WRAP_CONTENT;
		else lpBar.height = 0;
		webTools.requestLayout();		
	}

	void removeAd() {
		if (adview != null) {
			//adContainer.removeViewAt(0);
			adview.destroy();
			adview = null;
		}
	}

	public String getSite(String url) {//identical
		String site = "";
		String[] tmp = url.split("/");
		// if url is http://m.baidu.com, then url.split("/")[2] is m.baidu.com
		if (tmp.length > 2)	site = tmp[2];
		else site = tmp[0];
		
		return site;
	}
	
	
	boolean startDownload(String url, String contentDisposition, String openAfterDone) {
		int posQ = url.indexOf("src=");
		if (posQ > 0) url = url.substring(posQ + 4);// get src part

		url = url.replace("%2D", "-");
        url = url.replace("%5F", "_");
        url = url.replace("%3F", "?");
        url = url.replace("%3D", "=");
        url = url.replace("%2E", ".");
        url = url.replace("%2F", "/");
        url = url.replace("%3A", ":");// replace %3A%2F%2F to :// if any for URLDecoder.decode(url) fail for some url, such as baidu tieba
		String apkName = WebUtil.getName(url);
		// image file from shuimu do not have ext. so we add it manually
		if (!apkName.contains(".")) {
			if (".jpg".equals(contentDisposition)) {
				apkName += ".jpg";
				contentDisposition = null;
			}
			else apkName += ".html";// if no ext, set as html file. maybe need consider contentDisposition.
		}
		else {// http://m.img.huxiu.com/portal/201304/18/171605dz0dp8yn0pu88zdy.jpg!278x80
			int index = apkName.lastIndexOf(".");
			String suffix = apkName.substring(index, apkName.length());
			if (".jpg".equals(contentDisposition) && suffix.startsWith(".jpg")) {
				apkName = apkName.replace(suffix, ".jpg");
			}
		}

		if (noSdcard)
			Toast.makeText(mContext, R.string.sdcard_needed, Toast.LENGTH_LONG).show();

		DownloadTask dl = downloadState.get(url);
		if (dl != null) {
			if (dl.pauseDownload) dl.pauseDownload = false;// resume download if it paused
			return true;// the file is downloading, not start a new download task.
		}

		Random random = new Random();
		int id = random.nextInt() + 1000;

		DownloadTask dltask = new DownloadTask();
		dltask.appstate = this;
		dltask.NOTIFICATION_ID = id;
		downloadState.put(url, dltask);
		dltask.execute(url, apkName, contentDisposition, openAfterDone);
		return true;
	}
	
	boolean openNewPage(String url, int newIndex, boolean changeToNewPage, boolean closeIfCannotBack) {//identical
		boolean result = true;

		if (webAdapter.getCount() == 9) {// max pages is 9
			Toast.makeText(mContext, R.string.nomore_pages, Toast.LENGTH_LONG).show();
			return false; // not open new page if got max pages
		} else {
			webAdapter.insert(new MyWebView(mContext, this), newIndex);
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

		return result;
	}
	
	void recordPages() {//identical
		try {// write opened url to /data/data/easy.browser/files/pages
			FileOutputStream fo = openFileOutput("pages", 0);
			ObjectOutputStream oos = new ObjectOutputStream(fo);
			for (int i = 0; i < serverWebs.size(); i++) {
				if (!HOME_PAGE.equals(serverWebs.get(i).m_url)) {
					oos.writeObject(serverWebs.get(i).m_url);
				}
			}
			oos.flush();
			oos.close();
			fo.close();
		} catch (Exception e) {}
	}
	
	void addRemoveFavo(String url, String title) {//identical
		for (int i = mBookMark.size() - 1; i >= 0; i--)
			if (mBookMark.get(i).m_url.equals(url)) {
				removeFavo(i);
				return;
			}

		addFavo(url, title);// add favo if not found it
	}
	
	void removeFavo(final int order) {//identical
		new AlertDialog.Builder(this)
				.setTitle(R.string.remove_bookmark)
				.setMessage(mBookMark.get(order).m_title)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {//index out of bound error reported by a few user
									mBookMark.remove(order);
									updateBookmark();
								} catch (Exception e) {}
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}

	void removeHistory(final int order) {//identical
		new AlertDialog.Builder(this)
				.setTitle(R.string.remove_history)
				.setMessage(mHistory.get(order).m_title)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {//index out of bound error reported by a few user
									mHistory.remove(order);
									updateHistory();
								} catch (Exception e) {}
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}

	// favo dialog
	EditText titleText;
	private void addFavo(final String url, final String title) {//identical
		if (url == null) {
			Toast.makeText(mContext, "null url", Toast.LENGTH_LONG).show();
			return;
		}

		LinearLayout favoView = (LinearLayout) mActivity.getLayoutInflater().inflate(
				R.layout.addfavo_browser, null);
		titleText = (EditText) favoView.findViewById(R.id.edit_favo);
		titleText.setText(title);
		titleText.setSelection(titleText.getText().length());

		// need user's confirm to add to bookmark
		new AlertDialog.Builder(this)
				.setView(favoView)
				.setMessage(url)
				.setTitle(R.string.add_bookmark)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String site = getSite(url);
								String title = titleText.getText().toString();
								// add a blank character to occupy the space
								if ("".equals(title)) title += (char) 0xa0;
								TitleUrl titleUrl = new TitleUrl(title, url, site);
								mBookMark.add(titleUrl);
								// sort by name
								Collections.sort(mBookMark, new MyComparator());
								updateBookmark();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();
	}
	
	void createShortcut(String url, String title) {//identical
		Intent i = new Intent(this, mActivity.getClass());
		i.setData(Uri.parse(url));
	    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		Intent shortcutIntent = new Intent();
	    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, i);
	    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
	    shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(mContext, R.drawable.explorer));
	    shortcutIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	    shortcutIntent.putExtra("duplicate", false); // Just create once
	    sendBroadcast(shortcutIntent);
	}
	
	private void shareUrl(String title, String url) {//identical
		if (title == null) title = "";
		if (url == null) url = "";
		
		Intent shareIntent = new Intent(Intent.ACTION_VIEW);
		shareIntent.setClassName(getPackageName(), mActivity.getClass().getName());
		Uri data = null;
		String from = "\n(from ";
		boolean chineseLocale = Locale.CHINA.equals(mLocale) || "easy.browser".equals(getPackageName());//easy.browser only release in China
				
		//if (shareMode != 1) scrollToMain();
		switch (shareMode) {
		case 2:// facebook or weibo
			if (chineseLocale)// weibo for chinese locale
				data = Uri.parse("http://v.t.sina.com.cn/share/share.php?url=" + url + "&title=" + title + "&appkey=3792856654&ralateUid=1877224203&source=bookmark");
			else // facebook for none chinese locale
				data = Uri.parse("http://www.facebook.com/sharer.php?u=" + url + "&t=" + title + from + browserName + ")");
			break;
		case 3:
			if (chineseLocale) {// qzone for chinese locale 
				if ("".equals(title)) title = url;
				data = Uri.parse("http://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?url=" + url + "&desc=" + title + "&title" + title + "&site=" + browserName);
			}
			else // twitter for none chinese locale
				data = Uri.parse("http://twitter.com/intent/tweet?url=" + url + "&text=" + title + from + browserName + ")");
			break;
		case 4:
			if (chineseLocale) // tencent weibo for chinese localse
				data = Uri.parse("http://share.v.t.qq.com/index.php?c=share&a=index&url=" + url + "&title=" + title + url + from + browserName + ")");
			else // google+ for none chinese locale
				data = Uri.parse("https://plusone.google.com/_/+1/confirm?hl=en&url=" + url);
			break;
		case 1:
		default:
			if (!"".equals(title)) title = title + "\n";
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
			intent.putExtra(Intent.EXTRA_TEXT, title + url + from + browserName + ")");
			util.startActivity(
					Intent.createChooser(intent, getString(R.string.sharemode)),
					true, mContext);
			return;
		}
		
		shareIntent.setData(data);
		util.startActivity(shareIntent, false, mContext);
	}
}
