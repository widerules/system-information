package easy.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import base.lib.WebUtil;

import common.lib.EasyApp;
import common.lib.TitleUrl;

public class MyListAdapter extends ArrayAdapter<TitleUrl> {
	Context mContext;
	EasyApp mAppstate;
	ArrayList localList;
	public int type = 0;// 0:bookmark, 1:history, 2:downloads

	public MyListAdapter(Context context, List<TitleUrl> titles, EasyApp appstate) {
		super(context, 0, titles);
		mContext = context;
		mAppstate = appstate;
		localList = (ArrayList) titles;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup parent) {
		final TitleUrl tu = (TitleUrl) localList.get(position);

		if (convertView == null) {
			final LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			convertView = inflater.inflate(R.layout.web_list, parent, false);
			
			convertView.setBackgroundResource(R.drawable.webname_layout);
		}

		final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.webicon);
		String filename;
		if (type != 2) filename = mContext.getFilesDir().getAbsolutePath() + "/" + tu.m_site + ".png";
		else filename = mContext.getFilesDir().getAbsolutePath() + "/" + WebUtil.getSite(tu.m_site) + ".png";
		
		File f = new File(filename);
		if (f.exists())
			try {
				btnIcon.setImageURI(Uri.parse(filename));
				btnIcon.setVisibility(View.VISIBLE);
			} catch (Exception e) {}// catch an null pointer exception on 1.6
		else btnIcon.setVisibility(View.GONE);

		final ImageView btnDelete = (ImageView) convertView.findViewById(R.id.webclose);
		btnDelete.setVisibility(View.GONE);
		btnDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (type == 0) {// bookmark
					File favicon = new File(mAppstate.dataPath + "files/" + mAppstate.mBookMark.get(position).m_site + ".png");
					favicon.delete();//delete favicon of the site
					mAppstate.mBookMark.remove(position);
					mAppstate.updateBookmark();
				}
				else if (type == 1) {// history
					mAppstate.mHistory.remove(mAppstate.mHistory.size() - 1 - position);
					mAppstate.updateHistory();
				}
				else {// downloads
					mAppstate.mDownloads.remove(position);
					mAppstate.updateDownloads();
				}
				btnDelete.setVisibility(View.GONE);
			}
		});

		TextView webname = (TextView) convertView.findViewById(R.id.webname);
		webname.setText(tu.m_title);
		if (type == 1) webname.setTextColor(0xffddddff);
		else if (type == 2) webname.setTextColor(0xffffdd8b);

		webname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (type != 2) {// bookmark and history
					mAppstate.serverWebs.get(mAppstate.webIndex).loadUrl(tu.m_url);
					mAppstate.imgBookmark.performClick();
				}
				else // open downloads file
					mAppstate.openDownload(tu);
			}
		});
		webname.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (btnDelete.getVisibility() == View.GONE) 
					btnDelete.setVisibility(View.VISIBLE);
				else btnDelete.setVisibility(View.GONE);
				return true;
			}
		});

		return convertView;
	}
}
