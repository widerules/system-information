package simple.home.jtbuaa;

import java.util.Collections;
import java.util.HashMap;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class AppAlphaList extends AlphaList<ResolveInfo> {

	AppAlphaList(Context context, PackageManager pmgr, boolean largeScreen,
			HashMap<String, Object> packageSize) {
		super(context, pmgr, largeScreen, packageSize);
	}

	String getAlpha(ResolveInfo info) {
		return info.activityInfo.applicationInfo.dataDir;
	}
	
	String getPackageName(ResolveInfo info) {
		return info.activityInfo.packageName;
	}
	
	String getProcessName(ResolveInfo info) {
		return info.activityInfo.processName;
	}
	
	String getLabel(ResolveInfo info) {
		return info.loadLabel(pm).toString();
	}
	
	String getSourceDir(ResolveInfo info) {
		return info.activityInfo.applicationInfo.sourceDir;
	}
	
	Drawable getIcon(ResolveInfo info) {
		return info.loadIcon(pm);
	}
	
	int getFlag(ResolveInfo info) {
		return info.activityInfo.applicationInfo.flags;
	}
	
	boolean start(ResolveInfo info) {
		return util.startApp(info, mContext);
	}
	
	void sort() {
		Collections.sort(mApps, new myComparator());//sort by name
	}
}
