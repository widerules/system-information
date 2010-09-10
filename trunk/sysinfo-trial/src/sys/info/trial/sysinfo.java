package sys.info.trial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
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
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class sysinfo extends TabActivity {

	WebView serverWeb;
	ListView properList, appList;
	ProgressDialog m_dialog;
	String version;
	TabHost tabHost;
	String sdcard, nProcess, nApk;
	CharSequence[] propertyItems;
	SimpleAdapter properListItemAdapter;
		
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
		Properties.BatteryString = (String) propertyItems[0];
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
		properList.invalidateViews();
		setPropList();
		m_dialog.cancel();
	}
	
	OnPropertyItemClickListener mpropertyCL = new OnPropertyItemClickListener();

    class OnPropertyItemClickListener implements OnItemClickListener {
    	public void onItemClick(AdapterView<?> arg0, 
    			View arg1, 
    			int arg2, //index of selected item, start from 0
    			long arg3) {
    		switch (arg2) {
    		case 0://battery
    			Intent i = new Intent();
    			ComponentName cn = new ComponentName("com.android.settings", "BatteryInfo");
    			i.setComponent(cn);
    			try {
    				startActivity(i);
                } catch (ActivityNotFoundException e) {
    				e.printStackTrace();
    			}
                break;
    		}
    	}
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        String []resolutions = Properties.resolution(dm);
        Properties.resolution = resolutions[0] + "*" + resolutions[1];
        
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
        properListItemAdapter = new SimpleAdapter(this, Properties.properListItem,   
                     R.layout.property_list,  
                     new String[] {"ItemTitle", "ItemText"},   
                     new int[] {R.id.ItemTitle, R.id.ItemText}  
                 );  
        properList.setAdapter(properListItemAdapter);
        properList.setOnItemClickListener(mpropertyCL);
        
        //will support app list later
        //appList = (ListView)findViewById(R.id.AppList);
        //Properties.appListItem = new ArrayList<HashMap<String, Object>>();  
        //SimpleAdapter appListItemAdapter = new SimpleAdapter(this, Properties.appListItem,   
        //             R.layout.app_list,  
        //             new String[] {"ItemTitle", "ItemText"},   
        //             new int[] {R.id.ItemTitle, R.id.ItemText}  
        //         );  
        //appList.setAdapter(appListItemAdapter);
        
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Integer.parseInt(resolutions[2]), Integer.parseInt(resolutions[3]));
        tabHost.getTabWidget().getChildAt(0).setLayoutParams(lp);
        tabHost.getTabWidget().getChildAt(1).setLayoutParams(lp);
        //tabHost.getTabWidget().getChildAt(2).setLayoutParams(lp);

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
    	initPropList();
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
    	
    	Properties.camera();
    	
    	Properties.dr();
    	
    	Properties.processor();
    	
    	Properties.memtotal();
    	
    	SensorManager sensorMgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	Properties.sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL).size() + "";
    	
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Properties.telephonies(tm);
        
		try {serverWeb.loadUrl(getString(R.string.url));}
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
		//wifi
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if ((wifiInfo != null) && (wifiInfo.getMacAddress() != null))
        	Properties.setInfo((String) propertyItems[4], wifiInfo.getMacAddress());
        else
        	Properties.setInfo((String) propertyItems[4], "not avaiable");
        
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
    			Properties.setInfo((String) propertyItems[3], lo.getLatitude() + ":" + lo.getLongitude());
    			foundLoc = true;
    		    break;
    		}
    	}
    	if (!foundLoc) Properties.setInfo((String) propertyItems[3], getString(R.string.locationHint));
    	
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
