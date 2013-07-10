package common.lib;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
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
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.TextSize;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import base.lib.BaseApp;
import base.lib.StringComparator;
import base.lib.WebUtil;
import base.lib.util;
import base.lib.WrapAdView;
import base.lib.WrapInterstitialAd;

public class MyApp extends BaseApp {
	public final String HOME_PAGE = "file:///android_asset/home.html";
	public final String HOME_BLANK = "about:blank";
	public String browserName;
	public String m_homepage = null;

	// Ads
	public FrameLayout adContainer;
	public LinearLayout adContainer2;	
	public WrapAdView adview = null, adview2 = null;
	WrapInterstitialAd interstitialAd = null;
	public boolean interstitialAdClicked = false;
	boolean clicked = false;
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
				adContainer.setVisibility(View.GONE);
			}
			else if (msg.what == 1) {// remove ad when click ad
				clicked = true;
				//removeAd();
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
	AppHandler mAppHandler = new AppHandler();
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

	// settings
	public SharedPreferences sp;
	public Editor sEdit;

	public boolean showUrl = true;
	public boolean showControlBar = true;
	public boolean showStatusBar = true;
	final int urlHeight = 40, barHeight = 40;
	public int rotateMode = 1;
	public boolean incognitoMode = false;
	public boolean updownButton = true;
	public boolean snapFullWeb = false;
	public boolean blockImage = false;
	public boolean cachePrefer = false;
	public boolean blockPopup = false;
	public boolean blockJs = false;
	public TextSize textSize = TextSize.NORMAL;
	final int historyCount = 16;
	public long html5cacheMaxSize = 1024 * 1024 * 8;
	public int ua = 0;
	public int searchEngine = 3;
	public int shareMode = 2;
	public int SETTING_RESULTCODE = 1002;
	public boolean enableProxy = false;
	public int localPort;
	public boolean overviewPage = false;
	Locale mLocale;

	// bookmark and history
	public boolean historyChanged = false, bookmarkChanged = false, downloadsChanged = false;
	public boolean noSdcard = false;
	public boolean noHistoryOnSdcard = false;
	public boolean firstRun = false;

	public Activity mActivity;// the main activity
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
	public ArrayList<String> siteArray = new ArrayList<String>();
	public ArrayAdapter<String> urlAdapter;
	public AutoCompleteTextView webAddress;
	public ProgressBar loadProgress;
	public ImageView imgNext, imgPrev, imgHome, imgRefresh, imgNew;
	public WebAdapter webAdapter;
	public LinearLayout webTools, webControl, fakeWebControl, urlLine;
	public MyViewFlipper webpages;

	public WrapValueCallback mUploadMessage;
	public final int FILECHOOSER_RESULTCODE = 1001;

	public InputMethodManager imm;

	public int revertCount = 0;
	public boolean needRevert = false;
	public class WebAdapter extends ArrayAdapter<MyWebView> {
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
		WebUtil.readTextSize(sp);// init the text size
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
		if (rotateMode == 1) mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		else if (rotateMode == 2) mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
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
						mActivity.getWindow().setFlags(
								WindowManager.LayoutParams.FLAG_FULLSCREEN,
								WindowManager.LayoutParams.FLAG_FULLSCREEN);								
					}
					else {
						adContainer.setVisibility(View.VISIBLE);
						mActivity.getWindow().clearFlags(
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
					mActivity.moveTaskToBack(true);
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
		if (webControl.getVisibility() == View.INVISIBLE) {
			if (urlLine.getLayoutParams().height == 0) setUrlHeight(true);// show url if hided
		
			//if (webControl.getWidth() < minWebControlWidth) scrollToMain();/////////////////////////////////not identical////////////////////////////
			webAdapter.notifyDataSetInvalidated();
			webControl.setVisibility(View.VISIBLE);
			webControl.bringToFront();
		} else webControl.setVisibility(View.INVISIBLE);	
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
	
	public void loadPage() {// load home page // not identical. need inherit
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
		searchBar.setVisibility(View.INVISIBLE);
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

	void removeAd() {
		if (adview != null) {
			adContainer.setVisibility(View.GONE);
			adview.destroy();
			adview = null;
		}
	}

	public boolean startDownload(String url, String contentDisposition, String openAfterDone) {
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
			// the file is downloading. show download control then.
			Intent intent = new Intent("downloadControl");
			intent.setClassName(mContext.getPackageName(), DownloadControl.class.getName());
			intent.putExtra("name", apkName);
			intent.putExtra("url", url);
			util.startActivity(intent, false, mContext);
			return true;
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
	
	public boolean openNewPage(String url, int newIndex, boolean changeToNewPage, boolean closeIfCannotBack) {//identical
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
	
	public void reloadPage() {// identical
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
		new AlertDialog.Builder(mActivity)
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

	public void removeHistory(final int order) {//identical
		new AlertDialog.Builder(mActivity)
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
	public void addFavo(final String url, final String title) {//identical
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
		new AlertDialog.Builder(mActivity)
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
	
	public void createShortcut(String url, String title) {//identical
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
	
	public void shareUrl(String title, String url) {//identical
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
	
	public WriteTask wtask = new WriteTask();
	public class WriteTask extends AsyncTask<String, Integer, String> {//identical

		@Override
		protected String doInBackground(String... params) {
			if ("history".equals(params[0])) {
				writeBookmark("history", mHistory);
				historyChanged = false;
			} else if ("bookmark".equals(params[0])) {
				writeBookmark("bookmark", mBookMark);
				bookmarkChanged = false;
			} else {
				writeBookmark("downloads", mDownloads);
				downloadsChanged = false;
			}

			return null;
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
		webAddress.setAdapter(urlAdapter);		
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
				File file = new File(downloadPath + "bookmark/" + filename);
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
	
	public void pauseAction() {
		if (historyChanged) {
			WriteTask wtask = new WriteTask();
			wtask.execute("history");
		}
		if (bookmarkChanged) {
			WriteTask wtask = new WriteTask();
			wtask.execute("bookmark");
		}
		if (downloadsChanged) {
			WriteTask wtask = new WriteTask();
			wtask.execute("downloads");
		}

		sEdit.putBoolean("show_zoom", serverWebs.get(webIndex).zoomVisible);
		sEdit.putBoolean("html5", serverWebs.get(webIndex).html5);
		sEdit.commit();
	}
}
