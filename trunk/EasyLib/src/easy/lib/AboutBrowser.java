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
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import easy.lib.util;

public class AboutBrowser extends Activity{
	
	CheckBox cbZoomControl, cbCss, cbHtml5, cbBlockImg;
	RadioGroup fontSize, historyCount, encodingType, snapSize, changeUA;
	
	SharedPreferences perferences;
	SharedPreferences.Editor editor;
	
	DisplayMetrics dm;
	
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

        Button btnClearCache = (Button) findViewById(R.id.clear_cache);
        btnClearCache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		editor.putBoolean("clear_cache", true);
        		editor.commit();
        		finish();
			}
        });

        final Context context = this;
        Button btnClearAll = (Button) findViewById(R.id.clear_all);
        btnClearAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(context).
	    		setTitle(R.string.browser_name).
	    		setIcon(R.drawable.stop).
	    		setMessage(R.string.clear_hint).
	    		setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {//share
					@Override
					public void onClick(DialogInterface dialog, int which) {
		        		editor.putBoolean("clear_all", true);
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
    	
    	cbCss = (CheckBox) findViewById(R.id.homepage_css);
    	cbCss.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
        		editor.putBoolean("css", cbCss.isChecked());
        		editor.commit();
    		}
    	});
    	
    	
    	cbHtml5 = (CheckBox) findViewById(R.id.html5);
    	try {
    		WebSettings.class.getMethod("setAppCacheEnabled", new Class[] {boolean.class});
        	cbHtml5.setEnabled(true);
    	}
    	catch(Exception e) {
        	cbHtml5.setEnabled(false);
    		e.printStackTrace();
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
    	
    	historyCount = (RadioGroup) findViewById(R.id.max_history);
    	historyCount.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
        		editor.putInt("history_count", historyCount.indexOfChild(findViewById(historyCount.getCheckedRadioButtonId())));
        		editor.commit();
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
    	
    	dm = new DisplayMetrics();
    	getWindowManager().getDefaultDisplay().getMetrics(dm);
	}
	
	@Override
	protected void onResume() {
		cbZoomControl.setChecked(perferences.getBoolean("show_zoom", false));
		cbCss.setChecked(perferences.getBoolean("css", false));
		cbHtml5.setChecked(perferences.getBoolean("html5", false));
		cbBlockImg.setChecked(perferences.getBoolean("block_image", false));
		
    	if (dm.density < 1) 
    		((RadioButton) fontSize.getChildAt(perferences.getInt("textsize", 3))).setChecked(true);//smaller
    	else  
    		((RadioButton) fontSize.getChildAt(perferences.getInt("textsize", 2))).setChecked(true);//normal

		
		((RadioButton) historyCount.getChildAt(perferences.getInt("history_count", 1))).setChecked(true);
		((RadioButton) encodingType.getChildAt(perferences.getInt("encoding", 1))).setChecked(true);
		((RadioButton) snapSize.getChildAt(perferences.getInt("full_screen", 1))).setChecked(true);
		((RadioButton) changeUA.getChildAt(perferences.getInt("ua", 0))).setChecked(true);
        
		super.onResume();
	}
	
}
