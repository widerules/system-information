package base.lib;

import java.text.Collator;
import java.util.Comparator;

import android.content.pm.ResolveInfo;

public class stringComparator implements Comparator<String> {
	public stringComparator() {
	}

	public final int compare(String a, String b) {
		if (a == null) return -1;
		else if (b == null) return 1;
		else return sCollator.compare(a, b);
	}

	private final Collator sCollator = Collator.getInstance();
}