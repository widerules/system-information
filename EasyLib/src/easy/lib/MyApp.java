package easy.lib;

import java.io.File;
import java.util.HashMap;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import easy.lib.SimpleBrowser.DownloadTask;

public class MyApp extends Application {
	HashMap<Integer, DownloadTask> downloadState;

	@Override
	public void onCreate() {
		super.onCreate();
		downloadState = new HashMap<Integer, DownloadTask>();

		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
	}

	@Override
	public File getCacheDir() {// NOTE: this method is used in Android 2.2 and
								// higher
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		boolean cacheToSd = sp.getBoolean("cache_tosd", false);

		if (cacheToSd)
			return new File(util.preparePath(getBaseContext()) + "cache/");
		else
			return super.getCacheDir();
	}
}
