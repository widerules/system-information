package simple.home.jtbuaa;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class simpleHome extends Activity implements OnGestureListener, OnTouchListener {

	final boolean durtyMode = false;
	WebView serverWeb;
	GridView favoAppList;
	ListView sysAppList, userAppList, shortAppList;
	AlertDialog m_altDialog;
	String version, myPackageName;
	ViewFlipper mainlayout;
	GestureDetector mGestureDetector;
	ResolveInfo appDetail;
	List<ResolveInfo> mAllApps;
	ArrayList mFavoApps, mSysApps, mUserApps;
	private Button btnSys, btnUser, btnWeb;
	static int grayColor = 0xFFEEEEEE;
	static int whiteColor = 0xFFFFFFFF;
	Context mContext;
	PackageManager pm;
	favoAppAdapter favoAdapter;
	shortAppAdapter shortAdapter;
	ApplicationsAdapter sysAdapter, userAdapter;
	ResolveInfo selected_ri, ri_phone, ri_sms, ri_contact;
	ImageView shortcut_phone, shortcut_sms, shortcut_contact;
	RelativeLayout shortcutBar, adsParent, base;
	final static int UPDATE_RI_PHONE = 0, UPDATE_RI_SMS = 1, UPDATE_RI_CONTACT = 2, UPDATE_USER = 3; 
	AdView adview;
	
	ProgressDialog mProgressDialog;
	private static final int MAX_PROGRESS = 100;

	//download related
	String downloadPath;
	NotificationManager nManager;
	ArrayList<packageIDpair> downloadAppID;
	MyApp appstate;
	
	//package size related
	HashMap<String, Object> packagesSize;
	Method getPackageSizeInfo;
	IPackageStatsObserver sizeObserver;
	static int sizeM = 1024*1024; 

	class packageIDpair {
		String packageName;
		int notificationID;
		
		packageIDpair(String name, int id) {
			packageName = name;
			notificationID = id;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0: {//progress dialog
        	if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMax(MAX_PROGRESS);
                mProgressDialog.setTitle(getString(R.string.loading));
        	}
            return mProgressDialog;
    	}
        case 1: {//help dialog
        	return new AlertDialog.Builder(this).
        	setMessage(getString(R.string.app_name) + " " + version + "\n\n"
        			+ getString(R.string.help_message)
        			+ getString(R.string.about_dialog_notes) + "\n" + getString(R.string.about_dialog_text2)). 
        	setPositiveButton(getString(R.string.ok), 
        	          new DialogInterface.OnClickListener() {
	        	  public void onClick(DialogInterface dialog, int which) {}
        	}).create();
        }
        case 2: {//sorry dialog
        	return new AlertDialog.Builder(this).
        	setMessage(getString(R.string.sorry)).
        	setPositiveButton(getString(R.string.ok), 
        	          new DialogInterface.OnClickListener() {
	        	  public void onClick(DialogInterface dialog, int which) {}
        	}).create();
        }
        }
        return null;
	}

	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, 0, 0, R.string.wallpaper).setIcon(android.R.drawable.ic_menu_gallery).setAlphabeticShortcut('W');
    	menu.add(0, 1, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences)
    		.setIntent(new Intent(android.provider.Settings.ACTION_SETTINGS));
    	menu.add(0, 2, 0, R.string.help).setIcon(android.R.drawable.ic_menu_help).setAlphabeticShortcut('H');
    	return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case 0:
	        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
	        startActivity(Intent.createChooser(pickWallpaper, getString(R.string.wallpaper)));
			break;
		case 1:
			return super.onOptionsItemSelected(item);
		case 2:
			showDialog(1);
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		selected_ri = (ResolveInfo) v.getTag();
		if (favoAppList.getVisibility() == View.VISIBLE)
			menu.add(0, 0, 0, getString(R.string.removeFromFavo));
		else {
			menu.add(0, 0, 0, getString(R.string.addtoFavo));
			menu.add(0, 1, 0, getString(R.string.appdetail));
			menu.add(0, 2, 0, getString(R.string.killapp));
		}
	}
	
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case 0://add/remove shortcut
			if (favoAppList.getVisibility() == View.VISIBLE) {
				favoAdapter.remove(selected_ri);
			}
			else if (favoAdapter.getPosition(selected_ri) < 0) {
				favoAdapter.add(selected_ri);
				favoAdapter.sort(new ResolveInfo.DisplayNameComparator(pm));
			}
			try {//save shortcut to file
				FileOutputStream fo = this.openFileOutput("favo", 0);
				ObjectOutputStream oos = new ObjectOutputStream(fo);
				for (int i = 0; i < favoAdapter.getCount(); i++) {
					oos.writeObject(((ResolveInfo)favoAdapter.localApplist.get(i)).activityInfo.name);
				}
				oos.flush();
				oos.close();
				fo.close();
			} catch (Exception e) {}
			break;
		case 1://get app detail info
			Intent intent;
			if (appDetail != null) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(appDetail.activityInfo.packageName, appDetail.activityInfo.name);
				intent.putExtra("pkg", selected_ri.activityInfo.packageName);
				intent.putExtra("com.android.settings.ApplicationPkgName", selected_ri.activityInfo.packageName);
			}
			else {//2.6 tahiti change the action.
				intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", selected_ri.activityInfo.packageName, null));
	        	startActivity(intent);
			}
			try {
				startActivity(intent);
			} catch (Exception e) {
				Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
			}
			
			break;
		case 2://kill app
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			am.restartPackage(selected_ri.activityInfo.packageName);
			break;
		}
		return false;
	}
	
	BroadcastReceiver wallpaperReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			base.setBackgroundDrawable(new ClippedDrawable(getWallpaper()));
		}
	};
	
	OnBtnClickListener mBtnCL = new OnBtnClickListener();
	class OnBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			AdRequest adRequest = new AdRequest();
			adview.loadAd(adRequest);
			
			int newTab = 0;

			String text = (String) ((Button) v).getText();
			if (text.equals(getString(R.string.systemapps))) newTab = 0;
			else if (text.equals(getString(R.string.userapps))) newTab = 1;
			else if (text.equals(getString(R.string.online))) newTab = 2;
			
			if (mainlayout.getDisplayedChild() != newTab) {
				switch(mainlayout.getDisplayedChild()) {
				case 0:
					btnSys.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				case 1:
					btnUser.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				case 2:
					btnWeb.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				}
				
				switch(newTab) {
				case 0:
					btnSys.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				case 1:
					btnUser.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				case 2:
					btnWeb.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				}
				
				if (mainlayout.getDisplayedChild() > newTab) {//move to left if newTab is on the left
					/*// 设置切入动画
					mainlayout.setInAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), android.R.anim.slide_in_left));
		            // 设置切出动画
		            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), android.R.anim.slide_out_right));*/
					while(mainlayout.getDisplayedChild() != newTab)
						mainlayout.showPrevious();
				}
				else {//move to right if newTab is on the right
					/*// 设置切入动画
		            mainlayout.setInAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), R.anim.slide_in_right));
		            // 设置切出动画
		            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), R.anim.slide_out_left));*/
					while(mainlayout.getDisplayedChild() != newTab)
						mainlayout.showNext();
				}
			}
		}
	}
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContext = this.getBaseContext();
        myPackageName = this.getPackageName();
    	pm = getPackageManager();
    	PackageInfo pi = null;
        try {
        	pi = pm.getPackageInfo(myPackageName, 0);
        	version = "v" + pi.versionName;
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

        try {
        	getPackageSizeInfo = PackageManager.class.getMethod(
        		    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    	sizeObserver = new IPackageStatsObserver.Stub() {
			@Override
			public void onGetStatsCompleted(PackageStats pStats,
					boolean succeeded) throws RemoteException {
				
				long size = pStats.codeSize + pStats.cacheSize + pStats.dataSize;
		        String ssize = new String();
		        if (size > 10 * sizeM) 		ssize = size / sizeM + "M";
		        else if (size > 10 * 1024)	ssize = size / 1024 + "K";
		        else if (size > 0)			ssize = size + "B";
		        else 						ssize = "";
				packagesSize.put(pStats.packageName, ssize);
			}
		};
		packagesSize = new HashMap<String, Object>();
    	
    	nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	downloadAppID = new ArrayList();
    	appstate = ((MyApp) getApplicationContext());

    	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
    	setContentView(R.layout.ads);
    	
        mainlayout = (ViewFlipper)findViewById(R.id.mainFrame);
        //mainlayout.setOnTouchListener(this);
        mainlayout.setLongClickable(true);
        mGestureDetector = new GestureDetector(this);
        
        adview = (AdView) this.findViewById(R.id.adView);
        
    	//favorite app tab
    	favoAppList = (GridView) findViewById(R.id.favos);
    	favoAppList.setVerticalScrollBarEnabled(false);
    	favoAppList.inflate(this, R.layout.app_list, null);
    	favoAppList.setFadingEdgeLength(0);//no shadow when scroll
    	favoAppList.setScrollingCacheEnabled(false);
    	//favoAppList.setOnTouchListener(this);
        
    	shortAppList = (ListView) findViewById(R.id.business);
    	shortAppList.setVisibility(View.INVISIBLE);//why can't put it to the right of parent?
    	
    	//system app tab
    	sysAppList = new ListView(this);
    	sysAppList.inflate(this, R.layout.app_list, null);
    	sysAppList.setFadingEdgeLength(0);//no shadow when scroll
    	sysAppList.setScrollingCacheEnabled(false);
    	//sysAppList.setOnTouchListener(this);
        
    	//user app tab
        userAppList = new ListView(this);
        userAppList.inflate(this, R.layout.app_list, null);
        userAppList.setFadingEdgeLength(0);//no shadow when scroll
        userAppList.setScrollingCacheEnabled(false);
        //userAppList.setOnTouchListener(this);
        
        //online tab
        serverWeb = new WebView(this);
        //serverWeb.setOnTouchListener(this);
        WebSettings webSettings = serverWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSaveFormData(true);
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
        webSettings.setDefaultTextEncodingName("utf-8");
        //webSettings.setSupportZoom(true); //设置可以支持缩放         
        // webSettings.setDefaultZoom(.ZoomDensity.FAR); //设置默认缩放方式尺寸是far  
        //webSettings.setBuiltInZoomControls(true);//设置出现缩放工具 
        serverWeb.setScrollBarStyle(0);
        serverWeb.setWebChromeClient(new WebChromeClient() {
        	@Override
        	public void onProgressChanged(WebView view, int progress) {
        		if (mProgressDialog != null) {
    				mProgressDialog.setProgress(progress);
    				if (progress >= 50) {//50% is ok
    					mProgressDialog.dismiss();
    				}
        		}
        	}
		});
		serverWeb.setWebViewClient(new WebViewClient() {

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
		        handler.proceed();//接受证书
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (!url.equals("file:///android_asset/online.html")) showDialog(0);
				super.onPageStarted(view, url, favicon);
			}
			 
			@Override
			public void onPageFinished(WebView view, String url) {
        		if (mProgressDialog != null) {
    				if(mProgressDialog.isShowing())	mProgressDialog.dismiss();
        		}
			}         
			
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				String[] tmp = url.split("\\.");
				if ((tmp.length > 0) && (FileType.MIMEMAP.containsKey(tmp[tmp.length-1].toUpperCase()))) {//files need download
					if (appstate.downloadState.isEmpty()) {
						String ss[] = url.split("/");
						String apkName = ss[ss.length-1]; //得到apk文件的全名(包括后缀)
						
				    	Random random = new Random();
				    	int id = random.nextInt() + 1000;
				    	
						DownloadTask dltask = new DownloadTask();
						dltask.NOTIFICATION_ID = id;
						appstate.downloadState.put(id, dltask);
						dltask.execute(url, apkName);
					}
					else {//only support one download a time, for we can't control the pending intent
						showDialog(2);
					}
					return true;
				}
				return false;
			}
		});
		
        mainlayout.addView(sysAppList);
        mainlayout.addView(userAppList);
		mainlayout.addView(serverWeb);
        
        btnSys = (Button) findViewById(R.id.btnSystemApp);
        btnSys.setOnClickListener(mBtnCL);
        
        btnUser = (Button) findViewById(R.id.btnUserApp);
        btnUser.setOnClickListener(mBtnCL);
        
        btnWeb = (Button) findViewById(R.id.btnOnline);
		btnWeb.setOnClickListener(mBtnCL);

		mSysApps = new ArrayList<ResolveInfo>();
		mUserApps = new ArrayList<ResolveInfo>();
		mFavoApps = new ArrayList<ResolveInfo>();
		favoAdapter = new favoAppAdapter(getBaseContext(), mFavoApps);
		favoAppList.setAdapter(favoAdapter);
		shortAdapter = new shortAppAdapter(getBaseContext(), mFavoApps);
		shortAppList.setAdapter(shortAdapter);
		
        adsParent = (RelativeLayout) findViewById(R.id.adsParent);
        base = (RelativeLayout) findViewById(R.id.base);
        base.setBackgroundDrawable(new ClippedDrawable(getWallpaper()));
        shortcutBar = (RelativeLayout) findViewById(R.id.shortcut_bar);
        shortcutBar.setOnClickListener(new OnClickListener() {//by click this bar to show/hide mainlayout
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (adsParent.getVisibility() == View.VISIBLE) {
					adsParent.setVisibility(View.INVISIBLE);
					favoAppList.setVisibility(View.VISIBLE);
					shortcutBar.setBackgroundResource(R.drawable.shortcut_bar_layout_revert);
				}
				else {
					adsParent.setVisibility(View.VISIBLE);
					favoAppList.setVisibility(View.INVISIBLE);
					shortcutBar.setBackgroundResource(R.drawable.shortcut_bar_layout);
				}
			}
        });
        
		shortcut_phone = (ImageView) findViewById(R.id.shortcut_phone);
		shortcut_sms = (ImageView) findViewById(R.id.shortcut_sms);
		shortcut_contact = (ImageView) findViewById(R.id.shortcut_contact);
		
		//for package add/remove
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		registerReceiver(packageReceiver, filter);
		
		//for wall paper changed
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
		registerReceiver(wallpaperReceiver, filter);
		
		//task for init, such as load webview, load package list
		InitTask initTask = new InitTask();
        initTask.execute("");
    }
    
    @Override 
    protected void onDestroy() {
    	unregisterReceiver(packageReceiver);
    	unregisterReceiver(wallpaperReceiver);
    	
    	super.onDestroy();
    }
    
	BroadcastReceiver packageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
            String action = intent.getAction();
            String packageName = intent.getDataString().split(":")[1];//it always in the format of package:x.y.z
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            	if ((intent.getFlags() & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
            		for (int i = 0; i < sysAdapter.getCount(); i++) {
            			ResolveInfo info = sysAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				sysAdapter.remove(info);
            				break;
            			}
            		}
            	}
            	else {
            		for (int i = 0; i < userAdapter.getCount(); i++) {
            			ResolveInfo info = userAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				userAdapter.remove(info);
            				break;
            			}
            		}
            	}
            	if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {//not remove shortcut if it is just replace 
            		for (int i = 0; i < favoAdapter.getCount(); i++) {
            			ResolveInfo info = favoAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				favoAdapter.remove(info);
            				break;
            			}
            		}
            	}
            }
            else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
	    		try {//get size of new installed package
					getPackageSizeInfo.invoke(pm, packageName, sizeObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}

            	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            	mainIntent.setPackage(packageName);
            	List<ResolveInfo> targetApps = pm.queryIntentActivities(mainIntent, 0);

            	if (targetApps.size() > 0) {//the new package may not support Launcher category, we will omit it.
                	if ((intent.getFlags() & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
        				sysAdapter.add(targetApps.get(0));
        		    	Collections.sort(sysAdapter.localApplist, new ResolveInfo.DisplayNameComparator(pm));//sort by name
                	}
                	else {
        				userAdapter.add(targetApps.get(0));
        		    	Collections.sort(userAdapter.localApplist, new ResolveInfo.DisplayNameComparator(pm));//sort by name
                	}
            	}
            	
            	for (int i = 0; i < downloadAppID.size(); i++) {//cancel download notification if install succeed
            		if (downloadAppID.get(i).packageName.startsWith(packageName.toLowerCase()))
            		{
                		nManager.cancel(downloadAppID.get(i).notificationID);
                		downloadAppID.remove(i);
            		}
            	}
            }
		}
		
	};

    private class ApplicationsAdapter extends ArrayAdapter<ResolveInfo> {
    	ArrayList localApplist;
        public ApplicationsAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = (ResolveInfo) localApplist.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.app_list, parent, false);
            }

            if (position % 2 == 1)
            	convertView.setBackgroundColor(whiteColor);
            else
            	convertView.setBackgroundColor(grayColor);
            
            final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.appicon);
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
            
    		OnClickListener startListener = new OnClickListener() {//start app
				@Override
				public void onClick(View arg0) {
					if (info.activityInfo.applicationInfo.packageName.equals(myPackageName)) return;//not start system info again.
					
					Intent i = new Intent(Intent.ACTION_MAIN);
					i.setComponent(new ComponentName(
							info.activityInfo.applicationInfo.packageName,
							info.activityInfo.name));
					i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);//not start a new activity but bring it to front if it already launched.
					try {
						startActivity(i);
					} catch(Exception e) {
						Toast.makeText(getBaseContext(), e.toString(), 3500).show();
					}
				}
            };
            
            btnIcon.setImageDrawable(info.loadIcon(pm));
            btnIcon.setOnClickListener(startListener);
            
            LinearLayout lapp = (LinearLayout) convertView.findViewById(R.id.app);
            lapp.setOnClickListener(startListener);
        	lapp.setTag(info);
            registerForContextMenu(lapp);
            //lapp.setOnTouchListener(simpleHome.this);
            
            Object o = packagesSize.get(info.activityInfo.packageName);
            if ((o != null) && durtyMode)
           		textView1.setText(info.loadLabel(pm) + " (" + o.toString() + ")");//the size is not very precise
            else 
            	textView1.setText(info.loadLabel(pm));
            
            final Button btnVersion = (Button) convertView.findViewById(R.id.appversion);
            btnVersion.setVisibility(View.VISIBLE);
            try {
            	btnVersion.setText(pm.getPackageInfo(info.activityInfo.packageName, 0).versionName);
			} catch (NameNotFoundException e) {
				btnVersion.setText("unknown");
			}
			btnVersion.setEnabled((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);//disable for system app now.
			btnVersion.setOnClickListener(new OnClickListener() {//delete app
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Uri uri = Uri.fromParts("package", info.activityInfo.packageName, null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					startActivity(intent);
				}
			});
            
            final TextView textView3 = (TextView) convertView.findViewById(R.id.appsource);
            String source = "";
            int textColor = 0xFF888888;
            if (durtyMode) {
                if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
                	source = info.activityInfo.applicationInfo.sourceDir + " (debugable) " + info.activityInfo.packageName;
                	textColor = 0xFFEECC77;//brown for debuggable apk
                }
                else if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
                	source = info.activityInfo.applicationInfo.sourceDir;//we can use source dir to remove it.
                }
                else {
                	source = info.activityInfo.packageName;//we can use package name to uninstall it.
                }
            }
            else if(o != null) source = o.toString();
        	textView3.setText(source);
        	textView3.setTextColor(textColor);//must set color here, otherwise it will be wrong for some item.
			
            return convertView;
        }
    }

    private class favoAppAdapter extends ArrayAdapter<ResolveInfo> {
    	ArrayList<ResolveInfo> localApplist;
        public favoAppAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList<ResolveInfo>) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = (ResolveInfo) localApplist.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.favo_list, parent, false);
            }

            convertView.setBackgroundColor(0);
            
            final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.favoappicon);
            
            btnIcon.setImageDrawable(info.loadIcon(pm));
    		btnIcon.setOnClickListener(new OnClickListener() {//kill app
				@Override
				public void onClick(View arg0) {
					if (info.activityInfo.applicationInfo.packageName.equals(myPackageName)) return;//not start system info again.
					
					Intent i = new Intent(Intent.ACTION_MAIN);
					i.setComponent(new ComponentName(
							info.activityInfo.applicationInfo.packageName,
							info.activityInfo.name));
					i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);//not start a new activity but bring it to front if it already launched.
					try {
						startActivity(i);
					} catch(Exception e) {
						Toast.makeText(getBaseContext(), e.toString(), 3500).show();
					}
				}
    		});
    		btnIcon.setTag(info);
            registerForContextMenu(btnIcon);
            
            return convertView;
        }
    }
    
    private class shortAppAdapter extends ArrayAdapter<ResolveInfo> {
    	ArrayList<ResolveInfo> localApplist;
        public shortAppAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList<ResolveInfo>) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = (ResolveInfo) localApplist.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.favo_list, parent, false);
            }

            final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.favoappicon);
            TextView appname = (TextView) convertView.findViewById(R.id.favoappname);
            
            btnIcon.setImageDrawable(info.loadIcon(pm));
    		btnIcon.setOnClickListener(new OnClickListener() {//kill app
				@Override
				public void onClick(View arg0) {
					if (info.activityInfo.applicationInfo.packageName.equals(myPackageName)) return;//not start system info again.
					
					Intent i = new Intent(Intent.ACTION_MAIN);
					i.setComponent(new ComponentName(
							info.activityInfo.applicationInfo.packageName,
							info.activityInfo.name));
					i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);//not start a new activity but bring it to front if it already launched.
					try {
						startActivity(i);
					} catch(Exception e) {
						Toast.makeText(getBaseContext(), e.toString(), 3500).show();
					}
				}
    		});
    		btnIcon.setTag(info);
            registerForContextMenu(btnIcon);
            
            appname.setText(info.loadLabel(pm));
            
            return convertView;
        }
    }
    
	class InitTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {//do all time consuming work here
			
	    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
	    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	    	mAllApps = pm.queryIntentActivities(mainIntent, 0);
	    	Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name

	    	FileInputStream fi = null;
	    	ObjectInputStream ois = null;
			try {//read favorite data
				fi = mContext.openFileInput("favo");
				ois = new ObjectInputStream(fi);
				String activityName;
				while ((activityName = (String) ois.readObject()) != null) {
					//Log.d("=============", activityName);
					for (int i = 0; i < mAllApps.size(); i++)
						if (mAllApps.get(i).activityInfo.name.equals(activityName)) {
							mFavoApps.add(mAllApps.get(i));
							break;
						}
				}
			} catch (EOFException e) {//only when read eof need send out msg.
				try {
					ois.close();
					fi.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
            
	    	//read all resolveinfo
	    	String label_sms = "簡訊 Messaging メッセージ 信息 消息 메시지  Mensajes Messaggi Berichten SMS a MMS SMS/MMS"; //use label name to get short cut
	    	String label_phone = "電話 Phone 电话 拨号键盘 키패드  Telefon Teléfono Téléphone Telefono Telefoon Телефон 휴대전화  Dialer";
	    	String label_contact = "聯絡人 联系人 Contacts 連絡先 通讯录 전화번호부  Kontakty Kontakte Contactos Contatti Contacten Контакты 주소록";
	    	for (int i = 0; i < mAllApps.size(); i++) {
	    		ResolveInfo ri = mAllApps.get(i);
	    		if ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
	    			mSysApps.add(ri);
	    			String name = ri.loadLabel(pm).toString() ; 
	    			//Log.d("===============", name);
	    			if (label_sms.contains(name)) {
	    				if ((ri_sms == null) && (!name.equals("MM"))) {
	    					ri_sms = ri;
	    		        	Message msgsms = mAppHandler.obtainMessage();
	    		        	msgsms.what = UPDATE_RI_SMS;
	    		        	mAppHandler.sendMessage(msgsms);//inform UI thread to update UI.
	    				}
	    			}
	    			else if (label_phone.contains(name)) {
	    				if (ri_phone == null) {
	    					ri_phone = ri;
	    		        	Message msgphone = mAppHandler.obtainMessage();
	    		        	msgphone.what = UPDATE_RI_PHONE;
	    		        	mAppHandler.sendMessage(msgphone);//inform UI thread to update UI.
	    				}
	    			}
	    			else if (label_contact.contains(name)) {
	    				if (ri_contact == null) {
	    					ri_contact = ri;
	    		        	Message msgcontact = mAppHandler.obtainMessage();
	    		        	msgcontact.what = UPDATE_RI_CONTACT;
	    		        	mAppHandler.sendMessage(msgcontact);//inform UI thread to update UI.
	    				}
	    			}
	    		}
	    		else mUserApps.add(ri);
	    		
	    		try {
					getPackageSizeInfo.invoke(pm, ri.activityInfo.packageName, sizeObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
	    	}
        	Message msguser = mAppHandler.obtainMessage();
        	msguser.what = UPDATE_USER;
        	mAppHandler.sendMessage(msguser);//inform UI thread to update UI.
	    	
        	String status = Environment.getExternalStorageState();
        	if (status.equals(Environment.MEDIA_MOUNTED)) {
        		downloadPath = Environment.getExternalStorageDirectory() + "/simpleHome/";   
    			java.io.File myFilePath = new java.io.File(downloadPath);
    			try
    			{
    			    if(myFilePath.isDirectory()) ;//folder exist
    			    else myFilePath.mkdir();//create folder
    			}
    			catch(Exception e) {
    				e.printStackTrace();
    			}
        	} 
        	else downloadPath = getFilesDir().getPath() + "/";
			   
        	FileType.initMimeMap();//init the file type map
			try {serverWeb.loadUrl("file:///android_asset/online.html");}
			catch (Exception e) {}
			
	    	mainIntent = new Intent(Intent.ACTION_VIEW, null);
	    	mainIntent.addCategory(Intent.CATEGORY_DEFAULT);
	    	List<ResolveInfo> viewApps = pm.queryIntentActivities(mainIntent, 0);
	    	appDetail = null;
	    	for (int i = 0; i < viewApps.size(); i++) {
	    		if (viewApps.get(i).activityInfo.name.contains("InstalledAppDetails")) {
	    			appDetail = viewApps.get(i);
	    			return null;//get the activity for app detail setting
	    		}
	    	}
	    	
			return null;
		}
	}
	
    appHandler mAppHandler = new appHandler();

    class appHandler extends Handler {

        public void handleMessage(Message msg) {
        	switch (msg.what) {
        	case UPDATE_USER:
        		sysAdapter = new ApplicationsAdapter(getBaseContext(), mSysApps);
        		sysAppList.setAdapter(sysAdapter);
        	
        		userAdapter = new ApplicationsAdapter(getBaseContext(), mUserApps);
        		userAppList.setAdapter(userAdapter);
        		break;
        	case UPDATE_RI_PHONE:
    			shortcut_phone.setImageDrawable(ri_phone.loadIcon(pm));
    			shortcut_phone.setOnClickListener(new OnClickListener() {//start app
    				@Override
    				public void onClick(View arg0) {
    					Intent i = new Intent(Intent.ACTION_MAIN);
    					i.setComponent(new ComponentName(
    							ri_phone.activityInfo.applicationInfo.packageName,
    							ri_phone.activityInfo.name));
    					i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);//not start a new activity but bring it to front if it already launched.
    					try {
    						startActivity(i);
    					} catch(Exception e) {
    						Toast.makeText(getBaseContext(), e.toString(), 3500).show();
    					}
    				}
    			});
        		break;
        	case UPDATE_RI_SMS:
    			shortcut_sms.setImageDrawable(ri_sms.loadIcon(pm));
    			shortcut_sms.setOnClickListener(new OnClickListener() {//start app
    				@Override
    				public void onClick(View arg0) {
    					Intent i = new Intent(Intent.ACTION_MAIN);
    					i.setComponent(new ComponentName(
    							ri_sms.activityInfo.applicationInfo.packageName,
    							ri_sms.activityInfo.name));
    					i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);//not start a new activity but bring it to front if it already launched.
    					try {
    						startActivity(i);
    					} catch(Exception e) {
    						Toast.makeText(getBaseContext(), e.toString(), 3500).show();
    					}
    				}
    			});
        		break;
        	case UPDATE_RI_CONTACT:
    			shortcut_contact.setImageDrawable(ri_contact.loadIcon(pm));
    			shortcut_contact.setOnClickListener(new OnClickListener() {//start app
    				@Override
    				public void onClick(View arg0) {
    					Intent i = new Intent(Intent.ACTION_MAIN);
    					i.setComponent(new ComponentName(
    							ri_contact.activityInfo.applicationInfo.packageName,
    							ri_contact.activityInfo.name));
    					i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);//not start a new activity but bring it to front if it already launched.
    					try {
    						startActivity(i);
    					} catch(Exception e) {
    						Toast.makeText(getBaseContext(), e.toString(), 3500).show();
    					}
    				}
    			});
        		break;
        	}
        }
    };

	class DownloadTask extends AsyncTask<String, Integer, String> {
		private String URL_str; //网络歌曲的路径
		private File download_file; //下载的文件
		private int total_read = 0; //已经下载文件的长度(以字节为单位)
		private int readLength = 0; //一次性下载的长度(以字节为单位)
		private int apk_length = 0; //音乐文件的长度(以字节为单位)
		private String apkName; //下载的文件名
		int NOTIFICATION_ID;
		private Notification notification;
		private int oldProgress;
		boolean stopDownload = false;//true to stop download
		boolean pauseDownload = false;//true to pause download

		@Override
		protected String doInBackground(String... params) {//download here
	    	URL_str = params[0]; //获取下载链接的url
	    	apkName = params[1]; //获取下载链接的url
	    	
	    	notification = new Notification(android.R.drawable.stat_sys_download, "start download", System.currentTimeMillis());   
			
			Intent intent = new Intent();
			intent.setAction("simple.home.jtbuaa.downloadControl");//this intent is to pause/stop download
			intent.putExtra("id", NOTIFICATION_ID);
			intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NO_HISTORY);
	        //Log.d("==============", "id: " + NOTIFICATION_ID);

	        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);  
	        notification.setLatestEventInfo(mContext, apkName, "downloading...", contentIntent);
	        
	        notification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.notification_dialog);
	        notification.contentView.setProgressBar(R.id.progress_bar, 100, 0, false);
	        notification.contentView.setTextViewText(R.id.progress, "0%");
	        notification.contentView.setTextViewText(R.id.title, apkName);
	        nManager.notify(NOTIFICATION_ID, notification);
	        
	    	FileOutputStream fos = null; //文件输出流
	    	InputStream is = null; //网络文件输入流
	    	URL url = null;
	    	try {
	        	url = new URL(URL_str); //网络歌曲的url
	        	HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); //打开网络连接
        		download_file = new File(downloadPath + apkName);
        		fos = new FileOutputStream(download_file, false);
	        	total_read = 0; //初始化“已下载部分”的长度，此处应为0
	        	apk_length = httpConnection.getContentLength(); //要下载的文件的总长度
	        	is = httpConnection.getInputStream();
	        	if (is == null) { //如果下载失败则打印日志，并返回
                	notification.icon = android.R.drawable.stat_notify_error;
	    	        notification.setLatestEventInfo(mContext, apkName, "download failed", contentIntent);  
	    	        nManager.notify(NOTIFICATION_ID, notification);
	            	return "download failed";
	        	}

	        	byte buf[] = new byte[1024]; //定义下载缓冲区
	        	readLength = 0; //一次性下载的长度
	        	Log.i("info", "download start...");
	        	
	        	oldProgress = 0;
	        	//如果读取网络文件的数据流成功，且用户没有选择停止下载，则开始下载文件
	        	while (readLength != -1 && !stopDownload) {
	        		if (pauseDownload) {
	        			//Thread.sleep(5000);//wait for 5 seconds to check again.
	        			continue;
	        		}
	        		
	            	if((readLength = is.read(buf))>0){
	                	fos.write(buf, 0, readLength);
	                	total_read += readLength; //已下载文件的长度增加
	            	}

                	int progress = (int) ((total_read+0.0)/apk_length*100);
                	if (oldProgress != progress) {//the device will get no response if update too often
                		oldProgress = progress;
                		notification.contentView.setProgressBar(R.id.progress_bar, 100, progress, false);//update download progress
            	        notification.contentView.setTextViewText(R.id.progress, progress + "%");
                		nManager.notify(NOTIFICATION_ID, notification);
                	}
	            	
	            	if (total_read == apk_length) { //当已下载的长度等于网络文件的长度，则下载完成
	                	Log.i("info", "download complete...");
	            	}

	        	}
            	//关闭输入输出流
            	fos.close();
            	is.close();
            	httpConnection.disconnect();
            	
            	appstate.downloadState.remove(NOTIFICATION_ID);
            	if (stopDownload) {//stop download by user. clear notification
            		nManager.cancel(NOTIFICATION_ID);
            	}
            	else {//download success. change notification, start package manager to install package
                	notification.icon = android.R.drawable.stat_sys_download_done;

                	String[] tmp = apkName.split("\\.");
        			intent = new Intent();
        			intent.setAction(Intent.ACTION_VIEW);
        			intent.setDataAndType(Uri.fromFile(download_file), FileType.MIMEMAP.get(tmp[tmp.length-1].toUpperCase()));
        	        contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);  
        	        notification.contentView.setOnClickPendingIntent(R.id.notification_dialog, contentIntent);
        	        notification.setLatestEventInfo(mContext, apkName, "download finished", contentIntent);//click listener for download progress bar
        	        nManager.notify(NOTIFICATION_ID, notification);
        	        
        			downloadAppID.add(new packageIDpair(apkName.toLowerCase(), NOTIFICATION_ID));//apkName from appchina is always packageName+xxx.apk. so we use this pair to store package name and nofification id.
        			
   	        		Process p = Runtime.getRuntime().exec("chmod 644 " + download_file.getPath());//try to change file property     
   	        		p.waitFor(); 
    				startActivity(intent);//call system package manager to install app. it will not return result code, so not use startActivityForResult();
            	}
				
	    	} catch (Exception e) {
	    		notification.icon = android.R.drawable.stat_notify_error;
	    		notification.setLatestEventInfo(mContext, apkName, "download fail: " + e.getMessage(), contentIntent);  
	    		nManager.notify(NOTIFICATION_ID, notification);
	    		e.printStackTrace();
	    	}

	    	return null;
		}

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() == 0) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {//press Back key in webview will go backword.
				if (adsParent.getVisibility() == View.VISIBLE) { 
					if ((mainlayout.getDisplayedChild() == 2) && (serverWeb.canGoBack())) serverWeb.goBack();
					else shortcutBar.performClick();
				}
				return true;
			}	
			//else if ((keyCode == KeyEvent.KEYCODE_HOME)  && !event.isLongPress()) {//press Home key will goto the favorite tab.
			//	Log.d("==============", "home key");
			//	btnFavo.performClick();
			//	return true;
			//}
		}
		
		return false;
	}
	
	/*@Override
	public void onAttachedToWindow()
	{
	    this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);//help to get Home key.  
	    super.onAttachedToWindow();
	}*/
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig); //not restart activity each time screen orientation changes
    }


	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		// TODO Auto-generated method stub
		if (e1.getX() == e2.getX()) return false;
		else if ((e1.getX() < e2.getX()) && (mainlayout.getDisplayedChild() == 0)) return false;//don't show previous for first view
		else if ((e1.getX() > e2.getX()) && (mainlayout.getDisplayedChild() == mainlayout.getChildCount()-1)) return false;//don't show next for the last view

		int oldIndex = mainlayout.getDisplayedChild(); 
		boolean scroll = false;
		
		if(e2.getX() - e1.getX() > 100 + Math.abs(e2.getY() - e1.getY())) {//the finger move on x direction more than y direction, from left to right
			// 设置切入动画
			mainlayout.setInAnimation(AnimationUtils.loadAnimation(
                    getApplicationContext(), android.R.anim.slide_in_left));
            // 设置切出动画
            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
                    getApplicationContext(), android.R.anim.slide_out_right));
			mainlayout.showPrevious(); //move to left
			scroll = true;
		}
		else if(e1.getX() - e2.getX() > 100 + Math.abs(e1.getY() - e2.getY())) {//the finger move on x direction more than y direction, from right to left
			// 设置切入动画
            mainlayout.setInAnimation(AnimationUtils.loadAnimation(
                    getApplicationContext(), R.anim.slide_in_right));
            // 设置切出动画
            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
                    getApplicationContext(), R.anim.slide_out_left));
			mainlayout.showNext(); //move to right
			scroll = true;
		}

		if (scroll) {
			switch(oldIndex) {
			case 0:
				btnSys.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			case 1:
				btnUser.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			case 2:
				btnWeb.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			}
			
			switch(mainlayout.getDisplayedChild()) {
			case 0:
				btnSys.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			case 1:
				btnUser.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			case 2:
				btnWeb.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			}
			return true;
		}
		else return false;
	}


	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event);
	}
}

/** from Android Home sample
 * When a drawable is attached to a View, the View gives the Drawable its dimensions
 * by calling Drawable.setBounds(). In this application, the View that draws the
 * wallpaper has the same size as the screen. However, the wallpaper might be larger
 * that the screen which means it will be automatically stretched. Because stretching
 * a bitmap while drawing it is very expensive, we use a ClippedDrawable instead.
 * This drawable simply draws another wallpaper but makes sure it is not stretched
 * by always giving it its intrinsic dimensions. If the wallpaper is larger than the
 * screen, it will simply get clipped but it won't impact performance.
 */
class ClippedDrawable extends Drawable {
    private final Drawable mWallpaper;

    public ClippedDrawable(Drawable wallpaper) {
        mWallpaper = wallpaper;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        // Ensure the wallpaper is as large as it really is, to avoid stretching it
        // at drawing time
        mWallpaper.setBounds(left, top, left + mWallpaper.getIntrinsicWidth(),
                top + mWallpaper.getIntrinsicHeight());
    }

    public void draw(Canvas canvas) {
        mWallpaper.draw(canvas);
    }

    public void setAlpha(int alpha) {
        mWallpaper.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        mWallpaper.setColorFilter(cf);
    }

    public int getOpacity() {
        return mWallpaper.getOpacity();
    }
}
