package simple.home.jtbuaa;

import java.text.Collator;
import java.util.Comparator;

import android.content.pm.ResolveInfo;

public class stringCompatator implements Comparator<String> {
	public stringCompatator() {
	}

	public final int compare(String a, String b) {
	    return sCollator.compare(a, b);
	}

	private final Collator   sCollator = Collator.getInstance();
}