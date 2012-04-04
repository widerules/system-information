package easy.lib;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

import easy.lib.SimpleBrowser.DownloadTask;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class downloadControl extends Activity{
	static boolean mAdAvailable;
	static {
	       try {
	    	   wrapAdView.checkAvailable();
	           Class.forName("com.google.ads.AdView");
	    	   mAdAvailable = true;
	       } catch (Throwable t) {
	    	   mAdAvailable = false;
	       }
	   }
	wrapAdView adview;
	LinearLayout adContainer;
	
	
	Button btnPause, btnStop;
	boolean pause = false, stop = false, failed = false;
	TextView tv;
	MyApp appstate;
	int id;
	DownloadTask dlt;
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		init(intent);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.pause);

        boolean paid = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("paid", false);
        if (!paid && mAdAvailable) {
    		adview = new wrapAdView(this, 3, "a14f3f6bc126143");
    		adContainer = (LinearLayout) findViewById(R.id.adContainer);
    		adContainer.addView(adview.getInstance());
    		adview.loadAd();
        }

        init(getIntent());
	}
	
	private void clear() {
		NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nManager.cancel(id);
    	appstate.downloadState.remove(id);
	}
	
	private void init(Intent intent) {
        id = intent.getIntExtra("id", 0);
        
        appstate = (MyApp) getApplicationContext();
        dlt = appstate.downloadState.get(id);
        
        if (dlt != null) {
        	pause = dlt.pauseDownload;
        	stop = dlt.stopDownload;
        	failed = dlt.downloadFailed;
        }
        else {//the corresponding download state is deleted, so can't control it.
        	finish();
        	return;
        }
        
        tv = (TextView) findViewById(R.id.download_name);
        String hint = getString(R.string.download_hint) + "\n";
        String name = intent.getStringExtra("name") + "\n";
        String errorMsg = intent.getStringExtra("errorMsg"); 
        if (errorMsg != null) hint = errorMsg;
        else hint += name;
    	tv.setText(hint);
    	
        btnPause = (Button) findViewById(R.id.pause);
        btnStop = (Button) findViewById(R.id.stop);
        
		if (pause) 
			btnPause.setText(getString(R.string.resume));
		else if (failed)
			btnPause.setText(getString(R.string.retry));
		else 
			btnPause.setText(getString(R.string.pause));
		
		if (failed) {
			final String url = intent.getStringExtra("url");
	        btnPause.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {//start new task to download
					clear();
					Intent intent = new Intent("simpleHome.action.START_DOWNLOAD");
					intent.putExtra("url", url);
	                sendBroadcast(intent);
					finish();
				}
	        });

	        btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {//remove notification
					clear();
					finish();
				}
	        });
		}
		else {
	        btnPause.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (dlt != null) dlt.pauseDownload = !pause;
					finish();
				}
	        });

	        btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (dlt != null) dlt.stopDownload = !stop;
					finish();
				}
	        });
		}
	}
	
}