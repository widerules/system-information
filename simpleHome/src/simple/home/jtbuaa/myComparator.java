package simple.home.jtbuaa;

import java.text.Collator;
import java.util.Comparator;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class myComparator implements Comparator<ResolveInfo> {
	public myComparator(PackageManager pm) {
	    mPM = pm;
	}

	public final int compare(ResolveInfo a, ResolveInfo b) {
	    CharSequence  sa = a.loadLabel(mPM);
	    if (sa == null) sa = a.activityInfo.name;
	    CharSequence  sb = b.loadLabel(mPM);
	    if (sb == null) sb = b.activityInfo.name;

	    String sa1 = sa.toString().trim();
	    String sb1 = sb.toString().trim();
	    
	    String sa2 = "";
	    String sb2 = "";
	    
	    for (int i = 0; i < sa1.length(); i++)
	    	sa2 += HanziToPinyin.getInstance().getToken(sa1.charAt(i)).target; 
	    for (int i = 0; i < sb1.length(); i++)
	    	sb2 += HanziToPinyin.getInstance().getToken(sb1.charAt(i)).target;
	    
	    return sCollator.compare(sa2, sb2);
	}

	private final Collator   sCollator = Collator.getInstance();
	private PackageManager   mPM;
}