package sys.info.jtbuaa;

import java.io.IOException;
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

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
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

import com.paypal.android.MEP.*;

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
	List<ResolveInfo> mAllApps;
	WakeLock wakeLock;
	PayPal ppObj;
		
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
			ppObj = PayPal.getInstance();
			if (ppObj == null) {
				ppObj = PayPal.initWithAppID(this.getBaseContext(), "APP-0UH20368BU458643J", PayPal.ENV_LIVE);
				//if (ppObj == null) ppObj = PayPal.initWithAppID(this.getBaseContext(), "APP-80W284485P519543T", PayPal.ENV_SANDBOX);
			}

			Intent i = new Intent(this, About.class);
			i.putExtra("version", version);
			startActivity(i);
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
        tabHost.removeView(_vortexView);
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

    	    	if ((Properties.revision != null) && (Properties.revision.trim().length() > 0))
        	    	subItems[11] = "Firmware:\t" + Properties.firmware + "(" + Properties.revision + ")";
    	    	else
        	    	subItems[11] = "Version.Release:\t" + Build.VERSION.RELEASE;
    	    	
    	    	subItems[12] = Properties.runCmd("cat", "/proc/version")[0];
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
        int tabWidth = metrics.widthPixels / 6;//make sure display all 6 tabs
        
        int width, height;
        if (metrics.widthPixels > metrics.heightPixels) {
        	width = metrics.widthPixels;
        	height = metrics.heightPixels;
        }
        else {
        	height = metrics.widthPixels;
        	width = metrics.heightPixels;
        }
        resolutions = new String[7];
        resolutions[0] = width+"";
        resolutions[1] = height+"";
        resolutions[2] = metrics.density+"";
        resolutions[3] = metrics.xdpi+"";
        resolutions[4] = metrics.ydpi+"";
		Properties.resolution = resolutions[0] + "*" + resolutions[1];
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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        fullversion = true;
    	PackageManager pm = getPackageManager();

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
        	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        	mAllApps = pm.queryIntentActivities(mainIntent, 0);
        	Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name
        	
            appList.setAdapter(new ApplicationsAdapter(this, mAllApps));
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
    
    private class ApplicationsAdapter extends ArrayAdapter<ResolveInfo> {
        public ApplicationsAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = mAllApps.get(position);

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
            if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {//red for debugable
            	textView3.setText(info.activityInfo.applicationInfo.sourceDir + " (debugable) " + info.activityInfo.packageName);
            	textView1.setTextColor(0xFFFF7777);
            }
            else if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {//black for system
            	textView3.setText(info.activityInfo.applicationInfo.sourceDir);
            	textView1.setTextColor(0xFF000000);
            }
            else {//green for data
            	textView3.setText(info.activityInfo.applicationInfo.sourceDir);// + " (" + info.activityInfo.packageName + ")");
            	textView1.setTextColor(0xFF77CC77);
            }
            
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
    			Properties.setInfo((String) propertyItems[3], lo.getLatitude() + " : " + lo.getLongitude());
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
