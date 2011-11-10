package simple.home.jtbuaa;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;

public class SelectHome extends Activity{
	private List<ResolveInfo> mHomeList;
	private int currentHomeIndex = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    	mainIntent.addCategory(Intent.CATEGORY_HOME);
        PackageManager pm = getPackageManager();
    	mHomeList = pm.queryIntentActivities(mainIntent, 0);
    	Collections.sort(mHomeList, new ResolveInfo.DisplayNameComparator(pm));//sort by name
        
        int N = mHomeList.size();
        String configuredHome = Settings.System.getString(getContentResolver(), "configured_home");
        CharSequence[] mValue = new CharSequence[N];
        CharSequence[] mTitle = new CharSequence[N];
        for (int i = 0; i < N; i++) {
            ResolveInfo ri = mHomeList.get(i);
            mValue[i] = Integer.toString(i);
            mTitle[i] = ri.activityInfo.loadLabel(pm);
            if (configuredHome != null && configuredHome.equals(ri.activityInfo.name)) {
                currentHomeIndex = i;
            }
        }
        new AlertDialog.Builder(this).setTitle(R.string.menu_choose_home).setSingleChoiceItems(mTitle, currentHomeIndex,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //select current home, do nothing
                if(which != currentHomeIndex) {
                    Settings.System.putString(getContentResolver(), "configured_home",
                            mHomeList.get(which).activityInfo.name);

					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					am.restartPackage(mHomeList.get(currentHomeIndex).activityInfo.packageName);

                    Intent intent =  new Intent(Intent.ACTION_MAIN, null);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(intent);
                }
                else {
                    dialog.cancel();
                	finish();
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
				finish();
			}
        }).show();
	}
}
