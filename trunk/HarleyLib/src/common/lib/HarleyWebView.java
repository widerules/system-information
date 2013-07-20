package common.lib;

import easy.lib.R;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebChromeClient.CustomViewCallback;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class HarleyWebView extends MyWebView {
	public HarleyWebView(Context context, HarleyApp appstate) {
		super(context, appstate);
		
		setScrollbarFadingEnabled(true);// hide scroll bar when not scroll. from API5, not work on cupcake.

		// open Geolocation by default
		localSettings.setGeolocationEnabled(true);//API5
		//localSettings.setGeolocationDatabasePath(getDir("databases", MODE_PRIVATE).getPath());//API5. no use for get location in baidu map?

		localSettings.setDefaultZoom(ZoomDensity.MEDIUM);//start from API7

		localSettings.setDomStorageEnabled(true);// API7, key to enable gmail

		// loads the WebView completely zoomed out. fit for hao123, but not fit for homepage. from API7
		//localSettings.setUseWideViewPort(overviewPage);
		localSettings.setLoadWithOverviewMode(overviewPage);
		
		setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int progress) {
				if (progress == 100)
					mProgress = 0;
				else mProgress = progress;

				if (isForeground) {
					loadProgress.setProgress(progress);
					if (progress == 100)
						loadProgress.setVisibility(View.INVISIBLE);
				}
			}

			// For Android 3.0+
			public void openFileChooser(ValueCallback<Uri> uploadMsg,
					String acceptType) {
				if (null == mUploadMessage)
					mUploadMessage = new wrapValueCallback();
				mUploadMessage.mInstance = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				startActivityForResult(Intent.createChooser(i,
						getString(R.string.select_file)),
						FILECHOOSER_RESULTCODE);
			}

			// For Android < 3.0
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				openFileChooser(uploadMsg, "");
			}

			
			//I don't know how to reflect a Interface, so it will crash on cupcake
			@Override
			public void onGeolocationPermissionsShowPrompt(final String origin,
					final GeolocationPermissions.Callback callback) {
				callback.invoke(origin, true, false);//use maps.google.com or map.baidu.com to verify
			}

			@Override
			public View getVideoLoadingProgressView() {
				TextView view = new TextView(mContext);
				view.setText(R.string.wait);
				view.setTextSize(20);
				return view;// this if for hint when load video
			}
			
			@Override
			public void onShowCustomView(View view,	final CustomViewCallback callback) {
				super.onShowCustomView(view, callback);

				if (view instanceof FrameLayout) {
					mCustomViewContainer = (FrameLayout) view;
					mCustomViewCallback = callback;
					if (mCustomViewContainer.getFocusedChild() instanceof VideoView) {
						mVideoView = (VideoView) mCustomViewContainer.getFocusedChild();
						mVideoView.setOnCompletionListener(new OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								mp.stop();
								onHideCustomView();
							}
						});
						mVideoView.setOnErrorListener(new OnErrorListener() {
							@Override
							public boolean onError(MediaPlayer mp, int what, int extra) {
								mp.stop();
								onHideCustomView();
								return true;
							}
						});
						mVideoView.requestFocus();
						mVideoView.start();
					}
					else ;//it is android.webkit.HTML5VideoFullScreen$VideoSurfaceView instead of VideoView
					
					browserView.setVisibility(View.GONE);
                    setContentView(mCustomViewContainer);
				}
			}// API 7. http://www.w3.org/2010/05/video/mediaevents.html for verify

			public void onHideCustomView() {
				hideCustomView();
			}


			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog,
					boolean isUserGesture, android.os.Message resultMsg) {
				if (openNewPage(null, webIndex+1, true, true)) {// open new page success
					((WebView.WebViewTransport) resultMsg.obj)
							.setWebView(serverWebs.get(webIndex));
					resultMsg.sendToTarget();
					return true;
				} else return false;
			}
		});
	}
	
	private float oldX, oldY;
	// indicate if horizontal scrollbar can't go more to the left
	private boolean overScrollLeft = false;
	// indicate if horizontal scrollbar can't go more to the right
    private boolean overScrollRight = false;
    // indicate if horizontal scrollbar can't go more to the left OR right
    private boolean isScrolling = false;
    private boolean dragEdge = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {// onTouchListener may not work. so put relate code here
		int scrollBarWidth = getVerticalScrollbarWidth();
		// width of the view depending of you set in the layout
		int viewWidth = computeHorizontalScrollExtent();
		// width of the webpage depending of the zoom
        int innerWidth = computeHorizontalScrollRange();
        // position of the left side of the horizontal scrollbar
        int scrollBarLeftPos = computeHorizontalScrollOffset();
        // position of the right side of the horizontal scrollbar, the width of scroll is the width of view minus the width of vertical scrollbar
        int scrollBarRightPos = scrollBarLeftPos + viewWidth - scrollBarWidth;

        // if left pos of scroll bar is 0 left over scrolling is true
        if(scrollBarLeftPos == 0) {
            overScrollLeft = true;
        } else {
            overScrollLeft = false;
        }

        // if right pos of scroll bar is superior to webpage width: right over scrolling is true
        //Log.d("=============scrollBarRightPos", scrollBarRightPos+"");
        //Log.d("=============innerWidth", innerWidth+"");
        if(scrollBarRightPos >= innerWidth-6) {
            overScrollRight = true;
        } else {
            overScrollRight = false;
        }

        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: // when user touch the screen
        	dragEdge = false;
        	
        	// if scrollbar is the most left or right
            if(overScrollLeft || overScrollRight) {
                isScrolling = false;
            } else {
                isScrolling = true;
            }
            oldX = event.getX();
            oldY = event.getY();
            
			if (!this.isFocused()) {
				this.setFocusableInTouchMode(true);
				this.requestFocus();
				webAddress.setFocusableInTouchMode(false);
				webAddress.clearFocus();
			}
            break;
        case MotionEvent.ACTION_MOVE: // when user stop to touch the screen
        	// if scrollbar can't go more to the left OR right 
        	// this allow to force the user to do another gesture when he reach a side
            if (!isScrolling && (Math.abs(oldY - event.getY()) < 50)) {
                if ((event.getX() > oldX+100) && overScrollLeft && (oldX < 100)) {
                    // left action
                	if (!reverted) {
                		if (!bookmarkOpened) showBookmark();
                	}
                	else {
	                	if (!menuOpened) onMenuOpened(0, null);
                	}
                	dragEdge = true;
                }
                else if ((event.getX() < oldX-100) && overScrollRight && (oldX > dm.widthPixels-100)) {
                	// right action
                	if (!reverted) {
                		if (!menuOpened) onMenuOpened(0, null);
                	}
                	else {
                		if (!bookmarkOpened) showBookmark();
                	}
                	dragEdge = true;
                }
            }
            break;
        case MotionEvent.ACTION_UP:
            if (!dragEdge) {
            	scrollToMain();

            	if (webControl.getVisibility() == View.VISIBLE)// close webcontrol page if it is open.
					webControl.setVisibility(View.INVISIBLE);
								
				if (!showUrl) setUrlHeight(false);
				if (!showControlBar) setBarHeight(false);
            }
            break;
        default:
            break;
        }
		        
		return super.onTouchEvent(event);
	}
}
