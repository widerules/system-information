package simple.home.jtbuaa;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarkEditor extends Activity{
	ArrayList<TitleUrl> mBookMark = new ArrayList<TitleUrl>();
	ListView bookmarkList;
	BookmarkAdapter bAdapter;

	@Override
	protected void onResume() {
		
		FileInputStream fi = null;
		try {
			fi = openFileInput(getIntent().getStringExtra("filename"));
			mBookMark = util.readBookmark(fi);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		bAdapter = new BookmarkAdapter(getBaseContext(), mBookMark);
		bookmarkList.setAdapter(bAdapter);

		super.onResume();
	}

	@Override
	protected void onPause() {
		FileOutputStream fo;
		try {
			fo = this.openFileOutput(getIntent().getStringExtra("filename"), 0);
			util.writeBookmark(fo, mBookMark);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		super.onPause();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
    	setContentView(R.layout.bookmarks);
    	bookmarkList = (ListView) findViewById(R.id.bookmark_list); 

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
                convertView = getLayoutInflater().inflate(R.layout.app_list, parent, false);
            
            final TextView textView1 = (TextView) convertView.findViewById(R.id.appname);
           	textView1.setText(tu.m_title);

            return convertView;
        }
    }
}
