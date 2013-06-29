package common.lib;

import java.util.HashMap;

import android.app.NotificationManager;
import android.content.Context;
import base.lib.BaseApp;

public class MyApp extends BaseApp {
	// download related
	public Context mContext;
	public String downloadPath;
	public NotificationManager nManager;
	public HashMap<String, Integer> downloadAppID;
	public HashMap<Integer, DownloadTask> downloadState;

	@Override
	public void onCreate() {
		super.onCreate();
		downloadAppID = new HashMap<String, Integer>();
		downloadState = new HashMap<Integer, DownloadTask>();
	}
}
