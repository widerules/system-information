package simple.home.jtbuaa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.Rect;
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
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
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

public class SimpleBrowser extends Activity {

	boolean paid;

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
    
	//browser related
	AutoCompleteTextView webAddress;
	ArrayAdapter<String> urlAdapter;
	ArrayList<MyWebview> serverWebs;
	int webIndex;
	ViewFlipper webpages;
	ImageView imgNext, imgPrev, imgHome, imgRefresh, imgNew;
	WebAdapter webAdapter;
	RelativeLayout webControl, webtools_center;
	TextView btnNewpage;
	InputMethodManager imm;
	ProgressBar loadProgress;

	AlertDialog m_sourceDialog;
	ArrayList<TitleUrl> mHistory = new ArrayList<TitleUrl>();
	ArrayList<TitleUrl> mBookMark = new ArrayList<TitleUrl>();
	boolean historyChanged, bookmarkChanged;
	ImageView imgAddFavo, imgGo;

	final String BLANK_PAGE = "about:blank";
	
	AdView adview;
	AdRequest adRequest = new AdRequest();

	//download related
	String downloadPath;
	NotificationManager nManager;
	ArrayList<packageIDpair> downloadAppID;
	MyApp appstate;
	
	ListView webList;
	Context mContext;

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
	public String title = "";
	public String pageSource;

	class MyJavaScriptInterface
	{
	    @SuppressWarnings("unused")
	    public void processHTML(String html)
	    {
	    	pageSource = html;//to get page source, part 1
	    }
	}
	
	public MyWebview(Context context) {
		super(context);
		title = getString(R.string.browser_name);
		
        setScrollBarStyle(0);
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(showZoomControl.isChecked());
        
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
        		
				if (!paid) adview.loadAd(adRequest);

				super.onPageStarted(view, url, favicon);
			}
			 
			@Override
			public void onPageFinished(WebView view, String url) {
        		loadProgress.setVisibility(View.INVISIBLE);//hide progressbar anyway
        		imgRefresh.setImageResource(R.drawable.refresh);

				webAdapter.notifyDataSetChanged();//what this for?
        		loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");//to get page source, part 3        		
				
                webControl.setVisibility(View.INVISIBLE);

        		if (!url.equals(BLANK_PAGE)) { 
        			String site = "";
        			String[] tmp = url.split("/");
        			if (tmp.length >= 2) site = tmp[2];//if url is http://m.baidu.com, then url.split("/")[2] is m.baidu.com
        			else site = tmp[0];
        			boolean found = false;
            		for (int i = mHistory.size()-1; i >= 0; i--) {
            			if (mHistory.get(i).m_url.equals(url)) return;//record one url only once in the history list.
            			else if (mHistory.get(i).m_site.equals(site)) {
            				found = true;
            				mHistory.remove(i);//only keep the latest history of the same site.
            				break;
            			}
            		}
            		
            		if (!found) urlAdapter.add(site);//update the auto-complete edittext, no duplicate
            			
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
            		
        			TitleUrl titleUrl = new TitleUrl(view.getTitle(), url, site);
            		mHistory.add(titleUrl);
            		historyChanged = true;
            		
            		if (mHistory.size() > 16) {//remove oldest history
            			site = mHistory.get(0).m_site;
            			mHistory.remove(0);//delete the first history if list larger than 16;
            			
            			found = false;
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
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (!url.startsWith("http")) {
					Uri uri = Uri.parse(url);
					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					intent.addCategory(Intent.CATEGORY_BROWSABLE);
					return util.startActivity(intent, false, mContext);
				}
				else return startDownload(url);
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

        final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.webicon);
        btnIcon.setImageBitmap(wv.getFavicon());
        
        TextView webname = (TextView) convertView.findViewById(R.id.webname);
        if ((wv.getTitle() != null) && (!wv.getTitle().equals("")))
        	webname.setText(wv.getTitle());
        else webname.setText(wv.title);
        
        webname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				webControl.setVisibility(View.INVISIBLE);
				webIndex = position;
				while (webpages.getDisplayedChild() != webIndex) webpages.showNext();
				webAddress.setText(serverWebs.get(webIndex).getUrl());//refresh the display url
				webpages.getChildAt(webIndex).requestFocus();
			}
		});
        
        ImageView btnStop = (ImageView) convertView.findViewById(R.id.webclose);
        btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				serverWebs.get(webIndex).stopLoading();//remove current page, so stop loading at first
				if (webAdapter.getCount() > 1) {
					((MyWebview) webpages.getChildAt(position)).destroy();
					webAdapter.remove((MyWebview) webpages.getChildAt(position));
					webpages.removeViewAt(position);
					imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), webAdapter.getCount(), 0, mContext));//show the changed page number
					if (webIndex == webAdapter.getCount()) webIndex = webAdapter.getCount()-1;
				}
				else {//return to home page if only one page when click close button
					webControl.setVisibility(View.INVISIBLE);
					loadPage(homePage());
					serverWebs.get(webIndex).title = getString(R.string.browser_name);
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
    		case 4://open in new tab
    			openNewPage(url, "");
    			break;
    		case 5://add bookmark
    			addFavo(url, url);
    			break;
    		case 6://share url
    			shareUrl(url);
    			break;
    		case 7://copy url
    			ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    			ClipMan.setText(url);
    			break;
    		}
    		return true;
        }
    };

    if (result.getType() == HitTestResult.IMAGE_TYPE ||
            result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
        // Menu options for an image.
        //set the header title to the image url
        menu.setHeaderTitle(result.getExtra());
        menu.add(0, 0, 0, R.string.save_img).setOnMenuItemClickListener(handler);
        menu.add(0, 1, 0, R.string.view_img).setOnMenuItemClickListener(handler);
        menu.add(0, 2, 0, R.string.share_img).setOnMenuItemClickListener(handler).setVisible(false);
        menu.add(0, 3, 0, R.string.set_wallpaper).setOnMenuItemClickListener(handler).setVisible(false);
    } else if (result.getType() == HitTestResult.ANCHOR_TYPE ||
            result.getType() == HitTestResult.SRC_ANCHOR_TYPE) {
        // Menu options for a hyperlink.
        //set the header title to the link url
        menu.setHeaderTitle(result.getExtra());
        menu.add(0, 4, 0, R.string.open_new).setOnMenuItemClickListener(handler);
        menu.add(0, 5, 0, R.string.add_bookmark).setOnMenuItemClickListener(handler);
        menu.add(0, 6, 0, R.string.shareurl).setOnMenuItemClickListener(handler);
        menu.add(0, 7, 0, R.string.copy_url).setOnMenuItemClickListener(handler);
    }
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
				if (val.pauseDownload) val.pauseDownload = false;
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
        
		intent.setAction("simple.home.jtbuaa.downloadControl");//this intent is to pause/stop download
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
    paid = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("paid", false);

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
	    				webpages.destroyDrawingCache();
	        			
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
			}).
    		setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {//cancel
				@Override
				public void onClick(DialogInterface dialog, int which) {
    				webpages.destroyDrawingCache();//the snap will not refresh if not destroy cache
				}
			}).setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
    				webpages.destroyDrawingCache();
				}
			}).
    		create();

	
	aboutView = getLayoutInflater().inflate(R.layout.about_browser, null);
    TextView mailTo = (TextView) aboutView.findViewById(R.id.mailto);
    mailTo.setText(Html.fromHtml("<u>"+ getString(R.string.author) +"</u>"));
    mailTo.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.author), null));
			util.startActivity(intent, true, getBaseContext());
		}
	});

    TextView downloads = (TextView) aboutView.findViewById(R.id.downloads);
    downloads.setText(Html.fromHtml("<u>"+ getString(R.string.downloads) +"</u>"));
    downloads.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			Intent intent = new Intent("com.estrongs.action.PICK_DIRECTORY");
			intent.setData(Uri.parse("file:///sdcard/simpleHome/"));
			if (!util.startActivity(intent, false, getBaseContext())) {
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.estrongs.android.pop"));
				util.startActivity(intent, true, getBaseContext());
			}
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
	
	DisplayMetrics dm  = new DisplayMetrics();
	getWindowManager().getDefaultDisplay().getMetrics(dm);
	
	//menu icon
    int[] menu_image_array = { R.drawable.explorer, R.drawable.capture, 
    		R.drawable.share, R.drawable.favorites_add, 
    		R.drawable.copy, R.drawable.about };
    //menu text
    String[] menu_name_array = { getString(R.string.source), getString(R.string.snap), 
    		getString(R.string.shareurl), getString(R.string.history_bookmark), 
    		getString(R.string.copy), getString(R.string.help) };
    
    //create AlertDialog
	menuView = View.inflate(this, R.layout.grid_menu, null);
    menuDialog = new AlertDialog.Builder(this).create();
    menuDialog.setView(menuView);
    WindowManager.LayoutParams params = menuDialog.getWindow().getAttributes();
    //240 for 1024h, 140 for 800h, 70 for 480h, to show menu dialog in correct position
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
	        		util.startActivity(intent, true, getBaseContext());
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
        	case 2://share url
        		shareUrl(serverWebs.get(webIndex).getTitle() + " " + serverWebs.get(webIndex).getUrl());
        		break;
        	case 3://history/bookmark
        		Intent intent = new Intent("simple.home.jtbuaa.bookmark");
        		intent.setClassName(getPackageName(), getPackageName()+".BookmarkEditor");
        		util.startActivity(intent, false, getBaseContext());
        		break;
        	case 4://copy
        	    try {
        	        KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
        	        shiftPressEvent.dispatch(serverWebs.get(webIndex));
        	    }
        	    catch (Exception e) {
        	    	e.printStackTrace();
        	    }
        	    break;
        	case 5://about
        		if (aboutDialog == null) {
        			aboutDialog = new AlertDialog.Builder(mContext).
        					setTitle(getString(R.string.browser_name) + " " + util.getVersion(getBaseContext())).
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
    
    
    
    adview = (AdView) findViewById(R.id.adView);
    if (paid) {
    	LayoutParams lp = adview.getLayoutParams();
    	lp.height = 0;
    }

    loadProgress = (ProgressBar) findViewById(R.id.loadprogress);
    
    imgAddFavo = (ImageView) findViewById(R.id.addfavorite);
    imgAddFavo.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			addFavo(serverWebs.get(webIndex).getUrl(), serverWebs.get(webIndex).getTitle());
		}
    });
    
    imgGo = (ImageView) findViewById(R.id.go);
    imgGo.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (!webAddress.getText().toString().equals(BLANK_PAGE)) 
				serverWebs.get(webIndex).loadUrl(webAddress.getText().toString());
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
			String url = webAddress.getText().toString();
			if (!url.startsWith("http") && !url.startsWith("file")) url = "http://" + url;
			serverWebs.get(webIndex).loadUrl(url);
			return false;
		}
    });
    
    WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
    webIndex = 0;
    serverWebs = new ArrayList<MyWebview>();
    serverWebs.add(new MyWebview(this));
    webpages = (ViewFlipper) findViewById(R.id.webpages);
    webpages.addView(serverWebs.get(webIndex));
    
	webtools_center = (RelativeLayout) findViewById(R.id.webtools_center);
	android.view.ViewGroup.LayoutParams lp = webtools_center.getLayoutParams();
	lp.width = dm.widthPixels/2 + 40;
	
	imgNext = (ImageView) findViewById(R.id.next);
	imgNext.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (serverWebs.get(webIndex).canGoForward()) {
				WebBackForwardList wbfl = serverWebs.get(webIndex).copyBackForwardList();
				if (wbfl.getItemAtIndex(wbfl.getCurrentIndex()+1).getUrl().equals(BLANK_PAGE))
					loadPage(homePage());//goBack will show blank page at this time, so load the home page.
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
					loadPage(homePage());//goBack will show blank page at this time, so load the home page.
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
				if (!webAddress.getText().toString().equals(BLANK_PAGE)) 
					serverWebs.get(webIndex).reload();
				else loadPage(homePage());
			}
		}
	});
	imgHome = (ImageView) findViewById(R.id.home);
	imgHome.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			loadPage(homePage());
		}
	});
	imgNew = (ImageView) findViewById(R.id.newpage);
	imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), 1, 0, mContext));
	imgNew.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (webControl.getVisibility() == View.INVISIBLE) {
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
	
	btnNewpage = (TextView) findViewById(R.id.opennewpage);
	btnNewpage.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {//add a new page
			openNewPage(homePage());
		}
	});
	//web list
	webAdapter = new WebAdapter(this, serverWebs);
	webList = (ListView) findViewById(R.id.weblist);
	webList.inflate(this, R.layout.web_list, null);
	webList.setFadingEdgeLength(0);//no shadow when scroll
	webList.setScrollingCacheEnabled(false);
	webList.setAdapter(webAdapter);

	
	downloadPath = util.preparePath(getFilesDir().getPath());

	setLayout();
	
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
	
	super.onDestroy();
}

BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context arg0, Intent intent) {
		startDownload(intent.getStringExtra("url"));
	}
};

private void loadNewPage(String url, boolean update) {
	if (!update)//only open new page if not update from bookmark/history
		if ((!serverWebs.get(webIndex).title.equals(getString(R.string.browser_name))) || serverWebs.get(webIndex).canGoBack()) 
		btnNewpage.performClick();//open the url in a new page if current page is not the main page
	serverWebs.get(webIndex).loadUrl(url);
	serverWebs.get(webIndex).title = url; 
	serverWebs.get(webIndex).requestFocus();
}

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

@Override
protected void onNewIntent(Intent intent) {//go back to home if press Home key.
	if (intent.getAction().equals(Intent.ACTION_VIEW)) //view webpages
		loadNewPage(intent.getDataString(), intent.getBooleanExtra("update", false));
	super.onNewIntent(intent); 
}

private void addFavo(final String url, final String title) {
	if (url == null) {
		Toast.makeText(mContext, "null url", Toast.LENGTH_LONG).show();
		return;
	}
	
	for (int i = mBookMark.size()-1; i >= 0; i--) 
		if (mBookMark.get(i).m_url.equals(url)) {//ask user whether to delete the bookmark if already exist.
			final int ii = i;
			new AlertDialog.Builder(mContext).
			setTitle(R.string.remove_bookmark).
			setMessage(title).
			setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//deleteFile(mBookMark.get(ii).m_title + ".snap.png");//delete snap too
		    		mBookMark.remove(ii);
		    		bookmarkChanged = true;
				}
			}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			}).show();
			return;//no need to add it again if always in bookmark
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
	    		
	    		bookmarkChanged = true;
			}
		}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();	
}


private void openNewPage(String data) {
	openNewPage("", data);
}

private void openNewPage(String url, String data) {
	if (webAdapter.getCount() == 9) //max count is 9.
		Toast.makeText(mContext, R.string.nomore_pages, Toast.LENGTH_LONG).show();
	else {
		webAdapter.add(new MyWebview(mContext));
		webIndex = webAdapter.getCount() - 1;
		if (url.equals(""))	loadPage(data);
		else serverWebs.get(webIndex).loadUrl(url);
        webpages.addView(webAdapter.getItem(webIndex));
        while (webpages.getDisplayedChild() != webIndex) webpages.showNext();
		webpages.getChildAt(webIndex).requestFocus();
		imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), webAdapter.getCount(), 0, mContext));
	}
}

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if (event.getRepeatCount() == 0) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {//press Back key in webview will go backword.
			if(webControl.getVisibility() == View.VISIBLE) imgNew.performClick();//hide web control
			else if (serverWebs.get(webIndex).canGoBack()) imgPrev.performClick();
			else return super.onKeyDown(keyCode, event);
			
			return true;
		}	
	}
	return super.onKeyDown(keyCode, event);
}

@Override
protected void onResume() {
	int hCount = mHistory.size();
	int bCount = mBookMark.size();
	FileInputStream fi = null;
	try {
		fi = openFileInput("history");
		mHistory = util.readBookmark(fi);
		fi = openFileInput("bookmark");
		mBookMark = util.readBookmark(fi);		
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	historyChanged = false;
	bookmarkChanged = false;

	//once set the adapter, the item count of the adapter is always 0, it is bug of Android?
	//we can only set the adapter to the auto-complete edittext once, otherwise there will be duplicate items in it?
	if (webAddress.getAdapter() == null) {
		urlAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
		for (int i = 0; i < mHistory.size(); i++) 
			if (urlAdapter.getPosition(mHistory.get(i).m_site) < 0) urlAdapter.add(mHistory.get(i).m_site);
		for (int i = 0; i < mBookMark.size(); i++) 
			if (urlAdapter.getPosition(mBookMark.get(i).m_site) < 0) urlAdapter.add(mBookMark.get(i).m_site);
		urlAdapter.sort(new stringCompatator());
		
		webAddress.setAdapter(urlAdapter);

		try {//there are a null pointer error reported for the if line below, hard to reproduce, maybe someone use instrument tool to test it. so just catch it.
			if (getIntent().getAction().equals(Intent.ACTION_VIEW)) 
				//open the url from intent in a new page if the old page is under reading.
				loadNewPage(getIntent().getDataString(), getIntent().getBooleanExtra("update", false));
			else if ((serverWebs.get(webIndex).getUrl() == null) || (serverWebs.get(webIndex).getUrl().equals(BLANK_PAGE))) loadPage(homePage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	else if ((serverWebs.get(webIndex).getUrl() == null) || (serverWebs.get(webIndex).getUrl().equals(BLANK_PAGE))) 
		if ((hCount != mHistory.size()) || (bCount != mBookMark.size())) loadPage(homePage());//reload home page if history/bookmark changed.
		
	
	super.onResume();
}

@Override
protected void onPause() {
	FileOutputStream fo;
	try {
		if (historyChanged) {
			fo = this.openFileOutput("history", 0);
			util.writeBookmark(fo, mHistory);
		}
		
		if (bookmarkChanged) {
			fo = this.openFileOutput("bookmark", 0);
			util.writeBookmark(fo, mBookMark);
		}
	} catch (FileNotFoundException e) {
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
	DisplayMetrics dm = new DisplayMetrics();  
	getWindowManager().getDefaultDisplay().getMetrics(dm);
	
	int width = dm.widthPixels;
	if (width < 100) return;//can't work on so small screen.
	
    LayoutParams lp = webtools_center.getLayoutParams();
    if (width >= 320)
    	lp.width = width/2 + 30;//15
    else lp.width = width/2-15;
}

void loadPage(String data) {
    serverWebs.get(webIndex).loadDataWithBaseURL("", data, "text/html", "utf-8", "");
}

String homePage() {//three part, 1 is recommend, 2 is bookmark displayed by scaled image, 3 is history displayed by link
	String ret = "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">";
	ret += "<html>";
	ret += "<title>" + getString(R.string.browser_name) + "</title>";
	ret += "<body>";
	
	ret += "<p><h3><a href=\"http://www.appchina.com\">AppChina应用汇</a></h3></p>";
	ret += "<p><h3><a href=\"http://www.baidu.com/\">百度</a></h3></p>";
	ret += "<p><h3><a href=\"http://www.google.com/\">Google</a></h3></p>";
	ret += "<p><h3><a href=\"http://m.hao123.com/?z=2&type=android&tn=diandianhome\">好123</a></h3></p>";
	
	ret += "<h3><p>" + getString(R.string.bookmark) + "</p></h3>";
	ret += "<dl type=\"disc\">";
	for (int i = 0; i < mBookMark.size(); i++) {
		String imgHref = "<li style=\"padding-left:25px; list-style-image:url(file://" + getFilesDir() + "/" + mBookMark.get(i).m_site + ".png)\">" 
				+ "<h5><a href=\"" + mBookMark.get(i).m_url + "\">";
		imgHref += mBookMark.get(i).m_title;
		imgHref += "</a></h5></li>";
		ret += imgHref;
	}
	ret += "</dl>";
	/*ret += "<table border=\"0\" width=\"100%\" cellpadding=\"10\">";//the effect of snap is not clear. so use list
	ret += "<tr>"; 
	for (int i = 0; i < mBookMark.size(); i++) {
		if ((i%2 == 0) && (i > 0)) ret += "</tr><tr>"; //three item per row
		String imgHref = "<td width=\"50%\" valign=\"top\">";
		String title = mBookMark.get(i).m_title;
		imgHref += "<p><pre>" + title.substring(0, Math.min(14, title.length())) + "</pre></p>";//control the display length of title not more than 15 character
		imgHref += "<p><a href=\"" + mBookMark.get(i).m_url + "\">";
		imgHref += "<img src=\"file://" + getFilesDir() + "/" + mBookMark.get(i).m_title + ".snap.png\"/>";
		imgHref += "</a></p>";
		imgHref += "</td>";
		ret += imgHref;
	}
	ret += "</tr></table>";*/
	
	ret += "<h3><p>" + getString(R.string.history) + "</p></h3>";
	ret += "<dl type=\"disc\">";
	for (int i = 0; i < mHistory.size(); i++) {
		String imgHref = "<li style=\"padding-left:25px; list-style-image:url(file://" + getFilesDir() + "/" + mHistory.get(i).m_site + ".png)\">" 
				+ "<h5><a href=\"" + mHistory.get(i).m_url + "\">";
		imgHref += mHistory.get(i).m_title;
		imgHref += "</a></h5></li>";
		ret += imgHref;
	}
	ret += "</dl>";
	
	ret += "</body>";
	ret += "</html>";
	return ret;
}

}
