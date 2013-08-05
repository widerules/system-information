package common.lib;

import java.lang.reflect.Method;

import android.webkit.WebSettings;

public class WrapWebSettings {
	WebSettings mInstance;

    public enum ZoomDensity {
        FAR(150),      // 240dpi
        MEDIUM(100),    // 160dpi
        CLOSE(75);     // 120dpi
        ZoomDensity(int size) {
            value = size;
        }
        int value;
    }

    public WrapWebSettings(WebSettings settings) {
		mInstance = settings;
	}

	static Method setDatabaseEnabled = null;
	static Method setDatabasePath = null;
	static Method setGeolocationEnabled = null;
	static Method setGeolocationDatabasePath = null;
	static Method setLoadWithOverviewMode = null;
	static Method setAppCacheEnabled = null;
	static Method setAppCachePath = null;
	static Method setAppCacheMaxSize = null;
	static Method setDomStorageEnabled = null;
	static Method setDefaultZoom = null;
	static Method setDisplayZoomControls = null;
	static Method setForceUserScalable = null;
	static {
		try {
			//API 5
			setDatabaseEnabled = WebSettings.class.getMethod("setDatabaseEnabled", new Class[] { boolean.class });
			setDatabasePath = WebSettings.class.getMethod("setDatabasePath", new Class[] { String.class });
			setGeolocationEnabled = WebSettings.class.getMethod("setGeolocationEnabled", new Class[] { boolean.class });
			setGeolocationDatabasePath = WebSettings.class.getMethod("setGeolocationDatabasePath", new Class[] { String.class });
			
			//API 7
			setLoadWithOverviewMode = WebSettings.class.getMethod("setLoadWithOverviewMode", new Class[] { boolean.class });
			setAppCacheEnabled = WebSettings.class.getMethod("setAppCacheEnabled", new Class[] { boolean.class });
			setAppCachePath = WebSettings.class.getMethod("setAppCachePath", new Class[] { String.class });
			setAppCacheMaxSize = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[] { long.class });
			setDomStorageEnabled = WebSettings.class.getMethod("setDomStorageEnabled", new Class[] { boolean.class });
			setDefaultZoom = WebSettings.class.getMethod("setDefaultZoom", new Class[] { ZoomDensity.class });
			
			//API 11
			setDisplayZoomControls = WebSettings.class.getMethod("setDisplayZoomControls", new Class[] { boolean.class });
			
			//API 14
			setForceUserScalable = WebSettings.class.getMethod("setForceUserScalable", new Class[] { boolean.class });
		} catch (Exception e) {}
	}

	public synchronized void setDatabaseEnabled(boolean flag) {// API 5
		if (setDatabaseEnabled != null)
			try {setDatabaseEnabled.invoke(mInstance, flag);} catch (Exception e) {}
	}

	public synchronized void setDatabasePath(String databasePath) {// API 5
		if (setDatabasePath != null)
			try {setDatabasePath.invoke(mInstance, databasePath);} catch (Exception e) {}
	}

	public synchronized void setGeolocationEnabled(boolean enabled) {// API 5
		if (setGeolocationEnabled != null)
			try {setGeolocationEnabled.invoke(mInstance, enabled);} catch (Exception e) {}
	}

	public synchronized void setGeolocationDatabasePath(String databasePath) {// API 5
		if (setGeolocationDatabasePath != null)
			try {setGeolocationDatabasePath.invoke(mInstance, databasePath);} catch (Exception e) {}
	}

	public synchronized void setLoadWithOverviewMode(boolean overview) {// API 7
		if (setLoadWithOverviewMode != null)
			try {setLoadWithOverviewMode.invoke(mInstance, overview);} catch (Exception e) {}
	}

	public synchronized void setAppCacheEnabled(boolean flag) {// API 7
		if (setAppCacheEnabled != null)
			try {setAppCacheEnabled.invoke(mInstance, flag);} catch (Exception e) {}
	}

	public synchronized void setAppCachePath(String databasePath) {// API 7
		if (setAppCachePath != null)
			try {setAppCachePath.invoke(mInstance, databasePath);} catch (Exception e) {}
	}

	public synchronized void setAppCacheMaxSize(long max) {// API 7
		if (setAppCacheMaxSize != null)
			try {setAppCacheMaxSize.invoke(mInstance, max);} catch (Exception e) {}
	}

	public synchronized void setDomStorageEnabled(boolean flag) {// API 7
		if (setDomStorageEnabled != null)
			try {setDomStorageEnabled.invoke(mInstance, flag);} catch (Exception e) {}
	}

	public synchronized void setDefaultZoom(ZoomDensity zoom) {// API 7
		if (setDefaultZoom != null)
			try {setDefaultZoom.invoke(mInstance, zoom);} catch (Exception e) {}
	}

	public synchronized boolean setDisplayZoomControls(boolean enabled) {// API 11
		if (setDisplayZoomControls != null)
			try {
				setDisplayZoomControls.invoke(mInstance, enabled);
				return true;
			} catch (Exception e) {}
		return false;
	}
	
	public synchronized void setForceUserScalable(boolean flag) {// API 14
		if (setForceUserScalable != null)
			try {setForceUserScalable.invoke(mInstance, flag);} catch (Exception e) {}
	}
}