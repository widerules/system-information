package easy.lib;

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

public class downloadControl extends Activity {
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

		boolean paid = PreferenceManager.getDefaultSharedPreferences(this)
				.getBoolean("paid", false);
		if (!paid && mAdAvailable) {
			adview = new wrapAdView(this, 3, "a14f3f6bc126143", null);
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
		
		id = intent.getIntExtra("id", 0);
		nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		String errorMsg = intent.getStringExtra("errorMsg");
		if (errorMsg != null) {
			tv.setText(errorMsg);
			btnPause.setVisibility(View.INVISIBLE);
			btnStop.setText(getString(R.string.cancel));
			btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					nManager.cancel(id);// clear notification
					finish();
				}
			});

			return;
		}
		
		appstate = (MyApp) getApplicationContext();
		dlt = appstate.downloadState.get(id);

		if (dlt != null) {
			pause = dlt.pauseDownload;
			stop = dlt.stopDownload;
			failed = dlt.downloadFailed;
		} else {// the corresponding download state is deleted, so can't control it.
			finish();
			return;
		}

		tv.setText(getString(R.string.download_hint) + "\n" + intent.getStringExtra("name") + "\n");

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
				public void onClick(View arg0) {// start new task to download
					nManager.cancel(id);
					appstate.downloadState.remove(id);
					
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
					nManager.cancel(id);
					appstate.downloadState.remove(id);
					
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
					finish();
				}
			});
		}
	}
}
