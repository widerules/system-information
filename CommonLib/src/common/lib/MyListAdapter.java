package common.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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


public class MyListAdapter extends ArrayAdapter<TitleUrl> {
	SimpleBrowser mActivity;
	ArrayList localList;
	public int type = 0;// 0:bookmark, 1:history, 2:downloads

	public MyListAdapter(SimpleBrowser activity, List<TitleUrl> titles) {
		super(activity, 0, titles);
		mActivity = activity;
		localList = (ArrayList) titles;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup parent) {
		final TitleUrl tu = (TitleUrl) localList.get(position);

		if (convertView == null) {
			final LayoutInflater inflater = mActivity.getLayoutInflater();
			convertView = inflater.inflate(R.layout.web_list, parent, false);
			
			convertView.setBackgroundResource(R.drawable.webname_layout);
		}

		final ImageView btnIcon = (ImageView) convertView.findViewById(R.id.webicon);
		String filename;
		if (type != 2) filename = mActivity.getFilesDir().getAbsolutePath() + "/" + tu.m_site + ".png";
		else filename = mActivity.getFilesDir().getAbsolutePath() + "/" + WebUtil.getSite(tu.m_site) + ".png";
		
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
					File favicon = new File(mActivity.appstate.dataPath + "files/" + mActivity.mBookMark.get(position).m_site + ".png");
					favicon.delete();//delete favicon of the site
					mActivity.mBookMark.remove(position);
					mActivity.updateBookmark();
				}
				else if (type == 1) {// history
					mActivity.mHistory.remove(mActivity.mHistory.size() - 1 - position);
					mActivity.updateHistory();
				}
				else {// downloads
					mActivity.mDownloads.remove(position);
					mActivity.updateDownloads();
				}
				btnDelete.setVisibility(View.GONE);
			}
		});

		TextView webIndex = (TextView) convertView.findViewById(R.id.webindex);
		webIndex.setText(position+1 + ".");
		
		TextView webname = (TextView) convertView.findViewById(R.id.webname);
		webname.setText(tu.m_title);
		if (type == 1) webname.setTextColor(0xffddddff);
		else if (type == 2) webname.setTextColor(0xffffdd8b);

		webname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (type != 2) {// bookmark and history
					mActivity.serverWebs.get(mActivity.webIndex).loadUrl(tu.m_url);
					mActivity.hideBookmark();
				}
				else // open downloads file
					WebUtil.openDownload(tu, mActivity);
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