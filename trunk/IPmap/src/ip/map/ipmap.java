package ip.map;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
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
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class ipmap extends MapActivity implements AdListener{
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
    
    String errmsg;
    
	//ad
	AdView adview;
	AdRequest adRequest = new AdRequest();
	
	AlertDialog aboutDialog = null;
	View aboutView;
	Context mContext;

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
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
        setContentView(R.layout.main);      
        
        // Look up the AdView as a resource and load a request.
        adview = (AdView)this.findViewById(R.id.adView);
        //adRequest.addTestDevice("224902DD10187A82256A507A0007230D");

        mContext = this;
        
    	aboutView = getLayoutInflater().inflate(R.layout.about, null);
        TextView mailTo = (TextView) aboutView.findViewById(R.id.mailto);
        mailTo.setText(Html.fromHtml("<u>"+ getString(R.string.author) +"</u>"));
        mailTo.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.author), null));
    			try {
    				startActivity(intent);
    			} catch (Exception e) {
    				e.printStackTrace();
					AlertDialog dlg = new AlertDialog.Builder(mContext).
							setMessage(e.toString()).
							setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							}).
							create();
					dlg.show();
    			}
    		}
    	});

        mapView = (MapView) findViewById(R.id.IPmap);
        mapView.setBuiltInZoomControls(true);
        mc = mapView.getController();
        mc.setZoom(15);
        
        et = (EditText)findViewById(R.id.IPadress);
        et.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if ((event.getAction() == android.view.KeyEvent.ACTION_DOWN) 
						&& ((keyCode == android.view.KeyEvent.KEYCODE_SEARCH)
								|| (keyCode == android.view.KeyEvent.KEYCODE_ENTER))) { 
					showOnMap(false);
					adview.loadAd(adRequest);
				}
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
            			+ "\n\n" + getString(R.string.lati) + myLati  
            			+ "\n" + getString(R.string.longti) + myLongti  
            			+ "\n\n" + getString(R.string.ip) + myIP);
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
    		if (aboutDialog == null) {
    			String title = getString(R.string.app_name) + version;
    			aboutDialog = new AlertDialog.Builder(this).
    					setTitle(title).
    					setIcon(R.drawable.icon).
    					setView(aboutView).
    					setMessage(getString(R.string.help_text) + "\n" + getString(R.string.help_text2) + " http://www.geoiptool.com").
    					setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
    						@Override
    						public void onClick(DialogInterface dialog, int which) {
    						}
    					}).setPositiveButton("Ok",
    		      	          new DialogInterface.OnClickListener() {
    		      	        	  public void onClick(DialogInterface dialog, int which) {}
    		      	          }).create();
    		}
    		return aboutDialog;
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
                	errmsg = line;
                	break;
                }
    		}
    	} catch (IOException e) {
    		if (m_msgDialog != null) e.printStackTrace();
    		errmsg = e.getLocalizedMessage();
    	}
		return result;
    }

    private String getGeo() {
    	Log.d("===============", "getGeo");
        String host = et.getText().toString().trim();
        if ((host == null) || (host.equals(""))) host = getString(R.string.hint);//default host is google.
    	Log.d("===============", host);
        
        String[] geoResult = getLocationFromIPaddress(host);
	    if ((geoResult != null) && (!geoResult[0].equals(""))) {
	        double lat = Double.parseDouble(geoResult[0]);
	        double lng = Double.parseDouble(geoResult[1]);
	 
	        p = new GeoPoint(
	            (int) (lat * 1E6), 
	            (int) (lng * 1E6));
	 
	        mc.setZoom(mapView.getZoomLevel());
	        mc.animateTo(p);
	        setMark(p);
	    
	        String info = getString(R.string.lati) + geoResult[0] + "\n";
	        info += getString(R.string.longti) + geoResult[1] + "\n";
	        info += getString(R.string.country) + geoResult[2] + "\n";
	        info += getString(R.string.city) + geoResult[3];
	        	
	        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = geoCoder.getFromLocation(lat, lng, 1);

                String add = "";
                if (addresses.size() > 0) 
                {
                    for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
                         i++)
                       add += addresses.get(0).getAddressLine(i) + "\n";
                }
                info += "\n" + getString(R.string.address) + add;
            }
            catch (IOException e) {//just print log if geoservice error                
                e.printStackTrace();
            }
            
            if (m_msgDialog != null) m_msgDialog.setMessage(info);
    	    return geoResult[0]+","+geoResult[1]; 
	    }
	    
        if (m_msgDialog != null) {
        	if (errmsg.equals(""))
        	    m_msgDialog.setMessage(getString(R.string.unknownaddress));
        	else 
        	    m_msgDialog.setMessage(errmsg);
        }
        
	    return null;
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

    private String[] getLocationFromIPaddress(String host) {
        String entity = httpGet("http://www.geoiptool.com/en/?IP=" + host);
        if ((entity == null) || (entity.length() == 0)) return null;
        
        String []tmp = entity.split("\n");
        Log.d("==============", tmp.length+"");
        
        int latitudeLine = 0, longitudeLine = 0, countryLine = 0, cityLine = 0;
        for (int i = 0; i < tmp.length; i++) {
        	if (tmp[i].contains("Latitude:")) {
                //Log.d("=============="+i, tmp[i]);
                //Log.d("=============="+i+1, tmp[i+1]);
                latitudeLine = i+1;
        	}
        	else if (tmp[i].contains("Longitude:")) {
                //Log.d("=============="+i, tmp[i]);
                //Log.d("=============="+i+1, tmp[i+1]);
                longitudeLine = i+1;
        	}
        	else if (tmp[i].contains("Country:")) {
                //Log.d("=============="+i, tmp[i]);
                //Log.d("=============="+i+1, tmp[i+1]);
                countryLine = i+1;
        	}
        	else if (tmp[i].contains("City:")) {
                //Log.d("=============="+i, tmp[i]);
                //Log.d("=============="+i+1, tmp[i+1]);
                cityLine = i+1;
        	}
        }
        
    	String[] result = new String[4];
        if (latitudeLine > 0) {
            result[0] = tmp[latitudeLine].split(">")[1].split("<")[0];
            result[1] = tmp[longitudeLine].split(">")[1].split("<")[0];
            result[2] = tmp[countryLine].split(">")[2].split("<")[0];
            result[3] = tmp[cityLine].split(">")[1].split("<")[0];
        }
        else return null;
        
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
                errmsg = "sever return code: " + statusCode;
			}
        } catch (IllegalArgumentException e) {
			errmsg = e.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			errmsg = e.toString();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			errmsg = e.toString();
		} catch (IOException e) {
			e.printStackTrace();
			errmsg = e.toString();
		}
    	return result;
    }
    
    private String httpGet(String uri) {
        HttpParams params = new BasicHttpParams();
        HttpClient httpClient = new DefaultHttpClient(params);
        try {
            HttpGet httpGet = new HttpGet(uri); 
			HttpResponse response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {  
				httpGet.abort();
				errmsg = response.getStatusLine().getReasonPhrase();
			}
			else return EntityUtils.toString(response.getEntity());
        } catch (IllegalArgumentException e) {
			errmsg = e.toString();
		} catch (ClientProtocolException e) {
			errmsg = e.toString();
		} catch (IOException e) {
			errmsg = e.toString();
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
    	errmsg = "";
		new Thread(new Runnable() {
		    public void run() {
		    	Looper.prepare();
				String geo = getGeo();
	            if (m_dialog != null) m_dialog.dismiss();
			    if ((geo != null) && (geo.length() > 3)) {
				    //sendIntent("geo:" + geo + "?z=17");//launch Google Map
				    if (showOnStreet) 
				    	sendIntent("google.streetview:cbll=" + geo + "&cbp=1,0,,0,5&mz=17");
			    }
			    mHandler.post(mUpdateResults);
			    Looper.loop();//this must put at the end of the thread, otherwise UI can't update, why?
		    }
		}).start();
    }
    
    @Override
    public void onReceiveAd(Ad arg0) {
      Log.d("================jingtao", "Did Receive Ad");
    }

    @Override
    public void onFailedToReceiveAd(Ad arg0, ErrorCode errorCode) {
      Log.d("================jingtao", "failed to receive ad (" + errorCode + ")");
    }

	@Override
	public void onDismissScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLeaveApplication(Ad arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPresentScreen(Ad arg0) {
		// TODO Auto-generated method stub
		
	}
}