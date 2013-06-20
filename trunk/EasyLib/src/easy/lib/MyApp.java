package easy.lib;

import java.util.HashMap;

import base.lib.BaseApp;

import easy.lib.SimpleBrowser.DownloadTask;

public class MyApp extends BaseApp {
	HashMap<Integer, DownloadTask> downloadState;

	@Override
	public void onCreate() {
		super.onCreate();
		downloadState = new HashMap<Integer, DownloadTask>();
	}
}
