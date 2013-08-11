package common.lib;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.text.ClipboardManager;
import android.util.Log;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WebAdapter extends ArrayAdapter<MyWebView> {
	ArrayList localWeblist;
	SimpleBrowser mActivity;

	public WebAdapter(SimpleBrowser activity, List<MyWebView> webs) {
		super(activity, 0, webs);
		localWeblist = (ArrayList) webs;
		mActivity = activity;
	}

	@Override
	public View getView(final int position, View convertView,
			ViewGroup parent) {
		final MyWebView wv = (MyWebView) localWeblist.get(position);

		final LayoutInflater inflater = mActivity.getLayoutInflater();
		if (mActivity.needRevert) 
			convertView = inflater.inflate(R.layout.revert_web_list, parent, false);
		else
			convertView = inflater.inflate(R.layout.web_list, parent, false);

		if (position == mActivity.webIndex)
			convertView.setBackgroundColor(0x80ffffff);
		else
			convertView.setBackgroundColor(0);

		final ImageView btnIcon = (ImageView) convertView
				.findViewById(R.id.webicon);
		try {
			btnIcon.setImageBitmap(wv.getFavicon());
		} catch (Exception e) {
		}// catch an null pointer exception on 1.6}

		TextView webIndex = (TextView) convertView.findViewById(R.id.webindex);
		webIndex.setText(position+1 + ".");
		
		final TextView webname = (TextView) convertView
				.findViewById(R.id.webname);
		if ((wv.getTitle() != null) && (!"".equals(wv.getTitle())))
			webname.setText(wv.getTitle());
		else
			webname.setText(wv.m_url);

		webname.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mActivity.webControl.setVisibility(View.GONE);
				mActivity.changePage(position);
			}
		});
		webname.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				CharSequence operations[];
				if (mActivity.mAdAvailable) operations = new CharSequence[8];
				else operations = new CharSequence[10];

				operations[0] = mActivity.getString(R.string.open);
				operations[1] = mActivity.getString(R.string.shareurl); 
				operations[2] = mActivity.getString(R.string.save);
				operations[3] = mActivity.getString(R.string.copy_url); 
				operations[4] = mActivity.getString(R.string.bookmark); 
				operations[5] = mActivity.getString(R.string.remove_history);
				if (mActivity.mAdAvailable) {
					operations[6] = mActivity.getString(R.string.close);
					operations[7] = mActivity.getString(R.string.close_all);
				}
				else {
					operations[6] = mActivity.getString(R.string.set_homepage);
					operations[7] = mActivity.getString(R.string.add_shortcut);
					operations[8] = mActivity.getString(R.string.close);
					operations[9] = mActivity.getString(R.string.close_all);
				}

				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle(webname.getText());
				builder.setItems(operations, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	switch(which) {
				    	case 0:// open
				    		webname.performClick();
				    		break;
				    	case 1:// share url
				    		mActivity.shareUrl("", wv.m_url);
				    		break;
				    	case 2:// save
				    		mActivity.appstate.startDownload(wv.m_url, "", "no");
				    		break;
				    	case 3:// copy url
							ClipboardManager ClipMan = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
							ClipMan.setText(wv.m_url);
				    		break;
				    	case 4:// bookmark
							String url = wv.m_url;
							if (mActivity.HOME_PAGE.equals(url)) return;// not add home page
							mActivity.addRemoveFavo(url, webname.getText().toString());
				    		break;
				    	case 5:// remove history
				    		for (int i = mActivity.mHistory.size() - 1; i >= 0; i--)
				    			if (mActivity.mHistory.get(i).m_url.equals(wv.m_url)) {
				    				mActivity.removeHistory(i);
				    				break;
				    			}
				    		break;
				    	case 6:// close or set homepage
				    		if (mActivity.mAdAvailable) mActivity.closePage(position, false);
				    		else {
				    			mActivity.m_homepage = wv.m_url;
				    			mActivity.sEdit.putString("homepage", wv.m_url);
				    			mActivity.sEdit.commit();
				    		}
				    		break;
				    	case 7:// close all or add shortcut
				    		if (mActivity.mAdAvailable) {
				    			while (mActivity.serverWebs.size() > 1) mActivity.closePage(0, false);
				    			mActivity.loadPage();
				    		}
				    		else 
				    			mActivity.createShortcut(wv.m_url, webname.getText().toString());
				    		break;
				    	case 8:// close
				    		mActivity.closePage(position, false);
				    		break;
				    	case 9:// close all
				    		while (mActivity.serverWebs.size() > 1) mActivity.closePage(0, false);
				    		mActivity.loadPage();
				    		break;
				    	}
				    }
				});
				builder.show();					
				return true;
			}
		});

		ImageView btnStop = (ImageView) convertView
				.findViewById(R.id.webclose);
		btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mActivity.closePage(position, false);
			}
		});

		return convertView;
	}
}