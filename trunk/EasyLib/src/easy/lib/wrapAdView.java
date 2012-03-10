package easy.lib;

import android.app.Activity;
import android.view.View;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class wrapAdView {
	AdView mInstance;
	AdRequest adRequest;
	
	public wrapAdView(Activity activity, int size, String deviceID) {
		switch(size) {
		case 0:
			mInstance = new AdView(activity, AdSize.BANNER, deviceID);
			break;
		case 1:
			mInstance = new AdView(activity, AdSize.IAB_BANNER, deviceID);
			break;
		case 2:
			mInstance = new AdView(activity, AdSize.IAB_LEADERBOARD, deviceID);
			break;
		case 3:
			mInstance = new AdView(activity, AdSize.IAB_MRECT, deviceID);
			break;
		}
		adRequest = new AdRequest();
		//adRequest.addTestDevice("E3CE9F94F56824C07AE1C3A5B434F664");//for test
	}

	static {
	       try {
	           Class.forName("com.google.ads.AdView");
	       } catch (Exception ex) {
	           throw new RuntimeException(ex);
	       }
	   }
	
	public static void checkAvailable() {}
	
	public void loadAd() {
		mInstance.loadAd(adRequest);
	}
	
	void destroy() {
		mInstance.destroy();
	}
	
	public View getInstance() {
		return mInstance;
	}
}