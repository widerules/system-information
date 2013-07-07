package easy.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import common.lib.ClearFolderTask;
import common.lib.EasyApp;
import common.lib.EasyWebView;
import common.lib.MyViewFlipper;
import common.lib.ProxySettings;
import common.lib.WrapValueCallback;
import common.lib.WrapWebSettings;
import base.lib.WebUtil;
import base.lib.util;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
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
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
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

//for get webpage source on cupcake
public class SimpleBrowser extends Activity {



	Context mContext;
	Button btnNewpage;
	ListView webList;


	// snap dialog
	ImageView snapView;
	Bitmap bmp;
	AlertDialog snapDialog = null;


	// menu dialog
	AlertDialog menuDialog;// menu Dialog
	GridView menuGrid;
	View menuView;
	int historyIndex = -1;
	AlertDialog downloadsDialog = null;


	LinearLayout imageBtnList;
	RelativeLayout webs;

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

	// bookmark and history
	AlertDialog m_sourceDialog = null;
	ImageView imgAddFavo, imgGo;

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
	
	boolean gotoSettings = false;// will set to true if open settings activity,
									// and set to false again after exit
									// settings. not remove ad is goto settings
	
	String dataPath = "";
	EasyApp appstate;


	public void ClearCache() {
		appstate.serverWebs.get(appstate.webIndex).clearCache(true);
		// mContext.deleteDatabase("webviewCache.db");//this may get
		// disk IO crash
		ClearFolderTask cltask = new ClearFolderTask();
		// clear cache on sdcard and in data folder
		cltask.execute(appstate.downloadPath + "cache/webviewCache/", dataPath + "cache/webviewCache/");		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == appstate.FILECHOOSER_RESULTCODE) {
			if ((null == appstate.mUploadMessage) || (null == appstate.mUploadMessage.mInstance))
				return;
			Uri result = intent == null || resultCode != RESULT_OK ? null
					: intent.getData();
			if (mValueCallbackAvailable)
				appstate.mUploadMessage.onReceiveValue(result);
			appstate.mUploadMessage.mInstance = null;
		} else if (requestCode == appstate.SETTING_RESULTCODE) {
			boolean shouldReload = false;

			boolean clearData = appstate.sp.getBoolean("clear_data", false);
			if (clearData) {
				appstate.sEdit.putBoolean("clear_data", false);

				for (int i = 0; i < appstate.webAdapter.getCount(); i++)
					appstate.serverWebs.get(i).stopLoading();// stop loading while clear

				boolean clearCache = appstate.sp.getBoolean("clear_cache", false);
				if (clearCache) ClearCache();

				boolean clearCookie = appstate.sp.getBoolean("clear_cookie", false);
				if (clearCookie) {
					CookieSyncManager.createInstance(mContext);
					CookieManager.getInstance().removeAllCookie();
				}

				boolean clearFormdata = appstate.sp.getBoolean("clear_formdata", false);
				if (clearFormdata) {
					WebViewDatabase.getInstance(mContext).clearFormData();
				}

				boolean clearPassword = appstate.sp.getBoolean("clear_password", false);
				if (clearPassword) {
					WebViewDatabase.getInstance(mContext)
							.clearHttpAuthUsernamePassword();
					WebViewDatabase.getInstance(mContext)
							.clearUsernamePassword();
				}

				boolean clearIcon = appstate.sp.getBoolean("clear_icon", false);
				if (clearIcon) {
					ClearFolderTask cltask = new ClearFolderTask();
					// clear cache on sdcard and in data folder
					cltask.execute(dataPath + "files/", "png");
					if (appstate.HOME_BLANK.equals(appstate.webAddress.getText().toString())) shouldReload = true;
				}
				
				boolean clearHome = appstate.sp.getBoolean("clear_home", false);
				if (clearHome) {
					appstate.m_homepage = null;
					appstate.sEdit.putString("homepage", null);
				}
				
				boolean clearHistory = appstate.sp.getBoolean("clear_history", false);
				boolean clearBookmark = appstate.sp.getBoolean("clear_bookmark", false);

				if (clearHistory && clearBookmark && clearCache && clearCookie
						&& clearFormdata && clearPassword && clearIcon) {// clear all
					// mContext.deleteDatabase("webview.db");//this may get disk IO crash
					ClearFolderTask cltask = new ClearFolderTask();
					// clear files folder and app_databases folder
					cltask.execute(getFilesDir().getAbsolutePath(),
							getDir("databases", MODE_PRIVATE).getAbsolutePath());
				}

				if (clearHistory) {
					appstate.mHistory.clear();
					appstate.wtask.execute("history");
					clearFile("searchwords");
					appstate.siteArray.clear();
					appstate.urlAdapter.clear();
					appstate.historyChanged = false;
					if (appstate.HOME_BLANK.equals(appstate.webAddress.getText().toString()))
						shouldReload = true;
				}

				if (clearBookmark) {
					appstate.mBookMark.clear();
					appstate.wtask.execute("bookmark");
					appstate.bookmarkChanged = false;
					if (appstate.HOME_BLANK.equals(appstate.webAddress.getText().toString()))
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

			boolean tmpShow = appstate.sp.getBoolean("show_statusBar", true);
			if (tmpShow != appstate.showStatusBar) {
				appstate.showStatusBar = tmpShow;
				if (!appstate.showStatusBar) {// full screen
					getWindow().setFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN,
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				} else {
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}
			}
			
			appstate.showUrl = appstate.sp.getBoolean("show_url", true);
			appstate.showControlBar = appstate.sp.getBoolean("show_controlBar", true);
			setWebpagesLayout();
			
			int tmpMode = appstate.sp.getInt("rotate_mode", 1);
			if (appstate.rotateMode != tmpMode) {
				appstate.rotateMode = tmpMode;
				if (appstate.rotateMode == 1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				else if (appstate.rotateMode == 2) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				setLayout();
			}

			// default to full screen now
			appstate.snapFullWeb = appstate.sp.getBoolean("full_web", false);

			appstate.incognitoMode = appstate.sp.getBoolean("incognito", false);
			
			appstate.updownButton = appstate.sp.getBoolean("up_down", false);
			if (appstate.updownButton) appstate.upAndDown.setVisibility(View.VISIBLE);
			else appstate.upAndDown.setVisibility(View.INVISIBLE);
			
			appstate.shareMode = appstate.sp.getInt("share_mode", 2);
			
			appstate.searchEngine = appstate.sp.getInt("search_engine", 3);

			boolean tmpEnableProxy = appstate.sp.getBoolean("enable_proxy", false);
			int tmpLocalPort = appstate.sp.getInt("local_port", 1984);
			if ((appstate.enableProxy != tmpEnableProxy) || (appstate.localPort != tmpLocalPort)) {
				appstate.enableProxy = tmpEnableProxy;
				appstate.localPort = tmpLocalPort;
				if (appstate.enableProxy)
					ProxySettings.setProxy(mContext, "127.0.0.1", appstate.localPort);
				else
					try {
						ProxySettings.resetProxy(mContext);
					} catch (Exception e) {}
			}

			WebSettings localSettings = appstate.serverWebs.get(appstate.webIndex).getSettings();

			appstate.ua = appstate.sp.getInt("ua", 0);
			if (appstate.ua <= 1)
				localSettings.setUserAgent(appstate.ua);
			else
				localSettings.setUserAgentString(WebUtil.selectUA(appstate.ua));

			// hideExit = sp.getBoolean("hide_exit", true);

			WebUtil.readTextSize(appstate.sp); // no need to reload page if fontSize changed
			localSettings.setTextSize(appstate.textSize);

			appstate.blockImage = appstate.sp.getBoolean("block_image", false);
			localSettings.setBlockNetworkImage(appstate.blockImage);

			boolean tmpCachePrefer = appstate.sp.getBoolean("cache_prefer", false);
			if (tmpCachePrefer != appstate.cachePrefer) {
				appstate.cachePrefer = tmpCachePrefer;
				if (appstate.cachePrefer)
					localSettings
							.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
				else
					localSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
			}

			appstate.blockPopup = appstate.sp.getBoolean("block_popup", false);
			localSettings.setJavaScriptCanOpenWindowsAutomatically(appstate.blockPopup);
			// localSettings.setSupportMultipleWindows(!blockPopup);

			appstate.blockJs = appstate.sp.getBoolean("block_js", false);
			//localSettings.setJavaScriptEnabled(!blockJs);

			WrapWebSettings webSettings = new WrapWebSettings(localSettings);
			appstate.overviewPage = appstate.sp.getBoolean("overview_page", false);
			webSettings.setLoadWithOverviewMode(appstate.overviewPage);

			boolean showZoom = appstate.sp.getBoolean("show_zoom", false);
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
				appstate.serverWebs.get(appstate.webIndex).zoomVisible = showZoom;
			} else {
				if (showZoom)
					appstate.serverWebs.get(appstate.webIndex).setZoomControl(View.VISIBLE);
				else
					appstate.serverWebs.get(appstate.webIndex).setZoomControl(View.GONE);
			}

			boolean html5 = appstate.sp.getBoolean("html5", false);
			appstate.serverWebs.get(appstate.webIndex).html5 = html5;
			webSettings.setAppCacheEnabled(html5);// API7
			webSettings.setDatabaseEnabled(html5);// API5
			// webSettings.setGeolocationEnabled(html5);//API5
			if (html5) {
				webSettings.setAppCachePath(getDir("databases", MODE_PRIVATE)
						.getPath());// API7
				// it will cause crash on OPhone if not set the max size
				webSettings.setAppCacheMaxSize(appstate.html5cacheMaxSize);
				webSettings.setDatabasePath(getDir("databases", MODE_PRIVATE)
						.getPath());// API5. how slow will it be if set path to
				// sdcard?
				// webSettings.setGeolocationDatabasePath(getDir("databases",
				// MODE_PRIVATE).getPath());//API5
			}

			String tmpEncoding = WebUtil.getEncoding(appstate.sp.getInt("encoding", 0));
			if (!tmpEncoding.equals(localSettings.getDefaultTextEncodingName())) {
				if ("AUTOSELECT".equals(tmpEncoding) && "Latin-1".equals(localSettings.getDefaultTextEncodingName())) ;//not reload in this case
				else {
					localSettings.setDefaultTextEncodingName(tmpEncoding);
					// set default encoding to autoselect
					appstate.sEdit.putInt("encoding", 0);
					shouldReload = true;
				}
			}

			if (shouldReload) appstate.serverWebs.get(appstate.webIndex).reload();
			appstate.sEdit.commit();
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
		appstate.setUrlHeight(appstate.showUrl);
		appstate.setBarHeight(appstate.showControlBar);
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
					appstate.startDownload(url, ext, "yes");
					break;
				case 4:// copy url
					ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipMan.setText(url);
					break;
				case 5:// share url
					appstate.shareUrl("", url);
					break;
				case 6:// open in background
					appstate.openNewPage(url, appstate.webAdapter.getCount(), false, true);// use openNewPage(url, webIndex+1, true) for open in new tab 
					break;
				case 7:// add short cut
					appstate.createShortcut(url, appstate.mBookMark.get(item.getOrder()).m_title);
					break;
				case 8:// remove bookmark
					appstate.removeFavo(item.getOrder());
					break;
				case 9:// remove history
					appstate.removeHistory(item.getOrder());
					break;
				case 10:// add bookmark. not use now
					if (historyIndex > -1)
						appstate.addFavo(url, appstate.mHistory.get(historyIndex).m_title);
					else appstate.addFavo(url, url);
					break;
				case 11://set homepage
					appstate.m_homepage = url;
					appstate.sEdit.putString("homepage", url);
					appstate.sEdit.commit();
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

			if (appstate.HOME_PAGE.equals(appstate.serverWebs.get(appstate.webIndex).getUrl())) {// only operate bookmark/history in home page
				menu.add(0, 6, 0, R.string.open_background).setOnMenuItemClickListener(handler);
				
				boolean foundBookmark = false;
				for (int i = appstate.mBookMark.size() - 1; i >= 0; i--)
					if ((appstate.mBookMark.get(i).m_url.equals(url))
							|| (url.equals(appstate.mBookMark.get(i).m_url + "/"))) {
						foundBookmark = true;
						if (!appstate.mAdAvailable) {
							menu.add(0, 7, i, R.string.add_shortcut).setOnMenuItemClickListener(handler);// only work in pro version
							menu.add(0, 11, i, R.string.set_homepage).setOnMenuItemClickListener(handler);// only work in pro version
						}
						menu.add(0, 8, i, R.string.remove_bookmark)
								.setOnMenuItemClickListener(handler);
						break;
					}
				//if (!foundBookmark) menu.add(0, 7, 0, R.string.add_bookmark).setOnMenuItemClickListener(handler);// no add bookmark on long click?

				historyIndex = -1;
				for (int i = appstate.mHistory.size() - 1; i >= 0; i--)
					if ((appstate.mHistory.get(i).m_url.equals(url))
							|| (url.equals(appstate.mHistory.get(i).m_url + "/"))) {
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

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (menuDialog == null) initMenuDialog();
		
		if (menuDialog.isShowing()) menuDialog.dismiss();
		else {
			appstate.setUrlHeight(true);
			appstate.setBarHeight(true);
			
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
							String snap = appstate.downloadPath + "snap/" + appstate.serverWebs.get(appstate.webIndex).getTitle() + ".png";
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
							String snap = appstate.downloadPath + "snap/" + appstate.serverWebs.get(appstate.webIndex).getTitle() + ".png";
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
				intent.putExtra(Intent.EXTRA_TEXT, appstate.serverWebs.get(appstate.webIndex).pageSource);
				intent.putExtra(Intent.EXTRA_SUBJECT, appstate.serverWebs.get(appstate.webIndex).getTitle());
				if (!util.startActivity(intent, false, getBaseContext())) 
					appstate.shareUrl("", appstate.serverWebs.get(appstate.webIndex).pageSource);
			}
		})
		.setNeutralButton(R.string.save, new DialogInterface.OnClickListener() {// save
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					String snap = appstate.downloadPath + "source/" + appstate.serverWebs.get(appstate.webIndex).getTitle() + ".txt";
					FileOutputStream fos = new FileOutputStream(snap);
					fos.write(appstate.serverWebs.get(appstate.webIndex).pageSource.getBytes());
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
		if (appstate.dm.heightPixels <= 480)
			params.y = 70;
		else if (appstate.dm.heightPixels <= 800)
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
						if ("".equals(appstate.serverWebs.get(appstate.webIndex).pageSource)) {
							appstate.serverWebs.get(appstate.webIndex).pageSource = "Loading... Please try again later.";
							appstate.serverWebs.get(appstate.webIndex).getPageSource();
						}

						if (m_sourceDialog == null) initSourceDialog();
						m_sourceDialog.setTitle(appstate.serverWebs.get(appstate.webIndex)
								.getTitle());
						if (appstate.HOME_PAGE.equals(appstate.serverWebs.get(appstate.webIndex).getUrl()))
							m_sourceDialog.setIcon(R.drawable.explorer);
						else
							m_sourceDialog.setIcon(new BitmapDrawable(
									appstate.serverWebs.get(appstate.webIndex).getFavicon()));
						m_sourceDialog.setMessage(appstate.serverWebs.get(appstate.webIndex).pageSource);
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
						if (!appstate.snapFullWeb) {
							// the snap will not refresh if not destroy cache
							appstate.webpages.destroyDrawingCache();
							appstate.webpages.setDrawingCacheEnabled(true);
							bmp = appstate.webpages.getDrawingCache();
						} else {
							Picture pic = appstate.serverWebs.get(appstate.webIndex)
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
						snapDialog.setTitle(appstate.serverWebs.get(appstate.webIndex).getTitle());
						if (appstate.HOME_PAGE.equals(appstate.serverWebs.get(appstate.webIndex).getUrl()))
							snapDialog.setIcon(R.drawable.explorer);
						else
							snapDialog.setIcon(new BitmapDrawable(appstate.serverWebs
									.get(appstate.webIndex).getFavicon()));
						snapDialog.show();
					} catch (Exception e) {
						Toast.makeText(mContext, e.toString(),
								Toast.LENGTH_LONG).show();
					}
					break;
				case 2:// copy
					appstate.webControl.setVisibility(View.INVISIBLE);// hide webControl when copy
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
						shiftPressEvent.dispatch(appstate.serverWebs.get(appstate.webIndex));
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
					appstate.shareUrl(appstate.serverWebs.get(appstate.webIndex).getTitle(), appstate.serverWebs.get(appstate.webIndex).m_url);
					break;
				case 6:// search
					appstate.webControl.setVisibility(View.INVISIBLE);// hide webControl when search
						// serverWebs.get(webIndex).showFindDialog("e", false);
					if (appstate.searchBar == null) initSearchBar();
					appstate.searchBar.bringToFront();
					appstate.searchBar.setVisibility(View.VISIBLE);
					appstate.etSearch.requestFocus();
					appstate.toSearch = "";
					appstate.imm.toggleSoftInput(0, 0);
					break;
				case 7:// settings
					gotoSettings = true;
					intent = new Intent(getPackageName() + "about");
					intent.setClassName(getPackageName(), AboutBrowser.class.getName());
					startActivityForResult(intent, appstate.SETTING_RESULTCODE);
					break;
				}
				menuDialog.dismiss();
			}
		});
	}
	
	public void initUpDown() {
		appstate.upAndDown = (LinearLayout) findViewById(R.id.up_down);
		if (appstate.updownButton) appstate.upAndDown.setVisibility(View.VISIBLE);
		else appstate.upAndDown.setVisibility(View.INVISIBLE);
		
		ImageView dragButton = (ImageView) findViewById(R.id.page_drag);
		dragButton.setAlpha(80);
		dragButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int[] temp = new int[] { 0, 0 };
				int eventAction = event.getAction();
				int x = (int) event.getRawX();
				int y = (int) event.getRawY();
				int offset = - appstate.urlLine.getHeight() - appstate.adContainer.getHeight();
				switch (eventAction) {
				case MotionEvent.ACTION_DOWN:// prepare for drag
					if (!appstate.showUrl) appstate.setUrlHeight(appstate.showUrl);
					if (!appstate.showControlBar) appstate.setBarHeight(appstate.showControlBar);
					temp[0] = (int) event.getX();
					temp[1] = y;
					break;

				case MotionEvent.ACTION_MOVE:// drag the button
					appstate.upAndDown.layout(
							x - temp[0], 
							y - temp[1] - appstate.upAndDown.getHeight() + offset, 
							x - temp[0] + appstate.upAndDown.getWidth(), 
							y - temp[1] + offset
						);
					appstate.upAndDown.postInvalidate();
					break;

				case MotionEvent.ACTION_UP:// reset the margin when stop drag
					MarginLayoutParams lp = (MarginLayoutParams) appstate.upAndDown.getLayoutParams();
					lp.setMargins(
							0, 
							Math.min(appstate.upAndDown.getTop(), 0), 
							Math.min(Math.max(webs.getWidth()-appstate.upAndDown.getRight(), 0), webs.getWidth()-appstate.upAndDown.getWidth()), 
							Math.min(Math.max(webs.getHeight()-appstate.upAndDown.getBottom(), 0), webs.getHeight()-20)
						);
					appstate.upAndDown.setLayoutParams(lp);
					break;
				}
				return true;
			}
		});
		
		ImageView upButton, downButton;
		upButton = (ImageView) findViewById(R.id.page_up);
		upButton.setAlpha(40);
		Matrix matrix = new Matrix();
		matrix.postRotate(180f, 24*appstate.dm.density, 24*appstate.dm.density);
		upButton.setImageMatrix(matrix);// rotate 180 degree
		upButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int eventAction = event.getAction();
				switch (eventAction) {
				case MotionEvent.ACTION_DOWN:
					appstate.actioned = false;
					lastTime = event.getEventTime();
					timeInterval = 100;
					break;
				case MotionEvent.ACTION_MOVE:
					if (event.getEventTime() - lastTime > timeInterval) {
						timeInterval = 70;
						lastTime = event.getEventTime();
						appstate.scrollUp();
					}
					break;
				case MotionEvent.ACTION_UP:
					if (!appstate.actioned) appstate.scrollUp();
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
					appstate.actioned = false;
					lastTime = event.getEventTime();
					timeInterval = 100;// the interval for first scroll is longer than the after, to avoid scroll twice
					break;
				case MotionEvent.ACTION_MOVE:
					if (event.getEventTime() - lastTime > timeInterval) {
						timeInterval = 70;
						lastTime = event.getEventTime();
						appstate.scrollDown();
					}
					break;
				case MotionEvent.ACTION_UP:
					if (!appstate.actioned) appstate.scrollDown();
					break;
				}
				return true;
			}
		});
	}
	
	long lastTime = 0;
	long timeInterval = 100;
	
	public void initSearchBar() {		
		appstate.imgSearchPrev = (ImageView) findViewById(R.id.search_prev);
		appstate.imgSearchPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				appstate.searchPrevAction();
			}
		});
		appstate.imgSearchNext = (ImageView) findViewById(R.id.search_next);
		appstate.imgSearchNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				appstate.searchNextAction();
			}
		});

		appstate.imgSearchClose = (ImageView) findViewById(R.id.close_search);
		appstate.imgSearchClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				appstate.hideSearchBox();
			}
		});

		appstate.searchBar = (RelativeLayout) findViewById(R.id.search_bar);
		appstate.searchHint = (TextView) findViewById(R.id.search_hint);
		appstate.etSearch = (EditText) findViewById(R.id.search);
		appstate.etSearch.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP)
					switch (keyCode) {
					case KeyEvent.KEYCODE_SEARCH:
					case KeyEvent.KEYCODE_ENTER:
					case KeyEvent.KEYCODE_DPAD_CENTER:
						appstate.imgSearchNext.performClick();
						break;
					}
				return false;
			}
		});		
	}
	
	public void initWebControl() {// identical
		// web control
		appstate.webControl = (LinearLayout) findViewById(R.id.webcontrol);
		//appstate.fakeWebControl = (LinearLayout) browserView.findViewById(R.id.fakeWebcontrol);///////////////////////////////build error////////////
		
		btnNewpage = (Button) findViewById(R.id.opennewpage);
		btnNewpage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {// add a new page
				if (appstate.m_homepage != null) appstate.openNewPage(appstate.m_homepage, appstate.webIndex+1, true, false);
				else appstate.openNewPage("", appstate.webIndex+1, true, false);
			}
		});
		// web list
		webList = (ListView) findViewById(R.id.weblist);
		//webList.inflate(mContext, R.layout.web_list, null);
		webList.setFadingEdgeLength(0);// no shadow when scroll
		webList.setScrollingCacheEnabled(false);
		webList.setAdapter(appstate.webAdapter);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// mContext = getApplicationContext();//this will cause force close when
		// select locale in google translate
		mContext = this;
		appstate = ((EasyApp) getApplicationContext());
		appstate.mContext = mContext;
		appstate.mActivity = this;
		appstate.webAdapter = appstate.new WebAdapter(mContext, appstate.serverWebs);

		appstate.dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(appstate.dm);

		appstate.browserName = getString(R.string.browser_name);
		
		// init settings
		appstate.sp = PreferenceManager.getDefaultSharedPreferences(mContext);
		appstate.sEdit = appstate.sp.edit();
		appstate.readPreference();

		setAsDefaultApp();
		
		appstate.nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		appstate.mContext = mContext;


		appstate.imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

		// hide titlebar of application, must be before setting the layout
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.browser);
        
		initWebControl();

		appstate.loadProgress = (ProgressBar) findViewById(R.id.loadprogress);

		imgAddFavo = (ImageView) findViewById(R.id.addfavorite);
		imgAddFavo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String url = appstate.serverWebs.get(appstate.webIndex).m_url;
				if (appstate.HOME_PAGE.equals(url)) return;// not add home page

				appstate.addRemoveFavo(url, appstate.serverWebs.get(appstate.webIndex).getTitle());
			}
		});
		imgAddFavo.setOnLongClickListener(new OnLongClickListener() {// long click to show bookmark
			@Override
			public boolean onLongClick(View arg0) {
				appstate.listBookmark();
				return true;
			}
		});

		imgGo = (ImageView) findViewById(R.id.go);
		imgGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				appstate.gotoUrl(appstate.webAddress.getText().toString().toLowerCase());
			}
		});
		imgGo.setOnLongClickListener(new OnLongClickListener() {// long click to select search engine
			@Override
			public boolean onLongClick(View arg0) {
				CharSequence engine[] = new CharSequence[] {getString(R.string.bing), getString(R.string.baidu), getString(R.string.google), getString(R.string.yandex), getString(R.string.duckduckgo)};
				appstate.selectEngine(engine);
				return true;
			}
		});

		appstate.webAddress = (AutoCompleteTextView) findViewById(R.id.url);
		appstate.webAddress.bringToFront();
		appstate.webAddress.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				appstate.gotoUrl(appstate.urlAdapter.getItem(position));
			}

		});
		appstate.webAddress.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView view, int actionId,
					KeyEvent event) {
				imgGo.performClick();
				return false;
			}
		});
		appstate.webAddress.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				view.setFocusableInTouchMode(true);
				appstate.serverWebs.get(appstate.webIndex).setFocusable(false);
				return false;
			}
		});

		dataPath = "/data/data/" + getPackageName() + "/";
		appstate.downloadPath = util.preparePath(mContext);
		if (appstate.downloadPath == null) appstate.downloadPath = dataPath;
		if (appstate.downloadPath.startsWith(dataPath)) appstate.noSdcard = true;

		// should read in below sequence: 1, sdcard. 2, data/data. 3, native browser
		try {
			FileInputStream fi = null;
			if (appstate.noSdcard) fi = openFileInput("history");
			else {
				try {// try to open history on sdcard at first
					File file = new File(appstate.downloadPath + "bookmark/history");
					fi = new FileInputStream(file);
				} catch (FileNotFoundException e) {// read from /data/data if fail
					appstate.noHistoryOnSdcard = true;
					fi = openFileInput("history");
				}
			}
			
			try {// close anyway
				fi.close();
			} catch (Exception e) {}
		} catch (FileNotFoundException e1) {
			appstate.firstRun = true;
		}
		
		appstate.urlAdapter = new ArrayAdapter<String>(mContext,	android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
		appstate.initSiteArray();
		
		cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		WebIconDatabase.getInstance().open(
				getDir("databases", MODE_PRIVATE).getPath());
		appstate.webIndex = 0;
		appstate.serverWebs.add(new EasyWebView(mContext, appstate));
		appstate.webpages = (MyViewFlipper) findViewById(R.id.webpages);
		appstate.webpages.addView(appstate.serverWebs.get(appstate.webIndex));

		appstate.webTools = (LinearLayout) findViewById(R.id.webtools);
		appstate.urlLine = (LinearLayout) findViewById(R.id.urlline);
		webs = (RelativeLayout) findViewById(R.id.webs);
		
		appstate.adContainer2 = (LinearLayout) findViewById(R.id.adContainer2);
		imageBtnList = (LinearLayout) findViewById(R.id.imagebtn_list);
		imageBtnList.bringToFront();
		
		if (!appstate.showStatusBar) 
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

		appstate.imgNext = (ImageView) findViewById(R.id.next);
		appstate.imgNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (appstate.serverWebs.get(appstate.webIndex).canGoForward())
					appstate.serverWebs.get(appstate.webIndex).goForward();
			}
		});
		appstate.imgNext.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				appstate.updownAction();
				return true;
			}
		});

		appstate.imgPrev = (ImageView) findViewById(R.id.prev);
		appstate.imgPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				appstate.imgPrevClick();
			}
		});
		appstate.imgPrev.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				appstate.listPageHistory();
				return true;
			}
		});

		appstate.imgRefresh = (ImageView) findViewById(R.id.refresh);
		appstate.imgRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				appstate.reloadPage();
			}
		});
		
		appstate.imgHome = (ImageView) findViewById(R.id.home);
		appstate.imgHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if ((appstate.m_homepage != null) && (!"".equals(appstate.m_homepage))) appstate.serverWebs.get(appstate.webIndex).loadUrl(appstate.m_homepage);
				else if (!appstate.HOME_PAGE.equals(appstate.serverWebs.get(appstate.webIndex).m_url)) appstate.loadPage();
			}
		});
		appstate.imgHome.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				appstate.globalSetting();
				return true;
			}
		});

		appstate.imgNew = (ImageView) findViewById(R.id.newpage);
		appstate.imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), 1, 2, appstate.dm.density, mContext));
		appstate.imgNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				appstate.imgNewClick();
			}
		});
		appstate.imgNew.setOnLongClickListener(new OnLongClickListener() {// long click to show history
			@Override
			public boolean onLongClick(View arg0) {
				appstate.listHistory();
				return true;
			}
		});

		appstate.adContainer = (FrameLayout) findViewById(R.id.adContainer);
		appstate.createAd();
		setLayout();
		setWebpagesLayout();
		initUpDown();

		appstate.urlLine.bringToFront();// set the z-order
		appstate.webTools.bringToFront();

		final FrameLayout toolAndAd = (FrameLayout) findViewById(R.id.webtoolnad);
		toolAndAd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {//reverse the position of webtoolbutton and ad
				LayoutParams lp1 = imageBtnList.getLayoutParams();
				LayoutParams lp2 = appstate.adContainer2.getLayoutParams();
				
				lp1.height = (int)(50 * appstate.dm.density);
				lp2.height = (int)(40 * appstate.dm.density);
				
				appstate.adContainer2.setLayoutParams(lp1);
				imageBtnList.setLayoutParams(lp2);
				
				if (appstate.adContainer2.getVisibility() == View.GONE) 
					return; // no need to change position if not width enough

				appstate.revertCount++;
				if ((appstate.revertCount > 1) && (appstate.revertCount % 2 == 0))
					appstate.needRevert = true;
				else appstate.needRevert = false;
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
				if (!appstate.incognitoMode && appstate.readPages("pages")) {
					appstate.closePage(0, false);// the first page is no use if open saved url or homepage
				}
				else if ((appstate.m_homepage != null) && !"".equals(appstate.m_homepage)) appstate.serverWebs.get(appstate.webIndex).loadUrl(appstate.m_homepage);
				else appstate.loadPage();// load about:blank if no url saved or homepage specified
			}
			else appstate.serverWebs.get(appstate.webIndex).loadUrl(getIntent().getDataString());
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

	@Override
	protected void onDestroy() {
		unregisterReceiver(screenLockReceiver);
		unregisterReceiver(downloadReceiver);
		unregisterReceiver(packageReceiver);

		if (appstate.adview != null) appstate.adview.destroy();
		if (appstate.adview2 != null) appstate.adview2.destroy();

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
	protected void onNewIntent(Intent intent) {// open file from sdcard // identical
		appstate.newIntentAction(intent);
		
		super.onNewIntent(intent);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() == 0) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				appstate.actionBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {//identical
		// Not call for super(). Bug on API Level > 11. refer to
		// http://stackoverflow.com/questions/7469082
	}

	@Override
	protected void onPause() {
		appstate.pauseAction();
		
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
		getWindowManager().getDefaultDisplay().getMetrics(appstate.dm);

		if (appstate.dm.widthPixels < 320*appstate.dm.density) {
			imageBtnList.getLayoutParams().width = appstate.dm.widthPixels;
			appstate.adContainer2.setVisibility(View.GONE);
			appstate.adContainer.setVisibility(View.VISIBLE);
		}
		else if (appstate.dm.widthPixels <= 640*appstate.dm.density) {
			imageBtnList.getLayoutParams().width = (int) (320 * appstate.dm.density);
			appstate.adContainer2.setVisibility(View.GONE);			
			appstate.adContainer.setVisibility(View.VISIBLE);
		}
		else {
			imageBtnList.getLayoutParams().width = (int) (320 * appstate.dm.density);
			if (appstate.adview2 != null) appstate.adview2.loadAd();
			appstate.adContainer2.setVisibility(View.VISIBLE);
			appstate.adContainer.setVisibility(View.GONE);
		}		
	}

}
