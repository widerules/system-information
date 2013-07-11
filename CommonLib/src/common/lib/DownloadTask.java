package common.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.widget.RemoteViews;
import base.lib.util;

@TargetApi(3)
public class DownloadTask extends AsyncTask<String, Integer, String> {
	public MyApp appstate;
	public int NOTIFICATION_ID;
	public String apkName = ""; // file name to download
	public boolean pauseDownload = false;// true to pause download
	
	private String URL_str; // source url
	private File download_file;
	private long total_read = 0; // downloaded size (in bytes)
	private int readLength = 0; // downloaded size each time(in bytes)
	private long apk_length = 0; // length of target(in bytes)
	private long skip_length = 0;// if found local file not download
	private long sizeM = 1024 * 1024;
	// finished last time, need continue to download
	private Notification notification;
	private int oldProgress;
	boolean stopDownload = false;// true to stop download
	boolean downloadFailed = false;

	@Override
	protected String doInBackground(String... params) {// download here
		URL_str = params[0]; // get download url
		if (URL_str.startsWith("file"))
			return URL_str;// not download local file
		apkName = params[1]; // get download file name
		if (apkName.contains("%"))
			// for some filename contain % will cause error
			apkName = apkName.split("%")[apkName.split("%").length - 1];

		notification = new Notification(
				android.R.drawable.stat_sys_download,
				appstate.mContext.getString(R.string.start_download),
				System.currentTimeMillis());

		Intent intent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(appstate.mContext, 0, intent, 0);
		notification.setLatestEventInfo(appstate.mContext, apkName,
				appstate.mContext.getString(R.string.start_download), contentIntent);
		appstate.nManager.notify(NOTIFICATION_ID, notification);

		// this intent is to pause/stop download
		intent.setAction("downloadControl");
		intent.putExtra("name", apkName);
		intent.putExtra("url", URL_str);
		// request_code will help to diff different thread
		contentIntent = PendingIntent.getActivity(appstate.mContext,
				NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(appstate.mContext, apkName,
				appstate.mContext.getString(R.string.downloading), contentIntent);

		FileOutputStream fos = null;
		InputStream is = null;
		URL url = null;
		try {
			url = new URL(URL_str);
			HttpURLConnection httpConnection = null;
			HttpClient httpClient = null;
			String contentDisposition = params[2];
			// Log.d("=============", URL_str + contentDisposition);
			if (URL_str.contains("?") || contentDisposition != null) {
				// need httpget
				httpClient = new DefaultHttpClient();
				HttpGet request = new HttpGet(URL_str);
				String cookies = CookieManager.getInstance().getCookie(
						URL_str);
				request.addHeader("Cookie", cookies);
				// Log.d("=============", cookies);
				HttpResponse response = httpClient.execute(request);
				is = response.getEntity().getContent();
				apk_length = response.getEntity().getContentLength();
				// Log.d("=============", apk_length+"");
				Header[] headers = response.getAllHeaders();
				for (int i = 0; i < headers.length; i++) {
					Header h = headers[i];
					// Log.d("===========", "Header names: "+h.getName() +
					// "  Value: "+h.getValue());
					if ("Content-Disposition".equals(h.getName())
							&& h.getValue().toLowerCase()
									.contains("filename")) {
						String value = URLDecoder.decode(h.getValue());
						apkName = value.split("=")[1].trim();
						if (apkName.startsWith("\""))
							apkName = apkName.substring(1);
						if (apkName.endsWith("\""))
							apkName = apkName.substring(0,
									apkName.length() - 1);
						if (apkName.contains("'"))
							apkName = apkName.split("'")[apkName.split("'").length - 1];// utf-8''CHUN%E5%85%89%E8%BC%9D%E8%8D%92%E9%87%8E.rar
						if (apkName.contains("?"))
							apkName = apkName.replace("?", "1");// ???????.doc
						notification.setLatestEventInfo(appstate.mContext, apkName,
								appstate.mContext.getString(R.string.downloading),
								contentIntent);
					}
				}
			} else {
				httpConnection = (HttpURLConnection) url.openConnection();
				apk_length = httpConnection.getContentLength(); // file size
				// need to
				// download
				is = httpConnection.getInputStream();
			}

			download_file = new File(appstate.downloadPath + apkName);
			// apkName.split(".")[1] will get java.lang.ArrayIndexOutOfBoundsException if apkName contain chinese character
			// MimeTypeMap.getFileExtensionFromUrl(apkName) will get null
			String ext = apkName.substring(apkName.lastIndexOf(".")+1, apkName.length());
			
			intent.setAction(Intent.ACTION_VIEW);
			MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
			String mimeType = mimeTypeMap.getMimeTypeFromExtension(ext);
			if (mimeType == null) mimeType = "";// must set a value to mimeType otherwise it will error when download finished
			if (!"".equals(mimeType)) 
				intent.setDataAndType(Uri.fromFile(download_file), mimeType);
			else
				intent.setData(Uri.fromFile(download_file));

			if (download_file.length() == apk_length) {
				// found local file with same name and length,
				// no need to download, just send intent to view it
				downloadSuccessRoutine(notification, apk_length, intent, download_file, NOTIFICATION_ID, mimeType, params[3]);
				appstate.downloadState.remove(NOTIFICATION_ID);
				return appstate.downloadPath + apkName;
			} else if (download_file.length() < apk_length) {
				// local file size < need to download,
				// need continue to download
				fos = new FileOutputStream(download_file, true);
				skip_length = download_file.length();
			} else
				// need overwrite
				fos = new FileOutputStream(download_file, false);

			notification.contentView = new RemoteViews(appstate.mContext.getPackageName(), R.layout.notification_dialog);
			notification.contentView.setProgressBar(R.id.progress_bar, 100,
					0, false);
			notification.contentView.setTextViewText(R.id.progress, "0%");
			notification.contentView.setTextViewText(R.id.title, apkName);
			appstate.nManager.notify(NOTIFICATION_ID, notification);

			total_read = 0; // init the downloaded size to 0

			byte buf[] = new byte[10240]; // download buffer. is that ok for 10240?
			readLength = 0;

			oldProgress = 0;
			// begin to download if read data stream success, and user do not stop download
			while (readLength != -1 && !stopDownload) {
				if (pauseDownload) {
					continue;
				}

				if ((readLength = is.read(buf)) > 0) {
					if (skip_length == 0)
						fos.write(buf, 0, readLength);
					else if (skip_length < readLength) {
						fos.write(buf, (int) skip_length,
								(int) (readLength - skip_length));
						skip_length = 0;
					} else
						skip_length -= readLength;// just read and skip, not
					// write if need skip

					total_read += readLength; // increase the download size
				}

				int progress = (int) ((total_read + 0.0) / apk_length * 100);
				if (oldProgress != progress) {// the device will get no response if update too often
					oldProgress = progress;
					notification.contentView.setProgressBar(
							R.id.progress_bar, 100, progress, false);// update download progress
					notification.contentView.setTextViewText(R.id.progress,
							progress + "%");
					appstate.nManager.notify(NOTIFICATION_ID, notification);
				}
			}
			// stop download by user. clear notification here for the
			// close() and shutdown() may be very slow
			if (stopDownload) {
				appstate.nManager.cancel(NOTIFICATION_ID);
				appstate.downloadState.remove(URL_str);
			}

			try { fos.close();
			} catch (IOException e1) {}

			try { is.close();
			} catch (IOException e1) {}

			if (httpConnection != null)
				httpConnection.disconnect();
			else
				httpClient.getConnectionManager().shutdown();

			if (!stopDownload) {// download success. change notification,
								// start package manager to install package
				downloadSuccessRoutine(notification, total_read, intent, download_file, NOTIFICATION_ID, mimeType, params[3]);
			}

		} catch (Exception e) {
			downloadFailed = true;
			notification.icon = android.R.drawable.stat_notify_error;
			
			intent.putExtra("errorMsg", e.toString());
			// request_code will help to diff different thread
			contentIntent = PendingIntent.getActivity(
					appstate.mContext,
					NOTIFICATION_ID, 
					intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setLatestEventInfo(
					appstate.mContext, 
					apkName,
					e.toString(), 
					contentIntent);
			appstate.nManager.notify(NOTIFICATION_ID, notification);

			// below line will cause error simetime, reported by emilio. so
			// commented it. may not a big issue to keep zero file.
			// if (download_file.length() == 0)
			// download_file.delete();//delete empty file
		}

		// remove download id whether download success nor fail, otherwise
		// can't download again on fail
		appstate.downloadState.remove(NOTIFICATION_ID);

		return null;
	}

	void downloadSuccessRoutine(Notification notification, long total_read, Intent intent, File download_file, int NOTIFICATION_ID, String mimeType, String openAfterDone) {
		notification.icon = android.R.drawable.stat_sys_download_done;

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
		df.setMaximumFractionDigits(2);
		String ssize = total_read + "B ";
		if (total_read > sizeM)
			ssize = df.format(total_read * 1.0 / sizeM) + "M ";
		else if (total_read > 1024)
			ssize = df.format(total_read * 1.0 / 1024) + "K ";
		
		PendingIntent contentIntent = PendingIntent.getActivity(appstate.mContext, 0, intent, 0);
		notification.contentView.setOnClickPendingIntent(
				R.id.notification_dialog, contentIntent);
		notification.setLatestEventInfo(appstate.mContext, download_file.getPath(), ssize
				+ appstate.mContext.getString(R.string.download_finish),
				contentIntent);// click listener for download
								// progress bar
		appstate.nManager.notify(NOTIFICATION_ID, notification);

		// change file property, for on some device the property is wrong
		try {
			Process p = Runtime.getRuntime().exec("chmod 644 " + download_file.getPath());
			try {
				p.waitFor();
			} catch (InterruptedException e) {}
		} catch (IOException e1) {}

		if (mimeType.startsWith("image")) {
			Intent intentAddPic = new Intent(
					"simpleHome.action.PIC_ADDED");
			intentAddPic.putExtra("picFile", download_file.getName());
			// add to picture list and enable change background by
			// shake
			appstate.mContext.sendBroadcast(intentAddPic);
		} 
		else if (mimeType.startsWith("application/vnd.android.package-archive")) {
			try {
				PackageInfo pi = appstate.mContext.getPackageManager()
						.getPackageArchiveInfo(appstate.downloadPath + download_file.getName(), 0);
				appstate.downloadAppID.put(pi.packageName, NOTIFICATION_ID);
			} catch (Exception e) {}

			// call system package manager to install app.
			// it will not return result code,
			// so not use startActivityForResult();
		}
		if ("yes".equals(openAfterDone)) util.startActivity(intent, false, appstate.mContext);// try to start some app to launch the download file
	}

}
