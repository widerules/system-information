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
		String titleA = a.m_title;
		try {
			titleA = HanziToPinyin.getInstance().getToken(titleA.charAt(0)).target;
		} catch (Exception e) {
		}

		String titleB = b.m_title;
		try {
			titleB = HanziToPinyin.getInstance().getToken(titleB.charAt(0)).target;
		} catch (Exception e) {
		}

		return sCollator.compare(titleA, titleB);
	}

	private final Collator sCollator = Collator.getInstance();
}