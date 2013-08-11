package common.lib;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.RejectedExecutionException;

import base.lib.StringComparator;
import base.lib.WebUtil;
import base.lib.WrapAdView;
import base.lib.WrapInterstitialAd;
import base.lib.util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Browser;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.webkit.WebSettings.TextSize;
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

public class SimpleBrowser extends Activity {
	public String HOME_PAGE = "file:///android_asset/home.html";
	public final String HOME_BLANK = "about:blank";
	public String browserName;
	public String m_homepage = null;

	public Context mContext;

	ListView webList;

	// Ads
	public LinearLayout adContainer;
	public LinearLayout adContainer2;	
	public WrapAdView adview = null, adview2 = null;
	public WrapInterstitialAd interstitialAd = null;
	public boolean interstitialAdClicked = false;
	public static boolean mAdAvailable;
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
			if (msg.what == 0) {// show ad when get ad
				adContainer.setVisibility(View.VISIBLE);
			}
			else if (msg.what == -1) {// hide ad when can't get ad
				//adContainer.setVisibility(View.GONE);
			}
			else if (msg.what == 1) {// remove ad when click ad // disabled
				removeAd();
			}
			else if (msg.what == -2) {// fail to get InterstitialAd
				Bundle data = msg.getData();
				String errorMsg = data.getString("msg");
				//if (errorMsg != null) Toast.makeText(mContext, "Can't load AdMob. " + errorMsg, Toast.LENGTH_LONG).show();
			}
			else if (msg.what == -3) {// try to load InterstitialAd when click link on homepage
				interstitialAd.loadAd();
			}
			else if (msg.what == -4) {// show InterstitialAd if it is ready
				if (interstitialAdClicked) {
					interstitialAd.show();
					interstitialAdClicked = false;
				}
			}
		}
	}
	public AppHandler mAppHandler = new AppHandler();
	public DisplayMetrics dm;

	// search
	public EditText etSearch;
	public TextView searchHint;
	public RelativeLayout searchBar;
	public ImageView imgSearchNext, imgSearchPrev, imgSearchClose;
	public String toSearch = "";
	int matchCount = 0, matchIndex = 0;

	// page up and down button
	public LinearLayout upAndDown;

	// source dialog
	AlertDialog m_sourceDialog = null;
	public String sourceOrCookie = "";
	public String subFolder = "source";


	// settings
	public SharedPreferences sp;
	public Editor sEdit;

	public boolean showUrl = true;
	public boolean showControlBar = true;
	public boolean showStatusBar = true;
	public final int urlHeight = 40, barHeight = 40;
	public int rotateMode = 1;
	public boolean incognitoMode = false;
	public boolean updownButton = true;
	public boolean snapFullWeb = false;
	public boolean blockImage = false;
	public boolean cachePrefer = false;
	public boolean blockPopup = false;
	public boolean blockJs = false;
	public TextSize textSize = TextSize.NORMAL;
	final int historyCount = 30;
	public long html5cacheMaxSize = 1024 * 1024 * 8;
	public int ua = 0;
	public int searchEngine = 5;
	public int shareMode = 2;
	public int SETTING_RESULTCODE = 1002;
	public boolean enableProxy = false;
	public int localPort;
	public boolean overviewPage = false;
	public Locale mLocale;

	public ImageView imgAddFavo;
	public ImageView imgGo;

	public int revertCount = 0;
	public boolean needRevert = false;

	// bookmark and history
	public boolean historyChanged = false, bookmarkChanged = false, downloadsChanged = false;
	public boolean noSdcard = false;
	public boolean noHistoryOnSdcard = false;
	public boolean firstRun = false;

	// browser related
	public ArrayList<MyWebView> serverWebs = new ArrayList<MyWebView>();
	public int webIndex;
	public ArrayList<TitleUrl> mHistory = new ArrayList<TitleUrl>();
	public ArrayList<TitleUrl> mBookMark = new ArrayList<TitleUrl>();
	public ArrayList<TitleUrl> mSystemHistory = new ArrayList<TitleUrl>();
	public ArrayList<TitleUrl> mSystemBookMark = new ArrayList<TitleUrl>();
	public ArrayList<TitleUrl> mDownloads = new ArrayList<TitleUrl>();
	public ArrayList<String> siteArray = new ArrayList<String>();
	public ArrayAdapter<String> urlAdapter, emptyUrlAdapter;
	public AutoCompleteTextView webAddress;
	public ProgressBar loadProgress;
	public ImageView imgNext, imgPrev, imgRefresh, imgNew;
	public WebAdapter webAdapter;
	public LinearLayout webTools, webControl, urlLine;
	public MyViewFlipper webpages;

	public WrapValueCallback mUploadMessage;
	public final int FILECHOOSER_RESULTCODE = 1001;

	public InputMethodManager imm;
	public GridView menuGrid = null;

	LinearLayout imageBtnList;
	RelativeLayout webs;
	Button btnNewpage;

	ConnectivityManager cm;

	// upload related
	static boolean mValueCallbackAvailable;
	static {
		try {
			WrapValueCallback.checkAvailable();
			mValueCallbackAvailable = true;
		} catch (Throwable t) {
			mValueCallbackAvailable = false;
		}
	}


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
	
	public MyApp appstate;

	// snap dialog
	public ImageView snapView;
	public Bitmap bmp;
	public AlertDialog snapDialog = null;
	public void initSnapDialog(String browserName) {
		snapView = (ImageView) getLayoutInflater().inflate(
				R.layout.snap_browser, null);
		snapDialog = new AlertDialog.Builder(mContext)
				.setView(snapView)
				.setTitle(browserName)
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
							String title = serverWebs.get(webIndex).getTitle();
							if (title == null) title = WebUtil.getSite(serverWebs.get(webIndex).m_url);
							title += "_snap.png";
							String site = appstate.downloadPath + "snap/";
							String snap = site + title;
							FileOutputStream fos = new FileOutputStream(snap);
							bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
							fos.close();
							Toast.makeText(getBaseContext(), getString(R.string.save) + " " + snap, Toast.LENGTH_LONG).show();
							addDownloads(new TitleUrl(title, "file://" + snap, serverWebs.get(webIndex).m_url));
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
					cltask.execute(appstate.dataPath + "files/", "png");
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

				String[] paras = {"", "", ""};
				if (clearHistory) {
					mHistory.clear();
					paras[0] = "history";
					clearFile("searchwords");
					siteArray.clear();
					urlAdapter.clear();
				}

				if (clearBookmark) {
					mBookMark.clear();
					paras[1] = "bookmark";
				}

				boolean clearDownloads = sp.getBoolean("clear_downloads", false);
				if (clearDownloads) {
					mDownloads.clear();
					paras[2] = "downloads";
					updateDownloads();
				}

				if (clearHistory || clearBookmark || clearDownloads) 
					executeWtask(paras);

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
			}

			// default to full screen now
			snapFullWeb = sp.getBoolean("full_web", false);

			incognitoMode = sp.getBoolean("incognito", false);
			if (!incognitoMode) webAddress.setAdapter(urlAdapter);
			else webAddress.setAdapter(emptyUrlAdapter);

			updownButton = sp.getBoolean("up_down", false);
			if (updownButton) upAndDown.setVisibility(View.VISIBLE);
			else upAndDown.setVisibility(View.GONE);
			
			shareMode = sp.getInt("share_mode", 1);
			
			searchEngine = sp.getInt("search_engine", 5);

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
				localSettings.setUserAgentString(WebUtil.selectUA(ua));

			// hideExit = sp.getBoolean("hide_exit", true);

			textSize = WebUtil.readTextSize(sp); // no need to reload page if fontSize changed
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

			WrapWebSettings webSettings = new WrapWebSettings(localSettings);
			overviewPage = sp.getBoolean("overview_page", false);

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

			String tmpEncoding = WebUtil.getEncoding(sp.getInt("encoding", 0));
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

	public void setWebpagesLayout() {
		setUrlHeight(showUrl);
		setBarHeight(showControlBar);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		if (showUrl)
			lp.addRule(RelativeLayout.BELOW, R.id.urlline);
		else
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		if (showControlBar)
			lp.addRule(RelativeLayout.ABOVE, R.id.webtools);
		else 
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		webs.setLayoutParams(lp);
		webs.requestLayout();
	}
	
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		menuOpenAction();
		return false;// show system menu if return true.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// must create one menu?
		return super.onCreateOptionsMenu(menu);
	}

	public void initSourceDialog(String browserName) {		
		m_sourceDialog = new AlertDialog.Builder(this)
		.setTitle(browserName)
		.setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {// share
			@Override
			public void onClick(DialogInterface dialog,	int which) {
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.fromParts("mailto", "", null));
				intent.putExtra(Intent.EXTRA_TEXT, sourceOrCookie);
				intent.putExtra(Intent.EXTRA_SUBJECT, serverWebs.get(webIndex).getTitle());
				if (!util.startActivity(intent, false, getBaseContext())) 
					shareUrl("", sourceOrCookie);
			}
		})
		.setNeutralButton(R.string.save, new DialogInterface.OnClickListener() {// save
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					String title = serverWebs.get(webIndex).getTitle();
					if (title == null) title = WebUtil.getSite(serverWebs.get(webIndex).m_url);
					title += "_" + subFolder + ".txt";
					String site = appstate.downloadPath + subFolder + "/";
					String snap = site + title;
					FileOutputStream fos = new FileOutputStream(snap);
					fos.write(sourceOrCookie.getBytes());
					fos.close();
					Toast.makeText(getBaseContext(), getString(R.string.save) + " " + snap, Toast.LENGTH_LONG).show();
					addDownloads(new TitleUrl(title, "file://" + snap, serverWebs.get(webIndex).m_url));
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
	
	public void showSourceDialog(String browserName) {
		if (m_sourceDialog == null) initSourceDialog(browserName);
		m_sourceDialog.setTitle(serverWebs.get(webIndex).getTitle());
		if (HOME_PAGE.equals(serverWebs.get(webIndex).getUrl()))
			m_sourceDialog.setIcon(R.drawable.explorer);
		else
			m_sourceDialog.setIcon(new BitmapDrawable(serverWebs.get(webIndex).getFavicon()));
		m_sourceDialog.setMessage(sourceOrCookie);
		m_sourceDialog.show();
	}

	public void initUpDown() {
		upAndDown = (LinearLayout) findViewById(R.id.up_down);
		if (updownButton) upAndDown.setVisibility(View.VISIBLE);
		else upAndDown.setVisibility(View.GONE);
		
		ImageView dragButton = (ImageView) findViewById(R.id.page_drag);
		dragButton.setAlpha(80);
		dragButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int[] temp = new int[] { 0, 0 };
				int eventAction = event.getAction();
				int x = (int) event.getRawX();
				int y = (int) event.getRawY();
				int offset = - urlLine.getHeight();
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
		
		// page up and down button
		ImageView upButton, downButton;
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
	
	public void initSearchBar() {
		imgSearchPrev = (ImageView) findViewById(R.id.search_prev);
		imgSearchPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				searchPrevAction();
			}
		});
		imgSearchNext = (ImageView) findViewById(R.id.search_next);
		imgSearchNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				searchNextAction();
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
				if (m_homepage != null) openNewPage(m_homepage, webIndex+1, true, false);
				else openNewPage("", webIndex+1, true, false);
			}
		});
		// web list
		webList = (ListView) findViewById(R.id.weblist);
		webList.setFadingEdgeLength(0);// no shadow when scroll
		webList.setScrollingCacheEnabled(false);
		webList.setAdapter(webAdapter);
	}
	
	public void initAddFavo() {
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
				listBookmark();
				return true;
			}
		});
	}

	public void initImgNext() {
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
				updownAction();
				return true;
			}
		});
	}

	public void initImgPrev() {
		imgPrev = (ImageView) findViewById(R.id.prev);
		imgPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				imgPrevClick();
			}
		});
		imgPrev.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				listPageHistory();
				return true;
			}
		});
	}
	
	public void initImgNew() {
		imgNew = (ImageView) findViewById(R.id.newpage);
		imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), 1, 2, dm.density, mContext));
		imgNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				imgNewClick();
			}
		});
		imgNew.setOnLongClickListener(new OnLongClickListener() {// long click to show history
			@Override
			public boolean onLongClick(View arg0) {
				listHistory();
				return true;
			}
		});
	}

	public void initToolbar() {
		final FrameLayout toolAndAd = (FrameLayout) findViewById(R.id.webtoolnad);
		toolAndAd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {//reverse the position of webtoolbutton and ad
				exchangePosition();
			}
		});
		
		LinearLayout buttonList = (LinearLayout) findViewById(R.id.imagebtn_list);
		buttonList.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {// do nothing when click imagebtn_list
			}
		});
	}
	
	public void initWebAddress() {
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
	}
	
	public void initImgRefresh() {
		imgRefresh = (ImageView) findViewById(R.id.refresh);
		imgRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				reloadPage();
			}
		});
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// mContext = getApplicationContext();//this will cause force close when
		// select locale in google translate
		mContext = this;
		appstate = ((MyApp) getApplicationContext());
		appstate.mActivity = this;
		appstate.mContext = this;
		webAdapter = new WebAdapter(this, serverWebs);

		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		// init settings
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		sEdit = sp.edit();
		readPreference();

		appstate.nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		// hide titlebar of application, must be before setting the layout
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// for package added
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addDataScheme("package");
		registerReceiver(packageReceiver, filter);

		filter = new IntentFilter("simpleHome.action.START_DOWNLOAD");
		registerReceiver(downloadReceiver, filter);
	}

	public void initViews() {
		initWebControl();
		loadProgress = (ProgressBar) findViewById(R.id.loadprogress);
		initAddFavo();
		
		initWebAddress();

		appstate.downloadPath = util.preparePath(mContext);
		appstate.dataPath = "/data/data/" + getPackageName() + "/";
		if (appstate.downloadPath == null) appstate.downloadPath = appstate.dataPath;
		if (appstate.downloadPath.startsWith(appstate.dataPath)) noSdcard = true;

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
		
		urlAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
		emptyUrlAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
		initSiteArray();

		cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		WebIconDatabase.getInstance().open(getDir("databases", MODE_PRIVATE).getPath());

		while (serverWebs.size() > 0) {
			MyWebView tmp = (MyWebView) webpages.getChildAt(0);
			if (tmp == null) break;//sometime it is null if close page very quick
			webAdapter.remove(tmp);
			webAdapter.notifyDataSetInvalidated();
			try {
				webpages.removeView(tmp);
			} catch (Exception e) {
			}// null pointer reported by 3 user. really strange.
			tmp.destroy();
			System.gc();
		}

		webTools = (LinearLayout) findViewById(R.id.webtools);
		urlLine = (LinearLayout) findViewById(R.id.urlline);
		webs = (RelativeLayout) findViewById(R.id.webs);
		
		adContainer = (LinearLayout) findViewById(R.id.adContainer);
		adContainer2 = (LinearLayout) findViewById(R.id.adContainer2);
		imageBtnList = (LinearLayout) findViewById(R.id.imagebtn_list);
		imageBtnList.bringToFront();
		
		if (!showStatusBar) 
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

		initImgNext();
		initImgPrev();
		initImgRefresh();
		initImgNew();

		setLayout();
		setWebpagesLayout();
		initUpDown();

		urlLine.bringToFront();// set the z-order
		webTools.bringToFront();

		initToolbar();
	}
	
	public void initFirstPage(MyWebView myWebView) {
		webIndex = 0;
		serverWebs.add(myWebView);
		webpages = (MyViewFlipper) findViewById(R.id.webpages);
		webpages.addView(serverWebs.get(webIndex));
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
	}
	
	public void exchangePosition() {
		//reverse the position of buttons and ads
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

	@Override
	protected void onDestroy() {
		unregisterReceiver(downloadReceiver);
		unregisterReceiver(packageReceiver);

		if (adview != null) adview.destroy();
		if (adview2 != null) adview2.destroy();

		super.onDestroy();
	}

	BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			appstate.startDownload(intent.getStringExtra("url"), null, "yes");
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

	@Override
	protected void onNewIntent(Intent intent) {// open file from sdcard
		newIntentAction(intent);

		super.onNewIntent(intent);
	}

	void addDownloads(TitleUrl tu) {
		for (int i = 0; i < mDownloads.size(); i++)
			if (mDownloads.get(i).m_url.equals(tu.m_url))
				return;
		
		mDownloads.add(tu);
		Collections.sort(mDownloads, new MyComparator());// sort by name
		updateDownloads();
	}
	
	public MyListAdapter downloadsAdapter;
	public void updateDownloads() {
		if (downloadsAdapter != null) {
			downloadsAdapter.notifyDataSetChanged();
		}
		downloadsChanged = true;
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Not call for super(). Bug on API Level > 11. refer to
		// http://stackoverflow.com/questions/7469082
	}

	@Override
	protected void onPause() {
		pauseAction();

		try {
			if (baiduPause != null) baiduPause.invoke(this, this);
		} catch (Exception e) {}

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			if (baiduResume != null) baiduResume.invoke(this, this);
		} catch (Exception e) {}
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

	public void setLayout() {
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		createAd(dm.widthPixels / dm.density);

		if (dm.widthPixels < 320*dm.density) {
			imageBtnList.getLayoutParams().width = dm.widthPixels;
			adContainer2.setVisibility(View.GONE);
		}
		else if (dm.widthPixels <= 640*dm.density) {
			imageBtnList.getLayoutParams().width = (int) (320 * dm.density);
			adContainer2.setVisibility(View.GONE);			
		}
		else {
			imageBtnList.getLayoutParams().width = (int) (320 * dm.density);
			if (adview2 != null) adview2.loadAd();
			adContainer2.setVisibility(View.VISIBLE);
		}
	}

	public void hideBookmark() {}
	public void updateBookmark() {}
	public void updateHistory() {}
	public void actionBack() {}
	public void createAd(float width) {}
	public void menuOpenAction() {}

	public void readPreference() {
		blockImage = sp.getBoolean("block_image", false);
		cachePrefer = sp.getBoolean("cache_prefer", false);
		blockPopup = sp.getBoolean("block_popup", false);
		blockJs = sp.getBoolean("block_js", false);
		// hideExit = sp.getBoolean("hide_exit", true);
		overviewPage = sp.getBoolean("overview_page", false);
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
		textSize = WebUtil.readTextSize(sp);// init the text size
		enableProxy = sp.getBoolean("enable_proxy", false);
		if (enableProxy) {
			localPort = sp.getInt("local_port", 1984);
			ProxySettings.setProxy(mContext, "127.0.0.1", localPort);
		}

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
	
	public boolean actioned = false;
	public void scrollUp() {
		actioned = true;
		if (!showUrl) setUrlHeight(showUrl);
		if (!showControlBar) setBarHeight(showControlBar);
		if (serverWebs.get(webIndex).myCanScrollVertically(-1))// pageUp/pageDown have animation which is slow
			serverWebs.get(webIndex).scrollBy(0, -serverWebs.get(webIndex).getHeight()+10);		
	}
	public void scrollDown() {
		actioned = true;
		if (!showUrl) setUrlHeight(showUrl);
		if (!showControlBar) setBarHeight(showControlBar);
		if (serverWebs.get(webIndex).myCanScrollVertically(1))
			serverWebs.get(webIndex).scrollBy(0, serverWebs.get(webIndex).getHeight()-10);		
	}

	public void selectEngine(CharSequence engine[]) {// identical
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
	}
	
	public void globalSetting() {// identical
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
				WrapWebSettings webSettings = new WrapWebSettings(localSettings);
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
					//setWebpagesLayout(); ////////////////////////////////////////////////////not identical. need more research///////////////////////
					sEdit.putBoolean("show_url", showUrl);
					sEdit.putBoolean("show_controlBar", showControlBar);
					sEdit.putBoolean("show_statusBar", showStatusBar);
					break;
				case 1:
					incognitoMode = selected;
					sEdit.putBoolean("incognito", incognitoMode);
					if (!incognitoMode) webAddress.setAdapter(urlAdapter);
					else webAddress.setAdapter(emptyUrlAdapter);
					break;
				case 2:
					updownButton = selected;
					if (updownButton) upAndDown.setVisibility(View.VISIBLE);
					else upAndDown.setVisibility(View.GONE);
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
					webSettings.setLoadWithOverviewMode(overviewPage);
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
	}
	
	public void updownAction() {// identical
		CharSequence operations[] = new CharSequence[] {
				getString(R.string.scroll_top), 
				getString(R.string.page_up), 
				getString(R.string.page_down), 
				getString(R.string.scroll_bottom)
		};
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
	}
	
	public void imgPrevClick() {// identical
		if (serverWebs.get(webIndex).canGoBack())
			serverWebs.get(webIndex).goBack();
		else if (serverWebs.get(webIndex).shouldCloseIfCannotBack)
			closePage(webIndex, false);
	}
	
	public void imgNewClick() {
		if (webControl.getVisibility() == View.GONE) {
			if (urlLine.getLayoutParams().height == 0) setUrlHeight(true);// show url if hided
			adContainer.setVisibility(View.VISIBLE);
		
			webAdapter.notifyDataSetInvalidated();
			webControl.setVisibility(View.VISIBLE);
			webControl.bringToFront();
		} else webControl.setVisibility(View.GONE);	
	}
	
	public void listPageHistory() {// identical
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
	}
	
	public void listBookmark() {// identical
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
	}
	
	public void listHistory() {// identical
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
	}
	
	public void loadPage() {// load home page
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

	public void changePage(int position) {//identical
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
			loadProgress.setVisibility(View.GONE);
		}
	}

	public void closePage(int position, boolean clearData) {//identical
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
				webpages.removeView(tmp);
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
			webControl.setVisibility(View.GONE);
			loadPage();
			webIndex = 0;
			//serverWebs.get(webIndex).clearHistory();// is that necessary to clear history?
		}
		changePage(webIndex);
		
		recordPages();
	}

	public void gotoUrl(String url) {// identical
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
				case 2:
					//url = "http://ad2.easou.com:8080/j10ad/ea2.jsp?channel=11&wver=t&cid=bip1065_10713_001&key=" + url;// easou
					url = "http://www.baidu.com/s?word=" + url; // baidu
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

	public void newIntentAction(Intent intent) {
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
	}
	
	public void hideSearchBox() {// identical
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
		searchBar.setVisibility(View.GONE);
		matchCount = 0;
		// remove the match by an impossible search
		serverWebs.get(webIndex).findAll("jingtao10175jtbuaa@gmail.com");
		searchHint.setText("");
	}

	public void findMatchCount() {// identical
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

	public void searchPrevAction() {// identical
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
	
	public void searchNextAction() {// identical
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
	
	public void setUrlHeight(boolean showUrlNow) {
		LayoutParams lpUrl = urlLine.getLayoutParams();
		if (showUrlNow) 
			lpUrl.height = LayoutParams.WRAP_CONTENT;
		else lpUrl.height = 0;
		urlLine.requestLayout();		
	}
	
	public void setBarHeight(boolean showBarNow) {
		LayoutParams lpBar = webTools.getLayoutParams();
		if (showBarNow) 
			lpBar.height = LayoutParams.WRAP_CONTENT;
		else lpBar.height = 0;
		webTools.requestLayout();		
	}

	public void removeAd() {
		if (adview != null) {
			adContainer.setVisibility(View.GONE);
			adContainer.removeAllViews();
			adview.destroy();
			adview = null;
		}
	}

	public boolean openNewPage(String url, int newIndex, boolean changeToNewPage, boolean closeIfCannotBack) {
		if (url == null) return true;
		else if ("".equals(url)) return true;
		else if (!url.startsWith("http")) {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			util.startActivity(intent, true, mContext);
			return false;
		}
		else return true;// the real logic is put to child class, to solve issue of can't open history/bookmark on second page
	}
	
	public void reloadPage() {// identical
		if (loadProgress.getVisibility() == View.VISIBLE) {
			imgRefresh.setImageResource(R.drawable.refresh);
			// webpage is loading then stop it
			serverWebs.get(webIndex).stopLoading();
			loadProgress.setVisibility(View.GONE);
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
	
	public boolean readPages(String filename) {// identical
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
	
	public void recordPages() {//identical
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
	
	public void addRemoveFavo(String url, String title) {//identical
		for (int i = mBookMark.size() - 1; i >= 0; i--)
			if (mBookMark.get(i).m_url.equals(url)) {
				removeFavo(i);
				return;
			}

		addFavo(url, title);// add favo if not found it
	}
	
	public void removeFavo(final int order) {//identical
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

	public void removeHistory(final int order) {//identical
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

	// favo dialog
	EditText titleText;
	public void addFavo(final String url, final String title) {//identical
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
								String site = WebUtil.getSite(url);
								String title = titleText.getText().toString();
								// add a blank character to occupy the space
								if ("".equals(title)) title += (char) 0xa0;
								TitleUrl titleUrl = new TitleUrl(title, url, site);
								mBookMark.add(titleUrl);
								// sort by name
								Collections.sort(mBookMark, new MyComparator());
								updateBookmark();
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
	
	public void ClearCache() {
		serverWebs.get(webIndex).clearCache(true);
		// mContext.deleteDatabase("webviewCache.db");//this may get
		// disk IO crash
		ClearFolderTask cltask = new ClearFolderTask();
		// clear cache on sdcard and in data folder
		cltask.execute(appstate.downloadPath + "cache/webviewCache/", appstate.dataPath + "cache/webviewCache/");		
	}
	
	public void clearFile(String filename) {
		try {// clear the pages file
			FileOutputStream fo = openFileOutput(filename, 0);
			ObjectOutputStream oos = new ObjectOutputStream(fo);
			oos.writeObject("");
			oos.flush();
			oos.close();
			fo.close();
		} catch (Exception e) {}
	}

	public void createShortcut(String url, String title) {//identical
		Intent i = new Intent(mContext, getClass());
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
	
	public void shareUrl(String title, String url) {//identical
		if (title == null) title = "";
		if (url == null) url = "";
		
		Intent shareIntent = new Intent(Intent.ACTION_VIEW);
		shareIntent.setClassName(getPackageName(), getClass().getName());
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
	
	public class WriteTask extends AsyncTask<String, Integer, String> {//identical

		@Override
		protected String doInBackground(String... params) {
			writeFiles(params);

			return null;
		}
	}
	
	void writeFiles(String... params) {
		for (int i = 0; i < params.length; i++) {
			if ("history".equals(params[i])) {
				writeBookmark("history", mHistory);
				historyChanged = false;
			} 
			else if ("bookmark".equals(params[i])) {
				writeBookmark("bookmark", mBookMark);
				bookmarkChanged = false;
			}
			else if ("downloads".equals(params[i])) {
				writeBookmark("downloads", mDownloads);
				downloadsChanged = false;
			}
		}
	}

	void writeBookmark(String filename, ArrayList<TitleUrl> bookmark) {//identical
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

	public void initSiteArray() {// identical
		getSystemHistory();
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
			mDownloads = readBookmark("downloads");

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
		}

		urlAdapter.sort(new StringComparator());
		if (!incognitoMode) webAddress.setAdapter(urlAdapter);
	}
	
	void getSystemHistory() {// read history and bookmark from native browser //identical
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
					String site = WebUtil.getSite(url);
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

	ArrayList<TitleUrl> readBookmark(String filename) {//identical
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
	
	public void executeWtask(String... paras) {
		WriteTask wtask = new WriteTask();
		try {wtask.execute(paras);}
		catch(RejectedExecutionException e) {
			e.printStackTrace();
			writeFiles(paras);
		}// why no exception before reorg?
	}
	
	public void pauseAction() {
		String[] paras = {"", "", ""};
		if (historyChanged) paras[0] = "history";
		if (bookmarkChanged) paras[1] = "bookmark";
		if (downloadsChanged) paras[2] = "downloads";
		if (historyChanged || bookmarkChanged || downloadsChanged)
			executeWtask(paras);

		sEdit.putBoolean("show_zoom", serverWebs.get(webIndex).zoomVisible);
		sEdit.putBoolean("html5", serverWebs.get(webIndex).html5);
		sEdit.commit();
	}
	
	public SimpleAdapter getMenuAdapter(String[] menuNameArray,
			int[] imageResourceArray) {
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < menuNameArray.length; i++) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", imageResourceArray[i]);
			map.put("itemText", menuNameArray[i]);
			data.add(map);
		}
		SimpleAdapter simperAdapter = new SimpleAdapter(mContext, data,
				R.layout.icon_list, new String[] { "itemImage", "itemText" },
				new int[] { R.id.appicon, R.id.appname });
		return simperAdapter;
	}
}