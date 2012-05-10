package easy.lib;

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

public class AboutBrowser extends Activity{
	
	CheckBox cbEnableProxy, cbBlockPopup, cbBlockJs, cbCacheToSD, cbZoomControl, cbCss, cbHtml5, cbBlockImg, cbHideExit;
	RadioGroup fontSize, historyCount, encodingType, snapSize, changeUA, searchEngine;
	CheckBox clrHistory, clrBookmark, clrCookie, clrFormdata, clrPassword, clrCache;
	LinearLayout advanceSettings, basicSettings;
	Button btnAdvance, btnReset;
	EditText etPort;
	
	boolean resetDefault = false;
	
	InputMethodManager imm;
	
	SharedPreferences perferences;
	SharedPreferences.Editor editor;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			LayoutParams lp = (LayoutParams) advanceSettings.getLayoutParams();
			if (lp.height < 0) {//<0 means wrap content or match parent.
				btnAdvance.performClick();//hide the advanced settings if it is shown
				return true;
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
        setContentView(R.layout.about_browser);
        
		perferences = PreferenceManager.getDefaultSharedPreferences(this);
		editor = perferences.edit();

        Button btnShare = (Button) findViewById(R.id.title);
		btnShare.setText(getString(R.string.browser_name) + " " + util.getVersion(getBaseContext()));
        btnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
    	        String text = getString(R.string.browser_name) + ", "
    	        		+ getString(R.string.sharetext)
       	        		+ " https://play.google.com/store/apps/details?id=easy.browser";
        	        
    	        Intent intent = new Intent(Intent.ACTION_SEND);
    	        intent.setType("text/plain");  
    	        intent.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
        		intent.putExtra(Intent.EXTRA_TEXT, text);
       			util.startActivity(Intent.createChooser(intent, getString(R.string.sharemode)), true, getBaseContext());
			}
        });

        TextView tvHelp = (TextView) findViewById(R.id.help);
        tvHelp.setText(getString(R.string.browser_name) + getString(R.string.about_message) + "\n\n" + getString(R.string.about_dialog_notes));
        
        Button btnVote = (Button) findViewById(R.id.vote);
        btnVote.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=easy.browser"));
				if (!util.startActivity(intent, false, getBaseContext())) {
					finish();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=easy.browser"));
	    			intent.setClassName(getPackageName(), "easy.lib.SimpleBrowser");
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
				if (clrCache.isChecked()) message += "Cache, ";
				if (clrHistory.isChecked()) message += getString(R.string.history) + ", ";
				if (clrBookmark.isChecked()) message += getString(R.string.bookmark) + ", ";
				if (clrCookie.isChecked()) message += "Cookie, ";
				if (clrFormdata.isChecked()) message += getString(R.string.formdata) + ", ";
				if (clrPassword.isChecked()) message += getString(R.string.password) + ", ";
				message = message.trim();
				if ("".equals(message)) return;//return if no data selected.
				if (message.endsWith(",")) message = message.substring(0, message.length()-1);
				
				new AlertDialog.Builder(context).
	    		setTitle(R.string.browser_name).
	    		setIcon(R.drawable.stop).
	    		setMessage(message + " " + getString(R.string.clear_hint)).
	    		setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {//share
					@Override
					public void onClick(DialogInterface dialog, int which) {
		        		editor.putBoolean("clear_data", true);
		        		//editor.commit();
		        		finish();
					}
				}).
	    		setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {//cancel
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
			}
        });

        TextView tvMailTo = (TextView) findViewById(R.id.mailto);
    	if (getPackageName().equals("easy.browser")) 
    		tvMailTo.setText(Html.fromHtml("<u>"+ getString(R.string.browser_author) +"</u>"));
    	else
    		tvMailTo.setText(Html.fromHtml("<u>"+ getString(R.string.author) +"</u>"));
        tvMailTo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				if (getPackageName().equals("easy.browser")) 
					intent.setData(Uri.fromParts("mailto", getString(R.string.browser_author), null));
				else
					intent.setData(Uri.fromParts("mailto", getString(R.string.author), null));
				util.startActivity(intent, true, getBaseContext());
			}
		});


    	cbZoomControl = (CheckBox) findViewById(R.id.show_zoom);
    	cbBlockImg = (CheckBox) findViewById(R.id.block_image);
    	//cbCss = (CheckBox) findViewById(R.id.homepage_css);
    	//historyCount = (RadioGroup) findViewById(R.id.max_history);
    	snapSize = (RadioGroup) findViewById(R.id.snap_size);
    	fontSize = (RadioGroup) findViewById(R.id.font_size);
    	searchEngine = (RadioGroup) findViewById(R.id.search_engine);
    	
    	advanceSettings = (LinearLayout) findViewById(R.id.advance_settings);
    	basicSettings = (LinearLayout) findViewById(R.id.basic_settings);
        btnAdvance = (Button) findViewById(R.id.advance_button);
        btnAdvance.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LayoutParams lp = (LayoutParams) advanceSettings.getLayoutParams();
				if (lp.height == 0) {
					advanceSettings.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT,
			                LayoutParams.WRAP_CONTENT));
					basicSettings.setLayoutParams(new LayoutParams(
							LayoutParams.MATCH_PARENT, 0));
					btnAdvance.setText(getString(R.string.exit) + " " + getString(R.string.advance_button));
				}
				else {
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
    	cbHideExit = (CheckBox) findViewById(R.id.hide_exit);
    	cbCacheToSD = (CheckBox) findViewById(R.id.cache_tosd);
    	cbHtml5 = (CheckBox) findViewById(R.id.html5);
    	try {
    		WebSettings.class.getMethod("setAppCacheEnabled", new Class[] {boolean.class});
        	cbHtml5.setEnabled(true);
    	}
    	catch(Exception e) {
        	cbHtml5.setEnabled(false);
    	}
    	
		imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    	cbEnableProxy = (CheckBox) findViewById(R.id.enable_proxy);
    	etPort = (EditText) findViewById(R.id.local_port);
    	cbEnableProxy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean proxyEnabled = cbEnableProxy.isChecked();
				etPort.setEnabled(proxyEnabled);
				etPort.setFocusable(proxyEnabled);
				etPort.setFocusableInTouchMode(proxyEnabled);
				if (!proxyEnabled) imm.hideSoftInputFromWindow(etPort.getWindowToken(), 0);//close soft keyboard
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
				new AlertDialog.Builder(context).
	    		setTitle(R.string.browser_name).
	    		setIcon(R.drawable.stop).
	    		setMessage(getString(R.string.reset) + "?").
	    		setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {//share
					@Override
					public void onClick(DialogInterface dialog, int which) {
						resetDefault = true;
		        		finish();
					}
				}).
	    		setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {//cancel
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
			}
        });
	}
	
	@Override
	protected void onResume() {
		cbBlockPopup.setChecked(perferences.getBoolean("block_popup", false));
		cbBlockJs.setChecked(perferences.getBoolean("block_js", false));
		cbHideExit.setChecked(perferences.getBoolean("hide_exit", true));
		cbCacheToSD.setChecked(perferences.getBoolean("cache_tosd", false));
		cbZoomControl.setChecked(perferences.getBoolean("show_zoom", false));
		//cbCss.setChecked(perferences.getBoolean("css", false));
		cbHtml5.setChecked(perferences.getBoolean("html5", false));
		cbBlockImg.setChecked(perferences.getBoolean("block_image", false));
		cbEnableProxy.setChecked(perferences.getBoolean("enable_proxy", false));
		etPort.setText(perferences.getInt("local_port", 1984)+"");
		etPort.setEnabled(cbEnableProxy.isChecked());
		etPort.setFocusable(cbEnableProxy.isChecked());
		etPort.setFocusableInTouchMode(cbEnableProxy.isChecked());
		
   		((RadioButton) fontSize.getChildAt(perferences.getInt("textsize", 2))).setChecked(true);//normal
		//((RadioButton) historyCount.getChildAt(perferences.getInt("history_count", 1))).setChecked(true);
		((RadioButton) encodingType.getChildAt(perferences.getInt("encoding", 0))).setChecked(true);
		((RadioButton) snapSize.getChildAt(perferences.getInt("full_screen", 1))).setChecked(true);
		((RadioButton) changeUA.getChildAt(perferences.getInt("ua", 0))).setChecked(true);
		((RadioButton) searchEngine.getChildAt(perferences.getInt("search_engine", 3))).setChecked(true);
        
		clrHistory.setChecked(perferences.getBoolean("clear_history", false));
		clrBookmark.setChecked(perferences.getBoolean("clear_bookmark", false));
		clrCookie.setChecked(perferences.getBoolean("clear_cookie", false));
		clrFormdata.setChecked(perferences.getBoolean("clear_formdata", false));
		clrPassword.setChecked(perferences.getBoolean("clear_password", false));
		clrCache.setChecked(perferences.getBoolean("clear_cache", false));
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		if (resetDefault) {
	    	editor.putBoolean("show_zoom", false);
	    	editor.putBoolean("block_image", false);
	    	editor.putInt("full_screen", 1);
	    	editor.putInt("textsize", 2);
	    	editor.putInt("search_engine", 3);
	    	
	    	editor.putBoolean("block_popup", false);
	    	editor.putBoolean("block_js", false);
	    	editor.putBoolean("hide_exit", true);
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
		}
		else {
			editor.putBoolean("show_zoom", cbZoomControl.isChecked());
			editor.putBoolean("block_image", cbBlockImg.isChecked());
			//editor.putBoolean("css", cbCss.isChecked());
			//editor.putInt("history_count", historyCount.indexOfChild(findViewById(historyCount.getCheckedRadioButtonId())));
			editor.putInt("full_screen", snapSize.indexOfChild(findViewById(snapSize.getCheckedRadioButtonId())));
			editor.putInt("textsize", fontSize.indexOfChild(findViewById(fontSize.getCheckedRadioButtonId())));
			editor.putInt("search_engine", searchEngine.indexOfChild(findViewById(searchEngine.getCheckedRadioButtonId())));
			
			editor.putBoolean("block_popup", cbBlockPopup.isChecked());
			editor.putBoolean("block_js", cbBlockJs.isChecked());
			editor.putBoolean("hide_exit", cbHideExit.isChecked());
			editor.putBoolean("cache_tosd", cbCacheToSD.isChecked());
			editor.putBoolean("html5", cbHtml5.isChecked());
			editor.putBoolean("enable_proxy", cbEnableProxy.isChecked());
			try { editor.putInt("local_port", Integer.parseInt(etPort.getText().toString()));
			} catch(Exception e) {}//incase error in parse int
			editor.putInt("encoding", encodingType.indexOfChild(findViewById(encodingType.getCheckedRadioButtonId())));
			editor.putInt("ua", changeUA.indexOfChild(findViewById(changeUA.getCheckedRadioButtonId())));
			
			editor.putBoolean("clear_history", clrHistory.isChecked());
			editor.putBoolean("clear_bookmark", clrBookmark.isChecked());
			editor.putBoolean("clear_cookie", clrCookie.isChecked());
			editor.putBoolean("clear_formdata", clrFormdata.isChecked());
			editor.putBoolean("clear_password", clrPassword.isChecked());
			editor.putBoolean("clear_cache", clrCache.isChecked());
		}
	    editor.commit();
	    
		super.onPause();
	}
}
