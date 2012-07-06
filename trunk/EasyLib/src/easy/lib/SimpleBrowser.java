package easy.lib;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ZoomButtonsController;

//for get webpage source on cupcake
class wrapValueCallback {
	ValueCallback<Uri> mInstance;

	wrapValueCallback() {
	}

	static {
		try {
			Class.forName("android.webkit.ValueCallback");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void checkAvailable() {
	}

	void onReceiveValue(Uri value) {
		mInstance.onReceiveValue(value);
	}
}

class wrapWebSettings {
	WebSettings mInstance;

	wrapWebSettings(WebSettings settings) {
		mInstance = settings;
	}

	synchronized void setLoadWithOverviewMode(boolean overview) {// API 7
		try {
			Method method = WebSettings.class.getMethod(
					"setLoadWithOverviewMode", new Class[] { boolean.class });
			method.invoke(mInstance, overview);
		} catch (Exception e) {
		}
	}

	synchronized void setAppCacheEnabled(boolean flag) {// API 7
		try {
			Method method = WebSettings.class.getMethod("setAppCacheEnabled",
					new Class[] { boolean.class });
			method.invoke(mInstance, flag);
		} catch (Exception e) {
		}
	}

	synchronized void setAppCachePath(String databasePath) {// API 7
		try {
			Method method = WebSettings.class.getMethod("setAppCachePath",
					new Class[] { String.class });
			method.invoke(mInstance, databasePath);
		} catch (Exception e) {
		}
	}

	synchronized void setAppCacheMaxSize(long max) {// API 7
		try {
			Method method = WebSettings.class.getMethod("setAppCacheMaxSize",
					new Class[] { long.class });
			method.invoke(mInstance, max);
		} catch (Exception e) {
		}
	}

	synchronized void setDomStorageEnabled(boolean flag) {// API 7
		try {
			Method method = WebSettings.class.getMethod("setDomStorageEnabled",
					new Class[] { boolean.class });
			method.invoke(mInstance, flag);
		} catch (Exception e) {
		}
	}

	synchronized void setDatabaseEnabled(boolean flag) {// API 5
		try {
			Method method = WebSettings.class.getMethod("setDatabaseEnabled",
					new Class[] { boolean.class });
			method.invoke(mInstance, flag);
		} catch (Exception e) {
		}
	}

	synchronized void setDatabasePath(String databasePath) {// API 5
		try {
			Method method = WebSettings.class.getMethod("setDatabasePath",
					new Class[] { String.class });
			method.invoke(mInstance, databasePath);
		} catch (Exception e) {
		}
	}

	synchronized boolean setDisplayZoomControls(boolean enabled) {// API 11
		try {
			Method method = WebSettings.class.getMethod("setDisplayZoomControls",
					new Class[] { boolean.class });
			method.invoke(mInstance, enabled);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * synchronized void setGeolocationEnabled(boolean flag) {//API 5 try {
	 * Method method = WebSettings.class.getMethod("setGeolocationEnabled", new
	 * Class[] {boolean.class}); method.invoke(mInstance, flag); }
	 * catch(Exception e) {} }
	 * 
	 * synchronized void setGeolocationDatabasePath(String databasePath) {//API
	 * 5 try { Method method =
	 * WebSettings.class.getMethod("setGeolocationDatabasePath", new Class[]
	 * {String.class}); method.invoke(mInstance, databasePath); }
	 * catch(Exception e) {} }
	 */
}

public class SimpleBrowser extends Activity {

	final String BLANK_PAGE = "about:blank";
	boolean firstRun = false;
	int countDown = 0;

	ListView webList;
	Context mContext;

	// settings
	boolean fullScreen = false;
	// boolean getPageSource = false;
	boolean snapFullWeb = false;
	boolean html5 = false;
	boolean blockImage = false;
	boolean cachePrefer = false;
	boolean blockPopup = false;
	boolean blockJs = false;
	boolean collapse1 = false, collapse2 = false, collapse3 = false;
	TextSize textSize = TextSize.NORMAL;
	int historyCount = 16;
	long sizeM = 1024 * 1024;
	long html5cacheMaxSize = sizeM * 8;
	int ua = 0;
	boolean showZoom = false;
	int searchEngine = 3;
	private int SETTING_RESULTCODE = 1002;
	boolean enableProxy = false;
	int localPort;
	//boolean hideExit = true;
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
	AlertDialog menuDialog;// menu菜单Dialog
	GridView menuGrid;
	View menuView;
	int historyIndex = -1;
	AlertDialog downloadsDialog = null;

	// browser related
	AutoCompleteTextView webAddress;
	ArrayAdapter<String> urlAdapter;
	ArrayList<String> siteArray;

	ArrayList<MyWebview> serverWebs = new ArrayList<MyWebview>();
	int webIndex;
	MyViewFlipper webpages;
	ImageView imgNext, imgPrev, imgHome, imgRefresh, imgNew;
	WebAdapter webAdapter;
	RelativeLayout webControl, webtools_center, webTools;
	LinearLayout urlLine;
	int dips = 5;
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
	boolean noHistoryOnSdcard = true;

	// ad
	static boolean mAdAvailable;
	static {
		try {
			wrapAdView.checkAvailable();
			Class.forName("com.google.ads.AdView");
			mAdAvailable = true;
		} catch (Throwable t) {
			mAdAvailable = false;
		}
	}
	wrapAdView adview;
	DisplayMetrics dm;
	FrameLayout adContainer;
	AppHandler mAppHandler = new AppHandler();
	int clickCount = 0;
	boolean gotoSettings = false;//will set to true if open settings activity, and set to false again after exit settings
	float width_density;

	// download related
	String downloadPath = "";
	NotificationManager nManager;
	ArrayList<packageIDpair> downloadAppID;
	MyApp appstate;

	class packageIDpair {
		String packageName;
		File downloadedfile;
		int notificationID;

		packageIDpair(String name, int id, File file) {
			packageName = name;
			notificationID = id;
			downloadedfile = file;
		}
	}

	public void freeMemory(MyWebview webview) {// API 7
		try {
			Method method = WebView.class.getMethod("freeMemory");
			method.invoke(webview);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class MyWebview extends WebView {
		public String pageSource = "", mUrl = "";

		wrapWebSettings webSettings;
		
        ZoomButtonsController mZoomButtonsController;
        boolean zoomVisible = false;

		int mProgress = 0;
		boolean isForeground = true;

		public void getPageSource() {// to get page source, part 3
			if ("2.3.3".equals(android.os.Build.VERSION.RELEASE)) 
				// it will cause webkit crash on 2.3.3
				pageSource = getString(R.string.not_avaiable);
			else	// not work for wml. some webkit even not parse wml.
				loadUrl("javascript:window.JSinterface.processHTML(document.getElementsByTagName('html')[0].innerHTML);");
		}

		class MyJavaScriptInterface {
			@SuppressWarnings("unused")
			public void processHTML(String html) {
				if (html
						.contains("<link rel=\"stylesheet\" href=\"file:///android_asset/easybrowser.css\">"))
					// don't show source of home
					pageSource = "<head><title>Easy Browser</title></head><body>welcome!</body>";
				else pageSource = html;// to get page source, part 1
			}

			@SuppressWarnings("unused")
			public void saveCollapseState(int item, boolean state) {
				switch (item) {
				case 1:
					collapse1 = state;
					break;
				case 2:
					collapse2 = state;
					break;
				case 3:
					collapse3 = state;
					break;
				}
			}
		}

		public void setZoomControl(int visibility){
	        Class<?> classType;
	        Field field;
	        try {
	            classType = WebView.class;
	            field = classType.getDeclaredField("mZoomButtonsController");
	            field.setAccessible(true);
	            try {
	            	if (visibility == View.GONE) {
	            		mZoomButtonsController = (ZoomButtonsController) field.get(this);//backup the original zoom controller
	                    ZoomButtonsController myZoomButtonsController = new ZoomButtonsController(this);
			            myZoomButtonsController.getZoomControls().setVisibility(visibility);
		                field.set(this, myZoomButtonsController);
		                zoomVisible = false;
	            	}
	            	else {
	            		field.set(this, mZoomButtonsController);
	            		zoomVisible = true;
	            	}
	            } catch (IllegalArgumentException e) {
	                e.printStackTrace();
	            } catch (IllegalAccessException e) {
	                e.printStackTrace();
	            }
	        } catch (SecurityException e) {
	            e.printStackTrace();
	        } catch (NoSuchFieldException e) {
	            e.printStackTrace();
	        }
	    }
		
		@Override
	    public boolean onTouchEvent(MotionEvent ev) {//onTouchListener may not work. so put relate code here
			// close webcontrol page if it is open.
			webControl.setVisibility(View.INVISIBLE);
			
			if (fullScreen && (urlLine.getLayoutParams().height != 0)) hideBars();
			
			if (!this.isFocused()) {
				this.requestFocusFromTouch();
				this.setFocusable(true);
				webAddress.setFocusableInTouchMode(false);
				webAddress.clearFocus();
			}

			return super.onTouchEvent(ev);
		}

		public MyWebview(Context context) {
			super(context);

			setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
			// no white blank on the right of webview
			try {
				Method method = WebView.class.getMethod(
						"setScrollbarFadingEnabled",
						new Class[] { boolean.class });
				// hide scroll bar when not scroll. from API5, not work on cupcake.
				method.invoke(this, true);
			} catch (Exception e) {
			}

			WebSettings localSettings = getSettings();
			localSettings.setSaveFormData(true);
			localSettings.setTextSize(textSize);
			localSettings.setSupportZoom(true);
			localSettings.setBuiltInZoomControls(true);

			// otherwise can't scroll horizontal in some webpage, such as qiupu.
			localSettings.setUseWideViewPort(true);

			localSettings.setPluginsEnabled(true);
			//localSettings.setPluginState(WebSettings.PluginState.ON);
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
			if (!webSettings.setDisplayZoomControls(false)) //hide zoom button by default on API 11 and above
				setZoomControl(View.GONE);//default not show zoom control in new page
			
			// webSettings.setDefaultZoom(ZoomDensity.MEDIUM);//start from API7

			webSettings.setDomStorageEnabled(true);// API7, key to enable gmail

			// loads the WebView completely zoomed out. fit for hao123, but not fit for homepage. from API7
			webSettings.setLoadWithOverviewMode(overviewPage);

			registerForContextMenu(this);

			// to get page source, part 2
			addJavascriptInterface(new MyJavaScriptInterface(), "JSinterface");

			setDownloadListener(new DownloadListener() {
				@Override
				public void onDownloadStart(String url, String ua,
						String contentDisposition, String mimetype,
						long contentLength) {
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
					startDownload(url, contentDisposition);
				}
			});

			setWebChromeClient(new WebChromeClient() {
				@Override
				public void onProgressChanged(WebView view, int progress) {
					if (progress == 100)
						mProgress = 0;
					else
						mProgress = progress;

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

				/*
				 * @Override public void
				 * onGeolocationPermissionsShowPrompt(String origin,
				 * GeolocationPermissions.Callback callback) {
				 * callback.invoke(origin, true, false); }//I don't know how to
				 * reflect a Interface, so it will crash on cupcake
				 */

				/*
				 * @Override public void onShowCustomView(View view,
				 * CustomViewCallback callback) { super.onShowCustomView(view,
				 * callback); if (view instanceof FrameLayout){ FrameLayout
				 * frame = (FrameLayout) view; if (frame.getFocusedChild()
				 * instanceof VideoView){ VideoView video = (VideoView)
				 * frame.getFocusedChild(); //video.setOnErrorListener(this);
				 * //video.setOnCompletionListener(this); video.start();
				 * //video.stopPlayback();//call these 2 line when stop or
				 * change to other url //callback.onCustomViewHidden(); } } }API
				 * 7
				 */

				@Override
				public boolean onCreateWindow(WebView view, boolean isDialog,
						boolean isUserGesture, android.os.Message resultMsg) {
					if (openNewPage(null)) {// open new page success
						((WebView.WebViewTransport) resultMsg.obj)
								.setWebView(serverWebs.get(webIndex));
						resultMsg.sendToTarget();
						return true;
					} else
						return false;
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
				public void onPageStarted(WebView view, String url,
						Bitmap favicon) {
					super.onPageStarted(view, url, favicon);

					pageSource = "";
					mUrl = url;

					if (isForeground) {
						// close soft keyboard
						imm.hideSoftInputFromWindow(getWindowToken(), 0);
						loadProgress.setVisibility(View.VISIBLE);
						webAddress.setText(url);
						imgRefresh.setImageResource(R.drawable.stop);
					}

					if (adview != null)	adview.loadAd();// should only do this by wifi
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					pageSource = "";//prevent get incomplete page source during page loading
					
					if (isForeground) {
						// hide progressbar anyway
						loadProgress.setVisibility(View.INVISIBLE);
						imgRefresh.setImageResource(R.drawable.refresh);
						webControl.setVisibility(View.INVISIBLE);
					}
					mProgress = 0;
					// update the page title in webList
					webAdapter.notifyDataSetChanged();

					String title = view.getTitle();
					if (title == null) title = url;

					if (!BLANK_PAGE.equals(url)) {
						if (getString(R.string.browser_name).equals(title))
							// if title and url not sync, then sync it
							webAddress.setText(BLANK_PAGE);
						else {// handle the bookmark/history after load new page
							String site = "";
							String[] tmp = url.split("/");
							if (tmp.length > 2) site = tmp[2];
							// if url is http://m.baidu.com,
							// then url.split("/")[2] is m.baidu.com
							else site = tmp[0];
							for (int i = mHistory.size() - 1; i >= 0; i--) {
								if (mHistory.get(i).m_url.equals(url)) {
									mHistory.get(i).m_title = title;
									return;// record one url only once in the
										// history list.
								} else if (mHistory.get(i).m_site.equals(site)) {
									mHistory.remove(i);// only keep the latest
												// history of the same
												// site.
									break;
								}
							}

							if (siteArray.indexOf(site) < 0) {
								urlAdapter.add(site);// update the auto-complete
											// edittext without
											// duplicate
								siteArray.add(site);// the adapter will always
											// return 0 when get count
											// or search, so we use an
											// array to store the site.
							}

							try {// try to open the png, if can't open, then
								// need save
								FileInputStream fis = openFileInput(site
										+ ".png");
								try {
									fis.close();
								} catch (IOException e) {
									;
								}
							} catch (FileNotFoundException e1) {
								try {// save the Favicon
									if (view.getFavicon() != null) {
										FileOutputStream fos = openFileOutput(
												site + ".png", 0);
										view.getFavicon().compress(
												Bitmap.CompressFormat.PNG, 90,
												fos);
										fos.close();
									}
								} catch (Exception e) {
								}
							}

							TitleUrl titleUrl = new TitleUrl(title, url, site);
							mHistory.add(titleUrl);
							historyChanged = true;

							while (mHistory.size() > historyCount) {
								// remove oldest history
								site = mHistory.get(0).m_site;
								mHistory.remove(0);// delete the first history
											// if list larger than
											// historyCount;

								/*
								 * //not delete icon here. it can be clear when
								 * clear all boolean found = false; for (int i =
								 * mHistory.size()-1; i >= 0; i--) { if
								 * (mHistory.get(i).m_site.equals(site)) { found
								 * = true; break; } } if (!found) { for (int i =
								 * mBookMark.size()-1; i >= 0; i--) { if
								 * (mBookMark.get(i).m_site.equals(site)) {
								 * found = true; break; } } if (!found)
								 * deleteFile(mHistory.get(0).m_site +
								 * ".png");//delete the Favicon if not any
								 * reference }
								 */
							}
						}
					}
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (BLANK_PAGE.equals(url)) {// some site such as weibo and
									// mysilkbaby will send
									// BLANK_PAGE when login.
						return true;// we should do nothing but return true,
									// otherwise may not login.
					} else if (!url.startsWith("http") && !url.startsWith("file")) {
						Uri uri = Uri.parse(url);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						return util.startActivity(intent, false, mContext);
					} else
						return false;
				}
			});
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

			if (convertView == null) {
				final LayoutInflater inflater = getLayoutInflater();
				convertView = inflater
						.inflate(R.layout.web_list, parent, false);
			}
			if (position == webIndex)
				convertView.setBackgroundColor(0xAA222222);
			else
				convertView.setBackgroundColor(0xDD111111);

			final ImageView btnIcon = (ImageView) convertView
					.findViewById(R.id.webicon);
			try {
				btnIcon.setImageBitmap(wv.getFavicon());
			} catch (Exception e) {
			}// catch an null pointer exception on 1.6}

			TextView webname = (TextView) convertView
					.findViewById(R.id.webname);
			if ((wv.getTitle() != null) && (!"".equals(wv.getTitle())))
				webname.setText(wv.getTitle());
			else
				webname.setText(wv.getUrl());

			webname.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					webControl.setVisibility(View.INVISIBLE);
					changePage(position);
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
					webAdapter.getCount(), 2, mContext));// show the changed
										// page number
			if ((webIndex > position) || (webIndex == webAdapter.getCount()))
				webIndex -= 1;
		} else {// return to home page if only one page when click close button
			webControl.setVisibility(View.INVISIBLE);
			loadPage(true);
			webIndex = 0;
			serverWebs.get(webIndex).clearHistory();
		}
		changePage(webIndex);
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
				if (clearCache) {
					serverWebs.get(webIndex).clearCache(true);
					// mContext.deleteDatabase("webviewCache.db");//this may get
					// disk IO crash
					ClearFolderTask cltask = new ClearFolderTask();
					// clear cache on sdcard and in data folder
					cltask.execute(downloadPath + "cache/webviewCache/",
							"/data/data/" + mContext.getPackageName()
									+ "/cache/webviewCache/");
				}

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

				boolean clearHistory = sp.getBoolean("clear_history", false);
				boolean clearBookmark = sp.getBoolean("clear_bookmark", false);

				if (clearHistory && clearBookmark && clearCache && clearCookie
						&& clearFormdata && clearPassword) {// clear all
					// mContext.deleteDatabase("webview.db");//this may get disk
					// IO crash
					ClearFolderTask cltask = new ClearFolderTask();
					// clear files folder and app_databases folder
					cltask.execute(getFilesDir().getAbsolutePath(),
							getDir("databases", MODE_PRIVATE).getAbsolutePath());
				}

				if (clearHistory) {
					mHistory.clear();
					writeBookmark("history", mHistory);
					historyChanged = false;
					if (BLANK_PAGE.equals(webAddress.getText().toString()))
						shouldReload = true;
				}

				if (clearBookmark) {
					mBookMark.clear();
					writeBookmark("bookmark", mBookMark);
					bookmarkChanged = false;
					if (BLANK_PAGE.equals(webAddress.getText().toString()))
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
				message = message.trim();
				if (!"".equals(message)) {
					if (message.endsWith(","))
						message = message.substring(0, message.length() - 1);
					Toast.makeText(mContext,
							message + " " + getString(R.string.data_cleared),
							Toast.LENGTH_LONG).show();
				}
			}

			boolean tmpFullScreen = sp.getBoolean("full_screen_display", false);
			// hide url editor and tool buttons
			if (tmpFullScreen != fullScreen) {
				fullScreen = tmpFullScreen;
				if (fullScreen) {
					getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
					hideBars();
				} else {
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
					showBars();
				}
			}

			// default to full screen now
			snapFullWeb = sp.getBoolean("full_web", false);

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
					} catch (Exception e) {
						e.printStackTrace();
					}
			}

			WebSettings localSettings = serverWebs.get(webIndex).getSettings();

			ua = sp.getInt("ua", 0);
			if (ua <= 1)
				localSettings.setUserAgent(ua);
			else
				localSettings.setUserAgentString(selectUA(ua));

			//hideExit = sp.getBoolean("hide_exit", true);

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
			localSettings.setJavaScriptEnabled(!blockJs);

			
			
			wrapWebSettings webSettings = new wrapWebSettings(localSettings);
			overviewPage = sp.getBoolean("overview_page", false);
			webSettings.setLoadWithOverviewMode(overviewPage);

			showZoom = sp.getBoolean("show_zoom", false);
			if (webSettings.setDisplayZoomControls(showZoom)) {//hide zoom button by default on API 11 and above
				localSettings.setBuiltInZoomControls(showZoom);//setDisplayZoomControls(false) not work, so we need disable zoom control
				serverWebs.get(webIndex).zoomVisible = showZoom;
			}
			else {
			    if (showZoom) serverWebs.get(webIndex).setZoomControl(View.VISIBLE);
			    else serverWebs.get(webIndex).setZoomControl(View.GONE);
			}

			html5 = sp.getBoolean("html5", false);
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

				sEdit.putBoolean("html5", false);// close html5 by default
			}

			String tmpEncoding = getEncoding(sp.getInt("encoding", 0));
			if (!tmpEncoding.equals(localSettings.getDefaultTextEncodingName())) {
				localSettings.setDefaultTextEncodingName(tmpEncoding);
				// set default encoding to autoselect
				sEdit.putInt("encoding", 0);
				shouldReload = true;
			}

			if (shouldReload) {
				if (BLANK_PAGE.equals(webAddress.getText().toString()))
					loadPage(true);
				else
					serverWebs.get(webIndex).reload();
			}
			/*
			 * css default to true now. String url =
			 * serverWebs.get(webIndex).getUrl() + ""; boolean oldCss = css; css
			 * = sp.getBoolean("css", false); if ((oldCss != css) &&
			 * url.equals(BLANK_PAGE)) loadPage(true);//reload homepage if css
			 * effect changed
			 * 
			 * disable setting of historyCount historyCount =
			 * sp.getInt("history_count", 1) == 1 ? 10 : 15;
			 */
			sEdit.commit();
		}
	}
	
	public void hideBars() {
		LayoutParams lp = webTools.getLayoutParams();
		lp.height = 0;
		webTools.requestLayout();

		lp = urlLine.getLayoutParams();
		lp.height = 0;
		urlLine.requestLayout();
	}
	
	public void showBars() {
		LayoutParams lp = webTools.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		webTools.requestLayout();

		lp = urlLine.getLayoutParams();
		lp.height = (int) (33 * dm.density);
		urlLine.requestLayout();
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
					startDownload(url, ext);
					break;
				case 4:// copy url
					ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipMan.setText(url);
					break;
				case 5:// share url
					shareUrl(url);
					break;
				case 7:// add bookmark
					if (historyIndex > -1)
						addFavo(url, mHistory.get(historyIndex).m_title);
					else
						addFavo(url, url);
					break;
				case 8:// remove bookmark
					removeFavo(item.getOrder());
					break;
				case 9:// remove history
					removeHistory(item.getOrder());
					break;
				case 10:// open in new tab
					openNewPage(url);
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
			menu.add(0, 0, 0, R.string.save)
					.setOnMenuItemClickListener(handler);

			boolean foundBookmark = false;
			for (int i = mBookMark.size() - 1; i >= 0; i--)
				if ((mBookMark.get(i).m_url.equals(url))
						|| (url.equals(mBookMark.get(i).m_url + "/"))) {
					foundBookmark = true;
					menu.add(0, 8, i, R.string.remove_bookmark)
							.setOnMenuItemClickListener(handler);
					break;
				}
			if (!foundBookmark)
				menu.add(0, 7, 0, R.string.add_bookmark)
						.setOnMenuItemClickListener(handler);

			historyIndex = -1;
			for (int i = mHistory.size() - 1; i >= 0; i--)
				if ((mHistory.get(i).m_url.equals(url))
						|| (url.equals(mHistory.get(i).m_url + "/"))) {
					historyIndex = i;
					menu.add(0, 9, i, R.string.remove_history)
							.setOnMenuItemClickListener(handler);
					break;
				}

			menu.add(0, 10, 1000, R.string.open_new)
					.setOnMenuItemClickListener(handler);
		}
	}

	boolean startDownload(String url, String contentDisposition) {
		int posQ = url.indexOf("src=");
		if (posQ > 0)
			url = url.substring(posQ + 4);// get src part
		url = url.replace("%2D", "-");
		url = url.replace("%5F", "_");
		url = url.replace("%3F", "?");
		url = url.replace("%3D", "=");
		url = url.replace("%2E", ".");
		url = url.replace("%2F", "/");
		url = url.replace("%3A", ":");// replace %3A%2F%2F to :// if any
		if (url.endsWith("/"))
			url = url.substring(0, url.length() - 1); // such as
									// http://m.cnbeta.com/,
									// http://www.google.com.hk/

		String readableUrl = url;
		try {
			readableUrl = URLDecoder.decode(url);
		} catch (Exception e) {
		}// report crash by Elad, so catch it.
		posQ = readableUrl.indexOf("?");
		if (posQ > 0)
			readableUrl = readableUrl.substring(0, posQ);// cut off post paras
									// if any.

		String ss[] = readableUrl.split("/");
		String apkName = ss[ss.length - 1].toLowerCase(); // get download file
									// name
		if (apkName.contains("="))
			apkName = apkName.split("=")[apkName.split("=").length - 1];
		// image from shuimu do not have ext. so we add it manually
		if (!apkName.contains(".") && ".jpg".equals(contentDisposition)) {
			apkName += ".jpg";
			contentDisposition = null;
		}

		if (downloadPath.startsWith(getFilesDir().getPath()))
			Toast.makeText(mContext, R.string.sdcard_needed, Toast.LENGTH_LONG)
					.show();

		Iterator iter = appstate.downloadState.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadTask val = (DownloadTask) entry.getValue();
			if ((val != null) && val.apkName.equals(apkName)) {
				if (val.pauseDownload)
					val.pauseDownload = false;// resume download if it paused
				return true;// the file is downloading, not start a new download
						// task.
			}
		}

		Random random = new Random();
		int id = random.nextInt() + 1000;

		DownloadTask dltask = new DownloadTask();
		dltask.NOTIFICATION_ID = id;
		appstate.downloadState.put(id, dltask);
		dltask.execute(url, apkName, contentDisposition);
		return true;
	}

	class DownloadTask extends AsyncTask<String, Integer, String> {
		private String URL_str; // 网络歌曲的路径
		private File download_file; // 下载的文件
		private long total_read = 0; // 已经下载文件的长度(以字节为单位)
		private int readLength = 0; // 一次性下载的长度(以字节为单位)
		private long apk_length = 0; // 音乐文件的长度(以字节为单位)
		private long skip_length = 0;// if found local file not download
						// finished last time, need continue to
						// download
		String apkName = ""; // 下载的文件名
		int NOTIFICATION_ID;
		private Notification notification;
		private int oldProgress;
		boolean stopDownload = false;// true to stop download
		boolean pauseDownload = false;// true to pause download
		boolean downloadFailed = false;

		@Override
		protected String doInBackground(String... params) {// download here
			URL_str = params[0]; // get download url
			if (URL_str.startsWith("file"))
				return URL_str;// not download local file
			apkName = params[1]; // get download file name
			if (apkName.contains("%"))
				// for some filename contain % will cause error
				apkName = apkName.split("%")[apkName.split("%").length - 1];

			notification = new Notification(
					android.R.drawable.stat_sys_download,
					getString(R.string.start_download),
					System.currentTimeMillis());

			Intent intent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(mContext,
					0, intent, 0);
			notification.setLatestEventInfo(mContext, apkName,
					getString(R.string.start_download), contentIntent);
			nManager.notify(NOTIFICATION_ID, notification);

			// this intent is to pause/stop download
			intent.setAction(getPackageName() + ".downloadControl");
			intent.putExtra("id", NOTIFICATION_ID);
			intent.putExtra("name", apkName);
			intent.putExtra("url", URL_str);
			// request_code will help to diff different thread
			contentIntent = PendingIntent.getActivity(mContext,
					NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(mContext, apkName,
					getString(R.string.downloading), contentIntent);

			FileOutputStream fos = null;
			InputStream is = null;
			URL url = null;
			try {
				url = new URL(URL_str);
				HttpURLConnection httpConnection = null;
				HttpClient httpClient = null;
				String contentDisposition = params[2];
				// Log.d("=============", URL_str + contentDisposition);
				if (URL_str.contains("?") || contentDisposition != null) {
					// need httpget
					httpClient = new DefaultHttpClient();
					HttpGet request = new HttpGet(URL_str);
					String cookies = CookieManager.getInstance().getCookie(
							URL_str);
					request.addHeader("Cookie", cookies);
					// Log.d("=============", cookies);
					HttpResponse response = httpClient.execute(request);
					is = response.getEntity().getContent();
					apk_length = response.getEntity().getContentLength();
					// Log.d("=============", apk_length+"");
					Header[] headers = response.getAllHeaders();
					for (int i = 0; i < headers.length; i++) {
						Header h = headers[i];
						// Log.d("===========", "Header names: "+h.getName() +
						// "  Value: "+h.getValue());
						if ("Content-Disposition".equals(h.getName())
								&& h.getValue().toLowerCase()
										.contains("filename")) {
							String value = URLDecoder.decode(h.getValue());
							apkName = value.split("=")[1].trim();
							if (apkName.startsWith("\""))
								apkName = apkName.substring(1);
							if (apkName.endsWith("\""))
								apkName = apkName.substring(0,
										apkName.length() - 1);
							if (apkName.contains("'"))
								apkName = apkName.split("'")[apkName.split("'").length - 1];// utf-8''CHUN%E5%85%89%E8%BC%9D%E8%8D%92%E9%87%8E.rar
							if (apkName.contains("?"))
								apkName = apkName.replace("?", "1");// ???????.doc
							notification.setLatestEventInfo(mContext, apkName,
									getString(R.string.downloading),
									contentIntent);
						}
					}
				} else {
					httpConnection = (HttpURLConnection) url.openConnection();
					apk_length = httpConnection.getContentLength(); // file size
											// need to
											// download
					is = httpConnection.getInputStream();
				}

				download_file = new File(downloadPath + apkName);
				boolean found = false;
				MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
				String mimeType = mimeTypeMap
						.getMimeTypeFromExtension(MimeTypeMap
								.getFileExtensionFromUrl(apkName))
						+ "";
				if (!"".equals(mimeType)) {
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(download_file), mimeType);
					List<ResolveInfo> list = getPackageManager()
							.queryIntentActivities(intent, 0);
					if ((list != null) && !list.isEmpty())
						found = true;
				}
				if (!found) {
					intent.setAction("com.estrongs.action.PICK_FILE");
					intent.setData(Uri.fromFile(new File(downloadPath)));
				}

				if (download_file.length() == apk_length) {
					// found local file with same name and length,
					// no need to download, just send intent to view it
					String[] tmp = apkName.split("\\.");
					util.startActivity(intent, false, mContext);
					appstate.downloadState.remove(NOTIFICATION_ID);
					nManager.cancel(NOTIFICATION_ID);
					return downloadPath + apkName;
				} else if (download_file.length() < apk_length) {
					// local file size < need to download,
					// need continue to download
					fos = new FileOutputStream(download_file, true);
					skip_length = download_file.length();
				} else
					// need overwrite
					fos = new FileOutputStream(download_file, false);

				notification.contentView = new RemoteViews(getApplication()
						.getPackageName(), R.layout.notification_dialog);
				notification.contentView.setProgressBar(R.id.progress_bar, 100,
						0, false);
				notification.contentView.setTextViewText(R.id.progress, "0%");
				notification.contentView.setTextViewText(R.id.title, apkName);
				nManager.notify(NOTIFICATION_ID, notification);

				total_read = 0; // 初始化“已下载部分”的长度，此处应为0

				byte buf[] = new byte[10240]; // download buffer. is that ok for
								// 10240?
				readLength = 0; // 一次性下载的长度

				oldProgress = 0;
				// 如果读取网络文件的数据流成功，且用户没有选择停止下载，则开始下载文件
				while (readLength != -1 && !stopDownload) {
					if (pauseDownload) {
						continue;
					}

					if ((readLength = is.read(buf)) > 0) {
						if (skip_length == 0)
							fos.write(buf, 0, readLength);
						else if (skip_length < readLength) {
							fos.write(buf, (int) skip_length,
									(int) (readLength - skip_length));
							skip_length = 0;
						} else
							skip_length -= readLength;// just read and skip, not
											// write if need skip

						total_read += readLength; // increase the download size
					}

					int progress = (int) ((total_read + 0.0) / apk_length * 100);
					if (oldProgress != progress) {// the device will get no
									// response if update too
									// often
						oldProgress = progress;
						notification.contentView.setProgressBar(
								R.id.progress_bar, 100, progress, false);// update
													// download
													// progress
						notification.contentView.setTextViewText(R.id.progress,
								progress + "%");
						nManager.notify(NOTIFICATION_ID, notification);
					}
				}
				// stop download by user. clear notification here for the close() and shutdown() may be very slow
				if (stopDownload)
					nManager.cancel(NOTIFICATION_ID);

				try {
					fos.close();
				} catch (IOException e1) {
				}

				try {
					is.close();
				} catch (IOException e1) {
				}

				if (httpConnection != null)
					httpConnection.disconnect();
				else
					httpClient.getConnectionManager().shutdown();

				if (!stopDownload) {// download success. change notification,
									// start package manager to install package
					notification.icon = android.R.drawable.stat_sys_download_done;

					DecimalFormat df = (DecimalFormat) NumberFormat
							.getInstance();
					df.setMaximumFractionDigits(2);
					String ssize = total_read + "B ";
					if (total_read > sizeM)
						ssize = df.format(total_read * 1.0 / sizeM) + "M ";
					else if (total_read > 1024)
						ssize = df.format(total_read * 1.0 / 1024) + "K ";

					contentIntent = PendingIntent.getActivity(mContext, 0,
							intent, 0);
					notification.contentView.setOnClickPendingIntent(
							R.id.notification_dialog, contentIntent);
					notification.setLatestEventInfo(mContext, apkName, ssize
							+ getString(R.string.download_finish),
							contentIntent);// click listener for download
											// progress bar
					nManager.notify(NOTIFICATION_ID, notification);

					// change file property, for on some device the property is wrong
					Process p = Runtime.getRuntime().exec(
							"chmod 644 " + download_file.getPath());
					p.waitFor();

					if (mimeType.startsWith("image")) {
						Intent intentAddPic = new Intent(
								"simpleHome.action.PIC_ADDED");
						intentAddPic.putExtra("picFile", apkName);
						// add to picture list and enable change background by shake
						sendBroadcast(intentAddPic);
					} else if (mimeType.startsWith("application"))
						try {
							PackageInfo pi = getPackageManager()
									.getPackageArchiveInfo(
											downloadPath + apkName, 0);
							downloadAppID.add(new packageIDpair(pi.packageName,
									NOTIFICATION_ID, download_file));
						} catch (Exception e) {
						}

					// call system package manager to install app.
					// it will not return result code,
					// so not use startActivityForResult();
					util.startActivity(intent, false, mContext);
				}

			} catch (Exception e) {
				e.printStackTrace();
				downloadFailed = true;
				notification.icon = android.R.drawable.stat_notify_error;
				intent.putExtra("errorMsg", e.toString());
				// request_code will help to diff different thread
				contentIntent = PendingIntent.getActivity(mContext,
						NOTIFICATION_ID, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				notification.setLatestEventInfo(mContext, apkName,
						e.toString(), contentIntent);
				nManager.notify(NOTIFICATION_ID, notification);

				// below line will cause error simetime, reported by emilio. so
				// commented it. may not a big issue to keep zero file.
				// if (download_file.length() == 0)
				// download_file.delete();//delete empty file
			}

			// remove download id whether download success nor fail, otherwise can't download again on fail
			appstate.downloadState.remove(NOTIFICATION_ID);

			return null;
		}

	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (fullScreen && (urlLine.getLayoutParams().height == 0)) showBars();
		else menuDialog.show();

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

	private void shareUrl(String text) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
		intent.putExtra(Intent.EXTRA_TEXT, text);
		util.startActivity(
				Intent.createChooser(intent, getString(R.string.sharemode)),
				true, mContext);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// mContext = getApplicationContext();//this will cause force close when select locale in google translate
		mContext = this;

		// init settings
		sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		sEdit = sp.edit();

		// paid = sp.getBoolean("paid", false);
		// debug = sp.getBoolean("debug", false);
		// css = sp.getBoolean("css", false);
		// html5 = sp.getBoolean("html5", false);
		blockImage = sp.getBoolean("block_image", false);
		cachePrefer = sp.getBoolean("cache_prefer", false);
		blockPopup = sp.getBoolean("block_popup", false);
		blockJs = sp.getBoolean("block_js", false);
		//hideExit = sp.getBoolean("hide_exit", true);
		overviewPage = sp.getBoolean("overview_page", false);
		collapse1 = sp.getBoolean("collapse1", false);
		collapse2 = sp.getBoolean("collapse2", false);
		collapse3 = sp.getBoolean("collapse3", false);
		ua = sp.getInt("ua", 0);
		showZoom = sp.getBoolean("show_zoom", false);
		mLocale = getBaseContext().getResources().getConfiguration().locale;
		if ("ru_RU".equals(mLocale) || "ru".equals(mLocale))
			searchEngine = sp.getInt("search_engine", 3);
		else
			searchEngine = sp.getInt("search_engine", 4);
		fullScreen = sp.getBoolean("full_screen_display", false);
		// default to full screen
		snapFullWeb = sp.getBoolean("full_web", false);
		readTextSize(sp);// init the text size
		enableProxy = sp.getBoolean("enable_proxy", false);
		if (enableProxy) {
			localPort = sp.getInt("local_port", 1984);
			ProxySettings.setProxy(mContext, "127.0.0.1", localPort);
		}

		nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		downloadAppID = new ArrayList();
		appstate = ((MyApp) getApplicationContext());

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		// hide titlebar of application, must be before setting the layout
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		setContentView(R.layout.browser);

		snapView = (ImageView) getLayoutInflater().inflate(
				R.layout.snap_browser, null);
		snapDialog = new AlertDialog.Builder(this)
				.setView(snapView)
				.setTitle(R.string.browser_name)
				.setPositiveButton(R.string.share,
						new DialogInterface.OnClickListener() {// share
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									String snap = downloadPath
											+ "snap/snap.png";
									FileOutputStream fos = new FileOutputStream(
											snap);

									bmp.compress(Bitmap.CompressFormat.PNG, 90,
											fos);
									fos.close();

									Intent intent = new Intent(
											Intent.ACTION_SEND);
									intent.setType("image/*");
									intent.putExtra(Intent.EXTRA_SUBJECT,
											R.string.share);
									intent.putExtra(Intent.EXTRA_STREAM,
											Uri.fromFile(new File(snap)));
									util.startActivity(Intent.createChooser(
											intent,
											getString(R.string.sharemode)),
											true, mContext);
								} catch (Exception e) {
									Toast.makeText(getBaseContext(),
											e.toString(), Toast.LENGTH_LONG)
											.show();
								}
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {// cancel
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();

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
		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
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

		m_sourceDialog = new AlertDialog.Builder(this)
				.setTitle(R.string.browser_name)
				.setPositiveButton(R.string.share,
						new DialogInterface.OnClickListener() {// share
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(Intent.ACTION_SENDTO);
								intent.setData(Uri
										.fromParts("mailto", "", null));
								intent.putExtra(Intent.EXTRA_TEXT,
										serverWebs.get(webIndex).pageSource);
								intent.putExtra(Intent.EXTRA_SUBJECT,
										serverWebs.get(webIndex).getTitle());
								if (!util.startActivity(intent, false,
										getBaseContext()))
									shareUrl(serverWebs.get(webIndex).pageSource);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {// cancel
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();

		final Context localContext = this;
		menuGrid = (GridView) menuView.findViewById(R.id.gridview);
		menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
		menuGrid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				switch (arg2) {
				case 2:// copy
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
				case 5:// share url
					shareUrl(serverWebs.get(webIndex).getTitle() + " "
							+ serverWebs.get(webIndex).getUrl());
					break;
				case 6:// search
						// serverWebs.get(webIndex).showFindDialog("e", false);
					searchBar.bringToFront();
					searchBar.setVisibility(View.VISIBLE);
					etSearch.requestFocus();
					toSearch = "";
					imm.toggleSoftInput(0, 0);
					break;
				case 3:// exit
						finish();
					break;
				case 0:// view page source
					try {
						if ("".equals(serverWebs.get(webIndex).pageSource)) {
							serverWebs.get(webIndex).pageSource = "Loading... Please try again later.";
							serverWebs.get(webIndex).getPageSource();
						}

						m_sourceDialog.setTitle(serverWebs.get(webIndex)
								.getTitle());
						if (BLANK_PAGE
								.equals(serverWebs.get(webIndex).getUrl()))
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
													+ downloadPath
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
						snapView.setImageBitmap(bmp);
						snapDialog
								.setTitle(serverWebs.get(webIndex).getTitle());
						if (BLANK_PAGE
								.equals(serverWebs.get(webIndex).getUrl()))
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
				case 7:// about
					gotoSettings = true;
					intent = new Intent("easy.lib.about");
					intent.setClassName(getPackageName(),
							"easy.lib.AboutBrowser");
					startActivityForResult(intent, SETTING_RESULTCODE);
					break;
				}
				menuDialog.dismiss();
			}
		});

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

		loadProgress = (ProgressBar) findViewById(R.id.loadprogress);

		imgAddFavo = (ImageView) findViewById(R.id.addfavorite);
		imgAddFavo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String url = serverWebs.get(webIndex).getUrl();
				if (BLANK_PAGE.equals(url))
					return;// not add blank page

				boolean foundBookmark = false;
				for (int i = mBookMark.size() - 1; i >= 0; i--)
					if (mBookMark.get(i).m_url.equals(url)) {
						foundBookmark = true;
						removeFavo(i);
						break;
					}
				if (!foundBookmark)
					addFavo(url, serverWebs.get(webIndex).getTitle());
			}
		});

		imgGo = (ImageView) findViewById(R.id.go);
		imgGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				gotoUrl(webAddress.getText().toString());
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

		downloadPath = util.preparePath(mContext);
		if (downloadPath == null) downloadPath = "/data/data/" + getPackageName() + "/";//fix null pointer close for 4 users
		mHistory = readBookmark("history");
		mBookMark = readBookmark("bookmark");
		// Collections.sort(mBookMark, new myComparator());//sort the bookmark

		siteArray = new ArrayList<String>();
		urlAdapter = new ArrayAdapter<String>(mContext,
				android.R.layout.simple_dropdown_item_1line,
				new ArrayList<String>());
		String site;
		for (int i = 0; i < mHistory.size(); i++) {
			site = mHistory.get(i).m_site;
			if (siteArray.indexOf(site) < 0) {
				siteArray.add(site);
				urlAdapter.add(site);
			}
		}
		for (int i = 0; i < mBookMark.size(); i++) {
			site = mBookMark.get(i).m_site;
			if (siteArray.indexOf(mBookMark.get(i).m_site) < 0) {
				siteArray.add(site);
				urlAdapter.add(site);
			}
		}

		getHistoryList();// read history from native browser
		for (int i = 0; i < mSystemHistory.size(); i++) {
			site = mSystemHistory.get(i).m_site;
			if (siteArray.indexOf(site) < 0) {
				siteArray.add(site);
				urlAdapter.add(site);
			}
		}

		try {
			FileInputStream fi = null;
			try {// try to open history on sdcard at first
				File file = new File(downloadPath + "bookmark/history");
				fi = new FileInputStream(file);
				noHistoryOnSdcard = false;
			} catch (FileNotFoundException e) {// read from /data/data if fail
				fi = openFileInput("history");
			}
			try {
				fi.close();
			} catch (Exception e) {
			}
		} catch (FileNotFoundException e1) {
			firstRun = true;
		}
		if (firstRun) {// copy from system bookmark if first run
			for (int i = 0; i < mSystemHistory.size(); i++) {
				if (i > historyCount)
					break;
				mHistory.add(mSystemHistory.get(i));
			}

			for (int i = 0; i < mSystemBookMark.size(); i++)
				mBookMark.add(mSystemBookMark.get(i));
			Collections.sort(mBookMark, new myComparator());

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

		/*
		 * try {//so many error report on 2.3.6 related to clearcache
		 * Log.d("================", "try clear the cache");
		 * serverWebs.get(webIndex).clearCache(true);
		 * mContext.deleteDatabase("webviewCache.db"); } catch (Exception e) {
		 * e.printStackTrace(); new AlertDialog.Builder(this).
		 * setTitle(R.string.browser_name).
		 * setMessage("It seems the database of webview is corrupted.").
		 * setPositiveButton(R.string.share, new
		 * DialogInterface.OnClickListener() {//share
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * Intent intent = new Intent(Intent.ACTION_VIEW, null);
		 * intent.addCategory(Intent.CATEGORY_DEFAULT); List<ResolveInfo>
		 * viewApps = getPackageManager().queryIntentActivities(intent, 0);
		 * ResolveInfo appDetail = null; for (int i = 0; i < viewApps.size();
		 * i++) { if
		 * (viewApps.get(i).activityInfo.name.contains("InstalledAppDetails")) {
		 * appDetail = viewApps.get(i);//get the activity for app detail setting
		 * break; } } if (appDetail != null) { intent = new
		 * Intent(Intent.ACTION_VIEW);
		 * intent.setClassName(appDetail.activityInfo.packageName,
		 * appDetail.activityInfo.name); intent.putExtra("pkg",
		 * mContext.getPackageName());
		 * intent.putExtra("com.android.settings.ApplicationPkgName",
		 * mContext.getPackageName()); } else {//2.6 tahiti change the action.
		 * intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
		 * Uri.fromParts("package", mContext.getPackageName(), null)); }
		 * util.startActivity(intent, true, getBaseContext()); }
		 * }).setNegativeButton(R.string.cancel, new
		 * DialogInterface.OnClickListener() {//cancel
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) { }
		 * }).show(); }
		 */

		webTools = (RelativeLayout) findViewById(R.id.webtools);
		urlLine = (LinearLayout) findViewById(R.id.urlline);
		if (fullScreen) {// hide url bar and tools bar
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			LayoutParams lp = webTools.getLayoutParams();
			lp.height = 0;

			lp = urlLine.getLayoutParams();
			lp.height = 0;
		}

		webtools_center = (RelativeLayout) findViewById(R.id.webtools_center);

		imgNext = (ImageView) findViewById(R.id.next);
		imgNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (serverWebs.get(webIndex).canGoForward()) {
					WebBackForwardList wbfl = serverWebs.get(webIndex)
							.copyBackForwardList();
					if (BLANK_PAGE.equals(wbfl.getItemAtIndex(
							wbfl.getCurrentIndex() + 1).getUrl()))
						loadPage(true);// goBack will show blank page at this
										// time, so load the home page.
					else
						serverWebs.get(webIndex).goForward();
				}
			}
		});
		imgPrev = (ImageView) findViewById(R.id.prev);
		imgPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (serverWebs.get(webIndex).canGoBack()) {
					WebBackForwardList wbfl = serverWebs.get(webIndex)
							.copyBackForwardList();
					if (BLANK_PAGE.equals(wbfl.getItemAtIndex(
							wbfl.getCurrentIndex() - 1).getUrl()))
						loadPage(true);// goBack will show blank page at this
										// time, so load the home page.
					else
						serverWebs.get(webIndex).goBack();
				}
			}
		});
		imgRefresh = (ImageView) findViewById(R.id.refresh);
		imgRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (loadProgress.getVisibility() == View.VISIBLE) {
					// webpage is loading then stop it
					serverWebs.get(webIndex).stopLoading();
					loadProgress.setVisibility(View.INVISIBLE);
				} else {// reload the webpage
					if (!BLANK_PAGE.equals(webAddress.getText().toString()))
						serverWebs.get(webIndex).reload();
					else
						loadPage(true);
				}
			}
		});
		imgHome = (ImageView) findViewById(R.id.home);
		imgHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				loadPage(true);
			}
		});
		imgNew = (ImageView) findViewById(R.id.newpage);
		imgNew.setImageBitmap(util.generatorCountIcon(
				util.getResIcon(getResources(), R.drawable.newpage), 1, 2,
				mContext));
		imgNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (webControl.getVisibility() == View.INVISIBLE) {
					webAdapter.notifyDataSetInvalidated();
					webControl.setVisibility(View.VISIBLE);
					webControl.bringToFront();
				} else {
					webControl.setVisibility(View.INVISIBLE);
				}
			}
		});

		// web control
		webControl = (RelativeLayout) findViewById(R.id.webcontrol);

		btnNewpage = (Button) findViewById(R.id.opennewpage);
		btnNewpage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {// add a new page
				openNewPage("");
			}
		});
		// web list
		webAdapter = new WebAdapter(mContext, serverWebs);
		webList = (ListView) findViewById(R.id.weblist);
		webList.inflate(mContext, R.layout.web_list, null);
		webList.setFadingEdgeLength(0);// no shadow when scroll
		webList.setScrollingCacheEnabled(false);
		webList.setAdapter(webAdapter);

		adContainer = (FrameLayout) findViewById(R.id.adContainer);
		setLayout();

		try {// there are a null pointer error reported for the if line below,
				// hard to reproduce, maybe someone use instrument tool to test
				// it. so just catch it.
			if (Intent.ACTION_MAIN.equals(getIntent().getAction()))
				loadPage(false);
			else
				serverWebs.get(webIndex).loadUrl(getIntent().getDataString());
		} catch (Exception e) {
		}

		// setPrefer();//can't get complete component set. can't work on 2.2
		// even you can get the full set. so don't set it.

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

	@Override
	protected void onDestroy() {
		unregisterReceiver(screenLockReceiver);
		unregisterReceiver(downloadReceiver);
		unregisterReceiver(packageReceiver);

		if (adview != null)
			adview.destroy();

		super.onDestroy();
	}

	BroadcastReceiver screenLockReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			clickCount += 1;
		}
	};

	BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			startDownload(intent.getStringExtra("url"), null);
		}
	};

	BroadcastReceiver packageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				// it always in the format of package:x.y.z
				String packageName = intent.getDataString().split(":")[1];
				for (int i = 0; i < downloadAppID.size(); i++) {
					// cancel download notification if install succeed
					if (downloadAppID.get(i).packageName.equals(packageName)) { 
						// only remove the notification internal id, not delete file.
						// otherwise when user click the ad again, it will download again.
						// traffic is important than storage.
						// user can download it manually when click downloads
						nManager.cancel(downloadAppID.get(i).notificationID);
						downloadAppID.remove(i);
						break;
					}
				}

			}
		}
	};

	void gotoUrl(String url) {
		if (!BLANK_PAGE.equals(url)) {
			if (!url.contains(".")) {
				switch (searchEngine) {
				case 1:// bing
					url = "http://www.bing.com/search?q=" + url;
					break;
				case 2:// baidu
					url = "http://www.baidu.com/s?wd=" + url;
					break;
				case 4:// yandex
					url = "http://yandex.ru/yandsearch?clid=1911433&text="
							+ url;
					break;
				case 3:// google
				default:
					url = "http://www.google.com/search?q=" + url;
					break;
				}
			} else if (!url.contains("://"))
				url = "http://" + url;
			serverWebs.get(webIndex).loadUrl(url);
		}
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
		webAddress.setText(serverWebs.get(webIndex).mUrl);// refresh the display
															// url

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
				String url = serverWebs.get(i).getUrl();
				if ((uri + "/").equals(url) || uri.equals(url)) {
					changePage(i); // show correct page
					found = true;
					break;
				} else if (BLANK_PAGE.equals(url))
					blankIndex = i;
			}

			if (!found) {
				if (blankIndex < 0)
					openNewPage(uri);
				else {
					serverWebs.get(blankIndex).loadUrl(uri);
					changePage(blankIndex);
				}
			}
		}

		super.onNewIntent(intent);
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
								// deleteFile(mBookMark.get(ii).m_title +
								// ".snap.png");//delete snap too
								mBookMark.remove(order);
								bookmarkChanged = true;
								loadPage(false);
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
								// deleteFile(mBookMark.get(ii).m_title +
								// ".snap.png");//delete snap too
								mHistory.remove(order);
								historyChanged = true;
								loadPage(false);
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
								Collections.sort(mBookMark, new myComparator());
								loadPage(false);

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

	private boolean openNewPage(String url) {
		boolean result = true;

		if (webAdapter.getCount() == 9) {// max pages is 9
			Toast.makeText(mContext, R.string.nomore_pages, Toast.LENGTH_LONG)
					.show();
			result = false;
		} else {
			webAdapter.add(new MyWebview(mContext));
			webAdapter.notifyDataSetInvalidated();
			webpages.addView(webAdapter.getItem(webAdapter.getCount() - 1));
			imgNew.setImageBitmap(util.generatorCountIcon(
					util.getResIcon(getResources(), R.drawable.newpage),
					webAdapter.getCount(), 2, mContext));
			changePage(webAdapter.getCount() - 1);
		}

		if (url != null) {
			if ("".equals(url))
				loadPage(true);
			// else if (url.endsWith(".pdf"))//can't open local pdf by google
			// doc
			// serverWebs.get(webIndex).loadUrl("http://docs.google.com/gview?embedded=true&url="
			// + url);
			else {
				try {
					url = URLDecoder.decode(url);
				} catch (Exception e) {
				}
				serverWebs.get(webIndex).loadUrl(url);
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() == 0) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				// press Back key in webview will go backword.
				if (webControl.getVisibility() == View.VISIBLE)
					imgNew.performClick();// hide web control
				else if (searchBar.getVisibility() == View.VISIBLE)
					hideSearchBox();
				else if (BLANK_PAGE.equals(webAddress.getText().toString())) {
					// hide browser when click back key on homepage.
					// this is a singleTask activity, so if return
					// super.onKeyDown(keyCode, event), app will exit.
					// when use click browser icon again, it will call onCreate,
					// user's page will not reopen.
					// singleInstance will work here, but it will cause
					// downloadControl not work? or select file not work?
					moveTaskToBack(true);
				} else if (serverWebs.get(webIndex).canGoBack())
					imgPrev.performClick();
				else closePage(webIndex, false);//close current page if can't go back

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
		protected String doInBackground(String... params) {
			clearFolder(new File(params[0]));
			if (!params[0].equals(params[1]))
				clearFolder(new File(params[1]));

			return null;
		}
	}

	static int clearFolder(final File dir) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					// first delete subdirectories recursively
					if (child.isDirectory())
						deletedFiles += clearFolder(child);
					// then delete the files and subdirectories in this dir
					if (child.delete())
						deletedFiles++;
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
		if (historyChanged || noHistoryOnSdcard) {
			WriteTask wtask = new WriteTask();
			wtask.execute("history");
		}
		if (bookmarkChanged || noHistoryOnSdcard) {
			WriteTask wtask = new WriteTask();
			wtask.execute("bookmark");
		}

		sEdit.putBoolean("collapse1", collapse1);
		sEdit.putBoolean("collapse2", collapse2);
		sEdit.putBoolean("collapse3", collapse3);
		sEdit.putBoolean("show_zoom", serverWebs.get(webIndex).zoomVisible);
		sEdit.commit();

		if (!gotoSettings && (clickCount > 0)) clickCount -= 1;
		
		try {
			Class c = Class.forName("com.baidu.mobstat.StatService");
			Method method = c.getMethod(
					"onPause", new Class[] { Context.class });
			method.invoke(this, this);//StatService.onPause(this);//for baidu tongji
		} catch (Exception e) {}

		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if (gotoSettings) gotoSettings = false;
		else if (clickCount == 0) createAd();
		
		try {
			Class c = Class.forName("com.baidu.mobstat.StatService");
			Method method = c.getMethod(
					"onResume", new Class[] { Context.class });
			method.invoke(this, this);//StatService.onResume(this);//for baidu tongji
		} catch (Exception e) {}
	}
	
	@Override
	public void onStop() {
		super.onStop();
		
		if (!gotoSettings) removeAd();//ad will occupy cpu and data quota even in background
	}
	
	@Override
	public File getCacheDir() {
		// NOTE: this method is used in Android 2.1
		return getApplicationContext().getCacheDir();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig); // not restart activity each
							// time screen orientation
							// changes
		setLayout();
	}

	void setLayout() {
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		int width = dm.widthPixels;

		LayoutParams lp = webtools_center.getLayoutParams();
		if (width >= 320)
			lp.width = width / 2 + 30;
		else
			lp.width = width / 2 + 20;

		width_density = width / dm.density;

		if (clickCount == 0) createAd();
	}

	void removeAd() {
		if (adview != null) {
			adContainer.removeViewAt(0);
			adview.destroy();
			adview = null;
		}
	}
	
	void createAd() {
		if ((cm == null) || (cm.getActiveNetworkInfo() == null) || !cm.getActiveNetworkInfo().isConnected()) return;
		
		//AdView adView = new AdView(this, "6148");//adview of tapit
		//adView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.FILL_PARENT));
		//adContainer.addView(adView);

		if (mAdAvailable) {
			removeAd();
			
			if (width_density < 320) ;//do nothing for it is too narrow. 
			// but it will cause force close if not create adview?
			if (width_density < 468)// AdSize.BANNER require 320*50
				adview = new wrapAdView(this, 0, "a14f3f6bc126143", mAppHandler);
			else if (width_density < 728)
				adview = new wrapAdView(this, 1, "a14f3f6bc126143", mAppHandler);
				// AdSize.IAB_BANNER require 468*60 but return 702*90 on BKB(1024*600) and S1.
				// return width = request width * density.
			else	// AdSize.IAB_LEADERBOARD require 728*90, return 1092*135 on BKB
				adview = new wrapAdView(this, 2, "a14f3f6bc126143", mAppHandler);

			if ((adview != null) && (adview.getInstance() != null)) {
				adContainer.addView(adview.getInstance());
				adview.loadAd();
			}
		}
	}

	class AppHandler extends Handler {

		public void handleMessage(Message msg) {
			if (msg.what > 0) {
				clickCount += 2;// hide ad for three times
				//Log.d("=============Ads removead", clickCount + "");
				removeAd();
				/*LayoutParams lp = adContainer.getLayoutParams();
				lp.height = 0;// it will dismiss the banner for no enough space
								// for new ad.
				adContainer.requestLayout();*/
			}
		}
	}

	void loadPage(boolean notJudge) {
		if ((notJudge) || (serverWebs.get(webIndex).getUrl() == null)
				|| (serverWebs.get(webIndex).getUrl().equals(BLANK_PAGE)))
			serverWebs.get(webIndex).loadDataWithBaseURL(BLANK_PAGE,
					homePage(), "text/html", "utf-8", BLANK_PAGE);
	}

	String homePage() {// three part, 1 is recommend, 2 is bookmark displayed by
						// scaled image, 3 is history displayed by link
		String fileDir = "<li style='background-image:url(file://" + getFilesDir().getAbsolutePath() + "/";
		String ret = "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">";
		StringBuilder sb = new StringBuilder(ret);
		sb.append("<meta name=\"viewport\" content=\"width=device-width\">");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<link rel=\"shortcut icon\" href=\"file:///android_asset/favicon.ico\">");
		sb.append("<title>");
		sb.append(getString(R.string.browser_name));
		sb.append("</title>");

		sb.append("<link rel=\"stylesheet\" href=\"file:///android_asset/easybrowser.css\">");
		sb.append("<script type=\"text/javascript\" src=\"file:///android_asset/easybrowser.js\"></script>");

		sb.append("</head>");
		sb.append("<body>");

		String tmp = getString(R.string.top);
		if (countDown > 0)
			tmp += getString(R.string.url_can_longclick);
		//if (countDown > 1) tmp += "\t" + countDown;
		if (collapse1) {
			sb.append("<h4 id=\"title1\" onClick=\"collapse(1)\" >+\t");
			sb.append(tmp);
			sb.append("</h4>");
			sb.append("<dl id=\"content1\" type=\"disc\" style=\"display: none;\" >");
		} else {
			sb.append("<h4 id=\"title1\" onClick=\"collapse(1)\" >-\t");
			sb.append(tmp);
			sb.append("</h4>");
			sb.append("<dl id=\"content1\" type=\"disc\">");
		}
		if (Locale.CHINA.equals(mLocale) || Locale.TAIWAN.equals(mLocale)) {
			sb.append(fileDir);
			sb.append("weibo.com.png)'><a href=\"http://weibo.com\">新浪微博</a></li>");
			// sb.append(fileDir);
			// sb.append("3g.gfan.com.png)'><a href=\"http://3g.gfan.com\">机锋市场</a></li>");
			// sb.append(fileDir);
			// sb.append("www.appchina.com.png)'><a href=\"http://www.appchina.com\">应用汇</a></li>");
			sb.append(fileDir);
			sb.append("m.hao123.com.png)'><a href=\"http://m.hao123.com/?type=android&tn=easy.browser\">好123</a></li>");
			// sb.append(fileDir);
			// sb.append("www.taobao.com.png)'><a href=\"http://www.taobao.com\">淘宝</a></li>");
			sb.append(fileDir);
			sb.append("www.baidu.com.png)'><a href=\"http://www.baidu.com\">百度</a></li>");
			sb.append("<li><a href=\"http://www.9yu.co/index.html?c=2\">九鱼</a></li>");// no favicon
			sb.append(fileDir);
			sb.append("bpc.borqs.com.png)'><a href=\"http://bpc.borqs.com\">梧桐</a></li>");
		} else {
			// sb.append(fileDir);
			// sb.append("www.amazon.com.png)'><a href=\"http://www.amazon.com\">Amazon</a></li>");
			// sb.append(fileDir);
			// sb.append("www.bing.com.png>)'<a href=\"http://www.bing.com\">Bing</a></li>");
			sb.append("<li><a href=\"http://www.1mobile.com/app/market/?cid=9\">1mobile</a></li>");// no
																									// favicon
			sb.append(fileDir);
			sb.append("m.facebook.com.png)'><a href=\"http://www.facebook.com\">Facebook</a></li>");
			sb.append(fileDir);
			sb.append("www.google.com.png)'><a href=\"http://www.google.com\">Google</a></li>");
			sb.append(fileDir);
			sb.append("mobile.twitter.com.png)'><a href=\"http://twitter.com\">Twitter</a></li>");
			// sb.append(fileDir);
			// sb.append("en.wikipedia.org.png)'><a href=\"http://en.wikipedia.org/wiki/Main_Page\">Wikipedia</a></li>");
			sb.append(fileDir);
			sb.append("bpc.borqs.com.png)'><a href=\"http://bpc.borqs.com\">Phoenix3</a></li>");
		}
		// additional top list for some locale
		if (Locale.JAPAN.equals(mLocale) || Locale.JAPANESE.equals(mLocale)) {
			sb.append(fileDir);
			sb.append("m.yahoo.co.jp.png)'><a href=\"http://www.yahoo.co.jp\">Yahoo!JAPAN</a></li>");
		}
		else if ("ru_RU".equals(mLocale.toString())) {
			sb.append(fileDir);
			sb.append("www.yandex.ru.png)'><a href=\"http://www.yandex.ru/?clid=1911433\">Яндекс</a></li>");
		}
		sb.append("</dl>");

		if (mBookMark.size() > 0) {
			tmp = getString(R.string.bookmark);
			if (countDown > 0)
				tmp += getString(R.string.pic_can_longclick);
			//if (countDown > 1) tmp += "\t" + countDown;
			if (collapse2) {
				sb.append("<h4 id=\"title2\" onClick=\"collapse(2)\" >+\t");
				sb.append(tmp);
				sb.append("</h4>");
				sb.append("<dl id=\"content2\" type=\"disc\" style=\"display: none;\" >");
			} else {
				sb.append("<h4 id=\"title2\" onClick=\"collapse(2)\" >-\t");
				sb.append(tmp);
				sb.append("</h4>");
				sb.append("<dl id=\"content2\" type=\"disc\">");
			}
			for (int i = 0; i < mBookMark.size(); i++) {
				sb.append(fileDir);
				sb.append(mBookMark.get(i).m_site);
				sb.append(".png)'><a href=\"");
				sb.append(mBookMark.get(i).m_url);
				sb.append("\">");
				sb.append(mBookMark.get(i).m_title);
				sb.append("</a></li>");
			}
			sb.append("</dl>");
		}

		if (mHistory.size() > 0) {
			tmp = getString(R.string.history);
			if (countDown > 0)
				tmp += getString(R.string.text_can_longclick);
			//if (countDown > 1) tmp += "\t" + countDown;
			if (collapse3) {
				sb.append("<h4 id=\"title3\" onClick=\"collapse(3)\" >+\t");
				sb.append(tmp);
				sb.append("</h4>");
				sb.append("<dl id=\"content3\" type=\"disc\" style=\"display: none;\" >");
			} else {
				sb.append("<h4 id=\"title3\" onClick=\"collapse(3)\" >-\t");
				sb.append(tmp);
				sb.append("</h4>");
				sb.append("<dl id=\"content3\" type=\"disc\">");
			}
			for (int i = mHistory.size()-1; i >= 0; i--) {
				sb.append(fileDir);
				sb.append(mHistory.get(i).m_site);
				sb.append(".png)'><a href=\"");
				sb.append(mHistory.get(i).m_url);
				sb.append("\">");
				sb.append(mHistory.get(i).m_title);
				sb.append("</a></li>");
			}
			sb.append("</dl>");
		}
		if (countDown > 0)
			countDown -= 1;

		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
	}

	class TitleUrl {
		String m_title;
		String m_url;
		String m_site;

		TitleUrl(String title, String url, String site) {
			if (title != null)
				m_title = title;
			else
				m_title = url;
			m_url = url;
			m_site = site;
		}
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

		try {// write to /sdcard/simpleHome/bookmark/
			File file = new File(downloadPath + "bookmark/" + filename);
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
			e.printStackTrace();
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
			if (cursor.moveToFirst()) {
				int columnTitle = cursor
						.getColumnIndex(Browser.BookmarkColumns.TITLE);
				int columnUrl = cursor
						.getColumnIndex(Browser.BookmarkColumns.URL);
				int columnBookmark = cursor
						.getColumnIndex(Browser.BookmarkColumns.BOOKMARK);

				while (!cursor.isAfterLast()) {
					try {
						String url = cursor.getString(columnUrl).trim();
						String site = "";
						String[] tmp = url.split("/");
						if (tmp.length > 2)
							site = tmp[2];// if url is http://m.baidu.com, then
											// url.split("/")[2] is m.baidu.com
						else
							site = tmp[0];

						TitleUrl titleUrl = new TitleUrl(
								cursor.getString(columnTitle), url, site);
						if (cursor.getInt(columnBookmark) >= 1)
							mSystemBookMark.add(titleUrl);
						else
							mSystemHistory.add(titleUrl);
					} catch (Exception e) {
					}

					cursor.moveToNext();
				}
			}
			cursor.close();
		}
	}

	ArrayList<TitleUrl> readBookmark(String filename) {
		ArrayList<TitleUrl> bookmark = new ArrayList<TitleUrl>();
		ObjectInputStream ois = null;
		FileInputStream fi = null;
		try {// read favorite or shortcut data from sdcard at first. if fail
				// then read from /data/data
			try {
				File file = new File(downloadPath + "bookmark/" + filename);
				fi = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				fi = openFileInput(filename);
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
			} catch (Exception e1) {
			}
		} catch (Exception e) {
		}

		return bookmark;
	}

}
