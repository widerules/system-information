package simple.home.jtbuaa;

import simple.home.jtbuaa.simpleHome.DownloadTask;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class downloadControl extends Activity{
	Button btnPause, btnStop, btnReturn;
	boolean pause = false, stop = false;
	Intent intent;
	MyApp appstate;
	int id;
	DownloadTask dlt;
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		init(intent);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.pause);
        
        init(getIntent());
	}
	
	private void init(Intent intent) {
        id = intent.getIntExtra("id", 0);
        //Log.d("==============", "id: " + id);
        
        appstate = (MyApp) getApplicationContext();
        dlt = appstate.downloadState.get(id);
        
        if (dlt != null) {
        	pause = dlt.pauseDownload;
        	stop = dlt.stopDownload;
        }
        
        btnPause = (Button) findViewById(R.id.pause);
		if (pause) {
			btnPause.setText(getString(R.string.resume));
		}
		else {
			btnPause.setText(getString(R.string.pause));
		}
		
        btnPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (dlt != null) dlt.pauseDownload = !pause;
				finish();
			}
        });

        btnStop = (Button) findViewById(R.id.stop);
        btnStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (dlt != null) dlt.stopDownload = !stop;
				finish();
			}
        });

        btnReturn = (Button) findViewById(R.id.just_return);
        btnReturn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
        });
	}
	
}
