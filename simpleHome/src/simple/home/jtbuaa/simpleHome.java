package simple.home.jtbuaa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class simpleHome extends Activity {

	WebView serverWeb;
	ListView favoAppList, sysAppList, userAppList;
	AlertDialog m_altDialog;
	String version, myPackageName;
	FrameLayout mainlayout;
	ArrayList mFavoApps, mSysApps, mUserApps;
	private Button btnFavo, btnSys, btnUser, btnWeb;
	int currentTab;
	static int grayColor = 0xFFEEEEEE;
	static int whiteColor = 0xFFFFFFFF;
	Context mContact;
	PackageManager pm;
	favoAppAdapter favoAdapter;
	ApplicationsAdapter sysAdapter, userAdapter;
	ResolveInfo ri;

	@Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
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
    	menu.add(0, 0, 0, getString(R.string.changeback));
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
		if (currentTab == 0)
			menu.add(0, 0, 0, getString(R.string.removeFromFavo));
		else {
			menu.add(0, 0, 0, getString(R.string.addtoFavo));
			menu.add(0, 1, 0, getString(R.string.backup)).setEnabled(false);
		}
	}
	
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case 0:
			if (currentTab ==0) {
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
				e.printStackTrace();
			}
			break;
		case 1://not implement now
			break;
		}
		return false;
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig); //not restart activity each time screen orientation changes
	}

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
			
			if (currentTab != newTab) {
				switch(currentTab) {
				case 0:
					btnFavo.setBackgroundResource(R.drawable.button_layout_unselected);
					favoAppList.setVisibility(View.INVISIBLE);
					break;
				case 1:
					btnSys.setBackgroundResource(R.drawable.button_layout_unselected);
					sysAppList.setVisibility(View.INVISIBLE);
					break;
				case 2:
					btnUser.setBackgroundResource(R.drawable.button_layout_unselected);
					userAppList.setVisibility(View.INVISIBLE);
					break;
				case 3:
					btnWeb.setBackgroundResource(R.drawable.button_layout_unselected);
					serverWeb.setVisibility(View.INVISIBLE);
					break;
				}
				
				switch(newTab) {
				case 0:
					btnFavo.setBackgroundResource(R.drawable.button_layout_selected);
					favoAppList.setVisibility(View.VISIBLE);
					break;
				case 1:
					btnSys.setBackgroundResource(R.drawable.button_layout_selected);
					sysAppList.setVisibility(View.VISIBLE);
					break;
				case 2:
					btnUser.setBackgroundResource(R.drawable.button_layout_selected);
					userAppList.setVisibility(View.VISIBLE);
					break;
				case 3:
					btnWeb.setBackgroundResource(R.drawable.button_layout_selected);
					serverWeb.setVisibility(View.VISIBLE);
					break;
				}
				
				currentTab = newTab;
			}
		}
	}
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContact = this.getBaseContext();
        
        myPackageName = this.getApplicationInfo().packageName;

    	pm = getPackageManager();
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE); // hide titlebar of application, must be before setting the layout
    	//getWindow().requestFeature(Window.FEATURE_PROGRESS);
    	setContentView(R.layout.ads);
    	
        mainlayout = (FrameLayout)findViewById(R.id.mainFrame);
        
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	List<ResolveInfo> mAllApps = pm.queryIntentActivities(mainIntent, 0);
    	//mainIntent.removeCategory(Intent.CATEGORY_LAUNCHER);
    	//mainIntent.addCategory(Intent.CATEGORY_HOME);
    	//mAllApps.addAll(pm.queryIntentActivities(mainIntent, 0));//may add some strange activity.
    	Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name

    	mSysApps = new ArrayList();
    	mUserApps = new ArrayList();
    	for (int i = 0; i < mAllApps.size(); i++) {
    		ResolveInfo ri = mAllApps.get(i);
    		if ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) 
    			mSysApps.add(ri);
    		else mUserApps.add(ri);
    		
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
		mFavoApps = new ArrayList();
		try {
			fi = this.openFileInput("favo");
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
			e1.printStackTrace();
		}

    	//favorite app tab
    	favoAppList = new ListView(this);
    	favoAppList.inflate(this, R.layout.app_list, null);
    	favoAppList.setFadingEdgeLength(0);//no shadow when scroll
    	favoAppList.setScrollingCacheEnabled(false);
    	favoAdapter = new favoAppAdapter(this, mFavoApps);
    	favoAppList.setAdapter(favoAdapter);
        
    	//system app tab
    	sysAppList = new ListView(this);
    	sysAppList.inflate(this, R.layout.app_list, null);
    	sysAppList.setFadingEdgeLength(0);//no shadow when scroll
    	sysAppList.setScrollingCacheEnabled(false);
    	sysAdapter = new ApplicationsAdapter(this, mSysApps);
    	sysAppList.setAdapter(sysAdapter);
        
    	//user app tab
        userAppList = new ListView(this);
        userAppList.inflate(this, R.layout.app_list, null);
        userAppList.setFadingEdgeLength(0);//no shadow when scroll
        userAppList.setScrollingCacheEnabled(false);
        userAdapter = new ApplicationsAdapter(this, mUserApps);
        userAppList.setAdapter(userAdapter);
        
        //online tab
        final Activity activity = this;
        serverWeb = new WebView(this);
        WebSettings webSettings = serverWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
        serverWeb.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
			     // Activities and WebViews measure progress with different scales.
			     // The progress meter will automatically disappear when we reach 100%
			     activity.setProgress(progress * 1000);
			   }
		});
		serverWeb.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;//this will not launch browser when redirect.
			}
		});
		serverWeb.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String ua, String contentDisposition,
					String mimetype, long contentLength) {//need Download Manager
				ContentValues values = new ContentValues();
		        /*values.put(Downloads.URI, url);//指定下载地址
		        values.put(Downloads.COOKIE_DATA, cookie);//如果下载Server需要cookie,设置cookie
		        values.put(Downloads.VISIBILITY,Downloads.VISIBILITY_HIDDEN);//设置下载提示是否在屏幕顶部显示 
		        values.put(Downloads.NOTIFICATION_PACKAGE, getPackageName());//设置下载完成之后回调的包名 
		        values.put(Downloads.NOTIFICATION_CLASS, DownloadCompleteReceiver.class.getName());//设置下载完成之后负责接收的Receiver，这个类要继承BroadcastReceiver      
		        values.put(Downloads.DESTINATION,save_path);//设置下载到的路径，这个需要在Receiver里自行处理
		        values.put(Downloads.TITLE,title);//设置下载任务的名称
		        this.getContentResolver().insert(Downloads.CONTENT_URI, values);*/
			}
		});
        
        try {
        	PackageInfo pi = pm.getPackageInfo(myPackageName, 0);
        	version = "v" + pi.versionName;
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

        currentTab = 0;
    	sysAppList.setVisibility(View.INVISIBLE);
        userAppList.setVisibility(View.INVISIBLE);
		serverWeb.setVisibility(View.INVISIBLE);
		mainlayout.addView(serverWeb);
        mainlayout.addView(userAppList);
        mainlayout.addView(sysAppList);
        mainlayout.addView(favoAppList);
        favoAppList.setBackgroundDrawable(getWallpaper());
        
        btnFavo = (Button) findViewById(R.id.btnFavoriteApp);
        btnFavo.setOnClickListener(mBtnCL);
        
        btnSys = (Button) findViewById(R.id.btnSystemApp);
        btnSys.setOnClickListener(mBtnCL);
        
        btnUser = (Button) findViewById(R.id.btnUserApp);
        btnUser.setOnClickListener(mBtnCL);
        
        btnWeb = (Button) findViewById(R.id.btnOnline);
		btnWeb.setOnClickListener(mBtnCL);

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		registerReceiver(packageReceiver, filter);
		
		PageTask task = new PageTask();
        task.execute("");
    }
    
	BroadcastReceiver packageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
            String action = intent.getAction();
            String packageName = intent.getDataString().split(":")[1];
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            	if ((intent.getFlags() & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
            		for (int i = 0; i < mSysApps.size(); i++) {
            			ResolveInfo info = (ResolveInfo) mSysApps.get(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				sysAdapter.remove(info);
            				break;
            			}
            		}
            	}
            	else {
            		for (int i = 0; i < mUserApps.size(); i++) {
            			ResolveInfo info = (ResolveInfo) mUserApps.get(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				userAdapter.remove(info);
            				break;
            			}
            		}
            	}
        		for (int i = 0; i < mFavoApps.size(); i++) {
        			ResolveInfo info = (ResolveInfo) mFavoApps.get(i);
        			if (info.activityInfo.packageName.equals(packageName)) {
        				favoAdapter.remove(info);
        				break;
        			}
        		}
            }
            else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            	mainIntent.setPackage(packageName);
            	List<ResolveInfo> targetApps = pm.queryIntentActivities(mainIntent, 0);

            	if ((intent.getFlags() & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
    				sysAdapter.add(targetApps.get(0));
    		    	Collections.sort(sysAdapter.localApplist, new ResolveInfo.DisplayNameComparator(pm));//sort by name
            	}
            	else {
    				userAdapter.add(targetApps.get(0));
    		    	Collections.sort(userAdapter.localApplist, new ResolveInfo.DisplayNameComparator(pm));//sort by name
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
        	//convertView.setBackgroundColor(0x00000000);
            
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
            lapp.setTag(info);
            registerForContextMenu(lapp);
            
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
    	ArrayList localApplist;
        public favoAppAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList) apps;
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
    
	class PageTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			try {serverWeb.loadUrl("file:///android_asset/online.html");}
			catch (Exception e) {}
			return null;
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (currentTab == 3)
				serverWeb.goBack();
			return true;
		}
		else return false;
	}
}
