package simple.home.jtbuaa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class downloadControl extends Activity{
	Button btnPause, btnStop;
	boolean pause, stop;
	Intent intent;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        this.setContentView(R.layout.pause);
        
        intent = getIntent();
        final int id = intent.getIntExtra("id", 0);
        pause = intent.getBooleanExtra("pause", false);
        stop = intent.getBooleanExtra("stop", false);
        
        btnPause = (Button) findViewById(R.id.pause);
        btnPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (pause) {
					btnPause.setText(getString(R.string.resume));
				}
				else {
					btnPause.setText(getString(R.string.pause));
				}
				intent.putExtra("pause", !pause);
				intent.setAction("simple.home.jtbuaa.downloadBack");
				startActivity(intent);
				//sendBroadcast(intent);
				finish();
			}
        });

        btnStop = (Button) findViewById(R.id.stop);
        btnPause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				intent.putExtra("stop", !stop);
				intent.setAction("simple.home.jtbuaa.downloadBack");
				startActivity(intent);
				//sendBroadcast(intent);
				finish();
			}
        });

	}
}
