package hello.world;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class HelloActivity extends Activity
{
	WebView wv;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    	
    	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide titlebar of application, must be before setting the layout
        setContentView(R.layout.main);
        wv = (WebView) findViewById(R.id.webview);
        wv.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);//no white blank on the right of webview
    	WebSettings localSettings = wv.getSettings();
    	localSettings.setJavaScriptEnabled(true);

        wv.loadUrl("file:///android_asset/qiupu/index.html");
    }
}
