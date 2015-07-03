package com.ianglei.jdaily.fragment;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ianglei.jdaily.R;
import com.ianglei.jdaily.db.ListeningDBHelper;
import com.ianglei.jdaily.model.ListeningItem;

public class DetailAdapter extends BaseAdapter
{
	private Context context;
	private ListeningItem item;
	private ProgressBar loadingBar;
	private String transcript;
	private TextView transcriptTextView;
	
	public DetailAdapter(Context context, ListeningItem item)
	{
		this.context = context;
		this.item = item;
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.activity_detail, null);
			viewHolder = new ViewHolder();
			viewHolder.titleTextView = (TextView) convertView
					.findViewById(R.id.ItemTitle);
			viewHolder.coverImgView = (ImageView) convertView
					.findViewById(R.id.coverimg);
			viewHolder.transTextView = (TextView) convertView.findViewById(R.id.Transcript);
			transcriptTextView = viewHolder.transTextView;
			loadingBar = (ProgressBar)convertView.findViewById(R.id.ProgressBar);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		ListeningDBHelper.queryDetailById(item);
		transcript = item.getTranscript();
		
		if(transcript == null)
		{
//			TranscriptTask task = new TranscriptTask();
//			task.execute(item);			
		}
		else {
			viewHolder.transTextView.setText(transcript);
		}

		return null;
	}
	
//	class TranscriptTask extends AsyncTask<ListeningItem, Integer, String>
//	{
//		ListeningItem item;
//		
//		@Override
//		protected String doInBackground(ListeningItem... params)
//		{
//			item = params[0];
//			Document doc;
//			try
//			{
//				doc = Jsoup.connect(item.getLink()).timeout(45000).get();
//				int pcount = doc.select("div.text p, div.text br").size();
//				
//				StringBuilder builder = new StringBuilder(2048);
//							
//				for(int i = 0; i < pcount; i++)
//				{
//					Element dateElement = doc.select("div.text p, div.text br").get(i);
//					String dateText = dateElement.text();
//					builder.append(dateText);
//					builder.append("\n");
//				}
//				
//				transcript = builder.toString();
//				return transcript;
//			} catch (IOException e)
//			{
//				e.printStackTrace();
//				return null;
//			}
//		}
//		
//		@Override
//		protected void onPostExecute(String result)
//		{
//			super.onPostExecute(result);
//			loadingBar.setVisibility(View.GONE);
//			transcriptTextView.setText(result);
//			item.setTranscript(result);
//			ListeningDBHelper.updateTranscript(item);
//		}
//		
//		@Override
//		protected void onPreExecute()
//		{
//			super.onPreExecute();
//			loadingBar.setVisibility(View.VISIBLE);
//		}
//	}
	
	static class ViewHolder {
		TextView titleTextView;
		TextView transTextView;
		ImageView coverImgView;

	}

}
