package easy.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import common.lib.ClearFolderTask;
import common.lib.EasyApp;
import common.lib.EasyWebView;
import common.lib.MyApp.WriteTask;
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
import android.graphics.Matrix;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SimpleBrowser extends Activity {
	Context mContext;

	ListView webList;

	// source dialog
	AlertDialog m_sourceDialog = null;

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

	// bookmark and history
	ImageView imgAddFavo, imgGo, imgHome;

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
	
	EasyApp appstate;

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
				if (clearCache) appstate.ClearCache();

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
					cltask.execute(appstate.dataPath + "files/", "png");
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

				String[] paras = {"", "", ""};
				if (clearHistory) {
					appstate.mHistory.clear();
					paras[0] = "history";
					appstate.clearFile("searchwords");
					appstate.siteArray.clear();
					appstate.urlAdapter.clear();
					if (appstate.HOME_BLANK.equals(appstate.webAddress.getText().toString()))
						shouldReload = true;
				}

				if (clearBookmark) {
					appstate.mBookMark.clear();
					paras[1] = "bookmark";
					if (appstate.HOME_BLANK.equals(appstate.webAddress.getText().toString()))
						shouldReload = true;
				}

				if (clearHistory || clearBookmark) {
					WriteTask wtask = appstate.new WriteTask();
					wtask.execute(paras);
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
			}

			// default to full screen now
			appstate.snapFullWeb = appstate.sp.getBoolean("full_web", false);

			appstate.incognitoMode = appstate.sp.getBoolean("incognito", false);
			if (!appstate.incognitoMode) appstate.webAddress.setAdapter(appstate.urlAdapter);
			else appstate.webAddress.setAdapter(appstate.emptyUrlAdapter);

			appstate.updownButton = appstate.sp.getBoolean("up_down", false);
			if (appstate.updownButton) appstate.upAndDown.setVisibility(View.VISIBLE);
			else appstate.upAndDown.setVisibility(View.GONE);
			
			appstate.shareMode = appstate.sp.getInt("share_mode", 1);
			
			appstate.searchEngine = appstate.sp.getInt("search_engine", 5);

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

			appstate.textSize = WebUtil.readTextSize(appstate.sp); // no need to reload page if fontSize changed
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
			//localSettings.setJavaScriptEnabled(!appstate.blockJs);

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
			webSettings.setGeolocationEnabled(html5);//API5
			if (html5) {
				webSettings.setAppCachePath(getDir("databases", MODE_PRIVATE).getPath());// API7
				// it will cause crash on OPhone if not set the max size
				webSettings.setAppCacheMaxSize(appstate.html5cacheMaxSize);
				webSettings.setDatabasePath(getDir("databases", MODE_PRIVATE)
						.getPath());// API5. how slow will it be if set path to sdcard?
				webSettings.setGeolocationDatabasePath(getDir("databases", MODE_PRIVATE).getPath());//API5
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

	public void setWebpagesLayout() {
		appstate.setUrlHeight(appstate.showUrl);
		appstate.setBarHeight(appstate.showControlBar);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		if (appstate.showUrl)
			lp.addRule(RelativeLayout.BELOW, R.id.urlline);
		else
			lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		if (appstate.showControlBar)
			lp.addRule(RelativeLayout.ABOVE, R.id.webtools);
		else 
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		webs.setLayoutParams(lp);
		webs.requestLayout();
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
				case 3:// open in foreground
					appstate.openNewPage(url, appstate.webIndex+1, true, true); 
					break;
				case 4:// copy url
					ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					ClipMan.setText(url);
					break;
				case 5:// share url
					appstate.shareUrl("", url);
					break;
				case 6:// open in background
					appstate.openNewPage(url, appstate.webAdapter.getCount(), false, true);// use openNewPage(url, webIndex+1, true, true) for open in new tab 
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
				case 10:// add bookmark
					int historyIndex = -1;
					for (int i = 0; i < appstate.mHistory.size(); i++) {
						if (appstate.mHistory.get(i).m_url.equals(url)) {
							historyIndex = i;
							break;
						}
					}
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
			if (appstate.dm.heightPixels > appstate.dm.density*480) // only show this menu item on large screen
				menu.add(0, 3, 0, R.string.open_new).setOnMenuItemClickListener(handler);
			menu.add(0, 4, 0, R.string.copy_url).setOnMenuItemClickListener(handler);
			menu.add(0, 5, 0, R.string.shareurl).setOnMenuItemClickListener(handler);
			menu.add(0, 6, 0, R.string.open_background).setOnMenuItemClickListener(handler);

			if (appstate.HOME_PAGE.equals(appstate.serverWebs.get(appstate.webIndex).getUrl())) {// only operate bookmark/history in home page
				boolean foundBookmark = false;
				for (int i = appstate.mBookMark.size() - 1; i >= 0; i--)
					if ((appstate.mBookMark.get(i).m_url.equals(url))
							|| (url.equals(appstate.mBookMark.get(i).m_url + "/"))) {
						foundBookmark = true;
						if (!appstate.mAdAvailable) {
							menu.add(0, 7, i, R.string.add_shortcut).setOnMenuItemClickListener(handler);// only work in pro version
							menu.add(0, 11, i, R.string.set_homepage).setOnMenuItemClickListener(handler);// only work in pro version
						}
						menu.add(0, 8, i, R.string.remove_bookmark).setOnMenuItemClickListener(handler);
						break;
					}
				if (!foundBookmark)
					menu.add(0, 10, 0, R.string.add_bookmark).setOnMenuItemClickListener(handler);

				for (int i = appstate.mHistory.size() - 1; i >= 0; i--)
					if ((appstate.mHistory.get(i).m_url.equals(url))
							|| (url.equals(appstate.mHistory.get(i).m_url + "/"))) {
						menu.add(0, 9, i, R.string.remove_history)
								.setOnMenuItemClickListener(handler);
						break;
					}
			}

			menu.add(0, 0, 0, R.string.save).setOnMenuItemClickListener(handler);
		}
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		appstate.menuOpenAction();
		return false;// show system menu if return true.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");// must create one menu?
		return super.onCreateOptionsMenu(menu);
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
					String title = appstate.serverWebs.get(appstate.webIndex).getTitle();
					if (title == null) title = WebUtil.getSite(appstate.serverWebs.get(appstate.webIndex).m_url);
					title += "_source.txt";
					String site = appstate.downloadPath + "source/";
					String snap = site + title;
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
	
	public void showSourceDialog() {
		if (m_sourceDialog == null) initSourceDialog();
		m_sourceDialog.setTitle(appstate.serverWebs.get(appstate.webIndex).getTitle());
		if (appstate.HOME_PAGE.equals(appstate.serverWebs.get(appstate.webIndex).getUrl()))
			m_sourceDialog.setIcon(R.drawable.explorer);
		else
			m_sourceDialog.setIcon(new BitmapDrawable(appstate.serverWebs.get(appstate.webIndex).getFavicon()));
		m_sourceDialog.setMessage(appstate.serverWebs.get(appstate.webIndex).pageSource);
		m_sourceDialog.show();
	}

	public void initUpDown() {
		appstate.upAndDown = (LinearLayout) findViewById(R.id.up_down);
		if (appstate.updownButton) appstate.upAndDown.setVisibility(View.VISIBLE);
		else appstate.upAndDown.setVisibility(View.GONE);
		
		ImageView dragButton = (ImageView) findViewById(R.id.page_drag);
		dragButton.setAlpha(80);
		dragButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int[] temp = new int[] { 0, 0 };
				int eventAction = event.getAction();
				int x = (int) event.getRawX();
				int y = (int) event.getRawY();
				int offset = - appstate.urlLine.getHeight();
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
		
		// page up and down button
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

	public void initWebControl() {
		// web control
		appstate.webControl = (LinearLayout) findViewById(R.id.webcontrol);
		
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
		appstate.mEasyActivity = this;
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

		appstate.downloadPath = util.preparePath(mContext);
		appstate.dataPath = "/data/data/" + getPackageName() + "/";
		if (appstate.downloadPath == null) appstate.downloadPath = appstate.dataPath;
		if (appstate.downloadPath.startsWith(appstate.dataPath)) appstate.noSdcard = true;

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
		
		appstate.urlAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
		appstate.emptyUrlAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
		appstate.initSiteArray();

		cm = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		WebIconDatabase.getInstance().open(getDir("databases", MODE_PRIVATE).getPath());

		while (appstate.serverWebs.size() > 0) {
			EasyWebView tmp = (EasyWebView) appstate.webpages.getChildAt(0);
			if (tmp == null) break;//sometime it is null if close page very quick
			appstate.webAdapter.remove(tmp);
			appstate.webAdapter.notifyDataSetInvalidated();
			try {
				appstate.webpages.removeView(tmp);
			} catch (Exception e) {
			}// null pointer reported by 3 user. really strange.
			tmp.destroy();
			System.gc();
		}

		appstate.webIndex = 0;
		appstate.serverWebs.add(new EasyWebView(mContext, appstate));
		appstate.webpages = (MyViewFlipper) findViewById(R.id.webpages);
		appstate.webpages.addView(appstate.serverWebs.get(appstate.webIndex));

		appstate.webTools = (LinearLayout) findViewById(R.id.webtools);
		appstate.urlLine = (LinearLayout) findViewById(R.id.urlline);
		webs = (RelativeLayout) findViewById(R.id.webs);
		
		appstate.adContainer = (LinearLayout) findViewById(R.id.adContainer);
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
		
		imgHome = (ImageView) findViewById(R.id.home);
		imgHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if ((appstate.m_homepage != null) && (!"".equals(appstate.m_homepage))) appstate.serverWebs.get(appstate.webIndex).loadUrl(appstate.m_homepage);
				else if (!appstate.HOME_PAGE.equals(appstate.serverWebs.get(appstate.webIndex).m_url)) appstate.loadPage();
			}
		});
		imgHome.setOnLongClickListener(new OnLongClickListener() {
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

		setLayout();
		setWebpagesLayout();
		initUpDown();

		appstate.urlLine.bringToFront();// set the z-order
		appstate.webTools.bringToFront();

		final FrameLayout toolAndAd = (FrameLayout) findViewById(R.id.webtoolnad);
		toolAndAd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {//reverse the position of webtoolbutton and ad
				exchangePosition();
			}
		});
		toolAndAd.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				onMenuOpened(0, null);
				return false;
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
	}

	void exchangePosition() {
		//reverse the position of buttons and ads
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

	@Override
	protected void onDestroy() {
		unregisterReceiver(downloadReceiver);
		unregisterReceiver(packageReceiver);

		if (appstate.adview != null) appstate.adview.destroy();
		if (appstate.adview2 != null) appstate.adview2.destroy();

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
	protected void onSaveInstanceState(Bundle outState) {
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

	void setLayout() {
		getWindowManager().getDefaultDisplay().getMetrics(appstate.dm);
		appstate.createAd(appstate.dm.widthPixels / appstate.dm.density);

		if (appstate.dm.widthPixels < 320*appstate.dm.density) {
			imageBtnList.getLayoutParams().width = appstate.dm.widthPixels;
			appstate.adContainer2.setVisibility(View.GONE);
		}
		else if (appstate.dm.widthPixels <= 640*appstate.dm.density) {
			imageBtnList.getLayoutParams().width = (int) (320 * appstate.dm.density);
			appstate.adContainer2.setVisibility(View.GONE);			
		}
		else {
			imageBtnList.getLayoutParams().width = (int) (320 * appstate.dm.density);
			if (appstate.adview2 != null) appstate.adview2.loadAd();
			appstate.adContainer2.setVisibility(View.VISIBLE);
		}
	}

}