package com.ianglei.jdaily.fragment;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

import com.ianglei.jdaily.NumberProgressBar;
import com.ianglei.jdaily.R;
import com.ianglei.jdaily.db.ListeningDBHelper;
import com.ianglei.jdaily.fragment.ListAdapter.ViewHolder;
import com.ianglei.jdaily.http.HttpAgent;
import com.ianglei.jdaily.model.ListeningItem;
import com.ianglei.jdaily.rss.RSSBBC6minParser;
import com.ianglei.jdaily.util.JUtils;
import com.ianglei.jdaily.xlist.XListView;
import com.ianglei.jdaily.xlist.XListView.IXListViewListener;

public class ListFragment extends Fragment implements IXListViewListener, UpdateCallback
{

	private static final String TAG = "ListFragment";
	private XListView listView;
	private ListAdapter listAdapter;
	private Handler mHandler;
	private ProgressBar loadingBar; 
	
	private int startPos = 0;
	private final int step = 10;
	
	//临时存放，完整的队列在adapter中存放
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
		listAdapter.setUpdateCallback(this);
		
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
				listAdapter.addList(list);
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
					//图片
					Element jpgElement = doc.select("img[data-pid][width=624]").get(k);
					String jpgURL = jpgElement.attr("src");
					Log.i(TAG, "JPG: " + jpgURL);
					
					//图片pid作为唯一标识
					String pid = jpgElement.attr("data-pid");
					Log.i(TAG, "Pid: " + pid);
					
					if(!ListeningDBHelper.isListeningExist(pid))
					{
						//Episode 150212
						Element dateElement = doc.select("div.details > h3 > b").get(k);
						String dateText = dateElement.text();
						Log.i(TAG, "Date: " + dateText);
						
						//主题					
						Element ahrefElement = doc.select("h2 > a[href*=/6-minute-english/]").get(k);
						String titleText = ahrefElement.text();
						Log.i(TAG, "Title: " + titleText);
						
						//拼接绝对地址 learningenglish/english/features/6-minute-english/ep-150205
						String urlText = "http://www.bbc.co.uk" + ahrefElement.attr("href");
						Log.i(TAG, "URL: " + urlText);
						
						//概要描述
						Element descElement = doc.select("div.details > p").get(k);
						String descText = descElement.text();
						Log.i(TAG, "Desc: " + descText);
												
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
			Log.i(TAG, "Get rss count is " + result.size());
			listAdapter.addList(result);
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
			Log.i(TAG, "Title:" + item.getTitle() + " Image:" + params[0]);
			
			return result;
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			item.setCoverpath(result);
			Log.i(TAG, "Title:" + item.getTitle() + " LocalImage:" + result);
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
				list.clear();
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
				
				if(ListeningDBHelper.getCountByCategory(6) == listAdapter.getCount())
				{
					listView.setPullLoadEnable(false);
				}
				else {
					
					listAdapter.addList(ListeningDBHelper.getItemListByCategory(6, startPos));
					startPos += step;
					listAdapter.notifyDataSetChanged();
					onLoad();
					
					listView.setPullLoadEnable(true);
				}
			}
		}, 1000);
	}

	
	class Mp3Task extends AsyncTask<ListeningItem, Integer, ListeningItem>
	{
		ListeningItem item;
		NumberProgressBar bnp;
		
		public Mp3Task(NumberProgressBar bnp)
		{
			this.bnp = bnp;
		}
		
		@Override
		protected ListeningItem doInBackground(ListeningItem... params)
		{
			Document doc;
			String localPath = null;
			String localMp3Path = null;
			String localPdfPath = null;
			String transcript = null;
			
			try
			{
				item = params[0];
				doc = Jsoup.connect(params[0].getLink()).timeout(45000).get();

				//MP3 and pdf has no fixed order
				Element aElement1 = doc.select("a.download").get(0);
				Element aElement2 = doc.select("a.download").get(1);
				
				String[] urls = new String[2];
				
				urls[0] = aElement1.attr("href");
				urls[1] = aElement2.attr("href");
								
				for(String s : urls)
				{
				    localPath = JUtils.getResourcePath(s);
				    if(localPath.endsWith("mp3"))
				    {
				    	localMp3Path = localPath;
				    }
				    else if(localPath.endsWith("pdf")){
				    	localPdfPath = localPath;
					}
				    	try
						{
							URL url = new URL(s);
							HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
							urlConnection.setRequestMethod("GET");
							//urlConnection.setDoOutput(true);
							urlConnection.connect();
							
							BufferedOutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(localPath));
							
							InputStream inputStream = urlConnection.getInputStream();
							
							int totalSize = urlConnection.getContentLength();
		
				    		//variable to store total downloaded bytes
				    		int downloadedSize = 0;
		
				    		//create a buffer...
				    		byte[] buffer = new byte[1024000];
				    		int bufferLength = 0; //used to store a temporary size of the buffer
		
				    		//now, read through the input buffer and write the contents to the file
				    		while ( (bufferLength = inputStream.read(buffer)) != -1 ) {
				    			//add the data in the buffer to the file in the file output stream (the file on the sd card
				    			fileOutput.write(buffer, 0, bufferLength);
				    			//add up the size so we know how much is downloaded
				    			downloadedSize += bufferLength;
				    			Log.i(TAG, "download "+downloadedSize+" of "+totalSize);
		
				    			publishProgress(Integer.valueOf(100*downloadedSize/totalSize));
				    			
				    		}
				    		Log.i(TAG, "download "+downloadedSize+" of "+totalSize);
				    		//close the output stream when done
				    		fileOutput.close();
				    		urlConnection.disconnect();    		  		
							
						} catch (MalformedURLException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}		
				    }

				
				try {

					//Download transcript
					doc = Jsoup.connect(item.getLink()).timeout(45000).get();
					int pcount = doc.select("div.text p, div.text br").size();
					
					StringBuilder builder = new StringBuilder(2048);
								
					for(int i = 0; i < pcount; i++)
					{
						Element dateElement = doc.select("div.text p, div.text br").get(i);
						String dateText = dateElement.text();
						builder.append(dateText);
						builder.append("\n");
					}
					
					transcript = builder.toString();
					
					item.setTranscript(transcript);
					item.setMp3path(localMp3Path);
					item.setPdfpath(localPdfPath);
					
				} catch (IOException e)
				{
					e.printStackTrace();
					return null;
				}
				
				return item;
				
			} catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			bnp.setProgress(values[0].intValue());
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			bnp.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(ListeningItem result)
		{
			super.onPostExecute(result);
			bnp.setVisibility(View.GONE);			

			ListeningDBHelper.updateInfoPath(item);

		}
	}


	@Override
	public void startProgress(ListeningItem item, int position) {
		int firstVisiblePosition = listView.getFirstVisiblePosition();
		int lastVisiblePosition = listView.getLastVisiblePosition();
		if(position >= firstVisiblePosition && position <= lastVisiblePosition)
		{
			View view = listView.getChildAt(position - firstVisiblePosition);//屏幕上看起来的第几个
			if(view.getTag() instanceof ViewHolder)
			{
				ViewHolder viewHolder = (ViewHolder)view.getTag();
		    	viewHolder.downButton.setVisibility(View.GONE);
		    	viewHolder.bnp.setVisibility(View.VISIBLE);
		    	listAdapter.notifyDataSetChanged();
				
				Mp3Task task = new Mp3Task(viewHolder.bnp);
				task.execute(item);
			}
		}
	}
}
