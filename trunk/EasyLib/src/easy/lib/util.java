package easy.lib;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

class TitleUrl {
	String m_title;
	String m_url;
	String m_site;
	
	TitleUrl(String title, String url, String site) {
		m_title =  title;
		m_url = url;
		m_site = site;
	}
}


public class util {
	
    static public boolean startActivity(Intent intent, boolean showToast, Context context) {
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			context.startActivity(intent);
			return true;
		} catch (Exception e) {
			if (showToast)
				Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return false;
		}
    }

	static public boolean startApp(ResolveInfo info, Context context) {
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setComponent(new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name));
		return startActivity(i, true, context);
	}	

    static public String getVersion(Context context) {
    	String version = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) 
				version = pi.versionName == null ? String.valueOf(pi.versionCode) : pi.versionName;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return version;
    }
    
    static public String getVersionCode(Context context) {
    	String version = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (pi != null) 
				version = String.valueOf(pi.versionCode);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return version;
    }
    
	static public String preparePath(Context context) {
		String defaultPath = "/data/data/" + context.getPackageName();
		try {
			defaultPath = context.getFilesDir().getPath(); 
		}
		catch (Exception e) {}
		
		String downloadPath = defaultPath + "/";
		
    	String status = Environment.getExternalStorageState();
    	if (status.equals(Environment.MEDIA_MOUNTED)) {
    		downloadPath = Environment.getExternalStorageDirectory() + "/simpleHome/";   
			java.io.File myFilePath = new java.io.File(downloadPath);
			try
			{
			    if(myFilePath.isDirectory()) ;//folder exist
			    else myFilePath.mkdir();//create folder
			    
				java.io.File snapPath = new java.io.File(downloadPath + "snap/");
			    if(snapPath.isDirectory()) ;//folder exist
			    else snapPath.mkdir();//create folder
			    
				java.io.File apkPath = new java.io.File(downloadPath + "apk/");
			    if(apkPath.isDirectory()) ;//folder exist
			    else apkPath.mkdir();//create folder
			}
			catch(Exception e) {
				e.printStackTrace();
				downloadPath = defaultPath + "/";
			}
    	}
    	
    	return downloadPath;
	}
	
    /** 
     * get bitmap from resource id 
     * @param res 
     * @param resId 
     * @return 
     */  
    static public Bitmap getResIcon(Resources res,int resId){  
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
    static public Bitmap generatorCountIcon(Bitmap icon, int count, int scheme, Context context){  
        //初始化画布  
        int iconSize=(int)context.getResources().getDimension(android.R.dimen.app_icon_size);  
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
        else if (scheme == 1) {//for miss call and unread sms
            countPaint.setColor(Color.DKGRAY);  
            countPaint.setTextSize(25f);  
            countPaint.setTypeface(Typeface.DEFAULT_BOLD);  
            canvas.drawText(String.valueOf(count), iconSize-30, 20, countPaint);
        }
        else {//for easy browser. i don't know why the font change if invoke from easy browser. if from eash home, it is ok for 25f.
            countPaint.setColor(Color.BLACK);  
            countPaint.setTextSize(20f);  
            canvas.drawText(String.valueOf(count), iconSize/2-3, iconSize/2+10, countPaint);
        }
        return contactIcon;  
    }  

    
    static void writeBookmark(FileOutputStream fo, ArrayList<TitleUrl> bookmark) {
    	try {
    		ObjectOutputStream oos = new ObjectOutputStream(fo);
    		TitleUrl tu;
    			for (int i = 0; i < bookmark.size(); i++) {
    				tu = bookmark.get(i);
    				oos.writeObject(tu.m_title);
    				oos.writeObject(tu.m_url);
    				oos.writeObject(tu.m_site);
    			}
    		oos.flush();
    		oos.close();
    		fo.close();
    	} catch (Exception e) {}
    }

    static ArrayList<TitleUrl> readBookmark(FileInputStream fi) 
    {
    	ArrayList<TitleUrl> bookmark = new ArrayList<TitleUrl>();
    	ObjectInputStream ois = null;
    	try {//read favorite or shortcut data
    		ois = new ObjectInputStream(fi);
    		TitleUrl tu;
    		String title, url, site;
    		while ((title = (String) ois.readObject()) != null) {
    			url = (String) ois.readObject();
    			site = (String) ois.readObject();
    			tu = new TitleUrl(title, url, site);
    			bookmark.add(tu);
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
    	return bookmark;
    }

}
