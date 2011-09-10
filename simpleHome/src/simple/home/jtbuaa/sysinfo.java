package simple.home.jtbuaa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
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

public class sysinfo extends Activity {

	WebView serverWeb;
	ListView favoAppList, sysAppList, userAppList;
	AlertDialog m_altDialog;
	String version, myPackageName;
	FrameLayout mainlayout;
	List<ResolveInfo> mAllApps;
	ArrayList mFavoApps, mSysApps, mUserApps;
	private Button btnFavo, btnSys, btnUser, btnWeb;
	int currentTab;
	static int grayColor = 0xFFEEEEEE;
	static int whiteColor = 0xFFFFFFFF;
	Context mContact;
	PackageManager pm;
	ApplicationsAdapter favoAdapter, sysAdapter, userAdapter;
		
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
    	menu.add(0, 0, 0, getString(R.string.help));
    	menu.add(0, 1, 0, getString(R.string.about));
    	return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case 0:
			break;
		case 1:
			showDialog(1);
			break;
		}
		return true;
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
				//mainlayout.invalidate();
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
    	setContentView(R.layout.ads);
    	
        mainlayout = (FrameLayout)findViewById(R.id.mainFrame);
        
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	//mainIntent.addCategory(Intent.CATEGORY_HOME); 
    	//mainIntent.addCategory(Intent.CATEGORY_DEFAULT);
    	//mainIntent.addCategory(Intent.CATEGORY_BROWSABLE);
    	//mainIntent.addCategory(Intent.CATEGORY_FRAMEWORK_INSTRUMENTATION_TEST);
    	//mainIntent.addCategory(Intent.CATEGORY_INFO);
    	//mainIntent.addCategory(Intent.CATEGORY_MONKEY);
    	mAllApps = pm.queryIntentActivities(mainIntent, 0);
    	mFavoApps = new ArrayList();
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
    	Collections.sort(mSysApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name
    	Collections.sort(mUserApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name
    	
    	//favorite app tab
    	favoAppList = new ListView(this);
    	favoAppList.setFadingEdgeLength(0);//no shadow when scroll
    	favoAppList.inflate(this, R.layout.app_list, null);
    	favoAdapter = new ApplicationsAdapter(this, mFavoApps);
    	favoAppList.setAdapter(favoAdapter);
        mainlayout.addView(favoAppList);
        
    	//system app tab
    	sysAppList = new ListView(this);
    	sysAppList.setFadingEdgeLength(0);//no shadow when scroll
    	sysAppList.inflate(this, R.layout.app_list, null);
    	sysAdapter = new ApplicationsAdapter(this, mSysApps);
    	sysAppList.setAdapter(sysAdapter);
        mainlayout.addView(sysAppList);
        
    	//user app tab
        userAppList = new ListView(this);
        userAppList.setFadingEdgeLength(0);//no shadow when scroll
        userAppList.inflate(this, R.layout.app_list, null);
        userAdapter = new ApplicationsAdapter(this, mUserApps);
        userAppList.setAdapter(userAdapter);
        mainlayout.addView(userAppList);
        
        //online tab
        serverWeb = new WebView(this);
        WebSettings webSettings = serverWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
		serverWeb.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;//this will not launch browser when redirect.
			}
		});
		mainlayout.addView(serverWeb);
        
        try {
        	PackageInfo pi = pm.getPackageInfo(myPackageName, 0);
        	version = "v" + pi.versionName;
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

        currentTab = 0;
    	sysAppList.setVisibility(View.VISIBLE);
        userAppList.setVisibility(View.VISIBLE);
		serverWeb.setVisibility(View.VISIBLE);
        
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
            				sysAdapter.notifyDataSetChanged();
            				break;
            			}
            		}
            	}
            	else {
            		for (int i = 0; i < mUserApps.size(); i++) {
            			ResolveInfo info = (ResolveInfo) mUserApps.get(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				userAdapter.remove(info);
            				userAdapter.notifyDataSetChanged();
            				break;
            			}
            		}
            	}
        		for (int i = 0; i < mFavoApps.size(); i++) {
        			ResolveInfo info = (ResolveInfo) mFavoApps.get(i);
        			if (info.activityInfo.packageName.equals(packageName)) {
        				favoAdapter.remove(info);
        				favoAdapter.notifyDataSetChanged();
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
    				sysAdapter.notifyDataSetChanged();
            	}
            	else {
    				userAdapter.add(targetApps.get(0));
    		    	Collections.sort(userAdapter.localApplist, new ResolveInfo.DisplayNameComparator(pm));//sort by name
    				userAdapter.notifyDataSetChanged();
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
            final ResolveInfo info;
            info = (ResolveInfo) localApplist.get(position);

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

	class PageTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {
			try {serverWeb.loadUrl("online.html");}
			catch (Exception e) {}
			return null;
		}
	}
}
