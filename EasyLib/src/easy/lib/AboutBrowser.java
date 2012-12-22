package easy.lib;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import easy.lib.util;

public class AboutBrowser extends Activity {

	CheckBox cbEnableProxy, cbBlockPopup, cbBlockJs, cbCacheToSD,
			cbZoomControl, cbHtml5, cbBlockImg, cbCachePrefer,
			cbOverview, cbSnapSize, cbIncognito,
			cbUrl, cbControlBar, cbStatusBar;
	RadioGroup fontSize, historyCount, encodingType, changeUA,
			searchEngine, shareMode, rotateMode;
	CheckBox clrHistory, clrBookmark, clrCookie, clrFormdata, clrPassword,
			clrCache, clrIcon, clrHome;
	LinearLayout advanceSettings, basicSettings;
	Button btnAdvance, btnReset;
	EditText etPort;

	boolean resetDefault = false;

	InputMethodManager imm;

	SharedPreferences perferences;
	SharedPreferences.Editor editor;

	String packageName;
	String marketUrl = "http://bpc.borqs.com/market.html?id=";
	String appUrl;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			LayoutParams lp = (LayoutParams) advanceSettings.getLayoutParams();
			if (lp.height < 0) {// <0 means wrap content or match parent.
				btnAdvance.performClick();// hide the advanced settings if it is
											// shown
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	void share() {
		Intent shareIntent = new Intent(Intent.ACTION_VIEW);
		shareIntent.setClassName(packageName, "easy.lib.SimpleBrowser");
		Uri data = null;
		
		String shareText = getString(R.string.browser_name) + ", " + getString(R.string.sharetext) + "...\n\n";
		boolean chineseLocale = Locale.CHINA.equals(getBaseContext().getResources().getConfiguration().locale);

		switch (shareMode.indexOfChild(findViewById(shareMode.getCheckedRadioButtonId()))) {
		case 2:
			if (chineseLocale) // weibo for chinese locale
				data = Uri.parse("http://v.t.sina.com.cn/share/share.php?url=" + appUrl + "&appkey=3792856654&ralateUid=1877224203&source=bookmark&title=" + shareText);
			else // facebook for none chinese locale
				data = Uri.parse("http://www.facebook.com/sharer.php?t=" + shareText + "&u=" + appUrl);
			break;
		case 3:
			if (chineseLocale) // qzone for chinese locale
				data = Uri.parse("http://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?url=" + appUrl + "&desc=" + shareText + "&title" + shareText);				
			else // twitter
				data = Uri.parse("http://twitter.com/intent/tweet?text=" + shareText + "&url=" + appUrl);
			break;
		case 4:
			if (chineseLocale) // tencent weibo for chinese locale
				data = Uri.parse("http://share.v.t.qq.com/index.php?c=share&a=index&url=" + appUrl + "&title=" + shareText);
			else // google+
			data = Uri.parse("https://plusone.google.com/_/+1/confirm?hl=en&url=https://market.android.com/details?id="+packageName);
			break;
		case 1:
		default:
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
			intent.putExtra(Intent.EXTRA_TEXT, shareText + appUrl);
			util.startActivity(
					Intent.createChooser(intent, getString(R.string.sharemode)),
					true, getBaseContext());
			return;
		}
		
		shareIntent.setData(data);
		util.startActivity(shareIntent, false, getBaseContext());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE); // hide titlebar of
														// application, must be
														// before setting the
														// layout
		setContentView(R.layout.about_browser);

		perferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = perferences.edit();
		packageName = getPackageName();
		appUrl = marketUrl + packageName;
		
		Button btnTitle = (Button) findViewById(R.id.title);
		btnTitle.setText(getString(R.string.browser_name) + " "
				+ util.getVersion(getBaseContext()) + " ("
				+ util.getVersionCode(getBaseContext()) + ")");
		btnTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.fromParts("mailto",
						getString(R.string.browser_author), null));
				util.startActivity(intent, true, getBaseContext());
			}
		});

		Button btnShare = (Button) findViewById(R.id.share);
		btnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				share();
			}
		});

		Button btnVote = (Button) findViewById(R.id.vote);
		btnVote.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id=" + packageName));
				if (!util.startActivity(intent, false, getBaseContext())) {
					finish();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri
							.parse("https://play.google.com/store/apps/details?id=" + packageName));
					intent.setClassName(packageName,
							"easy.lib.SimpleBrowser");
					util.startActivity(intent, true, getBaseContext());
				}
			}
		});

		final Context context = this;
		Button btnClear = (Button) findViewById(R.id.clear);
		btnClear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String message = "";
				if (clrCache.isChecked())
					message += "Cache, ";
				if (clrHistory.isChecked())
					message += getString(R.string.history) + ", ";
				if (clrBookmark.isChecked())
					message += getString(R.string.bookmark) + ", ";
				if (clrCookie.isChecked())
					message += "Cookie, ";
				if (clrPassword.isChecked())
					message += getString(R.string.password) + ", ";
				if (clrFormdata.isChecked())
					message += getString(R.string.formdata) + ", ";
				if (clrIcon.isChecked())
					message += getString(R.string.icon) + ", ";
				if (clrHome.isChecked())
					message += getString(R.string.home) + ", ";
				message = message.trim();
				if ("".equals(message))
					return;// return if no data selected.
				if (message.endsWith(","))
					message = message.substring(0, message.length() - 1);

				new AlertDialog.Builder(context)
						.setTitle(R.string.browser_name)
						.setIcon(R.drawable.stop)
						.setMessage(
								message + " " + getString(R.string.clear_hint))
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {// share
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										editor.putBoolean("clear_data", true);
										// editor.commit();
										finish();
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {// cancel
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}
		});

		rotateMode = (RadioGroup) findViewById(R.id.rotate_mode);
		cbIncognito = (CheckBox) findViewById(R.id.incognito_mode);
		cbZoomControl = (CheckBox) findViewById(R.id.show_zoom);
		cbBlockImg = (CheckBox) findViewById(R.id.block_image);
		cbCachePrefer = (CheckBox) findViewById(R.id.cache_prefer);
		cbSnapSize = (CheckBox) findViewById(R.id.snap_size);
		shareMode = (RadioGroup) findViewById(R.id.share_mode);
		fontSize = (RadioGroup) findViewById(R.id.font_size);
		searchEngine = (RadioGroup) findViewById(R.id.search_engine);
		cbUrl = (CheckBox) findViewById(R.id.show_url);
		cbControlBar = (CheckBox) findViewById(R.id.show_controlBar);
		cbStatusBar = (CheckBox) findViewById(R.id.show_statusBar);

		advanceSettings = (LinearLayout) findViewById(R.id.advance_settings);
		basicSettings = (LinearLayout) findViewById(R.id.basic_settings);
		btnAdvance = (Button) findViewById(R.id.advance_button);
		btnAdvance.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutParams lp = (LayoutParams) advanceSettings
						.getLayoutParams();
				if (lp.height == 0) {
					advanceSettings.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
					basicSettings.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT, 0));
					btnAdvance.setText(getString(R.string.exit) + " "
							+ getString(R.string.advance_button));
				} else {
					lp.height = 0;
					basicSettings.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
					btnAdvance.setText(getString(R.string.advance_button));
				}
				advanceSettings.requestLayout();
				basicSettings.requestLayout();
			}
		});

		cbBlockPopup = (CheckBox) findViewById(R.id.block_popup);
		cbBlockJs = (CheckBox) findViewById(R.id.block_js);
		cbOverview = (CheckBox) findViewById(R.id.overview_page);
		cbCacheToSD = (CheckBox) findViewById(R.id.cache_tosd);
		cbHtml5 = (CheckBox) findViewById(R.id.html5);
		try {
			WebSettings.class.getMethod("setAppCacheEnabled",
					new Class[] { boolean.class });
			cbHtml5.setEnabled(true);
		} catch (Exception e) {
			cbHtml5.setEnabled(false);
		}

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		cbEnableProxy = (CheckBox) findViewById(R.id.enable_proxy);
		etPort = (EditText) findViewById(R.id.local_port);
		cbEnableProxy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean proxyEnabled = cbEnableProxy.isChecked();
				etPort.setEnabled(proxyEnabled);
				etPort.setFocusable(proxyEnabled);
				etPort.setFocusableInTouchMode(proxyEnabled);
				if (!proxyEnabled) // close soft keyboard
					imm.hideSoftInputFromWindow(etPort.getWindowToken(), 0);
				else { // detect GAE and Orbot
					List<ApplicationInfo> li = getPackageManager().getInstalledApplications(0);
					boolean found = false;
					for (int i = 0; i < li.size(); i++) {
						if ("org.gaeproxy".equals(li.get(i).packageName) || "org.torproject.android".equals(li.get(i).packageName)) {
							found = true;
							break;
						}
					}
					if (!found) {
						new AlertDialog.Builder(context)
						.setTitle(getString(R.string.browser_name))
						.setMessage(getString(R.string.proxy_hint))
						.setPositiveButton(getString(R.string.ok),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								})
						.setNeutralButton("GAE",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Intent intent = new Intent(Intent.ACTION_VIEW);
										Uri data = Uri.parse("market://details?id=org.gaeproxy");
										intent.setData(data);
										if (!util.startActivity(intent, false, getBaseContext())) {
											intent.setData(Uri
													.parse("https://play.google.com/store/apps/details?id=org.gaeproxy"));
											intent.setClassName(packageName,
													"easy.lib.SimpleBrowser");
											util.startActivity(intent, true, getBaseContext());
										}
									}
								})
						.setNegativeButton(getString(R.string.cancel),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										cbEnableProxy.setChecked(false);
										etPort.setEnabled(false);
										etPort.setFocusable(false);
										etPort.setFocusableInTouchMode(false);
									}
								}).show();
					}
				}
			}
		});

		encodingType = (RadioGroup) findViewById(R.id.encoding);
		changeUA = (RadioGroup) findViewById(R.id.ua_group);

		clrCache = (CheckBox) findViewById(R.id.clear_cache);
		clrHistory = (CheckBox) findViewById(R.id.clear_history);
		clrBookmark = (CheckBox) findViewById(R.id.clear_bookmark);
		clrCookie = (CheckBox) findViewById(R.id.clear_cookie);
		clrPassword = (CheckBox) findViewById(R.id.clear_password);
		clrFormdata = (CheckBox) findViewById(R.id.clear_formdata);
		clrIcon = (CheckBox) findViewById(R.id.clear_icon);
		clrHome = (CheckBox) findViewById(R.id.clear_home);

		btnReset = (Button) findViewById(R.id.reset);
		btnReset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(context)
						.setTitle(R.string.browser_name)
						.setIcon(R.drawable.stop)
						.setMessage(getString(R.string.reset) + "?")
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {// share
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										resetDefault = true;
										finish();
									}
								})
						.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {// cancel
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								}).show();
			}
		});
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig); // not restart activity each
	}

	@Override
	protected void onResume() {
		boolean showStatusBar = perferences.getBoolean("show_statusBar", true);
		cbStatusBar.setChecked(showStatusBar);
		if (!showStatusBar)
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		cbUrl.setChecked(perferences.getBoolean("show_url", false));
		cbControlBar.setChecked(perferences.getBoolean("show_controlBar", true));

		int tmpMode = perferences.getInt("rotate_mode", 1);
		if (tmpMode < 0) tmpMode = 1;
		((RadioButton) rotateMode.getChildAt(tmpMode)).setChecked(true);
		if (tmpMode == 1) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		else if (tmpMode == 2) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		cbIncognito.setChecked(perferences.getBoolean("incognito", false));
		cbZoomControl.setChecked(perferences.getBoolean("show_zoom", false));
		cbBlockImg.setChecked(perferences.getBoolean("block_image", false));
		cbCachePrefer.setChecked(perferences.getBoolean("cache_prefer", false));

		cbBlockPopup.setChecked(perferences.getBoolean("block_popup", false));
		cbBlockJs.setChecked(perferences.getBoolean("block_js", false));
		cbCacheToSD.setChecked(perferences.getBoolean("cache_tosd", false));
		cbOverview.setChecked(perferences.getBoolean("overview_page", false));
		cbSnapSize.setChecked(perferences.getBoolean("full_web", false));
		cbHtml5.setChecked(perferences.getBoolean("html5", false));
		cbEnableProxy.setChecked(perferences.getBoolean("enable_proxy", false));
		etPort.setText(perferences.getInt("local_port", 1984) + "");
		etPort.setEnabled(cbEnableProxy.isChecked());
		etPort.setFocusable(cbEnableProxy.isChecked());
		etPort.setFocusableInTouchMode(cbEnableProxy.isChecked());

		try {
		((RadioButton) shareMode.getChildAt(perferences.getInt(
				"share_mode", 2))).setChecked(true);
		((RadioButton) fontSize.getChildAt(perferences.getInt("textsize", 2)))
				.setChecked(true);// normal
		((RadioButton) searchEngine.getChildAt(perferences.getInt(
				"search_engine", 3))).setChecked(true);
		((RadioButton) encodingType.getChildAt(perferences
				.getInt("encoding", 0))).setChecked(true);
		((RadioButton) changeUA.getChildAt(perferences.getInt("ua", 0)))
				.setChecked(true);
		} catch(Exception e) {} // some device will return -1, then cause error. catch it.

		clrCache.setChecked(perferences.getBoolean("clear_cache", false));
		clrHistory.setChecked(perferences.getBoolean("clear_history", false));
		clrBookmark.setChecked(perferences.getBoolean("clear_bookmark", false));
		clrCookie.setChecked(perferences.getBoolean("clear_cookie", false));
		clrFormdata.setChecked(perferences.getBoolean("clear_formdata", false));
		clrPassword.setChecked(perferences.getBoolean("clear_password", false));
		clrIcon.setChecked(perferences.getBoolean("clear_icon", false));
		clrHome.setChecked(perferences.getBoolean("clear_home", false));
		super.onResume();
		
		try {
			Class c = Class.forName("com.baidu.mobstat.StatService");
			Method method = c.getMethod(
					"onResume", new Class[] { Context.class });
			method.invoke(this, this);//StatService.onResume(this);//for baidu tongji
		} catch (Exception e) {}
	}

	@Override
	protected void onPause() {
		if (resetDefault) {
			editor.putBoolean("show_url", false);
			editor.putBoolean("show_controlBar", true);
			editor.putBoolean("show_statusBar", true);
			editor.putInt("rotate_mode", 1);
			editor.putBoolean("incognito", false);
			editor.putBoolean("show_zoom", false);
			editor.putBoolean("block_image", false);
			editor.putBoolean("cache_prefer", false);
			editor.putInt("full_screen", 1);
			editor.putInt("share_mode", 1);
			editor.putInt("textsize", 2);
			editor.putInt("search_engine", 3);
			
			editor.putInt("share_mode", 2);
			// the default value of searchEngine relies on locale.
			Locale locale = getBaseContext().getResources().getConfiguration().locale;
			if ("ru_RU".equals(locale.toString())) {
				editor.putInt("search_engine", 4);
			}
			else if (Locale.CHINA.equals(locale)) {
				editor.putInt("search_engine", 2);
			}
			else {
				editor.putInt("search_engine", 3);
			}


			editor.putBoolean("block_popup", false);
			editor.putBoolean("block_js", false);
			editor.putBoolean("full_web", false);
			editor.putBoolean("overview_page", false);
			editor.putBoolean("cache_tosd", false);
			editor.putBoolean("html5", false);
			editor.putBoolean("enable_proxy", false);
			editor.putInt("local_port", 1984);
			editor.putInt("encoding", 0);
			editor.putInt("ua", 0);

			editor.putBoolean("clear_cache", false);
			editor.putBoolean("clear_history", false);
			editor.putBoolean("clear_bookmark", false);
			editor.putBoolean("clear_cookie", false);
			editor.putBoolean("clear_password", false);
			editor.putBoolean("clear_formdata", false);
			editor.putBoolean("clear_icon", false);
			editor.putBoolean("clear_home", false);
		} else {
			editor.putBoolean("show_url", cbUrl.isChecked());
			editor.putBoolean("show_controlBar", cbControlBar.isChecked());
			editor.putBoolean("show_statusBar", cbStatusBar.isChecked());
			editor.putInt("rotate_mode", rotateMode.indexOfChild(findViewById(rotateMode.getCheckedRadioButtonId())));
			editor.putBoolean("incognito", cbIncognito.isChecked());
			editor.putBoolean("show_zoom", cbZoomControl.isChecked());
			editor.putBoolean("block_image", cbBlockImg.isChecked());
			editor.putBoolean("cache_prefer", cbCachePrefer.isChecked());
			editor.putBoolean("full_web", cbSnapSize.isChecked());
			editor.putInt("share_mode", shareMode
					.indexOfChild(findViewById(shareMode
							.getCheckedRadioButtonId())));
			editor.putInt("textsize", fontSize
					.indexOfChild(findViewById(fontSize
							.getCheckedRadioButtonId())));
			editor.putInt("search_engine", searchEngine
					.indexOfChild(findViewById(searchEngine
							.getCheckedRadioButtonId())));

			editor.putBoolean("block_popup", cbBlockPopup.isChecked());
			editor.putBoolean("block_js", cbBlockJs.isChecked());
			editor.putBoolean("overview_page", cbOverview.isChecked());
			editor.putBoolean("cache_tosd", cbCacheToSD.isChecked());
			editor.putBoolean("html5", cbHtml5.isChecked());
			editor.putBoolean("enable_proxy", cbEnableProxy.isChecked());
			try {
				editor.putInt("local_port",
						Integer.parseInt(etPort.getText().toString()));
			} catch (Exception e) {
			}// incase error in parse int
			editor.putInt("encoding", encodingType
					.indexOfChild(findViewById(encodingType
							.getCheckedRadioButtonId())));
			editor.putInt("ua", changeUA.indexOfChild(findViewById(changeUA
					.getCheckedRadioButtonId())));

			editor.putBoolean("clear_cache", clrCache.isChecked());
			editor.putBoolean("clear_history", clrHistory.isChecked());
			editor.putBoolean("clear_bookmark", clrBookmark.isChecked());
			editor.putBoolean("clear_cookie", clrCookie.isChecked());
			editor.putBoolean("clear_formdata", clrFormdata.isChecked());
			editor.putBoolean("clear_password", clrPassword.isChecked());
			editor.putBoolean("clear_icon", clrIcon.isChecked());
			editor.putBoolean("clear_home", clrHome.isChecked());
		}
		editor.commit();

		super.onPause();
		
		try {
			Class c = Class.forName("com.baidu.mobstat.StatService");
			Method method = c.getMethod(
					"onPause", new Class[] { Context.class });
			method.invoke(this, this);//StatService.onPause(this);//for baidu tongji
		} catch (Exception e) {}
	}
}
