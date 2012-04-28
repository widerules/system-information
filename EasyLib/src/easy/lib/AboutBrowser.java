package easy.lib;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import easy.lib.util;

public class AboutBrowser extends Activity{
	
	CheckBox cbCacheToSD, cbZoomControl, cbCss, cbHtml5, cbBlockImg;
	RadioGroup fontSize, historyCount, encodingType, snapSize, changeUA, searchEngine;
	CheckBox clrHistory, clrBookmark, clrCookie, clrFormdata, clrPassword, clrCache;
	LinearLayout advanceSettings, basicSettings;
	Button btnAdvance;
	
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
		String title = getString(R.string.embed_browser_name);
		if (getPackageName().equals("easy.browser")) 
			title = getString(R.string.browser_name) + " " + util.getVersion(getBaseContext());
		btnShare.setText(title);
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
		        		editor.commit();
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

    	cbCacheToSD = (CheckBox) findViewById(R.id.cache_tosd);
    	cbCacheToSD.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("cache_tosd", cbCacheToSD.isChecked());
        		editor.commit();
    		}
    	});

    	cbZoomControl = (CheckBox) findViewById(R.id.show_zoom);
    	cbZoomControl.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("show_zoom", cbZoomControl.isChecked());
        		editor.commit();
    		}
    	});

    	cbBlockImg = (CheckBox) findViewById(R.id.block_image);
    	cbBlockImg.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("block_image", cbBlockImg.isChecked());
        		editor.commit();
    		}
    	});
    	
    	/*cbCss = (CheckBox) findViewById(R.id.homepage_css);
    	cbCss.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("css", cbCss.isChecked());
        		editor.commit();
    		}
    	});*/
    	
    	
    	cbHtml5 = (CheckBox) findViewById(R.id.html5);
    	try {
    		WebSettings.class.getMethod("setAppCacheEnabled", new Class[] {boolean.class});
        	cbHtml5.setEnabled(true);
    	}
    	catch(Exception e) {
        	cbHtml5.setEnabled(false);
    	}

    	cbHtml5.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("html5", cbHtml5.isChecked());
        		editor.commit();
    		}
    	});
    	
    	
    	fontSize = (RadioGroup) findViewById(R.id.font_size);
    	fontSize.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
        		editor.putInt("textsize", fontSize.indexOfChild(findViewById(fontSize.getCheckedRadioButtonId())));
        		editor.commit();
			}
    	});
    	
    	/*historyCount = (RadioGroup) findViewById(R.id.max_history);
    	historyCount.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
        		editor.putInt("history_count", historyCount.indexOfChild(findViewById(historyCount.getCheckedRadioButtonId())));
        		editor.commit();
			}
    	});*/
    	
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
					btnAdvance.setText(getString(R.string.basic_button));
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

    	encodingType = (RadioGroup) findViewById(R.id.encoding);
    	encodingType.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
        		editor.putInt("encoding", encodingType.indexOfChild(findViewById(encodingType.getCheckedRadioButtonId())));
        		editor.commit();
			}
    	});
    	
    	snapSize = (RadioGroup) findViewById(R.id.snap_size);
    	snapSize.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
        		editor.putInt("full_screen", snapSize.indexOfChild(findViewById(snapSize.getCheckedRadioButtonId())));
        		editor.commit();
			}
    	});

    	changeUA = (RadioGroup) findViewById(R.id.ua_group);
    	changeUA.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
        		editor.putInt("ua", changeUA.indexOfChild(findViewById(changeUA.getCheckedRadioButtonId())));
        		editor.commit();
			}
    	});
    	
    	searchEngine = (RadioGroup) findViewById(R.id.search_engine);
    	searchEngine.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
        		editor.putInt("search_engine", searchEngine.indexOfChild(findViewById(searchEngine.getCheckedRadioButtonId())));
        		editor.commit();
			}
    	});
    	
    	clrHistory = (CheckBox) findViewById(R.id.clear_history);
    	clrHistory.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("clear_history", clrHistory.isChecked());
        		editor.commit();
    		}
    	});
    	clrBookmark = (CheckBox) findViewById(R.id.clear_bookmark);
    	clrBookmark.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("clear_bookmark", clrBookmark.isChecked());
        		editor.commit();
    		}
    	});
    	clrCookie = (CheckBox) findViewById(R.id.clear_cookie);
    	clrCookie.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("clear_cookie", clrCookie.isChecked());
        		editor.commit();
    		}
    	});
    	clrFormdata = (CheckBox) findViewById(R.id.clear_formdata);
    	clrFormdata.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("clear_formdata", clrFormdata.isChecked());
        		editor.commit();
    		}
    	});
    	clrPassword = (CheckBox) findViewById(R.id.clear_password);
    	clrPassword.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("clear_password", clrPassword.isChecked());
        		editor.commit();
    		}
    	});
    	clrCache = (CheckBox) findViewById(R.id.clear_cache);
    	clrCache.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("clear_cache", clrCache.isChecked());
        		editor.commit();
    		}
    	});
	}
	
	@Override
	protected void onResume() {
		cbCacheToSD.setChecked(perferences.getBoolean("cache_tosd", false));
		cbZoomControl.setChecked(perferences.getBoolean("show_zoom", false));
		//cbCss.setChecked(perferences.getBoolean("css", false));
		cbHtml5.setChecked(perferences.getBoolean("html5", false));
		cbBlockImg.setChecked(perferences.getBoolean("block_image", false));
		
   		((RadioButton) fontSize.getChildAt(perferences.getInt("textsize", 2))).setChecked(true);//normal
		//((RadioButton) historyCount.getChildAt(perferences.getInt("history_count", 1))).setChecked(true);
		((RadioButton) encodingType.getChildAt(perferences.getInt("encoding", 0))).setChecked(true);
		((RadioButton) snapSize.getChildAt(perferences.getInt("full_screen", 1))).setChecked(true);
		((RadioButton) changeUA.getChildAt(perferences.getInt("ua", 0))).setChecked(true);
		((RadioButton) searchEngine.getChildAt(perferences.getInt("search_engine", 2))).setChecked(true);
        
		clrHistory.setChecked(perferences.getBoolean("clear_history", false));
		clrBookmark.setChecked(perferences.getBoolean("clear_bookmark", false));
		clrCookie.setChecked(perferences.getBoolean("clear_cookie", false));
		clrFormdata.setChecked(perferences.getBoolean("clear_formdata", false));
		clrPassword.setChecked(perferences.getBoolean("clear_password", false));
		clrCache.setChecked(perferences.getBoolean("clear_cache", false));
		super.onResume();
	}
	
}
