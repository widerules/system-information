/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package simple.home.jtbuaa;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This activity corresponds to a download progress screen that is displayed 
 * when an application is uninstalled. The result of the application uninstall
 * is indicated in the result code that gets set to 0 or 1. The application gets launched
 * by an intent with the intent's class name explicitly set to UninstallAppProgress and expects
 * the application object of the application to uninstall.
 */
public class UninstallAppProgress extends Activity implements OnClickListener {
    private final String TAG="UninstallAppProgress";
    private boolean localLOGV = false;
    private ApplicationInfo mAppInfo;
    private TextView mStatusTextView;
    private Button mOkButton;
    private ProgressBar mProgressBar;
    private View mOkPanel;
    private volatile int mResultCode = -1;
    private final int UNINSTALL_COMPLETE = 1;
    public final static int SUCCEEDED=1;
    public final static int FAILED=0;
    private static Method deletePackage;
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UNINSTALL_COMPLETE:
                    mResultCode = msg.arg1;
                    // Update the status text
                    if (msg.arg1 == SUCCEEDED) {
                        mStatusTextView.setText(R.string.uninstall_done);
                    } else {
                        mStatusTextView.setText(R.string.uninstall_failed);
                    }
                    mProgressBar.setVisibility(View.INVISIBLE);
                    // Show the ok button
                    mOkPanel.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		try {
			deletePackage = PackageManager.class.getMethod("deletePackage", 
					new Class[] {String.class, IPackageDeleteObserver.class, Integer.class});
		} catch (Exception e) {
			deletePackage = null;
			e.printStackTrace();
		}
		
        Intent intent = getIntent();
        mAppInfo = intent.getParcelableExtra("appinfo");
        initView();
    }
    
    class PackageDeleteObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(boolean succeeded) {
            Message msg = mHandler.obtainMessage(UNINSTALL_COMPLETE);
            msg.arg1 = succeeded?SUCCEEDED:FAILED;
            mHandler.sendMessage(msg);
        }
    }
    
    void setResultAndFinish(int retCode) {
        setResult(retCode);
        finish();
    }
    
    public static View initSnippetForInstalledApp(Activity pContext,
            ApplicationInfo appInfo, int snippetId) {
        View appSnippet = pContext.findViewById(snippetId);
        String pkgName = appInfo.packageName;
        PackageManager pm = pContext.getPackageManager();
        CharSequence label = appInfo.loadLabel(pm);
        Drawable icon = appInfo.loadIcon(pm);
        ((ImageView)appSnippet.findViewById(R.id.app_icon)).setImageDrawable(icon);
        ((TextView)appSnippet.findViewById(R.id.app_name)).setText(label);
        return appSnippet;
    }

    public void initView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.uninstall_progress);
        // Initialize views
        initSnippetForInstalledApp(this, mAppInfo, R.id.app_snippet);
        mStatusTextView = (TextView)findViewById(R.id.center_text);
        mStatusTextView.setText(R.string.uninstalling);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setIndeterminate(true);
        // Hide button till progress is being displayed
        mOkPanel = (View)findViewById(R.id.ok_panel);
        mOkButton = (Button)findViewById(R.id.ok_button);
        mOkButton.setOnClickListener(this);
        mOkPanel.setVisibility(View.INVISIBLE);
        PackageDeleteObserver observer = new PackageDeleteObserver();
    	if (deletePackage != null) {
    		try {
    			deletePackage.invoke(getPackageManager(), mAppInfo.packageName, observer, 0);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
        //getPackageManager().deletePackage(mAppInfo.packageName, observer, 0);
    }

    public void onClick(View v) {
        if(v == mOkButton) {
            Log.i(TAG, "Finished uninstalling pkg: " + mAppInfo.packageName);
            setResultAndFinish(mResultCode);
        }
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (ev.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mResultCode == -1) {
                // Ignore back key when installation is in progress
                return true;
            } else {
                // If installation is done, just set the result code
                setResult(mResultCode);
            }
        }
        return super.dispatchKeyEvent(ev);
    }
}
