package simple.home.jtbuaa;

import simple.home.jtbuaa.simpleHome.DownloadTask;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class downloadControl extends Activity{
	Button btnPause, btnStop, btnReturn;
	boolean pause = false, stop = false, failed = false;
	TextView tv;
	Intent intent;
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
    	tv.setText(getString(R.string.download_hint) + intent.getStringExtra("name"));
    	
        btnPause = (Button) findViewById(R.id.pause);
        btnStop = (Button) findViewById(R.id.stop);
        btnReturn = (Button) findViewById(R.id.just_return);
        
		if (pause || failed) 
			btnPause.setText(getString(R.string.resume));
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

        btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
        });
	}
	
}
