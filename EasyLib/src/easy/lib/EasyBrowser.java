package easy.lib;

import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import common.lib.SimpleBrowser;

public class EasyBrowser extends SimpleBrowser {
	ImageView imgHome;

	void initImgHome() {
		imgHome = (ImageView) findViewById(R.id.home);
		imgHome.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if ((appstate.m_homepage != null) && (!"".equals(appstate.m_homepage))) appstate.serverWebs.get(appstate.webIndex).loadUrl(appstate.m_homepage);
				else if (!appstate.HOME_PAGE.equals(appstate.serverWebs.get(appstate.webIndex).m_url)) appstate.loadPage();
			}
		});
		imgHome.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				appstate.globalSetting();
				return true;
			}
		});
	}

	void initImgGo() {
		imgGo = (ImageView) findViewById(R.id.go);
		imgGo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				appstate.gotoUrl(appstate.webAddress.getText().toString().toLowerCase());
			}
		});
		imgGo.setOnLongClickListener(new OnLongClickListener() {// long click to select search engine
			@Override
			public boolean onLongClick(View arg0) {
				CharSequence engine[] = new CharSequence[] {getString(R.string.bing), getString(R.string.baidu), getString(R.string.google), getString(R.string.yandex), getString(R.string.duckduckgo)};
				appstate.selectEngine(engine);
				return true;
			}
		});
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		appstate.browserName = getString(R.string.browser_name);
		
		setAsDefaultApp();
		
		initImgHome();
		initImgGo();
	}
	
	public void setDefault(PackageManager pm, Intent intent, IntentFilter filter) {
		List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);
		int size = resolveInfoList.size();
		ComponentName[] arrayOfComponentName = new ComponentName[size];
		boolean seted = false;
		for (int i = 0; i < size; i++) {
			ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
			String packageName = activityInfo.packageName;
			String className = activityInfo.name;
			//clear default browser
			if (packageName.equals(mContext.getPackageName())) {
				seted = true;
				break;
			}
			try{pm.clearPackagePreferredActivities(packageName);} catch(Exception e) {}
			ComponentName componentName = new ComponentName(packageName, className);
			arrayOfComponentName[i] = componentName;
		}
		
		if (!seted) {
			ComponentName component = new ComponentName(mContext.getPackageName(), "easy.lib.SimpleBrowser");
			pm.addPreferredActivity(filter,	IntentFilter.MATCH_CATEGORY_SCHEME, arrayOfComponentName, component);
		}
	}

	public void setAsDefaultApp() {
		PackageManager pm = getPackageManager();
		
		try {pm.addPackageToPreferred(getPackageName());}// for 1.5 platform 
		catch(Exception e) {
			// set default browser for 1.6-2.1 platform. not work for 2.2 and up platform
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.BROWSABLE");
			intent.addCategory("android.intent.category.DEFAULT");
			
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.intent.action.VIEW");
			filter.addCategory("android.intent.category.BROWSABLE");
			filter.addCategory("android.intent.category.DEFAULT");
			filter.addDataScheme("http");
			
			Uri uri = Uri.parse("http://");
			intent.setDataAndType(uri, null);
			setDefault(pm, intent, filter);			
		} 
	}
}
