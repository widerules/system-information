package sys.info.jtbuaa;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;


import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class sysinfo extends TabActivity {

	WebView serverWeb;
	TextView ServiceText, TaskText, ProcessText;//, AppsText;
	ListView properList, appList;
	ProgressDialog m_dialog;
	AlertDialog m_altDialog;
	String version;
	int versionCode;
	TabHost tabHost;
	VortexView _vortexView;
	String sdcard, nProcess, glVender;//apklist;
	CharSequence[] propertyItems;
	SimpleAdapter properListItemAdapter;
	SensorManager sensorMgr;
	String[] resolutions, cameraSizes, processors, teles;
	String serviceInfo, sFeatureInfo, psInfo;
	boolean fullversion;
	ArrayAdapter itemAdapter;
		
	@Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0: {
            if (m_dialog != null ) m_dialog.cancel();
            m_dialog = new ProgressDialog(this);
            m_dialog.setTitle(getString(R.string.app_name));
            m_dialog.setMessage(getString(R.string.wait));
            m_dialog.setIndeterminate(true);
            m_dialog.setCancelable(true);
            Log.e(getString(R.string.tag), m_dialog.toString());
            return m_dialog;
        }
        case 1: {
        	String message = getString(R.string.about_dialog_text1) + version + "\n";
        	if (!fullversion) message += getString(R.string.license);
        	message += getString(R.string.about_dialog_text2);
        	
        	return new AlertDialog.Builder(this).
        	setMessage(message).
        	setPositiveButton(getString(R.string.ok),
	          new DialogInterface.OnClickListener() {
	        	  public void onClick(DialogInterface dialog, int which) {}
	          }).create();
        	}
        case 2: {
        	//Log.d("=================", (String) subItems[0]);
        	//itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {"ItemTitle", "ItemText"});
        	//itemView = (ListView) ListView.inflate(this.getBaseContext(), R.id.AppList , null);
        	//itemView.setAdapter(itemAdapter);
        	//m_altDialog = new AlertDialog.Builder(this).setView(itemView).create();
        	return m_altDialog;
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
    	if (!fullversion) menu.add(0, 4, 0, getString(R.string.fullversion));//.setVisible(false);
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
			switch (tabHost.getCurrentTab()) {
			case 0:
				refresh();
				break;
			case 1:
				serverWeb.reload();
				break;
			}
			break;
		case 1:
			String postData = "";
			postData += "processor=" + Properties.processor + "&";
			postData += "bogomips=" + Properties.bogomips + "&";
			postData += "hardware=" + Properties.hardware + "&";
			postData += "memtotal=" + Properties.memtotal + "&";
			postData += "resolution=" + Properties.resolution + "&";
			postData += "sCamera=" + Properties.sCamera + "&";
			postData += "sensors=" + Properties.sensors + "&";
			postData += "vendor=" + Properties.vendor + "&";
			postData += "product=" + Properties.product + "&";
			postData += "sdkversion=" + Properties.sdkversion + "&";
			postData += "imei=" + Properties.imei + "&";
			if (fullversion) postData += "clientVersion=" + "full-version&";
			postData += "versionCode=" + versionCode;
			serverWeb.postUrl(getString(R.string.url)+"/sign", EncodingUtils.getBytes(postData, "BASE64"));
			tabHost.setCurrentTab(1);
			break;
		case 2:
			showDialog(1);
			break;
		case 3:
			finish();
			System.exit(0);
			break;
		case 4:
			Uri uri = android.net.Uri.parse(getString(R.string.url3));
			Intent i = new Intent();
			i.setData(uri);
			try {
				startActivity(i);
            } catch (ActivityNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(this, getString(R.string.fvfail), Toast.LENGTH_SHORT).show();
			}
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
    	//register to receive battery intent
		Properties.BatteryString = (String) propertyItems[0];
		Properties.batteryHealth = getResources().getTextArray(R.array.batteryHealthState);
		Properties.batteryStatus = getResources().getTextArray(R.array.batteryStatus);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(Properties.BroadcastReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(Properties.BroadcastReceiver);
		
		super.onPause();
	}
	
	protected void update() {
		properList.invalidateViews();
		setPropList();
		m_dialog.cancel();
        glVender = _vortexView.getGlVender();
        tabHost.removeView(_vortexView);
	}

	
	OnPropertyItemClickListener mpropertyCL = new OnPropertyItemClickListener();
    class OnPropertyItemClickListener implements OnItemClickListener {
    	String[] subItems;
    	public void onItemClick(AdapterView<?> arg0, 
    			View arg1, 
    			int arg2, //index of selected item, start from 0
    			long arg3) {
    		switch (arg2) {
    		case 0: {//battery
    			subItems = new String[7];
    			subItems[0] = getString(R.string.level) + " " + Properties.Batterys[0];
    			subItems[1] = getString(R.string.health) + " " + Properties.Batterys[1];
    			subItems[2] = getString(R.string.status) + " " + Properties.Batterys[2];
    			subItems[3] = getString(R.string.voltage) + " " + Properties.Batterys[3];
    			subItems[4] = getString(R.string.temperature) + " " + Properties.Batterys[4];
    			subItems[5] = getString(R.string.plugged) + " " + Properties.Batterys[5];
    			subItems[6] = getString(R.string.technology) + " " + Properties.Batterys[6];
    			break;
    		}
    		case 1: {//build info
    			subItems = new String[13];
    	    	subItems[0] = "Board:\t" + android.os.Build.BOARD;
    	    	subItems[1] = "Brand:\t" + android.os.Build.BRAND;
    	    	subItems[2] = "Device:\t" + android.os.Build.DEVICE;
    	    	subItems[3] = "Host:\t" + android.os.Build.HOST;
    	    	subItems[4] = "ID:\t" + android.os.Build.ID;
    	    	subItems[5]= "Model:\t" + android.os.Build.MODEL;
    	    	subItems[6]= "Product:\t" + android.os.Build.PRODUCT;
    	    	subItems[7] = "Tags:\t" + android.os.Build.TAGS;
    	    	subItems[8]= "Type:\t" + android.os.Build.TYPE;
    	    	subItems[9] = "User:\t" + android.os.Build.USER;
    	    	subItems[10] = "Fingerprint:\t" + android.os.Build.FINGERPRINT;
    	    	subItems[11] = "INCREMENTAL: " + android.os.Build.VERSION.INCREMENTAL;
    	    	subItems[12] = Properties.runCmd("cat", "/proc/version")[0];       
    			break;
    		}
    		case 2: {//camera
    			subItems = cameraSizes;
    			break;
    		}
    		case 3: {//location
    			subItems = null;
    	        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    	        List ll = lm.getProviders(true);
    	    	for (int i = 0; i < ll.size(); i++) {
    	    		Location lo = lm.getLastKnownLocation((String) ll.get(i));
    	    		if (lo != null) {
    	    			subItems = new String[3];
    	    			subItems[0] = getString(R.string.latitude) + " " + lo.getLatitude();
    	    			subItems[1] = getString(R.string.longitude) + " " + lo.getLongitude();
    	    			subItems[2] = getString(R.string.altitude) + " " + lo.getAltitude();
    	    		    break;
    	    		}
    	    	}
    	    	if (subItems == null) return;
    			break;
    		}
    		case 4: {//networks
   				ConnectivityManager cmg = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       			NetworkInfo[] nis = cmg.getAllNetworkInfo();
       			subItems = new String[nis.length];
       			int i = 0;
       			for (NetworkInfo ni : nis) {
       				subItems[i] = ni.toString();
       				i += 1;
       			}
       			if ((subItems == null) || (subItems.length < 1)) return;
    			break;
    			//subItems = new String[5];
    			//subItems[0] = "Network Type: " + ;
    			//subItems[1] = "Roaming State: " + ;
    			//subItems[2] = "State: " + ;
    			//subItems[3] = "Local Address: " + ;
    			//subItems[4] = "Public Address: " + ;
    			//break;
    		}
    		case 5: {//processor
    			subItems = processors;
    			break;
    		}
    		case 6: {//screen
    			subItems = new String[4];
    			subItems[0] = "Density: " + resolutions[2];
    			subItems[1] = "xdpi: " + resolutions[3];
    			subItems[2] = "ydpi: " + resolutions[4];
    			if (glVender.startsWith("Android"))
        			subItems[3] = glVender + " Faked GPU";//not real GPU
    			else
        			subItems[3] = "GPU: " + glVender;
    			break;
    		}
    		case 7: {//sensors
    			List l = sensorMgr.getSensorList(Sensor.TYPE_ALL);
    			subItems = new String[l.size()];
    			for (int i = 0; i < l.size(); i++) {
    				Sensor ss = (Sensor) l.get(i);
    				subItems[i] = ss.getName() + ": " + ss.getPower() + "mA by " + ss.getVendor();
    			}
    			break;
    		}
    		case 8: {//storage
    			subItems = Properties.runCmd("df", "");
    			for (int i = 0; i < subItems.length; i++)
    				subItems[i] = subItems[i].split("\\(block")[0];
    			break;
    		}
    		case 9: {//telephony
    			subItems = teles;
    			break;
    		}
    		}
			showMyDialog(subItems);
			//showDialog(2);
    	}
    };
    
    public int getResolution() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getBaseContext().getSystemService(
                Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        d.getMetrics(metrics);
        int width, height;
        if (metrics.widthPixels > metrics.heightPixels) {
        	width = metrics.widthPixels;
        	height = metrics.heightPixels;
        }
        else {
        	height = metrics.widthPixels;
        	width = metrics.heightPixels;
        }
        resolutions = new String[5];
        resolutions[0] = width+"";
        resolutions[1] = height+"";
        resolutions[2] = metrics.density+"";
        resolutions[3] = metrics.xdpi+"";
        resolutions[4] = metrics.ydpi+"";
        Properties.resolution = resolutions[0] + "*" + resolutions[1];
        int tabWidth = 80;
        if (width >= 480) tabWidth = 120;
        
        switch (metrics.densityDpi) {
        case DisplayMetrics.DENSITY_LOW:
        	resolutions[2] += " (ldpi)";
        	break;
        case DisplayMetrics.DENSITY_MEDIUM:
        	resolutions[2] += " (mdpi)";
        	break;
        case DisplayMetrics.DENSITY_HIGH:
        	resolutions[2] += " (hdpi)";
        	break;
        case 320:
        	resolutions[2] += " (xdpi)";
        	break;
        default:
        	resolutions[2] += " (" + metrics.densityDpi + ")";
        }
        return tabWidth;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        fullversion = false;
       	
        tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.main, tabHost.getTabContentView(), true);
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getString(R.string.brief))
                .setContent(R.id.PropertyList));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(getString(R.string.online))
                .setContent(R.id.ViewServer));
        if (fullversion) {
            tabHost.addTab(tabHost.newTabSpec("tab3")
                    .setIndicator(getString(R.string.apps))
                    .setContent(R.id.AppList));
                    //.setContent(R.id.sViewApps));
            tabHost.addTab(tabHost.newTabSpec("tab4")
                    .setIndicator(getString(R.string.process))
                    .setContent(R.id.sViewProcess));
            tabHost.addTab(tabHost.newTabSpec("tab5")
                    .setIndicator(getString(R.string.service))
                    .setContent(R.id.sViewCpu));
            tabHost.addTab(tabHost.newTabSpec("tab6")
                    .setIndicator(getString(R.string.feature))
                    .setContent(R.id.sViewMem));
            //tabHost.addTab(tabHost.newTabSpec("tab7")
            //        .setIndicator("icon")
            //        .setContent(R.id.appicontest));
            ProcessText = (TextView)findViewById(R.id.TextViewProcess);
            ServiceText = (TextView)findViewById(R.id.TextViewCpu);
            TaskText = (TextView)findViewById(R.id.TextViewMem);
            
            //app tab
            appList = (ListView)findViewById(R.id.AppList);
            SimpleAdapter appListItemAdapter = new SimpleAdapter(this, getApp(),   
                         R.layout.app_list,  
                         new String[] {"appicon", "appname", "appversion", "appsource"},   
                         new int[] {R.id.appicon, R.id.appname, R.id.appversion, R.id.appsource}  
                     );  
            appList.setAdapter(appListItemAdapter);
        }

        int tabWidth = getResolution(); 
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tabWidth, 40);
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++)
        	tabHost.getTabWidget().getChildAt(i).setLayoutParams(lp);
        
        //brief tab
        properList = (ListView)findViewById(R.id.PropertyList);
        Properties.properListItem = new ArrayList<HashMap<String, Object>>();  
        properListItemAdapter = new SimpleAdapter(this, Properties.properListItem,   
                     R.layout.property_list,  
                     new String[] {"ItemTitle", "ItemText"},   
                     new int[] {R.id.ItemTitle, R.id.ItemText}  
                 );  
        properList.setAdapter(properListItemAdapter);
        properList.setOnItemClickListener(mpropertyCL);
        
        //online tab
        serverWeb = (WebView)findViewById(R.id.ViewServer);
        WebSettings webSettings = serverWeb.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString("sys.info.jtbuaa" + versionCode);
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

        PackageManager pm = getPackageManager();
        try {
        	PackageInfo pi = pm.getPackageInfo("sys.info.jtbuaa", 0);
        	version = "v" + pi.versionName;
        	versionCode = pi.versionCode;
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

        //just for get gl vender
        _vortexView = new VortexView(this);
        tabHost.addView(_vortexView);
        
    	showDialog(0);
    	initPropList();
    	//refresh();
    	PageTask task = new PageTask();
		task.execute("");
    }
    
    private List<Map<String, Object>> getApp() {
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    	
    	PackageManager pm = getPackageManager();
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo app : apps) {
        	Map<String, Object> map = new HashMap<String, Object>();
        	map.put("appicon", app.loadIcon(pm));
        	//map.put("appicon", R.drawable.icon);
        	map.put("appname", app.loadLabel(pm));
        	try {
				map.put("appversion", pm.getPackageInfo(app.activityInfo.packageName, 0).versionName);
			} catch (NameNotFoundException e) {
				map.put("appversion", "unknown");
			} 
            if((app.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            	map.put("appsource", app.activityInfo.applicationInfo.sourceDir + " (debugable) " + app.activityInfo.packageName);
            	list.add(0, map);
            }
            else {
            	map.put("appsource", app.activityInfo.applicationInfo.sourceDir);
            	list.add(map);
            }
        }
    	Log.d("===============", R.drawable.icon+"");
    	return list;
    }

	class PageTask extends AsyncTask<String, Integer, String> {
		String myresult;
		@Override
		protected String doInBackground(String... params) {
			refreshOnce();
			myresult = refresh();
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			//resText.setText(myresult);
			properList.invalidate();
            if (fullversion) {
	        ProcessText.setText(psInfo);
	        ServiceText.setText(serviceInfo);
	        TaskText.setText(sFeatureInfo);
	        //AppsText.setText(apklist);
            }
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

    public void refreshOnce() {//something not change, then put here
    	Properties.sdkversion = android.os.Build.VERSION.SDK;
    	
    	cameraSizes = Properties.camera();
    	
    	Properties.dr();
    	
    	processors = Properties.processor();
    	
    	Properties.memtotal();
    	
    	sensorMgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	Properties.sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL).size() + "";
    	
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        teles = Properties.telephonies(tm);
        
        String url = getString(R.string.url);
        if (fullversion) url += "/fullversion.jsp";
		try {serverWeb.loadUrl(url);}
		catch (Exception e) {}
    }
    
    public void initPropList() {
		propertyItems = getResources().getTextArray(R.array.propertyItem);
    	for (int i = 0; i < propertyItems.length; i++)
    		Properties.setInfo((String) propertyItems[i], " ");
    }
    
    public void setPropList() {
        //properties sort by alphabet, the first is battery, add in onresume().
		Properties.setInfo((String) propertyItems[1], "SDK version:" + Properties.sdkversion + "\tRELEASE:" + android.os.Build.VERSION.RELEASE);
		Properties.setInfo((String) propertyItems[2], Properties.sCamera + getString(R.string.pixels));
		Properties.setInfo((String) propertyItems[5], Properties.processor);
		Properties.setInfo((String) propertyItems[6], Properties.resolution);
		Properties.setInfo((String) propertyItems[7], Properties.sensors + " " + (String) propertyItems[5]);
		Properties.setInfo((String) propertyItems[8], "ram: " + Properties.memtotal);
		Properties.setInfo((String) propertyItems[9], "IMEI: " + Properties.imei);
    }
    
    public String refresh() {
		//network
		Enumeration<NetworkInterface> nets = null;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
			String tmp = "";
			for (NetworkInterface netIf : Collections.list(nets)) tmp += netIf.toString() + "\n";
			Properties.setInfo((String) propertyItems[4], tmp.trim());
		} catch (SocketException e) {
			Properties.setInfo((String) propertyItems[4], e.toString());
		}

        //location
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List ll = lm.getProviders(true);
        Boolean foundLoc = false;
    	for (int i = 0; i < ll.size(); i++) {
    		Location lo = lm.getLastKnownLocation((String) ll.get(i));
    		if (lo != null) {
    			Properties.setInfo((String) propertyItems[3], lo.getLatitude() + ":" + lo.getLongitude());
    			foundLoc = true;
    		    break;
    		}
    	}
    	if (!foundLoc) Properties.setInfo((String) propertyItems[3], getString(R.string.locationHint));
    	
        if (fullversion) {
        	ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        	List serviceList = am.getRunningServices(10000);
        	serviceInfo = "";
        	for (int i = 0; i < serviceList.size(); i++) {
        		RunningServiceInfo rs = (RunningServiceInfo) serviceList.get(i);
        		serviceInfo += rs.service.flattenToShortString() + "\n";
        	}
            //result += getString(R.string.nService) + serviceList.size() + "\n";//service number
            
            psInfo = "";
        	List appList = am.getRunningAppProcesses();
        	for (int i = 0; i < appList.size(); i++) {
        		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
        		psInfo += as.processName + "\n";
        	}
            //result += getString(R.string.nProcess) + appList.size() + "\n";//process number
            
        	sFeatureInfo = "";
            FeatureInfo[] featureInfos = getPackageManager().getSystemAvailableFeatures();
            if (featureInfos != null && featureInfos.length > 0) {
                for (FeatureInfo featureInfo : featureInfos) {
                	if (featureInfo.name == null) {//opengl es
                		sFeatureInfo += "Open GL ES " + featureInfo.getGlEsVersion() + "\n";
                	}
                	else sFeatureInfo += featureInfo.name + "\n";
                }
            }
        	/*List taskList = am.getRunningTasks(10000);
        	for (int i = 0; i < taskList.size(); i++) {
        		RunningTaskInfo ts = (RunningTaskInfo) taskList.get(i);
        		taskInfo += ts.baseActivity.flattenToShortString() + "\n";
        	}*/
        }
        
        //result += getString(R.string.nTask) + taskList.size() + "\n\n";//task number

        //result += getString(R.string.nProcess) + runCmd("ps", "") + "\n";//process number
        
		//setMap(getString(R.string.nApk), nApk);
        
		//send message to let view redraw.
		Message msg = mRedrawHandler.obtainMessage();
		mRedrawHandler.sendMessage(msg);
		//properListItemAdapter.notifyDataSetChanged();//no use?
		
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(400);
        return "";
    }
    
}
