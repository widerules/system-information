package sys.info.jtbuaa;

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
	TextView ServiceText, TaskText, ProcessText;//, AppsText;
	ListView sysAppList, userAppList;
	ProgressDialog m_dialog;
	AlertDialog m_altDialog;
	String version, myPackageName;
	int versionCode;
	FrameLayout mainlayout;
	String sdcard, nProcess, glVender;//apklist;
	CharSequence[] propertyTitles;
	String[] resolutions, cameraSizes, processors, teles;
	String serviceInfo, sFeatureInfo, psInfo;
	boolean fullversion;
	List<ResolveInfo> mAllApps;
	ArrayList mSysApps, mUserApps;
	//WakeLock wakeLock;
	private Button btnWeb, btnSys, btnUser;
	int currentTab;
	static int grayColor = 0xFFEEEEEE;
	static int whiteColor = 0xFFFFFFFF;
	Context mContact;
	PackageManager pm;
	ApplicationsAdapter sysAdapter, userAdapter;
		
	@Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0: {
            //if (m_dialog != null ) m_dialog.cancel();
            m_dialog = new ProgressDialog(this);
            m_dialog.setTitle(getString(R.string.app_name));
            m_dialog.setMessage(getString(R.string.wait));
            m_dialog.setIndeterminate(true);
            m_dialog.setCancelable(true);
            return m_dialog;
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

	
	OnSubItemClickListener msubitemCL = new OnSubItemClickListener();
    class OnSubItemClickListener implements OnItemClickListener {
    	public void onItemClick(AdapterView<?> arg0, 
    			View arg1, 
    			int arg2, //index of selected item, start from 0
    			long arg3) {
    		m_altDialog.dismiss();//dismiss the dialog when click on it.
    	}    	
    };
    
	public void showMyDialog(String[] valuse) {
		ArrayAdapter itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, valuse);
		View myView = getLayoutInflater().inflate(R.layout.popup , (ViewGroup) findViewById(R.id.popup_root));
    	ListView itemView = (ListView) myView.findViewById(R.id.PropertyList);
    	itemView.setAdapter(itemAdapter);
    	itemView.setOnItemClickListener(msubitemCL);
    	AlertDialog altDialog = new AlertDialog.Builder(this).setView(myView).create();
    	altDialog.setOnKeyListener(null);
    	m_altDialog = altDialog;
    	altDialog.show();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, 0, 0, getString(R.string.refresh));//.setVisible(false);
    	menu.add(0, 1, 0, getString(R.string.upload));
    	menu.add(0, 2, 0, getString(R.string.about));
    	menu.add(0, 3, 0, getString(R.string.exit)).setVisible(false);
    	return true;
    }
	
    UploadHandler mUploadHandler = new UploadHandler();

    class UploadHandler extends Handler {

        public void handleMessage(Message msg) {
    		Log.d("===========", "upload handle");
    		Toast.makeText(getBaseContext(), msg.getData().getString("uploadHint"), Toast.LENGTH_SHORT).show();
        }
    };
    
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case 0:
			switch (currentTab) {
			case 0://property
				break;
			case 1://system app
			case 2://user app
				break;
			case 3://server web
				if (btnWeb.getVisibility() == View.VISIBLE)
					serverWeb.reload();
				break;
			}
			break;
		case 1:
			break;
		case 2:
			showDialog(1);
			break;
		case 3:
			finish();
			System.exit(0);
			break;
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig); //not restart activity each time screen orientation changes
	}

	@Override
	protected void onResume () {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	protected void update() {
	}

	
	OnBtnClickListener mBtnCL = new OnBtnClickListener();
	class OnBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch(currentTab) {
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

			String text = (String) ((Button) v).getText();
			if (text.equals(getString(R.string.systemapps))) {
				btnSys.setBackgroundResource(R.drawable.button_layout_selected);
				sysAppList.bringToFront();
				currentTab = 0;
			}
			else if (text.equals(getString(R.string.userapps))) {
				btnUser.setBackgroundResource(R.drawable.button_layout_selected);
				userAppList.bringToFront();
				currentTab = 1;
			}
			else if (text.equals(getString(R.string.online))) {
				btnWeb.setBackgroundResource(R.drawable.button_layout_selected);
				serverWeb.bringToFront();
				currentTab = 2;
			}
			
			mainlayout.invalidate();
		}
	}
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mContact = this.getBaseContext();
        
        myPackageName = this.getApplicationInfo().packageName;

        fullversion = false;
    	pm = getPackageManager();
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE); // hide titlebar of application, must be before setting the layout
    	setContentView(R.layout.ads);
    	
        mainlayout = (FrameLayout)findViewById(R.id.mainFrame);
        
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	mAllApps = pm.queryIntentActivities(mainIntent, 0);
    	mSysApps = new ArrayList();
    	mUserApps = new ArrayList();
    	for (int i = 0; i < mAllApps.size(); i++) {
    		if ((mAllApps.get(i).activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) 
    			mSysApps.add(mAllApps.get(i));
    		else mUserApps.add(mAllApps.get(i));
    	}
    	Collections.sort(mSysApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name
    	Collections.sort(mUserApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name
    	
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
        webSettings.setUserAgentString(myPackageName + versionCode);
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
		serverWeb.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;//this will not launch browser when redirect.
			}
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				handler.proceed();
			}
		});
		mainlayout.addView(serverWeb);
        
        try {
        	PackageInfo pi = pm.getPackageInfo(myPackageName, 0);
        	version = "v" + pi.versionName;
        	versionCode = pi.versionCode;
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

        currentTab = 0;
        
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
    		Log.d("==============", packageName);
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
		String myresult;
		@Override
		protected String doInBackground(String... params) {
	        String url = getString(R.string.url);
			try {serverWeb.loadUrl(url);}
			catch (Exception e) {}
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
		}

		@Override
		protected void onPreExecute() {
			//resText.setText("task started");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			//resText.setText(values[0]);
		}
	}

    private RefreshHandler mRedrawHandler = new RefreshHandler();

    class RefreshHandler extends Handler {

        public void handleMessage(Message msg) {
    		Log.d("===========", "handle message");
        	sysinfo.this.update();
        }
    };

}
