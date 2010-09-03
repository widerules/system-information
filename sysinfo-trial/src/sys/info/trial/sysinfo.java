package sys.info.trial;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;


import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class sysinfo extends TabActivity {

	WebView serverWeb;
	ListView ViewList;
	ProgressDialog m_dialog;
	String version;
	TabHost tabHost;
	String processor, bogomips, hardware, memtotal="", resolution, dpi, sCamera, vendor, product, sensors = "", sdkversion, imei;
	String sdcard, nProcess, nApk;
	boolean refreshed;
		
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
        	return new AlertDialog.Builder(this).
        	setMessage(getString(R.string.about_dialog_text1) + version + "\n" + getString(R.string.license) + getString(R.string.about_dialog_text2)).
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
    	menu.add(0, 0, 0, getString(R.string.refresh));
    	menu.add(0, 1, 0, getString(R.string.upload));
    	menu.add(0, 2, 0, getString(R.string.about));
    	menu.add(0, 3, 0, getString(R.string.fullversion));//.setVisible(false);
    	menu.add(0, 4, 0, getString(R.string.exit));
    	return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case 0:
			serverWeb.reload();
			refresh();
			tabHost.setCurrentTab(1);
			return true;
		case 1:
			if (memtotal == "") 
				Toast.makeText(sysinfo.this, getString(R.string.memerr), Toast.LENGTH_SHORT).show();
			else {
				if (!refreshed) {
					refresh();
					refreshed = true;
				}
				String []paras ={processor, bogomips, hardware, memtotal, resolution, sCamera, sensors, vendor, product, sdkversion, imei};
				Properties.upload(getString(R.string.url), paras, this.getApplicationContext());
			}
			return true;
		case 2:
			showDialog(1);
			return true;
		case 3:
			Uri uri = android.net.Uri.parse(getString(R.string.url3));
			Intent i = new Intent();
			i.setData(uri);
			try {
				startActivity(i);
            } catch (ActivityNotFoundException e) {
				e.printStackTrace();
				Toast.makeText(this, getString(R.string.fvfail), Toast.LENGTH_SHORT).show();
			}
			return true;
		case 4:
			finish();
			System.exit(0);
			return true;
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
		Properties.BatteryString = getString(R.string.battery);
		Properties.batteryHealth = getResources().getTextArray(R.array.batteryHealthState);
        //Debug.stopMethodTracing();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(Properties.BroadcastReceiver);
		
		super.onPause();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        refreshed = false;
        
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int []resolutions = Properties.resolution(dm);
        resolution = Integer.toString(resolutions[0]) + "*" + Integer.toString(resolutions[1]);
        
        tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.main, tabHost.getTabContentView(), true);
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getString(R.string.brief))
                .setContent(R.id.PropertyList));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(getString(R.string.apps))
                .setContent(R.id.PropertyList));
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator(getString(R.string.online))
                .setContent(R.id.ViewServer));
        
        ViewList = (ListView)findViewById(R.id.PropertyList);
        
        Properties.listItem = new ArrayList<HashMap<String, Object>>();  
        
        SimpleAdapter listItemAdapter = new SimpleAdapter(this, Properties.listItem,   
                     R.layout.list_items,  
                     new String[] {"ItemTitle", "ItemText"},   
                     new int[] {R.id.ItemTitle, R.id.ItemText}  
                 );  
                  
        ViewList.setAdapter(listItemAdapter);
        
        tabHost.getTabWidget().getChildAt(0).setLayoutParams(new LinearLayout.LayoutParams(resolutions[2], resolutions[3]));
        tabHost.getTabWidget().getChildAt(1).setLayoutParams(new LinearLayout.LayoutParams(resolutions[2], resolutions[3]));
        tabHost.getTabWidget().getChildAt(2).setLayoutParams(new LinearLayout.LayoutParams(resolutions[2], resolutions[3]));

        serverWeb = (WebView)findViewById(R.id.ViewServer);
        serverWeb.getSettings().setJavaScriptEnabled(true);
        WebSettings webSettings = serverWeb.getSettings();
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
        
        PackageManager pm = getPackageManager();
        try {
        	PackageInfo pi = pm.getPackageInfo("sys.info.trial", 0);
        	version = "v" + pi.versionName;
        	List list = pm.getInstalledApplications(0);
        	nApk = Integer.toString(list.size());
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

    	//refresh();
    	PageTask task = new PageTask();
		task.execute("");
    }

	class PageTask extends AsyncTask<String, Integer, String> {
		String myresult;
		@Override
		protected String doInBackground(String... params) {
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

 
    public String refresh() {
		try {serverWeb.loadUrl(getString(R.string.url));}
		catch (Exception e) {}
		
    	SensorManager sensorMgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = tm.getDeviceId();
    	
    	Properties.setInfo(getString(R.string.processor), Properties.processor()[0]);
    	Properties.setInfo(getString(R.string.screen), resolution);
    	Properties.setInfo(getString(R.string.storage), "ram: " + Properties.memtotal());
    	Properties.setInfo(getString(R.string.camera), Properties.camera() + getString(R.string.pixels));
    	Properties.setInfo(getString(R.string.sensors), sensorMgr.getSensorList(Sensor.TYPE_ALL).size() + " sensors found");
    	Properties.setInfo(getString(R.string.battery), " ");
    	Properties.setInfo(getString(R.string.networks), " ");
    	Properties.setInfo(getString(R.string.telephony), "imei: " + imei);
    	Properties.setInfo(getString(R.string.location), " ");
    	Properties.setInfo(getString(R.string.buildinfo), "SDK version:" + android.os.Build.VERSION.SDK + "\tRELEASE:" + android.os.Build.VERSION.RELEASE);
    	
    	//battery
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(Properties.BroadcastReceiver, filter);
		
		//wifi
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if ((wifiInfo != null) && (wifiInfo.getMacAddress() != null))
        	Properties.setInfo(getString(R.string.networks), wifiInfo.getMacAddress());
        else
        	Properties.setInfo(getString(R.string.networks), "not avaiable");
        
        //String tmpsdcard = runCmd("df", "");
        //if (tmpsdcard != null) result += tmpsdcard + "\n\n";
        
		//setMap("dpi", dpi);

        //location
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List ll = lm.getProviders(true);
        Boolean foundLoc = false;
    	for (int i = 0; i < ll.size(); i++) {
    		Location lo = lm.getLastKnownLocation((String) ll.get(i));
    		if (lo != null) {
    			Properties.setInfo(getString(R.string.location), lo.getLatitude() + ":" + lo.getLongitude());
    			foundLoc = true;
    		    break;
    		}
    	}
    	if (!foundLoc) Properties.setInfo(getString(R.string.location), getString(R.string.locationHint));
    	
        //result += getString(R.string.nProcess) + runCmd("ps", "") + "\n";//process number
        
		//setMap(getString(R.string.nApk), nApk);
        
		//setMap(getString(R.string.vendor), Properties.dr()[1]);
        

		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(400);
        return "";
    }
    
}
