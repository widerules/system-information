package simple.home.jtbuaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class About extends Activity{
	
	CheckBox cbShake;
	SharedPreferences perferences;
	
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
    	DisplayMetrics dm = new DisplayMetrics();  
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		String res = runCmd("cat", "/proc/cpuinfo")
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.about);

        TextView tvTitle = (TextView) findViewById(R.id.title);
        tvTitle.setText(getString(R.string.app_name) + " " + getIntent().getStringExtra("version"));
        
        TextView tvHelp = (TextView) findViewById(R.id.help);
        tvHelp.setText(getString(R.string.help_message)
        		+ "\n\n" + getString(R.string.about_dialog_notes));
        
        Button btnVote = (Button) findViewById(R.id.vote);
        btnVote.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=simple.home.jtbuaa"));
				if (!util.startActivity(intent, false, getBaseContext())) {
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("https://market.android.com/details?id=simple.home.jtbuaa"));
					intent.setComponent(getComponentName());
					util.startActivity(intent, false, getBaseContext());
				}
			}
        });

        TextView tvMailTo = (TextView) findViewById(R.id.mailto);
        tvMailTo.setText(Html.fromHtml("<u>"+ getString(R.string.author) +"</u>"));
        tvMailTo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.author), null));
				util.startActivity(intent, true, getBaseContext());
			}
		});
        
        TextView tvInfo = (TextView) findViewById(R.id.info);
        tvInfo.setText(aboutMsg());
        
        Button btnShareHome = (Button) findViewById(R.id.title);
        btnShareHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
        });

    	final String downloadPath = util.preparePath(getFilesDir().getPath());

        Button btnShareDesktop = (Button) findViewById(R.id.share_desktop);
        btnShareDesktop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intentShareDesktop = new Intent("simpleHome.action.SHARE_DESKTOP");
                sendBroadcast(intentShareDesktop);//need get screen of home, so send intent to home
			}
        });

        Button btnShareWallpaper = (Button) findViewById(R.id.share_wallpaper);
        btnShareWallpaper.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String file = getIntent().getStringExtra("filename");
				if (file.equals("")) {
					file = downloadPath + "snap/snap.png";
					try {
						FileOutputStream fos = new FileOutputStream(file); 
						BitmapDrawable bd = (BitmapDrawable) WallpaperManager.getInstance(getBaseContext()).getDrawable();
				        bd.getBitmap().compress(Bitmap.CompressFormat.PNG, 90, fos);
				        fos.close();
					} catch (Exception e) {
						Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
						return;
					} 
				}
		        Intent intent = new Intent(Intent.ACTION_SEND);
		        intent.setType("image/*");  
		        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share); 
				intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(file)));
		        util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, getBaseContext());
			}
        });
        
        final String myPackageName = getApplication().getPackageName();
        TextView tvDownload1 = (TextView) findViewById(R.id.download_wallpaper1);
        tvDownload1.setText(Html.fromHtml("<u>" + getString(R.string.wallpaper_scene) + "</u>"));
        tvDownload1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.baidu.com/img?tn=bdidxiphone&ssid=0&from=844b&bd_page_type=1&uid=wiaui_1326248214_3176&pu=sz%401320_480&itj=41"));
				intent.setClassName(myPackageName, myPackageName+".SimpleBrowser");
				util.startActivity(intent, false, getBaseContext());
			}
		});
        TextView tvDownload2 = (TextView) findViewById(R.id.download_wallpaper2);
        tvDownload2.setText(Html.fromHtml("<u>" + getString(R.string.wallpaper_beauty) + "</u>"));
        tvDownload2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://m.baidu.com/img?tn=bdLISTIphone&word=%E9%BB%91%E4%B8%9D&st=103110&prest=11105i&pn=0&rn=10&vit=aladdin&from=844b&ssid=0&bd_page_type=1&ref=www_iphone&uid=wiaui_1325637338_1514"));
				intent.setClassName(myPackageName, myPackageName+".SimpleBrowser");
				util.startActivity(intent, false, getBaseContext());
			}
		});
        
		perferences = PreferenceManager.getDefaultSharedPreferences(this);
        cbShake = (CheckBox) findViewById(R.id.change_wallpaper);
        cbShake.setEnabled(perferences.getBoolean("shake_enabled", false));
        cbShake.setChecked(perferences.getBoolean("shake", false));
        cbShake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		SharedPreferences.Editor editor = perferences.edit();
        		editor.putBoolean("shake", cbShake.isChecked());
        		editor.commit();
			}
        });
        
        /*TextView tvFellow = (TextView) findViewById(R.id.fellow);
        tvFellow.setText(Html.fromHtml("<u>腾讯应用中心</u>"));
        tvFellow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://app.qq.com/g/s?aid=index&g_f=990424"));
				util.startActivity(intent, false, getBaseContext());
			}
		});*/
	}
	
	@Override
	protected void onResume() {
        cbShake.setEnabled(perferences.getBoolean("shake_enabled", false));
        cbShake.setChecked(perferences.getBoolean("shake", false));
        
		super.onResume();
	}
}
