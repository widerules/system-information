package easy.lib;

import java.text.Collator;
import java.util.Comparator;

import easy.lib.SimpleBrowser.TitleUrl;

import android.content.pm.ResolveInfo;

public class myComparator implements Comparator<TitleUrl> {
	public myComparator() {
	}

	@Override
	public final int compare(TitleUrl a, TitleUrl b) {
	    return sCollator.compare(a.m_title, b.m_title);
	}

	private final Collator sCollator = Collator.getInstance();
}