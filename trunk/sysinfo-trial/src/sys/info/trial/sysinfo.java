package sys.info.trial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class sysinfo extends TabActivity {
	static {
        System.loadLibrary("ifprint");
    }
	
	public native String dmesg();
	public native String ifprint();

	TextView resText;
	WebView serverWeb;
	ProgressDialog m_dialog;
	String version;
	Camera camera;
	TabHost tabHost;
	String processor, bogomips, hardware, memtotal="", resolution, dpi, sCamera, vendor, product, sensors = "", sdkversion, imei;
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
    	menu.add(0, 4, 0, getString(R.string.exit));
    	return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case 0:
			serverWeb.reload();
			tabHost.setCurrentTab(1);
			return true;
		case 1:
			upload();
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
		case 5:
            try {
           		FileWriter infoWriter = new FileWriter("/sdcard/sysinfo-trial.txt");
                infoWriter.write((String) resText.getText());
                infoWriter.close();
            } catch (FileNotFoundException e) {
                Log.d(getString(R.string.tag), "Couldn't find file sysinfo-trial.txt");
                return true;
            } catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return true;
	}

	void upload() {
		class SendData implements Runnable {
	        private String mUri;
	        private HttpEntity mEntity;

	        public SendData(String uri, HttpEntity entity) {
	            mUri = uri + "/sign";
	            mEntity = entity;
	        }
	        
	        public void run() {
	        	HttpResponse response = null;
	        	int statusCode = -1;
	    		try {
	    			HttpClient client = new DefaultHttpClient();
	    			HttpPost request = new HttpPost(mUri);
	    			request.setEntity(mEntity);
	    			response = client.execute(request);
	    			statusCode = (response == null ? -1 : response.getStatusLine().getStatusCode());
	    		} catch (Exception e) {e.printStackTrace();}
    			switch (statusCode) {
    			case 200:
    			case 301:
    			case 302:
    				serverWeb.reload();
					Looper.prepare();
					Toast.makeText(sysinfo.this, getString(R.string.ulsuccess), Toast.LENGTH_SHORT).show();
					Looper.loop();
    			default:;
					Looper.prepare();
					Toast.makeText(sysinfo.this, getString(R.string.ulfail) + "(" + statusCode + ")", Toast.LENGTH_SHORT).show();
					Looper.loop();
    			}
	        }
		}
		
		if (memtotal == "") {
			Toast.makeText(sysinfo.this, getString(R.string.memerr), Toast.LENGTH_SHORT).show();
			return;
		}
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
		nameValuePairs.add(new BasicNameValuePair("processor", processor));
		nameValuePairs.add(new BasicNameValuePair("bogomips", bogomips));
		nameValuePairs.add(new BasicNameValuePair("hardware", hardware));
		nameValuePairs.add(new BasicNameValuePair("memtotal", memtotal));
		nameValuePairs.add(new BasicNameValuePair("resolution", resolution));
		nameValuePairs.add(new BasicNameValuePair("camera", sCamera));
		nameValuePairs.add(new BasicNameValuePair("sensors", sensors));
		nameValuePairs.add(new BasicNameValuePair("vendor", vendor));
		nameValuePairs.add(new BasicNameValuePair("product", product));
		nameValuePairs.add(new BasicNameValuePair("sdkversion", sdkversion));
		nameValuePairs.add(new BasicNameValuePair("imei", imei));
		try {
			HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
			SendData s = new SendData(getString(R.string.url), entity);
	        Thread doSendData = new Thread(s);
	        doSendData.start();
		} catch (Exception e) {e.printStackTrace();}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig); //not restart activity each time screen orientation changes
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        float f = dm.xdpi;
        dpi = "dpi: " + (int)f + "\tscale-factor: " + dm.density + "\n";
        
        if (width > height) {
        	int tmp = width;
        	width = height;
        	height = tmp;
        }
        resolution = Integer.toString(width) + "*" + Integer.toString(height);
        
        tabHost = getTabHost();
        LayoutInflater.from(this).inflate(R.layout.main, tabHost.getTabContentView(), true);
        tabHost.addTab(tabHost.newTabSpec("tab1")
                .setIndicator(getString(R.string.brief))
                .setContent(R.id.sViewBrief));
        tabHost.addTab(tabHost.newTabSpec("tab2")
                .setIndicator(getString(R.string.online))
                .setContent(R.id.ViewServer));
        
        int tabHeight = 30, tabWidth = 80;
        if (width >= 480) {
        	tabHeight = 40;
        	tabWidth = 120;
        }
        tabHost.getTabWidget().getChildAt(0).setLayoutParams(new LinearLayout.LayoutParams(tabWidth, tabHeight));
        tabHost.getTabWidget().getChildAt(1).setLayoutParams(new LinearLayout.LayoutParams(tabWidth, tabHeight));

        serverWeb = (WebView)findViewById(R.id.ViewServer);
        serverWeb.getSettings().setJavaScriptEnabled(true);
        WebSettings webSettings = serverWeb.getSettings();
        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
        
        resText = (TextView)findViewById(R.id.TextViewBrief);
        
        PackageManager pm = getPackageManager();
        try {
        	PackageInfo pi = pm.getPackageInfo("sys.info.trial", 0);
        	version = "v" + pi.versionName;
        	List list = pm.getInstalledApplications(0);
        	nApk = Integer.toString(list.size());
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

    	resText.setText(refresh());
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
			resText.setText(myresult);
		}

		@Override
		protected void onPreExecute() {
			resText.setText("task started");
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			resText.setText(values[0]);
		}
	}

    public String runCmd(String cmd, String para) {
    	try {
        	String []cmds={cmd, para};  
    		Process proc;
    		if (para != "")
    			proc = Runtime.getRuntime().exec(cmds);
    		else
    			proc = Runtime.getRuntime().exec(cmd);
    		BufferedReader br=new BufferedReader(new InputStreamReader(proc.getInputStream()));
    		String result = null, line = null;
    		int count = 0;
    		
            while((line=br.readLine())!=null){
            	count = count + 1;
        		if (cmd == "getprop") {
        			result = line;
        			break;
        		}
        		else if (line.indexOf(":") > -1) {
            		if (line.indexOf("Processor") > -1) {
            			processor = line.split(":")[1];
            			result = getString(R.string.processor) + processor;
            		}
            		else if (line.indexOf("BogoMIPS") > -1) {
            			bogomips = line.split(":")[1];
            			result += "\nBogoMIPS:\t" + bogomips;
            		}
            		else if (line.indexOf("Hardware") > -1) {
            			hardware = line.split(":")[1];
            			result += "\n" + getString(R.string.hardware) + hardware;
            			break;
            		}
            		else if (line.indexOf("sdcard:") > -1) {
            			sdcard = line.split(",")[0];
            			result = sdcard;
            			break;
            		}
            		else if (line.indexOf("Memory:") > -1) {
            			if (line.indexOf("=") > -1) { 
            				memtotal = line.split("=")[1].trim().split(" ")[0].trim();
            				result = getString(R.string.memtotal) + memtotal;
            			}
            			break;
            		}
            		else if (line.indexOf("MemTotal:") > -1) {
            			Log.d(getString(R.string.tag), "memory2");
            			if (memtotal != null) { 
            				memtotal = line.split(":")[1].trim();
            				Log.d(getString(R.string.tag), "memtotal: " + memtotal);
        		        	int imem = Integer.valueOf(memtotal.split("k")[0].trim()) / 1024;
        		        	if (imem < 90) {}
        		        	else if (imem < 101) memtotal = "101MB";
        		        	else if (imem < 110) memtotal = "110MB";
        		        	else if (imem < 200) memtotal = "198MB";
        		        	else if (imem < 228) memtotal = "228MB";
        		        	else if (imem < 512) memtotal = "512MB";
        		        	else if (imem < 1024) memtotal = "1GB";
        		        	else if (imem < 2048) memtotal = "2GB";
        		        	else if (imem < 3072) memtotal = "3GB";
        		        	else if (imem < 4096) memtotal = "4GB";
            				result = getString(R.string.memtotal) + memtotal;
            			}
            			break;
            		}
        		}
            }
            if (cmd == "ps") return Integer.toString(count);
            else return result;
    	} catch (IOException e) {
    		e.printStackTrace();
    		return e.toString();
    	}
    }
 
    public String refresh() {
		String result = "";
		try {serverWeb.loadUrl(getString(R.string.url));}
		catch (Exception e) {}
		
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei = tm.getDeviceId();
      
        result = runCmd("cat", "/proc/cpuinfo") + "\n\n";
        
        String tmpmem = dmesg();
        int totalIndex = tmpmem.indexOf("MB total"); 
    	if (totalIndex > -1) {
    		String []tmp = tmpmem.substring(0, totalIndex).trim().split("=");
			memtotal = tmp[tmp.length-1].trim() + "MB";
			tmpmem = getString(R.string.memtotal) + memtotal;
    	}
    	else tmpmem = runCmd("cat", "/proc/meminfo");
        result += tmpmem + "\n";
        
        String tmpsdcard = runCmd("df", "");
        if (tmpsdcard != null) result += tmpsdcard + "\n\n";
        
        result += getString(R.string.resolution) + resolution + "\n";
        result += dpi;
        
        try {
        	camera = Camera.open();
        	Parameters param = camera.getParameters(); 
        	camera.release();
        	Size size = param.getPictureSize();
        	int maxWidth = size.width , maxHeight = size.height;
        	/*if (android.os.Build.VERSION.SDK_INT >= 5) {
            	List sl = param.getSupportedPictureSizes();
            	for (int i = 0; i < sl.size(); i++) {
            		Camera.Size cs = (Camera.Size)sl.get(i);
            		if (maxWidth < cs.width) {
            			maxWidth = cs.width;
            			maxHeight = cs.height;
            		}
            	}
        	}*/
    		sCamera = Integer.toString((int)Math.round(maxWidth * maxHeight / 1000000.0));
    		result += getString(R.string.camera) + sCamera + getString(R.string.pixels) + "\n";
        } catch (Exception e) {e.printStackTrace();}
        result += "\n";

        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List ll = lm.getAllProviders();
    	for (int i = 0; i < ll.size(); i++) {
    		Location lo = lm.getLastKnownLocation((String) ll.get(i));
    		if (lo != null) {
    		    result += getString(R.string.latitude) + lo.getLatitude() + "\n" + getString(R.string.longitude) + lo.getLongitude() + "\n\n";
    		    break;
    		}
    	}
    	
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if ((wifiInfo != null) && (wifiInfo.getMacAddress() != null))
            result += getString(R.string.wlan) + wifiInfo.getMacAddress() + "\n\n";
        
        result += getString(R.string.sensors) + "\n";
        sdkversion = android.os.Build.VERSION.SDK;
    	SensorManager sensorMgr = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
    	List list = sensorMgr.getSensorList(Sensor.TYPE_ALL);
    	for (int i = 0; i < list.size(); i++) {
    		Sensor sensor = (Sensor) list.get(i);
    		sensors += sensor.getName() + "\n";
    		result += "\t" + sensor.getName() + "\n";
    	}
    	result += "\n";
        
        result += getString(R.string.nProcess) + runCmd("ps", "") + "\n";//process number
        result += getString(R.string.nApk) + nApk + "\n\n";//apk number
        
        String dr = getPlatFormware();
         
       	if (dr != null) {
       		vendor = runCmd("getprop", "apps.setting.product.vendor");
       		product = runCmd("getprop", "apps.setting.product.model");
  	        result += getString(R.string.vendor) + vendor + " " + product;
       		result += " (dr" + dr + ")\n\n";
       	}
       	else {
       		vendor = android.os.Build.MODEL;
       		product = android.os.Build.PRODUCT;
   	        result += getString(R.string.vendor) + vendor + " " + product + "\n\n";
       	}
        
        result += getString(R.string.sdk) + sdkversion + "\tRELEASE: " + android.os.Build.VERSION.RELEASE;
        
        //result += ifprint();

        if (m_dialog != null) {
        	Log.d(getString(R.string.tag), m_dialog.toString());
        	m_dialog.cancel();
        }
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(400);
        return result;
    }
    
    private String getPlatFormware (){
        FileReader verReader;

        final File verFile = new File("/opl/etc/.build_version.xml");
        try {
                verReader = new FileReader(verFile);
        } catch (FileNotFoundException e) {
                Log.d(getString(R.string.tag), "Couldn't find or open version file " + verFile);
                return null;
             }
        try {
              XmlPullParser parser = Xml.newPullParser();
              parser.setInput(verReader);

              while (parser.getEventType() != parser.END_DOCUMENT) {
                 String name = parser.getName();
                 if ("device".equals(name)) {
                     return parser.getAttributeValue(null, "version");
                 }
                 else parser.next();
              }
        } catch (XmlPullParserException e) {
            Log.d(getString(R.string.tag), "Got execption parsing version resource.", e);
        } catch (IOException e) {
     		e.printStackTrace();
    	}
        return null;
     }
}
