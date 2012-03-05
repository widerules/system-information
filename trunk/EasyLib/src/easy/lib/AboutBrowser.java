package easy.lib;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import easy.lib.util;

public class AboutBrowser extends Activity{
	
	CheckBox cbZoomControl;
	SharedPreferences perferences;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.about_browser);

        Button btnShare = (Button) findViewById(R.id.title);
		String title = getString(R.string.embed_browser_name);
		if (getPackageName().equals("easy.browser")) 
			title = getString(R.string.browser_name) + " " + util.getVersion(getBaseContext());
		btnShare.setText(title);
        btnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
    	        String text = getString(R.string.sharetext) + getString(R.string.share_text1) 
       	        		+ "http://opda.co/?s=D/easy.browser"//opda will do the webpage reload for us.
       	        		+ getString(R.string.share_text2)
       	        		+ "https://market.android.com/details?id=easy.browser"
       	        		+ getString(R.string.share_text3);
        	        
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
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("https://market.android.com/details?id=easy.browser"));
					intent.setComponent(getComponentName());
					util.startActivity(intent, true, getBaseContext());
				}
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

    	CheckBox showZoomControl = (CheckBox) findViewById(R.id.show_zoom);
    	showZoomControl.setOnClickListener(new OnClickListener() {
    		@Override
    		public void onClick(View arg0) {
    			//serverWebs.get(webIndex).getSettings().setBuiltInZoomControls(showZoomControl.isChecked());
    		}
    	});

    	RadioButton btnFullScreen = (RadioButton) findViewById(R.id.radio_fullscreen);
		//showZoomControl.setChecked(serverWebs.get(webIndex).getSettings().getBuiltInZoomControls());

	}
	
	@Override
	protected void onResume() {
        //cbShake.setEnabled(perferences.getBoolean("shake_enabled", false));
        //cbShake.setChecked(perferences.getBoolean("shake", false));
        
		super.onResume();
	}
}
