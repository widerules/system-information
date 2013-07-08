package base.lib;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.InterstitialAd;
import com.google.ads.AdRequest.ErrorCode;

import base.lib.WrapAdView.Listener;

public class WrapInterstitialAd {
	InterstitialAd mInstance;
	AdRequest adRequest;
	Handler mHandler;

	public WrapInterstitialAd(Activity activity, String publisherID, Handler handler) {
		mInstance = new InterstitialAd(activity, publisherID);
		adRequest = new AdRequest();
		mHandler = handler;
	}

	public boolean isReady() {
		if (mInstance != null)
			return mInstance.isReady();
		else return false;
	}
	
	public void show() {
		if (mInstance != null) mInstance.show();
	}
	
	public void loadAd() {
		if ((mInstance != null) && (adRequest != null))
			mInstance.loadAd(adRequest);
	}
}
