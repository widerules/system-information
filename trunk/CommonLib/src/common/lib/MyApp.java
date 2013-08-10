package common.lib;

import java.util.HashMap;
import java.util.Random;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import base.lib.BaseApp;
import base.lib.WebUtil;
import base.lib.util;

public class MyApp extends BaseApp {
	public SimpleBrowser mActivity;// the main activity

	// download related
	public Context mContext;
	public String downloadPath, dataPath;
	public NotificationManager nManager;
	public HashMap<String, Integer> downloadAppID = new HashMap<String, Integer>();
	public HashMap<String, DownloadTask> downloadState = new HashMap<String, DownloadTask>();

	void addDownloads(TitleUrl tu) {}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	public boolean startDownload(String url, String contentDisposition, String openAfterDone) {
		int posQ = url.indexOf("src=");
		if (posQ > 0) url = url.substring(posQ + 4);// get src part

		url = url.replace("%2D", "-");
        url = url.replace("%5F", "_");
        url = url.replace("%3F", "?");
        url = url.replace("%3D", "=");
        url = url.replace("%2E", ".");
        url = url.replace("%2F", "/");
        url = url.replace("%3A", ":");// replace %3A%2F%2F to :// if any for URLDecoder.decode(url) fail for some url, such as baidu tieba
		String apkName = WebUtil.getName(url);
		// image file from shuimu do not have ext. so we add it manually
		if (!apkName.contains(".")) {
			if (".jpg".equals(contentDisposition)) {
				apkName += ".jpg";
				contentDisposition = null;
			}
			else apkName += ".html";// if no ext, set as html file. maybe need consider contentDisposition.
		}
		else {// http://m.img.huxiu.com/portal/201304/18/171605dz0dp8yn0pu88zdy.jpg!278x80
			int index = apkName.lastIndexOf(".");
			String suffix = apkName.substring(index, apkName.length());
			if (".jpg".equals(contentDisposition) && suffix.startsWith(".jpg")) {
				apkName = apkName.replace(suffix, ".jpg");
			}
		}

		if (mActivity.noSdcard)
			Toast.makeText(mContext, R.string.sdcard_needed, Toast.LENGTH_LONG).show();

		DownloadTask dl = downloadState.get(url);
		if (dl != null) {
			// the file is downloading. show download control then.
			Intent intent = new Intent("downloadControl");
			intent.setClassName(mContext.getPackageName(), DownloadControl.class.getName());
			intent.putExtra("name", apkName);
			intent.putExtra("url", url);
			util.startActivity(intent, false, mContext);
			return true;
		}

		Random random = new Random();
		int id = random.nextInt() + 1000;

		DownloadTask dltask = new DownloadTask();
		dltask.appstate = this;
		dltask.NOTIFICATION_ID = id;
		downloadState.put(url, dltask);
		dltask.execute(url, apkName, contentDisposition, openAfterDone);
		return true;
	}
	
}
