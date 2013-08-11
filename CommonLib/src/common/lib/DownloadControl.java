package common.lib;

import base.lib.WrapAdView;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DownloadControl extends Activity {
	static boolean mAdAvailable;
	static {
		try {
			Class.forName("com.google.ads.AdView");
			mAdAvailable = true;
		} catch (Throwable t) {
			mAdAvailable = false;
		}
	}
	WrapAdView adview;
	LinearLayout adContainer;

	Button btnPause, btnStop;
	boolean pause = false, stop = false, failed = false;
	TextView tv;
	MyApp appstate;
	String url;
	DownloadTask dlt;
	int nid;
	
	NotificationManager nManager;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		init(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.pause);

		if (mAdAvailable) {
			adview = new WrapAdView(this, 3, "a1502880ce4208b", null);
			adContainer = (LinearLayout) findViewById(R.id.adContainer);
			adContainer.addView(adview.getInstance());
			adview.loadAd();
		}

		init(getIntent());
	}

	private void init(Intent intent) {
		tv = (TextView) findViewById(R.id.download_name);
		btnPause = (Button) findViewById(R.id.pause);
		btnStop = (Button) findViewById(R.id.stop);
		btnStop.setText(getString(R.string.stop));
		
		String errorMsg = intent.getStringExtra("errorMsg");
		url = intent.getStringExtra("url");
		nid = intent.getIntExtra("nid", -1);
		nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		appstate = (MyApp) getApplicationContext();
		if (url != null) dlt = appstate.downloadState.get(url);

		if (dlt != null) {
			pause = dlt.pauseDownload;
			stop = dlt.stopDownload;
			failed = dlt.downloadFailed;
			
			tv.setText(getString(R.string.download_hint) + "\n" + intent.getStringExtra("name") + "\n");
		} 
		else {
			if (errorMsg == null) errorMsg = "unknown error";
			Log.d("================", errorMsg);
			failed = true;
			pause = false;
			
			tv.setText(intent.getStringExtra("name") + " " + getString(R.string.download_fail) + "\n\n" + errorMsg + "\n");
		}

		if (pause)
			btnPause.setText(getString(R.string.resume));
		else if (failed)
			btnPause.setText(getString(R.string.retry));
		else
			btnPause.setText(getString(R.string.pause));

		if (failed) {
			btnPause.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {// start new task to download.
					removeNotification();
					
					Intent intent = new Intent(
							"simpleHome.action.START_DOWNLOAD");
					intent.putExtra("url", url);
					sendBroadcast(intent);
					finish();
				}
			});

			btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {// remove notification
					removeNotification();
					
					finish();
				}
			});
		} else {
			btnPause.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (dlt != null)
						dlt.pauseDownload = !pause;
					finish();
				}
			});

			btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (dlt != null)
						dlt.stopDownload = !stop;
					removeNotification();
					finish();
				}
			});
		}
	}
	
	void removeNotification() {
		try {
			nManager.cancel(nid);
			appstate.downloadState.remove(url);
			} 
		catch(Exception e) {e.printStackTrace();}
	}
}
