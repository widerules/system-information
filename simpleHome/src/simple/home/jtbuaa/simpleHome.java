package simple.home.jtbuaa;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.android.internal.telephony.ITelephony;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.CallLog.Calls;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class simpleHome extends Activity implements OnGestureListener, OnTouchListener, SensorEventListener, sizedRelativeLayout.OnResizeChangeListener {

	//browser related
	ArrayList<MyWebview> serverWebs;
	int webIndex;
	ViewFlipper webpages;
	ImageView imgNext, imgPrev, imgShare, imgRefresh, imgNew;
	WebAdapter webAdapter;
	RelativeLayout webControl, webtools_center;
	TextView btnNewpage;
	InputMethodManager imm;
	
	//wall paper related
	SensorManager sensorMgr;
	Sensor mSensor;
	float last_x, last_y, last_z;
	long lastUpdate, lastSet;
	ArrayList<String> picList;
	CheckBox cbWallPaper;
	TextView mailto;
	View aboutView;
	AlertDialog m_aboutDialog;
	
	//app list related
	GridView favoAppList;
	ListView sysAppList, userAppList, shortAppList, webList;
	ImageView homeBar, shortBar;
	String version, myPackageName;
	ViewFlipper mainlayout;
	GestureDetector mGestureDetector;
	ResolveInfo appDetail;
	List<ResolveInfo> mAllApps;
	ArrayList<ResolveInfo> mFavoApps, mSysApps, mUserApps, mShortApps;
	private Button btnSys, btnUser, btnWeb;
	static int grayColor = 0xCCEEEEEE;
	static int whiteColor = 0xDDFFFFFF;
	Context mContext;
	PackageManager pm;
	favoAppAdapter favoAdapter;
	shortAppAdapter shortAdapter;
	ApplicationsAdapter sysAdapter, userAdapter;
	ResolveInfo ri_phone, ri_sms, ri_contact;
	CallObserver callObserver;
	SmsChangeObserver smsObserver;
	ImageView shortcut_phone, shortcut_sms, shortcut_contact;
	sizedRelativeLayout base;
	RelativeLayout shortcutBar, shortcutBar_center, adsParent;
    appHandler mAppHandler = new appHandler();
	final static int UPDATE_RI_PHONE = 0, UPDATE_RI_SMS = 1, UPDATE_RI_CONTACT = 2, UPDATE_USER = 3, UPDATE_PIC = 4; 
	AdView adview;
	
	String apkToDel, pkgToDel;
	boolean canRoot;
	
	ProgressDialog mProgressDialog;
	DisplayMetrics dm;
	String processor;
	
	//download related
	String downloadPath;
	NotificationManager nManager;
	ArrayList<packageIDpair> downloadAppID;
	MyApp appstate;
	
	//package size related
	HashMap<String, Object> packagesSize;
	Method getPackageSizeInfo;
	IPackageStatsObserver sizeObserver;
	static int sizeM = 1024*1024; 

	private static Method setPackage;
	
	static public com.android.internal.telephony.ITelephony getITelephony(TelephonyManager telMgr) throws Exception { 
	    Method getITelephonyMethod = telMgr.getClass().getDeclaredMethod("getITelephony"); 
	    getITelephonyMethod.setAccessible(true);//私有化函数也能使用 
	    return (com.android.internal.telephony.ITelephony)getITelephonyMethod.invoke(telMgr); 
	} 
	 

	class packageIDpair {
		String packageName;
		File downloadedfile;
		int notificationID;
		
		packageIDpair(String name, int id, File file) {
			packageName = name;
			notificationID = id;
			downloadedfile = file;
		}
	}
	
	class ricase {
		ResolveInfo mRi;
		int mCase;
		
		ricase(ResolveInfo ri, int thecase) {
			mRi = ri;
			mCase = thecase;
		}
	}
	ricase selected_case;
	
	class MyWebview extends WebView {

		public MyWebview(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
	        setScrollBarStyle(0);
	        //serverWeb.setOnTouchListener(this);
	        WebSettings webSettings = getSettings();
	        webSettings.setJavaScriptEnabled(true);
	        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	        webSettings.setSaveFormData(true);
	        webSettings.setTextSize(WebSettings.TextSize.SMALLER);
	        webSettings.setSupportZoom(true);
	        //webSettings.setDefaultZoom(ZoomDensity.MEDIUM);

	        setDownloadListener(new DownloadListener() {
				@Override
				public void onDownloadStart(String url, String ua, String contentDisposition,
						String mimetype, long contentLength) {
					startDownload(url);
				}
	        });
	        
	        setWebChromeClient(new WebChromeClient() {
	        	@Override
	        	public void onProgressChanged(WebView view, int progress) {
	        		if (mProgressDialog != null) {
	    				mProgressDialog.setProgress(progress);
	    				if (progress >= 50) {//50% is enough
	    					mProgressDialog.hide();
	    					mProgressDialog.setProgress(0);
	    				}
	        		}
	        	}
			});
	        
			setWebViewClient(new WebViewClient() {
				@Override
				public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
			        handler.proceed();//accept ssl certification when never needed.
				}
				
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					if (!url.equals("file:///android_asset/online.html")) showDialog(0);
					super.onPageStarted(view, url, favicon);
				}
				 
				@Override
				public void onPageFinished(WebView view, String url) {
					webAdapter.notifyDataSetChanged();
					//serverWeb.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
	        		if (mProgressDialog != null) {
	    				if (mProgressDialog.isShowing()) {
	    					mProgressDialog.hide();
	    					mProgressDialog.setProgress(0);
	    				}
	        		}
				}         
				
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if (!url.startsWith("http")) {
						Uri uri = Uri.parse(url);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						List<ResolveInfo> handleList = pm.queryIntentActivities(intent, 0);
						if (!handleList.isEmpty()) {
							startActivity(intent);
							return true;
						}
						else return false;
					}
					else return startDownload(url);
				}
			});
		}
	}
	
    private class WebAdapter extends ArrayAdapter<MyWebview> {
    	ArrayList localWeblist;
    	public WebAdapter(Context context, List<MyWebview> webs) {
            super(context, 0, webs);
            localWeblist = (ArrayList) webs;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final MyWebview wv = (MyWebview) localWeblist.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.web_list, parent, false);
            }

            final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.webicon);
            btnIcon.setImageBitmap(wv.getFavicon());
            
            TextView webname = (TextView) convertView.findViewById(R.id.webname);
            webname.setText(wv.getTitle());
            
            webname.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					webControl.setVisibility(View.INVISIBLE);
					webIndex = position;
					while (webpages.getDisplayedChild() != webIndex) webpages.showNext();
					webpages.getChildAt(webIndex).requestFocus();
				}
    		});
            
            ImageView btnStop = (ImageView) convertView.findViewById(R.id.webclose);
            btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					webControl.setVisibility(View.INVISIBLE);
					if (webAdapter.getCount() > 1) {
						((MyWebview) webpages.getChildAt(position)).destroy();
						webAdapter.remove((MyWebview) webpages.getChildAt(position));
						webpages.removeViewAt(position);
						imgNew.setImageBitmap(generatorCountIcon(getResIcon(getResources(), R.drawable.newpage), webAdapter.getCount(), 0));
						if (webIndex == webAdapter.getCount()) webIndex = webAdapter.getCount()-1;
					}
					else //return to home page if only one page when click close button
						serverWebs.get(webIndex).loadUrl("file:///android_asset/online.html");
					
					webpages.getChildAt(webIndex).requestFocus();
				}
            });
            
            return convertView;
        }
    }

    /** 
     * get bitmap from resource id 
     * @param res 
     * @param resId 
     * @return 
     */  
    private Bitmap getResIcon(Resources res,int resId){  
        Drawable icon=res.getDrawable(resId);  
        if(icon instanceof BitmapDrawable){  
            BitmapDrawable bd=(BitmapDrawable)icon;  
            return bd.getBitmap();  
        }else return null;  
    }  
    
    /** 
     * put number on gaven bitmap with blue color 
     * @param icon gaven bitmap
     * @return bitmap with count
     */  
    private Bitmap generatorCountIcon(Bitmap icon, int count, int scheme){  
        //初始化画布  
        int iconSize=(int)getResources().getDimension(android.R.dimen.app_icon_size);  
        Bitmap contactIcon=Bitmap.createBitmap(iconSize, iconSize, Config.ARGB_8888);  
        Canvas canvas=new Canvas(contactIcon);  
          
        //拷贝图片  
        Paint iconPaint=new Paint();  
        iconPaint.setDither(true);//防抖动  
        iconPaint.setFilterBitmap(true);//用来对Bitmap进行滤波处理，这样，当你选择Drawable时，会有抗锯齿的效果  
        Rect src=new Rect(0, 0, icon.getWidth(), icon.getHeight());  
        Rect dst=new Rect(0, 0, iconSize, iconSize);  
        canvas.drawBitmap(icon, src, dst, iconPaint);  
          
        //启用抗锯齿和使用设备的文本字距  
        Paint countPaint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.DEV_KERN_TEXT_FLAG);
        if (scheme == 0) {//for newpage icon
            countPaint.setColor(Color.BLACK);  
            countPaint.setTextSize(25f);  
            canvas.drawText(String.valueOf(count), iconSize/2-3, iconSize/2+13, countPaint);
        }
        else {//for miss call and unread sms
            countPaint.setColor(Color.DKGRAY);  
            countPaint.setTextSize(25f);  
            countPaint.setTypeface(Typeface.DEFAULT_BOLD);  
            canvas.drawText(String.valueOf(count), iconSize-30, 20, countPaint);
        }
        return contactIcon;  
    }  
    
    private void myStartActivity(Intent intent) {
		try {
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.toString(), 3500).show();
			e.printStackTrace();
		}
    }
    
	@Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0: {//progress dialog
        	if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMessage(getString(R.string.wait));
        	}
            return mProgressDialog;
    	}
        case 2: {//delete system app dialog
			return new AlertDialog.Builder(this).
			setTitle(getString(R.string.app_name) + " " + version).
			setIcon(R.drawable.error).
			setMessage(R.string.warning).
        	setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).
        	setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
        		@Override
        		public void onClick(DialogInterface dialog, int which) {//rm system app
					ShellInterface.doExec(new String[] {"mv " + apkToDel + " " + apkToDel + ".bak"}, false);
					Uri uri = Uri.fromParts("package", pkgToDel, null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					startActivityForResult(intent, 1);
	        	}
        	}).create();
        }
        }
        return null;
	}

	@Override
	protected void onResume() {
		if (!pkgToDel.equals("")) {
			String res = ShellInterface.doExec(new String[] {"ls /data/data/" + pkgToDel}, true);
			if (res.contains("No such file or directory")) {//uninstalled
				String[] cmds = {
						"rm " + apkToDel + ".bak",
						"rm " + apkToDel.replace(".apk", ".odex")};
				ShellInterface.doExec(cmds, false);
			}
			else ShellInterface.doExec(new String[] {"mv " + apkToDel + ".bak " + apkToDel}, false);
			
			pkgToDel = "";
		}
		
		super.onResume();
	}
	
    String ip() {
        //network
    	StringBuffer sb = new StringBuffer("");
		try {
			Enumeration<NetworkInterface> enumNI = NetworkInterface.getNetworkInterfaces();
			while (enumNI.hasMoreElements()) {
				NetworkInterface ni = enumNI.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress local = ips.nextElement();
					if (!local.isLoopbackAddress()) {
						sb.append(local.getHostAddress());
						break;
					}
				}
			}
			return sb.toString().trim();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
    
    String aboutMsg() {
		String res = getString(R.string.help_message)
		+ "\n\n" + getString(R.string.about_dialog_notes)
		+ "\n========================="
		+ "\n" + processor
		+ "\nAndroid " + android.os.Build.VERSION.RELEASE
		+ "\n" + dm.widthPixels+" * "+dm.heightPixels;
		
		String ipaddr = ip();
		if (!ipaddr.equals(""))
			res += "\n" + ipaddr;
		return res;
    }

    String runCmd(String cmd, String para) {//performance of runCmd is very low, may cause black screen. do not use it AFAC 
        String line = "";
        try {
            String []cmds={cmd, para};
            java.lang.Process proc;
            if (para != "")
                proc = Runtime.getRuntime().exec(cmds);
            else
                proc = Runtime.getRuntime().exec(cmd);
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            while((line=br.readLine())!=null) {
            	if ((line.contains("Processor")) || (line.contains("model name")) || (line.contains("MemTotal:"))) {
            		if (line.contains("processor	: 1")) continue;
            		line = line.split(":")[1].trim();
            		break;
            	}
            }
        	br.close();
        } catch (IOException e) {
            return e.toString();
        }
        return line;
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
    	menu.add(0, 0, 0, R.string.wallpaper).setIcon(android.R.drawable.ic_menu_gallery).setAlphabeticShortcut('W');
    	menu.add(0, 1, 0, R.string.settings).setIcon(android.R.drawable.ic_menu_preferences)
    		.setIntent(new Intent(android.provider.Settings.ACTION_SETTINGS));
    	menu.add(0, 2, 0, R.string.help).setIcon(android.R.drawable.ic_menu_help).setAlphabeticShortcut('H');
    	return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case 0://wallpaper
	        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
	        myStartActivity(Intent.createChooser(pickWallpaper, getString(R.string.wallpaper)));
			break;
		case 1://settings
			return super.onOptionsItemSelected(item);
		case 2://help dialog
        	if (m_aboutDialog == null) {
            	m_aboutDialog = new AlertDialog.Builder(this).
            	setTitle(getString(R.string.app_name) + " " + version).
            	setView(aboutView).
            	setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which) {}
            	}).
            	setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which) {//share simpleHome to friends
            	        String text = getString(R.string.sharetext); 
            	        if (getResources().getConfiguration().locale.getLanguage().equals("zh")) 
            	        	text += " http://yyh.co/168438 " + getString(R.string.sharetext2);
            	        else
	        				text += " https://market.android.com/details?id=simple.home.jtbuaa";
            	        
            	        Intent intent = new Intent(Intent.ACTION_SEND);
            	        intent.setType("text/plain");  
            	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
    	        		intent.putExtra(Intent.EXTRA_TEXT, text);
   	        			myStartActivity(Intent.createChooser(intent, getString(R.string.sharemode)));
            		}
            	}).create();
        	}
			m_aboutDialog.setMessage(aboutMsg());
			m_aboutDialog.show();
			break;
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		selected_case = (ricase) v.getTag();
		
		switch (selected_case.mCase) {
		case 0://on home
			menu.add(0, 0, 0, getString(R.string.removeFromFavo));
			break;
		case 1://on shortcut
			menu.add(0, 1, 0, getString(R.string.removeFromShort));
			break;
		case 2://on app list
			menu.add(0, 2, 0, getString(R.string.share));
			menu.add(0, 3, 0, getString(R.string.backapp));
			menu.add(0, 4, 0, getString(R.string.appdetail));
			menu.add(0, 5, 0, getString(R.string.addtoFavo));
			menu.add(0, 6, 0, getString(R.string.addtoShort));
			break;
		}
	}
	
	void writeFile(String name) {
		try {
			FileOutputStream fo = this.openFileOutput(name, 0);
			ObjectOutputStream oos = new ObjectOutputStream(fo);
			if (name.equals("short")) {
				for (int i = 0; i < mShortApps.size(); i++)
					oos.writeObject(((ResolveInfo)mShortApps.get(i)).activityInfo.name);
			}
			else if (name.equals("favo")) {
				for (int i = 0; i < mFavoApps.size(); i++)
					oos.writeObject(((ResolveInfo)mFavoApps.get(i)).activityInfo.name);
			}
			oos.flush();
			oos.close();
			fo.close();
		} catch (Exception e) {}
	}
	
	boolean backup(String sourceDir) {//copy file to sdcard
		String apk = sourceDir.split("/")[sourceDir.split("/").length-1];
		FileOutputStream fos;
		FileInputStream fis;
		String filename = downloadPath + apk;
		try {
			File target = new File(filename);
			fos = new FileOutputStream(target, false);
			fis = new FileInputStream(sourceDir);
			byte buf[] = new byte[10240];
			int readLength = 0;
        	while((readLength = fis.read(buf))>0){
       			fos.write(buf, 0, readLength);
        	}
			fos.close();
			fis.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	
	public boolean onContextItemSelected(MenuItem item){
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
		case 0://remove from home
			favoAdapter.remove(selected_case.mRi);
			writeFile("favo");
			break;
		case 1://remove from shortcut
			shortAdapter.remove(selected_case.mRi);
			writeFile("short");	//save shortcut to file
			break;
		case 2://app name share
	        Intent intent = new Intent(Intent.ACTION_SEND);
	        intent.setType("text/plain");  
	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
    		intent.putExtra(Intent.EXTRA_TEXT, selected_case.mRi.loadLabel(pm) + " " + getString(R.string.app_share_text));
   			myStartActivity(Intent.createChooser(intent, getString(R.string.sharemode)));
			break;
		case 3://backup app
			String sourceDir = selected_case.mRi.activityInfo.applicationInfo.sourceDir;
			String apk = sourceDir.split("/")[sourceDir.split("/").length-1];
			if (backup(sourceDir)) {
				Toast.makeText(this, 
						getString(R.string.backapp) + " " + getString(R.string.to) + " " + 
						downloadPath + apk, Toast.LENGTH_LONG).show();
				
				String odex = sourceDir.replace(".apk", ".odex");
				File target = new File(odex);
				if (target.exists()) backup(odex);//backup odex if any
			}
			break;
		case 4://get app detail info
			String source = selected_case.mRi.activityInfo.applicationInfo.sourceDir 
				+ "\n\n" 
				+ selected_case.mRi.activityInfo.packageName;
        	Toast.makeText(getBaseContext(), source, Toast.LENGTH_LONG).show();

			if (appDetail != null) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setClassName(appDetail.activityInfo.packageName, appDetail.activityInfo.name);
				intent.putExtra("pkg", selected_case.mRi.activityInfo.packageName);
				intent.putExtra("com.android.settings.ApplicationPkgName", selected_case.mRi.activityInfo.packageName);
			}
			else {//2.6 tahiti change the action.
				intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", selected_case.mRi.activityInfo.packageName, null));
			}
			myStartActivity(intent);
			break;
		case 5://add to home
			if (favoAdapter.getPosition(selected_case.mRi) < 0) { 
				favoAdapter.add(selected_case.mRi);
				writeFile("favo");
			}
			break;
		case 6://add to shortcut
			if (shortAdapter.getPosition(selected_case.mRi) < 0) {
				shortAdapter.insert(selected_case.mRi, 0);
				writeFile("short");	//save shortcut to file
			}
			break;
		}
		return false;
	}

	OnBtnClickListener mBtnCL = new OnBtnClickListener();
	class OnBtnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			AdRequest adRequest = new AdRequest();
			adview.loadAd(adRequest);
			
			int newTab = 0;

			String text = (String) ((Button) v).getText();
			if (text.equals(getString(R.string.systemapps))) newTab = 0;
			else if (text.equals(getString(R.string.userapps))) newTab = 1;
			else if (text.equals(getString(R.string.online))) newTab = 2;
			
			if (mainlayout.getDisplayedChild() != newTab) {
				switch(mainlayout.getDisplayedChild()) {
				case 0:
					btnSys.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				case 1:
					btnUser.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				case 2:
					btnWeb.setBackgroundResource(R.drawable.button_layout_unselected);
					break;
				}
				
				switch(newTab) {
				case 0:
					btnSys.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				case 1:
					btnUser.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				case 2:
					btnWeb.setBackgroundResource(R.drawable.button_layout_selected);
					break;
				}
				
				if (mainlayout.getDisplayedChild() > newTab) {//move to left if newTab is on the left
					/*// 设置切入动画
					mainlayout.setInAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), android.R.anim.slide_in_left));
		            // 设置切出动画
		            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), android.R.anim.slide_out_right));*/
					while(mainlayout.getDisplayedChild() != newTab)
						mainlayout.showPrevious();
				}
				else {//move to right if newTab is on the right
					/*// 设置切入动画
		            mainlayout.setInAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), R.anim.slide_in_right));
		            // 设置切出动画
		            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
		                    getApplicationContext(), R.anim.slide_out_left));*/
					while(mainlayout.getDisplayedChild() != newTab)
						mainlayout.showNext();
				}
			}
			if (mainlayout.getDisplayedChild() != 2) { 
				imm.hideSoftInputFromWindow(serverWebs.get(webIndex).getWindowToken(), 0);//hide input method
			}
			else webpages.getChildAt(webIndex).requestFocus();
		}
	}
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this.getBaseContext();
        myPackageName = this.getPackageName();
    	pm = getPackageManager();
    	PackageInfo pi = null;
        try {
        	pi = pm.getPackageInfo(myPackageName, 0);
        	version = "v" + pi.versionName;
    	} catch (NameNotFoundException e) {
    		e.printStackTrace();
    	}    

        try {
        	getPackageSizeInfo = PackageManager.class.getMethod(
        		    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    	sizeObserver = new IPackageStatsObserver.Stub() {
			@Override
			public void onGetStatsCompleted(PackageStats pStats,
					boolean succeeded) throws RemoteException {
				
				long size = pStats.codeSize + pStats.cacheSize + pStats.dataSize;
		        String ssize = new String();
		        if (size > 10 * sizeM) 		ssize = size / sizeM + "M";
		        else if (size > 10 * 1024)	ssize = size / 1024 + "K";
		        else if (size > 0)			ssize = size + "B";
		        else 						ssize = "";
				packagesSize.put(pStats.packageName, ssize);
			}
		};
		packagesSize = new HashMap<String, Object>();
    	
    	nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	downloadAppID = new ArrayList();
    	appstate = ((MyApp) getApplicationContext());
    	
    	sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
    	mSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		
		try {
			Field screenLayout = Configuration.class.getField("screenLayout");
	    	//1,2,3,4 are integer value of small, normal, large and XLARGE screen respectively.
	    	int screen_size = screenLayout.getInt(getResources().getConfiguration()) & Configuration.SCREENLAYOUT_SIZE_MASK; 
	    	if (screen_size < 3)//disable auto rotate screen for small and normal screen.
	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} catch (Exception e) {//no such field is sdk level <= 3
			e.printStackTrace();
		}
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
    	setContentView(R.layout.ads);
    	
        mainlayout = (ViewFlipper)findViewById(R.id.mainFrame);
        //mainlayout.setOnTouchListener(this);
        mainlayout.setLongClickable(true);
        mGestureDetector = new GestureDetector(this);
        
        adview = (AdView) this.findViewById(R.id.adView);
        
    	//favorite app tab
    	favoAppList = (GridView) findViewById(R.id.favos);
    	favoAppList.setVerticalScrollBarEnabled(false);
    	favoAppList.inflate(this, R.layout.app_list, null);
    	favoAppList.setFadingEdgeLength(0);//no shadow when scroll
    	favoAppList.setScrollingCacheEnabled(false);
    	//favoAppList.setOnTouchListener(this);
        
    	shortAppList = (ListView) findViewById(R.id.business);
    	shortAppList.bringToFront();
    	shortAppList.setVisibility(View.INVISIBLE);//why can't put it to the right of parent?
    	
    	//system app tab
    	sysAppList = (ListView) findViewById(R.id.sysapp);
    	sysAppList.inflate(this, R.layout.app_list, null);
    	sysAppList.setFadingEdgeLength(0);//no shadow when scroll
    	sysAppList.setScrollingCacheEnabled(false);
    	//sysAppList.setOnTouchListener(this);
        
    	//user app tab
    	userAppList = (ListView) findViewById(R.id.userapp);
        userAppList.inflate(this, R.layout.app_list, null);
        userAppList.setFadingEdgeLength(0);//no shadow when scroll
        userAppList.setScrollingCacheEnabled(false);
        //userAppList.setOnTouchListener(this);
        
        //online tab
        WebIconDatabase.getInstance().open(getDir("icons", MODE_PRIVATE).getPath());
        webIndex = 0;
        serverWebs = new ArrayList<MyWebview>();
        serverWebs.add(new MyWebview(this));
        webpages = (ViewFlipper) findViewById(R.id.webpages);
        webpages.addView(serverWebs.get(webIndex));
		
		webtools_center = (RelativeLayout) findViewById(R.id.webtools_center);
		dm = new DisplayMetrics();  
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		imgNext = (ImageView) findViewById(R.id.next);
		imgNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (serverWebs.get(webIndex).canGoForward()) serverWebs.get(webIndex).goForward();
			}
		});
		imgPrev = (ImageView) findViewById(R.id.prev);
		imgPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (serverWebs.get(webIndex).canGoBack()) serverWebs.get(webIndex).goBack();
			}
		});
		imgRefresh = (ImageView) findViewById(R.id.refresh);
		imgRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				serverWebs.get(webIndex).reload();
			}
		});
		imgShare = (ImageView) findViewById(R.id.share);
		imgShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
    	        Intent intent = new Intent(Intent.ACTION_SEND);
    	        intent.setType("text/plain");  
    	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
        		intent.putExtra(Intent.EXTRA_TEXT, serverWebs.get(webIndex).getTitle() + " " + serverWebs.get(webIndex).getUrl());
    	        myStartActivity(Intent.createChooser(intent, getString(R.string.sharemode)));
			}
		});
		imgNew = (ImageView) findViewById(R.id.newpage);
		imgNew.setImageBitmap(generatorCountIcon(getResIcon(getResources(), R.drawable.newpage), 1, 0));
		imgNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (webControl.getVisibility() == View.INVISIBLE) {
			        webControl.setVisibility(View.VISIBLE);
			        webControl.bringToFront();
				}
				else {
					webControl.setVisibility(View.INVISIBLE);
					webpages.getChildAt(webIndex).requestFocus();
				}
			}
		});
		
		//web control
		webControl = (RelativeLayout) findViewById(R.id.webcontrol);
		
		btnNewpage = (TextView) findViewById(R.id.opennewpage);
		btnNewpage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
		        webControl.setVisibility(View.INVISIBLE);
				webAdapter.add(new MyWebview(mContext));
				webIndex = webAdapter.getCount() - 1;
		        webpages.addView(webAdapter.getItem(webIndex));
		        while (webpages.getDisplayedChild() != webIndex) webpages.showNext();
		        serverWebs.get(webIndex).loadUrl("file:///android_asset/online.html");
				webpages.getChildAt(webIndex).requestFocus();
				imgNew.setImageBitmap(generatorCountIcon(getResIcon(getResources(), R.drawable.newpage), webAdapter.getCount(), 0));
			}
		});
    	//web list
		webAdapter = new WebAdapter(this, serverWebs);
    	webList = (ListView) findViewById(R.id.weblist);
    	webList.inflate(this, R.layout.web_list, null);
    	webList.setFadingEdgeLength(0);//no shadow when scroll
    	webList.setScrollingCacheEnabled(false);
    	webList.setAdapter(webAdapter);
    	
        btnSys = (Button) findViewById(R.id.btnSystemApp);
        btnSys.setOnClickListener(mBtnCL);
        
        btnUser = (Button) findViewById(R.id.btnUserApp);
        btnUser.setOnClickListener(mBtnCL);
        
        btnWeb = (Button) findViewById(R.id.btnOnline);
		btnWeb.setOnClickListener(mBtnCL);

		mSysApps = new ArrayList<ResolveInfo>();
		mUserApps = new ArrayList<ResolveInfo>();
		mFavoApps = new ArrayList<ResolveInfo>();
		mShortApps = new ArrayList<ResolveInfo>();
		favoAdapter = new favoAppAdapter(getBaseContext(), mFavoApps);
		favoAppList.setAdapter(favoAdapter);
		
        adsParent = (RelativeLayout) findViewById(R.id.adsParent);
        base = (sizedRelativeLayout) findViewById(R.id.base); 
        base.setResizeListener(this);
        base.setBackgroundDrawable(new ClippedDrawable(getWallpaper(), dm.widthPixels, dm.heightPixels));
        shortcutBar = (RelativeLayout) findViewById(R.id.shortcut_bar);
        shortcutBar_center = (RelativeLayout) findViewById(R.id.shortcut_bar_center);
        homeBar = (ImageView) findViewById(R.id.home_bar);
        homeBar.setOnClickListener(new OnClickListener() {//by click this bar to show/hide mainlayout
			@Override
			public void onClick(View arg0) {
				if (adsParent.getVisibility() == View.VISIBLE) {
					adsParent.setVisibility(View.INVISIBLE);
					favoAppList.setVisibility(View.VISIBLE);
					shortcutBar.setVisibility(View.VISIBLE);
					imm.hideSoftInputFromWindow(serverWebs.get(webIndex).getWindowToken(), 0);//hide input method
					if (mProgressDialog != null) mProgressDialog.hide();
				}
				else {
					adsParent.setVisibility(View.VISIBLE);
					favoAppList.setVisibility(View.INVISIBLE);
					if (shortAppList.getVisibility() == View.VISIBLE) shortBar.performClick();
					shortcutBar.setVisibility(View.INVISIBLE);
				}
			}
        });
        
        shortBar = (ImageView) findViewById(R.id.business_bar);
        shortBar.setOnClickListener(new OnClickListener() {//by click this bar to show/hide mainlayout
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if ((shortAppList.getVisibility() == View.INVISIBLE) && !mShortApps.isEmpty()) {
					shortAppList.setVisibility(View.VISIBLE);
				}
				else {
					shortAppList.setVisibility(View.INVISIBLE);
				}
			}
        });
		setLayout(dm.widthPixels);
        
		shortcut_phone = (ImageView) findViewById(R.id.shortcut_phone);
		shortcut_sms = (ImageView) findViewById(R.id.shortcut_sms);
		//shortcut_contact = (ImageView) findViewById(R.id.shortcut_contact);
		
		//for package add/remove
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		registerReceiver(packageReceiver, filter);
		
		//for wall paper changed
		filter = new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED);
		registerReceiver(wallpaperReceiver, filter);
		
		filter = new IntentFilter("simpleHome.action.HOME_CHANGED");
        registerReceiver(homeChangeReceiver, filter);

		filter = new IntentFilter("simpleHome.action.START_DOWNLOAD");
        registerReceiver(downloadReceiver, filter);
        
		LayoutInflater inflater = LayoutInflater.from(this);
		final simpleHome sensorListener = this;
		
		aboutView = inflater.inflate(R.layout.about, null);
		mailto = (TextView) aboutView.findViewById(R.id.mailto);
		mailto.setText(Html.fromHtml("<u>"+ getString(R.string.author) +"</u>"));
		mailto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "jtbuaa@gmail.com", null));
				List<ResolveInfo> handleList = pm.queryIntentActivities(intent, 0);
				if (!handleList.isEmpty()) {
					startActivity(intent);
					m_aboutDialog.cancel();
				}
			}
		});
		
    	cbWallPaper = (CheckBox) aboutView.findViewById(R.id.change_wallpaper);
    	cbWallPaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (cbWallPaper.isChecked()) {
			    	sensorMgr.registerListener(sensorListener, mSensor, SensorManager.SENSOR_DELAY_UI);
				}
				else {
					sensorMgr.unregisterListener(sensorListener);
				}
			}
    	});

    	//TelephonyManager tm = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
    	//getITelephony(tm).getActivePhoneType();
    	
    	ContentResolver cr = getContentResolver();
    	callObserver = new CallObserver(cr, mAppHandler);
    	smsObserver = new SmsChangeObserver(cr, mAppHandler);
    	getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, callObserver);
    	cr.registerContentObserver(Uri.parse("content://mms-sms/"), true, smsObserver);
    		
		pkgToDel = "";
    	//task for init, such as load webview, load package list
		InitTask initTask = new InitTask();
        initTask.execute("");
    }
    
    @Override 
    protected void onDestroy() {
    	unregisterReceiver(packageReceiver);
    	unregisterReceiver(wallpaperReceiver);
    	unregisterReceiver(homeChangeReceiver);
    	unregisterReceiver(downloadReceiver);
    	
    	super.onDestroy();
    }
    
	BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			startDownload(intent.getStringExtra("url"));
		}
	};
	
	BroadcastReceiver wallpaperReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (!cbWallPaper.isChecked())
				base.setBackgroundDrawable(new ClippedDrawable(getWallpaper(), base.getWidth(), base.getHeight()));
		}
	};
	
    BroadcastReceiver homeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String homeName = intent.getStringExtra("old_home");
            if(homeName.equals(myPackageName)) finish();
        }
    };

	BroadcastReceiver packageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
            String action = intent.getAction();
            String packageName = intent.getDataString().split(":")[1];//it always in the format of package:x.y.z
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            	boolean found = false;
        		for (int i = 0; i < userAdapter.getCount(); i++) {
        			ResolveInfo info = userAdapter.getItem(i);
        			if (info.activityInfo.packageName.equals(packageName)) {
        				userAdapter.remove(info);
        				found = true;
        				break;
        			}
        		}
            	if (!found) {
            		for (int i = 0; i < sysAdapter.getCount(); i++) {
            			ResolveInfo info = sysAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				sysAdapter.remove(info);
            				break;
            			}
            		}
            	}
            	if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {//not remove shortcut if it is just replace 
            		for (int i = 0; i < favoAdapter.getCount(); i++) {
            			ResolveInfo info = favoAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				favoAdapter.remove(info);
            				writeFile("favo");
            				break;
            			}
            		}
            		for (int i = 0; i < shortAdapter.getCount(); i++) {
            			ResolveInfo info = shortAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				shortAdapter.remove(info);
            				writeFile("short");
            				break;
            			}
            		}
            	}
            }
            else if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
	    		try {//get size of new installed package
					getPackageSizeInfo.invoke(pm, packageName, sizeObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}

            	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            	if (setPackage != null) {
            		try {
						setPackage.invoke(mainIntent, packageName);
					} catch (Exception e) {
						e.printStackTrace();
					}
            	}
            	List<ResolveInfo> targetApps = pm.queryIntentActivities(mainIntent, 0);

            	for (int i = 0; i < targetApps.size(); i++) {
                	if (targetApps.get(i).activityInfo.packageName.equals(packageName) ) {//the new package may not support Launcher category, we will omit it.
                    	if ((targetApps.get(i).activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
            				sysAdapter.add(targetApps.get(0));
            		    	Collections.sort(sysAdapter.localApplist, new ResolveInfo.DisplayNameComparator(pm));//sort by name
            		    	break;
                    	}
                    	else {
            				userAdapter.add(targetApps.get(0));
            		    	Collections.sort(userAdapter.localApplist, new ResolveInfo.DisplayNameComparator(pm));//sort by name
            		    	break;
                    	}
                	}
            	}
            	
            	for (int i = 0; i < downloadAppID.size(); i++) {//cancel download notification if install succeed
            		if (downloadAppID.get(i).packageName.startsWith(packageName.toLowerCase()))
            		{
                		nManager.cancel(downloadAppID.get(i).notificationID);
                		try {
                			downloadAppID.get(i).downloadedfile.delete();
                		} catch(Exception e) {};
                		downloadAppID.remove(i);
                		break;
            		}
            	}
            }
		}
		
	};

	void startApp(ResolveInfo info) {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setComponent(new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name));
		i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);//not start a new activity but bring it to front if it already launched.
		myStartActivity(i);
	}
	
    private class ApplicationsAdapter extends ArrayAdapter<ResolveInfo> {
    	ArrayList localApplist;
        public ApplicationsAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = (ResolveInfo) localApplist.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.app_list, parent, false);
            }

            if (position % 2 == 1)
            	convertView.setBackgroundColor(whiteColor);
            else
            	convertView.setBackgroundColor(grayColor);
            
            final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.appicon);
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
            final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            
            textView1.setTextColor(0xFF000000);
            List appList = am.getRunningAppProcesses();
            for (int i = 0; i < appList.size(); i++) {
        		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
            	if (info.activityInfo.processName.equals(as.processName)) {
                	textView1.setTextColor(0xFFFF7777);//red for running apk
        			break;
        		}
            }

            btnIcon.setImageDrawable(info.loadIcon(pm));
            btnIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {//kill app
					if (! info.activityInfo.packageName.equals(myPackageName)) {//don't kill myself
						ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
						am.restartPackage(info.activityInfo.packageName);
						//but we need to know when will it restart by itself?
						textView1.setTextColor(0xFF000000);//set color back to black after kill it.
					}
				}
            });
            
            LinearLayout lapp = (LinearLayout) convertView.findViewById(R.id.app);
            lapp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {//start app
					startApp(info);
					textView1.setTextColor(0xFFFF7777);//red for running apk
				}
            });
        	lapp.setTag(new ricase(info, 2));
            registerForContextMenu(lapp);
            //lapp.setOnTouchListener(simpleHome.this);
            
            Object o = packagesSize.get(info.activityInfo.packageName);
           	textView1.setText(info.loadLabel(pm));
            
            final Button btnVersion = (Button) convertView.findViewById(R.id.appversion);
            btnVersion.setVisibility(View.VISIBLE);
            try {
            	btnVersion.setText(pm.getPackageInfo(info.activityInfo.packageName, 0).versionName);
			} catch (NameNotFoundException e) {
				btnVersion.setText("unknown");
			}
			
			if ((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)
				btnVersion.setEnabled(canRoot);//for system app, need judge whether can uninstall
			btnVersion.setOnClickListener(new OnClickListener() {//delete app
				@Override
				public void onClick(View arg0) {
					if ((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {//user app
						Uri uri = Uri.fromParts("package", info.activityInfo.packageName, null);
						Intent intent = new Intent(Intent.ACTION_DELETE, uri);
						myStartActivity(intent);
					}
					else {//system app
						apkToDel = info.activityInfo.applicationInfo.sourceDir;
						pkgToDel = info.activityInfo.packageName;
						showDialog(2);
					}
				}
			});
            
            final TextView textView3 = (TextView) convertView.findViewById(R.id.appsource);
            String source = "";
            if(o != null) source = o.toString();
            if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
            	textView3.setTextColor(0xFFF8BF00);//brown for debuggable apk
            	source += " (debuggable)";
            }
            else textView3.setTextColor(0xFF888888);//gray for normal
        	textView3.setText(source);
			
            return convertView;
        }
    }

    private class favoAppAdapter extends ArrayAdapter<ResolveInfo> {
    	ArrayList<ResolveInfo> localApplist;
        public favoAppAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList<ResolveInfo>) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = (ResolveInfo) localApplist.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.favo_list, parent, false);
            }

            convertView.setBackgroundColor(0);
            
            final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.favoappicon);
            
            btnIcon.setImageDrawable(info.loadIcon(pm));
    		btnIcon.setOnClickListener(new OnClickListener() {//start app
				@Override
				public void onClick(View arg0) {
					startApp(info);
				}
    		});
    		btnIcon.setTag(new ricase(info, 0));
            registerForContextMenu(btnIcon);
            
            return convertView;
        }
    }
    
    private class shortAppAdapter extends ArrayAdapter<ResolveInfo> {
    	ArrayList<ResolveInfo> localApplist;
        public shortAppAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList<ResolveInfo>) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = (ResolveInfo) localApplist.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.favo_list, parent, false);
            }

            final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.favoappicon);
            btnIcon.setImageDrawable(info.loadIcon(pm));
            
            TextView appname = (TextView) convertView.findViewById(R.id.favoappname);
            appname.setText(info.loadLabel(pm));
            
            convertView.setOnClickListener(new OnClickListener() {//launch app
				@Override
				public void onClick(View arg0) {
					startApp(info);
					shortAppList.setVisibility(View.INVISIBLE);
				}
    		});
    		convertView.setTag(new ricase(info, 1));
            registerForContextMenu(convertView);
            
            return convertView;
        }
    }
    
    void readFile(String name) 
    {
    	FileInputStream fi = null;
    	ObjectInputStream ois = null;
		try {//read favorite or shortcut data
			fi = mContext.openFileInput(name);
			ois = new ObjectInputStream(fi);
			String activityName;
			while ((activityName = (String) ois.readObject()) != null) {
				for (int i = 0; i < mAllApps.size(); i++)
					if (mAllApps.get(i).activityInfo.name.equals(activityName)) {
						if (name.equals("favo")) mFavoApps.add(mAllApps.get(i));
						else if (name.equals("short")) mShortApps.add(mAllApps.get(i));
						break;
					}
			}
		} catch (EOFException e) {//only when read eof need send out msg.
			try {
				ois.close();
				fi.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
	class InitTask extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... params) {//do all time consuming work here
			canRoot = false;
	    	String res = ShellInterface.doExec(new String[] {"id"}, true);
	    	if (res.contains("root")) {
	    		res = ShellInterface.doExec(new String[] {"mount -o rw,remount -t yaffs2 /dev/block/mtdblock3 /system"}, true);
	    		if (res.contains("Error")) canRoot = false;
	    		else canRoot = true;
	    	}

	    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
	    	mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
	    	mAllApps = pm.queryIntentActivities(mainIntent, 0);
	    	Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(pm));//sort by name

            readFile("favo");
            readFile("short");
            boolean shortEmpty = mShortApps.isEmpty();
			
	    	//read all resolveinfo
	    	String label_sms = "簡訊 Messaging Messages メッセージ 信息 消息 메시지  Mensajes Messaggi Berichten SMS a MMS SMS/MMS"; //use label name to get short cut
	    	String label_phone = "電話 Phone 电话 拨号键盘 키패드  Telefon Teléfono Téléphone Telefono Telefoon Телефон 휴대전화  Dialer";
	    	String label_contact = "聯絡人 联系人 Contacts People 連絡先 通讯录 전화번호부  Kontakty Kontakte Contactos Contatti Contacten Контакты 주소록";
	    	for (int i = 0; i < mAllApps.size(); i++) {
	    		ResolveInfo ri = mAllApps.get(i);
	    		if ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
	    			mSysApps.add(ri);
	    			String name = ri.loadLabel(pm).toString() ; 
	    			//Log.d("===============", name);
	    			if (label_phone.contains(name)) {
	    				if (ri_phone == null) {
	    					ri_phone = ri;
	    		        	Message msgphone = mAppHandler.obtainMessage();
	    		        	msgphone.what = UPDATE_RI_PHONE;
	    		        	mAppHandler.sendMessage(msgphone);//inform UI thread to update UI.
	    				}
	    			} 
	    			else if (label_sms.contains(name)) {
	    				if ((ri_sms == null) && (!name.equals("MM"))) {
	    					ri_sms = ri;
	    		        	Message msgsms = mAppHandler.obtainMessage();
	    		        	msgsms.what = UPDATE_RI_SMS;
	    		        	mAppHandler.sendMessage(msgsms);//inform UI thread to update UI.
	    				}
	    			}
	    			else if ((shortEmpty) && label_contact.contains(name)) {//only add contact to shortcut if shortcut is empty.
	    				if (ri_contact == null) {
	    					mShortApps.add(ri);
	    					/*ri_contact = ri;
	    		        	Message msgcontact = mAppHandler.obtainMessage();
	    		        	msgcontact.what = UPDATE_RI_CONTACT;
	    		        	mAppHandler.sendMessage(msgcontact);//inform UI thread to update UI.*/
		    			}
	    			}
	    		}
	    		else mUserApps.add(ri);
	    		
	    		try {
					getPackageSizeInfo.invoke(pm, ri.activityInfo.packageName, sizeObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
	    	}
        	Message msguser = mAppHandler.obtainMessage();
        	msguser.what = UPDATE_USER;
        	mAppHandler.sendMessage(msguser);//inform UI thread to update UI.
	    	
        	String status = Environment.getExternalStorageState();
        	if (status.equals(Environment.MEDIA_MOUNTED)) {
        		downloadPath = Environment.getExternalStorageDirectory() + "/simpleHome/";   
    			java.io.File myFilePath = new java.io.File(downloadPath);
    			try
    			{
    			    if(myFilePath.isDirectory()) ;//folder exist
    			    else myFilePath.mkdir();//create folder
    			}
    			catch(Exception e) {
    				e.printStackTrace();
    			}
        	} 
        	else downloadPath = getFilesDir().getPath() + "/";
        	picList = new ArrayList();
        	new File(downloadPath).list(new OnlyPic());
        	if (picList.size() > 0) cbWallPaper.setEnabled(true);
			   
        	FileType.initMimeMap();//init the file type map
        	
			try {serverWebs.get(webIndex).loadUrl("file:///android_asset/online.html");}
			catch (Exception e) {}
			
			try {//for 1.5 which do not have this method
				setPackage = Intent.class.getMethod("setPackage", new Class[] {String.class});
			} catch (Exception e) {
				setPackage = null;
				e.printStackTrace();
			}
			
			processor = runCmd("cat", "/proc/cpuinfo");
			
	    	mainIntent = new Intent(Intent.ACTION_VIEW, null);
	    	mainIntent.addCategory(Intent.CATEGORY_DEFAULT);
	    	List<ResolveInfo> viewApps = pm.queryIntentActivities(mainIntent, 0);
	    	appDetail = null;
	    	for (int i = 0; i < viewApps.size(); i++) {
	    		if (viewApps.get(i).activityInfo.name.contains("InstalledAppDetails")) {
	    			appDetail = viewApps.get(i);
	    			return null;//get the activity for app detail setting
	    		}
	    	}
	    	
			return null;
		}
	}
	
	class OnlyPic implements FilenameFilter { 
	    public boolean accept(File dir, String s) {
	    	 String name = s.toLowerCase();
	         if (s.endsWith(".png") || s.endsWith(".jpg")) {
	        	 picList.add(s);
	        	 return true;
	         }
	         else return false;
	    } 
	} 
	
    class appHandler extends Handler {

        public void handleMessage(Message msg) {
        	switch (msg.what) {
        	case UPDATE_USER:
        		sysAdapter = new ApplicationsAdapter(getBaseContext(), mSysApps);
        		sysAppList.setAdapter(sysAdapter);
        	
        		userAdapter = new ApplicationsAdapter(getBaseContext(), mUserApps);
        		userAppList.setAdapter(userAdapter);
        		
        		shortAdapter = new shortAppAdapter(getBaseContext(), mShortApps);
        		shortAppList.setAdapter(shortAdapter);
        		break;
        	case UPDATE_RI_PHONE:
        		int missCallCount = callObserver.countUnread();
        		if (missCallCount > 0) {
            		BitmapDrawable bd = (BitmapDrawable) ri_phone.loadIcon(pm);
        			shortcut_phone.setImageBitmap(generatorCountIcon(bd.getBitmap(), missCallCount, 1));
        		}
        		else shortcut_phone.setImageDrawable(ri_phone.loadIcon(pm));

    			shortcut_phone.setOnClickListener(new OnClickListener() {//start app
    				@Override
    				public void onClick(View arg0) {
    					startApp(ri_phone);
    				}
    			});
        		break;
        	case UPDATE_RI_SMS:
        		int unreadCount = smsObserver.countUnread();
        		if (unreadCount > 0) {
            		BitmapDrawable bd = (BitmapDrawable) ri_sms.loadIcon(pm);
            		shortcut_sms.setImageBitmap(generatorCountIcon(bd.getBitmap(), unreadCount, 1));
        		}
        		else shortcut_sms.setImageDrawable(ri_sms.loadIcon(pm));
    			shortcut_sms.setOnClickListener(new OnClickListener() {//start app
    				@Override
    				public void onClick(View arg0) {
    					startApp(ri_sms);
    				}
    			});
        		break;
        	case UPDATE_RI_CONTACT:
    			shortcut_contact.setImageDrawable(ri_contact.loadIcon(pm));
    			shortcut_contact.setOnClickListener(new OnClickListener() {//start app
    				@Override
    				public void onClick(View arg0) {
    					startApp(ri_contact);
    				}
    			});
        		break;
        	}
        }
    };

    boolean startDownload(String url) {
		String[] tmp = url.split("\\.");
		if ((tmp.length > 0) && (FileType.MIMEMAP.containsKey(tmp[tmp.length-1].toUpperCase()))) {//files need download
			String ss[] = url.split("/");
			String apkName = ss[ss.length-1]; //get download file name
			
			Iterator iter = appstate.downloadState.entrySet().iterator();
			while (iter.hasNext()) {
				Entry entry = (Entry) iter.next();
				DownloadTask val = (DownloadTask) entry.getValue();
				if ((val != null) && val.apkName.equals(apkName)) {
					if (val.pauseDownload) val.pauseDownload = false;
					return true;//the file is downloading, not start a new download task.
				}
			}
			
	    	Random random = new Random();
	    	int id = random.nextInt() + 1000;
	    	
			DownloadTask dltask = new DownloadTask();
			dltask.NOTIFICATION_ID = id;
			appstate.downloadState.put(id, dltask);
			dltask.execute(url, apkName);
			return true;
		}
		else return false;
    }
    
	class DownloadTask extends AsyncTask<String, Integer, String> {
		private String URL_str; //网络歌曲的路径
		private File download_file; //下载的文件
		private long total_read = 0; //已经下载文件的长度(以字节为单位)
		private int readLength = 0; //一次性下载的长度(以字节为单位)
		private long apk_length = 0; //音乐文件的长度(以字节为单位)
		private long skip_length = 0;//if found local file not download finished last time, need continue to download
		String apkName = ""; //下载的文件名
		int NOTIFICATION_ID;
		private Notification notification;
		private int oldProgress;
		boolean stopDownload = false;//true to stop download
		boolean pauseDownload = false;//true to pause download
		boolean downloadFailed = false;

		@Override
		protected String doInBackground(String... params) {//download here
	    	URL_str = params[0]; //get download url
	    	apkName = params[1]; //get download file name
	    	if (apkName.contains("%")) apkName = apkName.split("%")[apkName.split("%").length-1];//for some filename contain % will cause error
	    	
	    	notification = new Notification(android.R.drawable.stat_sys_download, getString(R.string.start_download), System.currentTimeMillis());   
			
	    	Intent intent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);  
    		notification.setLatestEventInfo(mContext, apkName, getString(R.string.start_download), contentIntent);
	        nManager.notify(NOTIFICATION_ID, notification);
	        
			intent.setAction("simple.home.jtbuaa.downloadControl");//this intent is to pause/stop download
			intent.putExtra("id", NOTIFICATION_ID);
			intent.putExtra("name", apkName);
			intent.putExtra("url", URL_str);
			contentIntent = PendingIntent.getActivity(mContext, NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);//request_code will help to diff different thread
	        notification.setLatestEventInfo(mContext, apkName, getString(R.string.downloading), contentIntent);
	        
	    	FileOutputStream fos = null; //文件输出流
	    	InputStream is = null; //网络文件输入流
	    	URL url = null;
	    	try {
	        	url = new URL(URL_str); //网络歌曲的url
	        	HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection(); //打开网络连接
        		download_file = new File(downloadPath + apkName);
	        	apk_length = httpConnection.getContentLength(); //file size need to download
        		if (download_file.length() == apk_length) {//found local file with same name and length, no need to download, just send intent to view it
                	String[] tmp = apkName.split("\\.");
        			intent.setAction(Intent.ACTION_VIEW);
        			intent.setDataAndType(Uri.fromFile(download_file), FileType.MIMEMAP.get(tmp[tmp.length-1].toUpperCase()));
    				myStartActivity(intent);
                	appstate.downloadState.remove(NOTIFICATION_ID);
            		nManager.cancel(NOTIFICATION_ID);
    				return "";
        		}
        		else if (download_file.length() < apk_length) {//local file size < need to download, need continue to download
        			fos = new FileOutputStream(download_file, true);
        			skip_length = download_file.length();
        		}
        		else //need overwrite
        			fos = new FileOutputStream(download_file, false);
        		
    	        notification.contentView = new RemoteViews(getApplication().getPackageName(), R.layout.notification_dialog);
    	        notification.contentView.setProgressBar(R.id.progress_bar, 100, 0, false);
    	        notification.contentView.setTextViewText(R.id.progress, "0%");
    	        notification.contentView.setTextViewText(R.id.title, apkName);
    	        nManager.notify(NOTIFICATION_ID, notification);
    	        
	        	total_read = 0; //初始化“已下载部分”的长度，此处应为0
	        	is = httpConnection.getInputStream();

	        	byte buf[] = new byte[1024]; //download buffer
	        	readLength = 0; //一次性下载的长度
	        	
	        	oldProgress = 0;
	        	//如果读取网络文件的数据流成功，且用户没有选择停止下载，则开始下载文件
	        	while (readLength != -1 && !stopDownload) {
	        		if (pauseDownload) {
	        			continue;
	        		}
	        		
	            	if((readLength = is.read(buf))>0){
	            		if (skip_length == 0)
	            			fos.write(buf, 0, readLength);
	            		else if (skip_length < readLength) {
	            			fos.write(buf, (int) skip_length, (int) (readLength - skip_length));
	            			skip_length = 0;
	            		}
	            		else skip_length -= readLength;//just read and skip, not write if need skip
	            		
	                	total_read += readLength; //increase the download size
	            	}

                	int progress = (int) ((total_read+0.0)/apk_length*100);
                	if (oldProgress != progress) {//the device will get no response if update too often
                		oldProgress = progress;
                		notification.contentView.setProgressBar(R.id.progress_bar, 100, progress, false);//update download progress
            	        notification.contentView.setTextViewText(R.id.progress, progress + "%");
                		nManager.notify(NOTIFICATION_ID, notification);
                	}
	        	}
            	fos.close();
            	is.close();
            	httpConnection.disconnect();
            	
            	appstate.downloadState.remove(NOTIFICATION_ID);
            	if (stopDownload) {//stop download by user. clear notification
            		nManager.cancel(NOTIFICATION_ID);
            	}
            	else {//download success. change notification, start package manager to install package
                	notification.icon = android.R.drawable.stat_sys_download_done;

                	String[] tmp = apkName.split("\\.");
        			intent = new Intent();
        			intent.setAction(Intent.ACTION_VIEW);
        			intent.setDataAndType(Uri.fromFile(download_file), FileType.MIMEMAP.get(tmp[tmp.length-1].toUpperCase()));
        	        contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);  
        	        notification.contentView.setOnClickPendingIntent(R.id.notification_dialog, contentIntent);
        	        notification.setLatestEventInfo(mContext, apkName, getString(R.string.download_finish), contentIntent);//click listener for download progress bar
        	        nManager.notify(NOTIFICATION_ID, notification);
        	        
        	        //apkName from appchina is always packageName+xxx.apk. so we use this pair to store package name and nofification id.
        			downloadAppID.add(new packageIDpair(apkName.toLowerCase(), NOTIFICATION_ID, download_file));
        			
        			Process p = Runtime.getRuntime().exec("chmod 644 " + download_file.getPath());//change file property, for on some device the property is wrong
        			p.waitFor();
    				myStartActivity(intent);//call system package manager to install app. it will not return result code, so not use startActivityForResult();
    				
    				if ((apkName.toLowerCase().endsWith("jpg")) || (apkName.toLowerCase().endsWith("png"))) {
    					picList.add(apkName);//add to picture list
		        		cbWallPaper.setEnabled(true);
    				}
            	}
				
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    		downloadFailed = true;
	    		notification.icon = android.R.drawable.stat_notify_error;
	    		notification.setLatestEventInfo(mContext, apkName, getString(R.string.download_fail) + e.getMessage(), contentIntent);
	    		nManager.notify(NOTIFICATION_ID, notification);
	    		
    	        if (download_file.length() == 0) download_file.delete();//delete empty file
	    	}

	    	return null;
		}

	}
	
	@Override
	protected void onNewIntent(Intent intent) {//go back to home if press Home key.
		if ((intent.getAction().equals(Intent.ACTION_MAIN)) && (intent.hasCategory(Intent.CATEGORY_HOME))) {
			if (shortAppList.getVisibility() == View.VISIBLE) shortBar.performClick();
			if (adsParent.getVisibility() == View.VISIBLE) homeBar.performClick();
			if (mProgressDialog != null) mProgressDialog.hide();
		}
		else if (intent.getAction().equals(Intent.ACTION_VIEW)) {//view webpages
			serverWebs.get(webIndex).loadUrl(intent.getDataString());
			if (adsParent.getVisibility() == View.INVISIBLE) homeBar.performClick();
			btnWeb.performClick();
		}
		super.onNewIntent(intent); 
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() == 0) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {//press Back key in webview will go backword.
				if (shortAppList.getVisibility() == View.VISIBLE) shortBar.performClick();
				else homeBar.performClick();
				return true;
			}	
		}
		
		return false;
	}
	
	void setLayout(int width) {
		if (width < 100) return;//can't work on so small screen.
		
        LayoutParams lp = webtools_center.getLayoutParams();
        lp.width = width/2 + 40;
        
        lp = btnSys.getLayoutParams();
        lp.width = width/3 - 10;
        
        lp = btnUser.getLayoutParams();
        lp.width = width/3 - 10;
        
        lp = btnWeb.getLayoutParams();
        lp.width = width/3 - 10;

        lp = shortcutBar_center.getLayoutParams();
        if (width > 320)
        	lp.width = width/2-25;
        else lp.width = width/2-15;
        	
        lp = shortAppList.getLayoutParams();
       	if (width/2 - 140 > 200) lp.width = width/2 - 140;
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig); //not restart activity each time screen orientation changes
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		setLayout(dm.widthPixels);
		
		if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy) {
		// TODO Auto-generated method stub
		if (e1.getX() == e2.getX()) return false;
		else if ((e1.getX() < e2.getX()) && (mainlayout.getDisplayedChild() == 0)) return false;//don't show previous for first view
		else if ((e1.getX() > e2.getX()) && (mainlayout.getDisplayedChild() == mainlayout.getChildCount()-1)) return false;//don't show next for the last view

		int oldIndex = mainlayout.getDisplayedChild(); 
		boolean scroll = false;
		
		if(e2.getX() - e1.getX() > 100 + Math.abs(e2.getY() - e1.getY())) {//the finger move on x direction more than y direction, from left to right
			// 设置切入动画
			mainlayout.setInAnimation(AnimationUtils.loadAnimation(
                    getApplicationContext(), android.R.anim.slide_in_left));
            // 设置切出动画
            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
                    getApplicationContext(), android.R.anim.slide_out_right));
			mainlayout.showPrevious(); //move to left
			scroll = true;
		}
		else if(e1.getX() - e2.getX() > 100 + Math.abs(e1.getY() - e2.getY())) {//the finger move on x direction more than y direction, from right to left
			// 设置切入动画
            mainlayout.setInAnimation(AnimationUtils.loadAnimation(
                    getApplicationContext(), R.anim.slide_in_right));
            // 设置切出动画
            mainlayout.setOutAnimation(AnimationUtils.loadAnimation(
                    getApplicationContext(), R.anim.slide_out_left));
			mainlayout.showNext(); //move to right
			scroll = true;
		}

		if (scroll) {
			switch(oldIndex) {
			case 0:
				btnSys.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			case 1:
				btnUser.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			case 2:
				btnWeb.setBackgroundResource(R.drawable.button_layout_unselected);
				break;
			}
			
			switch(mainlayout.getDisplayedChild()) {
			case 0:
				btnSys.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			case 1:
				btnUser.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			case 2:
				btnWeb.setBackgroundResource(R.drawable.button_layout_selected);
				break;
			}
			return true;
		}
		else return false;
	}


	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		if (arg0.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {  
			long curTime = System.currentTimeMillis();  
			// 每100毫秒检测一次  
			if ((curTime - lastUpdate) > 100) {  
				long timeInterval = (curTime - lastUpdate);  
				lastUpdate = curTime;  
				  
				float x = arg0.values[SensorManager.DATA_X];  
				float y = arg0.values[SensorManager.DATA_Y];  
				float z = arg0.values[SensorManager.DATA_Z];  
				  
				float deltaX = x - last_x;
				float deltaY = y - last_y;
				float deltaZ = z - last_z;
				  
				double speed = Math.sqrt(deltaX*deltaX + deltaY*deltaY + deltaZ*deltaZ)/timeInterval * 100;
				//condition to change wallpaper: speed is enough; frequency is not too high; picList is not empty. 
				if ((speed > 8) && (curTime - lastSet > 500) && (picList != null) && (picList.size() > 0)) {
			    	Random random = new Random();
			    	int id = random.nextInt(picList.size());
				    try {
				    	Drawable cd = Drawable.createFromPath(downloadPath + picList.get(id));
				    	base.setBackgroundDrawable(new ClippedDrawable(cd, base.getWidth(), base.getHeight()));
				    	lastSet = System.currentTimeMillis();
					} catch (Exception e) {
						e.printStackTrace();
						picList.remove(id);
						if (picList.isEmpty()) {
							cbWallPaper.performClick();
			        		cbWallPaper.setEnabled(false);
						}
					}
				}  
				last_x = x;  
				last_y = y;  
				last_z = z;  
			}  
		}  
	}

	@Override
	public void onSizeChanged(int w, int h, int oldW, int oldH) {
		setLayout(oldW);
	}
}

/** from Android Home sample
 * When a drawable is attached to a View, the View gives the Drawable its dimensions
 * by calling Drawable.setBounds(). In this application, the View that draws the
 * wallpaper has the same size as the screen. However, the wallpaper might be larger
 * that the screen which means it will be automatically stretched. Because stretching
 * a bitmap while drawing it is very expensive, we use a ClippedDrawable instead.
 * This drawable simply draws another wallpaper but makes sure it is not stretched
 * by always giving it its intrinsic dimensions. If the wallpaper is larger than the
 * screen, it will simply get clipped but it won't impact performance.
 */
class ClippedDrawable extends Drawable {
    private final Drawable mWallpaper;
    int screenWidth, screenHeight;

    public ClippedDrawable(Drawable wallpaper, int sw, int sh) {
        mWallpaper = wallpaper;
        screenWidth = sw;
        screenHeight = sh;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        // Ensure the wallpaper is as large as it really is, to avoid stretching it
        // at drawing time
        int tmpHeight = mWallpaper.getIntrinsicHeight() * screenWidth / mWallpaper.getIntrinsicWidth();
        int tmpWidth = mWallpaper.getIntrinsicWidth() * screenHeight / mWallpaper.getIntrinsicHeight();
        if (tmpHeight >= screenHeight) {
        	top -= (tmpHeight - screenHeight)/2;
        	mWallpaper.setBounds(left, top, left + screenWidth, top + tmpHeight);
        }
        else {
        	left -= (tmpWidth - screenWidth)/2;
        	mWallpaper.setBounds(left, top, left + tmpWidth, top + screenHeight);
        }
    }

    public void draw(Canvas canvas) {
        mWallpaper.draw(canvas);
    }

    public void setAlpha(int alpha) {
        mWallpaper.setAlpha(alpha);
    }

    public void setColorFilter(ColorFilter cf) {
        mWallpaper.setColorFilter(cf);
    }

    public int getOpacity() {
        return mWallpaper.getOpacity();
    }
}

class sizedRelativeLayout extends RelativeLayout {

	public sizedRelativeLayout(Context context) {
		super(context);
	}
	
    public sizedRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	private OnResizeChangeListener mOnResizeChangeListener;
	
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        if(mOnResizeChangeListener!=null){
            mOnResizeChangeListener.onSizeChanged(w,h,oldW,oldH);
        }
        super.onSizeChanged(w,h,oldW,oldH);
    }
	
    public void setResizeListener(OnResizeChangeListener l) {
        mOnResizeChangeListener = l;
    }
	    
    public interface OnResizeChangeListener{
        void onSizeChanged(int w,int h,int oldW,int oldH);
    }
	
}

class SmsChangeObserver extends ContentObserver {
	ContentResolver mCR;
	Handler mHandler;
	
	public SmsChangeObserver(ContentResolver cr, Handler handler) {
		super(handler);
		mCR = cr;
		mHandler = handler;
	}
	
	public int countUnread() {
    	//get sms unread count
    	Cursor csr = mCR.query(Uri.parse("content://sms"),
    	                new String[] {"thread_id"},
    	                "read=0",
    	                null,
    	                null);
    	int ret = csr.getCount();
    	
    	//get mms unread count
    	csr = mCR.query(Uri.parse("content://mms"),
    	                new String[] {"thread_id"},
    	                "read=0",
    	                null,
    	                null);
    	return ret + csr.getCount();
	}
	
	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
    	Message msgsms = mHandler.obtainMessage();
    	msgsms.what = 1;//UPDATE_RI_SMS;
    	mHandler.sendMessage(msgsms);//inform UI thread to update UI.
	}
}

class CallObserver extends ContentObserver {
	ContentResolver mCR;
	Handler mHandler;
	
	public CallObserver(ContentResolver cr, Handler handler) {
		super(handler);
		mHandler = handler;
		mCR = cr;
	}
	
	public int countUnread() {
    	//get missed call number
    	Cursor csr = mCR.query(Calls.CONTENT_URI, 
    			new String[] {Calls.NUMBER, Calls.TYPE, Calls.NEW}, 
    			Calls.TYPE + "=" + Calls.MISSED_TYPE + " AND " + Calls.NEW + "=1", 
    			null, Calls.DEFAULT_SORT_ORDER);
    	return csr.getCount();
	}
	
	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
    	Message msgphone = mHandler.obtainMessage();
    	msgphone.what = 0;//UPDATE_RI_PHONE;
    	mHandler.sendMessage(msgphone);//inform UI thread to update UI.
	}
}
