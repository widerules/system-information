package simple.home.jtbuaa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class SelectHome extends Activity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent chooseHome = new Intent(Intent.ACTION_MAIN);
        chooseHome.addCategory(Intent.CATEGORY_HOME);
        startActivity(chooseHome);
	}
}
