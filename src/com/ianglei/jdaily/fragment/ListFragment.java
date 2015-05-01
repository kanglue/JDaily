package com.ianglei.jdaily.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.ianglei.jdaily.R;
import com.ianglei.jdaily.db.ListeningDBHelper;
import com.ianglei.jdaily.http.HttpAgent;
import com.ianglei.jdaily.model.ListeningItem;
import com.ianglei.jdaily.rss.RSSBBC6minParser;
import com.loopj.android.http.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;

public class ListFragment extends Fragment
{

	private static final String TAG = "ListFragment";
	
	ListAdapter listAdapter;
	
	private ProgressBar loadingBar; 
	
	ArrayList<ListeningItem> list = new ArrayList<ListeningItem>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
        AsyncHttpClient client = new AsyncHttpClient();

		View contextView = inflater.inflate(R.layout.fragment_list, container,
				false);
		ListView listView = (ListView) contextView
				.findViewById(R.id.ListeningList);
		
		listAdapter = new ListAdapter(getActivity(), listView, mHandler);
		listView.setAdapter(listAdapter);
		
		loadingBar = (ProgressBar)contextView.findViewById(R.id.ProgressBar);

		Bundle mBundle = getArguments();
		String title = mBundle.getString("arg");

		if (title.equalsIgnoreCase("6 Minute English"))
		{
			ArrayList<ListeningItem> list = ListeningDBHelper.getItemList(6);

			if (list.size() == 0)
			{
				// textView.setText("No Record");
				// textView.setVisibility(View.VISIBLE);
				RssTask task = new RssTask();
				task.execute(new Integer(0));
			} else
			{
				listAdapter.addItem(list);
			}

		}
		
		

		return contextView;
	}
	

	
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if (msg.what == 1 && msg.obj != null)
            {
                //fullScreenPlay((Channel)msg.obj);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("currentpos", (Parcelable)msg.obj);
                getActivity().startActivity(intent);
            }
        }
    };

	class RssTask extends AsyncTask<Integer, Integer, ArrayList<ListeningItem>>
	{
		@Override
		protected ArrayList<ListeningItem> doInBackground(Integer... params)
		{
			HttpAgent httpAgent = new HttpAgent();

			RSSBBC6minParser parser = new RSSBBC6minParser();

			String url = "http://www.bbc.co.uk/worldservice/learningenglish/general/sixminute/index.xml";
			String httpurl = "http://www.bbc.co.uk/learningenglish/english/features/6-minute-english";
			// List<ListeningItem> entryList =
			// parser.getRssItems(httpAgent.streampost(httpurl));
			// Log.d(TAG, "The count of entry is " + entryList.size());

			Document doc;

			try
			{
				doc = Jsoup.connect(httpurl).timeout(45000).get();
				
				int itemcount = doc.select("div.details > p").size();
				
				for(int k = 0; k < itemcount; k++)
				{
					//Episode 150212 / 12 Feb 2015
					Element dateElement = doc.select("div.details > h3").get(k);
					String dateText = dateElement.text();
					Log.d(TAG, "Date: " + dateText);
					
					///learningenglish/english/features/6-minute-english/ep-150205
					Element ahrefElement = doc.select("h2 > a[href*=/6-minute-english/]").get(k);
					String titleText = ahrefElement.text();
					Log.d(TAG, "Title: " + titleText);
					
					String urlText = "http://www.bbc.co.uk" + ahrefElement.attr("href");
					Log.d(TAG, "URL: " + urlText);
							
					
					Element jpgElement = doc.select("img[data-pid]").get(k);
					String jpgURL = jpgElement.attr("src");
					Log.d(TAG, "JPG: " + jpgURL);
					String pid = jpgElement.attr("data-pid");
					Log.d(TAG, "Pid: " + pid);
					
					Element descElement = doc.select("div.details > p").get(k);
					String descText = descElement.text();
					Log.d(TAG, "Desc: " + descText);
					
					
					ListeningItem item = new ListeningItem();
					item.setId(pid);
					item.setCoverpath(jpgURL);
					item.setCategory(6);
					item.setDescribe(descText);
					item.setLink(urlText);
					item.setLearnedtimes(0);
					item.setTitle(titleText);
					item.setUpdated(dateText);
					
					
					//DownPicTask task = new DownPicTask();
					//task.execute(item);

					list.add(item);
				}
				
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			new Thread(runnable).start();

			return list;
		}
		
		@Override
		protected void onPostExecute(ArrayList<ListeningItem> result)
		{
			listAdapter.addItem(result);
			loadingBar.setVisibility(View.GONE);
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			loadingBar.setVisibility(View.VISIBLE);
		}

	}
	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run()
		{
			if(list.size() > 0)
			{
				for(ListeningItem item : list)
				{
					ListeningDBHelper.insertListeningInfo(item);
				}
			}
			
		}
	};
	
//	class DownPicTask extends AsyncTask<ListeningItem, Integer, String>
//	{
//		String result;
//		ListeningItem item;
//		
//		@Override
//		protected String doInBackground(ListeningItem... params)
//		{
//			item = params[0];
//			result = HttpAgent.downloadResource(params[0].getCoverpath());
//			
//			return result;
//		}
//		
//		@Override
//		protected void onPostExecute(String result)
//		{
//			super.onPostExecute(result);
//			item.setCoverpath(result);
//			ListeningDBHelper.updateCoverImgPath(item);
//			
//			
//		}
//		
//	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

}
