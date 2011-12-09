package simple.home.jtbuaa;

import java.util.HashMap;

import android.app.Application;

import simple.home.jtbuaa.SimpleBrowser.DownloadTask;

public class MyApp extends Application {
	HashMap<Integer, DownloadTask> downloadState;
	
	public MyApp() {
    	downloadState = new HashMap<Integer, DownloadTask>();
	}
}
