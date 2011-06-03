package set.operator;

import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

public class setOperator extends Activity {
    /** Called when the activity is first created. */	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        setContentView(tv);
        
		try {
			List<String> command = new java.util.ArrayList<String>();
			command.add("setprop");
			command.add("gsm.operator.numeric");
			command.add("31038");
			  
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(command);
			builder.start();
			
			tv.setText("I have changed gsm.operator.numeric to 31038 so that you can find more applications on Android Market.\n\nIt will restore to 46000 after reboot the phone.");
		} catch (Exception e) {
			tv.setText(e.toString());
		}
    }
}
