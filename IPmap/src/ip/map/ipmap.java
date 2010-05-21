package ip.map;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ipmap extends MapActivity {
	MapView mapView; 
    MapController mc;
    GeoPoint p;
    
    EditText et;
    ProgressDialog m_dialog;
    
    final Handler mHandler = new Handler();
    
    final Runnable mUpdateResults = new Runnable() {
        public void run() {
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
        
        //Log.d("==================", getMyIP());
        //Log.d("==================", getLocationFromWIMI("66.249.89.99"));
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0: {
            m_dialog = new ProgressDialog(this);
            m_dialog.setTitle(getTitle());
            m_dialog.setMessage(getString(R.string.wait_msg));
            m_dialog.setIndeterminate(true);
            m_dialog.setCancelable(true);
            return m_dialog;
        }
        case 1: {
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
        			+ getString(R.string.help_text) + "\nhttp://www.hostip.info\n\n\njtbuaa@gmail.com").
        	setPositiveButton("Ok",
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
                	Toast.makeText(getBaseContext(), line, Toast.LENGTH_SHORT).show();
                	break;
                }
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
        	Toast.makeText(getBaseContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    	}
		return result;
    }

    private String getGeo() {
    	String geo = "", result = "";
    	Log.d("===============", "getGeo");
        String host = et.getText().toString();
        if ((host == null) || (host.trim().equals(""))) host = "www.google.com";//default host is google.
        String ip = host;
        
        if (!ip.matches("^[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}$")) {
        	//if input host name instead of IP address, use ping to get IP address.
            ip = ping(host);
            if (ip == "error") {
            	return geo;
            }
        }
        
        result = httpGet("http://api.hostip.info/get_html.php?ip=" + ip + "&position=true");
        if (result.length() > 3) {
    	    String [] results = result.split("\n");
    	    String Latitude = results[3].split(":")[1].trim();
    	    String Longitude = results[4].split(":")[1].trim();
    	    geo = Latitude + "," + Longitude;
        }
        else return geo;//cant get geo.
    	Log.d("===============", geo);
        
	    if (geo.length() > 3) {
	        String coordinates[] = geo.split(",");
	        double lat = Double.parseDouble(coordinates[0]);
	        double lng = Double.parseDouble(coordinates[1]);
	 
	        p = new GeoPoint(
	            (int) (lat * 1E6), 
	            (int) (lng * 1E6));
	 
	        mc.animateTo(p);
	        mc.setZoom(17); 
	        
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
                Toast.makeText(getBaseContext(), result + add, Toast.LENGTH_SHORT).show();
            }
            catch (IOException e) {                
                e.printStackTrace();
		    	Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }   
	    }
	    else {
	    	Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
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

    private String getLocationFromWIMI(String ip) {
    	//can't work now
    	//so get it from www.whatismyip.com/tools/ip-address-lookup.asp or www.ip2location.com/demo.aspx
    	/*POST /tools/ip-address-lookup.asp HTTP/1.1
    	Host: www.whatismyip.com
    	User-Agent: Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.10) Gecko/2009042315 Firefox/3.0.10
    	Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*;q=0.8
    	Accept-Language: en-us,en;q=0.5
    	Accept-Encoding: gzip,deflate
    	Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7
    	Keep-Alive: 300
    	Connection: keep-alive
    	Referer: http://www.whatismyip.com/tools/ip-address-lookup.asp
    	Cookie: __utma=18138879.765344629.1273900707.1274164493.1274170372.6; __utmz=18138879.1274152767.3.3.utmcsr=forum.whatismyip.com|utmccn=(referral)|utmcmd=referral|utmcct=/showthread.php; __utmc=18138879; ASPSESSIONIDQQSCRQTD=NGMKONFALFPICIIBHDDFKCJP; ASPSESSIONIDQQTDRRTD=FBNKONFAAAEHEAMIJMBPDFAO; ASPSESSIONIDSADCCAQD=HGIFLBBDNJPFJKDJMILFHKPM; ASPSESSIONIDSADBACQC=EMDELBBDMAALHBCJNCELDFJB; ASPSESSIONIDSABBACRD=DMPDLBBDHHOACDDLJPDNLNAB; ASPSESSIONIDQACBACRC=JAIDLBBDJNLELAFNFJMAEJDK; ASPSESSIONIDQQACTRBC=LEKAPPADPLLPGLONNIEBBBMD; ASPSESSIONIDSQACSQAC=FLMAPPADIKEBIDOEJBHEDGCE; ASPSESSIONIDQSABTQBC=OELAPPADPEKFFJKNIODIBGKG; ASPSESSIONIDQQBCSQAC=BMHAPPADOJBIOADMDNHFFCME; ASPSESSIONIDSQAATQBC=LPIAPPADMPJACHPBDHJPCBHA; ASPSESSIONIDCSATSCCD=EBHOBHKAPLDKNHGAELPJMOPC; ASPSESSIONIDCSATSDCD=BCLBHFIAJNHFIAGOJBFPICDF; ASPSESSIONIDSQRDQQTD=FDPFGAIAGBANNGOMEAAAKFHG; ASPSESSIONIDQQRDQRSD=KCDIPBKAFABHBOEBAPHBHILE; ASPSESSIONIDSCBDQDTT=OINADEKAHGLLIPGHKNOBKJCN; ASPSESSIONIDQAADSDSS=MEFGMAGAHACIEINMCEPBMOKP; ASPSESSIONIDQACCTDTT=JGLEOFMAOOPJGDIONCIOIAEB; ASPSESSIONIDQSTARQSC=LJHKKGABEMLHBOOBIPGPIMIE; ASPSESSIONIDSQACTRBC=DHDKDKABFBOAKGDNODOKIEPP; ASPSESSIONIDQSCDSQBC=IBIKDKABJLCPNFECOIHFFAIA; ASPSESSIONIDQSABRTBC=APPBEKABCJCCGMLDNDGFEKFG; __utmb=18138879.11.10.1274170372; ASPSESSIONIDQQDCSRBC=BNNBEKABMDABAEOOCFJHGCCB; ASPSESSIONIDSSTDTTQD=ENHBNGABINHFEGNHFBMEALBD; ASPSESSIONIDSQRCQQSC=GLGCNGABFKEHEADGJLLEDFJE; ASPSESSIONIDSQRDQRSC=LLKKHICBKMOFONKBPAJHDACO; ASPSESSIONIDCSARTCCD=ONJNAMABCJBNLKPLPPKDNJMH; ASPSESSIONIDCSAQTCDD=AOCPAMABELBMFCAEDFJBLGLC; ASPSESSIONIDASCSSCCC=INNBBMABFLPNJGKPDLEBCAFF
    	Content-Type: application/x-www-form-urlencoded
    	Content-Length: 27

    	IP=122.200.68.247&GL=LookupHTTP/1.0 200 OK*/
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(11);
		nameValuePairs.add(new BasicNameValuePair("txtLookup", ip));
    	return httpPost("http://www.ip2location.com/demo.aspx", nameValuePairs);
		//nameValuePairs.add(new BasicNameValuePair("IP", ip));
		//nameValuePairs.add(new BasicNameValuePair("GL", "Lookup"));
    	//return httpPost("http://www.whatismyip.com/tools/ip-address-lookup.asp", nameValuePairs);
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
				Toast.makeText(getBaseContext(), "(" + statusCode + ")", Toast.LENGTH_SHORT).show();
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    	Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    	Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	    	Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
				Toast.makeText(getBaseContext(), response.getStatusLine().getReasonPhrase(), Toast.LENGTH_LONG).show();
			}
			else {
			    return EntityUtils.toString(response.getEntity());
			}
		} catch (ClientProtocolException e) {
	    	Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
	    	Toast.makeText(getBaseContext(), e.getMessage() , Toast.LENGTH_SHORT).show();
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
			    if (geo.length() > 3) {
				    //sendIntent("geo:" + geo + "?z=17");//launch Google Map
				    mHandler.post(mUpdateResults);
				    if (showOnStreet) 
				    	sendIntent("google.streetview:cbll=" + geo + "&cbp=1,0,,0,5&mz=17");
			    }
	            if (m_dialog != null) m_dialog.dismiss();
			    Looper.loop();//this must put at the end of the thread, otherwise the dialog can't dismiss. why?
		    }
		}).start();
    }
    
}