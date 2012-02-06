package simple.home.jtbuaa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

public class AppAlphaList {

	//layout
	RelativeLayout view;
	
	//alpha list related
	GridView AlphaGrid;
	AlphaAdapter alphaAdapter;
	ArrayList<String> alphaList;
	final int MaxCount = 14;
	int mColumns;
    int mSelected = -1;
	Boolean DuringSelection = false;

	//app list related
	ListView AppList;
	List<ResolveInfo> mApps;
	ApplicationsAdapter appAdapter;
	static int whiteColor = 0xFFFFFFFF, grayColor = 0xDDDDDDDD, redColor = 0xFFFF7777, brownColor = 0xFFF8BF00;

	AlertDialog m_deleteDialog;
	ResolveInfo appToDel = null;
	Context mContext;
	PackageManager pm;
	boolean mLargeScreen;
	HashMap<String, Object> mPackagesSize;

	AppAlphaList(Context context, PackageManager pmgr, boolean largeScreen, HashMap<String, Object> packageSize) {
		mContext = context;
		pm = pmgr;
		mLargeScreen = largeScreen;//it is largeScreen if dm.widthPixels > 480
		mPackagesSize = packageSize;
		
		mApps = new ArrayList<ResolveInfo>();
		appAdapter = new ApplicationsAdapter(mContext, mApps);
		
		alphaList = new ArrayList<String>();
		alphaAdapter = new AlphaAdapter(mContext, alphaList);
				
		//init UI
    	view = (RelativeLayout) ((Activity) mContext).getLayoutInflater().inflate(R.layout.apps, null);
    	
    	AlphaGrid = (GridView) view.findViewById(R.id.alpha_list);
    	AlphaGrid.inflate(mContext, R.layout.alpha_list, null);
    	
    	AppList = (ListView) view.findViewById(R.id.applist); 
    	AppList.inflate(mContext, R.layout.app_list, null);
    	AppList.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if ((appAdapter.getCount() > 0) && (!DuringSelection)) {//revert the focus of alpha list when scroll app list
					String alpha = appAdapter.getItem(firstVisibleItem).activityInfo.applicationInfo.dataDir;
					int pos = alphaAdapter.getPosition(alpha);
					if (pos != mSelected) {
						TextView tv = (TextView)AlphaGrid.getChildAt(mSelected);
						if (tv != null) tv.setBackgroundResource(R.drawable.circle);//it may be circle_selected when user click in alpha grid, so we need set it back
					
						tv = (TextView)AlphaGrid.getChildAt(pos);
						if (tv != null) tv.requestFocus();//this will change its background color
						
						mSelected = pos;
					}
				}
			}
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				DuringSelection = false;//the scrollState will not change when setSelection(), but will change during scroll manually. so we turn off the flag here.
				//if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) scrolling = false;
				//else scrolling = true;//failed to get app running state
			}
    	});
	}

	void setAdapter() {
    	AlphaGrid.setAdapter(alphaAdapter);
    	AppList.setAdapter(appAdapter);
	}
	
	void sort() {
    	Collections.sort(mApps, new myComparator());//sort by name
    	
		String tmp = mApps.get(0).activityInfo.applicationInfo.dataDir;
    	alphaList.add(tmp);
    	for (int i = 1; i < mApps.size(); i++) {
    		String tmp2 = mApps.get(i).activityInfo.applicationInfo.dataDir;
    		if (!tmp.equals(tmp2)) {
    			tmp = tmp2;
    			alphaList.add(tmp);
    		}
    	}
    	
    	setColumns();
	}
	
	void add(ResolveInfo ri) {
		add(ri, true, true);
	}
	
	void add(ResolveInfo ri, boolean sort, boolean updateAlpha) {
		appAdapter.add(ri);
    	Collections.sort(appAdapter.localApplist, new myComparator());//sort by name
    	
    	if (updateAlpha) {
    		String tmp = ri.activityInfo.applicationInfo.dataDir.substring(0, 1);
    		if (!alphaList.contains(tmp)) {
    			alphaAdapter.add(tmp);
    	    	Collections.sort(alphaAdapter.localList, new stringCompatator());
    	    	setColumns();
    		}
    	}
	}
	
	private void removeAlpha(String alpha) {
		boolean found = false;
		for (int i = 0; i < appAdapter.getCount(); i++) {
			if (appAdapter.getItem(i).activityInfo.applicationInfo.dataDir.startsWith(alpha)) {
				found = true;
				break;
			}
		}
		if (!found) {
			alphaAdapter.remove(alpha);
			setColumns();
		}		
	}
	
	void remove(ResolveInfo info) {
		removeAlpha(info.activityInfo.applicationInfo.dataDir);
		appAdapter.remove(info);
	}
	
	ResolveInfo remove(String packageName) {
    	ResolveInfo info = null;
		for (int i = 0; i < appAdapter.getCount(); i++) {//once got a null pointer on v1.2.1. keep tracking  
			info = appAdapter.getItem(i);
			if (info.activityInfo.packageName.equals(packageName)) {
    			removeAlpha(info.activityInfo.applicationInfo.dataDir);
				appAdapter.remove(info);

				return info;
			}
		}
		return null;
	}
	
	void setColumns() {//set column number of alpha grid
		mColumns = MaxCount;
		if (alphaAdapter.getCount() < MaxCount) mColumns = alphaAdapter.getCount();
		else if (alphaAdapter.getCount() < MaxCount*2) mColumns = (int)(alphaAdapter.getCount()/2.0+0.5);
		AlphaGrid.setNumColumns(mColumns);
	}
	

	int getCount() {
		return appAdapter.getCount();
	}
	
    private class ApplicationsAdapter extends ArrayAdapter<ResolveInfo> {
    	ArrayList localApplist;
        public ApplicationsAdapter(Context context, List<ResolveInfo> apps) {
            super(context, 0, apps);
            localApplist = (ArrayList) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ResolveInfo info = (ResolveInfo) localApplist.get(position);

            if (convertView == null) 
                convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.app_list, parent, false);
            
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
            
           	if ((info.loadLabel(pm) == textView1.getText()) && (DuringSelection))//don't update the view here 
           		return convertView;//seldom come here
           	
           	if (info.loadLabel(pm) != textView1.getText()) {//only reset the appname, version, icon when needed
               	textView1.setText(info.loadLabel(pm));
               	
               	final boolean isUser = (info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
               	
                final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.appicon);
                btnIcon.setImageDrawable(info.loadIcon(pm));
                //if (android.os.Build.VERSION.SDK_INT >= 8) btnIcon.setEnabled(false);//currently we can't stop the other app after API level 8 if we have no platform signature
                btnIcon.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {//kill process when click
						ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
						am.restartPackage(info.activityInfo.packageName);
						//but we need to know when will it restart by itself?
						textView1.setTextColor(whiteColor);//set color back after kill it.
    					return false;
					}
                });
                
                LinearLayout lapp = (LinearLayout) convertView.findViewById(R.id.app);
                lapp.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						long pressTime = event.getEventTime() - event.getDownTime();//use this to avoid long click
						if ((pressTime > 0) && (pressTime < ViewConfiguration.getLongPressTimeout()) && (event.getAction() == MotionEvent.ACTION_UP)) {//start app when click
	    					if (util.startApp(info, mContext))//start success
	    						textView1.setTextColor(redColor);//red for running apk
							return true;
						}
						else return false;
					}
                });
            	lapp.setTag(new ricase(info, 2));
                ((Activity) mContext).registerForContextMenu(lapp); 
                
                final TextView btnVersion = (TextView) convertView.findViewById(R.id.appversion);
                try {
                	String version = pm.getPackageInfo(info.activityInfo.packageName, 0).versionName;
                	if ((version == null) || (version.trim().equals(""))) version = String.valueOf(pm.getPackageInfo(info.activityInfo.packageName, 0).versionCode);
                	btnVersion.setText(version);
    			} catch (NameNotFoundException e) {
    				btnVersion.setText(e.toString());
    			}
                btnVersion.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
    					if (isUser) {//user app
    						Uri uri = Uri.fromParts("package", info.activityInfo.packageName, null);
    						Intent intent = new Intent(Intent.ACTION_DELETE, uri);
    						util.startActivity(intent, true, mContext);
    						btnVersion.requestFocus();
    					}
    					else {//system app
    						appToDel = info;
    						showDelDialog(info.loadLabel(pm) + " " + btnVersion.getText());
    					}
					}
                });
    			
                final TextView textView3 = (TextView) convertView.findViewById(R.id.appsource);
                String source = "";
                Object o = mPackagesSize.get(info.activityInfo.packageName);
                if(o != null) source = o.toString();
                if((info.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) == ApplicationInfo.FLAG_DEBUGGABLE) {
                	textView3.setTextColor(brownColor);//brown for debuggable apk
                	source += " (debuggable)";
                }
                else textView3.setTextColor(grayColor);//gray for normal
            	textView3.setText(source);
           	}
           	
            textView1.setTextColor(whiteColor);//default color
            if (!DuringSelection) {//running state should be updated when not busy, for it is time consuming
                final ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<RunningAppProcessInfo> appList = am.getRunningAppProcesses();
                for (int i = 0; i < appList.size(); i++) {//a bottle neck
                	if (DuringSelection) break;//cancel current task if enter scroll mode will raise performance significantly
            		RunningAppProcessInfo as = (RunningAppProcessInfo) appList.get(i);
                	if (info.activityInfo.processName.equals(as.processName)) {
                    	textView1.setTextColor(redColor);//red for running apk
            			break;
            		}
                }
            }
           	
            return convertView;
        }
    }


    void showDelDialog(String title) {
    	if (m_deleteDialog == null) {
			m_deleteDialog = new AlertDialog.Builder(mContext).
			setTitle(title).
			setIcon(R.drawable.error).
			setMessage(R.string.warning).
			setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}).
			setPositiveButton(mContext.getString(R.string.delete), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {//rm system app
					String apkToDel = appToDel.activityInfo.applicationInfo.sourceDir;
					ShellInterface.doExec(new String[] {"mv " + apkToDel + " " + apkToDel + ".bak"});
					Uri uri = Uri.fromParts("package", appToDel.activityInfo.packageName, null);
					Intent intent = new Intent(Intent.ACTION_DELETE, uri);
					util.startActivity(intent, true, mContext);//this will launch package installer. after it close, onResume() will be invoke.
				}
			}).create();
        }
    	else m_deleteDialog.setTitle(title);
    	m_deleteDialog.show();
    }

    
    private class AlphaAdapter extends ArrayAdapter<String> {
    	ArrayList<String> localList;
        public AlphaAdapter(Context context, List<String> alphas) {
            super(context, 0, alphas);
            localList = (ArrayList<String>) alphas;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                final LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(R.layout.alpha_list, parent, false);
            }
            
        	if (alphaList.size() > mColumns) {//only tune it if more than one line
                if (position < mColumns) {//tune gravity to show diversify
                	if (mLargeScreen)
                    	((TextView)convertView).setGravity(Gravity.CENTER);
                	else
                    	((TextView)convertView).setGravity(Gravity.LEFT);
                }
                else 
                	((TextView)convertView).setGravity(Gravity.RIGHT);
        	}
        	
            final TextView btn = (TextView) convertView.findViewById(R.id.alpha);
            btn.setText(localList.get(position));
            btn.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {//find app when click
					String tmp = localList.get(position);
					DuringSelection = true;
					v.requestFocusFromTouch();//this will make app list get focus, very strange
					TextView tv = (TextView)AlphaGrid.getChildAt(mSelected);//restore the background
					if (tv != null) tv.setBackgroundResource(R.drawable.circle);
					mSelected = position;
					for (int i = 0; i < appAdapter.getCount(); i++) {
						if (appAdapter.getItem(i).activityInfo.applicationInfo.dataDir.startsWith(tmp)) {
							AppList.requestFocusFromTouch();
							AppList.setSelection(i);
							break;
						}
					}
					v.setBackgroundResource(R.drawable.circle_selected);//only set the background of selected one 
					return false;
				}
            });
            
        	return convertView;
        }
    }
    

}
