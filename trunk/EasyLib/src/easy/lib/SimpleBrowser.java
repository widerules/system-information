package easy.lib;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import common.lib.DownloadTask;
import common.lib.MyApp;
import common.lib.MyComparator;
import common.lib.MyViewFlipper;
import common.lib.ProxySettings;
import common.lib.TitleUrl;
import base.lib.stringComparator;
import base.lib.util;
import base.lib.wrapAdView;
import base.lib.wrapInterstitialAd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ZoomButtonsController;

//for get webpage source on cupcake
class wrapValueCallback {
	ValueCallback<Uri> mInstance;

	wrapValueCallback() {}

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

public class SimpleBrowser extends Activity {

	String HOME_PAGE = "file:///android_asset/home.html";
	final String HOME_BLANK = "about:blank";
	String m_homepage = null;
	boolean firstRun = false;
	int countDown = 0;

	ListView webList;
	int revertCount = 0;
	boolean needRevert = false;

	Context mContext;
	String browserName;

	//boolean flashInstalled = false;
	// settings
	boolean showUrl = true;
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
	boolean collapse1 = false, collapse2 = false, collapse3 = true;// default open top list and bookmark
	TextSize textSize = TextSize.NORMAL;
	final int historyCount = 16;
	long html5cacheMaxSize = 1024 * 1024 * 8;
	int ua = 0;
	int searchEngine = 3;
	int shareMode = 2;
	private int SETTING_RESULTCODE = 1002;
	boolean enableProxy = false;
	int localPort;
	// boolean hideExit = true;
	boolean overviewPage = false;
	Locale mLocale;

	SharedPreferences sp;
	Editor sEdit;

	// search
	EditText etSearch;
	TextView searchHint;
	RelativeLayout searchBar;
	ImageView imgSearchNext, imgSearchPrev, imgSearchClose;
	String toSearch = "";
	int matchCount = 0, matchIndex = 0;

	// snap dialog
	ImageView snapView;
	Bitmap bmp;
	AlertDialog snapDialog = null;

	// favo dialog
	EditText titleText;

	// menu dialog
	AlertDialog menuDialog;// menu Dialog
	GridView menuGrid;
	View menuView;
	int historyIndex = -1;
	AlertDialog downloadsDialog = null;

	// page up and down button
	LinearLayout upAndDown;
	ImageView upButton, downButton;

	// browser related
	AutoCompleteTextView webAddress;
	ArrayAdapter<String> urlAdapter;
	ArrayList<String> siteArray;

	ArrayList<MyWebview> serverWebs = new ArrayList<MyWebview>();
	int webIndex;
	MyViewFlipper webpages;
	ImageView imgNext, imgPrev, imgHome, imgRefresh, imgNew;
	WebAdapter webAdapter;
	LinearLayout webTools, webControl, urlLine;
	LinearLayout imageBtnList;
	LinearLayout adContainer2;	
	RelativeLayout webs;
	Button btnNewpage;
	InputMethodManager imm;
	ProgressBar loadProgress;

	ConnectivityManager cm;

	// upload related
	static boolean mValueCallbackAvailable;
	static {
		try {
			wrapValueCallback.checkAvailable();
			mValueCallbackAvailable = true;
		} catch (Throwable t) {
			mValueCallbackAvailable = false;
		}
	}
	private wrapValueCallback mUploadMessage;
	private int FILECHOOSER_RESULTCODE = 1001;

	// bookmark and history
	AlertDialog m_sourceDialog = null;
	ArrayList<TitleUrl> mHistory = new ArrayList<TitleUrl>();
	ArrayList<TitleUrl> mBookMark = new ArrayList<TitleUrl>();
	ArrayList<TitleUrl> mSystemHistory = new ArrayList<TitleUrl>();
	ArrayList<TitleUrl> mSystemBookMark = new ArrayList<TitleUrl>();
	boolean historyChanged = false, bookmarkChanged = false;
	ImageView imgAddFavo, imgGo;
	boolean noSdcard = false, noHistoryOnSdcard = false;

	// baidu tongji
	static Method baiduResume = null;
	static Method baiduPause = null;
	static Method baiduEvent = null;
	static {
		try {
			Class c = Class.forName("com.baidu.mobstat.StatService");
			baiduResume = c.getMethod("onResume", new Class[] { Context.class });
			baiduPause = c.getMethod("onPause", new Class[] { Context.class });
			baiduEvent = c.getMethod("onEvent", new Class[] { Context.class, String.class, String.class });
		} catch (Exception e) {}
	}
	
	// ad
	static boolean mAdAvailable;
	static {
		try {
			Class.forName("com.google.ads.AdView");
			mAdAvailable = true;
		} catch (Throwable t) {
			mAdAvailable = false;
		}
	}
	wrapAdView adview = null, adview2 = null;
	wrapInterstitialAd interstitialAd = null;
	DisplayMetrics dm;
	FrameLayout adContainer;
	AppHandler mAppHandler = new AppHandler();
	boolean clicked = false;
	boolean gotoSettings = false;// will set to true if open settings activity,
									// and set to false again after exit
									// settings. not remove ad is goto settings
	
	String dataPath = "";
	MyApp appstate;

	static Method freeMemoryMethod = null;
	static Method setLoadWithOverviewMode = null;
	static Method setAppCacheEnabled = null;
	static Method setAppCachePath = null;
	static Method setAppCacheMaxSize = null;
	static Method setDomStorageEnabled = null;
	static Method setDatabaseEnabled = null;
	static Method setDatabasePath = null;
	static Method setDisplayZoomControls = null;
	static Method setScrollbarFadingEnabled = null;
	static Method canScrollVerticallyMethod = null;
	static Method setForceUserScalable = null;
	static {
		try {
			//API 5
			setDatabaseEnabled = WebSettings.class.getMethod("setDatabaseEnabled", new Class[] { boolean.class });
			setDatabasePath = WebSettings.class.getMethod("setDatabasePath", new Class[] { String.class });
			setScrollbarFadingEnabled = WebView.class.getMethod("setScrollbarFadingEnabled", new Class[] { boolean.class });
			
			//API 7
			freeMemoryMethod  = WebView.class.getMethod("freeMemory");
			setLoadWithOverviewMode = WebSettings.class.getMethod("setLoadWithOverviewMode", new Class[] { boolean.class });
			setAppCacheEnabled = WebSettings.class.getMethod("setAppCacheEnabled", new Class[] { boolean.class });
			setAppCachePath = WebSettings.class.getMethod("setAppCachePath", new Class[] { String.class });
			setAppCacheMaxSize = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[] { long.class });
			setDomStorageEnabled = WebSettings.class.getMethod("setDomStorageEnabled", new Class[] { boolean.class });
			
			//API 11
			setDisplayZoomControls = WebSettings.class.getMethod("setDisplayZoomControls", new Class[] { boolean.class });
			
			//API 14
			canScrollVerticallyMethod = WebView.class.getMethod("canScrollVertically", new Class[] { int.class });
			setForceUserScalable = WebSettings.class.getMethod("setForceUserScalable", new Class[] { boolean.class });
		} catch (Exception e) {}
	}
	
	public void freeMemory(MyWebview webview) {
		if (freeMemoryMethod != null)
			try {freeMemoryMethod.invoke(webview);} catch (Exception e) {}
	}

	class wrapWebSettings {
		WebSettings mInstance;

		wrapWebSettings(WebSettings settings) {
			mInstance = settings;
		}

		synchronized void setLoadWithOverviewMode(boolean overview) {// API 7
			if (setLoadWithOverviewMode != null)
				try {setLoadWithOverviewMode.invoke(mInstance, overview);} catch (Exception e) {}
		}

		synchronized void setAppCacheEnabled(boolean flag) {// API 7
			if (setAppCacheEnabled != null)
				try {setAppCacheEnabled.invoke(mInstance, flag);} catch (Exception e) {}
		}

		synchronized void setAppCachePath(String databasePath) {// API 7
			if (setAppCachePath != null)
				try {setAppCachePath.invoke(mInstance, databasePath);} catch (Exception e) {}
		}

		synchronized void setAppCacheMaxSize(long max) {// API 7
			if (setAppCacheMaxSize != null)
				try {setAppCacheMaxSize.invoke(mInstance, max);} catch (Exception e) {}
		}

		synchronized void setDomStorageEnabled(boolean flag) {// API 7
			if (setDomStorageEnabled != null)
				try {setDomStorageEnabled.invoke(mInstance, flag);} catch (Exception e) {}
		}

		synchronized void setDatabaseEnabled(boolean flag) {// API 5
			if (setDatabaseEnabled != null)
				try {setDatabaseEnabled.invoke(mInstance, flag);} catch (Exception e) {}
		}

		synchronized void setDatabasePath(String databasePath) {// API 5
			if (setDatabasePath != null)
				try {setDatabasePath.invoke(mInstance, databasePath);} catch (Exception e) {}
		}

		synchronized boolean setDisplayZoomControls(boolean enabled) {// API 11
			if (setDisplayZoomControls != null)
				try {
					setDisplayZoomControls.invoke(mInstance, enabled);
					return true;
				} catch (Exception e) {}
			return false;
		}
		
		synchronized void setForceUserScalable(boolean flag) {// API 14
			if (setForceUserScalable != null)
				try {setForceUserScalable.invoke(mInstance, flag);} catch (Exception e) {}
		}
	}

	class MyWebview extends WebView {
		public String pageSource = "", m_url = "";

		wrapWebSettings webSettings;

		ZoomButtonsController mZoomButtonsController;
		boolean zoomVisible = false;
		boolean html5 = false;

		int mProgress = 0;
		boolean isForeground = true;
		boolean closeToBefore = true;
		String m_ready = "";
		boolean shouldCloseIfCannotBack = false;

		public boolean myCanScrollVertically(int direction) {// API 14
			if (canScrollVerticallyMethod != null)
				try {
					Object o = canScrollVerticallyMethod.invoke(this, direction);
					return "true".equals(o.toString());
				} catch(Exception e) {}
			
			return true;
		}
		
		public void getPageSource() {// to get page source, part 3
			loadUrl("javascript:window.JSinterface.processHTML(document.getElementsByTagName('html')[0].innerHTML);");
		}
		
		public void getPageReadyState() {// title1 will not be empty if ready
			loadUrl("javascript:window.JSinterface.processReady(document.getElementById('title1').innerHTML);");
		}

		class MyJavaScriptInterface {
			@SuppressWarnings("unused")
			public void processHTML(String html) {
				pageSource = html;// to get page source, part 1
			}
			
			@SuppressWarnings("unused")
			public void processReady(String ready) {
				m_ready = ready;
			}
			
			@SuppressWarnings("unused")
			public void saveCollapseState(String item, boolean state) {
				if ("1".equals(item))
					collapse1 = state;
				if ("2".equals(item))
					collapse2 = state;
				if ("3".equals(item))
					collapse3 = state;
			}
			
			@SuppressWarnings("unused")
			public void deleteItems(String bookmarks, String historys) {
				if (!"".equals(historys) && !",,,,".equals(historys)) {
					String[] tmp1 = historys.split(",,,,");
					for (int i = 0; i < tmp1.length; i++) {// the display of history is in revert order, so delete in revert order
						int index = mHistory.size() - 1 + i - (Integer.valueOf(tmp1[i]) - mBookMark.size());
						if ((index >= 0) && (index < mHistory.size()))// sometime the index is -1? 
							mHistory.remove(index);
					}
					updateHistory();
					historyChanged = true;
				}
				
				if (!"".equals(bookmarks) && !",,,,".equals(bookmarks)) {
					String[] tmp2 = bookmarks.split(",,,,");
					for (int i = tmp2.length-1; i >= 0; i--) 
						try{mBookMark.remove(Integer.valueOf(tmp2[i]) + 0);} catch(Exception e) {}// it will not treat as integer if not add 0
					updateBookmark();
					bookmarkChanged = true;
				}
			}
			
			@SuppressWarnings("unused")
			public void updateHome() {
				updateHomePage();
			}
			
			@SuppressWarnings("unused")
			public void showInterstitialAd() {
				if (interstitialAd.isReady()) interstitialAd.show();
				else {
					Toast.makeText(mContext, "Admob is loading", Toast.LENGTH_LONG).show();
					Message fail = mAppHandler.obtainMessage();
					fail.what = -3;
					mAppHandler.sendMessage(fail);
				}
			}
		}

		public void setZoomControl(int visibility) {
			Class<?> classType;
			Field field;
			try {
				classType = WebView.class;
				field = classType.getDeclaredField("mZoomButtonsController");
				field.setAccessible(true);
				if (visibility == View.GONE) {
					mZoomButtonsController = (ZoomButtonsController) field
							.get(this);// backup the original zoom
										// controller
					ZoomButtonsController myZoomButtonsController = new ZoomButtonsController(
							this);
					myZoomButtonsController.getZoomControls()
							.setVisibility(visibility);
					field.set(this, myZoomButtonsController);
					zoomVisible = false;
				} else {
					field.set(this, mZoomButtonsController);
					zoomVisible = true;
				}
			} catch (Exception e) {}
		}

		@Override
		public boolean onTouchEvent(MotionEvent ev) {// onTouchListener may not
														// work. so put relate
														// code here
			if (ev.getAction() == 0) {// touch down
				if (webControl.getVisibility() == View.VISIBLE)// close webcontrol page if it is open.
					webControl.setVisibility(View.INVISIBLE);
								
				if (!showUrl) setUrlHeight(false);
				if (!showControlBar) setBarHeight(false);

				if (!this.isFocused()) {
					this.setFocusableInTouchMode(true);
					this.requestFocus();
					webAddress.setFocusableInTouchMode(false);
					webAddress.clearFocus();
				}
			}
			
			//requestDisallowInterceptTouchEvent(true);
			return super.onTouchEvent(ev);
		}

		public MyWebview(Context context) {
			super(context);

			setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);// no white blank on the right of webview
			
			try {
				// hide scroll bar when not scroll. from API5, not work on cupcake.
				setScrollbarFadingEnabled.invoke(this, true);
			} catch (Exception e) {}

			WebSettings localSettings = getSettings();
			localSettings.setSaveFormData(true);
			localSettings.setTextSize(textSize);
			localSettings.setSupportZoom(true);
			localSettings.setBuiltInZoomControls(true);

			// otherwise can't scroll horizontal in some webpage, such as qiupu.
			localSettings.setUseWideViewPort(true);

			localSettings.setPluginsEnabled(true);
			// localSettings.setPluginState(WebSettings.PluginState.ON);
			// setInitialScale(1);
			localSettings.setSupportMultipleWindows(true);
			localSettings.setJavaScriptCanOpenWindowsAutomatically(blockPopup);
			localSettings.setBlockNetworkImage(blockImage);
			if (cachePrefer)
				localSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
			localSettings.setJavaScriptEnabled(!blockJs);

			if (ua <= 1)
				localSettings.setUserAgent(ua);
			else
				localSettings.setUserAgentString(selectUA(ua));

			webSettings = new wrapWebSettings(localSettings);
			if (!webSettings.setDisplayZoomControls(false)) // hide zoom button
															// by default on API
															// 11 and above
				setZoomControl(View.GONE);// default not show zoom control in
											// new page

			// webSettings.setDefaultZoom(ZoomDensity.MEDIUM);//start from API7

			webSettings.setDomStorageEnabled(true);// API7, key to enable gmail

			// loads the WebView completely zoomed out. fit for hao123, but not
			// fit for homepage. from API7
			webSettings.setLoadWithOverviewMode(overviewPage);
			webSettings.setForceUserScalable(true);

			registerForContextMenu(this);

			// to get page source, part 2
			addJavascriptInterface(new MyJavaScriptInterface(), "JSinterface");

			setDownloadListener(new DownloadListener() {
				@Override
				public void onDownloadStart(final String url, String ua,
						final String contentDisposition, String mimetype,
						long contentLength) {
					downloadAction(url, contentDisposition, mimetype);
				}
			});

			setWebChromeClient(new WebChromeClient() {
				boolean updated = false;
				
				@Override
				public void onProgressChanged(WebView view, int progress) {
					if (progress == 100) mProgress = 0;
					else mProgress = progress;
					
					if (HOME_PAGE.equals(view.getUrl())) {
						getPageReadyState();
						// the progress is not continuous from 0, 1, 2... 100. it always looks like 10, 13, 15, 16, 100
						if ("".equals(m_ready)) //must update the page on after some progress(like 13), other wise it will not update success
							Log.d("=================", progress+"");
							updateHomePage();
					}

					if (isForeground) {
						loadProgress.setProgress(progress);
						if (progress == 100)
							loadProgress.setVisibility(View.INVISIBLE);
					}
				}

				// For Android 3.0+
				public void openFileChooser(ValueCallback<Uri> uploadMsg,
						String acceptType) {
					if (null == mUploadMessage)
						mUploadMessage = new wrapValueCallback();
					mUploadMessage.mInstance = uploadMsg;
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.addCategory(Intent.CATEGORY_OPENABLE);
					i.setType("*/*");
					startActivityForResult(Intent.createChooser(i,
							getString(R.string.select_file)),
							FILECHOOSER_RESULTCODE);
				}

				// For Android < 3.0
				public void openFileChooser(ValueCallback<Uri> uploadMsg) {
					openFileChooser(uploadMsg, "");
				}

				@Override
				public boolean onCreateWindow(WebView view, boolean isDialog,
						boolean isUserGesture, android.os.Message resultMsg) {
					if (openNewPage(null, webIndex+1, true, true)) {// open new page success
						((WebView.WebViewTransport) resultMsg.obj)
								.setWebView(serverWebs.get(webIndex));
						resultMsg.sendToTarget();
						return true;
					} else return false;
				}
			});

			setWebViewClient(new WebViewClient() {
				@Override
				public void onReceivedSslError(WebView view,
						SslErrorHandler handler, SslError error) {
					// accept ssl certification whenever needed.
					if (handler != null) handler.proceed();
				}

				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					super.onPageStarted(view, url, favicon);
					m_url = url;
					pageSource = "";
					
					if (HOME_PAGE.equals(url)) view.getSettings().setJavaScriptEnabled(true);// not block js on homepage
					else view.getSettings().setJavaScriptEnabled(!blockJs);


					if (isForeground) {
						// close soft keyboard
						imm.hideSoftInputFromWindow(getWindowToken(), 0);
						loadProgress.setVisibility(View.VISIBLE);
						
						if (HOME_PAGE.equals(url)) webAddress.setText(HOME_BLANK);
						else webAddress.setText(url);
						
						if ((adview != null) && (adContainer2.getVisibility() != View.GONE)) adview.loadAd();// the refresh rate set by server side may not work. so we refresh by ourself
						else if ((adview2 != null) && (adContainer2.getVisibility() != View.GONE)) adview2.loadAd();
						if (interstitialAd != null && !interstitialAd.isReady()) interstitialAd.loadAd();
						
						imgRefresh.setImageResource(R.drawable.stop);

						if (!showUrl) setUrlHeight(true);
						if (!showControlBar) setBarHeight(true);
					}

					try {if (baiduEvent != null) baiduEvent.invoke(mContext, mContext, "1", url);
					} catch (Exception e) {}
					
					if (!incognitoMode) recordPages();
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					pageSource = "";// prevent get incomplete page source during page loading
					m_url = url;// must sync the url for it may change after pagestarted.
					mProgress = 0;
					pageFinishAction(view, url, isForeground);
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (HOME_BLANK.equals(url)) {// some site such as weibo and
						// mysilkbaby will send
						// BLANK_PAGE when login.
						return true;// we should do nothing but return true,
									// otherwise may not login.
					} else if (!url.startsWith("http") && !url.startsWith("file")) {
						overloadAction(url);
						return true; // not allow webpage to proceed
					} else if ("file:///sdcard/".equals(url)) {
						Intent intent = new Intent("android.intent.action.GET_CONTENT");
						intent.setType("file/*");
						util.startActivity(intent, true, mContext);
						return true;
					} else
						return false;
				}
			});
		}
	}

	void downloadAction(final String url, final String contentDisposition, String mimetype) {
		// need to know it is httpget, post or direct connect.
		// for example, I don't know how to handle this
		// http://yunfile.com/file/murongmr/5a0574ad/. firefox think
		// it is post.
		// url:
		// http://dl33.yunfile.com/file/downfile/murongmr/876b15e4/c7c3002a
		// ua: Mozilla/5.0 (Linux; U; Android 2.3.3; en-us; sdk
		// Build/GRI34) AppleWebKit/533.1 (KHTML, like Gecko)
		// Version/4.0 Mobile Safari/533.1
		// contentDisposition: attachment;
		// filename*="utf-8''CHUN%E5%85%89%E8%BC%9D%E8%8D%92%E9%87%8E.rar"
		// mimetype: application/octet-stream
		// contentLength: 463624
		boolean canOpen = false;
		String apkName = getName(url);
		if ((mimetype == null) || ("".equals(mimetype))) {
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String ext = apkName.substring(apkName.lastIndexOf(".")+1, apkName.length());
			mimetype = mimeTypeMap.getMimeTypeFromExtension(ext);
		}
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		if ((mimetype != null) && (!mimetype.equals("")) && (!mimetype.equals("application/vnd.android.package-archive")) && (!mimetype.equals("audio/mpeg"))) {
			//show chooser if it can open, otherwise download it.
			//download apk and mp3 directly without confirm. 
			intent.setDataAndType(Uri.parse(url), mimetype);
			List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
			if ((list != null) && !list.isEmpty()) canOpen = true;
		}

		if (canOpen) {
			try {
			new AlertDialog.Builder(mContext)
			.setTitle(getString(R.string.choose))
			.setMessage(apkName)
			.setPositiveButton(getString(R.string.open),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							if (!util.startActivity(intent, false, mContext))
								startDownload(url, contentDisposition, "yes");//download if open fail
						}
					})
			.setNeutralButton(getString(R.string.download),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							startDownload(url, contentDisposition, "yes");
						}
					})
			.setNegativeButton(getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					}).show();} catch(Exception e) {}
		} else startDownload(url, contentDisposition, "yes");
	}
	
	void overloadAction(String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.addCategory(Intent.CATEGORY_BROWSABLE);
		String data = intent.getDataString();
		if (!"".equals(data) && (data.startsWith("vnd.youtube"))) {
			if (!util.startActivity(intent, false, mContext)) {
				try {
				new AlertDialog.Builder(mContext)
				.setMessage("You need install plugin or client to play video.")
				.setPositiveButton("Youtube",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri
										.parse("market://details?id=com.google.android.youtube"));
								util.startActivity(intent, false, mContext);
							}
						})
				.setNeutralButton("Adobe flash",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(Intent.ACTION_VIEW, Uri
										.parse("market://details?id=com.adobe.flashplayer"));
								util.startActivity(intent, false, mContext);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).show();} catch(Exception e) {}
			}
		}
		else util.startActivity(intent, true, mContext);
	}
	
	void pageFinishAction(WebView view, String url, boolean isForeground) {
		if (isForeground) {
			// hide progressbar anyway
			loadProgress.setVisibility(View.INVISIBLE);
			imgRefresh.setImageResource(R.drawable.refresh);
			webControl.setVisibility(View.INVISIBLE);
			if (HOME_PAGE.equals(url)) webAddress.setText(HOME_BLANK);
			else webAddress.setText(url);						
		}
		// update the page title in webList
		webAdapter.notifyDataSetChanged();

		String title = view.getTitle();
		if (title == null) title = url;

		if (HOME_PAGE.equals(url)) ;// do nothing
		else {
			if (browserName.equals(title)) ;
				// if title and url not sync, then sync it
				//webAddress.setText(HOME_BLANK);
			else if (!incognitoMode) {// handle the bookmark/history after load new page
				if ((mHistory.size() > 0) && (mHistory.get(mHistory.size() - 1).m_url.equals(url))) return;// already the latest, no need to update history list

				String site = getSite(url);
				TitleUrl titleUrl = new TitleUrl(title, url, site);
				mHistory.add(titleUrl);// always add it to history if visit any page.
				historyChanged = true;

				for (int i = mHistory.size() - 2; i >= 0; i--) {
					if (mHistory.get(i).m_url.equals(url)) {
						if (title.equals(url)) {// use meaningful title to replace title with url content
							String meaningfulTitle = mHistory.get(i).m_title;
							if (!meaningfulTitle.equals(url)) 
								mHistory.set(mHistory.size()-1, mHistory.get(i));
						}
						mHistory.remove(i);// record one url only once in the history list. clear old duplicate history if any
						return;
					} 
					else if (title.equals(mHistory.get(i).m_title)) {
						mHistory.remove(i);// only keep the latest history of the same title. display multi item with same title is not useful to user
						break;
					}
				}

				if (siteArray.indexOf(site) < 0) {
					// update the auto-complete edittext without duplicate
					urlAdapter.add(site);
					// the adapter will always
					// return 0 when get count
					// or search, so we use an
					// array to store the site.
					siteArray.add(site);
				}

				try {// try to open the png, if can't open, then need save
					FileInputStream fis = openFileInput(site + ".png");
					try {fis.close();} catch (IOException e) {}
				} catch (Exception e1) {
					try {// save the Favicon
						if (view.getFavicon() != null) {
							Bitmap favicon = view.getFavicon();
							int width = favicon.getWidth();
						    int height = favicon.getHeight();
							if ((width > 16) || (height > 16)) {// scale the favicon if it is not 16*16
							    // calculate the scale
							    float scaleWidth = ((float) 16) / width;
							    float scaleHeight = ((float) 16) / height;

							    // create matrix for the manipulation
							    Matrix matrix = new Matrix();
							    // resize the bit map
							    matrix.postScale(scaleWidth, scaleHeight);

							    // recreate the new Bitmap
							    favicon = Bitmap.createBitmap(favicon, 0, 0, 
							                      width, height, matrix, true); 
							}
							FileOutputStream fos = openFileOutput(site + ".png", 0);
							favicon.compress(Bitmap.CompressFormat.PNG, 90,	fos);
							fos.close();
						}
					} catch (Exception e) {}
				}

				while (mHistory.size() > historyCount) 
					// delete from the first history until the list is not larger than historyCount;
					 //not delete icon here. it can be clear when clear all 
					mHistory.remove(0);
			}
		}
	}
	
	private class WebAdapter extends ArrayAdapter<MyWebview> {
		ArrayList localWeblist;

		public WebAdapter(Context context, List<MyWebview> webs) {
			super(context, 0, webs);
			localWeblist = (ArrayList) webs;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final MyWebview wv = (MyWebview) localWeblist.get(position);

			final LayoutInflater inflater = getLayoutInflater();
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
					    		addRemoveFavo(wv.m_url, webname.getText().toString());
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

	void recordPages() {
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
	
	void closePage(int position, boolean clearData) {
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
			MyWebview tmp = (MyWebview) webpages.getChildAt(position);
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

	public void ClearCache() {
		serverWebs.get(webIndex).clearCache(true);
		// mContext.deleteDatabase("webviewCache.db");//this may get
		// disk IO crash
		ClearFolderTask cltask = new ClearFolderTask();
		// clear cache on sdcard and in data folder
		cltask.execute(appstate.downloadPath + "cache/webviewCache/", dataPath + "cache/webviewCache/");		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if ((null == mUploadMessage) || (null == mUploadMessage.mInstance))
				return;
			Uri result = intent == null || resultCode != RESULT_OK ? null
					: intent.getData();
			if (mValueCallbackAvailable)
				mUploadMessage.onReceiveValue(result);
			mUploadMessage.mInstance = null;
		} else if (requestCode == SETTING_RESULTCODE) {
			boolean shouldReload = false;

			boolean clearData = sp.getBoolean("clear_data", false);
			if (clearData) {
				sEdit.putBoolean("clear_data", false);

				for (int i = 0; i < webAdapter.getCount(); i++)
					serverWebs.get(i).stopLoading();// stop loading while clear

				boolean clearCache = sp.getBoolean("clear_cache", false);
				if (clearCache) ClearCache();

				boolean clearCookie = sp.getBoolean("clear_cookie", false);
				if (clearCookie) {
					CookieSyncManager.createInstance(mContext);
					CookieManager.getInstance().removeAllCookie();
				}

				boolean clearFormdata = sp.getBoolean("clear_formdata", false);
				if (clearFormdata) {
					WebViewDatabase.getInstance(mContext).clearFormData();
				}

				boolean clearPassword = sp.getBoolean("clear_password", false);
				if (clearPassword) {
					WebViewDatabase.getInstance(mContext)
							.clearHttpAuthUsernamePassword();
					WebViewDatabase.getInstance(mContext)
							.clearUsernamePassword();
				}

				boolean clearIcon = sp.getBoolean("clear_icon", false);
				if (clearIcon) {
					ClearFolderTask cltask = new ClearFolderTask();
					// clear cache on sdcard and in data folder
					cltask.execute(dataPath + "files/", "png");
					if (HOME_BLANK.equals(webAddress.getText().toString())) shouldReload = true;
				}
				
				boolean clearHome = sp.getBoolean("clear_home", false);
				if (clearHome) {
					m_homepage = null;
					sEdit.putString("homepage", null);
				}
				
				boolean clearHistory = sp.getBoolean("clear_history", false);
				boolean clearBookmark = sp.getBoolean("clear_bookmark", false);

				if (clearHistory && clearBookmark && clearCache && clearCookie
						&& clearFormdata && clearPassword && clearIcon) {// clear all
					// mContext.deleteDatabase("webview.db");//this may get disk IO crash
					ClearFolderTask cltask = new ClearFolderTask();
					// clear files folder and app_databases folder
					cltask.execute(getFilesDir().getAbsolutePath(),
							getDir("databases", MODE_PRIVATE).getAbsolutePath());
				}

				if (clearHistory) {
					mHistory.clear();
					WriteTask wtask = new WriteTask();
					wtask.execute("history");
					clearFile("searchwords");
					siteArray.clear();
					urlAdapter.clear();
					historyChanged = false;
					if (HOME_BLANK.equals(webAddress.getText().toString()))
						shouldReload = true;
				}

				if (clearBookmark) {
					mBookMark.clear();
					WriteTask wtask = new WriteTask();
					wtask.execute("bookmark");
					bookmarkChanged = false;
					if (HOME_BLANK.equals(webAddress.getText().toString()))
						shouldReload = true;
				}

				String message = "";
				if (clearCache)
					message += "Cache, ";
				if (clearHistory)
					message += getString(R.string.history) + ", ";
				if (clearBookmark)
					message += getString(R.string.bookmark) + ", ";
				if (clearCookie)
					message += "Cookie, ";
				if (clearFormdata)
					message += getString(R.string.formdata) + ", ";
				if (clearPassword)
					message += getString(R.string.password) + ", ";
				if (clearIcon)
					message += getString(R.string.icon) + ", ";
				if (clearHome)
					message += getString(R.string.home) + ", ";
				message = message.trim();
				if (!"".equals(message)) {
					if (message.endsWith(","))
						message = message.substring(0, message.length() - 1);
					Toast.makeText(mContext,
							message + " " + getString(R.string.data_cleared),
							Toast.LENGTH_LONG).show();
				}
			}

			boolean tmpShow = sp.getBoolean("show_statusBar", true);
			if (tmpShow != showStatusBar) {
				showStatusBar = tmpShow;
				if (!showStatusBar) {// full screen
					getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				} else {
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
			}
			
			showUrl = sp.getBoolean("show_url", true);
			showControlBar = sp.getBoolean("show_controlBar", true);
			setWebpagesLayout();
			
			int tmpMode = sp.getInt("rotate_mode", 1);
			if (rotateMode != tmpMode) {
				rotateMode = tmpMode;
				if (rotateMode == 1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				else if (rotateMode == 2) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				setLayout();
			}

			// default to full screen now
			snapFullWeb = sp.getBoolean("full_web", false);

			incognitoMode = sp.getBoolean("incognito", false);
			
			updownButton = sp.getBoolean("up_down", false);
			if (updownButton) upAndDown.setVisibility(View.VISIBLE);
			else upAndDown.setVisibility(View.INVISIBLE);
			
			shareMode = sp.getInt("share_mode", 2);
			
			searchEngine = sp.getInt("search_engine", 3);

			boolean tmpEnableProxy = sp.getBoolean("enable_proxy", false);
			int tmpLocalPort = sp.getInt("local_port", 1984);
			if ((enableProxy != tmpEnableProxy) || (localPort != tmpLocalPort)) {
				enableProxy = tmpEnableProxy;
				localPort = tmpLocalPort;
				if (enableProxy)
					ProxySettings.setProxy(mContext, "127.0.0.1", localPort);
				else
					try {
						ProxySettings.resetProxy(mContext);
					} catch (Exception e) {}
			}

			WebSettings localSettings = serverWebs.get(webIndex).getSettings();

			ua = sp.getInt("ua", 0);
			if (ua <= 1)
				localSettings.setUserAgent(ua);
			else
				localSettings.setUserAgentString(selectUA(ua));

			// hideExit = sp.getBoolean("hide_exit", true);

			readTextSize(sp); // no need to reload page if fontSize changed
			localSettings.setTextSize(textSize);

			blockImage = sp.getBoolean("block_image", false);
			localSettings.setBlockNetworkImage(blockImage);

			boolean tmpCachePrefer = sp.getBoolean("cache_prefer", false);
			if (tmpCachePrefer != cachePrefer) {
				cachePrefer = tmpCachePrefer;
				if (cachePrefer)
					localSettings
							.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
				else
					localSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
			}

			blockPopup = sp.getBoolean("block_popup", false);
			localSettings.setJavaScriptCanOpenWindowsAutomatically(blockPopup);
			// localSettings.setSupportMultipleWindows(!blockPopup);

			blockJs = sp.getBoolean("block_js", false);
			//localSettings.setJavaScriptEnabled(!blockJs);

			wrapWebSettings webSettings = new wrapWebSettings(localSettings);
			overviewPage = sp.getBoolean("overview_page", false);
			webSettings.setLoadWithOverviewMode(overviewPage);

			boolean showZoom = sp.getBoolean("show_zoom", false);
			if (webSettings.setDisplayZoomControls(showZoom)) {// hide zoom
																// button by
																// default on
																// API 11 and
																// above
				localSettings.setBuiltInZoomControls(showZoom);// setDisplayZoomControls(false)
																// not work, so
																// we need
																// disable zoom
																// control
				serverWebs.get(webIndex).zoomVisible = showZoom;
			} else {
				if (showZoom)
					serverWebs.get(webIndex).setZoomControl(View.VISIBLE);
				else
					serverWebs.get(webIndex).setZoomControl(View.GONE);
			}

			boolean html5 = sp.getBoolean("html5", false);
			serverWebs.get(webIndex).html5 = html5;
			webSettings.setAppCacheEnabled(html5);// API7
			webSettings.setDatabaseEnabled(html5);// API5
			// webSettings.setGeolocationEnabled(html5);//API5
			if (html5) {
				webSettings.setAppCachePath(getDir("databases", MODE_PRIVATE)
						.getPath());// API7
				// it will cause crash on OPhone if not set the max size
				webSettings.setAppCacheMaxSize(html5cacheMaxSize);
				webSettings.setDatabasePath(getDir("databases", MODE_PRIVATE)
						.getPath());// API5. how slow will it be if set path to
				// sdcard?
				// webSettings.setGeolocationDatabasePath(getDir("databases",
				// MODE_PRIVATE).getPath());//API5
			}

			String tmpEncoding = getEncoding(sp.getInt("encoding", 0));
			if (!tmpEncoding.equals(localSettings.getDefaultTextEncodingName())) {
				if ("AUTOSELECT".equals(tmpEncoding) && "Latin-1".equals(localSettings.getDefaultTextEncodingName())) ;//not reload in this case
				else {
					localSettings.setDefaultTextEncodingName(tmpEncoding);
					// set default encoding to autoselect
					sEdit.putInt("encoding", 0);
					shouldReload = true;
				}
			}

			if (shouldReload) serverWebs.get(webIndex).reload();
			sEdit.commit();
		}
	}

	void clearFile(String filename) {
		try {// clear the pages file
			FileOutputStream fo = openFileOutput(filename, 0);
			ObjectOutputStream oos = new ObjectOutputStream(fo);
			oos.writeObject("");
			oos.flush();
			oos.close();
			fo.close();
		} catch (Exception e) {}
	}
	
	public void setWebpagesLayout() {
		setUrlHeight(showUrl);
		setBarHeight(showControlBar);
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

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		final HitTestResult result = ((WebView) v).getHitTestResult();
		final String url = result.getExtra();

		MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {// do the menu action
				switch (item.getItemId()) {
				case 0:// download
					String ext = null;
					if (result.getType() == HitTestResult.IMAGE_TYPE
							|| result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE)
						ext = ".jpg";
					startDownload(url, ext, "yes");
					break;
				case 4:// copy url
					ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipMan.setText(url);
					break;
				case 5:// share url
					shareUrl("", url);
					break;
				case 6:// open in background
					openNewPage(url, webAdapter.getCount(), false, true);// use openNewPage(url, webIndex+1, true) for open in new tab 
					break;
				case 7:// add short cut
					createShortcut(url, mBookMark.get(item.getOrder()).m_title);
					break;
				case 8:// remove bookmark
					removeFavo(item.getOrder());
					break;
				case 9:// remove history
					removeHistory(item.getOrder());
					break;
				case 10:// add bookmark. not use now
					if (historyIndex > -1)
						addFavo(url, mHistory.get(historyIndex).m_title);
					else addFavo(url, url);
					break;
				case 11://set homepage
					m_homepage = url;
					sEdit.putString("homepage", url);
					sEdit.commit();
					break;
				}
				return true;
			}
		};

		// set the title to the url
		menu.setHeaderTitle(result.getExtra());
		if (url != null) {
			menu.add(0, 4, 0, R.string.copy_url).setOnMenuItemClickListener(
					handler);
			menu.add(0, 5, 0, R.string.shareurl).setOnMenuItemClickListener(
					handler);

			if (HOME_PAGE.equals(serverWebs.get(webIndex).getUrl())) {// only operate bookmark/history in home page
				menu.add(0, 6, 0, R.string.open_background).setOnMenuItemClickListener(handler);
				
				boolean foundBookmark = false;
				for (int i = mBookMark.size() - 1; i >= 0; i--)
					if ((mBookMark.get(i).m_url.equals(url))
							|| (url.equals(mBookMark.get(i).m_url + "/"))) {
						foundBookmark = true;
						if (!mAdAvailable) {
							menu.add(0, 7, i, R.string.add_shortcut).setOnMenuItemClickListener(handler);// only work in pro version
							menu.add(0, 11, i, R.string.set_homepage).setOnMenuItemClickListener(handler);// only work in pro version
						}
						menu.add(0, 8, i, R.string.remove_bookmark)
								.setOnMenuItemClickListener(handler);
						break;
					}
				//if (!foundBookmark) menu.add(0, 7, 0, R.string.add_bookmark).setOnMenuItemClickListener(handler);// no add bookmark on long click?

				historyIndex = -1;
				for (int i = mHistory.size() - 1; i >= 0; i--)
					if ((mHistory.get(i).m_url.equals(url))
							|| (url.equals(mHistory.get(i).m_url + "/"))) {
						historyIndex = i;
						menu.add(0, 9, i, R.string.remove_history)
								.setOnMenuItemClickListener(handler);
						break;
					}
			}
			else {
				menu.add(0, 0, 0, R.string.save).setOnMenuItemClickListener(handler);
				menu.add(0, 6, 0, R.string.open_background).setOnMenuItemClickListener(handler);
			}
		}
	}

	void createShortcut(String url, String title) {
		Intent i = new Intent(this, SimpleBrowser.class);
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
	
	String getName(String url) {
		String readableUrl = url;
		try {
			readableUrl = URLDecoder.decode(url);// change something like http%3A%2F%2Fwww%2Ebaidu%2Ecom%3Fcid%3D%2D%5F1 to http://www.baidu.com?cid=-_1
		} catch (Exception e) {}// report crash by Elad, so catch it.
		
		if (readableUrl.endsWith("/"))
			readableUrl = readableUrl.substring(0, readableUrl.length() - 1); // such as http://m.cnbeta.com/, http://www.google.com.hk/
		
		int posQ = readableUrl.indexOf("?");
		if (posQ > 0)
			readableUrl = readableUrl.substring(0, posQ);// cut off post paras if any.

		String ss[] = readableUrl.split("/");
		String apkName = ss[ss.length - 1].toLowerCase(); // get download file
		// name
		if (apkName.contains("="))
			apkName = apkName.split("=")[apkName.split("=").length - 1];

		return apkName;
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
		String apkName = getName(url);
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

		DownloadTask dl = appstate.downloadState.get(url);
		if (dl != null) {
			if (dl.pauseDownload) dl.pauseDownload = false;// resume download if it paused
			return true;// the file is downloading, not start a new download task.
		}

		Random random = new Random();
		int id = random.nextInt() + 1000;

		DownloadTask dltask = new DownloadTask();
		dltask.appstate = appstate;
		dltask.NOTIFICATION_ID = id;
		appstate.downloadState.put(url, dltask);
		dltask.execute(url, apkName, contentDisposition, openAfterDone);
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (menuDialog == null) initMenuDialog();
		
		if (menuDialog.isShowing()) menuDialog.dismiss();
		else {
			setUrlHeight(true);
			setBarHeight(true);
			
			menuDialog.show();
		}

		return false;// show system menu if return true.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// must create one menu?
		return super.onCreateOptionsMenu(menu);
	}

	private SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(this, data,
				R.layout.icon_list, new String[] { "itemImage", "itemText" },
				new int[] { R.id.appicon, R.id.appname });
		return simperAdapter;
	}

	private void shareUrl(String title, String url) {
		if (title == null) title = "";
		if (url == null) url = "";
		
		Intent shareIntent = new Intent(Intent.ACTION_VIEW);
		shareIntent.setClassName(getPackageName(), SimpleBrowser.class.getName());
		Uri data = null;
		String from = "\n(from ";
		boolean chineseLocale = Locale.CHINA.equals(mLocale) || "easy.browser".equals(getPackageName());//easy.browser only release in China
				
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

	public void setDefault(PackageManager pm, Intent intent, IntentFilter filter) {
		List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
		int size = resolveInfoList.size();
		ComponentName[] arrayOfComponentName = new ComponentName[size];
		boolean seted = false;
		for (int i = 0; i < size; i++) {
			ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
			String packageName = activityInfo.packageName;
			String className = activityInfo.name;
			//clear default browser
			if (packageName.equals(mContext.getPackageName())) {
				seted = true;
				break;
			}
			try{pm.clearPackagePreferredActivities(packageName);} catch(Exception e) {}
			ComponentName componentName = new ComponentName(packageName, className);
			arrayOfComponentName[i] = componentName;
		}
		
		if (!seted) {
			ComponentName component = new ComponentName(mContext.getPackageName(), "easy.lib.SimpleBrowser");
			pm.addPreferredActivity(filter,	IntentFilter.MATCH_CATEGORY_SCHEME, arrayOfComponentName, component);
		}
	}

	public void setAsDefaultApp() {
		PackageManager pm = getPackageManager();
		
		try {pm.addPackageToPreferred(getPackageName());}// for 1.5 platform 
		catch(Exception e) {
			// set default browser for 1.6-2.1 platform. not work for 2.2 and up platform
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.BROWSABLE");
			intent.addCategory("android.intent.category.DEFAULT");
			
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.intent.action.VIEW");
			filter.addCategory("android.intent.category.BROWSABLE");
			filter.addCategory("android.intent.category.DEFAULT");
			filter.addDataScheme("http");
			
			Uri uri = Uri.parse("http://");
			intent.setDataAndType(uri, null);
			setDefault(pm, intent, filter);			
		} 
	}
	
	public void readPreference() {
		// css = sp.getBoolean("css", false);
		// html5 = sp.getBoolean("html5", false);
		blockImage = sp.getBoolean("block_image", false);
		cachePrefer = sp.getBoolean("cache_prefer", false);
		blockPopup = sp.getBoolean("block_popup", false);
		blockJs = sp.getBoolean("block_js", false);
		// hideExit = sp.getBoolean("hide_exit", true);
		overviewPage = sp.getBoolean("overview_page", false);
		collapse1 = sp.getBoolean("collapse1", false);
		collapse2 = sp.getBoolean("collapse2", true);
		collapse3 = sp.getBoolean("collapse3", true);
		ua = sp.getInt("ua", 0);
		//showZoom = sp.getBoolean("show_zoom", false);
		mLocale = getBaseContext().getResources().getConfiguration().locale;
		if ("ru_RU".equals(mLocale.toString()))
			searchEngine = sp.getInt("search_engine", 4); // yandex
		else if (Locale.CHINA.equals(mLocale)) 
			searchEngine = sp.getInt("search_engine", 2); // easou
		else
			searchEngine = sp.getInt("search_engine", 5); // duckduckgo
		shareMode = sp.getInt("share_mode", 2); // share by facebook/weibo by default
		snapFullWeb = sp.getBoolean("full_web", false);
		readTextSize(sp);// init the text size
		enableProxy = sp.getBoolean("enable_proxy", false);
		if (enableProxy) {
			localPort = sp.getInt("local_port", 1984);
			ProxySettings.setProxy(mContext, "127.0.0.1", localPort);
		}
		if (!mAdAvailable) m_homepage = sp.getString("homepage", null);

		incognitoMode = sp.getBoolean("incognito", false);
		updownButton = sp.getBoolean("up_down", false);
		
		showStatusBar = sp.getBoolean("show_statusBar", true);
		showUrl = sp.getBoolean("show_url", true);
		showControlBar = sp.getBoolean("show_controlBar", true);
		
		
		rotateMode = sp.getInt("rotate_mode", 1);
		if (rotateMode == 1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		else if (rotateMode == 2) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	String getSite(String url) {
		String site = "";
		String[] tmp = url.split("/");
		// if url is http://m.baidu.com, then url.split("/")[2] is m.baidu.com
		if (tmp.length > 2)	site = tmp[2];
		else site = tmp[0];
		
		return site;
	}
	
	public void initSnapDialog() {		
		snapView = (ImageView) getLayoutInflater().inflate(
				R.layout.snap_browser, null);
		snapDialog = new AlertDialog.Builder(this)
				.setView(snapView)
				.setTitle(R.string.browser_name)
				.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {// share
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							String snap = appstate.downloadPath + "snap/" + serverWebs.get(webIndex).getTitle() + ".png";
							FileOutputStream fos = new FileOutputStream(snap);

							bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
							fos.close();

							Intent intent = new Intent(Intent.ACTION_SEND);
							intent.setType("image/*");
							intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
							intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(snap)));
							util.startActivity(Intent.createChooser(
									intent,
									getString(R.string.sharemode)),
									true, mContext);
						} catch (Exception e) {
							Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
						}
					}
				})
				.setNeutralButton(R.string.save, new DialogInterface.OnClickListener() {// save
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							String snap = appstate.downloadPath + "snap/" + serverWebs.get(webIndex).getTitle() + ".png";
							FileOutputStream fos = new FileOutputStream(snap);
							bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
							fos.close();
							Toast.makeText(getBaseContext(), getString(R.string.save) + " " + snap, Toast.LENGTH_LONG).show();
						} catch (Exception e) {
							Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
						}
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {// cancel
					@Override
					public void onClick(DialogInterface dialog, int which) {}
				}).create();
	}

	public void initSourceDialog() {		
		m_sourceDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.browser_name)
		.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {// share
			@Override
			public void onClick(DialogInterface dialog,	int which) {
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.fromParts("mailto", "", null));
				intent.putExtra(Intent.EXTRA_TEXT, serverWebs.get(webIndex).pageSource);
				intent.putExtra(Intent.EXTRA_SUBJECT, serverWebs.get(webIndex).getTitle());
				if (!util.startActivity(intent, false, getBaseContext())) 
					shareUrl("", serverWebs.get(webIndex).pageSource);
			}
		})
		.setNeutralButton(R.string.save, new DialogInterface.OnClickListener() {// save
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					String snap = appstate.downloadPath + "source/" + serverWebs.get(webIndex).getTitle() + ".txt";
					FileOutputStream fos = new FileOutputStream(snap);
					fos.write(serverWebs.get(webIndex).pageSource.getBytes());
					fos.close();
					Toast.makeText(getBaseContext(), getString(R.string.save) + " " + snap, Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
				}
			}
		})
		.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {// cancel
			@Override
			public void onClick(DialogInterface dialog,	int which) {}
		}).create();
	}
	
	public void initMenuDialog() {		
		// menu icon
		int[] menu_image_array = { R.drawable.html_w, R.drawable.capture,
				R.drawable.copy, R.drawable.exit, R.drawable.downloads,
				R.drawable.share, R.drawable.search, R.drawable.about };
		// menu text
		String[] menu_name_array = { getString(R.string.source),
				getString(R.string.snap), getString(R.string.copy),
				getString(R.string.exit), getString(R.string.downloads),
				getString(R.string.shareurl), getString(R.string.search),
				getString(R.string.settings) };

		// create AlertDialog
		menuView = View.inflate(mContext, R.layout.grid_menu, null);
		menuDialog = new AlertDialog.Builder(this).create();
		menuDialog.setView(menuView);
		WindowManager.LayoutParams params = menuDialog.getWindow()
				.getAttributes();
		// 240 for 1024h, 140 for 800h, 70 for 480h, to show menu dialog in
		// correct position
		if (dm.heightPixels <= 480)
			params.y = 70;
		else if (dm.heightPixels <= 800)
			params.y = 140;
		else
			params.y = 240;
		menuDialog.getWindow().setAttributes(params);

		menuDialog.setCanceledOnTouchOutside(true);

		menuDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU)
					dialog.dismiss();
				return false;
			}

		});

		final Context localContext = this;
		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		menuGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 0:// view page source
					try {
						if ("".equals(serverWebs.get(webIndex).pageSource)) {
							serverWebs.get(webIndex).pageSource = "Loading... Please try again later.";
							serverWebs.get(webIndex).getPageSource();
						}

						if (m_sourceDialog == null) initSourceDialog();
						m_sourceDialog.setTitle(serverWebs.get(webIndex)
								.getTitle());
						if (HOME_PAGE.equals(serverWebs.get(webIndex).getUrl()))
							m_sourceDialog.setIcon(R.drawable.explorer);
						else
							m_sourceDialog.setIcon(new BitmapDrawable(
									serverWebs.get(webIndex).getFavicon()));
						m_sourceDialog.setMessage(serverWebs.get(webIndex).pageSource);
						m_sourceDialog.show();
					} catch (Exception e) {
						Toast.makeText(mContext, e.toString(),
								Toast.LENGTH_LONG).show();
					}
					break;
				case 1:// view snap
					try {// still got java.lang.RuntimeException: Canvas: trying
							// to use a recycled bitmap android.graphics.Bitmap
							// from one user. so catch it.
						if (!snapFullWeb) {
							// the snap will not refresh if not destroy cache
							webpages.destroyDrawingCache();
							webpages.setDrawingCacheEnabled(true);
							bmp = webpages.getDrawingCache();
						} else {
							Picture pic = serverWebs.get(webIndex)
									.capturePicture();

							// bmp = Bitmap.createScaledBitmap(???,
							// pic.getWidth(), pic.getHeight(), false);//check
							// here http://stackoverflow.com/questions/477572
							bmp = Bitmap.createBitmap(pic.getWidth(),
									pic.getHeight(), Bitmap.Config.ARGB_4444);
							// the size of the web page may be very large.

							Canvas canvas = new Canvas(bmp);
							pic.draw(canvas);
						}
						
						if (snapDialog == null) initSnapDialog();
						snapView.setImageBitmap(bmp);
						snapDialog.setTitle(serverWebs.get(webIndex).getTitle());
						if (HOME_PAGE.equals(serverWebs.get(webIndex).getUrl()))
							snapDialog.setIcon(R.drawable.explorer);
						else
							snapDialog.setIcon(new BitmapDrawable(serverWebs
									.get(webIndex).getFavicon()));
						snapDialog.show();
					} catch (Exception e) {
						Toast.makeText(mContext, e.toString(),
								Toast.LENGTH_LONG).show();
					}
					break;
				case 2:// copy
					webControl.setVisibility(View.INVISIBLE);// hide webControl when copy
					try {
						if (Integer.decode(android.os.Build.VERSION.SDK) > 10)
							Toast.makeText(mContext,
									getString(R.string.copy_hint),
									Toast.LENGTH_LONG).show();
					} catch (Exception e) {
					}

					try {
						KeyEvent shiftPressEvent = new KeyEvent(0, 0,
								KeyEvent.ACTION_DOWN,
								KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
						shiftPressEvent.dispatch(serverWebs.get(webIndex));
					} catch (Exception e) {
					}
					break;
				case 3:// exit
					clearFile("pages");
					ClearCache(); // clear cache when exit
					finish();
					break;
				case 4:// downloads
					Intent intent = new Intent(
							"com.estrongs.action.PICK_DIRECTORY");
					intent.setData(Uri.parse("file:///sdcard/simpleHome/"));
					if (!util.startActivity(intent, false, mContext)) {
						if (downloadsDialog == null)
							downloadsDialog = new AlertDialog.Builder(
									localContext)
									.setMessage(
											getString(R.string.downloads_to)
													+ appstate.downloadPath
													+ getString(R.string.downloads_open))
									.setPositiveButton(
											R.string.ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													Intent intent = new Intent(
															Intent.ACTION_VIEW,
															Uri.parse("market://details?id=com.estrongs.android.pop"));
													util.startActivity(intent,
															true,
															getBaseContext());
												}
											})
									.setNegativeButton(
											R.string.cancel,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
												}
											}).create();
						downloadsDialog.show();
					}
					break;
				case 5:// share url
					shareUrl(serverWebs.get(webIndex).getTitle(), serverWebs.get(webIndex).m_url);
					break;
				case 6:// search
					webControl.setVisibility(View.INVISIBLE);// hide webControl when search
						// serverWebs.get(webIndex).showFindDialog("e", false);
					if (searchBar == null) initSearchBar();
					searchBar.bringToFront();
					searchBar.setVisibility(View.VISIBLE);
					etSearch.requestFocus();
					toSearch = "";
					imm.toggleSoftInput(0, 0);
					break;
				case 7:// settings
					gotoSettings = true;
					intent = new Intent(getPackageName() + "about");
					intent.setClassName(getPackageName(), AboutBrowser.class.getName());
					startActivityForResult(intent, SETTING_RESULTCODE);
					break;
				}
				menuDialog.dismiss();
			}
		});
	}
	
	public void initUpDown() {
		upAndDown = (LinearLayout) findViewById(R.id.up_down);
		if (updownButton) upAndDown.setVisibility(View.VISIBLE);
		else upAndDown.setVisibility(View.INVISIBLE);
		
		ImageView dragButton = (ImageView) findViewById(R.id.page_drag);
		dragButton.setAlpha(80);
		dragButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int[] temp = new int[] { 0, 0 };
				int eventAction = event.getAction();
				int x = (int) event.getRawX();
				int y = (int) event.getRawY();
				int offset = - urlLine.getHeight() - adContainer.getHeight();
				switch (eventAction) {
				case MotionEvent.ACTION_DOWN:// prepare for drag
					if (!showUrl) setUrlHeight(showUrl);
					if (!showControlBar) setBarHeight(showControlBar);
					temp[0] = (int) event.getX();
					temp[1] = y;
					break;

				case MotionEvent.ACTION_MOVE:// drag the button
					upAndDown.layout(
							x - temp[0], 
							y - temp[1] - upAndDown.getHeight() + offset, 
							x - temp[0] + upAndDown.getWidth(), 
							y - temp[1] + offset
						);
					upAndDown.postInvalidate();
					break;

				case MotionEvent.ACTION_UP:// reset the margin when stop drag
					MarginLayoutParams lp = (MarginLayoutParams) upAndDown.getLayoutParams();
					lp.setMargins(
							0, 
							Math.min(upAndDown.getTop(), 0), 
							Math.min(Math.max(webs.getWidth()-upAndDown.getRight(), 0), webs.getWidth()-upAndDown.getWidth()), 
							Math.min(Math.max(webs.getHeight()-upAndDown.getBottom(), 0), webs.getHeight()-20)
						);
					upAndDown.setLayoutParams(lp);
					break;
				}
				return true;
			}
		});
		
		upButton = (ImageView) findViewById(R.id.page_up);
		upButton.setAlpha(40);
		Matrix matrix = new Matrix();
		matrix.postRotate(180f, 24*dm.density, 24*dm.density);
		upButton.setImageMatrix(matrix);// rotate 180 degree
		upButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int eventAction = event.getAction();
				switch (eventAction) {
				case MotionEvent.ACTION_DOWN:
					actioned = false;
					lastTime = event.getEventTime();
					timeInterval = 100;
					break;
				case MotionEvent.ACTION_MOVE:
					if (event.getEventTime() - lastTime > timeInterval) {
						timeInterval = 70;
						lastTime = event.getEventTime();
						scrollUp();
					}
					break;
				case MotionEvent.ACTION_UP:
					if (!actioned) scrollUp();
					break;
				}
				return true;
			}
		});
		
		downButton = (ImageView) findViewById(R.id.page_down);
		downButton.setAlpha(40);
		downButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int eventAction = event.getAction();
				switch (eventAction) {
				case MotionEvent.ACTION_DOWN:
					actioned = false;
					lastTime = event.getEventTime();
					timeInterval = 100;// the interval for first scroll is longer than the after, to avoid scroll twice
					break;
				case MotionEvent.ACTION_MOVE:
					if (event.getEventTime() - lastTime > timeInterval) {
						timeInterval = 70;
						lastTime = event.getEventTime();
						scrollDown();
					}
					break;
				case MotionEvent.ACTION_UP:
					if (!actioned) scrollDown();
					break;
				}
				return true;
			}
		});
	}
	
	long lastTime = 0;
	long timeInterval = 100;
	boolean actioned = false;
	void scrollUp() {
		actioned = true;
		if (!showUrl) setUrlHeight(showUrl);
		if (!showControlBar) setBarHeight(showControlBar);
		if (serverWebs.get(webIndex).myCanScrollVertically(-1))// pageUp/pageDown have animation which is slow
			serverWebs.get(webIndex).scrollBy(0, -serverWebs.get(webIndex).getHeight()+10);		
	}
	void scrollDown() {
		actioned = true;
		if (!showUrl) setUrlHeight(showUrl);
		if (!showControlBar) setBarHeight(showControlBar);
		if (serverWebs.get(webIndex).myCanScrollVertically(1))
			serverWebs.get(webIndex).scrollBy(0, serverWebs.get(webIndex).getHeight()-10);		
	}
	
	public void initSearchBar() {		
		imgSearchPrev = (ImageView) findViewById(R.id.search_prev);
		imgSearchPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!toSearch.equals(etSearch.getText().toString()))
					findMatchCount();
				else if (matchCount > 0) {
					serverWebs.get(webIndex).findNext(false);
					matchIndex -= 1;
					if (matchIndex < 0) {
						while (matchIndex < matchCount - 1) {
							serverWebs.get(webIndex).findNext(true);
							matchIndex += 1;
						}
					}
				}

				if (matchCount > 0)
					searchHint.setText((matchIndex + 1) + " of " + matchCount);
				else
					searchHint.setText("0 of 0");
			}
		});
		imgSearchNext = (ImageView) findViewById(R.id.search_next);
		imgSearchNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!toSearch.equals(etSearch.getText().toString()))
					findMatchCount();
				else if (matchCount > 0) {
					serverWebs.get(webIndex).findNext(true);
					matchIndex += 1;
					if (matchIndex >= matchCount)
						while (matchIndex > 0) {
							serverWebs.get(webIndex).findNext(false);
							matchIndex -= 1;
						}
				}

				if (matchCount > 0)
					searchHint.setText((matchIndex + 1) + " of " + matchCount);
				else
					searchHint.setText("0 of 0");
			}
		});

		imgSearchClose = (ImageView) findViewById(R.id.close_search);
		imgSearchClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				hideSearchBox();
			}
		});

		searchBar = (RelativeLayout) findViewById(R.id.search_bar);
		searchHint = (TextView) findViewById(R.id.search_hint);
		etSearch = (EditText) findViewById(R.id.search);
		etSearch.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP)
					switch (keyCode) {
					case KeyEvent.KEYCODE_SEARCH:
					case KeyEvent.KEYCODE_ENTER:
					case KeyEvent.KEYCODE_DPAD_CENTER:
						imgSearchNext.performClick();
						break;
					}
				return false;
			}
		});		
	}
	
	public void initWebControl() {
		// web control
		webControl = (LinearLayout) findViewById(R.id.webcontrol);

		btnNewpage = (Button) findViewById(R.id.opennewpage);
		btnNewpage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {// add a new page
				openNewPage("", webIndex+1, true, false);
			}
		});
		// web list
		webAdapter = new WebAdapter(mContext, serverWebs);
		webList = (ListView) findViewById(R.id.weblist);
		webList.inflate(mContext, R.layout.web_list, null);
		webList.setFadingEdgeLength(0);// no shadow when scroll
		webList.setScrollingCacheEnabled(false);
		webList.setAdapter(webAdapter);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// mContext = getApplicationContext();//this will cause force close when
		// select locale in google translate
		mContext = this;

		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		browserName = getString(R.string.browser_name);
		
		// init settings
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		sEdit = sp.edit();
		readPreference();

		setAsDefaultApp();
		
		appstate = ((MyApp) getApplicationContext());
		appstate.nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		appstate.mContext = mContext;


		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		// hide titlebar of application, must be before setting the layout
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.browser);
        LayoutInflater inflater = LayoutInflater.from(this);
        
		initWebControl();

		loadProgress = (ProgressBar) findViewById(R.id.loadprogress);

		imgAddFavo = (ImageView) findViewById(R.id.addfavorite);
		imgAddFavo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String url = serverWebs.get(webIndex).m_url;
				if (HOME_PAGE.equals(url)) return;// not add home page

				addRemoveFavo(url, serverWebs.get(webIndex).getTitle());
			}
		});
		imgAddFavo.setOnLongClickListener(new OnLongClickListener() {// long click to show bookmark
			@Override
			public boolean onLongClick(View arg0) {
				CharSequence bookmarks[] = new CharSequence[mBookMark.size()];
				for (int i = 0; i < mBookMark.size(); i++)
				{
					bookmarks[i] = mBookMark.get(i).m_title;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(getString(R.string.bookmark));
				builder.setItems(bookmarks, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        // the user clicked on engine[which]
						serverWebs.get(webIndex).loadUrl(mBookMark.get(which).m_url);
				    }
				});
				builder.show();
				return true;
			}
		});

		imgGo = (ImageView) findViewById(R.id.go);
		imgGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gotoUrl(webAddress.getText().toString().toLowerCase());
			}
		});
		imgGo.setOnLongClickListener(new OnLongClickListener() {// long click to select search engine
			@Override
			public boolean onLongClick(View arg0) {
				CharSequence engine[] = new CharSequence[] {getString(R.string.bing), getString(R.string.baidu), getString(R.string.google), getString(R.string.yandex), getString(R.string.duckduckgo)};
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(getString(R.string.search_engine));
				builder.setSingleChoiceItems(engine, searchEngine-1, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        // the user clicked on engine[which]
				    	searchEngine = which + 1;
				    	sEdit.putInt("search_engine", searchEngine);
				    	sEdit.commit();
				    	dialog.dismiss();
						gotoUrl(webAddress.getText().toString().toLowerCase());
				    }
				});
				builder.show();
				return true;
			}
		});

		webAddress = (AutoCompleteTextView) findViewById(R.id.url);
		webAddress.bringToFront();
		webAddress.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				gotoUrl(urlAdapter.getItem(position));
			}

		});
		webAddress.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView view, int actionId,
					KeyEvent event) {
				imgGo.performClick();
				return false;
			}
		});
		webAddress.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				view.setFocusableInTouchMode(true);
				serverWebs.get(webIndex).setFocusable(false);
				return false;
			}
		});

		dataPath = "/data/data/" + getPackageName() + "/";
		appstate.downloadPath = util.preparePath(mContext);
		if (appstate.downloadPath == null) appstate.downloadPath = dataPath;
		if (appstate.downloadPath.startsWith(dataPath)) noSdcard = true;

		// should read in below sequence: 1, sdcard. 2, data/data. 3, native browser
		try {
			FileInputStream fi = null;
			if (noSdcard) fi = openFileInput("history");
			else {
				try {// try to open history on sdcard at first
					File file = new File(appstate.downloadPath + "bookmark/history");
					fi = new FileInputStream(file);
				} catch (FileNotFoundException e) {// read from /data/data if fail
					noHistoryOnSdcard = true;
					fi = openFileInput("history");
				}
			}
			
			try {// close anyway
				fi.close();
			} catch (Exception e) {}
		} catch (FileNotFoundException e1) {
			firstRun = true;
		}
		
		siteArray = new ArrayList<String>();
		urlAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_dropdown_item_1line,
				new ArrayList<String>());
		getHistoryList();// read history and bookmark from native browser
		String site;
		for (int i = 0; i < mSystemHistory.size(); i++) {
			site = mSystemHistory.get(i).m_site;
			if (siteArray.indexOf(site) < 0) {
				siteArray.add(site);
				urlAdapter.add(site);
			}
		}
		for (int i = 0; i < mSystemBookMark.size(); i++) {
			site = mSystemBookMark.get(i).m_site;
			if (siteArray.indexOf(site) < 0) {
				siteArray.add(site);
				urlAdapter.add(site);
			}
		}
		
		if (!firstRun) {
			mHistory = readBookmark("history");
			mBookMark = readBookmark("bookmark");
			Collections.sort(mBookMark, new MyComparator());

			for (int i = 0; i < mHistory.size(); i++) {
				site = mHistory.get(i).m_site;
				if (siteArray.indexOf(site) < 0) {
					siteArray.add(site);
					urlAdapter.add(site);
				}
			}
			for (int i = 0; i < mBookMark.size(); i++) {
				site = mBookMark.get(i).m_site;
				if (siteArray.indexOf(site) < 0) {
					siteArray.add(site);
					urlAdapter.add(site);
				}
			}

			// read search words
			ObjectInputStream ois = null;
			FileInputStream fi = null;
			String word = null;
			try {
				fi = openFileInput("searchwords");
				ois = new ObjectInputStream(fi);
				while ((word = (String) ois.readObject()) != null) {
					if (siteArray.indexOf(word) < 0) {
						siteArray.add(word);
						urlAdapter.add(word);
					}
				}
			} catch (EOFException e) {// only when read eof need send out msg.
				try {
					ois.close();
					fi.close();
				} catch (Exception e1) {}
			} catch (Exception e) {}
		}
		else {// copy from system bookmark if first run
			for (int i = 0; i < mSystemHistory.size(); i++) {
				if (i > historyCount) break;
				mHistory.add(mSystemHistory.get(i));
			}

			for (int i = 0; i < mSystemBookMark.size(); i++)
				mBookMark.add(mSystemBookMark.get(i));
			Collections.sort(mBookMark, new MyComparator());

			historyChanged = true;
			bookmarkChanged = true;

			countDown = 1;
		}

		urlAdapter.sort(new stringComparator());
		webAddress.setAdapter(urlAdapter);

		cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		WebIconDatabase.getInstance().open(
				getDir("databases", MODE_PRIVATE).getPath());
		webIndex = 0;
		serverWebs.add(new MyWebview(mContext));
		webpages = (MyViewFlipper) findViewById(R.id.webpages);
		webpages.addView(serverWebs.get(webIndex));

		webTools = (LinearLayout) findViewById(R.id.webtools);
		urlLine = (LinearLayout) findViewById(R.id.urlline);
		webs = (RelativeLayout) findViewById(R.id.webs);
		
		adContainer2 = (LinearLayout) findViewById(R.id.adContainer2);
		imageBtnList = (LinearLayout) findViewById(R.id.imagebtn_list);
		imageBtnList.bringToFront();
		
		if (!showStatusBar) 
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

		imgNext = (ImageView) findViewById(R.id.next);
		imgNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (serverWebs.get(webIndex).canGoForward())
					serverWebs.get(webIndex).goForward();
			}
		});
		imgNext.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				CharSequence operations[] = new CharSequence[] {getString(R.string.scroll_top), getString(R.string.page_up), getString(R.string.page_down), getString(R.string.scroll_bottom)};
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setSingleChoiceItems(operations, -1, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
			    		ListView lw = ((AlertDialog)dialog).getListView();
				    	switch(which) {
				    	case 0:
				    		serverWebs.get(webIndex).pageUp(true);
				    		dialog.dismiss();
				    		break;
				    	case 1:
				    		serverWebs.get(webIndex).pageUp(false);
				    		lw.clearChoices();
				    		break;
				    	case 2:
				    		serverWebs.get(webIndex).pageDown(false);
				    		lw.clearChoices();
				    		break;
				    	case 3:
				    		serverWebs.get(webIndex).pageDown(true);
				    		dialog.dismiss();
				    		break;
				    	}
				    }
				}).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
				    	dialog.dismiss();
					}
				});
				builder.show();
				return true;
			}
		});

		imgPrev = (ImageView) findViewById(R.id.prev);
		imgPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (serverWebs.get(webIndex).canGoBack())
					serverWebs.get(webIndex).goBack();
				else if (serverWebs.get(webIndex).shouldCloseIfCannotBack)
					closePage(webIndex, false);
			}
		});
		imgPrev.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				final WebBackForwardList wbfl = serverWebs.get(webIndex).copyBackForwardList();
				if ((wbfl != null) && !incognitoMode) {
					int size = wbfl.getSize();
					final int current = wbfl.getCurrentIndex();
					if (size > 0) {
						CharSequence historys[] = new CharSequence[size];
						for (int i = 0; i < size; i++)
							historys[i] = wbfl.getItemAtIndex(i).getTitle();

						AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
						builder.setSingleChoiceItems(historys, current, new DialogInterface.OnClickListener() {
						    @Override
						    public void onClick(DialogInterface dialog, int which) {
						    	serverWebs.get(webIndex).goBackOrForward(which - current);
						    	dialog.dismiss();
						    }
						});
						builder.show();
					}
				}
				return true;
			}
		});

		imgRefresh = (ImageView) findViewById(R.id.refresh);
		imgRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (loadProgress.getVisibility() == View.VISIBLE) {
					imgRefresh.setImageResource(R.drawable.refresh);
					// webpage is loading then stop it
					serverWebs.get(webIndex).stopLoading();
					loadProgress.setVisibility(View.INVISIBLE);
				} else {// reload the webpage
					imgRefresh.setImageResource(R.drawable.stop);
					loadProgress.setProgress(1);// to make it seems feedback more fast
					String url = serverWebs.get(webIndex).getUrl();
					String m_url = serverWebs.get(webIndex).m_url;
					if (m_url.equals(url)) 
						serverWebs.get(webIndex).reload();
					else 
						serverWebs.get(webIndex).loadUrl(m_url);
				}
			}
		});
		imgHome = (ImageView) findViewById(R.id.home);
		imgHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if ((m_homepage != null) && (!"".equals(m_homepage))) serverWebs.get(webIndex).loadUrl(m_homepage);
				else if (!HOME_PAGE.equals(serverWebs.get(webIndex).m_url)) loadPage();
			}
		});
		imgHome.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				CharSequence operations[] = new CharSequence[] {
						getString(R.string.full_screen),
						getString(R.string.incognito),
						getString(R.string.page_updown),
						getString(R.string.block_image),
						getString(R.string.show_zoom),
						getString(R.string.overview_page),
						getString(R.string.hide)
				};
				boolean checkeditems[] = new boolean[] {
						!(showUrl || showControlBar || showStatusBar),
						incognitoMode, 
						updownButton, 
						blockImage, 
						serverWebs.get(webIndex).zoomVisible, 
						overviewPage, 
						false
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMultiChoiceItems(operations, checkeditems, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which, boolean selected) {
						WebSettings localSettings = serverWebs.get(webIndex).getSettings();
						wrapWebSettings webSettings = new wrapWebSettings(localSettings);
						switch(which) {
						case 0:
							boolean fullScreen = selected;
							showUrl = !fullScreen;
							showControlBar = !fullScreen;
							showStatusBar = !fullScreen;
							if (fullScreen) {
								adContainer.setVisibility(View.GONE);// hide ad when fullscreen
								getWindow().setFlags(
										WindowManager.LayoutParams.FLAG_FULLSCREEN,
										WindowManager.LayoutParams.FLAG_FULLSCREEN);								
							}
							else {
								adContainer.setVisibility(View.VISIBLE);
								getWindow().clearFlags(
										WindowManager.LayoutParams.FLAG_FULLSCREEN);
							}
							setWebpagesLayout();
							sEdit.putBoolean("show_url", showUrl);
							sEdit.putBoolean("show_controlBar", showControlBar);
							sEdit.putBoolean("show_statusBar", showStatusBar);
							break;
						case 1:
							incognitoMode = selected;
							sEdit.putBoolean("incognito", incognitoMode);
							break;
						case 2:
							updownButton = selected;
							if (updownButton) upAndDown.setVisibility(View.VISIBLE);
							else upAndDown.setVisibility(View.INVISIBLE);
							sEdit.putBoolean("up_down", updownButton);
							break;
						case 3:
							blockImage = selected;
							localSettings.setBlockNetworkImage(blockImage);
							sEdit.putBoolean("block_image", blockImage);
							break;
						case 4:
							boolean showZoom = selected;
							if (webSettings.setDisplayZoomControls(showZoom)) {
								localSettings.setBuiltInZoomControls(showZoom);
								serverWebs.get(webIndex).zoomVisible = showZoom;
							} else {
								if (showZoom)
									serverWebs.get(webIndex).setZoomControl(View.VISIBLE);
								else
									serverWebs.get(webIndex).setZoomControl(View.GONE);
							}
							sEdit.putBoolean("show_zoom", showZoom);
							break;
						case 5:
							overviewPage = selected;
							//localSettings.setUseWideViewPort(overviewPage);
							localSettings.setLoadWithOverviewMode(overviewPage);
							sEdit.putBoolean("overview_page", overviewPage);
							break;
						case 6:
							moveTaskToBack(true);
							break;
						}
						sEdit.commit();
						dialog.dismiss();
					}
				});
				builder.show();
				return true;
			}
		});

		imgNew = (ImageView) findViewById(R.id.newpage);
		imgNew.setImageBitmap(util.generatorCountIcon(
				util.getResIcon(getResources(), R.drawable.newpage), 
				1, 
				2,
				dm.density,
				mContext));
		imgNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (webControl.getVisibility() == View.INVISIBLE) {
					if (urlLine.getLayoutParams().height == 0) setUrlHeight(true);// show url if hided
				
					webAdapter.notifyDataSetInvalidated();
					webControl.setVisibility(View.VISIBLE);
					webControl.bringToFront();
				} else webControl.setVisibility(View.INVISIBLE);
			}
		});
		imgNew.setOnLongClickListener(new OnLongClickListener() {// long click to show history
			@Override
			public boolean onLongClick(View v) {
				CharSequence historys[] = new CharSequence[mHistory.size()];
				for (int i = 0; i < mHistory.size(); i++)
				{
					historys[i] = mHistory.get(i).m_title;
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(getString(R.string.history));
				builder.setItems(historys, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				        // the user clicked on engine[which]
						serverWebs.get(webIndex).loadUrl(mHistory.get(which).m_url);
				    }
				});
				builder.show();
				return true;
			}
		});

		adContainer = (FrameLayout) findViewById(R.id.adContainer);
		createAd();
		setLayout();
		setWebpagesLayout();
		initUpDown();

		urlLine.bringToFront();// set the z-order
		webTools.bringToFront();

		final FrameLayout toolAndAd = (FrameLayout) findViewById(R.id.webtoolnad);
		toolAndAd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {//reverse the position of webtoolbutton and ad
				LayoutParams lp1 = imageBtnList.getLayoutParams();
				LayoutParams lp2 = adContainer2.getLayoutParams();
				
				lp1.height = (int)(50 * dm.density);
				lp2.height = (int)(40 * dm.density);
				
				adContainer2.setLayoutParams(lp1);
				imageBtnList.setLayoutParams(lp2);
				
				if (adContainer2.getVisibility() == View.GONE) 
					return; // no need to change position if not width enough

				revertCount++;
				if ((revertCount > 1) && (revertCount % 2 == 0))
					needRevert = true;
				else needRevert = false;
				webList.invalidateViews();
			}
		});
		toolAndAd.setOnLongClickListener(new OnLongClickListener() {// long click tool bar to open menu
			@Override
			public boolean onLongClick(View arg0) {
				if (menuDialog == null) initMenuDialog();
				menuDialog.show();
				//onMenuOpened(0, null);
				return true;
			}
		});
		
		try {// there are a null pointer error reported for the if line below,
				// hard to reproduce, maybe someone use instrument tool to test
				// it. so just catch it.
			if (Intent.ACTION_MAIN.equals(getIntent().getAction())) {
				if (!incognitoMode && readPages("pages")) {
					closePage(0, false);// the first page is no use if open saved url or homepage
				}
				else if ((m_homepage != null) && !"".equals(m_homepage)) serverWebs.get(webIndex).loadUrl(m_homepage);
				else loadPage();// load about:blank if no url saved or homepage specified
			}
			else serverWebs.get(webIndex).loadUrl(getIntent().getDataString());
		} catch (Exception e) {}

		// for package added
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		registerReceiver(packageReceiver, filter);

		filter = new IntentFilter("simpleHome.action.START_DOWNLOAD");
		registerReceiver(downloadReceiver, filter);

		filter = new IntentFilter("android.intent.action.SCREEN_OFF");
		registerReceiver(screenLockReceiver, filter);
	}

	boolean readPages(String filename) {
		ObjectInputStream ois = null;
		FileInputStream fi = null;
		String url = null;
		try {
			fi = openFileInput(filename);
			ois = new ObjectInputStream(fi);
			while ((url = (String) ois.readObject()) != null) {
				if (!"".equals(url)) openNewPage(url, webAdapter.getCount(), false, false);
			}
		} catch (EOFException e) {// only when read eof need send out msg.
			try {
				ois.close();
				fi.close();
			} catch (Exception e1) {}
		} catch (Exception e) {}

		return ((url != null) && !"".equals(url));
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(screenLockReceiver);
		unregisterReceiver(downloadReceiver);
		unregisterReceiver(packageReceiver);

		if (adview != null) adview.destroy();

		super.onDestroy();
	}

	BroadcastReceiver screenLockReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
		}
	};

	BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			startDownload(intent.getStringExtra("url"), null, "yes");
		}
	};

	BroadcastReceiver packageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				// it always in the format of package:x.y.z
				String packageName = intent.getDataString().split(":")[1];
				Object id = appstate.downloadAppID.get(packageName);
				if (id != null) {
					// cancel download notification if install succeed
					// only remove the notification internal id, not delete file.
					// otherwise when user click the ad again, it will download again.
					// traffic is important than storage.
					// user can download it manually when click downloads
					appstate.nManager.cancel((Integer) id);
					appstate.downloadAppID.remove(packageName);
				}
			}
		}
	};

	void gotoUrl(String url) {
		if (HOME_BLANK.equals(url)) url = HOME_PAGE;
		else if (!url.contains("://")) {
			if (!url.contains(".")) {
				if ((!incognitoMode) && (siteArray.indexOf(url) < 0)) {
					siteArray.add(url);
					urlAdapter.add(url);
					try {// write to /data/data/easy.browser/files/
						FileOutputStream fo = openFileOutput("searchwords", MODE_APPEND);
						ObjectOutputStream oos = new ObjectOutputStream(fo);
						oos.writeObject(url);// record new search word
						oos.flush();
						oos.close();
						fo.close();
					} catch (Exception e) {}
				}
				
				switch (searchEngine) {
				case 1:// bing
					url = "http://www.bing.com/search?q=" + url;
					break;
				case 2:// easou
					url = "http://ad2.easou.com:8080/j10ad/ea2.jsp?channel=11&wver=t&cid=bip1065_10713_001&key=" + url;
					break;
				case 3:// google
					url = "http://www.google.com/search?q=" + url;
					break;
				case 4:// yandex
					url = "http://yandex.ru/touchsearch?clid=1911434&text=" + url;
					break;
				case 5:// DuckDuckGo
				default:
					url = "https://duckduckgo.com/?t=easybrowser&q=" + url;
					break;
				}
			}
			else url = "http://" + url;
		}
		
		if (!url.equals(serverWebs.get(webIndex).getUrl())) serverWebs.get(webIndex).loadUrl(url);//only load page if input different url
	}

	void changePage(int position) {
		while (webpages.getDisplayedChild() != position)
			webpages.showNext();
		for (int i = 0; i < serverWebs.size(); i++) {
			serverWebs.get(i).isForeground = false;
			freeMemory(serverWebs.get(i));
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
			localSettings.setUserAgentString(selectUA(ua));
		localSettings.setTextSize(textSize);
		localSettings.setBlockNetworkImage(blockImage);
		if (cachePrefer)
			localSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		else
			localSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		localSettings.setJavaScriptCanOpenWindowsAutomatically(blockPopup);
		// localSettings.setSupportMultipleWindows(true);
		localSettings.setJavaScriptEnabled(!blockJs);
		new wrapWebSettings(localSettings)
				.setLoadWithOverviewMode(overviewPage);

		if (serverWebs.get(position).mProgress > 0) {
			imgRefresh.setImageResource(R.drawable.stop);
			loadProgress.setVisibility(View.VISIBLE);
			loadProgress.setProgress(serverWebs.get(position).mProgress);
		} else {
			imgRefresh.setImageResource(R.drawable.refresh);
			loadProgress.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {// open file from sdcard
		if (!Intent.ACTION_MAIN.equals(intent.getAction())) {
			String uri = intent.getDataString();
			if (uri == null)
				return;

			boolean found = false;
			int blankIndex = -1;
			for (int i = 0; i < serverWebs.size(); i++) {
				String url = serverWebs.get(i).m_url;
				if ((uri + "/").equals(url) || uri.equals(url)) {
					changePage(i); // show correct page
					found = true;
					break;
				} else if (HOME_PAGE.equals(url))
					blankIndex = i;
			}

			if (!found) {
				if (blankIndex < 0)
					openNewPage(uri, webIndex + 1, true, true);
				else {
					serverWebs.get(blankIndex).loadUrl(uri);
					changePage(blankIndex);
				}
			}
		}

		super.onNewIntent(intent);
	}

	void addRemoveFavo(String url, String title) {
		for (int i = mBookMark.size() - 1; i >= 0; i--)
			if (mBookMark.get(i).m_url.equals(url)) {
				removeFavo(i);
				return;
			}

		addFavo(url, title);// add favo if not found it
	}
	
	void removeFavo(final int order) {
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
									bookmarkChanged = true;
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

	void removeHistory(final int order) {
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
									historyChanged = true;
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

	private void addFavo(final String url, final String title) {
		if (url == null) {
			Toast.makeText(mContext, "null url", Toast.LENGTH_LONG).show();
			return;
		}

		LinearLayout favoView = (LinearLayout) getLayoutInflater().inflate(
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
								String site = "";
								String[] tmp = url.split("/");
								if (tmp.length >= 2)
									site = tmp[2];// if url is
								// http://m.baidu.com, then
								// url.split("/")[2] is
								// m.baidu.com
								else
									site = tmp[0];

								String title = titleText.getText().toString();
								if ("".equals(title))
									title += (char) 0xa0;// add a blank
								// character to
								// occupy the space
								TitleUrl titleUrl = new TitleUrl(title, url,
										site);
								mBookMark.add(titleUrl);
								// sort by name
								Collections.sort(mBookMark, new MyComparator());
								//loadPage(false);

								bookmarkChanged = true;
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

	private boolean openNewPage(String url, int newIndex, boolean changeToNewPage, boolean closeIfCannotBack) {
		boolean result = true;

		if (webAdapter.getCount() == 9) {// max pages is 9
			Toast.makeText(mContext, R.string.nomore_pages, Toast.LENGTH_LONG).show();
			return false; // not open new page if got max pages
		} else {
			webAdapter.insert(new MyWebview(mContext), newIndex);
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

	void hideSearchBox() {
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
		searchBar.setVisibility(View.INVISIBLE);
		matchCount = 0;
		// remove the match by an impossible search
		serverWebs.get(webIndex).findAll("jingtao10175jtbuaa@gmail.com");
		searchHint.setText("");
	}

	void findMatchCount() {
		toSearch = etSearch.getText().toString();
		matchCount = serverWebs.get(webIndex).findAll(toSearch);
		if (matchCount > 0) {
			try {
				Method m = WebView.class.getMethod("setFindIsUp", Boolean.TYPE);
				m.invoke(serverWebs.get(webIndex), true);
			} catch (Throwable ignored) {
			}

			matchIndex = matchCount;
			while (matchIndex > 0) {
				serverWebs.get(webIndex).findNext(false);
				// move to select the first match
				matchIndex -= 1;
			}
		}
	}

	void actionBack() {
		// press Back key in webview will go backword.
		if (webControl.getVisibility() == View.VISIBLE)
			webControl.setVisibility(View.INVISIBLE);// hide web control
		else if ((searchBar != null) && searchBar.getVisibility() == View.VISIBLE)
			hideSearchBox();
		else if (HOME_BLANK.equals(webAddress.getText().toString())) {
			// hide browser when click back key on homepage.
			// this is a singleTask activity, so if return
			// super.onKeyDown(keyCode, event), app will exit.
			// when use click browser icon again, it will call onCreate,
			// user's page will not reopen.
			// singleInstance will work here, but it will cause
			// downloadControl not work? or select file not work?
			if (serverWebs.size() == 1)
				moveTaskToBack(true);
			else closePage(webIndex, false); // close blank page if more than one page
		} else if (serverWebs.get(webIndex).canGoBack())
			serverWebs.get(webIndex).goBack();
		else
			closePage(webIndex, false);// close current page if can't go back
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() == 0) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				actionBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	void readTextSize(SharedPreferences sp) {
		int iTextSize = sp.getInt("textsize", 2);
		switch (iTextSize) {
		case 1:
			textSize = TextSize.LARGER;
			break;
		case 2:
			textSize = TextSize.NORMAL;
			break;
		case 3:
			textSize = TextSize.SMALLER;
			break;
		case 4:
			textSize = TextSize.SMALLEST;
			break;
		case 5:
			textSize = TextSize.LARGEST;
			break;
		}
	}

	class ClearFolderTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {// ugly to handle png file in this way
			if ("png".equals(params[1]))
				clearFolder(new File(params[0]), "png");
			else {
				clearFolder(new File(params[0]), "");
				if (!params[0].equals(params[1]))
					clearFolder(new File(params[1]), "");
			}
			
			return null;
		}
	}

	static int clearFolder(final File dir, String suffix) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					// first delete subdirectories recursively
					if (child.isDirectory())
						deletedFiles += clearFolder(child, suffix);
					// then delete the files and subdirectories in this dir
					if (!"".equals(suffix)) {
						if (child.getName().endsWith(suffix))
							if (child.delete()) deletedFiles++;
					}
					else if (child.delete()) deletedFiles++;
				}
			} catch (Exception e) {
			}
		}
		return deletedFiles;
	}

	String selectUA(int ua) {
		switch (ua) {
		case 2:// ipad
			return "Mozilla/5.0 (iPad; U; CPU  OS 4_1 like Mac OS X; en-us)AppleWebKit/532.9(KHTML, like Gecko) Version/4.0.5 Mobile/8B117 Safari/6531.22.7";
		case 3:// iPhone
			return "Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3";
		case 4:// black berry
			return "Mozilla/5.0 (BlackBerry; U; BlackBerry 9800; en-US) AppleWebKit/534.1+ (KHTML, like Gecko)";
		case 5:// chrome
			return "Mozilla/5.0 (X11; Linux i686) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.151 Safari/535.19";
		case 6:// firefox
			return "Mozilla/5.0 (Windows NT 6.1; rv:12.0) Gecko/20120403211507 Firefox/12.0";
		case 7:// ie
			return "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0; Trident/4.0)";
		case 8:// nokia
			return "User-Agent: Mozilla/5.0 (SymbianOS/9.1; U; [en]; Series60/3.0 NokiaE60/4.06.0) AppleWebKit/413 (KHTML, like Gecko) Safari/413";
		case 9:// safari
			return "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en-us) AppleWebKit/48 (like Gecko) Safari/48";
		case 10:// wp
			return "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0";
		}
		return "";
	}

	String getEncoding(int iEncoding) {
		String tmpEncoding = "AUTOSELECT";
		switch (iEncoding) {
		case 1:
			tmpEncoding = "gbk";
			break;
		case 2:
			tmpEncoding = "big5";
			break;
		case 3:
			tmpEncoding = "gb2312";
			break;
		case 4:
			tmpEncoding = "utf-8";
			break;
		case 5:
			tmpEncoding = "iso-8859-1";
			break;
		case 6:
			tmpEncoding = "ISO-2022-JP";
			break;
		case 7:
			tmpEncoding = "SHIFT_JIS";
			break;
		case 8:
			tmpEncoding = "EUC-JP";
			break;
		case 9:
			tmpEncoding = "EUC-KR";
			break;
		}
		return tmpEncoding;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(). Bug on API Level > 11. refer to
		// http://stackoverflow.com/questions/7469082
	}

	@Override
	protected void onPause() {
		if (historyChanged) {
			WriteTask wtask = new WriteTask();
			wtask.execute("history");
		}
		if (bookmarkChanged) {
			WriteTask wtask = new WriteTask();
			wtask.execute("bookmark");
		}

		sEdit.putBoolean("collapse1", collapse1);
		sEdit.putBoolean("collapse2", collapse2);
		sEdit.putBoolean("collapse3", collapse3);
		sEdit.putBoolean("show_zoom", serverWebs.get(webIndex).zoomVisible);
		sEdit.putBoolean("html5", serverWebs.get(webIndex).html5);
		sEdit.commit();

		try {
			if (baiduPause != null) baiduPause.invoke(this, this);
		} catch (Exception e) {}

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		//if (gotoSettings) gotoSettings = false;
		//else if (!clicked) createAd();

		try {
			if (baiduResume != null) baiduResume.invoke(this, this);
		} catch (Exception e) {}
	}

	@Override
	public void onStop() {
		super.onStop();

		//if (!gotoSettings) // will force close if removeAd in onResume. if transfer from activity A to activity B, then A.onPause()->B.onResume()->A.onStop()
			//removeAd();// ad will occupy cpu and data quota even in background
	}

	@Override
	public File getCacheDir() {
		// NOTE: this method is used in Android 2.1
		return getApplicationContext().getCacheDir();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
		setLayout();
	}

	void setLayout() {
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		if (dm.widthPixels < 320*dm.density) {
			imageBtnList.getLayoutParams().width = dm.widthPixels;
			adContainer2.setVisibility(View.GONE);
			adContainer.setVisibility(View.VISIBLE);
		}
		else if (dm.widthPixels <= 640*dm.density) {
			imageBtnList.getLayoutParams().width = (int) (320 * dm.density);
			adContainer2.setVisibility(View.GONE);			
			adContainer.setVisibility(View.VISIBLE);
		}
		else {
			imageBtnList.getLayoutParams().width = (int) (320 * dm.density);
			if (adview2 != null) adview2.loadAd();
			adContainer2.setVisibility(View.VISIBLE);
			adContainer.setVisibility(View.GONE);
		}		
	}

	void removeAd() {
		if (adview != null) {
			adContainer.removeViewAt(0);
			adview.destroy();
			adview = null;
		}
	}

	void createAd() {
		if (mAdAvailable) {
			//removeAd();

			//if ((cm == null) || (cm.getActiveNetworkInfo() == null)
			//		|| !cm.getActiveNetworkInfo().isConnected())
			//	return;// not create ad if network error

			adview = new wrapAdView(this, 0, "a14f3f6bc126143", mAppHandler);// AdSize.BANNER require 320*50
			if ((adview != null) && (adview.getInstance() != null)) {
				adContainer.addView(adview.getInstance());
				adview.loadAd();
			}

			adview2 = new wrapAdView(this, 0, "a14d662bba1e443", mAppHandler);// AdSize.BANNER require 320*50
			if ((adview2 != null) && (adview2.getInstance() != null)) { 
				adContainer2.addView(adview2.getInstance());
				adview2.loadAd();
			}
			
			interstitialAd = new wrapInterstitialAd(this, "a14be3f4ec2bb11", mAppHandler);
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
				//if (errorMsg != null) Toast.makeText(mContext, "Can't load AdMob. " + errorMsg, Toast.LENGTH_LONG).show();
			}
			else if (msg.what == -3) {
				interstitialAd.loadAd();
			}
		}
	}

	String getTopList(String splitter) {
		String fileDir = "<li style='background-image:url(file://" + getFilesDir().getAbsolutePath() + "/";
		
		StringBuilder sb = new StringBuilder("");	
		if (Locale.CHINA.equals(mLocale) || Locale.TAIWAN.equals(mLocale)) {
			sb.append(fileDir);
			sb.append("easybrowser.shupeng.com.png)'><a href='http://easybrowser.shupeng.com'>书朋小说网</a></li>");
			//sb.append("tiantian.m.the9.com.png)'><a href='http://tiantian.m.the9.com'>热门游戏</a></li>");
			sb.append(splitter);
			sb.append(fileDir);
			sb.append("weibo.com.png)'><a href='http://weibo.com'>新浪微博</a></li>");
			sb.append(splitter);
			// sb.append(fileDir);
			// sb.append("www.taobao.com.png)'><a href='http://www.taobao.com'>淘宝</a></li>");
			sb.append(fileDir);
			sb.append("wap.easou.com.png)'><a href='http://i5.easou.com/kw.m?tp=7&p=1&cid=bip1065_10713_001&esid=2GsaHSnARzA&si=a91fc744144b561694707ad18923b4f9&wver=t'>宜搜</a></li>");
			sb.append(splitter);
			//sb.append("<li><a href='http://www.9yu.co/index.html?c=2'>美图</a></li>");// no favicon
			// sb.append(fileDir);
			// sb.append("bpc.borqs.com.png)'><a href='http://bpc.borqs.com'>梧桐</a></li>");
		} else {
			// sb.append("<li><a href='http://www.1mobile.com/app/market/?cid=9'>1mobile</a></li>");// no favicon
			//if (mAdAvailable) sb.append("<li style='background-image:url(file:///android_asset/favicon.ico)'><a href='http://bpc.borqs.com/market.html?id=easy.browser.pro'>Ad free version of Easy Browser</a></li>"); // suspended
			sb.append(fileDir);
			sb.append("m.admob.com.png)'><a style='text-decoration: underline; color:#0000ff' onclick='javascript:window.JSinterface.showInterstitialAd()'>AdMob</a></li>");
			sb.append(splitter);
			sb.append(fileDir);
			sb.append("helpx.adobe.com.png)'><a href='http://tinyurl.com/4aflash'>Adobe Flash player install/update</a></li>");
			sb.append(splitter);
			sb.append("<li style='background-image:url(file:///android_asset/favicon.ico)'><a href='market://details?id=easy.browser.com'>Easy Browser Pro, no Ads</a></li>");
			sb.append(splitter);
			//sb.append(fileDir);
			//sb.append("duckduckgo.com.png)'><a href='https://duckduckgo.com?t=easybrowser&q=DuckDuckGo'>DuckDuckGo</a></li>");
			//sb.append(splitter);
			//sb.append(fileDir);
			//sb.append("m.facebook.com.png)'><a href='http://www.facebook.com'>Facebook</a></li>");
			//sb.append(splitter);
			sb.append(fileDir);
			sb.append("www.moborobo.com.png)'><a href='http://www.moborobo.com/app/mobomarket.html'>MoboMarket</a></li>");
			sb.append(splitter);
			//sb.append("<li><a href='file:///sdcard/'>SDcard</a></li>");
			//sb.append(splitter);
			//sb.append(fileDir);
			//sb.append("mobile.twitter.com.png)'><a href='http://twitter.com'>Twitter</a></li>");
			//sb.append(splitter);
			
			// additional top list for some locale
			if (Locale.JAPAN.equals(mLocale) || Locale.JAPANESE.equals(mLocale)) {
				sb.append(fileDir);
				sb.append("m.yahoo.co.jp.png)'><a href='http://www.yahoo.co.jp'>Yahoo!JAPAN</a></li>");
				sb.append(splitter);
			} else if ("ru_RU".equals(mLocale.toString())) {
				sb.append(fileDir);
				sb.append("www.yandex.ru.png)'><a href='http://www.yandex.ru/?clid=1911433'>Яндекс</a></li>");
				sb.append(splitter);
			}
		}
		
		return sb.toString();
	}
	
	String getBookmark(String splitter) {
		String fileDir = "<li style='background-image:url(file://" + getFilesDir().getAbsolutePath() + "/";
		
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < mBookMark.size(); i++) {
			sb.append(fileDir);
			sb.append(mBookMark.get(i).m_site);
			sb.append(".png)'><input class='bookmark' type='checkbox' style='display:none; margin-right:20px'><a href='");
			sb.append(mBookMark.get(i).m_url);
			sb.append("'>");
			sb.append(mBookMark.get(i).m_title);
			sb.append("</a></li>");
			sb.append(splitter);
		}
		
		return sb.toString();
	}
	
	String getHistory(String splitter) {
		String fileDir = "<li style='background-image:url(file://" + getFilesDir().getAbsolutePath() + "/";
		
		StringBuilder sb = new StringBuilder("");			
		for (int i = mHistory.size() - 1; i >= 0; i--) {
			sb.append(fileDir);
			sb.append(mHistory.get(i).m_site);
			sb.append(".png)'><input class='history' type='checkbox' style='display:none; margin-right:20px'><a href='");
			sb.append(mHistory.get(i).m_url);
			sb.append("'>");
			sb.append(mHistory.get(i).m_title);
			sb.append("</a></li>");
			sb.append(splitter);
		}
		
		return sb.toString();
	}
	
	void updateBookmark() {
		serverWebs.get(webIndex).loadUrl("javascript:inject(\"2::::" + getBookmark("....") + "\");");// call javascript to inject bookmark
	}
	
	void updateHistory() {
		serverWebs.get(webIndex).loadUrl("javascript:inject(\"3::::" + getHistory("....") + "\");");// call javascript to inject bookmark
	}
	
	void updateHomePage() {
		serverWebs.get(webIndex).loadUrl("javascript:setTitle(\"" + getString(R.string.browser_name) + "\");");
		// top bar
		String tmp = getString(R.string.top);
		if (mAdAvailable) {
			if (countDown > 0) tmp += getString(R.string.url_can_longclick);
			serverWebs.get(webIndex).loadUrl("javascript:setTitleBar(\"1," + collapse1 + "," + tmp + "\");");
			serverWebs.get(webIndex).loadUrl("javascript:collapse(\"1," + !collapse1 + "\");");
			serverWebs.get(webIndex).loadUrl("javascript:inject(\"1::::" + getTopList("....") + "\");");// call javascript to inject toplist
		}
		else
			serverWebs.get(webIndex).loadUrl("javascript:hideTop();");// hide toplist for pro version
		
		// bookmark bar
		tmp = getString(R.string.bookmark);
		if (countDown > 0) tmp += getString(R.string.pic_can_longclick);
		serverWebs.get(webIndex).loadUrl("javascript:setTitleBar(\"2," + collapse2 + "," + tmp + "\");");

		// history bar
		tmp = getString(R.string.history);
		if (countDown > 0) tmp += getString(R.string.text_can_longclick);
		serverWebs.get(webIndex).loadUrl("javascript:setTitleBar(\"3," + collapse3 + "," + tmp + "\");");

		serverWebs.get(webIndex).loadUrl("javascript:collapse(\"2," + !collapse2 + "\");");
		serverWebs.get(webIndex).loadUrl("javascript:collapse(\"3," + !collapse3 + "\");");
		
		updateBookmark();
		updateHistory();

		serverWebs.get(webIndex).loadUrl("javascript:setButton(\"" + getString(R.string.edit_home) + "," + getString(R.string.delete) + "," + getString(R.string.cancel) + "\");");
		
		if (countDown > 0) countDown -= 1;
	}
	
	void loadPage() {// load home page
		serverWebs.get(webIndex).getSettings().setJavaScriptEnabled(true);

		WebBackForwardList wbfl = serverWebs.get(webIndex).copyBackForwardList();
		if (wbfl != null) {
			int size = wbfl.getSize();
			int current = wbfl.getCurrentIndex();
			for (int i = 0; i < size; i++) {
				if (HOME_PAGE.equals(wbfl.getItemAtIndex(i).getUrl())) {
					if (i-current != 0) {// webview will do nothing if equal to 0
						serverWebs.get(webIndex).goBackOrForward(i - current);
						return;
					}
					else break;
				}
			}
		}
		
		serverWebs.get(webIndex).loadUrl(HOME_PAGE);
	}

	class WriteTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			if ("history".equals(params[0])) {
				writeBookmark("history", mHistory);
				historyChanged = false;
			} else {
				writeBookmark("bookmark", mBookMark);
				bookmarkChanged = false;
			}

			return null;
		}
	}

	void writeBookmark(String filename, ArrayList<TitleUrl> bookmark) {
		try {// write to /data/data/easy.browser/files/
			FileOutputStream fo = openFileOutput(filename, 0);
			ObjectOutputStream oos = new ObjectOutputStream(fo);
			TitleUrl tu;
			for (int i = 0; i < bookmark.size(); i++) {
				tu = bookmark.get(i);
				oos.writeObject(tu.m_title);
				oos.writeObject(tu.m_url);
				oos.writeObject(tu.m_site);
			}
			oos.flush();
			oos.close();
			fo.close();
		} catch (Exception e) {
		}

		if (!noSdcard)
		try {// try to write to /sdcard/simpleHome/bookmark/
			File file = new File(appstate.downloadPath + "bookmark/" + filename);
			FileOutputStream fo = new FileOutputStream(file, false);
			ObjectOutputStream oos = new ObjectOutputStream(fo);
			TitleUrl tu;
			for (int i = 0; i < bookmark.size(); i++) {
				tu = bookmark.get(i);
				oos.writeObject(tu.m_title);
				oos.writeObject(tu.m_url);
				oos.writeObject(tu.m_site);
			}
			oos.flush();
			oos.close();
			fo.close();
		} catch (Exception e) {
		}
	}

	void getHistoryList() {
		String[] sHistoryBookmarksProjection = new String[] {
				Browser.BookmarkColumns._ID, Browser.BookmarkColumns.TITLE,
				Browser.BookmarkColumns.URL, Browser.BookmarkColumns.VISITS,
				Browser.BookmarkColumns.DATE, Browser.BookmarkColumns.CREATED,
				Browser.BookmarkColumns.BOOKMARK,
				Browser.BookmarkColumns.FAVICON };

		String orderClause = Browser.BookmarkColumns.DATE + " DESC";
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(Browser.BOOKMARKS_URI,
					sHistoryBookmarksProjection, null, null, orderClause);
		} catch (Exception e) {
		}

		if (cursor != null) {
			try {if (cursor.moveToFirst()) {
				int columnTitle = cursor
						.getColumnIndex(Browser.BookmarkColumns.TITLE);
				int columnUrl = cursor
						.getColumnIndex(Browser.BookmarkColumns.URL);
				int columnBookmark = cursor
						.getColumnIndex(Browser.BookmarkColumns.BOOKMARK);

				while (!cursor.isAfterLast()) {
					String url = cursor.getString(columnUrl).trim();
					String site = getSite(url);
					TitleUrl titleUrl = new TitleUrl(
							cursor.getString(columnTitle), url, site);
					if (cursor.getInt(columnBookmark) >= 1)
						mSystemBookMark.add(titleUrl);
					else
						mSystemHistory.add(titleUrl);

					cursor.moveToNext();
				}
			}} catch (Exception e) {}
			cursor.close();
		}
	}

	ArrayList<TitleUrl> readBookmark(String filename) {
		ArrayList<TitleUrl> bookmark = new ArrayList<TitleUrl>();
		ObjectInputStream ois = null;
		FileInputStream fi = null;
		try {// read favorite or shortcut data from sdcard at first. if fail
			 // then read from /data/data
			if (noSdcard || noHistoryOnSdcard) 
				fi = openFileInput(filename);
			else {
				File file = new File(appstate.downloadPath + "bookmark/" + filename);
				fi = new FileInputStream(file);
			}
			ois = new ObjectInputStream(fi);
			TitleUrl tu;
			String title, url, site;
			while ((title = (String) ois.readObject()) != null) {
				url = (String) ois.readObject();
				site = (String) ois.readObject();
				tu = new TitleUrl(title, url, site);
				bookmark.add(tu);
			}
		} catch (EOFException e) {// only when read eof need send out msg.
			try {
				ois.close();
				fi.close();
			} catch (Exception e1) {}
		} catch (Exception e) {}

		return bookmark;
	}

}
