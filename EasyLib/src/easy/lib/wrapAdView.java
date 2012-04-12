package easy.lib;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;
import com.google.ads.AdView;


public class wrapAdView {
	AdView mInstance;
	AdRequest adRequest;
	Handler mHandler;
	
	class Listener implements AdListener {
		@Override
		public void onDismissScreen(Ad arg0) {//Called when an ad is clicked and about to return to the application.
	    	if (mHandler != null) mHandler.sendMessage(mHandler.obtainMessage());
		}

		@Override
		public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
		}

		@Override
		public void onLeaveApplication(Ad arg0) {
		}

		@Override
		public void onPresentScreen(Ad arg0) {//Called when an Activity is created in front of the app
		}

		@Override
		public void onReceiveAd(Ad arg0) {
		}
	}

	public wrapAdView(Activity activity, int size, String deviceID, Handler handler) {
		mHandler = handler;
		
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
		try {adRequest = new AdRequest(); } catch (Exception e) {}
		//adRequest.addTestDevice("E3CE9F94F56824C07AE1C3A5B434F664");//for test
		
		mInstance.setAdListener(new Listener());
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
		if (mInstance != null) mInstance.loadAd(adRequest);
	}
	
	void destroy() {
		if (mInstance != null) mInstance.destroy();
	}
	
	public View getInstance() {
		return mInstance;
	}
}