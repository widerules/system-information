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
		mInstance.setAdListener(new Listener());
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
	
	class Listener implements AdListener {
		@Override
		public void onDismissScreen(Ad arg0) {
		}

		@Override
		public void onFailedToReceiveAd(Ad arg0, ErrorCode error) {
			if (mHandler != null) {
				Message fail = mHandler.obtainMessage();
				Bundle data = new Bundle();
				data.putString("msg", "onFailedToReceiveAd (" + error + ")");
				fail.setData(data);
				fail.what = -2;
				mHandler.sendMessage(fail);
			}
		}

		@Override
		public void onLeaveApplication(Ad arg0) {
			if (mHandler != null) {
				Message dismiss = mHandler.obtainMessage();
				dismiss.what = 1;
				mHandler.sendMessage(dismiss);
			}
		}

		@Override
		public void onPresentScreen(Ad arg0) {
		}

		@Override
		public void onReceiveAd(Ad arg0) {
		}
	}
}
