package simple.home.jtbuaa;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

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

	static public String preparePath(String defaultPath) {
		String downloadPath = defaultPath + "/";
		
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
        else {//for miss call and unread sms
            countPaint.setColor(Color.DKGRAY);  
            countPaint.setTextSize(25f);  
            countPaint.setTypeface(Typeface.DEFAULT_BOLD);  
            canvas.drawText(String.valueOf(count), iconSize-30, 20, countPaint);
        }
        return contactIcon;  
    }  

}
