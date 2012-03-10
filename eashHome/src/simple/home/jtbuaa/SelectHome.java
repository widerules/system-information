package simple.home.jtbuaa;

import java.util.Collections;
import java.util.List;

import easy.lib.R;
import easy.lib.wrapAdView;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class SelectHome extends Activity{
	
	static boolean mAdAvailable;
	static {
	       try {
	    	   wrapAdView.checkAvailable();
	           Class.forName("com.google.ads.AdView");
	    	   mAdAvailable = true;
	       } catch (Throwable t) {
	    	   mAdAvailable = false;
	       }
	   }
	wrapAdView adview;
	LinearLayout adContainer;

	private List<ResolveInfo> mHomeList;
	private int currentHomeIndex = 0;
	String mPackageName;
	PackageManager mPM;
	
	private void removeDefaultHome() {//refer to http://www.dotblogs.com.tw/neil/archive/2011/08/12/33058.aspx
        String activityName = FakeHome.class.getName();
        String packageName = this.getPackageName();
        ComponentName fakeHome = new ComponentName(packageName, activityName);
        
        mPM.setComponentEnabledSetting(fakeHome, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 1);
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);
        mPM.setComponentEnabledSetting(fakeHome, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 1);
        
        Settings.System.putString(getContentResolver(), "configured_home", "");

        //we don't start myself again for we use system chooser to select home, and won't go back to myself
        //Intent intent = new Intent();
        //intent.setClassName(getApplicationContext(), SelectHome.class.getName());
        //startActivity(intent);
	}

	private void selectOphoneHome() {
        mPackageName = this.getPackageName();
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_HOME);
    	mHomeList = mPM.queryIntentActivities(mainIntent, 0);
    	Collections.sort(mHomeList, new ResolveInfo.DisplayNameComparator(mPM));//sort by name

    	String myName = "";
    	String configuredHome = Settings.System.getString(getContentResolver(), "configured_home");
        if (configuredHome == null) {//first run after install
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (int i = 0; i < mHomeList.size(); i++) {
            	String oldName = mHomeList.get(i).activityInfo.packageName;
            	if (!oldName.equals(mPackageName))
        			am.restartPackage(oldName);//kill all old home
            	else myName = mHomeList.get(i).activityInfo.name;
            }
            
            //start myself
            Intent intent =  new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setClassName(mPackageName, mPackageName+".simpleHome");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
            
            Settings.System.putString(getContentResolver(), "configured_home", myName); 

            finish();
        }
        else {
            int N = mHomeList.size();
            CharSequence[] mValue = new CharSequence[N];
            CharSequence[] mTitle = new CharSequence[N];
            for (int i = 0; i < N; i++) {
                ResolveInfo ri = mHomeList.get(i);
                mValue[i] = Integer.toString(i);
                mTitle[i] = ri.activityInfo.loadLabel(mPM);
                if (configuredHome != null && configuredHome.equals(ri.activityInfo.name)) currentHomeIndex = i;
            }
            new AlertDialog.Builder(this).setTitle(R.string.menu_choose_home).setSingleChoiceItems(mTitle, currentHomeIndex,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Settings.System.putString(getContentResolver(), "configured_home",
                            mHomeList.get(which).activityInfo.name);

                    String oldName = mHomeList.get(currentHomeIndex).activityInfo.packageName;
                    if (!mPackageName.equals(oldName)) {//try to close the old home
    					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    					am.restartPackage(oldName);
                    }
                    else {//if I'm the old home, not stop me immediately, otherwise the new home may not launch 
    					Intent intentNewhome = new Intent("simpleHome.action.HOME_CHANGED");
    					intentNewhome.putExtra("old_home", oldName);
    	                sendBroadcast(intentNewhome);
                    }
                    
                    //launch the new home
                    Intent intent =  new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setClassName(mHomeList.get(which).activityInfo.packageName, 
                    		mHomeList.get(which).activityInfo.name);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(intent);
                    
                    dialog.cancel();
                	finish();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int arg1) {
                    dialog.cancel();
    			}
            }).show();
    	}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	mPM = getPackageManager();
    	
        setContentView(R.layout.select_home);
    	
        boolean paid = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("paid", false);
        if (!paid && mAdAvailable) {
    		adview = new wrapAdView(this, 3, "a14e79197567476");
    		adContainer = (LinearLayout) findViewById(R.id.adContainer);
    		adContainer.addView(adview.getInstance());
    		adview.loadAd();
        }
        
        Button btnSelect = (Button) findViewById(R.id.select);
        btnSelect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					Class.forName("oms.content.Action");//ophone
			        selectOphoneHome();
				} catch (ClassNotFoundException e) {//android phone
		        	removeDefaultHome();
				}
			}
        });
	}
    	
}
