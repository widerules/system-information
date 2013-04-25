package easy.lib;

import android.app.Activity;

import com.google.ads.AdRequest;
import com.google.ads.InterstitialAd;

public class wrapInterstitialAd {
	InterstitialAd mInstance;
	AdRequest adRequest;
	
	public wrapInterstitialAd(Activity activity, String publisherID) {
		mInstance = new InterstitialAd(activity, publisherID);
		adRequest = new AdRequest();
	}

	boolean isReady() {
		if (mInstance != null)
			return mInstance.isReady();
		else return false;
	}
	
	void show() {
		if (mInstance != null) mInstance.show();
	}
	
	void loadAd() {
		if ((mInstance != null) && (adRequest != null))
			mInstance.loadAd(adRequest);
	}
}
