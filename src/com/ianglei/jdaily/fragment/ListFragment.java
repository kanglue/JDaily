package com.ianglei.jdaily.fragment;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
import android.widget.ProgressBar;

import com.ianglei.jdaily.R;
import com.ianglei.jdaily.db.ListeningDBHelper;
import com.ianglei.jdaily.http.HttpAgent;
import com.ianglei.jdaily.model.ListeningItem;
import com.ianglei.jdaily.rss.RSSBBC6minParser;
import com.ianglei.jdaily.xlist.XListView;
import com.ianglei.jdaily.xlist.XListView.IXListViewListener;

public class ListFragment extends Fragment implements IXListViewListener
{

	private static final String TAG = "ListFragment";
	private XListView listView;
	private ListAdapter listAdapter;
	private Handler mHandler;
	private ProgressBar loadingBar; 
	
	private int startPos = 0;
	private final int step = 10;
	
	ArrayList<ListeningItem> list = new ArrayList<ListeningItem>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View contextView = inflater.inflate(R.layout.fragment_list, container,
				false);
		listView = (XListView) contextView
				.findViewById(R.id.xListView);
		listView.setPullLoadEnable(true);
		listView.setXListViewListener(this);
		
		mHandler = new Handler();
		listAdapter = new ListAdapter(getActivity(), listView, handler);
		listView.setAdapter(listAdapter);
		
		loadingBar = (ProgressBar)contextView.findViewById(R.id.ProgressBar);
				
		Bundle mBundle = getArguments();
		String title = mBundle.getString("arg");

		if (title.equalsIgnoreCase("6 Minute English"))
		{
			ArrayList<ListeningItem> list = ListeningDBHelper.getItemListByCategory(6, startPos);
			startPos += step;

			if (list.size() == 0)
			{
				RssTask task = new RssTask();
				task.execute(new Integer(0));
			} else
			{
				listAdapter.addItem(list);
			}
		}
		
		return contextView;
	}
	

	
    private Handler handler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if (msg.what == 1 && msg.obj != null)
            {
            	//Click to go detail
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("currentpos", (Parcelable)msg.obj);
                getActivity().startActivity(intent);
            }
        }
    };

    /**
     * Get data from BBC
     * @author jl
     *
     */
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
				
				//概要描述
				int itemcount = doc.select("div.details > p").size();
				
				for(int k = 0; k < itemcount; k++)
				{
					//Episode 150212
					Element dateElement = doc.select("div.details > h3 > b").get(k);
					String dateText = dateElement.text();
					Log.d(TAG, "Date: " + dateText);
					
					//主题					
					Element ahrefElement = doc.select("h2 > a[href*=/6-minute-english/]").get(k);
					String titleText = ahrefElement.text();
					Log.d(TAG, "Title: " + titleText);
					
					//拼接绝对地址 learningenglish/english/features/6-minute-english/ep-150205
					String urlText = "http://www.bbc.co.uk" + ahrefElement.attr("href");
					Log.d(TAG, "URL: " + urlText);
					
					//图片
					Element jpgElement = doc.select("img[data-pid]").get(k);
					String jpgURL = jpgElement.attr("src");
					Log.d(TAG, "JPG: " + jpgURL);
										
					//图片pid作为唯一标识
					String pid = jpgElement.attr("data-pid");
					Log.d(TAG, "Pid: " + pid);
					
					//概要描述
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
					
					list.add(item);
				}
				
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			new Thread(insertRunnable).start();

			return list;
		}
		
		@Override
		protected void onPostExecute(ArrayList<ListeningItem> result)
		{
			listAdapter.addItem(result);
			//loadingBar.setVisibility(View.GONE);
			onLoad();
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			//loadingBar.setVisibility(View.VISIBLE);
		}

	}
	
	/**
	 * insert to db
	 */
	Runnable insertRunnable = new Runnable() {		
		@Override
		public void run()
		{
			if(list.size() > 0)
			{
				for(ListeningItem item : list)
				{
					if(!ListeningDBHelper.isListeningExist(item.getId()))
					{
						ListeningDBHelper.insertListeningInfo(item);
						
						// TODO should check picpath start with http or not
						//Download pic for new insert
						DownPicTask task = new DownPicTask();
						task.execute(item);
					}
					else {
						Log.i(TAG, item.getId() + " has already exist.");
					}
				}
			}			
		}
	};
	
	class DownPicTask extends AsyncTask<ListeningItem, Integer, String>
	{
		String result;
		ListeningItem item;
		
		@Override
		protected String doInBackground(ListeningItem... params)
		{
			item = params[0];
			result = HttpAgent.downloadResource(params[0].getCoverpath());
			
			return result;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			item.setCoverpath(result);
			ListeningDBHelper.updateCoverImgPath(item);
		}
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
	}

	
	private void onLoad() {
		listView.stopRefresh();
		listView.stopLoadMore();
		listView.setRefreshTime("刚刚");
	}

	@Override
	public void onRefresh() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				RssTask task = new RssTask();
				task.execute(new Integer(0));
				onLoad();
			}
		}, 1000);
	}


	@Override
	public void onLoadMore() {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				
				list.addAll(ListeningDBHelper.getItemListByCategory(6, startPos));
				
				listAdapter.notifyDataSetChanged();
				onLoad();
				
				if(ListeningDBHelper.getCountByCategory(6) == list.size())
				{
					listView.setPullLoadEnable(false);
				}
				else {
					listView.setPullLoadEnable(true);
				}
			}
		}, 1000);
	}

}
