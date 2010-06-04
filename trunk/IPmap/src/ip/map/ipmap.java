package ip.map;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ipmap extends MapActivity {
	protected class BluePoint extends Overlay {
		GeoPoint g;
		public BluePoint(GeoPoint geo) {
			super();
			g = geo;
		}
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if (shadow ==false) {
				Projection projection = mapView.getProjection();
				Point p = new Point();
				projection.toPixels(g, p);
				
				Paint paint = new Paint();
				paint.setAntiAlias(true);
				
				paint.setARGB(255, 0, 0, 0);
				canvas.drawCircle(p.x, p.y, 9, paint);
				
				paint.setARGB(255, 224, 224, 224);
				canvas.drawCircle(p.x, p.y, 8, paint);
				
				paint.setARGB(255, 128, 128, 192);
				canvas.drawCircle(p.x, p.y, 4, paint);
			}
			super.draw(canvas, mapView, shadow);
		}
	}
	
	MapView mapView; 
    MapController mc;
    GeoPoint p;
    
    EditText et;
    ProgressDialog m_dialog;
    AlertDialog m_msgDialog;
    
    final Handler mHandler = new Handler();
    
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
            showDialog(2);//show the geo result
            mapView.invalidate();
        }
    };
   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);      
        
        mapView = (MapView) findViewById(R.id.IPmap);
        mapView.setBuiltInZoomControls(true);
        mc = mapView.getController();
        mc.setZoom(17);
        
        et = (EditText)findViewById(R.id.IPadress);
        et.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN) 
						&& ((keyCode == android.view.KeyEvent.KEYCODE_SEARCH)
								|| (keyCode == android.view.KeyEvent.KEYCODE_ENTER))) 
					showOnMap(false);
				return false;
			}
		});
        
        showDialog(2);
        Location ml = getMyLocation();
        if (ml != null) {
            p = new GeoPoint(
    	            (int) (ml.getLatitude() * 1E6), 
    	            (int) (ml.getLongitude() * 1E6));
            mc.animateTo(p);//animate map to your own location
            setMark(p);
            
            DecimalFormat df = new DecimalFormat();
            df.setMinimumFractionDigits(3);
            df.setMaximumFractionDigits(3);
            String myLati = df.format(ml.getLatitude());
            String myLongti = df.format(ml.getLongitude());
            String myIP = getMyIP();
            m_msgDialog.setMessage(getString(R.string.help_text) 
            			+ getString(R.string.lati) + myLati  
            			+ getString(R.string.longti) + myLongti  
            			+ getString(R.string.ip) + myIP);
            m_msgDialog.show();//show the help info at first.
        }
        
    }

    private void setMark(GeoPoint p) {
        BluePoint mark = new BluePoint(p);
        List<Overlay> overlays = mapView.getOverlays();
        overlays.clear();
        overlays.add(mark);
    }
    
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0: {//wait dialog
            m_dialog = new ProgressDialog(this);
            m_dialog.setTitle(getTitle());
            m_dialog.setMessage(getString(R.string.wait_msg));
            m_dialog.setIndeterminate(true);
            m_dialog.setCancelable(true);
            return m_dialog;
        }
        case 1: {//about dialog
        	String version = "";
            PackageManager pm = getPackageManager();
            try {
            	PackageInfo pi = pm.getPackageInfo("ip.map", 0);
            	version = "v" + pi.versionName;
        	} catch (NameNotFoundException e) {
        		e.printStackTrace();
        	}    
        	return new AlertDialog.Builder(this).
        	setMessage(getString(R.string.app_name) + " " + version + "\n\n" 
        			+ getString(R.string.help_text) + " " + getString(R.string.help_text2) 
        			+ "\nhttp://www.hostip.info\n\n\njtbuaa@gmail.com").
        	setPositiveButton("Ok",
	          new DialogInterface.OnClickListener() {
	        	  public void onClick(DialogInterface dialog, int which) {}
	          }).create();
        }
        case 2: {//message dialog
        	if (m_msgDialog == null) {
        	m_msgDialog = new AlertDialog.Builder(this).
        	setMessage(getString(R.string.help_text)).
        	setPositiveButton("Ok",
	          new DialogInterface.OnClickListener() {
	        	  public void onClick(DialogInterface dialog, int which) {}
	          }).create();
        	Log.d("==================", "" + m_msgDialog);
        	}
            return m_msgDialog;
    	}
        }
        return null;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, 0, 0, getString(R.string.map));
    	menu.add(0, 1, 0, getString(R.string.street));
    	menu.add(0, 2, 0, getString(R.string.about));
    	//menu.add(0, 3, 0, getString(R.string.exit));//no need to add exit menu.
    	//menu.add(0, 4, 0, "search");
    	return true;
    }

	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case 0:
			showOnMap(false);
			return true;
		case 1://street view
			showOnMap(true);
			return true;
		case 2:
			showDialog(1);
			return true;
		case 3:
			finish();
			System.exit(0);
			return true;
		case 4:
			sendIntent("market://search?q=ip.map");
			return true;
		}
		return true;
	}

    private String ping(String host) {
    	String result = "error";
    	Log.d("================", host);
    	try {
        	String []cmds={"ping", "-w", "1", "-c", "1", host};  
        	Process proc = Runtime.getRuntime().exec(cmds);
    		String line = null;
    		BufferedReader br=new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while((line=br.readLine())!=null) {
            	//only get the ip in (), eg, PING www.l.google.com (66.249.89.104) 56(84) bytes of data.
        		result = line.split(" ")[2];
        		result = result.substring(1, result.length()-1);
                break;
            }
            if (result == "error") {
        		BufferedReader er=new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                while((line=er.readLine())!=null){
                	//if ((line == "ping: unknown host " + host) || (line == "connect: Invalid argument")) {
                	//Toast.makeText(getBaseContext(), line, Toast.LENGTH_SHORT).show();
                	if (m_msgDialog != null) m_msgDialog.setMessage(line);
                	break;
                }
    		}
    	} catch (IOException e) {
    		if (m_msgDialog != null) e.printStackTrace();
        	//Toast.makeText(getBaseContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        	m_msgDialog.setMessage(e.getLocalizedMessage());
    	}
		return result;
    }

    private String getGeo() {
    	String geo = "", result = "";
    	Log.d("===============", "getGeo");
        String host = et.getText().toString().trim();
        if ((host == null) || (host.equals(""))) host = getString(R.string.hint);//default host is google.
        String ip = host;
        
        if (!ip.matches("^[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}$")) {//why ping can't return sometime?
        	//if input host name instead of IP address, use ping to get IP address.
            ip = ping(host);
            if (ip == "error") {
            	return geo;
            }
        }
        
        //must stop use IP2Location after 20 try everyday, to save money for user.
        geo = getLocationFromIP2Location(ip);//not finish yet.
        if (geo.length() < 3) {
            result = httpGet("http://api.hostip.info/get_html.php?ip=" + ip + "&position=true");
            if (result.length() > 3) {
        	    String [] results = result.split("\n");
        	    String Latitude = results[3].split(":")[1].trim();
        	    String Longitude = results[4].split(":")[1].trim();
        	    geo = Latitude + "," + Longitude;
                if (m_msgDialog != null) m_msgDialog.setMessage(result);//sometime it display nothing?
            }
            else return geo;//cant get geo.
        }
        
	    if (geo.length() > 3) {
	        String coordinates[] = geo.split(",");
	        double lat = Double.parseDouble(coordinates[0]);
	        double lng = Double.parseDouble(coordinates[1]);
	 
	        p = new GeoPoint(
	            (int) (lat * 1E6), 
	            (int) (lng * 1E6));
	 
	        mc.animateTo(p);
	        setMark(p);
	        
	        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = geoCoder.getFromLocation(
                    p.getLatitudeE6()  / 1E6, 
                    p.getLongitudeE6() / 1E6, 1);

                String add = "";
                if (addresses.size() > 0) 
                {
                    for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
                         i++)
                       add += addresses.get(0).getAddressLine(i) + "\n";
                }
                //if (m_msgDialog != null) m_msgDialog.setMessage(result + "\n" + add);
                //Toast.makeText(getBaseContext(), result + add, Toast.LENGTH_SHORT).show();
            }
            catch (IOException e) {                
                e.printStackTrace();
                if (m_msgDialog != null) m_msgDialog.setMessage(e.getMessage());
		    	//Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }   
	    }
	    else {
            if (m_msgDialog != null) m_msgDialog.setMessage(result);
	    	//Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
	    }
    	return geo;
    }
    
    private Location getMyLocation() {
    	Location lo = null;
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        List ll = lm.getProviders(true);
        Log.d("============", "getProviders(true) return: " + ll.size());
    	for (int i = 0; i < ll.size(); i++) {
    		String name = (String) ll.get(i);
    		lo = lm.getLastKnownLocation(name);
    		if (lo != null) {
    			break;
    		}
    	}
    	return lo;
    }
    
    private String getMyIP() {
    	//http://www.droidnova.com/get-the-ip-address-of-your-device,304.html
    	//if use wifi, then can only get internal IP address instead of external IP address by local API.
    	//so get it from www.whatismyip.com
    	//they recommand to use curl "http://whatismyip.com/automation/n09230945NL.asp"
    	return httpGet("http://whatismyip.com/automation/n09230945NL.asp");
    }

    private String getLocationFromIP2Location(String ip) {
    	String Latitude = "", Longitude = "";
    	String result = "", name, value;
        String entity = httpGet("http://ws.fraudlabs.com/ip2locationwebservice.asmx/IP2Location?IP=" + ip + "&LICENSE=02-T34H-J97K");
        String []tmp = entity.split("\n");
        for (int i = 2; i < tmp.length-2; i++) {
        	if (tmp[i].indexOf("Not available for this package") == -1) {
        		String [] pair = tmp[i].split(">");
        		name = pair[0].trim().substring(1);
        		value = pair[1].split("<")[0];
        		result += name + ": " + value + "\n";
            	if (name.indexOf("LATITUDE") > -1) Latitude = value;
            	else if (name.indexOf("LONGITUDE") > -1) Longitude = value;
        	}
        }
        	
        if (m_msgDialog != null) m_msgDialog.setMessage(result);
        result = Latitude + "," + Longitude;
        Log.d("==============", result);
        return result;
    }

    private String httpPost(String uri, List<NameValuePair> nameValuePairs) {
    	String result = "";
    	HttpResponse response = null;
    	int statusCode = -1;
		try {
			HttpEntity entity = new UrlEncodedFormEntity(nameValuePairs);
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost(uri);
			request.setEntity(entity);
			response = client.execute(request);
			statusCode = (response == null ? -1 : response.getStatusLine().getStatusCode());
			switch (statusCode) {
			case 200:
			case 301:
			case 302:
				return httpGet(uri);
			default:;
                if (m_msgDialog != null) m_msgDialog.setMessage("sever return code: " + statusCode);
				//Toast.makeText(getBaseContext(), "(" + statusCode + ")", Toast.LENGTH_SHORT).show();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
            if (m_msgDialog != null) m_msgDialog.setMessage(e.getMessage());
	    	//Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
            if (m_msgDialog != null) m_msgDialog.setMessage(e.getMessage());
	    	//Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			e.printStackTrace();
            if (m_msgDialog != null) m_msgDialog.setMessage(e.getMessage());
	    	//Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
    	return result;
    }
    
    private String httpGet(String uri) {
        HttpParams params = new BasicHttpParams();
        HttpClient httpClient = new DefaultHttpClient(params);
        HttpGet httpGet = new HttpGet(uri); 
        try {
			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {  
				httpGet.abort();
	            if (m_msgDialog != null) m_msgDialog.setMessage(response.getStatusLine().getReasonPhrase());
				//Toast.makeText(getBaseContext(), response.getStatusLine().getReasonPhrase(), Toast.LENGTH_LONG).show();
			}
			else return EntityUtils.toString(response.getEntity());
		} catch (ClientProtocolException e) {
            if (m_msgDialog != null) m_msgDialog.setMessage(e.getMessage());
	    	//Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
            if (m_msgDialog != null) m_msgDialog.setMessage(e.getMessage());
	    	//Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return "";
    }
    
    private void sendIntent(String intent) {
	    Uri uri = android.net.Uri.parse(intent);
		Intent i = new Intent();
		i.setData(uri);
		i.setAction("android.intent.action.VIEW");
		try {
			startActivity(i);
        } catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}    	
    }
    
    private void showOnMap(final boolean showOnStreet) {
    	showDialog(0);
		new Thread(new Runnable() {
		    public void run() {
		    	Looper.prepare();
				String geo = getGeo();
	            if (m_dialog != null) m_dialog.dismiss();
			    if (geo.length() > 3) {
				    //sendIntent("geo:" + geo + "?z=17");//launch Google Map
				    if (showOnStreet) 
				    	sendIntent("google.streetview:cbll=" + geo + "&cbp=1,0,,0,5&mz=17");
			    }
			    mHandler.post(mUpdateResults);
			    Looper.loop();//this must put at the end of the thread, otherwise UI can't update, why?
		    }
		}).start();
    }
    
}