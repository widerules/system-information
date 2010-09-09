package sys.info.trial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.Toast;

public class sysinfo extends TabActivity {

	WebView serverWeb;
	ListView properList, appList;
	ProgressDialog m_dialog;
	String version;
	TabHost tabHost;
	String sdcard, nProcess, nApk;
		
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
    	menu.add(0, 4, 0, getString(R.string.exit)).setVisible(false);//seems not need exit menu
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
			Properties.upload(getString(R.string.url), this);
			break;
		case 2:
			showDialog(1);
			break;
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
			break;
		case 4:
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
    	//register to receive battery intent
		Properties.BatteryString = getString(R.string.battery);
		Properties.batteryHealth = getResources().getTextArray(R.array.batteryHealthState);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(Properties.BroadcastReceiver, filter);
		
        //Debug.stopMethodTracing();
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(Properties.BroadcastReceiver);
		
		super.onPause();
	}
	
	protected void update() {
		properList.setVisibility(1);
		properList.invalidateViews();
		m_dialog.cancel();
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int []resolutions = Properties.resolution(dm);
        Properties.resolution = Integer.toString(resolutions[0]) + "*" + Integer.toString(resolutions[1]);
        
        tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.main, tabHost.getTabContentView(), true);
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getString(R.string.brief))
                .setContent(R.id.PropertyList));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(getString(R.string.online))
                .setContent(R.id.ViewServer));
        //tabHost.addTab(tabHost.newTabSpec("tab3")
        //        .setIndicator(getString(R.string.apps))
        //        .setContent(R.id.AppList));
        
        properList = (ListView)findViewById(R.id.PropertyList);
        Properties.properListItem = new ArrayList<HashMap<String, Object>>();  
        SimpleAdapter properListItemAdapter = new SimpleAdapter(this, Properties.properListItem,   
                     R.layout.property_list,  
                     new String[] {"ItemTitle", "ItemText"},   
                     new int[] {R.id.ItemTitle, R.id.ItemText}  
                 );  
        properList.setAdapter(properListItemAdapter);
        
        //will support app list later
        //appList = (ListView)findViewById(R.id.AppList);
        //Properties.appListItem = new ArrayList<HashMap<String, Object>>();  
        //SimpleAdapter appListItemAdapter = new SimpleAdapter(this, Properties.appListItem,   
        //             R.layout.app_list,  
        //             new String[] {"ItemTitle", "ItemText"},   
        //             new int[] {R.id.ItemTitle, R.id.ItemText}  
        //         );  
        //appList.setAdapter(appListItemAdapter);
        
        tabHost.getTabWidget().getChildAt(0).setLayoutParams(new LinearLayout.LayoutParams(resolutions[2], resolutions[3]));
        tabHost.getTabWidget().getChildAt(1).setLayoutParams(new LinearLayout.LayoutParams(resolutions[2], resolutions[3]));
        //tabHost.getTabWidget().getChildAt(2).setLayoutParams(new LinearLayout.LayoutParams(resolutions[2], resolutions[3]));

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

    	showDialog(0);
    	//refresh();
    	PageTask task = new PageTask();
		task.execute("");
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
    	
    	Properties.dr();
    	
    	SensorManager sensorMgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	Properties.sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL).size() + "";
    	
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Properties.imei = tm.getDeviceId();
        
		try {serverWeb.loadUrl(getString(R.string.url));}
		catch (Exception e) {}
    }
    
    public String refresh() {
		properList.setVisibility(0);//otherwise it will force close on nexus one.
        //properties sort by alphabet, the first is battery, add in onresume().
    	Properties.setInfo(getString(R.string.battery), " ");
    	Properties.setInfo(getString(R.string.buildinfo), "SDK version:" + Properties.sdkversion + "\tRELEASE:" + android.os.Build.VERSION.RELEASE);
    	Properties.setInfo(getString(R.string.camera), Properties.camera() + getString(R.string.pixels));
    	Properties.setInfo(getString(R.string.location), " ");
    	Properties.setInfo(getString(R.string.networks), " ");
    	Properties.setInfo(getString(R.string.processor), Properties.processor()[0]);//it will block sometime
    	Properties.setInfo(getString(R.string.screen), Properties.resolution);
    	Properties.setInfo(getString(R.string.sensors), Properties.sensors + " " + getString(R.string.sensors));
    	Properties.setInfo(getString(R.string.storage), "ram: " + Properties.memtotal());
    	Properties.setInfo(getString(R.string.telephony), "imei: " + Properties.imei);
    	
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
        
		//send message to let view redraw.
		Message msg = mRedrawHandler.obtainMessage();
		mRedrawHandler.sendMessage(msg);
		//update();
		
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(400);
        return "";
    }
    
}
