package sys.info.jtbuaa;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.util.EncodingUtils;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
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
import android.hardware.Sensor;
import android.hardware.SensorManager;


import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class sysinfo extends Activity {

	WebView serverWeb;
	TextView ServiceText, TaskText, ProcessText;//, AppsText;
	ListView properList, sysAppList, userAppList;
	ProgressDialog m_dialog;
	AlertDialog m_altDialog;
	String version, myPackageName;
	int versionCode;
	FrameLayout mainlayout;
	VortexView _vortexView;
	String sdcard, nProcess, glVender;//apklist;
	CharSequence[] propertyTitles;
	SimpleAdapter properListItemAdapter;
	SensorManager sensorMgr;
	String[] resolutions, cameraSizes, processors, teles;
	String serviceInfo, sFeatureInfo, psInfo;
	boolean fullversion;
	ArrayAdapter itemAdapter;
	List<ResolveInfo> mAllApps;
	WakeLock wakeLock;
	private Button btnBrief, btnWeb, btnSys, btnUser;
	int currentTab;
		
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
				refresh();
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
			String postData = "";
			postData += "processor=" + util.processor + "&";
			postData += "bogomips=" + util.bogomips + "&";
			postData += "hardware=" + util.hardware + "&";
			postData += "memtotal=" + util.memtotal + "&";
			postData += "resolution=" + util.resolution + "&";
			postData += "sCamera=" + util.sCamera + "&";
			postData += "sensors=" + util.sensors + "&";
			postData += "vendor=" + util.vendor + "&";
			postData += "product=" + util.product + "&";
			postData += "sdkversion=" + util.sdkversion + "&";
			postData += "imei=" + util.imei + "&";
			if (fullversion) postData += "clientVersion=" + "full-version&";
			postData += "versionCode=" + versionCode;
			serverWeb.postUrl(getString(R.string.url)+"/sign", EncodingUtils.getBytes(postData, "BASE64"));
			if (btnWeb.getVisibility() == View.VISIBLE)
				mainlayout.bringChildToFront(serverWeb);
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

		if (wakeLock == null) {
	        PowerManager powm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
	        wakeLock = powm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getApplication().getPackageName());//keep screen on
	        wakeLock.acquire();
		}
		
    	//register to receive battery intent
		util.batteryHealth = getResources().getTextArray(R.array.batteryHealthState);
		util.batteryStatus = getResources().getTextArray(R.array.batteryStatus);
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(util.BroadcastReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(util.BroadcastReceiver);
		if (wakeLock != null) {
			wakeLock.release();
			wakeLock = null;
		}

		super.onPause();
	}
	
	protected void update() {
		properList.invalidateViews();
		setPropList();
		m_dialog.cancel();
        glVender = _vortexView.getGlVender();
	}

	
	OnPropertyItemClickListener mpropertyCL = new OnPropertyItemClickListener();
    class OnPropertyItemClickListener implements OnItemClickListener {
    	String[] subItems;
    	public void onItemClick(AdapterView<?> arg0, 
    			View arg1, 
    			int arg2, //index of selected item, start from 0
    			long arg3) {
			subItems = null;
    		switch (arg2) {
    		case 0: {//battery
    			subItems = new String[7];
    			subItems[0] = getString(R.string.level) + " " + util.Batterys[0];
    			subItems[1] = getString(R.string.health) + " " + util.Batterys[1];
    			subItems[2] = getString(R.string.status) + " " + util.Batterys[2];
    			subItems[3] = getString(R.string.voltage) + " " + util.Batterys[3];
    			subItems[4] = getString(R.string.temperature) + " " + util.Batterys[4];
    			subItems[5] = getString(R.string.plugged) + " " + util.Batterys[5];
    			subItems[6] = getString(R.string.technology) + " " + util.Batterys[6];
    			break;
    		}
    		case 1: {//build info
    			subItems = new String[13];
    	    	subItems[0] = "Brand:\t" + Build.BRAND;
    	    	subItems[1]= "Product:\t" + Build.PRODUCT;
    	    	subItems[2] = "Device:\t" + Build.DEVICE;
    	    	subItems[3] = "Board:\t" + Build.BOARD;
    	    	subItems[4] = "Release:\t" + Build.VERSION.RELEASE;
    	    	subItems[5] = "ID:\t" + Build.ID;
    	    	subItems[6] = "Incremental:\t" + Build.VERSION.INCREMENTAL;
    	    	subItems[7]= "Type:\t" + Build.TYPE;
    	    	subItems[8] = "Tags:\t" + Build.TAGS;
    	    	
    	    	String CDDrequiredFingerprint = Build.BRAND + "/" + Build.PRODUCT + "/" + Build.DEVICE 
    			    + "/" + Build.BOARD + ":" + Build.VERSION.RELEASE + "/" + Build.ID + "/" 
    			    + Build.VERSION.INCREMENTAL + ":" + Build.TYPE + "/" + Build.TAGS;
    	    	
    	    	if (!Build.FINGERPRINT.trim().equals(CDDrequiredFingerprint.trim())) {         
        	    	subItems[9] =  "current  Fingerprint: " + Build.FINGERPRINT;
        	    	subItems[10] = "required Fingerprint: " + CDDrequiredFingerprint;
    	    	}
    	    	else {
        	    	subItems[9] = "Fingerprint:\t" + Build.FINGERPRINT;
        	    	subItems[10] = " ";
    	    	}

    	    	if ((util.revision != null) && (util.revision.trim().length() > 0))
        	    	subItems[11] = "Firmware:\t" + util.firmware + "(" + util.revision + ")";
    	    	else
        	    	subItems[11] = "Version.Release:\t" + Build.VERSION.RELEASE;
    	    	
    	    	subItems[12] = util.runCmd("cat", "/proc/version")[0];
    			break;
    		}
    		case 2: {//camera
    			subItems = cameraSizes;
    			break;
    		}
    		case 3: {//location
    	        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    	        List ll = lm.getProviders(true);
    	    	for (int i = 0; i < ll.size(); i++) {
    	    		Location lo = lm.getLastKnownLocation((String) ll.get(i));
    	    		if (lo != null) {
    	    			subItems = new String[4];
    	    			subItems[0] = getString(R.string.latitude) + " " + lo.getLatitude();
    	    			subItems[1] = getString(R.string.longitude) + " " + lo.getLongitude();
    	    			subItems[2] = getString(R.string.altitude) + " " + lo.getAltitude();
    	    			subItems[3] = getGeo(lo);
    	    		    break;
    	    		}
    	    	}
    	    	if (subItems == null) {
    	    		subItems = new String[1];
    	    		subItems[0] = util.propertyContents[3];
    	    	}
    			break;
    		}
    		case 4: {//networks
   				ConnectivityManager cmg = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       			NetworkInfo[] nis = cmg.getAllNetworkInfo();
       			for (NetworkInfo ni : nis) 
       				if (ni.isConnectedOrConnecting()) {
       					if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
           	       			subItems = new String[7];
       	       				WifiManager mWifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
       	       			    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
       	       				subItems[0] = "SSID: " + wifiInfo.getSSID();
       	       				subItems[1] = "BSSID: " + wifiInfo.getBSSID();
       	       				subItems[2] = "Mac: " + wifiInfo.getMacAddress();
       	       				subItems[3] = "Supplicant State: " + wifiInfo.getSupplicantState().toString();
       	       				subItems[4] = "Rssi: " + wifiInfo.getRssi();
       	       				subItems[5] = "Link Speed: " + wifiInfo.getLinkSpeed() + WifiInfo.LINK_SPEED_UNITS;
       	       				subItems[6] = "Network Id: " + wifiInfo.getNetworkId();
       					}
       					else {
           	       			subItems = new String[6];
           	       		    subItems[0] = getString(R.string.ifname);
           	       			if (ni.toString().contains("ifname"))
           	       				subItems[0] += ni.toString().split("ifname:")[1].split(",")[0];
           	       			else
           	       				subItems[0] += "unknown"; 
           	       			
           	       		    subItems[1] = getString(R.string.aptype);
           	       			if (ni.toString().contains("aptype"))
           	       				subItems[1] += ni.toString().split("aptype:")[1].split(",")[0];
           	       			else
           	       				subItems[1] += "unknown"; 
           	       			subItems[2] = getString(R.string.extra) + ni.getExtraInfo();
           	       			subItems[3] = getString(R.string.roaming) + ni.isRoaming();
           	       			subItems[4] = getString(R.string.state) + ni.getDetailedState().name();
           	       			if (ni.getSubtypeName().length() == 0)
               	       			subItems[5] = getString(R.string.type) + ni.getTypeName();
           	       			else
               	       			subItems[5] = getString(R.string.type) + ni.getTypeName() + " [" + ni.getSubtypeName() + "]";
       					}
       					break;
       				}
    	    	if (subItems == null) {
    	    		subItems = new String[1];
    	    		subItems[0] = util.propertyContents[4];
    	    	}
    			break;
    			//subItems[3] = "Local Address: " + ;
    			//subItems[4] = "Public Address: " + ;
    		}
    		case 5: {//processor
    			subItems = processors;
    			break;
    		}
    		case 6: {//screen
    			subItems = new String[6];
    			subItems[0] = getString(R.string.density) + resolutions[2];
    			subItems[1] = "xdpi: " + resolutions[3];
    			subItems[2] = "ydpi: " + resolutions[4];
    			subItems[3] = getString(R.string.refreshRate) + resolutions[5];
    			subItems[4] = getString(R.string.pixelFormat) + resolutions[6];
    			if (glVender.startsWith("Android"))
        			subItems[5] = glVender + " Faked GPU";//not real GPU
    			else
        			subItems[5] = "GPU: " + glVender;
    			break;
    		}
    		case 7: {//sensors
    			List l = sensorMgr.getSensorList(Sensor.TYPE_ALL);
    			subItems = new String[l.size()];
    			for (int i = 0; i < l.size(); i++) {
    				Sensor ss = (Sensor) l.get(i);
    				subItems[i] = ss.getName() + " v(" + ss.getVersion() + "): " + ss.getPower() + "mA by " + ss.getVendor();
    			}
    			break;
    		}
    		case 8: {//storage
    			subItems = util.runCmd("df", "");
    			for (int i = 0; i < subItems.length; i++)
    				subItems[i] = subItems[i].split("\\(block")[0];
    			break;
    		}
    		case 9: {//telephony
    			subItems = teles;
    			break;
    		}
    		}
   			if (subItems != null) showMyDialog(subItems);
    	}
    };
    
    public String getGeo(Location lo) {
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(
                lo.getLatitude(), 
                lo.getLongitude(), 1);

            String add = "";
            if (addresses.size() > 0) 
            {
                for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); i++)
                   add += addresses.get(0).getAddressLine(i) + ", ";
                add = add.substring(0, add.length()-2);//remote the last comma and space
            }
            return add;
        }
        catch (IOException e) { 
        	return e.getMessage();
        }   
    }
    
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
        int tabWidth;
        if (fullversion) tabWidth = height / 6;//make sure display all 6 tabs
        else tabWidth = height / 4;
        
        resolutions = new String[7];
        resolutions[0] = width+"";
        resolutions[1] = height+"";
        resolutions[2] = metrics.density+"";
        resolutions[3] = metrics.xdpi+"";
        resolutions[4] = metrics.ydpi+"";
        util.resolution = resolutions[0] + "*" + resolutions[1];
        resolutions[5] = d.getRefreshRate()+"";
        
        switch (metrics.densityDpi) {
        case DisplayMetrics.DENSITY_LOW:
        	resolutions[2] += " (ldpi-" + metrics.densityDpi + ")";
        	break;
        case DisplayMetrics.DENSITY_MEDIUM:
        	resolutions[2] += " (mdpi-" + metrics.densityDpi + ")";
        	break;
        case DisplayMetrics.DENSITY_HIGH:
        	resolutions[2] += " (hdpi-" + metrics.densityDpi + ")";
        	break;
        case 320:
        	resolutions[2] += " (xdpi-" + metrics.densityDpi + ")";
        	break;
        default:
        	resolutions[2] += " (dpi-" + metrics.densityDpi + ")";
        }
        
        resolutions[6] = "";
        switch (d.getPixelFormat()) {
        case android.graphics.PixelFormat.A_8:
            resolutions[6] += "A_8";
            break;
        case android.graphics.PixelFormat.JPEG:
            resolutions[6] += "JPEG";
            break;
        case android.graphics.PixelFormat.L_8:
            resolutions[6] += "L_8";
            break;
        case android.graphics.PixelFormat.LA_88:
            resolutions[6] += "LA_88";
            break;
        case android.graphics.PixelFormat.OPAQUE:
            resolutions[6] += "OPAQUE";
            break;
        case android.graphics.PixelFormat.RGB_332:
            resolutions[6] += "RGB_332";
            break;
        case android.graphics.PixelFormat.RGB_565:
            resolutions[6] += "RGB_565";
            break;
        case android.graphics.PixelFormat.RGB_888:
            resolutions[6] += "RGB_888";
            break;
        case android.graphics.PixelFormat.RGBA_4444:
            resolutions[6] += "RGBA_4444";
            break;
        case android.graphics.PixelFormat.RGBA_5551:
            resolutions[6] += "RGBA_5551";
            break;
        case android.graphics.PixelFormat.RGBA_8888:
            resolutions[6] += "RGBA_8888";
            break;
        case android.graphics.PixelFormat.RGBX_8888:
            resolutions[6] += "RGBX_8888";
            break;
        case android.graphics.PixelFormat.TRANSLUCENT:
            resolutions[6] += "TRANSLUCENT";
            break;
        case android.graphics.PixelFormat.TRANSPARENT:
            resolutions[6] += "TRANSPARENT";
            break;
        case android.graphics.PixelFormat.YCbCr_420_SP:
            resolutions[6] += "YCbCr_420_SP";
            break;
        case android.graphics.PixelFormat.YCbCr_422_I:
            resolutions[6] += "YCbCr_422_I";
            break;
        case android.graphics.PixelFormat.YCbCr_422_SP:
            resolutions[6] += "YCbCr_422_SP";
            break;
        case android.graphics.PixelFormat.UNKNOWN:
            resolutions[6] += "UNKNOWN-" + d.getPixelFormat();
            break;
        default:
            resolutions[6] += "UNKNOWN-" + d.getPixelFormat();
        }
        return tabWidth;
    }
    
    OnAppClickListener mAppsCL = new OnAppClickListener();
    class OnAppClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Log.d("===============", arg0.toString());
			Log.d("===============", arg1.toString());
			Log.d("===============", "" + arg2);
			Log.d("===============", "" + arg3);
			ResolveInfo ri = (ResolveInfo) arg0.getItemAtPosition(arg2);
			if (ri.activityInfo.applicationInfo.packageName.equals(myPackageName)) return;//not start system info again.
			
			Intent i = new Intent(Intent.ACTION_MAIN);
			i.setComponent(new ComponentName(
					ri.activityInfo.applicationInfo.packageName,
					ri.activityInfo.name));
			i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);//not start a new activity but bring it to front if it already launched.
			try {
				startActivity(i);
			} catch(Exception e) {
				Toast.makeText(getBaseContext(), e.toString(), 3500).show();
			}
		}
    }
    
	OnBtnClickListener mBtnCL = new OnBtnClickListener();
	class OnBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			_vortexView.bringToFront();
			
			switch(currentTab) {
			case 0:
				btnBrief.setBackgroundColor(0xFFEEEEEE);
				break;
			case 1:
				btnSys.setBackgroundColor(0xFFEEEEEE);
				break;
			case 2:
				btnUser.setBackgroundColor(0xFFEEEEEE);
				break;
			case 3:
				btnWeb.setBackgroundColor(0xFFEEEEEE);
				break;
			}

			String text = (String) ((Button) v).getText();
			if (text.equals(getString(R.string.brief))) {
				btnBrief.setBackgroundColor(0xFFCCCCEE);
				properList.bringToFront();
				currentTab = 0;
			}
			else if (text.equals(getString(R.string.systemapps))) {
				btnSys.setBackgroundColor(0xFFCCCCEE);
				sysAppList.bringToFront();
				currentTab = 1;
			}
			else if (text.equals(getString(R.string.userapps))) {
				btnUser.setBackgroundColor(0xFFCCCCEE);
				userAppList.bringToFront();
				currentTab = 2;
			}
			else if (text.equals(getString(R.string.online))) {
				btnWeb.setBackgroundColor(0xFFCCCCEE);
				serverWeb.bringToFront();
				currentTab = 3;
			}
			
			mainlayout.invalidate();
		}
	}
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        myPackageName = this.getApplicationInfo().packageName;

        fullversion = false;
    	PackageManager pm = getPackageManager();

    	setContentView(R.layout.ads);
    	
        mainlayout = (FrameLayout)findViewById(R.id.mainFrame);
        
        propertyTitles = getResources().getTextArray(R.array.propertyItem);
        util.propertyContents = new String[propertyTitles.length];
        
        //brief tab
        properList = new ListView(this);
        properList.setFadingEdgeLength(0);//no shadow when scroll
        properList.inflate(this, R.layout.property_list , null);
        properList.setAdapter(new PropertyAdapter(this, util.propertyContents));
        properList.setOnItemClickListener(mpropertyCL);
        mainlayout.addView(properList);
        
		
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    	mAllApps = pm.queryIntentActivities(mainIntent, 0);
    	ArrayList mSysApps = new ArrayList();
    	ArrayList mUserApps = new ArrayList();
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
    	sysAppList.setAdapter(new ApplicationsAdapter(this, mSysApps));
    	sysAppList.setOnItemClickListener(mAppsCL);
        mainlayout.addView(sysAppList);
        
    	//user app tab
        userAppList = new ListView(this);
        userAppList.setFadingEdgeLength(0);//no shadow when scroll
        userAppList.inflate(this, R.layout.app_list, null);
        userAppList.setAdapter(new ApplicationsAdapter(this, mUserApps));
        userAppList.setOnItemClickListener(mAppsCL);
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
			public void onPageFinished(WebView view, String url) {
				//btnWeb.setVisibility(view.VISIBLE);
				//serverWeb.setVisibility(view.VISIBLE);
			}
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				btnWeb.setVisibility(view.INVISIBLE);
				serverWeb.setVisibility(view.INVISIBLE);
				if (currentTab == 3) {
					btnBrief.setBackgroundColor(0xFFCCCCCC);
					properList.bringToFront();
					currentTab = 0;
				}
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

        //just for get gl vender
        _vortexView = new VortexView(this);
        mainlayout.addView(_vortexView);
        
    	showDialog(0);
    	//refresh();
    	PageTask task = new PageTask();
		task.execute("");
		
		getResolution();
        properList.bringToFront();
        currentTab = 0;

        
        btnBrief = (Button) findViewById(R.id.btnBrief);
        btnBrief.setOnClickListener(mBtnCL);
        
        btnSys = (Button) findViewById(R.id.btnSystemApp);
        btnSys.setOnClickListener(mBtnCL);
        
        btnUser = (Button) findViewById(R.id.btnUserApp);
        btnUser.setOnClickListener(mBtnCL);
        
        btnWeb = (Button) findViewById(R.id.btnOnline);
		btnWeb.setOnClickListener(mBtnCL);

/*        tabHost = getTabHost();
        
        LayoutInflater.from(this).inflate(R.layout.main, tabHost.getTabContentView(), true);
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getString(R.string.brief))
                .setContent(R.id.PropertyList1));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(getString(R.string.online))
                .setContent(R.id.ViewServer));
        tabHost.addTab(tabHost.newTabSpec("tab3")
                .setIndicator(getString(R.string.systemapps))
                .setContent(R.id.sysapp));
        tabHost.addTab(tabHost.newTabSpec("tab4")
                .setIndicator(getString(R.string.userapps))
                .setContent(R.id.userapp));
        if (fullversion) {
            tabHost.addTab(tabHost.newTabSpec("tab4")
                    .setIndicator(getString(R.string.process))
                    .setContent(R.id.sViewProcess));
            tabHost.addTab(tabHost.newTabSpec("tab5")
                    .setIndicator(getString(R.string.service))
                    .setContent(R.id.sViewService));
            tabHost.addTab(tabHost.newTabSpec("tab6")
                    .setIndicator(getString(R.string.feature))
                    .setContent(R.id.sViewFeature));
            //tabHost.addTab(tabHost.newTabSpec("tab7")
            //        .setIndicator("icon")
            //        .setContent(R.id.appicontest));
            ProcessText = (TextView)findViewById(R.id.TextViewProcess);
            ServiceText = (TextView)findViewById(R.id.TextViewService);
            TaskText = (TextView)findViewById(R.id.TextViewFeature);
        }

        int tabWidth = getResolution(); 
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tabWidth, 40);
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++)
        	tabHost.getTabWidget().getChildAt(i).setLayoutParams(lp);*/
    }
    
    
    private class PropertyAdapter extends ArrayAdapter<String> {
        public PropertyAdapter(Context context, String[] propertyContents) {
            super(context, 0, propertyContents);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.property_list , parent, false);
            }
            
            if (position % 2 == 1)
            	convertView.setBackgroundColor(0xFFFFFFFF);
            else
            	convertView.setBackgroundColor(0xFFEEEEEE);
            
            final TextView textView1 = (TextView) convertView.findViewById(R.id.ItemTitle);	
            textView1.setText(propertyTitles[position]);

            final TextView textView2 = (TextView) convertView.findViewById(R.id.ItemText);	
            textView2.setText(util.propertyContents[position]);
            
            return convertView;
        }
    }

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
            	convertView.setBackgroundColor(0xFFFFFFFF);
            else
            	convertView.setBackgroundColor(0xFFEEEEEE);
            
            
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.appicon);
            PackageManager pm = getPackageManager();
            imageView.setImageDrawable(info.loadIcon(pm));

            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);	
            textView1.setText(info.loadLabel(pm));

            final TextView textView2 = (TextView) convertView.findViewById(R.id.appversion);	
            try {
				textView2.setText(pm.getPackageInfo(info.activityInfo.packageName, 0).versionName);
			} catch (NameNotFoundException e) {
				textView2.setText("unknown");
			}
            
            final TextView textView3 = (TextView) convertView.findViewById(R.id.appsource);
            String source = "";
            int color = 0xFF000000;//black for normal apk
            if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
            	source = info.activityInfo.applicationInfo.sourceDir + " (debugable) " + info.activityInfo.packageName;
            	color = 0xFFEECC77;//brown for debuggable apk
            }
            else if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
            	source = info.activityInfo.applicationInfo.sourceDir;//we can use source dir to remove it.
            }
            else {
            	source = info.activityInfo.packageName;//we can use package name to uninstall it.
            }
        	textView3.setText(source);
            
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        	List appList = am.getRunningAppProcesses();
        	for (int i = 0; i < appList.size(); i++) {
        		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
            	if ((info.activityInfo.processName.equals(as.processName)) && (!as.processName.equals(myPackageName))) {
            		color = 0xFFFF7777;//red for running apk
        			break;
        		}
        	}
        	textView1.setTextColor(color); 
            
            return convertView;
        }
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
    	util.sdkversion = android.os.Build.VERSION.SDK;
    	
    	cameraSizes = util.camera();
    	
    	util.dr();
    	
    	processors = util.processor();
    	
    	util.memtotal();
    	
    	sensorMgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	util.sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL).size() + "";
    	
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        teles = util.telephonies(tm);
        
        String url = getString(R.string.url);
        if (fullversion) url += "/fullversion.jsp";
		try {serverWeb.loadUrl(url);}
		catch (Exception e) {}
    }
    
    public void setPropList() {
        //properties sort by alphabet, the first is battery, add in onresume().
    	util.propertyContents[1] = "SDK version:" + util.sdkversion + "\tRELEASE:" + android.os.Build.VERSION.RELEASE;
    	util.propertyContents[2] = util.sCamera + getString(R.string.pixels);
    	util.propertyContents[5] = util.processor;
    	util.propertyContents[6] = util.resolution;
    	util.propertyContents[7] = util.sensors + " " + (String) propertyTitles[7];
    	util.propertyContents[8] = "ram: " + util.memtotal;
    	util.propertyContents[9] = "IMEI: " + util.imei;
    }
    
    public String refresh() {
		//network
		Enumeration<NetworkInterface> nets = null;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
			String tmp = "";
			for (NetworkInterface netIf : Collections.list(nets)) tmp += netIf.toString() + "\n";
			util.propertyContents[4] = tmp.trim();
		} catch (SocketException e) {
			util.propertyContents[4] = e.toString();
		}

        //location
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List ll = lm.getProviders(true);
        Boolean foundLoc = false;
    	for (int i = 0; i < ll.size(); i++) {
    		Location lo = lm.getLastKnownLocation((String) ll.get(i));
    		if (lo != null) {
    			util.propertyContents[3] = lo.getLatitude() + " : " + lo.getLongitude();
    			foundLoc = true;
    		    break;
    		}
    	}
    	if (!foundLoc) util.propertyContents[3] = getString(R.string.locationHint);
    	
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
