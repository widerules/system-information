package common.lib;

import java.util.Locale;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import base.lib.WrapAdView;
import base.lib.WrapInterstitialAd;

import common.lib.MyApp;
import easy.lib.R;

public class EasyApp extends MyApp {
	public boolean collapse1 = false;// default open top list and bookmark
	public boolean collapse2 = false;
	public boolean collapse3 = true;
	int countDown = 0;
	
	public LinearLayout adRealContainer;

	public void readPreference() {
		super.readPreference();
		
		collapse1 = sp.getBoolean("collapse1", false);
		collapse2 = sp.getBoolean("collapse2", true);
		collapse3 = sp.getBoolean("collapse3", true);
	}
	
	public void createAd(float width) {
		if (mAdAvailable) {
			//removeAd();

			//if ((cm == null) || (cm.getActiveNetworkInfo() == null)
			//		|| !cm.getActiveNetworkInfo().isConnected())
			//	return;// not create ad if network error

			 if (width < 320) ;//do nothing for it is too narrow. 
             // but it will cause force close if not create adview?
             if (width < 468)// AdSize.BANNER require 320*50
                     adview = new WrapAdView(mActivity, 0, "a14f3f6bc126143", mAppHandler);
             else if (width < 728)
                     adview = new WrapAdView(mActivity, 1, "a14f3f6bc126143", mAppHandler);
                     // AdSize.IAB_BANNER require 468*60 but return 702*90 on BKB(1024*600) and S1.
                     // return width = request width * density.
             else    // AdSize.IAB_LEADERBOARD require 728*90, return 1092*135 on BKB
                     adview = new WrapAdView(mActivity, 2, "a14f3f6bc126143", mAppHandler);
			if ((adview != null) && (adview.getInstance() != null)) {
				adRealContainer.addView(adview.getInstance());
				adview.loadAd();
			}

			adview2 = new WrapAdView(mActivity, 0, "a14d662bba1e443", null);// AdSize.BANNER require 320*50
			if ((adview2 != null) && (adview2.getInstance() != null)) { 
				adContainer2.addView(adview2.getInstance());
				adview2.loadAd();
			}
			
			interstitialAd = new WrapInterstitialAd(mActivity, "a14be3f4ec2bb11", mAppHandler);
		}
	}

	public void actionBack() {
		// press Back key in webview will go backword.
		if (webControl.getVisibility() == View.VISIBLE)
			webControl.setVisibility(View.INVISIBLE);// hide web control
		else if ((searchBar != null) && searchBar.getVisibility() == View.VISIBLE)
			hideSearchBox();
		else if (HOME_BLANK.equals(webAddress.getText().toString())) {
			// hide browser when click back key on homepage.
			// this is a singleTask activity, so if return
			// super.onKeyDown(keyCode, event), app will exit.
			// when use click browser icon again, it will call onCreate,
			// user's page will not reopen.
			// singleInstance will work here, but it will cause
			// downloadControl not work? or select file not work?
			if (serverWebs.size() == 1)
				mActivity.moveTaskToBack(true);
			else closePage(webIndex, false); // close blank page if more than one page
		} else if (serverWebs.get(webIndex).canGoBack())
			serverWebs.get(webIndex).goBack();
		else
			closePage(webIndex, false);// close current page if can't go back
	}
	
	public void updateBookmark() {
		serverWebs.get(webIndex).loadUrl("javascript:inject(\"2::::" + getBookmark("....") + "\");");// call javascript to inject bookmark
	}
	
	public void updateHistory() {
		serverWebs.get(webIndex).loadUrl("javascript:inject(\"3::::" + getHistory("....") + "\");");// call javascript to inject bookmark
	}
	
	public void updateHomePage() {
		serverWebs.get(webIndex).loadUrl("javascript:setTitle(\"" + getString(R.string.browser_name) + "\");");
		// top bar
		String tmp = getString(R.string.top);
		if (mAdAvailable) {
			if (countDown > 0) tmp += getString(R.string.url_can_longclick);
			serverWebs.get(webIndex).loadUrl("javascript:setTitleBar(\"1," + collapse1 + "," + tmp + "\");");
			serverWebs.get(webIndex).loadUrl("javascript:collapse(\"1," + !collapse1 + "\");");
			serverWebs.get(webIndex).loadUrl("javascript:inject(\"1::::" + getTopList("....") + "\");");// call javascript to inject toplist
		}
		else
			serverWebs.get(webIndex).loadUrl("javascript:hideTop();");// hide toplist for pro version
		
		// bookmark bar
		tmp = getString(R.string.bookmark);
		if (countDown > 0) tmp += getString(R.string.pic_can_longclick);
		serverWebs.get(webIndex).loadUrl("javascript:setTitleBar(\"2," + collapse2 + "," + tmp + "\");");

		// history bar
		tmp = getString(R.string.history);
		if (countDown > 0) tmp += getString(R.string.text_can_longclick);
		serverWebs.get(webIndex).loadUrl("javascript:setTitleBar(\"3," + collapse3 + "," + tmp + "\");");

		serverWebs.get(webIndex).loadUrl("javascript:collapse(\"2," + !collapse2 + "\");");
		serverWebs.get(webIndex).loadUrl("javascript:collapse(\"3," + !collapse3 + "\");");
		
		updateBookmark();
		updateHistory();

		serverWebs.get(webIndex).loadUrl("javascript:setButton(\"" + getString(R.string.edit_home) + "," + getString(R.string.delete) + "," + getString(R.string.cancel) + "\");");
		
		if (countDown > 0) countDown -= 1;
	}
	
	String getTopList(String splitter) {
		String fileDir = "<li style='background-image:url(file://" + getFilesDir().getAbsolutePath() + "/";
		
		StringBuilder sb = new StringBuilder("");	
		if (Locale.CHINA.equals(mLocale) || Locale.TAIWAN.equals(mLocale)) {
			sb.append(fileDir);
			sb.append("easybrowser.shupeng.com.png)'><a href='http://easybrowser.shupeng.com'>书朋小说网</a></li>");
			//sb.append("tiantian.m.the9.com.png)'><a href='http://tiantian.m.the9.com'>热门游戏</a></li>");
			sb.append(splitter);
			sb.append(fileDir);
			sb.append("weibo.com.png)'><a href='http://weibo.com'>新浪微博</a></li>");
			sb.append(splitter);
			// sb.append(fileDir);
			// sb.append("www.taobao.com.png)'><a href='http://www.taobao.com'>淘宝</a></li>");
			sb.append(fileDir);
			sb.append("wap.easou.com.png)'><a href='http://i5.easou.com/kw.m?tp=7&p=1&cid=bip1065_10713_001&esid=2GsaHSnARzA&si=a91fc744144b561694707ad18923b4f9&wver=t'>宜搜</a></li>");
			sb.append(splitter);
			//sb.append("<li><a href='http://www.9yu.co/index.html?c=2'>美图</a></li>");// no favicon
			// sb.append(fileDir);
			// sb.append("bpc.borqs.com.png)'><a href='http://bpc.borqs.com'>梧桐</a></li>");
		} else {
			// sb.append("<li><a href='http://www.1mobile.com/app/market/?cid=9'>1mobile</a></li>");// no favicon
			//if (mAdAvailable) sb.append("<li style='background-image:url(file:///android_asset/favicon.ico)'><a href='http://bpc.borqs.com/market.html?id=easy.browser.pro'>Ad free version of Easy Browser</a></li>"); // suspended
			sb.append(fileDir);
			sb.append("m.admob.com.png)'><a style='text-decoration: underline; color:#0000ff' onclick='javascript:window.JSinterface.showInterstitialAd()'>AdMob</a></li>");
			sb.append(splitter);
			sb.append(fileDir);
			sb.append("helpx.adobe.com.png)'><a href='http://tinyurl.com/4aflash'>Adobe Flash player install/update</a></li>");
			sb.append(splitter);
			sb.append("<li style='background-image:url(file:///android_asset/favicon.ico)'><a href='market://details?id=easy.browser.com'>Easy Browser Pro, no Ads</a></li>");
			sb.append(splitter);
			//sb.append(fileDir);
			//sb.append("duckduckgo.com.png)'><a href='https://duckduckgo.com?t=easybrowser&q=DuckDuckGo'>DuckDuckGo</a></li>");
			//sb.append(splitter);
			//sb.append(fileDir);
			//sb.append("m.facebook.com.png)'><a href='http://www.facebook.com'>Facebook</a></li>");
			//sb.append(splitter);
			sb.append(fileDir);
			sb.append("www.moborobo.com.png)'><a href='http://www.moborobo.com/app/mobomarket.html'>MoboMarket</a></li>");
			sb.append(splitter);
			//sb.append("<li><a href='file:///sdcard/'>SDcard</a></li>");
			//sb.append(splitter);
			//sb.append(fileDir);
			//sb.append("mobile.twitter.com.png)'><a href='http://twitter.com'>Twitter</a></li>");
			//sb.append(splitter);
			
			// additional top list for some locale
			if (Locale.JAPAN.equals(mLocale) || Locale.JAPANESE.equals(mLocale)) {
				sb.append(fileDir);
				sb.append("m.yahoo.co.jp.png)'><a href='http://www.yahoo.co.jp'>Yahoo!JAPAN</a></li>");
				sb.append(splitter);
			} else if ("ru_RU".equals(mLocale.toString())) {
				sb.append(fileDir);
				sb.append("www.yandex.ru.png)'><a href='http://www.yandex.ru/?clid=1911433'>Яндекс</a></li>");
				sb.append(splitter);
			}
		}
		
		return sb.toString();
	}
	
	String getBookmark(String splitter) {
		String fileDir = "<li style='background-image:url(file://" + getFilesDir().getAbsolutePath() + "/";
		
		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < mBookMark.size(); i++) {
			sb.append(fileDir);
			sb.append(mBookMark.get(i).m_site);
			sb.append(".png)'><input class='bookmark' type='checkbox' style='display:none; margin-right:20px'><a href='");
			sb.append(mBookMark.get(i).m_url);
			sb.append("'>");
			sb.append(mBookMark.get(i).m_title);
			sb.append("</a></li>");
			sb.append(splitter);
		}
		
		return sb.toString();
	}
	
	String getHistory(String splitter) {
		String fileDir = "<li style='background-image:url(file://" + getFilesDir().getAbsolutePath() + "/";
		
		StringBuilder sb = new StringBuilder("");			
		for (int i = mHistory.size() - 1; i >= 0; i--) {
			sb.append(fileDir);
			sb.append(mHistory.get(i).m_site);
			sb.append(".png)'><input class='history' type='checkbox' style='display:none; margin-right:20px'><a href='");
			sb.append(mHistory.get(i).m_url);
			sb.append("'>");
			sb.append(mHistory.get(i).m_title);
			sb.append("</a></li>");
			sb.append(splitter);
		}
		
		return sb.toString();
	}
	
	public void pauseAction() {
		super.pauseAction();
		
		sEdit.putBoolean("collapse1", collapse1);
		sEdit.putBoolean("collapse2", collapse2);
		sEdit.putBoolean("collapse3", collapse3);
	}
}
