package simple.home.jtbuaa;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.WallpaperManager;
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
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
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
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.CallLog.Calls;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class simpleHome extends Activity implements SensorEventListener, sizedRelativeLayout.OnResizeChangeListener {

	final static int homeTab = 1;
	
	//wall paper related
	String downloadPath;
	SensorManager sensorMgr;
	Sensor mSensor;
	float last_x, last_y, last_z;
	long lastUpdate, lastSet;
	ArrayList<String> picList;
	CheckBox cbWallPaper;
	WallpaperManager mWallpaperManager;
	
    private void setWallpaperDimension() {
        WallpaperManager wpm = (WallpaperManager)getSystemService(WALLPAPER_SERVICE);

        Display display = getWindowManager().getDefaultDisplay();
        boolean isPortrait = display.getWidth() < display.getHeight();

        final int width = isPortrait ? display.getWidth() : display.getHeight();
        final int height = isPortrait ? display.getHeight() : display.getWidth();
        wpm.suggestDesiredDimensions(width * 2, height);
    }
    
    private void updateWallpaperOffset(View view, float offset) {
        IBinder token = view.getWindowToken();
        if (token != null) {
        	Log.d("==============", token.toString());
            mWallpaperManager.setWallpaperOffsetSteps(0.5f, 0);
            mWallpaperManager.setWallpaperOffsets(view.getWindowToken(), offset, 0);
        }
    }
    
	//about dialog
	TextView mailto;
	View aboutView;
	AlertDialog m_aboutDialog;
	
	//alpha list related
	GridView sysAlpha, userAlpha;
	AlphaAdapter sysAlphaAdapter, userAlphaAdapter;
	ArrayList<String> mSysAlpha, mUserAlpha;
	final int MaxCount = 14;
	Boolean DuringSelection = false;
    RadioButton btnSystem, btnUser, btnHome;

	//app list related
	private List<View> mListViews;
	GridView favoAppList;
	ListView sysAppList, userAppList, shortAppList;
	ImageView homeBar, shortBar;
	String version, myPackageName;
	ViewPager mainlayout;
	RelativeLayout home, systems, users;
	ResolveInfo appDetail;
	List<ResolveInfo> mAllApps;
	ArrayList<ResolveInfo> mFavoApps, mSysApps, mUserApps, mShortApps;
	static int whiteColor = 0xFFFFFFFF, grayColor = 0xDDDDDDDD, redColor = 0xFFFF7777, brownColor = 0xFFF8BF00;
	PackageManager pm;
	favoAppAdapter favoAdapter;
	shortAppAdapter shortAdapter;
	ApplicationsAdapter sysAdapter, userAdapter;
	ResolveInfo ri_phone, ri_sms, ri_contact;
	CallObserver callObserver;
	SmsChangeObserver smsObserver;
	ImageView shortcut_phone, shortcut_sms, shortcut_contact;
	sizedRelativeLayout base;
	FrameLayout apps;
	RelativeLayout shortcutBar, shortcutBar_center, adsParent;
    appHandler mAppHandler = new appHandler();
	final static int UPDATE_RI_PHONE = 0, UPDATE_RI_SMS = 1, UPDATE_RI_CONTACT = 2, UPDATE_USER = 3, UPDATE_SHAKE_WALLPAPER = 4; 
	ContextMenu mMenu;
	
	String apkToDel, pkgToDel;
	boolean canRoot;
	
	DisplayMetrics dm;
	String processor;
	Field pid;
	

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
	 
	class MyPagerAdapter extends PagerAdapter{
	    @Override
	    public void destroyItem(View arg0, int arg1, Object arg2) {
	        ((ViewPager) arg0).removeView(mListViews.get(arg1));
	    }
	    
	    @Override
	    public void finishUpdate(View arg0) {
	    }

	    @Override
	    public int getCount() {
	        return mListViews.size();
	    }

	    @Override
	    public Object instantiateItem(View arg0, int arg1) {
	        ((ViewPager) arg0).addView(mListViews.get(arg1),0);
	        return mListViews.get(arg1);
	    }

	    @Override
	    public boolean isViewFromObject(View arg0, Object arg1) {
	        return arg0==(arg1);
	    }

	    @Override
	    public void restoreState(Parcelable arg0, ClassLoader arg1) {
	    }

	    @Override
	    public Parcelable saveState() {
	        return null;
	    }

	    @Override
	    public void startUpdate(View arg0) {
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
       
	@Override
	protected Dialog onCreateDialog(int id) {
        switch (id) {
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
					ShellInterface.doExec(new String[] {"mv " + apkToDel + " " + apkToDel + ".bak"});
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
			else ShellInterface.doExec(new String[] {"mv " + apkToDel + ".bak " + apkToDel});
			
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
						if (sb.length() > 0) sb.append(", ");
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
		
		/*ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appList = am.getRunningAppProcesses(); 
        for (int i = 0; i < appList.size(); i++) {
    		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
    		if (as.processName.equals(myPackageName)) {
        		try {//memory used by me
        			Debug.MemoryInfo info = am.getProcessMemoryInfo(new int[] {pid.getInt(as)})[0];
        			Log.d("==============", myPackageName + " " + info.getTotalPss()+"kb");
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			break;
    		}
        }*/

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
	        util.startActivity(Intent.createChooser(pickWallpaper, getString(R.string.wallpaper)), true, getBaseContext());
			break;
		case 1://settings
			return super.onOptionsItemSelected(item);
		case 2://help dialog
        	if (m_aboutDialog == null) {
            	m_aboutDialog = new AlertDialog.Builder(this).
            	setTitle(getString(R.string.app_name) + " " + version).
            	setView(aboutView).
            	setNegativeButton(R.string.vote, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=simple.home.jtbuaa"));
						if (!util.startActivity(intent, false, getBaseContext())) {
							intent.setAction(Intent.ACTION_VIEW);
							intent.setData(Uri.parse("https://market.android.com/details?id=simple.home.jtbuaa"));
							intent.setComponent(getComponentName());
							util.startActivity(intent, false, getBaseContext());
						}
            		}
            	}).
            	setPositiveButton(R.string.share, new DialogInterface.OnClickListener() {
            		public void onClick(DialogInterface dialog, int which) {//share simpleHome to friends
            	        String text = getString(R.string.sharetext) + getString(R.string.share_text1) 
           	        		+ "http://opda.co/?s=D/simple.home.jtbuaa"//opda will do the webpage reload for us.
           	        		+ getString(R.string.share_text2)
           	        		+ "https://market.android.com/details?id=simple.home.jtbuaa"
           	        		+ getString(R.string.share_text3);
            	        
            	        Intent intent = new Intent(Intent.ACTION_SEND);
            	        intent.setType("text/plain");  
            	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
    	        		intent.putExtra(Intent.EXTRA_TEXT, text);
   	        			util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, getBaseContext());
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
			menu.add(0, 3, 0, getString(R.string.backapp)).setEnabled(!downloadPath.startsWith(getFilesDir().getPath()));//no need to backup if no sdcard
			menu.add(0, 4, 0, getString(R.string.appdetail));
			menu.add(0, 5, 0, getString(R.string.addtoFavo));
			menu.add(0, 6, 0, getString(R.string.addtoShort));
			mMenu = menu;
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
		case 2://share app name and link
	        Intent intent = new Intent(Intent.ACTION_SEND);
	        intent.setType("text/plain");  
	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
	        String text = selected_case.mRi.loadLabel(pm) + getString(R.string.app_share_text) + getString(R.string.share_text1) 
	        	+ "http://opda.co/?s=D/" + selected_case.mRi.activityInfo.packageName
	        	+ getString(R.string.share_text2)
	        	+ "https://market.android.com/details?id=" + selected_case.mRi.activityInfo.packageName
	        	+ getString(R.string.share_text3);
    		intent.putExtra(Intent.EXTRA_TEXT, text);
   			util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, getBaseContext());
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
			util.startActivity(intent, true, getBaseContext());
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
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    	
    	sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
    	mSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	
		dm = new DisplayMetrics();  
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		try {
			Field screenLayout = Configuration.class.getField("screenLayout");
	    	//1,2,3,4 are integer value of small, normal, large and XLARGE screen respectively.
	    	int screen_size = screenLayout.getInt(getResources().getConfiguration()) & Configuration.SCREENLAYOUT_SIZE_MASK; 
	    	if (screen_size < 3)//disable auto rotate screen for small and normal screen.
	    		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} catch (Exception e) {//no such field if sdk level <= 3
			e.printStackTrace();
		}
		
        setWallpaperDimension();
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
    	setContentView(R.layout.ads);
    	
        home = (RelativeLayout) getLayoutInflater().inflate(R.layout.home, null);
        
        
    	//favorite app tab
    	favoAppList = (GridView) home.findViewById(R.id.favos);
    	favoAppList.setVerticalScrollBarEnabled(false);
    	favoAppList.inflate(this, R.layout.app_list, null);
    	favoAppList.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
		    	shortAppList.setVisibility(View.INVISIBLE);
				return false;
			}
    	});
        
    	shortAppList = (ListView) home.findViewById(R.id.business);
    	shortAppList.bringToFront();
    	shortAppList.setVisibility(View.INVISIBLE);
    	
    	//system app tab
    	systems = (RelativeLayout) getLayoutInflater().inflate(R.layout.apps, null);
    	sysAppList = (ListView) systems.findViewById(R.id.applist); 
    	sysAppList.inflate(this, R.layout.app_list, null);
    	sysAppList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if ((sysAlpha != null) && (sysAdapter != null) && (!DuringSelection)) {//revert the focus of alpha list when scroll app list
					String alpha = sysAdapter.getItem(firstVisibleItem).activityInfo.applicationInfo.dataDir;
					for (int i = 0; i < sysAlphaAdapter.getCount(); i++) 
						if (alpha.charAt(0) == sysAlphaAdapter.getItem(i).charAt(0)) {
							RelativeLayout rl = (RelativeLayout)sysAlpha.getChildAt(i);
							if (rl != null) {
								TextView tv = (TextView) rl.findViewById(R.id.alpha);
								if (tv != null) tv.requestFocus();
							}
							break;
						}
				}
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				DuringSelection = false;//the scrollState will not change when setSelection(), but will change during scroll manually. so we turn off the flag here.
			}
    	});
    	sysAlpha = (GridView) systems.findViewById(R.id.alpha_list); 
    	sysAlpha.inflate(this, R.layout.alpha_list, null);
        
    	//user app tab
    	users = (RelativeLayout) getLayoutInflater().inflate(R.layout.apps, null);
    	userAppList = (ListView) users.findViewById(R.id.applist); 
        userAppList.inflate(this, R.layout.app_list, null);
        userAppList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if ((userAlpha != null) && (userAdapter != null) && (!DuringSelection)) {
					String alpha = userAdapter.getItem(firstVisibleItem).activityInfo.applicationInfo.dataDir;
					for (int i = 0; i < userAlphaAdapter.getCount(); i++) 
						if (alpha.charAt(0) == userAlphaAdapter.getItem(i).charAt(0)) {
							RelativeLayout rl = (RelativeLayout)userAlpha.getChildAt(i);
							if (rl != null) {
								TextView tv = (TextView) rl.findViewById(R.id.alpha);
								if (tv != null) tv.requestFocus();
							}
							break;
						}
				}
			}
			@Override
			public void onScrollStateChanged(AbsListView arg0, int arg1) {
				DuringSelection = false;
			}
        });
    	userAlpha = (GridView) users.findViewById(R.id.alpha_list); 
    	userAlpha.inflate(this, R.layout.alpha_list, null);
        
        mListViews = new ArrayList<View>();
        mListViews.add(systems);
        mListViews.add(home);
        mListViews.add(users);
        
        btnSystem = (RadioButton) findViewById(R.id.radio_system);
        btnUser = (RadioButton) findViewById(R.id.radio_user);
        btnHome = (RadioButton) findViewById(R.id.radio_home);
        
        mWallpaperManager = WallpaperManager.getInstance(this);
        
        mainlayout = (ViewPager)findViewById(R.id.mainFrame);
        mainlayout.setLongClickable(true);
        MyPagerAdapter myAdapter = new MyPagerAdapter();
        mainlayout.setAdapter(myAdapter);
        mainlayout.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_SETTLING) {
					switch(mainlayout.getCurrentItem()) {
					case 0:
						btnSystem.setChecked(true);
						btnUser.setText(R.string.systemapps);
						break;
					case 1:
						btnHome.setChecked(true);
						btnUser.setText(R.string.home);
						break;
					case 2:
						btnUser.setChecked(true);
						btnUser.setText(R.string.userapps);
						break;
					}
				}
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		        IBinder token = apps.getWindowToken();
		        if (token != null) {
		        	Log.d("============", token.toString());
		            mWallpaperManager.setWallpaperOffsetSteps(0.5f, 0);
		            mWallpaperManager.setWallpaperOffsets(apps.getWindowToken(),
		                    Math.max(0.f, Math.min(positionOffsetPixels/positionOffset, 1.f)), 0);
		        }
			}

			@Override
			public void onPageSelected(int arg0) {
			}
        	
        });
        mainlayout.setCurrentItem(homeTab);
        
		mSysApps = new ArrayList<ResolveInfo>();
		mUserApps = new ArrayList<ResolveInfo>();
		mFavoApps = new ArrayList<ResolveInfo>();
		mShortApps = new ArrayList<ResolveInfo>();
		mSysAlpha = new ArrayList<String>();
		mUserAlpha = new ArrayList<String>();
		
		favoAdapter = new favoAppAdapter(getBaseContext(), mFavoApps);
		favoAppList.setAdapter(favoAdapter);
		
        adsParent = (RelativeLayout) findViewById(R.id.adsParent);
        base = (sizedRelativeLayout) home.findViewById(R.id.base); 
        base.setResizeListener(this);
        shortcutBar = (RelativeLayout) home.findViewById(R.id.shortcut_bar);
        shortcutBar_center = (RelativeLayout) home.findViewById(R.id.shortcut_bar_center);
        homeBar = (ImageView) home.findViewById(R.id.home_bar);
        homeBar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setClassName(myPackageName, myPackageName+".SimpleBrowser");
				util.startActivity(intent, true, getBaseContext());
			}
        });
        
        shortBar = (ImageView) home.findViewById(R.id.business_bar);
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
        
		shortcut_phone = (ImageView) home.findViewById(R.id.shortcut_phone);
		shortcut_sms = (ImageView) home.findViewById(R.id.shortcut_sms);
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

		filter = new IntentFilter("simpleHome.action.PIC_ADDED");
        registerReceiver(picAddReceiver, filter);
        
        filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);  
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);  
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);  
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);  
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);  
        filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);  
        filter.addDataScheme("file");  
        registerReceiver(sdcardListener, filter);  
        
		LayoutInflater inflater = LayoutInflater.from(this);
		final simpleHome sensorListener = this;
		
		aboutView = inflater.inflate(R.layout.about, null);
		mailto = (TextView) aboutView.findViewById(R.id.mailto);
		mailto.setText(Html.fromHtml("<u>"+ getString(R.string.author) +"</u>"));
		mailto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "jtbuaa@gmail.com", null));
				if (util.startActivity(intent, false, getBaseContext())) m_aboutDialog.cancel();
			}
		});
		
        apps = (FrameLayout) findViewById(R.id.apps);
    	
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
    	unregisterReceiver(picAddReceiver);
    	unregisterReceiver(homeChangeReceiver);
    	unregisterReceiver(sdcardListener);
    	
    	super.onDestroy();
    }
    
    BroadcastReceiver sdcardListener = new BroadcastReceiver() {  
        @Override  
        public void onReceive(Context context, Intent intent) {  
              
            String action = intent.getAction();  
            if(Intent.ACTION_MEDIA_MOUNTED.equals(action)  
                    || Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)  
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)  
                    ){// SD卡成功挂载
            	if (downloadPath != null) 
            		downloadPath = Environment.getExternalStorageDirectory() + "/simpleHome/"; 
            	if (mMenu != null) mMenu.getItem(3).setEnabled(true);
            } else if(Intent.ACTION_MEDIA_REMOVED.equals(action)  
                    || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)  
                    || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)  
                    ){// SD卡挂载失败
            	if (downloadPath != null) 
            		downloadPath = getFilesDir().getPath() + "/";
            	if (mMenu != null) mMenu.getItem(3).setEnabled(false);
            }  
        }  
    };  
    
 	BroadcastReceiver wallpaperReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context arg0, Intent arg1) {
 			apps.setBackgroundColor(0);//set back ground to transparent to show wallpaper
 		}
 	};
 	
    BroadcastReceiver picAddReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String picName = intent.getStringExtra("picFile");
			picList.add(picName);//add to picture list
    		cbWallPaper.setEnabled(true);
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
            String action = intent.getAction();
            String packageName = intent.getDataString().split(":")[1];//it always in the format of package:x.y.z
            if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
            	boolean inUser = false;
            	ResolveInfo info = null;
        		for (int i = 0; i < userAdapter.getCount(); i++) {
        			info = userAdapter.getItem(i);
        			if (info.activityInfo.packageName.equals(packageName)) {
        				userAdapter.remove(info);
        				inUser = true;
        				break;
        			}
        		}
            	if (!inUser) {
            		for (int i = 0; i < sysAdapter.getCount(); i++) {
            			info = sysAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				sysAdapter.remove(info);
            				break;
            			}
            		}
            	}
    			String tmp = info.activityInfo.applicationInfo.dataDir.substring(0, 1);
    			
            	if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {//not remove shortcut if it is just replace 
            		for (int i = 0; i < favoAdapter.getCount(); i++) {
            			info = favoAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				favoAdapter.remove(info);
            				writeFile("favo");
            				break;
            			}
            		}
            		for (int i = 0; i < shortAdapter.getCount(); i++) {
            			info = shortAdapter.getItem(i);
            			if (info.activityInfo.packageName.equals(packageName)) {
            				shortAdapter.remove(info);
            				writeFile("short");
            				break;
            			}
            		}
            		
        			boolean found = false;
            		if (inUser) {//check user alpha list
            			for (int i = 0; i < userAdapter.getCount(); i++) {
            				if (userAdapter.getItem(i).activityInfo.applicationInfo.dataDir.startsWith(tmp)) {
            					found = true;
            					break;
            				}
            			}
            			if (!found) {
            				userAlphaAdapter.remove(tmp);
                    		if (userAlphaAdapter.getCount() < MaxCount) userAlpha.setNumColumns(userAlphaAdapter.getCount());
                    		else if (userAlphaAdapter.getCount() < MaxCount*2) userAlpha.setNumColumns((int)(userAlphaAdapter.getCount()/2.0+0.5));
                    		else userAlpha.setNumColumns(MaxCount);
            			}
            		}
            		else {//check system alpha list
            			for (int i = 0; i < sysAdapter.getCount(); i++) {
            				if (sysAdapter.getItem(i).activityInfo.applicationInfo.dataDir.startsWith(tmp)) {
            					found = true;
            					break;
            				}
            			}
            			if (!found) {
            				sysAlphaAdapter.remove(tmp);
                    		if (sysAlphaAdapter.getCount() < MaxCount) sysAlpha.setNumColumns(sysAlphaAdapter.getCount());
                    		else if (sysAlphaAdapter.getCount() < MaxCount*2) sysAlpha.setNumColumns((int)(sysAlphaAdapter.getCount()/2.0+0.5));
                    		else sysAlpha.setNumColumns(MaxCount);
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
                		ResolveInfo ri = targetApps.get(i);
        	    	    CharSequence  sa = ri.loadLabel(pm);
        	    	    if (sa == null) sa = ri.activityInfo.name;
        	    	    String sa1 = sa.toString().trim();
        	    	    String sa2 = "";
        	    	    for (int j = 0; j < sa1.length(); j++)
        	    	    	sa2 += HanziToPinyin.getInstance().getToken(sa1.charAt(j)).target; 
        	    		ri.activityInfo.applicationInfo.dataDir = sa2.trim().toUpperCase() ;//we borrow dataDir to store the Pinyin of the label.
        	    		String tmp = ri.activityInfo.applicationInfo.dataDir.substring(0, 1);
        	    		
                    	if ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
            				sysAdapter.add(ri);
            		    	Collections.sort(sysAdapter.localApplist, new myComparator());//sort by name
            		    	
        	    			if (!mSysAlpha.contains(tmp)) {
        	    				sysAlphaAdapter.add(tmp);
            			    	Collections.sort(sysAlphaAdapter.localList, new stringCompatator());
                        		if (sysAlphaAdapter.getCount() < MaxCount) sysAlpha.setNumColumns(sysAlphaAdapter.getCount());
                        		else if (sysAlphaAdapter.getCount() < MaxCount*2) sysAlpha.setNumColumns((int)(sysAlphaAdapter.getCount()/2.0+0.5));
                        		else sysAlpha.setNumColumns(MaxCount);
        	    			}
            		    	break;
                    	}
                    	else {
            				userAdapter.add(ri);
            		    	Collections.sort(userAdapter.localApplist, new myComparator());//sort by name
            		    	
        	    			if (!mUserAlpha.contains(tmp)) {
                				userAlphaAdapter.add(tmp);
            			    	Collections.sort(userAlphaAdapter.localList, new stringCompatator());
                        		if (userAlphaAdapter.getCount() < MaxCount) userAlpha.setNumColumns(userAlphaAdapter.getCount());
                        		else if (userAlphaAdapter.getCount() < MaxCount*2) userAlpha.setNumColumns((int)(userAlphaAdapter.getCount()/2.0+0.5));
                        		else userAlpha.setNumColumns(MaxCount);
        	    			}
            		    	break;
                    	}
                	}
            	}
            }
		}
		
	};

	boolean startApp(ResolveInfo info) {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setComponent(new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name));
		return util.startActivity(i, true, getBaseContext());
	}
	
    private class AlphaAdapter extends ArrayAdapter<String> {
    	ArrayList<String> localList;
        public AlphaAdapter(Context context, List<String> alphas) {
            super(context, 0, alphas);
            localList = (ArrayList<String>) alphas;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.alpha_list, parent, false);
            }
            final TextView btn = (TextView) convertView.findViewById(R.id.alpha);
            btn.setText(localList.get(position));
            btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					String tmp = localList.get(position);
					DuringSelection = true;
					switch(mainlayout.getCurrentItem()) {
					case 0://system app
						for (int i = 0; i < sysAdapter.getCount(); i++) {
							if (sysAdapter.getItem(i).activityInfo.applicationInfo.dataDir.startsWith(tmp)) {
								sysAppList.requestFocusFromTouch();
								sysAppList.setSelection(i);
								break;
							}
						}
						break;
					case 2://user app
						for (int i = 0; i < userAdapter.getCount(); i++) {
							if (userAdapter.getItem(i).activityInfo.applicationInfo.dataDir.startsWith(tmp)) {
								userAppList.requestFocusFromTouch();
								userAppList.setSelection(i);
								break;
							}
						}
						break;
					}
				}
            });
            
        	return convertView;
        }
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
            
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
            
           	if ((info.loadLabel(pm) == textView1.getText()) && (DuringSelection))//don't update the view here 
           		return convertView;//seldom come here
           	
           	if (info.loadLabel(pm) != textView1.getText()) {//only reset the appname, version, icon when needed
               	textView1.setText(info.loadLabel(pm));
               	
                final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.appicon);
                btnIcon.setImageDrawable(info.loadIcon(pm));
                //if (android.os.Build.VERSION.SDK_INT >= 8) btnIcon.setEnabled(false);//currently we can't stop the other app after API level 8 if we have no platform signature
                btnIcon.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View arg0) {//kill app
    					if (! info.activityInfo.packageName.equals(myPackageName)) {//don't kill myself
    						ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    						am.restartPackage(info.activityInfo.packageName);
    						//but we need to know when will it restart by itself?
    						textView1.setTextColor(whiteColor);//set color back after kill it.
    					}
    				}
                });
                
                LinearLayout lapp = (LinearLayout) convertView.findViewById(R.id.app);
                lapp.setOnClickListener(new OnClickListener() {
    				@Override
    				public void onClick(View arg0) {//start app
    					if (startApp(info))//start success
    						textView1.setTextColor(redColor);//red for running apk
    					else {
    						if ((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) //user app
    							userAdapter.remove(info);
    						else
    							sysAdapter.remove(info);//fail to start, remove the item.
    					}
    				}
                });
            	lapp.setTag(new ricase(info, 2));
                registerForContextMenu(lapp); 
                
                final TextView btnVersion = (TextView) convertView.findViewById(R.id.appversion);
                try {
                	String version = pm.getPackageInfo(info.activityInfo.packageName, 0).versionName;
                	if ((version == null) || (version.trim().equals(""))) version = String.valueOf(pm.getPackageInfo(info.activityInfo.packageName, 0).versionCode);
                	btnVersion.setText(version);
    			} catch (NameNotFoundException e) {
    				btnVersion.setText(e.toString());
    			}
    			btnVersion.setOnClickListener(new OnClickListener() {//delete app
    				@Override
    				public void onClick(View arg0) {
    					if ((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {//user app
    						Uri uri = Uri.fromParts("package", info.activityInfo.packageName, null);
    						Intent intent = new Intent(Intent.ACTION_DELETE, uri);
    						util.startActivity(intent, true, getBaseContext());
    						arg0.requestFocus();
    					}
    					else {//system app
    						apkToDel = info.activityInfo.applicationInfo.sourceDir;
    						pkgToDel = info.activityInfo.packageName;
    						showDialog(2);
    					}
    				}
    			});
           	}
           	
            if (!DuringSelection) {//running state, size and color should be updated when not busy each time
                textView1.setTextColor(whiteColor);
                final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningAppProcessInfo> appList = am.getRunningAppProcesses();
                for (int i = 0; i < appList.size(); i++) {//a bottle neck
            		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
                	if (info.activityInfo.processName.equals(as.processName)) {
                    	textView1.setTextColor(redColor);//red for running apk
            			break;
            		}
                }
                
                final TextView textView3 = (TextView) convertView.findViewById(R.id.appsource);
                String source = "";
                Object o = packagesSize.get(info.activityInfo.packageName);
                if(o != null) source = o.toString();
                if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
                	textView3.setTextColor(brownColor);//brown for debuggable apk
                	source += " (debuggable)";
                }
                else textView3.setTextColor(grayColor);//gray for normal
            	textView3.setText(source);
            }
            
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
			fi = openFileInput(name);
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

            readFile("favo");
            readFile("short");
            boolean shortEmpty = mShortApps.isEmpty();
			
	    	//read all resolveinfo
	    	String label_sms = "簡訊 Messaging Messages メッセージ 信息 消息 메시지  Mensajes Messaggi Berichten SMS a MMS SMS/MMS"; //use label name to get short cut
	    	String label_phone = "電話 Phone 电话 拨号键盘 키패드  Telefon Teléfono Téléphone Telefono Telefoon Телефон 휴대전화  Dialer";
	    	String label_contact = "聯絡人 联系人 Contacts People 連絡先 通讯录 전화번호부  Kontakty Kontakte Contactos Contatti Contacten Контакты 주소록";
	    	for (int i = 0; i < mAllApps.size(); i++) {
	    		ResolveInfo ri = mAllApps.get(i);
	    		
	    	    CharSequence  sa = ri.loadLabel(pm);
	    	    if (sa == null) sa = ri.activityInfo.name;
	    	    String sa1 = sa.toString().trim();
	    	    String sa2 = "";
	    	    for (int j = 0; j < sa1.length(); j++)
	    	    	sa2 += HanziToPinyin.getInstance().getToken(sa1.charAt(j)).target; 
	    		ri.activityInfo.applicationInfo.dataDir = sa2.toUpperCase() ;//we borrow dataDir to store the Pinyin of the label.
	    		
    			String tmp = ri.activityInfo.applicationInfo.dataDir.substring(0, 1);
    			
	    		if ((ri.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM) {
	    			mSysApps.add(ri);
	    			
	    			if (!mSysAlpha.contains(tmp)) mSysAlpha.add(tmp);
	    			
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
	    		else {
	    			mUserApps.add(ri);
	    			if (!mUserAlpha.contains(tmp)) mUserAlpha.add(tmp);
	    		}
		    	Collections.sort(mSysApps, new myComparator());//sort by name
		    	Collections.sort(mUserApps, new myComparator());//sort by name
		    	Collections.sort(mSysAlpha, new stringCompatator());
		    	Collections.sort(mUserAlpha, new stringCompatator());
	    		
	    		try {
					getPackageSizeInfo.invoke(pm, ri.activityInfo.packageName, sizeObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
	    	}
        	Message msguser = mAppHandler.obtainMessage();
        	msguser.what = UPDATE_USER;
        	mAppHandler.sendMessage(msguser);//inform UI thread to update UI.
	    	
        	downloadPath = util.preparePath(getFilesDir().getPath());
        	
        	picList = new ArrayList();
        	new File(downloadPath).list(new OnlyPic());
        	if (picList.size() > 0) {
	        	Message msgshake = mAppHandler.obtainMessage();
	        	msgshake.what = UPDATE_SHAKE_WALLPAPER;
	        	mAppHandler.sendMessage(msgshake);
        	}
			   
			try {//for 1.5 which do not have this method
				setPackage = Intent.class.getMethod("setPackage", new Class[] {String.class});
			} catch (Exception e) {
				setPackage = null;
				e.printStackTrace();
			}
			
			try {
				pid = RunningAppProcessInfo.class.getField("pid");
			} catch (Exception e) {//no such field is sdk level <= 3
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
        		
        		sysAlphaAdapter = new AlphaAdapter(getBaseContext(), mSysAlpha);
        		sysAlpha.setAdapter(sysAlphaAdapter);
        		if (sysAlphaAdapter.getCount() < MaxCount) sysAlpha.setNumColumns(sysAlphaAdapter.getCount());
        		else if (sysAlphaAdapter.getCount() < MaxCount*2) sysAlpha.setNumColumns((int)(sysAlphaAdapter.getCount()/2.0+0.5));
        		else sysAlpha.setNumColumns(MaxCount);
        	
        		userAlphaAdapter = new AlphaAdapter(getBaseContext(), mUserAlpha);
        		userAlpha.setAdapter(userAlphaAdapter);
        		if (userAlphaAdapter.getCount() < MaxCount) userAlpha.setNumColumns(userAlphaAdapter.getCount());
        		else if (userAlphaAdapter.getCount() < MaxCount*2) userAlpha.setNumColumns((int)(userAlphaAdapter.getCount()/2.0+0.5));
        		else userAlpha.setNumColumns(MaxCount);

        		break;
        	case UPDATE_RI_PHONE:
        		int missCallCount = callObserver.countUnread();
        		if (missCallCount > 0) {
            		BitmapDrawable bd = (BitmapDrawable) ri_phone.loadIcon(pm);
        			shortcut_phone.setImageBitmap(util.generatorCountIcon(bd.getBitmap(), missCallCount, 1, getBaseContext()));
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
            		shortcut_sms.setImageBitmap(util.generatorCountIcon(bd.getBitmap(), unreadCount, 1, getBaseContext()));
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
        	case UPDATE_SHAKE_WALLPAPER:
        		cbWallPaper.setEnabled(true);
        		break;
        	}
        }
    };

	
	@Override
	protected void onNewIntent(Intent intent) {//go back to home if press Home key.
		if ((intent.getAction().equals(Intent.ACTION_MAIN)) && (intent.hasCategory(Intent.CATEGORY_HOME))) {
			if (mainlayout.getCurrentItem() != homeTab) mainlayout.setCurrentItem(homeTab);
			else if (shortAppList.getVisibility() == View.VISIBLE) shortBar.performClick();
		}
		super.onNewIntent(intent); 
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getRepeatCount() == 0) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {//press Back key in webview will go backword.
				if (mainlayout.getCurrentItem() != homeTab) mainlayout.setCurrentItem(homeTab);
				else if (shortAppList.getVisibility() == View.VISIBLE) shortBar.performClick();
				else this.openOptionsMenu();
				return true;
			}	
		}
		return false;
	}
	
	void setLayout(int width) {
		if (width < 100) return;//can't work on so small screen.
		
        LayoutParams lp = shortcutBar_center.getLayoutParams();
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
				    	Drawable drawable = Drawable.createFromPath(downloadPath + picList.get(id));
				    	ClippedDrawable cd = new ClippedDrawable(drawable, apps.getWidth(), apps.getHeight());
				    	apps.setBackgroundDrawable(cd);
				    	//mWallpaperManager.setBitmap(cd.getBitmap());//null pointer error?
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
		//setLayout(oldW);
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
class ClippedDrawable extends BitmapDrawable {
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
        // Ensure the wallpaper is as large as it really is, to avoid stretching it at drawing time
        int tmpHeight = mWallpaper.getIntrinsicHeight() * screenWidth / mWallpaper.getIntrinsicWidth();
        int tmpWidth = mWallpaper.getIntrinsicWidth() * screenHeight / mWallpaper.getIntrinsicHeight();
        if (tmpHeight >= screenHeight) { 
        	top -= (tmpHeight - screenHeight)/2;
        	mWallpaper.setBounds(left, top, left + screenWidth, top + tmpHeight);
        }
        else {//if the pic width is wider than screen width, then we need show part of the pic.
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

