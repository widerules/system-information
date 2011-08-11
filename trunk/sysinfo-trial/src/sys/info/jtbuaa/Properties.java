package sys.info.jtbuaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;

public class Properties {
	private static boolean loadFail = false;
	static {
		try {
            System.loadLibrary("ifprint");
		} catch(java.lang.UnsatisfiedLinkError e)
		{
			loadFail = true; 
		}
    }
	
	public native static String dmesg();
	public native static String armv7();
	
	
	static String processor, bogomips, hardware, memtotal="", resolution, dpi, sCamera, vendor, product, sensors = "", sdkversion, imei;
	static String revision, firmware;

	static ArrayList<HashMap<String, Object>> properListItem;
	public static void setInfo(String title, String text) {
        HashMap<String, Object> map = new HashMap<String, Object>();  
        map.put("ItemTitle", title);  
        map.put("ItemText", text);
        boolean found = false;
		for (int i = 0; i < properListItem.size(); i++)
			if (properListItem.get(i).containsValue(title)) {
				if (text != " ") properListItem.set(i, map);
				found = true;
				break;
			}
		if (!found) properListItem.add(map);
	}
	
	public static String sensors(SensorManager sensorMgr){
		String sensors = "";
    	List list = sensorMgr.getSensorList(Sensor.TYPE_ALL);
    	for (int i = 0; i < list.size(); i++) {
    		Sensor sensor = (Sensor) list.get(i);
    		sensors += sensor.getName() + " ";
    	}
    	return sensors;
	};
	
	public static String [] telephonies(TelephonyManager tm){
		imei = tm.getDeviceId();
		String [] result = new String[7];
		result[0] = "Network Operator: " + tm.getNetworkOperator();
		result[1] = "Operator Name: " + tm.getNetworkOperatorName();
		result[2] = "Country ISO: " + tm.getNetworkCountryIso();
		result[3] = "IMSI: " + tm.getSubscriberId();
		result[4] = "IMEI: " + imei;
		result[5] = "Line1 Number: " + tm.getLine1Number();
		result[6] = "Sim Serial Number: " + tm.getSimSerialNumber();
		//result[7] = "Device Software Version: " + tm.getDeviceSoftwareVersion();
		//result[8] = "Voice Mail alpha tag: " + tm.getVoiceMailAlphaTag();
		//result[9] = "Voice Mail Number: " + tm.getVoiceMailNumber();
		//result[10] = "callstate: " + tm.getCallState();
		//result[11] = "networkType: " + tm.getNetworkType();
		//result[12] = "phoneType: " + tm.getPhoneType();
		//result[13] = "dataState: " + tm.getDataState();
		return result;
	};
	
	private static Method mDebug_getSupportedPictureSizes;

	static {
		initCompatibility();
	};
	
	private static void initCompatibility() {
	    try {
	        Class c = Parameters.class;
	    	mDebug_getSupportedPictureSizes = c.getMethod("getSupportedPictureSizes");
	    	//below is from android doc but sames not work
	    	//mDebug_getSupportedPictureSizes = Debug.class.getMethod(
	        //        "getSupportedPictureSizes", new Class[] { Parameters.class } );
	    } catch (NoSuchMethodException nsme) {
	       Log.d("===================1", nsme.toString() );
	    }
	}
	
	public static void dr(){
   		vendor = android.os.Build.MODEL;
   		product = android.os.Build.PRODUCT;
   		firmware = "";
   		revision = "";
   		try {
   			Class c = Class.forName("android.os.SystemProperties");
   			Method mSystemProperties_get = c.getMethods()[1];//may cause error if SystemProperties add new method.
			vendor = (String) mSystemProperties_get.invoke(c, "apps.setting.product.vendor", vendor);
			product = (String) mSystemProperties_get.invoke(c, "apps.setting.product.model", product);
			firmware = (String) mSystemProperties_get.invoke(c, "apps.setting.platformversion", firmware);
			revision = (String) mSystemProperties_get.invoke(c, "ro.build.revision", revision);
		} catch (Exception e) {
			e.printStackTrace();
		}
	};
		
    private static Object getSupportedPictureSizes(Parameters param) throws IOException {
	    try {
		    return mDebug_getSupportedPictureSizes.invoke(param);
		} catch (InvocationTargetException ite) {
		    Throwable cause = ite.getCause();
	        Log.d("=================3", ite.toString() );
		} catch (IllegalAccessException ie) {
		    System.err.println("unexpected " + ie);
		}
		return null;
	}
		   
	public static String[] camera(){
		String[] sizes = null;
    	int maxWidth = 0 , maxHeight = 0;
	    try {
	    	Camera camera = Camera.open();
	    	Parameters param = camera.getParameters(); 
	    	camera.release();
	    	
	    	Size size = param.getPictureSize();
	    	maxWidth = size.width;
	    	maxHeight = size.height;
	    	
	    	if (mDebug_getSupportedPictureSizes != null) {
	    		List sl = (List) getSupportedPictureSizes(param);
	    		sizes = new String[sl.size()];
		    	for (int i = 0; i < sl.size(); i++) {
		    		Camera.Size cs = (Camera.Size)sl.get(i);
		    		sizes[i] = cs.width + "*" + cs.height;
		    		Log.d("===============", cs.width + "*" + cs.height);
		    		if (maxWidth < cs.width) {
		    			maxWidth = cs.width;
		    			maxHeight = cs.height;
		    		}
		    	}
	    	}
	    	else {
		    	sizes = new String[1];
		    	sizes[0] = maxWidth + "*" + maxHeight;
	    	}
	    } catch (Exception e) {e.printStackTrace();}
		sCamera = Integer.toString((int)Math.round(maxWidth * maxHeight / 1000000.0));
		return sizes;
	};
	
	public static String [] processor(){
		String [] result = readFile("/proc/cpuinfo");
		if (!loadFail) {
			String [] s = new String [result.length+2];//stupid?
			for (int i = 0; i < result.length; i++)	{
				s[i] = result[i];
			}
			s[result.length] = "";
			s[result.length+1] = armv7().trim();
			return s;
		}
		else return result;
	};
	
	public static String memtotal(){
		if (loadFail) memtotal = "";
		else memtotal = dmesg();
        int totalIndex = memtotal.indexOf("MB total"); 
    	if (totalIndex > -1) {
    		String []tmp = memtotal.substring(0, totalIndex).trim().split("=");
    		memtotal = tmp[tmp.length-1].trim() + "MB";
    	}
    	else readFile("/proc/meminfo");
    	return memtotal;
	};
	
    private static String [] readFile(String fileName) {
		String [] result = readFileByLine(fileName);
		if (result == null) return null;
		
		for (int i = 0; i < result.length; i++) {
    		if (result[i].indexOf(":") > -1) {
        		if (result[i].indexOf("BogoMIPS") > -1) {
        			bogomips = result[i].split(":")[1];
        		}
        		else if (result[i].indexOf("Hardware") > -1) {
        			hardware = result[i].split(":")[1];
        			break;
        		}
        		else if (result[i].indexOf("Processor") > -1) {
        			processor = result[i].split(":")[1];
        		}
        		else if (result[i].indexOf("model name") > -1) {
        			processor = result[i].split(":")[1];
        			break;
        		}
        		else if (result[i].indexOf("sdcard:") > -1) {
        			//result[1] = line.split(",")[0];
        			break;
        		}
        		else if (result[i].indexOf("Memory:") > -1) {
        			if (result[i].indexOf("=") > -1) { 
        				memtotal = result[i].split("=")[1].trim().split(" ")[0].trim();
        			}
        			break;
        		}
        		else if (result[i].indexOf("MemTotal:") > -1) {
		        	int imem = Integer.valueOf(result[i].split(":")[1].split("k")[0].trim()) / 1024;
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
        			break;
        		}
    		}
		}
		return result;
    }
    
    private static String [] readFileByLine(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) return null;
		
		try {
			return readBuffer(new BufferedReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
    		String[] result = new String[1];
    		result[0] = e.toString();
    		return result;
		}
    }
    
    static String [] runCmd(String cmd, String para) {//performance of runCmd is very low, may cause black screen. do not use it AFAC 
    	try {
        	String []cmds={cmd, para};  
    		java.lang.Process proc;
    		if (para != "")
    			proc = Runtime.getRuntime().exec(cmds);
    		else
    			proc = Runtime.getRuntime().exec(cmd);
    		return readBuffer(new BufferedReader(new InputStreamReader(proc.getInputStream())));
    	} catch (IOException e) {
    		String[] result = new String[1];
    		result[0] = e.toString();
    		return result;
    	}
    }

    static String [] readBuffer(BufferedReader br) {
		String line = null;
    	Vector vet = new Vector();
		try {
			while((line=br.readLine())!=null) vet.add(line);
			br.close();
		} catch (IOException e) {
    		String[] result = new String[1];
    		result[0] = e.toString();
    		return result;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
		    		e.printStackTrace();
				}
			}
		}
		
		String[] result = new String[vet.size()];
		for (int i = 0; i < vet.size(); i++) result[i] = (String) vet.get(i);
		return result;
    }
    

	static String[] Batterys;
	static String BatteryString;
	static CharSequence[] batteryHealth, batteryStatus;
	static BroadcastReceiver BroadcastReceiver = new BroadcastReceiver() {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            	Batterys = new String[7];
            	
            	//battery electrical amount, return by a number
            	Batterys[0] = intent.getIntExtra("level", 0) + "%";
            	
                //battery health status, return by a number  
                //BatteryManager.BATTERY_HEALTH_GOOD 
                //BatteryManager.BATTERY_HEALTH_OVERHEAT : over heat
                //BatteryManager.BATTERY_HEALTH_DEAD : no energy
                //BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE : over voltage
                //BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE : unknown error  
                //Log.d("Battery", "health " + intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN));
            	int healthState = intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN);
            	if (healthState > 0) healthState = healthState-1;//for intel pad, it will return 0 for the healthState.
            	setInfo(BatteryString, (String) batteryHealth[healthState] + "(" + intent.getIntExtra("level", 0) + "%)");
            	Batterys[1] = (String) batteryHealth[healthState];
            	
                //battery status, return by a number  
                // BatteryManager.BATTERY_STATUS_CHARGING : is charging  
                // BatteryManager.BATTERY_STATUS_DISCHARGING : is discharging
                // BatteryManager.BATTERY_STATUS_NOT_CHARGING : is not charging
                // BatteryManager.BATTERY_STATUS_FULL
            	int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
            	if (status > 0) status = status-1;//for intel pad
            	Batterys[2] = (String) batteryStatus[status];
                 
                //voltage of battery
            	Batterys[3] = intent.getIntExtra("voltage", 0) + "mV";

                //temperature of battery, unit is 0.1 degree. 197 means 19.7 CF.
            	Batterys[4] = intent.getIntExtra("temperature", 0)/10.0 + "";
                  
                //charging type. BatteryManager.BATTERY_PLUGGED_AC means charger. other value means USB  
            	int type = intent.getIntExtra("plugged", 0);
            	if (type == BatteryManager.BATTERY_PLUGGED_AC) Batterys[5] = "Charger";
            	else Batterys[5] = "USB";
            	
            	Batterys[6] = intent.getStringExtra("technology");
            }  
        }
	};

}
