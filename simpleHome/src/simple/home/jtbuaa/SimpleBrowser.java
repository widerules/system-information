package simple.home.jtbuaa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class SimpleBrowser extends Activity {

	//browser related
	AutoCompleteTextView webAddress;
	ArrayAdapter<String> urlAdapter;
	ArrayList<MyWebview> serverWebs;
	int webIndex;
	ViewFlipper webpages;
	ImageView imgNext, imgPrev, imgShare, imgRefresh, imgNew;
	WebAdapter webAdapter;
	RelativeLayout webControl, webtools_center;
	TextView btnNewpage;
	InputMethodManager imm;
	
	AlertDialog m_sourceDialog;
	ArrayList<TitleUrl> mHistory = new ArrayList<TitleUrl>();
	ArrayList<TitleUrl> mBookMark = new ArrayList<TitleUrl>();
	boolean historyChanged, bookmarkChanged;
	ImageView imgAddFavo;

	ProgressBar loadProgress;
	
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
	
	void hideWebControl() {
		if (webControl.getVisibility() == View.VISIBLE) imgNew.performClick();		
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
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setSaveFormData(true);
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
        webSettings.setSupportZoom(true);

        addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");//to get page source, part 2
        
        setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent arg1) {//just close webcontrol page if it is open.
	        	hideWebControl();
	        	view.requestFocusFromTouch();
				return false;
			}
        });

        setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				Log.d("===============", view.toString());
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
				imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);//close soft keyboard
        		loadProgress.setVisibility(View.VISIBLE);
        		webAddress.setText(url);
				super.onPageStarted(view, url, favicon);
			}
			 
			@Override
			public void onPageFinished(WebView view, String url) {
				webAdapter.notifyDataSetChanged();//what this for?
				//serverWeb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        		loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");//to get page source, part 3        		
				
        		if (!url.equals("file:///android_asset/online.html")) {
        			String site = "";
        			String[] tmp = url.split("/");
        			if (tmp.length >= 2) site = tmp[2];//if url is http://m.baidu.com, then url.split("/")[2] is m.baidu.com
        			else site = tmp[0];
            		for (int i = mHistory.size()-1; i >= 0; i--) 
            			if (mHistory.get(i).m_site.equals(site)) return;//record one site only once in the history list.
            		
        			TitleUrl titleUrl = new TitleUrl(view.getTitle(), url, site);
            		mHistory.add(titleUrl);
            		urlAdapter.add(site);
        			try {//save the Favicon
        				FileOutputStream fos = openFileOutput(site+".png", 0);
        				view.getFavicon().compress(Bitmap.CompressFormat.PNG, 90, fos); 
        		        fos.close();
        			} catch (Exception e) {
        				e.printStackTrace();
        			} 
            		
            		if (mHistory.size() > 16) {
            			deleteFile(mHistory.get(0).m_title + ".png");//delete the Favicon
            			mHistory.remove(0);//delete the first history if list larger than 16;
            		}
            		historyChanged = true;
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
				webpages.getChildAt(webIndex).requestFocus();
				adview.loadAd(adRequest);
			}
		});
        
        ImageView btnStop = (ImageView) convertView.findViewById(R.id.webclose);
        btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (webAdapter.getCount() > 1) {
					((MyWebview) webpages.getChildAt(position)).destroy();
					webAdapter.remove((MyWebview) webpages.getChildAt(position));
					webpages.removeViewAt(position);
					imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), webAdapter.getCount(), 0, mContext));
					if (webIndex == webAdapter.getCount()) webIndex = webAdapter.getCount()-1;
				}
				else {//return to home page if only one page when click close button
					webControl.setVisibility(View.INVISIBLE);
					serverWebs.get(webIndex).loadUrl("file:///android_asset/online.html");
					serverWebs.get(webIndex).title = getString(R.string.app_name);
					serverWebs.get(webIndex).clearHistory();
				}
				adview.loadAd(adRequest);
				webpages.getChildAt(webIndex).requestFocus();
			}
        });
        
        return convertView;
    }
}

boolean startDownload(String url) {
	if (!url.contains(".")) return false;//not a file
	
	int posQ = url.indexOf("?");
	if (posQ > 0) url = url.substring(0, posQ);//cut off post paras if any.
	
	String ss[] = url.split("/");
	String apkName = ss[ss.length-1].toLowerCase() ; //get download file name
	if (apkName.contains("=")) apkName = apkName.split("=")[apkName.split("=").length-1];
	if ((apkName.endsWith(".txt")) || (apkName.endsWith(".html")) || (apkName.endsWith(".htm"))) return false;//should not download txt and html file.
	
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
		if (result != null)
			serverWebs.get(webIndex).loadUrl("file:///android_asset/warning.html");
	}
	
	@Override
	protected String doInBackground(String... params) {//download here
    	URL_str = params[0]; //get download url
    	if (URL_str.startsWith("file")) return "local file";//not download local file 
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
				return "";
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
public boolean onCreateOptionsMenu(Menu menu) {
	super.onCreateOptionsMenu(menu);
	menu.add(0, 0, 0, R.string.history).setAlphabeticShortcut('H');
	menu.add(0, 1, 0, R.string.bookmark).setAlphabeticShortcut('B');
	menu.add(0, 2, 0, R.string.source).setAlphabeticShortcut('S');
	menu.add(0, 3, 0, R.string.snap).setAlphabeticShortcut('N');
	return true;
}

public boolean onOptionsItemSelected(MenuItem item){
	switch (item.getItemId()) {
	case 0://history
		Intent intent = new Intent("simple.home.jtbuaa.bookmark");
		intent.setClassName(getPackageName(), getPackageName()+".BookmarkEditor");
		intent.putExtra("filename", "history");
		util.startActivity(intent, false, getBaseContext());
		break;
	case 1://bookmark
   		intent = new Intent("simple.home.jtbuaa.bookmark");
   		intent.setClassName(getPackageName(), getPackageName()+".BookmarkEditor");
   		intent.putExtra("filename", "bookmark");
   		util.startActivity(intent, false, getBaseContext());        		
		break;
	case 2://view page source
		intent = new Intent(Intent.ACTION_SENDTO);
		intent.setData(Uri.fromParts("mailto", "", null));
		intent.putExtra(Intent.EXTRA_TEXT, serverWebs.get(webIndex).pageSource);
		intent.putExtra(Intent.EXTRA_SUBJECT, serverWebs.get(webIndex).getTitle());
		if (!util.startActivity(intent, false, getBaseContext())) {
	    	if (m_sourceDialog == null) {
				m_sourceDialog = new AlertDialog.Builder(this).
				setMessage(serverWebs.get(webIndex).pageSource).create();
	        }
	    	else m_sourceDialog.setMessage(serverWebs.get(webIndex).pageSource);
	    	m_sourceDialog.show();
		}
		break;
	case 3://snap
		try {
			String snap = downloadPath + "snap/snap.png";
			FileOutputStream fos = new FileOutputStream(snap); 
			Picture pic = serverWebs.get(webIndex).capturePicture();
			Bitmap bmp = Bitmap.createBitmap(serverWebs.get(webIndex).getWidth(), serverWebs.get(webIndex).getHeight(), Bitmap.Config.ARGB_8888);//the size of the web page may be very large. 
			Canvas canvas = new Canvas(bmp); 
	        pic.draw(canvas);
	        bmp.compress(Bitmap.CompressFormat.PNG, 90, fos); 
	        fos.close();
			
	        intent = new Intent(Intent.ACTION_SEND);
	        intent.setType("image/*");  
	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share); 
			intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(snap)));
	        util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, mContext);
		}
		catch (Exception e) {
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}
		break;
	}
	return true;
}

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    mContext = this;

	nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	downloadAppID = new ArrayList();
	appstate = ((MyApp) getApplicationContext());
	
	imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
	getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
	
	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
	setContentView(R.layout.browser);

    adview = (AdView) findViewById(R.id.adView);

    loadProgress = (ProgressBar) findViewById(R.id.loadprogress);
    
    imgAddFavo = (ImageView) findViewById(R.id.addfavorite);
    imgAddFavo.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			final String url = serverWebs.get(webIndex).getUrl(); 
			for (int i = mBookMark.size()-1; i >= 0; i--) 
				if (mBookMark.get(i).m_url.equals(url)) {//ask use whether to delete the bookmark if already exist.
					final int ii = i;
					new AlertDialog.Builder(mContext).
					setTitle(R.string.remove_bookmark).
					setMessage(url).
					setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
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
			
			//need use's confirm to add to bookmark
			new AlertDialog.Builder(mContext).
				setTitle(R.string.add_bookmark).
				setMessage(url).
				setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String site = "";
						String[] tmp = url.split("/");
						if (tmp.length >= 2) site = tmp[2];//if url is http://m.baidu.com, then url.split("/")[2] is m.baidu.com
						else site = tmp[0];
						
						String title = serverWebs.get(webIndex).getTitle();
						
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
    });
    
    webAddress = (AutoCompleteTextView) findViewById(R.id.url);
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
			if (!url.startsWith("http")) url = "http://" + url;
			serverWebs.get(webIndex).loadUrl(url);
			return false;
		}
    });
    urlAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
    webAddress.setAdapter(urlAdapter);
    
    WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
    webIndex = 0;
    serverWebs = new ArrayList<MyWebview>();
    serverWebs.add(new MyWebview(this));
    webpages = (ViewFlipper) findViewById(R.id.webpages);
    webpages.addView(serverWebs.get(webIndex));
    
	webtools_center = (RelativeLayout) findViewById(R.id.webtools_center);
	DisplayMetrics dm  = new DisplayMetrics();
	getWindowManager().getDefaultDisplay().getMetrics(dm);
	android.view.ViewGroup.LayoutParams lp = webtools_center.getLayoutParams();
	lp.width = dm.widthPixels/2 + 40;
	
	imgNext = (ImageView) findViewById(R.id.next);
	imgNext.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (serverWebs.get(webIndex).canGoForward()) serverWebs.get(webIndex).goForward();
			adview.loadAd(adRequest);
		}
	});
	imgPrev = (ImageView) findViewById(R.id.prev);
	imgPrev.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if (serverWebs.get(webIndex).canGoBack()) serverWebs.get(webIndex).goBack();
			adview.loadAd(adRequest);
		}
	});
	imgRefresh = (ImageView) findViewById(R.id.refresh);
	imgRefresh.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			serverWebs.get(webIndex).reload();
			adview.loadAd(adRequest);
		}
	});
	imgShare = (ImageView) findViewById(R.id.share);
	imgShare.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
	        Intent intent = new Intent(Intent.ACTION_SEND);
	        intent.setType("text/plain");  
	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
    		intent.putExtra(Intent.EXTRA_TEXT, serverWebs.get(webIndex).getTitle() + " " + serverWebs.get(webIndex).getUrl());
	        util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, mContext);
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
			if (webAdapter.getCount() == 9) //max count is 9.
				Toast.makeText(mContext, R.string.nomore_pages, Toast.LENGTH_LONG).show();
			else {
		        webControl.setVisibility(View.INVISIBLE);
				webAdapter.add(new MyWebview(mContext));
				webIndex = webAdapter.getCount() - 1;
		        serverWebs.get(webIndex).loadUrl("file:///android_asset/online.html");
		        webpages.addView(webAdapter.getItem(webIndex));
		        while (webpages.getDisplayedChild() != webIndex) webpages.showNext();
				webpages.getChildAt(webIndex).requestFocus();
				imgNew.setImageBitmap(util.generatorCountIcon(util.getResIcon(getResources(), R.drawable.newpage), webAdapter.getCount(), 0, mContext));
			}
			adview.loadAd(adRequest);
		}
	});
	//web list
	webAdapter = new WebAdapter(this, serverWebs);
	webList = (ListView) findViewById(R.id.weblist);
	webList.inflate(this, R.layout.web_list, null);
	webList.setFadingEdgeLength(0);//no shadow when scroll
	webList.setScrollingCacheEnabled(false);
	webList.setAdapter(webAdapter);

	try {//there are a null pointer error reported for the if line below, hard to reproduce, maybe someone use instrument tool to test it. so just catch it.
		if (!getIntent().getAction().equals(Intent.ACTION_VIEW)) 
			serverWebs.get(webIndex).loadUrl("file:///android_asset/online.html");
		else //open the url from intent in a new page if the old page is under reading.
			loadNewPage(getIntent().getDataString(), getIntent().getBooleanExtra("update", false));
	}
	catch (Exception e) {
		e.printStackTrace();
		serverWebs.get(webIndex).loadUrl("file:///android_asset/online.html");
	}

	
	downloadPath = util.preparePath(getFilesDir().getPath());
	
	//for package add
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

@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
	if (event.getRepeatCount() == 0) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {//press Back key in webview will go backword.
			if(webControl.getVisibility() == View.VISIBLE) imgNew.performClick();
			else if (serverWebs.get(webIndex).canGoBack()) serverWebs.get(webIndex).goBack();
			else return super.onKeyDown(keyCode, event);
			
			return true;
		}	
	}
	return super.onKeyDown(keyCode, event);
}

@Override
protected void onResume() {
	FileInputStream fi = null;
	try {
		fi = openFileInput("history");
		mHistory = util.readBookmark(fi);
		fi = openFileInput("bookmark");
		mBookMark = util.readBookmark(fi);
		
		if (urlAdapter.isEmpty()) {
			urlAdapter.add("www.baidu.com");
			urlAdapter.add("www.google.com");
			for (int i = 0; i < mHistory.size(); i++) 
				urlAdapter.add(mHistory.get(i).m_site);
			for (int i = 0; i < mBookMark.size(); i++) 
				urlAdapter.add(mBookMark.get(i).m_site);
			urlAdapter.sort(new stringCompatator());
			
			int l = 0;
			while (l < urlAdapter.getCount()-1) {// remove duplicate
				while (urlAdapter.getItem(l).equals(urlAdapter.getItem(l+1))) 
					urlAdapter.remove(urlAdapter.getItem(l));
				l += 1;
			}
		}
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	}
	historyChanged = false;
	bookmarkChanged = false;

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
public boolean onMenuOpened(int featureId, Menu menu) {
    return true;// return true will show system menu
}

}
