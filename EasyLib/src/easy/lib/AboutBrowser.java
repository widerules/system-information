package easy.lib;

import java.lang.reflect.Method;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
			cbFullscreen, cbOverview, cbSnapSize;
	RadioGroup fontSize, historyCount, encodingType, changeUA,
			searchEngine, shareMode;
	CheckBox clrHistory, clrBookmark, clrCookie, clrFormdata, clrPassword,
			clrCache;
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

		switch (shareMode.indexOfChild(findViewById(shareMode.getCheckedRadioButtonId()))) {
		case 2:// facebook
			data = Uri.parse("http://www.facebook.com/sharer.php?t=" + shareText + "&u=" + appUrl);
			break;
		case 3:// twitter
			data = Uri.parse("http://twitter.com/intent/tweet?text=" + shareText + "&url=" + appUrl);
			break;
		case 4:// google+
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
				share();
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
				if (clrFormdata.isChecked())
					message += getString(R.string.formdata) + ", ";
				if (clrPassword.isChecked())
					message += getString(R.string.password) + ", ";
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

		TextView tvMailTo = (TextView) findViewById(R.id.mailto);
		tvMailTo.setText(Html.fromHtml("<u>"
				+ getString(R.string.browser_author) + "</u>"));
		tvMailTo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setData(Uri.fromParts("mailto",
						getString(R.string.browser_author), null));
				util.startActivity(intent, true, getBaseContext());
			}
		});

		cbFullscreen = (CheckBox) findViewById(R.id.full_screen);
		cbZoomControl = (CheckBox) findViewById(R.id.show_zoom);
		cbBlockImg = (CheckBox) findViewById(R.id.block_image);
		cbCachePrefer = (CheckBox) findViewById(R.id.cache_prefer);
		cbSnapSize = (CheckBox) findViewById(R.id.snap_size);
		shareMode = (RadioGroup) findViewById(R.id.share_mode);
		fontSize = (RadioGroup) findViewById(R.id.font_size);
		searchEngine = (RadioGroup) findViewById(R.id.search_engine);

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
		//cbHideExit = (CheckBox) findViewById(R.id.hide_exit);
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
				if (!proxyEnabled)
					imm.hideSoftInputFromWindow(etPort.getWindowToken(), 0);// close
																			// soft
																			// keyboard
			}
		});

		encodingType = (RadioGroup) findViewById(R.id.encoding);
		changeUA = (RadioGroup) findViewById(R.id.ua_group);

		clrHistory = (CheckBox) findViewById(R.id.clear_history);
		clrBookmark = (CheckBox) findViewById(R.id.clear_bookmark);
		clrCookie = (CheckBox) findViewById(R.id.clear_cookie);
		clrFormdata = (CheckBox) findViewById(R.id.clear_formdata);
		clrPassword = (CheckBox) findViewById(R.id.clear_password);
		clrCache = (CheckBox) findViewById(R.id.clear_cache);

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
	protected void onResume() {
		cbFullscreen.setChecked(perferences.getBoolean("full_screen_display",
				false));
		boolean tmpFullScreen = perferences.getBoolean("full_screen_display", false);
		if (tmpFullScreen)
			getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		else
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		cbFullscreen.setChecked(tmpFullScreen);

		cbZoomControl.setChecked(perferences.getBoolean("show_zoom", false));
		cbBlockImg.setChecked(perferences.getBoolean("block_image", false));
		cbCachePrefer.setChecked(perferences.getBoolean("cache_prefer", false));

		cbBlockPopup.setChecked(perferences.getBoolean("block_popup", false));
		cbBlockJs.setChecked(perferences.getBoolean("block_js", false));
		cbCacheToSD.setChecked(perferences.getBoolean("cache_tosd", false));
		//cbHideExit.setChecked(perferences.getBoolean("hide_exit", true));
		cbOverview.setChecked(perferences.getBoolean("overview_page", false));
		cbSnapSize.setChecked(perferences.getBoolean("full_web", false));
		cbHtml5.setChecked(perferences.getBoolean("html5", false));
		cbEnableProxy.setChecked(perferences.getBoolean("enable_proxy", false));
		etPort.setText(perferences.getInt("local_port", 1984) + "");
		etPort.setEnabled(cbEnableProxy.isChecked());
		etPort.setFocusable(cbEnableProxy.isChecked());
		etPort.setFocusableInTouchMode(cbEnableProxy.isChecked());

		((RadioButton) shareMode.getChildAt(perferences.getInt(
				"share_mode", 1))).setChecked(true);
		((RadioButton) fontSize.getChildAt(perferences.getInt("textsize", 2)))
				.setChecked(true);// normal
		((RadioButton) searchEngine.getChildAt(perferences.getInt(
				"search_engine", 3))).setChecked(true);
		((RadioButton) encodingType.getChildAt(perferences
				.getInt("encoding", 0))).setChecked(true);
		((RadioButton) changeUA.getChildAt(perferences.getInt("ua", 0)))
				.setChecked(true);

		clrHistory.setChecked(perferences.getBoolean("clear_history", false));
		clrBookmark.setChecked(perferences.getBoolean("clear_bookmark", false));
		clrCookie.setChecked(perferences.getBoolean("clear_cookie", false));
		clrFormdata.setChecked(perferences.getBoolean("clear_formdata", false));
		clrPassword.setChecked(perferences.getBoolean("clear_password", false));
		clrCache.setChecked(perferences.getBoolean("clear_cache", false));
		super.onResume();
		
		try {
			Class c = Class.forName("com.baidu.mobstat.StatService");
			Method method = c.getMethod(
					"onResume", new Class[] { Context.class });
			method.invoke(this, this);//StatService.onResume(this);//for baidu tongji
		} catch (Exception e) {e.printStackTrace();}
	}

	@Override
	protected void onPause() {
		if (resetDefault) {
			editor.putBoolean("full_screen_display", false);
			// editor.putBoolean("page_source", false);
			editor.putBoolean("show_zoom", false);
			editor.putBoolean("block_image", false);
			editor.putBoolean("cache_prefer", false);
			editor.putInt("full_screen", 1);
			editor.putInt("share_mode", 1);
			editor.putInt("textsize", 2);
			editor.putInt("search_engine", 3);
			
			// the default value of shareMode and searchEngine relies on locale.
			Locale locale = getBaseContext().getResources().getConfiguration().locale;
			if ("ru_RU".equals(locale.toString())) {
				editor.putInt("share_mode", 2);
				editor.putInt("search_engine", 4);
			}
			else if (Locale.CHINA.equals(locale)) {
				editor.putInt("share_mode", 1);
				editor.putInt("search_engine", 2);
			}
			else {
				editor.putInt("share_mode", 2);
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

			editor.putBoolean("clear_history", false);
			editor.putBoolean("clear_bookmark", false);
			editor.putBoolean("clear_cookie", false);
			editor.putBoolean("clear_formdata", false);
			editor.putBoolean("clear_password", false);
			editor.putBoolean("clear_cache", false);
		} else {
			editor.putBoolean("full_screen_display", cbFullscreen.isChecked());
			// editor.putBoolean("page_source", cbPageSource.isChecked());
			editor.putBoolean("show_zoom", cbZoomControl.isChecked());
			editor.putBoolean("block_image", cbBlockImg.isChecked());
			editor.putBoolean("cache_prefer", cbCachePrefer.isChecked());
			editor.putBoolean("full_web", cbSnapSize.isChecked());
			// editor.putInt("history_count",
			// historyCount.indexOfChild(findViewById(historyCount.getCheckedRadioButtonId())));
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
			//editor.putBoolean("hide_exit", cbHideExit.isChecked());
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

			editor.putBoolean("clear_history", clrHistory.isChecked());
			editor.putBoolean("clear_bookmark", clrBookmark.isChecked());
			editor.putBoolean("clear_cookie", clrCookie.isChecked());
			editor.putBoolean("clear_formdata", clrFormdata.isChecked());
			editor.putBoolean("clear_password", clrPassword.isChecked());
			editor.putBoolean("clear_cache", clrCache.isChecked());
		}
		editor.commit();

		super.onPause();
		
		try {
			Class c = Class.forName("com.baidu.mobstat.StatService");
			Method method = c.getMethod(
					"onPause", new Class[] { Context.class });
			method.invoke(this, this);//StatService.onPause(this);//for baidu tongji
		} catch (Exception e) {e.printStackTrace();}
	}
}
