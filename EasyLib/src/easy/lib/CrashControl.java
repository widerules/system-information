package easy.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import easy.lib.SimpleBrowser.DownloadTask;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CrashControl extends Activity {
	Button btnRetry, btnCancel;
	TextView tv;
	MyApp appstate;
	int id;
	boolean retry = false;
	
	NotificationManager nManager;

	// for information collection
	private Map<String, String> infos = new HashMap<String, String>();

	// format the date as part of log
	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

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

	private void init(Intent intent) {
		tv = (TextView) findViewById(R.id.download_name);
		btnRetry = (Button) findViewById(R.id.pause);
		btnCancel = (Button) findViewById(R.id.stop);
		
		id = intent.getIntExtra("id", 0);
		nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		final String errorMsg = intent.getStringExtra("errorMsg");
		
		tv.setText(getString(R.string.browser_name) + " " + getString(R.string.crashed) + "\n\n" + errorMsg);

		btnCancel.setText(getString(R.string.cancel));
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				nManager.cancel(id);// remove notification
				finish();
			}
		});

		retry = errorMsg.contains("WebViewDatabase");// should clear the database and retry if SQLite* exception
		
		if (retry) {// clear database and retry
			btnRetry.setText(getString(R.string.retry));
			btnRetry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {// start new task to download
					deleteDatabase("webview.db");
					
					Intent intent = new Intent("android.intent.action.MAIN");
					intent.setClassName(getPackageName(), "easy.lib.SimpleBrowser");
					util.startActivity(intent, false, CrashControl.this);
					nManager.cancel(id);// remove notification
					finish();
				}
			});

		} else {// send error log to author
			btnRetry.setText(getString(R.string.sendto));
			btnRetry.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// collect device info
					collectDeviceInfo(CrashControl.this);

					StringBuffer sb = new StringBuffer();
					for (Map.Entry<String, String> entry : infos.entrySet()) {
						String key = entry.getKey();
						String value = entry.getValue();
						sb.append(key + "=" + value + "\n");
					}
					sb.append(errorMsg);


					Intent intent = new Intent(Intent.ACTION_SENDTO);
					intent.setData(Uri.fromParts("mailto",
							getString(R.string.browser_author), null));
					intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback) + "\n\n\n\n\n====================\n" + sb.toString());
					intent.putExtra(Intent.EXTRA_SUBJECT, getPackageName()
							+ getString(R.string.sorry));
					if (!util.startActivity(intent, false, CrashControl.this)) {
						// save the error log
						final String path = saveCrashInfo2File(sb);
						// show toast
						new Thread() {
							@Override
							public void run() {
								Looper.prepare();
								Toast.makeText(
										CrashControl.this,
										getPackageName() + getString(R.string.sorry) + path,
										Toast.LENGTH_LONG).show();
								Looper.loop();
							}
						}.start();
					}
					
					nManager.cancel(id);// remove notification
					finish();
				}
			});
		}
	}
	/**
	 * collect device info
	 * 
	 * @param ctx
	 */
	public void collectDeviceInfo(Context ctx) {
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				Log.d(field.getName(), field.get(null).toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		infos.put("versionName", util.getVersion(ctx));
		infos.put("versionCode", util.getVersionCode(ctx));
	}

	/**
	 * save error log to file
	 * 
	 * @param ex
	 * @return filename
	 */
	private String saveCrashInfo2File(StringBuffer sb) {

		String path = util.preparePath(this) + "crash/";
		try {
			long timestamp = System.currentTimeMillis();
			String time = formatter.format(new Date());
			String fileName = "crash-" + time + "-" + timestamp + ".log";
			File dir = new File(path);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(path + fileName);
			fos.write(sb.toString().getBytes());
			fos.close();
			return path + fileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
