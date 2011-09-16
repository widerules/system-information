package simple.home.jtbuaa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

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
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class simpleHome extends Activity implements OnGestureListener, OnTouchListener {

	WebView serverWeb;
	myGridView favoAppList;
	ListView sysAppList, userAppList;
	AlertDialog m_altDialog;
	String version, myPackageName;
	ViewFlipper mainlayout;
	GestureDetector mGestureDetector;
	List<ResolveInfo> mAllApps;
	ArrayList mFavoApps, mSysApps, mUserApps;
	private Button btnFavo, btnSys, btnUser, btnWeb;
	static int grayColor = 0xFFEEEEEE;
	static int whiteColor = 0xFFFFFFFF;
	Context mContext;
	PackageManager pm;
	favoAppAdapter favoAdapter;
	ApplicationsAdapter sysAdapter, userAdapter;
	ResolveInfo ri;
	
	ProgressDialog mProgressDialog;
	private static final int MAX_PROGRESS = 100;
	
	NotificationManager nManager;
	ArrayList<packageIDpair> downloadAppID;
	
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
        case 0:
        {
        	if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setMax(MAX_PROGRESS);
                //mProgressDialog.setTitle(getString(R.string.loading));//android.content.res.Resources$NotFoundException: String resource ID #0x7f050010
                mProgressDialog.setTitle("loading...");
        	}
            return mProgressDialog;
    	}
        case 1: {
        	return new AlertDialog.Builder(this).
        	setMessage(getString(R.string.app_name) + " " + version + "\n\n" 
        			+ getString(R.string.about_dialog_notes) + "\n" + getString(R.string.about_dialog_text2)). 
        	setPositiveButton("Ok",
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
    	menu.add(0, 0, 0, getString(R.string.changeback)).setVisible(false);
    	menu.add(0, 1, 0, getString(R.string.help));
    	menu.add(0, 2, 0, getString(R.string.about));
    	return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			showDialog(1);
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		ri = (ResolveInfo) v.getTag();
		if (mainlayout.getDisplayedChild() == 0)
			menu.add(0, 0, 0, getString(R.string.removeFromFavo));
		else {
			menu.add(0, 0, 0, getString(R.string.addtoFavo));
			menu.add(0, 1, 0, getString(R.string.backup)).setVisible(false);
		}
	}
	
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case 0:
			if (mainlayout.getDisplayedChild() ==0) {
				favoAdapter.remove(ri);
			}
			else {
				if (favoAdapter.getPosition(ri) < 0) {
					favoAdapter.add(ri);
					favoAdapter.sort(new ResolveInfo.DisplayNameComparator(pm));
					}
			}
			try {
				FileOutputStream fo = this.openFileOutput("favo", 0);
				ObjectOutputStream oos = new ObjectOutputStream(fo);
				for (int i = 0; i < favoAdapter.getCount(); i++) {
					oos.writeObject(((ResolveInfo)favoAdapter.localApplist.get(i)).activityInfo.name);
				}
				oos.flush();
				oos.close();
				fo.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
			break;
		case 1://not implement now
			break;
		}
		return false;
	}
	
	class myGridView extends GridView {//just for reset the back ground. otherwise we can use ListView directly.

		public myGridView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected final void onSizeChanged(final int w, final int h,  
	            final int oldw, final int oldh) {
			setBackground();
	    }  
		
		public void setBackground() {
			int w = getWidth();
			int h = getHeight();
			if ((w <= 0) || (h <= 0)) return;
	        Log.d("================", "w: " + w + " h: " + h);
			
			int x = 0, y = 0;
	        Bitmap oldbmp = ((BitmapDrawable) getWallpaper()).getBitmap();
	        Log.d("================", "dw: " + oldbmp.getWidth() + " dh: " + oldbmp.getHeight());
	        Matrix matrix = new Matrix();   
	        float scalew = ((float)w) / oldbmp.getWidth();
	        float scaleh = ((float)h) / oldbmp.getHeight();
	        Log.d("================", "scalew: " + scalew + " scaleh: " + scaleh);
	        if (scalew > scaleh) {
	        	scalew = scaleh;
	        	if (scalew < 1) y = (oldbmp.getHeight() - h) / 2;
	        }
	        else if ((scalew < scaleh) && (scalew < 1)) x = (oldbmp.getWidth() - w) / 2;//centerize the pic.
	        matrix.postScale(scalew, scalew);
	        Bitmap newbmp = Bitmap.createBitmap(oldbmp, x, y, w, h, matrix, true);//wrong for some pic?
	        //Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, w, h, matrix, true);
	        BitmapDrawable bd = new BitmapDrawable(newbmp);
	        
			setBackgroundDrawable(bd);
		}
	};
	
	BroadcastReceiver wallpaperReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			favoAppList.setBackground();
		}
	};
	
	OnBtnClickListener mBtnCL = new OnBtnClickListener();
	class OnBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			int newTab = 0;

			String text = (String) ((Button) v).getText();
			if (text.equals(getString(R.string.favoriteapps))) newTab = 0;
			else if (text.equals(getString(R.string.systemapps))) newTab = 1;
			else if (text.equals(getString(R.string.userapps))) newTab = 2;
			else if (text.equals(getString(R.string.online))) newTab = 3;
			
			if (mainlayout.getDisplayedChild() != newTab) {
				switch(mainlayout.getDisplayedChild()) {
				case 0:
					btnFavo.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				case 1:
					btnSys.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				case 2:
					btnUser.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				case 3:
					btnWeb.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				}
				
				switch(newTab) {
				case 0:
					btnFavo.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				case 1:
					btnSys.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				case 2:
					btnUser.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				case 3:
					btnWeb.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				}
				
				if (mainlayout.getDisplayedChild() > newTab) {//move to left if newTab is on the left
					// 设置切入动画
					mainlayout.setInAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), android.R.anim.slide_in_left));
		            // 设置切出动画
		            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), android.R.anim.slide_out_right));
					while(mainlayout.getDisplayedChild() != newTab)
						mainlayout.showPrevious();
				}
				else {//move to right if newTab is on the right
					// 设置切入动画
		            mainlayout.setInAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), R.anim.slide_in_right));
		            // 设置切出动画
		            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), R.anim.slide_out_left));
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
		        if (size > 10 * sizeM) 		ssize = " (" + size / sizeM + "M)";
		        else if (size > 10 * 1024)	ssize = " (" + size / 1024 + "K)";
		        else if (size > 0)			ssize = " (" + size + "B)";
		        else 						ssize = "";
				packagesSize.put(pStats.packageName, ssize);
			}
		};
		packagesSize = new HashMap<String, Object>();
    	
    	nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	downloadAppID = new ArrayList();
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
    	setContentView(R.layout.ads);
    	
        mainlayout = (ViewFlipper)findViewById(R.id.mainFrame);
        mainlayout.setOnTouchListener(this);
        mainlayout.setLongClickable(true);
        mGestureDetector = new GestureDetector(this);
        
    	//favorite app tab
    	favoAppList = new myGridView(this);
    	favoAppList.setNumColumns(GridView.AUTO_FIT);
    	favoAppList.setVerticalScrollBarEnabled(false);
    	favoAppList.inflate(this, R.layout.app_list, null);
    	favoAppList.setFadingEdgeLength(0);//no shadow when scroll
    	favoAppList.setScrollingCacheEnabled(false);
    	favoAppList.setOnTouchListener(this);
        
    	//system app tab
    	sysAppList = new ListView(this);
    	sysAppList.inflate(this, R.layout.app_list, null);
    	sysAppList.setFadingEdgeLength(0);//no shadow when scroll
    	sysAppList.setScrollingCacheEnabled(false);
    	sysAppList.setOnTouchListener(this);
        
    	//user app tab
        userAppList = new ListView(this);
        userAppList.inflate(this, R.layout.app_list, null);
        userAppList.setFadingEdgeLength(0);//no shadow when scroll
        userAppList.setScrollingCacheEnabled(false);
        userAppList.setOnTouchListener(this);
        
        //online tab
        serverWeb = new WebView(this);
        serverWeb.setOnTouchListener(this);
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
    				if (progress >= MAX_PROGRESS) {
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
				if (url.substring(url.length()-4).equals(".apk")){
					String ss[] = url.split("/");
					String apkName = ss[ss.length-1]; //得到音乐文件的全名(包括后缀)
					DownloadTask dltask = new DownloadTask();
					dltask.execute(url, apkName);
					return true;
				}
				return false;
			}
		});
		
        try {
        	PackageInfo pi = pm.getPackageInfo(myPackageName, 0);
        	version = "v" + pi.versionName;
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

        mainlayout.addView(favoAppList);
        mainlayout.addView(sysAppList);
        mainlayout.addView(userAppList);
		mainlayout.addView(serverWeb);
        
        btnFavo = (Button) findViewById(R.id.btnFavoriteApp);
        btnFavo.setOnClickListener(mBtnCL);
        
        btnSys = (Button) findViewById(R.id.btnSystemApp);
        btnSys.setOnClickListener(mBtnCL);
        
        btnUser = (Button) findViewById(R.id.btnUserApp);
        btnUser.setOnClickListener(mBtnCL);
        
        btnWeb = (Button) findViewById(R.id.btnOnline);
		btnWeb.setOnClickListener(mBtnCL);

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
        		for (int i = 0; i < favoAdapter.getCount(); i++) {
        			ResolveInfo info = favoAdapter.getItem(i);
        			if (info.activityInfo.packageName.equals(packageName)) {
        				favoAdapter.remove(info);
        				break;
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
            
            final ImageButton btnIcon = (ImageButton) convertView.findViewById(R.id.appicon);
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
			final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            
            btnIcon.setImageDrawable(info.loadIcon(pm));
            btnIcon.setEnabled(false);
    		btnIcon.setOnClickListener(new OnClickListener() {//kill app
				@Override
				public void onClick(View arg0) {
					am.restartPackage(info.activityInfo.packageName);
		        	textView1.setTextColor(0xFF000000);//set color back to black after kill it. 
		        	btnIcon.setEnabled(false);
				}
    		});
    		

            LinearLayout lapp = (LinearLayout) convertView.findViewById(R.id.app);
            lapp.setOnClickListener(new OnClickListener() {//start app
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
	            		btnIcon.setEnabled(true);
	                	textView1.setTextColor(0xFFFF7777);//red for running apk
					} catch(Exception e) {
						Toast.makeText(getBaseContext(), e.toString(), 3500).show();
					}
				}
            });
            lapp.setOnTouchListener(simpleHome.this);
            
            Object o = packagesSize.get(info.activityInfo.packageName);
            if (o != null)
           		textView1.setText(info.loadLabel(pm) + o.toString());//the size is not very precise
            else 
            	textView1.setText(info.loadLabel(pm));
            
            textView1.setTextColor(0xFF000000);
        	List appList = am.getRunningAppProcesses();
        	for (int i = 0; i < appList.size(); i++) {
        		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
            	if ((info.activityInfo.processName.equals(as.processName)) && (!as.processName.equals(myPackageName))) {
            		btnIcon.setEnabled(true);
                	textView1.setTextColor(0xFFFF7777);//red for running apk
        			break;
        		}
        	}
        	textView1.setTag(info);
            registerForContextMenu(textView1);
            
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
            int textColor = 0xFF000000;
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
            
            final ImageButton btnIcon = (ImageButton) convertView.findViewById(R.id.favoappicon);
            final TextView textView1 = (TextView) convertView.findViewById(R.id.favoappname);
			final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            
            btnIcon.setImageDrawable(info.loadIcon(pm));
            btnIcon.setEnabled(false);
    		btnIcon.setOnClickListener(new OnClickListener() {//kill app
				@Override
				public void onClick(View arg0) {
					am.restartPackage(info.activityInfo.packageName);
		        	textView1.setTextColor(0xFF000000);//set color back to black after kill it. 
		        	btnIcon.setEnabled(false);
				}
    		});
    		

            textView1.setOnClickListener(new OnClickListener() {//start app
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
	            		btnIcon.setEnabled(true);
	                	textView1.setTextColor(0xFFFF7777);//red for running apk
					} catch(Exception e) {
						Toast.makeText(getBaseContext(), e.toString(), 3500).show();
					}
				}
            });
            textView1.setTag(info);
            registerForContextMenu(textView1);
            textView1.setOnTouchListener(simpleHome.this);
            
            textView1.setText(info.loadLabel(pm));
            textView1.setTextColor(0xFF000000);
        	List appList = am.getRunningAppProcesses();
        	for (int i = 0; i < appList.size(); i++) {
        		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
            	if ((info.activityInfo.processName.equals(as.processName)) && (!as.processName.equals(myPackageName))) {
            		btnIcon.setEnabled(true);
                	textView1.setTextColor(0xFFFF7777);//red for running apk
        			break;
        		}
        	}
            
            return convertView;
        }
    }
    
	class InitTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {//do all time consuming work here
			mSysApps = new ArrayList<ResolveInfo>();
			mUserApps = new ArrayList<ResolveInfo>();
			mFavoApps = new ArrayList<ResolveInfo>();
			
	    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
	    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	    	mAllApps = pm.queryIntentActivities(mainIntent, 0);
	    	//mainIntent.removeCategory(Intent.CATEGORY_LAUNCHER);
	    	//mainIntent.addCategory(Intent.CATEGORY_HOME);
	    	//mAllApps.addAll(pm.queryIntentActivities(mainIntent, 0));//may add some strange activity.
	    	Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name

	    	for (int i = 0; i < mAllApps.size(); i++) {
	    		ResolveInfo ri = mAllApps.get(i);
	    		if ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
	    			mSysApps.add(ri);
	    		else mUserApps.add(ri);
	    		
	    		try {
					getPackageSizeInfo.invoke(pm, ri.activityInfo.packageName, sizeObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
	    		//if (ri.filter.hasAction(Intent.ACTION_DIAL)) //phone, message, contact, ... should add to favorite
	    			//mFavoApps.add(ri);
	    	}
	    	
	    	/*ArrayList packages = (ArrayList) pm.getInstalledPackages(0);
	    	for (int i = 0; i < packages.size(); i++) {
	    		PackageInfo pi = (PackageInfo) packages.get(i);
	    		Intent intent = pm.getLaunchIntentForPackage(pi.packageName);
	    		if (intent == null) {//no Launcher activity
	    		}
	    	}*/

			FileInputStream fi;
			try {//read favorite data
				fi = mContext.openFileInput("favo");
				ObjectInputStream ois = new ObjectInputStream(fi);
				String activityName;
				while ((activityName = (String) ois.readObject()) != null) {
					for (int i = 0; i < mAllApps.size(); i++)
						if (mAllApps.get(i).activityInfo.name.equals(activityName)) {
							mFavoApps.add(mAllApps.get(i));
							break;
						}
				}
				ois.close();
				fi.close();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
			}

			try {serverWeb.loadUrl("file:///android_asset/online.html");}
			catch (Exception e) {}
			
        	Message msg = mAppHandler.obtainMessage();
        	mAppHandler.sendMessage(msg);//inform UI thread to update UI.
            
			return null;
		}
	}
	
    appHandler mAppHandler = new appHandler();

    class appHandler extends Handler {

        public void handleMessage(Message msg) {
        	sysAdapter = new ApplicationsAdapter(getBaseContext(), mSysApps);
        	sysAppList.setAdapter(sysAdapter);
        	
            userAdapter = new ApplicationsAdapter(getBaseContext(), mUserApps);
            userAppList.setAdapter(userAdapter);

        	favoAdapter = new favoAppAdapter(getBaseContext(), mFavoApps);
        	favoAppList.setAdapter(favoAdapter);
        }
    };

	class DownloadTask extends AsyncTask<String, Integer, String> {
		private String URL_str; //网络歌曲的路径
		private File download_file; //下载的文件
		private int total_read = 0; //已经下载文件的长度(以字节为单位)
		private int readLength = 0; //一次性下载的长度(以字节为单位)
		private int apk_length = 0; //音乐文件的长度(以字节为单位)
		private boolean stopDownload = false; //是否停止下载，停止下载为true
		private boolean pauseDownload = false;
		private Thread downThread; //下载线程
		private String apkName; //下载的文件名
		private int NOTIFICATION_ID;
		private Handler mHandler = new Handler();
		private Notification notification;
		private int oldProgress;

		@Override
		protected String doInBackground(String... params) {//download here
	    	URL_str = params[0]; //获取下载链接的url
	    	apkName = params[1]; //获取下载链接的url
	    	
	    	Random random = new Random();
	    	NOTIFICATION_ID = random.nextInt() + 1000;

	    	notification = new Notification(android.R.drawable.stat_sys_download, "start download", System.currentTimeMillis());   
	        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, getIntent(), 0);  
	        notification.setLatestEventInfo(mContext, apkName, "downloading...", contentIntent);
	        notification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.notification_dialog);
	        notification.contentView.setProgressBar(R.id.progress_bar, 100, 0, false);
	        notification.contentView.setTextViewText(R.id.progress, "0%");
	        notification.contentView.setTextViewText(R.id.title, apkName);
	        nManager.notify(NOTIFICATION_ID, notification);
	        
	    	FileOutputStream fos = null; //文件输出流
	    	FileInputStream fis = null; //文件输出流
	    	InputStream is = null; //网络文件输入流
	    	URL url = null;
	    	try {
	        	url = new URL(URL_str); //网络歌曲的url
	        	HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); //打开网络连接
	        	download_file = new File(Environment.getExternalStorageDirectory()+ "/" + apkName);
	        	fos = new FileOutputStream(download_file, false); //初始化文件输出流
	        	fis = new FileInputStream(download_file); //初始化文件输入流
	        	total_read = fis.available(); //初始化“已下载部分”的长度，此处应为0
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
	        			Thread.sleep(5000);//wait for 5 seconds to check again.
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
	                	stopDownload = true;
	                	Log.i("info", "download complete...");
	                	//关闭输入输出流
	                	fos.close();
	                	fis.close();
	                	is.close();
	                	httpConnection.disconnect();
	                	break;
	            	}

	        	}
            	notification.icon = android.R.drawable.stat_sys_download_done;
    	        notification.setLatestEventInfo(mContext, apkName, "download finished", contentIntent);
    	        nManager.notify(NOTIFICATION_ID, notification);
    	        
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(download_file.getPath())), "application/vnd.android.package-archive");
				downloadAppID.add(new packageIDpair(apkName.toLowerCase(), NOTIFICATION_ID));//apkName from appchina is always packageName+xxx.apk. so we use this pair to store package name and nofification id.
				startActivity(intent);//call system package manager to install app. it will not return result code, so not use startActivityForResult();
				
	    	} catch (Exception e) {
	    		notification.icon = android.R.drawable.stat_notify_error;
	    		notification.setLatestEventInfo(mContext, apkName, "download fail: " + e.getMessage(), contentIntent);  
	    		nManager.notify(NOTIFICATION_ID, notification);
	    		e.printStackTrace();
	    	}

	    	return null;
		}

	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (mainlayout.getDisplayedChild() == 3) serverWeb.goBack();
			return true;
		}
		else return false;
	}
	
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
				btnFavo.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			case 1:
				btnSys.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			case 2:
				btnUser.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			case 3:
				btnWeb.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			}
			
			switch(mainlayout.getDisplayedChild()) {
			case 0:
				btnFavo.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			case 1:
				btnSys.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			case 2:
				btnUser.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			case 3:
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
