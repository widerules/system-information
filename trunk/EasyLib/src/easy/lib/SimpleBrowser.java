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
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Map.Entry;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

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
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.Html;
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
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.webkit.GeolocationPermissions;


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

class wrapAdView {
	AdView mInstance;
	AdRequest adRequest;
	
	wrapAdView(Activity activity, int size, String deviceID) {
		switch(size) {
		case 0:
			mInstance = new AdView(activity, AdSize.BANNER, deviceID);
			break;
		case 1:
			mInstance = new AdView(activity, AdSize.IAB_BANNER, deviceID);
			break;
		case 2:
			mInstance = new AdView(activity, AdSize.IAB_LEADERBOARD, deviceID);
			break;
		}
		adRequest = new AdRequest();
		//adRequest.addTestDevice("E3CE9F94F56824C07AE1C3A5B434F664");//for test
	}

	static {
	       try {
	           Class.forName("com.google.ads.AdView");
	       } catch (Exception ex) {
	           throw new RuntimeException(ex);
	       }
	   }
	
	public static void checkAvailable() {}
	
	void loadAd() {
		mInstance.loadAd(adRequest);
	}
	
	void destroy() {
		mInstance.destroy();
	}
	
	View getInstance() {
		return mInstance;
	}
}


class wrapWebSettings {
	WebSettings mInstance;
	
	wrapWebSettings(WebSettings settings) {
		mInstance = settings;
	}

	synchronized void setJavaScriptEnabled(boolean enabled) {
    	mInstance.setJavaScriptEnabled(enabled);
    }
    
	synchronized void setSaveFormData(boolean save) {
    	mInstance.setSaveFormData(save);
    }
    
	synchronized void setTextSize(TextSize t) {
    	mInstance.setTextSize(t);
    }
    
	synchronized void setSupportZoom(boolean support) {
    	mInstance.setSupportZoom(support);
    }
    
	synchronized void setBuiltInZoomControls(boolean enabled) {
    	mInstance.setBuiltInZoomControls(enabled);
    }
    
	synchronized void setUseWideViewPort(boolean use) {
    	mInstance.setUseWideViewPort(use);
    }
    
	synchronized void setPluginsEnabled(boolean flag) {
    	mInstance.setPluginsEnabled(flag);
    }

	synchronized void setLoadWithOverviewMode(boolean overview) {//from API 7
    	try {
    		Method method = WebSettings.class.getMethod("setLoadWithOverviewMode", new Class[] {boolean.class});
    		method.invoke(mInstance, overview);
    	}
    	catch(Exception e) {e.printStackTrace();}
    }    
    
	synchronized void setAppCacheEnabled(boolean flag) {//API 7
    	try {
    		Method method = WebSettings.class.getMethod("setAppCacheEnabled", new Class[] {boolean.class});
    		method.invoke(mInstance, flag);
    	}
    	catch(Exception e) {e.printStackTrace();}
    }
    
	synchronized void setDomStorageEnabled(boolean flag) {//API 7
    	try {
    		Method method = WebSettings.class.getMethod("setDomStorageEnabled", new Class[] {boolean.class});
    		method.invoke(mInstance, flag);
    	}
    	catch(Exception e) {e.printStackTrace();}
    }
	
	synchronized void setDatabaseEnabled(boolean flag) {//API 5
    	try {
    		Method method = WebSettings.class.getMethod("setDatabaseEnabled", new Class[] {boolean.class});
    		method.invoke(mInstance, flag);
    	}
    	catch(Exception e) {e.printStackTrace();}
    }
    
	synchronized void setDatabasePath(String databasePath) {//API 5
    	try {
    		Method method = WebSettings.class.getMethod("setDatabasePath", new Class[] {String.class});
    		method.invoke(mInstance, databasePath);
    	}
    	catch(Exception e) {e.printStackTrace();}
    }

	synchronized void setGeolocationEnabled(boolean flag) {//API 5
    	try {
    		Method method = WebSettings.class.getMethod("setGeolocationEnabled", new Class[] {boolean.class});
    		method.invoke(mInstance, flag);
    	}
    	catch(Exception e) {e.printStackTrace();}
	}
	
	synchronized void setGeolocationDatabasePath(String databasePath) {//API 5
    	try {
    		Method method = WebSettings.class.getMethod("setGeolocationDatabasePath", new Class[] {String.class});
    		method.invoke(mInstance, databasePath);
    	}
    	catch(Exception e) {e.printStackTrace();}
	}
}


public class SimpleBrowser extends Activity {

	boolean paid, debug;
	final String BLANK_PAGE = "about:blank";

	ListView webList;
	Context mContext;

	//about dialog
	View aboutView;
	CheckBox showZoomControl;
	RadioButton btnFullScreen;
	AlertDialog aboutDialog = null;

	//snap dialog
	ImageView snapView;
	Bitmap bmp;
	AlertDialog snapDialog = null;
	
	//menu dialog
	AlertDialog menuDialog;// menu菜单Dialog
    GridView menuGrid;
    View menuView;
    int historyIndex = -1;
    AlertDialog downloadsDialog = null;
    
	//browser related
	AutoCompleteTextView webAddress;
	ArrayAdapter<String> urlAdapter;
	ArrayList<String> siteArray;
	
	ArrayList<MyWebview> serverWebs;
	int webIndex;
	ViewFlipper webpages;
	ImageView imgNext, imgPrev, imgHome, imgRefresh, imgNew;
	WebAdapter webAdapter;
	RelativeLayout webControl, webtools_center;
	Button btnNewpage;
	InputMethodManager imm;
	ProgressBar loadProgress;

	//upload related
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
	private final static int FILECHOOSER_RESULTCODE = 1001;

	//bookmark and history
	AlertDialog m_sourceDialog = null;
	ArrayList<TitleUrl> mHistory = new ArrayList<TitleUrl>();
	ArrayList<TitleUrl> mBookMark = new ArrayList<TitleUrl>();
	boolean historyChanged, bookmarkChanged;
	ImageView imgAddFavo, imgGo;
	
	//ad
	static boolean mAdAvailable;
	static {
	       try {
	    	   wrapAdView.checkAvailable();
	    	   mAdAvailable = true;
	       } catch (Throwable t) {
	    	   mAdAvailable = false;
	       }
	   }
	wrapAdView adview;
	LinearLayout adContainer;
	DisplayMetrics dm;
	
	//download related
	String downloadPath;
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
	
class MyWebview extends WebView {
	public String pageSource = getString(R.string.not_avaiable);

	wrapWebSettings webSettings;
	
	class MyJavaScriptInterface
	{
	    @SuppressWarnings("unused")
	    public void processHTML(String html)
	    {
	    	pageSource = html;//to get page source, part 1
	    }
	}

    void setTextSize(String url) {
		if (dm.density < 1) {//the text size on home page should not be so big, otherwise looks strange?
			webSettings.setTextSize(TextSize.SMALLER);
		}
		else if (dm.density == 1.0) {
			webSettings.setTextSize(TextSize.NORMAL);
		}
		else {
			webSettings.setTextSize(TextSize.LARGER);
		}
    }
    
	public MyWebview(Context context) {
		super(context);
		
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);//no white blank on the right of webview
        //setScrollbarFadingEnabled(true);//hide scroll bar when not scroll. from API5, not work on cupcake.
        
        webSettings = new wrapWebSettings(getSettings());
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(showZoomControl.isChecked());
        webSettings.setUseWideViewPort(true);//otherwise can't scroll horizontal in some webpage, such as qiupu.
        webSettings.setPluginsEnabled(true);
        webSettings.setLoadWithOverviewMode(true);//loads the WebView completely zoomed out. fit for hao123, but not fit for homepage. from API7
        //webSettings.setDefaultZoom(ZoomDensity.MEDIUM);//start from API7
        webSettings.setAppCacheEnabled(true);//API7
        webSettings.setDomStorageEnabled(true);//API7, enable gmail
        webSettings.setDatabaseEnabled(true);//API5
        webSettings.setDatabasePath(getDir("databases", MODE_PRIVATE).getPath());//API5
        webSettings.setGeolocationEnabled(true);//API5
        webSettings.setGeolocationDatabasePath(getDir("databases", MODE_PRIVATE).getPath());//API5

        
        registerForContextMenu(this);

        addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");//to get page source, part 2
        
        setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent arg1) {//just close webcontrol page if it is open.
	        	webControl.setVisibility(View.INVISIBLE);
	        	view.requestFocusFromTouch();
				return false;
			}
        });

        setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String ua, String contentDisposition,
					String mimetype, long contentLength) {
				startDownload(url);
			}
        });
        
        setWebChromeClient(new WebChromeClient() {
        	@Override
        	public void onProgressChanged(WebView view, int progress) {
        		loadProgress.setProgress(progress);
        		if (progress == 100) loadProgress.setVisibility(View.INVISIBLE);
        	}

        	// For Android 3.0+
            public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType ) 
            {
        		if (null == mUploadMessage) mUploadMessage = new wrapValueCallback();
            	mUploadMessage.mInstance = uploadMsg;  
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);  
                i.addCategory(Intent.CATEGORY_OPENABLE);  
                i.setType("*/*");  
                startActivityForResult( Intent.createChooser( i, getString(R.string.select_file)), FILECHOOSER_RESULTCODE );
            }

            // For Android < 3.0
            public void openFileChooser( ValueCallback<Uri> uploadMsg ) 
            {
                openFileChooser( uploadMsg, "" );
            }
            
	        /*@Override
	        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
	            callback.invoke(origin, true, false);
	        }//I don't know how to reflect a Interface, so it will carsh on cupcake*/
		});
        
		setWebViewClient(new WebViewClient() {
			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
		        handler.proceed();//accept ssl certification when never needed.
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				imm.hideSoftInputFromWindow(getWindowToken(), 0);//close soft keyboard
        		loadProgress.setVisibility(View.VISIBLE);
        		webAddress.setText(url);
        		imgRefresh.setImageResource(R.drawable.stop);
        		
				setTextSize(url);
				
				if (!paid && mAdAvailable) adview.loadAd();

				super.onPageStarted(view, url, favicon);
			}
			 
			@Override
			public void onPageFinished(WebView view, String url) {
        		loadProgress.setVisibility(View.INVISIBLE);//hide progressbar anyway
        		imgRefresh.setImageResource(R.drawable.refresh);
				webAdapter.notifyDataSetChanged();//update the page title in webList
				
				view.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);//use cache if avaiable for next load

				String title = view.getTitle();
				if (title == null) title = url;
				
        		if (!android.os.Build.VERSION.RELEASE.equals("2.3.3")) {//it will cause webkit crash on 2.3.3
        			if (url.equals(BLANK_PAGE) || title.equals(getString(R.string.browser_name)) && (!debug)) 
        				pageSource = "<head><title>Easy Browser</title></head><body>welcome!</body>";
        			else //not work for wml. some webkit even not parse wml.
        				loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");//to get page source, part 3
        		}
				
                webControl.setVisibility(View.INVISIBLE);

        		if (!url.equals(BLANK_PAGE)) {
        			if(title.equals(getString(R.string.browser_name)))//if title and url not sync, then sync it.
        				webAddress.setText(BLANK_PAGE);
        			else {//handle the bookmark/history after load new page
        				setTextSize(url);
        				
            			String site = "";
            			String[] tmp = url.split("/");
            			if (tmp.length > 2) site = tmp[2];//if url is http://m.baidu.com, then url.split("/")[2] is m.baidu.com
            			else site = tmp[0];
                		for (int i = mHistory.size()-1; i >= 0; i--) {
                			if (mHistory.get(i).m_url.equals(url)) return;//record one url only once in the history list.
                			else if (mHistory.get(i).m_site.equals(site)) {
                				mHistory.remove(i);//only keep the latest history of the same site.
                				break;
                			}
                		}
                		
                		if (siteArray.indexOf(site) < 0) {
                			urlAdapter.add(site);//update the auto-complete edittext without duplicate
                			siteArray.add(site);//the adapter will always return 0 when get count or search, so we use an array to store the site.
                		}
                			
        				try {//try to open the png, if can't open, then need save
    						FileInputStream fis = openFileInput(site+".png");
    						try {fis.close();} 
    						catch (IOException e) {;}
    					} catch (FileNotFoundException e1) {
                			try {//save the Favicon
                				FileOutputStream fos = openFileOutput(site+".png", 0);
                				view.getFavicon().compress(Bitmap.CompressFormat.PNG, 90, fos); 
                		        fos.close();
                			} catch (Exception e) {
                				e.printStackTrace();
                			} 
    					}
                		
            			TitleUrl titleUrl = new TitleUrl(title, url, site);
                		mHistory.add(titleUrl);
                		historyChanged = true;
                		
                		if (mHistory.size() > 16) {//remove oldest history
                			site = mHistory.get(0).m_site;
                			mHistory.remove(0);//delete the first history if list larger than 16;
                			
                			boolean found = false;
                			for (int i = mHistory.size()-1; i >= 0; i--) {
                				if (mHistory.get(i).m_site.equals(site)) {
                					found = true;
                					break;
                				}
                			}
                			if (!found) {
                    			for (int i = mBookMark.size()-1; i >= 0; i--) {
                    				if (mBookMark.get(i).m_site.equals(site)) {
                    					found = true;
                    					break;
                    				}
                    			}
                				if (!found) deleteFile(mHistory.get(0).m_site + ".png");//delete the Favicon if not any reference 
                			}
                		}
            		}
        		}
			}         
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(view.getHitTestResult().getType() > 0)
			         return false;// From a user click, handle it by webview.
			    else {
					if (!url.startsWith("http")) {
						Uri uri = Uri.parse(url);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						return util.startActivity(intent, false, mContext);
					}
					else return startDownload(url);
			    }
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        final MyWebview wv = (MyWebview) localWeblist.get(position);

        if (convertView == null) {
            final LayoutInflater inflater = getLayoutInflater();
            convertView = inflater.inflate(R.layout.web_list, parent, false);
        }
    	if (position == webIndex) 
    		convertView.setBackgroundColor(0xAA222222);
    	else
    		convertView.setBackgroundColor(0xDD111111);

        final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.webicon);
        btnIcon.setImageBitmap(wv.getFavicon());
        
        TextView webname = (TextView) convertView.findViewById(R.id.webname);
        if ((wv.getTitle() != null) && (!wv.getTitle().equals("")))
        	webname.setText(wv.getTitle());
        else webname.setText(wv.getUrl());
        
        webname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				webControl.setVisibility(View.INVISIBLE);
				changePage(position);
			}
		});
        
        ImageView btnStop = (ImageView) convertView.findViewById(R.id.webclose);
        btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				serverWebs.get(webIndex).stopLoading();//remove current page, so stop loading at first
				if (webAdapter.getCount() > 1) {
					MyWebview tmp = (MyWebview) webpages.getChildAt(position);
					webAdapter.remove(tmp);
					webAdapter.notifyDataSetInvalidated();
					webpages.removeViewAt(position);
					tmp.destroy();
					imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), webAdapter.getCount(), 2, mContext));//show the changed page number
					if (webIndex == webAdapter.getCount()) webIndex = webAdapter.getCount()-1;
				}
				else {//return to home page if only one page when click close button
					webControl.setVisibility(View.INVISIBLE);
					loadPage(true);
					serverWebs.get(webIndex).clearHistory();
				}
				webAddress.setText(serverWebs.get(webIndex).getUrl());//refresh the display url
				webpages.getChildAt(webIndex).requestFocus();
			}
        });
        
        return convertView;
    }
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    if (requestCode == FILECHOOSER_RESULTCODE) {
        if ((null == mUploadMessage) || (null == mUploadMessage.mInstance)) return;
        Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
        if (mValueCallbackAvailable) mUploadMessage.onReceiveValue(result);
        mUploadMessage.mInstance = null;
    }
}

@Override
public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	super.onCreateContextMenu(menu, v, menuInfo);

    HitTestResult result = ((WebView)v).getHitTestResult();
    final String url = result.getExtra();

    MenuItem.OnMenuItemClickListener handler = new MenuItem.OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem item) {// do the menu action
    		switch (item.getItemId()) {
    		case 0://save image
    			startDownload(url, ".jpg");
    			break;
    		case 1://view image
    			serverWebs.get(webIndex).loadUrl(url);
    			break;
    		case 4://copy url
    			ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    			ClipMan.setText(url);
    			break;
    		case 5://share url
    			shareUrl(url);
    			break;
    		case 6://open in new tab
    			openNewPage(url);
    			break;
    		case 7://add bookmark
    			if (historyIndex > -1) addFavo(url, mHistory.get(historyIndex).m_title);
    			else addFavo(url, url);
    			break;
    		case 8://remove bookmark
    			removeFavo(item.getOrder()); 
    			break;
    		case 9://remove history
    			removeHistory(item.getOrder());
    			break;
    		}
    		return true;
        }
    };

    //set the header title to the image url
    menu.setHeaderTitle(result.getExtra());
    if (result.getType() == HitTestResult.IMAGE_TYPE ||
            result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
        // Menu options for an image.
        menu.add(0, 0, 0, R.string.save_img).setOnMenuItemClickListener(handler);
        menu.add(0, 1, 0, R.string.view_img).setOnMenuItemClickListener(handler);
        menu.add(0, 2, 0, R.string.share_img).setOnMenuItemClickListener(handler).setVisible(false);
        menu.add(0, 3, 0, R.string.set_wallpaper).setOnMenuItemClickListener(handler).setVisible(false);
    } else if (result.getType() == HitTestResult.ANCHOR_TYPE ||
            result.getType() == HitTestResult.SRC_ANCHOR_TYPE) {
        // Menu options for a hyperlink.
        menu.add(0, 4, 0, R.string.copy_url).setOnMenuItemClickListener(handler);
        menu.add(0, 5, 0, R.string.shareurl).setOnMenuItemClickListener(handler);
        menu.add(0, 6, 0, R.string.open_new).setOnMenuItemClickListener(handler);
        
        historyIndex = -1;
    	for (int i = mHistory.size()-1; i >= 0; i--) 
    		if (mHistory.get(i).m_url.equals(url)) {
    			historyIndex = i;
    			menu.add(0, 9, i, R.string.remove_history).setOnMenuItemClickListener(handler);
    			break;
    		}
    	
        boolean foundBookmark = false;
    	for (int i = mBookMark.size()-1; i >= 0; i--) 
    		if (mBookMark.get(i).m_url.equals(url)) {
    			foundBookmark = true;
                menu.add(0, 8, i, R.string.remove_bookmark).setOnMenuItemClickListener(handler);
    			break;
    		}
        if (!foundBookmark) 
        		menu.add(0, 7, 0, R.string.add_bookmark).setOnMenuItemClickListener(handler);
    } else if (url != null)
        menu.add(0, 6, 0, R.string.open_new).setOnMenuItemClickListener(handler);
}

boolean startDownload(String url) {
	return startDownload(url, "");
}

boolean startDownload(String url, String ext) {
	int posQ = url.indexOf("src=");
	if (posQ > 0) url = url.substring(posQ+4);//get src part
	url = url.replace("%2D", "-");
	url = url.replace("%5F", "_");
	url = url.replace("%3F", "?");
	url = url.replace("%3D", "=");
	url = url.replace("%2E", ".");
	url = url.replace("%2F", "/");
	url = url.replace("%3A", ":");// replace %3A%2F%2F to :// if any
	
	posQ = url.indexOf("?");
	if (posQ > 0) url = url.substring(0, posQ);//cut off post paras if any.

	String ss[] = url.split("/");
	String apkName = ss[ss.length-1].toLowerCase() ; //get download file name
	if (apkName.contains("=")) apkName = apkName.split("=")[apkName.split("=").length-1];
	if ((apkName.endsWith(".txt")) || (apkName.endsWith(".html")) || (apkName.endsWith(".htm"))) return false;//should not download txt and html file.
	
	if (!apkName.contains(".")) {
		if (!ext.equals("")) apkName = apkName + ext;
		else return false;//not a file
	}
	
	MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
	if (mimeTypeMap.hasExtension(mimeTypeMap.getFileExtensionFromUrl(apkName))) {//files need download
		if (downloadPath.startsWith(getFilesDir().getPath())) 
			Toast.makeText(mContext, R.string.sdcard_needed, Toast.LENGTH_LONG).show();
		
		Iterator iter = appstate.downloadState.entrySet().iterator();
		while (iter.hasNext()) {
			Entry entry = (Entry) iter.next();
			DownloadTask val = (DownloadTask) entry.getValue();
			if ((val != null) && val.apkName.equals(apkName)) {
				if (val.pauseDownload) val.pauseDownload = false;//resume download if it paused
				return true;//the file is downloading, not start a new download task.
			}
		}
		
    	Random random = new Random();
    	int id = random.nextInt() + 1000;
    	
		DownloadTask dltask = new DownloadTask();
		dltask.NOTIFICATION_ID = id;
		appstate.downloadState.put(id, dltask);
		dltask.execute(url, apkName);
		return true;
	}
	else return false;
}

class DownloadTask extends AsyncTask<String, Integer, String> {
	private String URL_str; //网络歌曲的路径
	private File download_file; //下载的文件
	private long total_read = 0; //已经下载文件的长度(以字节为单位)
	private int readLength = 0; //一次性下载的长度(以字节为单位)
	private long apk_length = 0; //音乐文件的长度(以字节为单位)
	private long skip_length = 0;//if found local file not download finished last time, need continue to download
	String apkName = ""; //下载的文件名
	int NOTIFICATION_ID;
	private Notification notification;
	private int oldProgress;
	boolean stopDownload = false;//true to stop download
	boolean pauseDownload = false;//true to pause download
	boolean downloadFailed = false;

	@Override
	protected void onPostExecute(String result) {
		if (result != null) {
			String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
			serverWebs.get(webIndex).loadData(header + result, "text/html", "UTF-8");
		}
	}
	
	@Override
	protected String doInBackground(String... params) {//download here
    	URL_str = params[0]; //get download url
    	if (URL_str.startsWith("file")) return URL_str;//not download local file 
    	apkName = params[1]; //get download file name
    	if (apkName.contains("%")) apkName = apkName.split("%")[apkName.split("%").length-1];//for some filename contain % will cause error
    	
    	notification = new Notification(android.R.drawable.stat_sys_download, getString(R.string.start_download), System.currentTimeMillis());   
		
    	Intent intent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);  
		notification.setLatestEventInfo(mContext, apkName, getString(R.string.start_download), contentIntent);
        nManager.notify(NOTIFICATION_ID, notification);
        
		intent.setAction(getPackageName() + ".downloadControl");//this intent is to pause/stop download
		intent.putExtra("id", NOTIFICATION_ID);
		intent.putExtra("name", apkName);
		intent.putExtra("url", URL_str);
		contentIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);//request_code will help to diff different thread
        notification.setLatestEventInfo(mContext, apkName, getString(R.string.downloading), contentIntent);
        
		MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
		String mimeType = mimeTypeMap.getMimeTypeFromExtension(mimeTypeMap.getFileExtensionFromUrl(apkName));
		
    	FileOutputStream fos = null; //文件输出流
    	InputStream is = null; //网络文件输入流
    	URL url = null;
    	try {
        	url = new URL(URL_str); //网络歌曲的url
        	HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); //打开网络连接
    		download_file = new File(downloadPath + apkName);
        	apk_length = httpConnection.getContentLength(); //file size need to download
    		if (download_file.length() == apk_length) {//found local file with same name and length, no need to download, just send intent to view it
            	String[] tmp = apkName.split("\\.");
    			intent.setAction(Intent.ACTION_VIEW);
    			intent.setDataAndType(Uri.fromFile(download_file), mimeType);
				util.startActivity(intent, false, mContext);
            	appstate.downloadState.remove(NOTIFICATION_ID);
        		nManager.cancel(NOTIFICATION_ID);
				return downloadPath + apkName;
    		}
    		else if (download_file.length() < apk_length) {//local file size < need to download, need continue to download
    			fos = new FileOutputStream(download_file, true);
    			skip_length = download_file.length();
    		}
    		else //need overwrite
    			fos = new FileOutputStream(download_file, false);
    		
	        notification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.notification_dialog);
	        notification.contentView.setProgressBar(R.id.progress_bar, 100, 0, false);
	        notification.contentView.setTextViewText(R.id.progress, "0%");
	        notification.contentView.setTextViewText(R.id.title, apkName);
	        nManager.notify(NOTIFICATION_ID, notification);
	        
        	total_read = 0; //初始化“已下载部分”的长度，此处应为0
        	is = httpConnection.getInputStream();

        	byte buf[] = new byte[10240]; //download buffer. is that ok for 10240?
        	readLength = 0; //一次性下载的长度
        	
        	oldProgress = 0;
        	//如果读取网络文件的数据流成功，且用户没有选择停止下载，则开始下载文件
        	while (readLength != -1 && !stopDownload) {
        		if (pauseDownload) {
        			continue;
        		}
        		
            	if((readLength = is.read(buf))>0){
            		if (skip_length == 0)
            			fos.write(buf, 0, readLength);
            		else if (skip_length < readLength) {
            			fos.write(buf, (int) skip_length, (int) (readLength - skip_length));
            			skip_length = 0;
            		}
            		else skip_length -= readLength;//just read and skip, not write if need skip
            		
                	total_read += readLength; //increase the download size
            	}

            	int progress = (int) ((total_read+0.0)/apk_length*100);
            	if (oldProgress != progress) {//the device will get no response if update too often
            		oldProgress = progress;
            		notification.contentView.setProgressBar(R.id.progress_bar, 100, progress, false);//update download progress
        	        notification.contentView.setTextViewText(R.id.progress, progress + "%");
            		nManager.notify(NOTIFICATION_ID, notification);
            	}
        	}
        	fos.close();
        	is.close();
        	httpConnection.disconnect();
        	
        	appstate.downloadState.remove(NOTIFICATION_ID);
        	if (stopDownload) {//stop download by user. clear notification
        		nManager.cancel(NOTIFICATION_ID);
        	}
        	else {//download success. change notification, start package manager to install package
            	notification.icon = android.R.drawable.stat_sys_download_done;

    			intent = new Intent();
    			intent.setAction(Intent.ACTION_VIEW);
    			intent.setDataAndType(Uri.fromFile(download_file), mimeType);
    	        contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);  
    	        notification.contentView.setOnClickPendingIntent(R.id.notification_dialog, contentIntent);
    	        notification.setLatestEventInfo(mContext, apkName, getString(R.string.download_finish), contentIntent);//click listener for download progress bar
    	        nManager.notify(NOTIFICATION_ID, notification);
    	        
    			Process p = Runtime.getRuntime().exec("chmod 644 " + download_file.getPath());//change file property, for on some device the property is wrong
    			p.waitFor();
    			
				if ((apkName.toLowerCase().endsWith("jpg")) || (apkName.toLowerCase().endsWith("png"))) {
					Intent intentAddPic = new Intent("simpleHome.action.PIC_ADDED");
					intentAddPic.putExtra("picFile", apkName);
	                sendBroadcast(intentAddPic);//add to picture list and enable change background by shake
				}
				else if (apkName.toLowerCase().endsWith("apk")) {
					PackageInfo pi = getPackageManager().getPackageArchiveInfo(downloadPath + apkName, 0);
					//PackageParser packageParser  =  PackageParser(downloadPath + apkName);
        			downloadAppID.add(new packageIDpair(pi.packageName, NOTIFICATION_ID, download_file));
				}

				util.startActivity(intent, false, mContext);//call system package manager to install app. it will not return result code, so not use startActivityForResult();
        	}
			
    	} catch (Exception e) {
    		e.printStackTrace();
    		downloadFailed = true;
    		notification.icon = android.R.drawable.stat_notify_error;
    		intent.putExtra("errorMsg", e.toString());
			contentIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);//request_code will help to diff different thread
    		notification.setLatestEventInfo(mContext, apkName, getString(R.string.download_fail), contentIntent);
    		nManager.notify(NOTIFICATION_ID, notification);
    		
	        if (download_file.length() == 0) download_file.delete();//delete empty file
    	}

    	return null;
	}

}


@Override
public boolean onMenuOpened(int featureId, Menu menu) {
    menuDialog.show();
    
    return false;// show system menu if return true.
}

@Override
public boolean onCreateOptionsMenu(Menu menu) {
	menu.add("menu");//must create one menu?
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

private void shareUrl(String text)
{
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");  
    intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
	intent.putExtra(Intent.EXTRA_TEXT, text);
    util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, mContext);	
}


@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mContext = this;
    
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    paid = sp.getBoolean("paid", false);
    debug = sp.getBoolean("debug", false);

	nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	downloadAppID = new ArrayList();
	appstate = ((MyApp) getApplicationContext());
	
	imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	
	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
	setContentView(R.layout.browser);

	
	snapView = (ImageView) getLayoutInflater().inflate(R.layout.snap_browser, null);
    snapDialog = new AlertDialog.Builder(this).
    		setView(snapView).
    		setTitle(R.string.browser_name).
    		setIcon(R.drawable.explorer).
    		setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {//share
				@Override
				public void onClick(DialogInterface dialog, int which) {
	        		try {
	        			String snap = downloadPath + "snap/snap.png";
	        			FileOutputStream fos = new FileOutputStream(snap);
	        			
	        	        bmp.compress(Bitmap.CompressFormat.PNG, 90, fos); 
	        	        fos.close();
	        			
	        	        Intent intent = new Intent(Intent.ACTION_SEND);
	        	        intent.setType("image/*");  
	        	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share); 
	        			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(snap)));
	        	        util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, mContext);
	        		}
	        		catch (Exception e) {
	        			Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
	        		}
				}
			}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {//cancel
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).create();

	
	aboutView = getLayoutInflater().inflate(R.layout.about_browser, null);
    TextView mailTo = (TextView) aboutView.findViewById(R.id.mailto);
	if (getPackageName().equals("easy.browser")) 
		mailTo.setText(Html.fromHtml("<u>"+ getString(R.string.browser_author) +"</u>"));
	else
		mailTo.setText(Html.fromHtml("<u>"+ getString(R.string.author) +"</u>"));
    mailTo.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(Intent.ACTION_SENDTO);
			if (getPackageName().equals("easy.browser")) 
				intent.setData(Uri.fromParts("mailto", getString(R.string.browser_author), null));
			else
				intent.setData(Uri.fromParts("mailto", getString(R.string.author), null));
			util.startActivity(intent, true, getBaseContext());
		}
	});

	showZoomControl = (CheckBox) aboutView.findViewById(R.id.show_zoom);
	showZoomControl.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			serverWebs.get(webIndex).getSettings().setBuiltInZoomControls(showZoomControl.isChecked());
		}
	});

	btnFullScreen = (RadioButton) aboutView.findViewById(R.id.radio_fullscreen);
		
	//menu icon
    int[] menu_image_array = { R.drawable.explorer, R.drawable.capture,	R.drawable.copy, 
    		R.drawable.downloads, R.drawable.share, R.drawable.about };
    //menu text
    String[] menu_name_array = { getString(R.string.source), getString(R.string.snap), getString(R.string.copy), 
    		getString(R.string.downloads), getString(R.string.shareurl), getString(R.string.help) };
    
    //create AlertDialog
	menuView = View.inflate(this, R.layout.grid_menu, null);
    menuDialog = new AlertDialog.Builder(this).create();
    menuDialog.setView(menuView);
    WindowManager.LayoutParams params = menuDialog.getWindow().getAttributes();
    //240 for 1024h, 140 for 800h, 70 for 480h, to show menu dialog in correct position
    dm = new DisplayMetrics();
	getWindowManager().getDefaultDisplay().getMetrics(dm);
    if (dm.heightPixels <= 480) params.y = 70;
    else if (dm.heightPixels <= 800) params.y = 140;
    else params.y = 240;
    menuDialog.getWindow().setAttributes(params);
    
    menuDialog.setCanceledOnTouchOutside(true);
    
    menuDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_MENU) dialog.dismiss();
			return false;
		}
    	
    });

	m_sourceDialog = new AlertDialog.Builder(this).
    		setTitle(R.string.browser_name).
    		setIcon(R.drawable.explorer).
    		setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {//share
				@Override
				public void onClick(DialogInterface dialog, int which) {
	        		Intent intent = new Intent(Intent.ACTION_SENDTO);
	        		intent.setData(Uri.fromParts("mailto", "", null));
	        		intent.putExtra(Intent.EXTRA_TEXT, serverWebs.get(webIndex).pageSource);
	        		intent.putExtra(Intent.EXTRA_SUBJECT, serverWebs.get(webIndex).getTitle());
	        		if (!util.startActivity(intent, false, getBaseContext()))
						shareUrl(serverWebs.get(webIndex).pageSource);
				}
			}).
    		setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {//cancel
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).
    		create();

    menuGrid = (GridView) menuView.findViewById(R.id.gridview);
    menuGrid.setAdapter(getMenuAdapter(menu_name_array, menu_image_array));
    menuGrid.setOnItemClickListener(new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            switch (arg2) {
        	case 0://view page source
       	    	m_sourceDialog.setMessage(serverWebs.get(webIndex).pageSource);
       	    	m_sourceDialog.show();
        		break;
        	case 1://view snap
    			if (btnFullScreen.isChecked()) {
					webpages.destroyDrawingCache();//the snap will not refresh if not destroy cache
    				webpages.setDrawingCacheEnabled(true);
    				bmp = webpages.getDrawingCache();
    			}
    			else {
        			Picture pic = serverWebs.get(webIndex).capturePicture();

    				bmp = Bitmap.createBitmap(
    						pic.getWidth(), 
    						pic.getHeight(), 
    						Bitmap.Config.ARGB_8888);//the size of the web page may be very large. 
    			
        			Canvas canvas = new Canvas(bmp); 
        	        pic.draw(canvas);
    			}
        		snapView.setImageBitmap(bmp);
        		snapDialog.show();
        		
        		break;
        	case 2://copy
        		try {
            		if (Integer.decode(android.os.Build.VERSION.SDK) > 10) 
            			Toast.makeText(mContext, getString(R.string.copy_hint), Toast.LENGTH_LONG).show();
        		}
        	    catch (Exception e) {
        	    	e.printStackTrace();
        	    }
        		
        	    try {
        	        KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
        	        shiftPressEvent.dispatch(serverWebs.get(webIndex));
        	    }
        	    catch (Exception e) {
        	    	e.printStackTrace();
        	    }
        	    break;
        	case 3://downloads
    			Intent intent = new Intent("com.estrongs.action.PICK_DIRECTORY");
    			intent.setData(Uri.parse("file:///sdcard/simpleHome/"));
    			if (!util.startActivity(intent, false, mContext)) {
    				if (downloadsDialog == null) 
    					downloadsDialog = new AlertDialog.Builder(mContext).
    						setMessage(getString(R.string.downloads_to) + downloadPath + getString(R.string.downloads_open)).
    						setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
				    				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.estrongs.android.pop"));
				    				util.startActivity(intent, true, getBaseContext());
								}
							}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							}).
    						create();
    				downloadsDialog.show();
    			}
        		break;
        	case 4://share url
        		shareUrl(serverWebs.get(webIndex).getTitle() + " " + serverWebs.get(webIndex).getUrl());
        		break;
        	case 5://about
        		if (aboutDialog == null) {
        			String title = getString(R.string.embed_browser_name);
        			if (getPackageName().equals("easy.browser")) 
        				title = getString(R.string.browser_name) + " " + util.getVersion(getBaseContext());
        			aboutDialog = new AlertDialog.Builder(mContext).
        					setTitle(title).
        					setIcon(R.drawable.explorer).
        					setView(aboutView).
        					setMessage(getString(R.string.browser_name) + getString(R.string.about_message) + "\n\n" + getString(R.string.about_dialog_notes)).
        					setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
        						@Override
        						public void onClick(DialogInterface dialog, int which) {
        						}
        					}).create();
        		}
        		showZoomControl.setChecked(serverWebs.get(webIndex).getSettings().getBuiltInZoomControls());
        		aboutDialog.show();
        		break;
            }
            menuDialog.dismiss();
        }
    });
    
    
    
    loadProgress = (ProgressBar) findViewById(R.id.loadprogress);
    
    imgAddFavo = (ImageView) findViewById(R.id.addfavorite);
    imgAddFavo.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
	        boolean foundBookmark = false;
	        String url = serverWebs.get(webIndex).getUrl();
	    	for (int i = mBookMark.size()-1; i >= 0; i--) 
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
			String url = webAddress.getText().toString();
			if (!url.equals(BLANK_PAGE)) {
				if (!url.contains("://")) url = "http://" + url;
				serverWebs.get(webIndex).loadUrl(url);
			}
		}
    });
    
    webAddress = (AutoCompleteTextView) findViewById(R.id.url);
    webAddress.bringToFront();
    webAddress.setOnItemClickListener(new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			serverWebs.get(webIndex).loadUrl("http://" + urlAdapter.getItem(position));
		}
    	
    });
    webAddress.setOnEditorActionListener(new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
			imgGo.performClick();
			return false;
		}
    });
	FileInputStream fi = null;
	try {
		fi = openFileInput("history");
		mHistory = readBookmark(fi);
		fi = openFileInput("bookmark");
		mBookMark = readBookmark(fi);		
	} catch (Exception e) {
		e.printStackTrace();
	}
	historyChanged = false;
	bookmarkChanged = false;

	siteArray = new ArrayList<String>();
	urlAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
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
	
	urlAdapter.sort(new stringCompatator());
	webAddress.setAdapter(urlAdapter);


	WebIconDatabase.getInstance().open(getDir("databases", MODE_PRIVATE).getPath());
    webIndex = 0;
    serverWebs = new ArrayList<MyWebview>();
    serverWebs.add(new MyWebview(this));
    webpages = (ViewFlipper) findViewById(R.id.webpages);
    webpages.addView(serverWebs.get(webIndex));
    
    
	webtools_center = (RelativeLayout) findViewById(R.id.webtools_center);
	
	imgNext = (ImageView) findViewById(R.id.next);
	imgNext.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (serverWebs.get(webIndex).canGoForward()) {
				WebBackForwardList wbfl = serverWebs.get(webIndex).copyBackForwardList();
				if (wbfl.getItemAtIndex(wbfl.getCurrentIndex()+1).getUrl().equals(BLANK_PAGE))
					loadPage(true);//goBack will show blank page at this time, so load the home page.
				else serverWebs.get(webIndex).goForward();
			}
		}
	});
	imgPrev = (ImageView) findViewById(R.id.prev);
	imgPrev.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (serverWebs.get(webIndex).canGoBack()) {
				WebBackForwardList wbfl = serverWebs.get(webIndex).copyBackForwardList();
				if (wbfl.getItemAtIndex(wbfl.getCurrentIndex()-1).getUrl().equals(BLANK_PAGE))
					loadPage(true);//goBack will show blank page at this time, so load the home page.
				else serverWebs.get(webIndex).goBack(); 
			}
		}
	});
	imgRefresh = (ImageView) findViewById(R.id.refresh);
	imgRefresh.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (loadProgress.getVisibility() == View.VISIBLE) {//webpage is loading then stop it
				serverWebs.get(webIndex).stopLoading();
				loadProgress.setVisibility(View.INVISIBLE);
			}
			else {//reload the webpage
				if (!webAddress.getText().toString().equals(BLANK_PAGE)) { 
					serverWebs.get(webIndex).getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);//not use cache when refresh
					serverWebs.get(webIndex).reload();
				}
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
	imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), 1, 2, mContext));
	imgNew.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (webControl.getVisibility() == View.INVISIBLE) {
				webAdapter.notifyDataSetInvalidated();
		        webControl.setVisibility(View.VISIBLE);
		        webControl.bringToFront();
			}
			else {
				webControl.setVisibility(View.INVISIBLE);
				webpages.getChildAt(webIndex).requestFocus();
			}
		}
	});
	
	//web control
	webControl = (RelativeLayout) findViewById(R.id.webcontrol);
	
	btnNewpage = (Button) findViewById(R.id.opennewpage);
	btnNewpage.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {//add a new page
			openNewPage("");
		}
	});
	//web list
	webAdapter = new WebAdapter(this, serverWebs);
	webList = (ListView) findViewById(R.id.weblist);
	webList.inflate(this, R.layout.web_list, null);
	webList.setFadingEdgeLength(0);//no shadow when scroll
	webList.setScrollingCacheEnabled(false);
	webList.setAdapter(webAdapter);
	
	
	downloadPath = util.preparePath(mContext);

	adContainer = (LinearLayout) findViewById(R.id.adContainer);
	setLayout();
	
	try {//there are a null pointer error reported for the if line below, hard to reproduce, maybe someone use instrument tool to test it. so just catch it.
		if (getIntent().getAction().equals(Intent.ACTION_MAIN))	loadPage(false);
		else serverWebs.get(webIndex).loadUrl(getIntent().getDataString());
	}
	catch (Exception e) {
		e.printStackTrace();
	}    

	//for package added
	IntentFilter filter = new IntentFilter();
	filter.addAction(Intent.ACTION_PACKAGE_ADDED);
	filter.addDataScheme("package");
	registerReceiver(packageReceiver, filter);
	
	filter = new IntentFilter("simpleHome.action.START_DOWNLOAD");
    registerReceiver(downloadReceiver, filter);
}

@Override 
protected void onDestroy() {
	unregisterReceiver(packageReceiver);
	unregisterReceiver(downloadReceiver);
	
	if (!paid && mAdAvailable) adview.destroy();
	
	super.onDestroy();
}

BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context arg0, Intent intent) {
		startDownload(intent.getStringExtra("url"));
	}
};

BroadcastReceiver packageReceiver = new BroadcastReceiver() {

	@Override
	public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getDataString().split(":")[1];//it always in the format of package:x.y.z
        	for (int i = 0; i < downloadAppID.size(); i++) {//cancel download notification if install succeed
        		if (downloadAppID.get(i).packageName.equals(packageName))
        		{
            		nManager.cancel(downloadAppID.get(i).notificationID);
            		try {
            			downloadAppID.get(i).downloadedfile.delete();
            		} catch(Exception e) {};
            		downloadAppID.remove(i);
            		break;
        		}
        	}
      	
        }
	}
};

void changePage(int position) {
	webIndex = position;
	while (webpages.getDisplayedChild() != webIndex) webpages.showNext();
	webAddress.setText(serverWebs.get(webIndex).getUrl());//refresh the display url
	webpages.getChildAt(webIndex).requestFocus();
}

@Override
protected void onNewIntent(Intent intent) {//open file from sdcard
	if (!intent.getAction().equals(Intent.ACTION_MAIN)) {
		Uri uri = intent.getData();
		if (uri == null) return;
		
		boolean found = false;
		for (int i = 0; i < serverWebs.size(); i++) {
			if (serverWebs.get(i).getUrl().equals(uri.toString())) {
				changePage(i);  //show correct page
				found = true;
				break;
			}
		}
		
		if (!found) openNewPage(intent.getDataString());
	}
	
	super.onNewIntent(intent); 
}

void removeFavo(final int order) {
	new AlertDialog.Builder(mContext).
	setTitle(R.string.remove_bookmark).
	setMessage(mBookMark.get(order).m_title).
	setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//deleteFile(mBookMark.get(ii).m_title + ".snap.png");//delete snap too
    		mBookMark.remove(order);
    		bookmarkChanged = true;
    		loadPage(false);
		}
	}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	}).show();
}

void removeHistory(final int order) {
	new AlertDialog.Builder(mContext).
	setTitle(R.string.remove_history).
	setMessage(mHistory.get(order).m_title).
	setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//deleteFile(mBookMark.get(ii).m_title + ".snap.png");//delete snap too
    		mHistory.remove(order);
    		historyChanged = true;
    		loadPage(false);
		}
	}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		}
	}).show();
}

private void addFavo(final String url, final String title) {
	if (url == null) {
		Toast.makeText(mContext, "null url", Toast.LENGTH_LONG).show();
		return;
	}
	
	//need user's confirm to add to bookmark
	new AlertDialog.Builder(mContext).
		setTitle(R.string.add_bookmark).
		setMessage(title).
		setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String site = "";
				String[] tmp = url.split("/");
				if (tmp.length >= 2) site = tmp[2];//if url is http://m.baidu.com, then url.split("/")[2] is m.baidu.com
				else site = tmp[0];
				
				TitleUrl titleUrl = new TitleUrl(title, url, site);
	    		mBookMark.add(titleUrl);
	    		loadPage(false);
	    		
	    		bookmarkChanged = true;
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();	
}


private void openNewPage(String url) {
	int maxPages = 6;//max count is 6 for free version.
	if (paid) maxPages = 9;//9 for paid version.
	if (webAdapter.getCount() == maxPages) {
		Toast.makeText(mContext, R.string.nomore_pages, Toast.LENGTH_LONG).show();
		if (!url.equals("")) serverWebs.get(webIndex).loadUrl(url);
	}
	else {
		webAdapter.add(new MyWebview(mContext));
		webAdapter.notifyDataSetInvalidated();
		webIndex = webAdapter.getCount() - 1;
        webpages.addView(webAdapter.getItem(webIndex));
        while (webpages.getDisplayedChild() != webIndex) webpages.showNext();
		webpages.getChildAt(webIndex).requestFocus();
		imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), webAdapter.getCount(), 2, mContext));
		
		if (url.equals(""))	loadPage(true);
		else serverWebs.get(webIndex).loadUrl(url);
	}
}

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if (event.getRepeatCount() == 0) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {//press Back key in webview will go backword.
			if(webControl.getVisibility() == View.VISIBLE) imgNew.performClick();//hide web control
			else if (webAddress.getText().toString().equals(BLANK_PAGE)) return super.onKeyDown(keyCode, event);//leave this activity if press back key in homepage
			else if (serverWebs.get(webIndex).canGoBack()) imgPrev.performClick();
			else return super.onKeyDown(keyCode, event);
			
			return true;
		}	
	}
	return super.onKeyDown(keyCode, event);
}

@Override
protected void onPause() {
	FileOutputStream fo;
	try {
		if (historyChanged) {
			fo = this.openFileOutput("history", 0);
			writeBookmark(fo, mHistory);
		}
		
		if (bookmarkChanged) {
			fo = this.openFileOutput("bookmark", 0);
			writeBookmark(fo, mBookMark);
		}
	} catch (Exception e) {
		e.printStackTrace();
	}

	super.onPause();
}

@Override
public void onConfigurationChanged(Configuration newConfig) {
	super.onConfigurationChanged(newConfig); //not restart activity each time screen orientation changes
	setLayout();
}

void setLayout() {
	getWindowManager().getDefaultDisplay().getMetrics(dm);
	
	int width = dm.widthPixels;
	if (width < 100) return;//can't work on so small screen.
	
    LayoutParams lp = webtools_center.getLayoutParams();
    if (width >= 320)
    	lp.width = width/2 + 30;//15
    else lp.width = width/2-15;
    
    if (!paid && mAdAvailable) {
    	if (adContainer.getChildCount() > 0) {
    		adContainer.removeViewAt(0);
    		adview.destroy();
    	}
    	
    	if (dm.widthPixels <= 702)
    		adview = new wrapAdView(this, 0, "a14f3f6bc126143");//AdSize.IAB_BANNER require 350*50 and return 350*50
		else if (dm.widthPixels < 1092) 
    		adview = new wrapAdView(this, 1, "a14f3f6bc126143");//AdSize.IAB_BANNER require 468*60 but return 702*90
    	else
    		adview = new wrapAdView(this, 2, "a14f3f6bc126143");//AdSize.IAB_LEADERBOARD require 728*90, return 1092*135
    	
    	adContainer.addView(adview.getInstance());
    	adview.loadAd();
    }
}

void loadPage(boolean notJudge) {
	if ((notJudge) || (serverWebs.get(webIndex).getUrl() == null) || (serverWebs.get(webIndex).getUrl().equals(BLANK_PAGE)))
		serverWebs.get(webIndex).loadDataWithBaseURL(BLANK_PAGE, homePage(), "text/html", "utf-8", BLANK_PAGE);
}

String homePage() {//three part, 1 is recommend, 2 is bookmark displayed by scaled image, 3 is history displayed by link
	String ret = "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">";
	ret += "<meta name=\"viewport\" content=\"width=device-width\">";
	ret += "<html>";
	ret += "<head>";
	ret += "<title>" + getString(R.string.browser_name) + "</title>";
	ret += "<link rel=\"stylesheet\" href=\"file:///android_asset/jquery.mobile-1.0.1.min.css\" />";
	ret += "<script src=\"file:///android_asset/jquery-1.7.1.min.js\"></script>";
	ret += "<script src=\"file:///android_asset/jquery.mobile-1.0.1.min.js\"></script>";
	ret += "</head>";
	ret += "<body>";

	ret += "<div data-role=\"collapsible\" data-collapsed=\"false\" data-theme=\"b\" data-content-theme=\"d\">";
	ret += "<h5>" + getString(R.string.top) + "</h5>";
	ret += "<ul type=\"disc\">";
	Locale locale = getBaseContext().getResources().getConfiguration().locale;
	if (locale.equals(Locale.CHINA) || locale.equals(Locale.CHINESE)) {
		ret += "<h5><li><a href=\"http://www.appchina.com\">AppChina应用汇</a></li></h5>";
		ret += "<h5><li><a href=\"http://weibo.com/\">新浪微博</a></li></h5>";
		ret += "<h5><li><a href=\"http://m.hao123.com/?z=2&type=android&tn=diandianhome\">好123</a></li></h5>";
		ret += "<h5><li><a href=\"http://www.baidu.com/\">百度</a></li></h5>";
		ret += "<h5><li><a href=\"http://www.taobao.com/\">淘宝</a></li></h5>";
	}
	else {
		ret += "<h5><li><a href=\"http://www.amazon.com/\">Amazon</a></li></h5>";
		ret += "<h5><li><a href=\"http://www.apple.com/\">Apple</a></li></h5>";
		//ret += "<h5><li><a href=\"http://www.facebook.com/\">Facebook</a></li></h5>";//need try with proxy at first
		ret += "<h5><li><a href=\"http://www.google.com/\">Google</a></li></h5>";
		ret += "<h5><li><a href=\"http://www.twitter.org/\">Twitter</a></li></h5>";
		ret += "<h5><li><a href=\"http://www.wikipedia.org/\">Wikipedia</a></li></h5>";
	}
	ret += "</ul>";
	ret += "</div>";
	
	ret += "<div data-role=\"collapsible\" data-theme=\"b\" data-content-theme=\"d\"";
	if (mBookMark.size() > 0) ret += "data-collapsed=\"false\">";
	else ret += ">";
	ret += "<h5>" + getString(R.string.bookmark) + "</h5>";
	ret += "<dl type=\"disc\">";
	for (int i = 0; i < mBookMark.size(); i++) {
		String imgHref = "<li style=\"padding-left:25px; list-style-image:url(file://" + getFilesDir() + "/" + mBookMark.get(i).m_site + ".png)\">" 
				+ "<h5><a href=\"" + mBookMark.get(i).m_url + "\">";
		imgHref += mBookMark.get(i).m_title;
		imgHref += "</a></h5></li>";
		ret += imgHref;
	}
	ret += "</dl>";
	ret += "</div>";
	
	ret += "<div data-role=\"collapsible\" data-theme=\"b\" data-content-theme=\"d\"";
	if (mHistory.size() > 0) ret += "data-collapsed=\"false\">";
	else ret += ">";
	ret += "<h5>" + getString(R.string.history) + "</h5>";
	ret += "<dl type=\"disc\">";
	for (int i = 0; i < mHistory.size(); i++) {
		String imgHref = "<li style=\"padding-left:25px; list-style-image:url(file://" + getFilesDir() + "/" + mHistory.get(i).m_site + ".png)\">" 
				+ "<h5><a href=\"" + mHistory.get(i).m_url + "\">";
		imgHref += mHistory.get(i).m_title;
		imgHref += "</a></h5></li>";
		ret += imgHref;
	}
	ret += "</dl>";
	ret += "</div>";
	
	ret += "</body>";
	ret += "</html>";
	return ret;
}

class TitleUrl {
	String m_title;
	String m_url;
	String m_site;
	
	TitleUrl(String title, String url, String site) {
		if (title != null) m_title = title;
		else m_title = url;
		m_url = url;
		m_site = site;
	}
}

void writeBookmark(FileOutputStream fo, ArrayList<TitleUrl> bookmark) {
	try {
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
	} catch (Exception e) {}
}

ArrayList<TitleUrl> readBookmark(FileInputStream fi) 
{
	ArrayList<TitleUrl> bookmark = new ArrayList<TitleUrl>();
	ObjectInputStream ois = null;
	try {//read favorite or shortcut data
		ois = new ObjectInputStream(fi);
		TitleUrl tu;
		String title, url, site;
		while ((title = (String) ois.readObject()) != null) {
			url = (String) ois.readObject();
			site = (String) ois.readObject();
			tu = new TitleUrl(title, url, site);
			bookmark.add(tu);
		}
	} catch (EOFException e) {//only when read eof need send out msg.
		try {
			ois.close();
			fi.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	} catch (Exception e) {
		e.printStackTrace();
	}
	return bookmark;
}

}
