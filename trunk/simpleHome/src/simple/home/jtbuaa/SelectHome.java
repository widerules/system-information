package simple.home.jtbuaa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class SelectHome extends Activity{
	private List<ResolveInfo> mHomeList;
	private int currentHomeIndex = 0;
	String mPackageName;
	PackageManager mPM;
	
	public ComponentName queryDefaultHome() {
        List<IntentFilter> intentList = new ArrayList<IntentFilter>();
        List<ComponentName> cnList = new ArrayList<ComponentName>();
        mPM.getPreferredActivities(intentList, cnList, null);
        IntentFilter dhIF;
        for(int i = 0; i < cnList.size(); i++) {
            dhIF = intentList.get(i);
            if(dhIF.hasAction(Intent.ACTION_MAIN) &&
          		dhIF.hasCategory(Intent.CATEGORY_HOME) &&
           		dhIF.hasCategory(Intent.CATEGORY_DEFAULT)) {
                return cnList.get(i);
            }
        }
        return null;
	}
	
	private void removeDefaultHome() {
        String activityName = FakeHome.class.getName();
        String packageName = this.getPackageName();
        ComponentName fakeHome = new ComponentName(packageName, activityName);
        
        mPM.setComponentEnabledSetting(fakeHome, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 1);
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);
        mPM.setComponentEnabledSetting(fakeHome, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 1);
        
        Intent intent = new Intent();
        intent.setClassName(getApplicationContext(), SelectHome.class.getName());
        startActivity(intent);
	}
	
	private void setDefaultHome(String packageName, String activityName) {
		IntentFilter filter = new IntentFilter("android.intent.action.MAIN");
		filter.addCategory("android.intent.category.HOME");
		filter.addCategory("android.intent.category.DEFAULT");
		
		ComponentName component = new ComponentName(packageName, activityName);
    	mPM.addPreferredActivity(filter, IntentFilter.MATCH_CATEGORY_EMPTY, new ComponentName[] {component}, component);
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mPackageName = this.getPackageName();
        
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_HOME);
    	mPM = getPackageManager();
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
                    //select current home, do nothing
                    if(which != currentHomeIndex) {
                    	//removeDefaultHome();
                    	//setDefaultHome(mHomeList.get(which).activityInfo.packageName, mHomeList.get(which).activityInfo.name);
                    	
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
                    }
                    
                    dialog.cancel();
                	finish();
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    			@Override
    			public void onClick(DialogInterface dialog, int arg1) {
                    dialog.cancel();
    				finish();
    			}
            }).setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface arg0) {
					finish();
				}
            }).show();
    	}
	}
}
