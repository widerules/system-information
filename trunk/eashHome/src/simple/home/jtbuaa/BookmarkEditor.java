package simple.home.jtbuaa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarkEditor extends Activity{
	ArrayList<TitleUrl> mBookMark = new ArrayList<TitleUrl>();
	ArrayList<TitleUrl> mHistory = new ArrayList<TitleUrl>();
	ListView bookmarkList, historyList;
	BookmarkAdapter bAdapter, hAdapter;
	boolean deleted;
	Button btnHistory, btnBookmark;

	@Override
	protected void onResume() {
		
		FileInputStream fi = null;
		try {
			fi = openFileInput("history");
			mHistory = util.readBookmark(fi);
			fi = openFileInput("bookmark");
			mBookMark = util.readBookmark(fi);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		deleted = false;
		
		hAdapter = new BookmarkAdapter(getBaseContext(), mHistory);
		historyList.setAdapter(hAdapter);
		
		bAdapter = new BookmarkAdapter(getBaseContext(), mBookMark);
		bookmarkList.setAdapter(bAdapter);

		super.onResume();
	}

	@Override
	protected void onPause() {
		if (deleted) {//only backup the list if it changed.
			FileOutputStream fo;
			try {
				fo = this.openFileOutput("bookmark", 0);
				util.writeBookmark(fo, mBookMark);
				fo = this.openFileOutput("history", 0);
				util.writeBookmark(fo, mHistory);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
    	setContentView(R.layout.bookmarks);
    	bookmarkList = (ListView) findViewById(R.id.bookmark_list);
    	historyList = (ListView) findViewById(R.id.history_list);
    	
    	btnBookmark = (Button) findViewById(R.id.bookmark);
    	btnBookmark.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				historyList.setVisibility(View.INVISIBLE);
				bookmarkList.setVisibility(View.VISIBLE);
				btnBookmark.setBackgroundResource(R.drawable.button_layout);
				btnHistory.setBackgroundColor(0x0);
			}
    	});
    	
    	btnHistory = (Button) findViewById(R.id.history);
    	btnHistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				historyList.setVisibility(View.VISIBLE);
				bookmarkList.setVisibility(View.INVISIBLE);
				btnHistory.setBackgroundResource(R.drawable.button_layout);
				btnBookmark.setBackgroundColor(0x0);
			}
    	});
	}
	
    private class BookmarkAdapter extends ArrayAdapter<TitleUrl> {
    	ArrayList localList;
        public BookmarkAdapter(Context context, List<TitleUrl> apps) {
            super(context, 0, apps);
            localList = (ArrayList) apps;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final TitleUrl tu = (TitleUrl) localList.get(position);

            if (convertView == null) 
                convertView = getLayoutInflater().inflate(R.layout.url_list, parent, false);
            
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
           	textView1.setText(tu.m_title);

            final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.appicon);
	    	Drawable bd = (Drawable) Drawable.createFromPath(getFilesDir().getAbsolutePath() + "/" + tu.m_site + ".png");
            btnIcon.setImageDrawable(bd);

            LinearLayout lapp = (LinearLayout) convertView.findViewById(R.id.app);
            lapp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setClassName(getPackageName(), getPackageName()+".SimpleBrowser");
					intent.setData(Uri.parse(tu.m_url));
					intent.putExtra("update", true);
					util.startActivity(intent, true, getBaseContext());
					finish();//it will show bookmark when click history if not finish()
				}
            });
            
            final TextView btnVersion = (TextView) convertView.findViewById(R.id.appversion);
			btnVersion.setText(R.string.delete);
            btnVersion.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					localList.remove(tu);
					bAdapter.notifyDataSetChanged();
					hAdapter.notifyDataSetChanged();
					deleted = true;//mark as changed
				}
            });
            
            final TextView textView3 = (TextView) convertView.findViewById(R.id.appsource);
        	textView3.setText(tu.m_url);

            return convertView;
        }
    }
}
