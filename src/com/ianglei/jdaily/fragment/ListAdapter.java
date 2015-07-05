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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ianglei.jdaily.ImageLoader;
import com.ianglei.jdaily.NumberCircleProgressBar;
import com.ianglei.jdaily.R;
import com.ianglei.jdaily.db.ListeningDBHelper;
import com.ianglei.jdaily.model.ListeningItem;
import com.ianglei.jdaily.pic.AsynImageLoader;
import com.ianglei.jdaily.util.DealSecondClickUtil;
import com.ianglei.jdaily.util.JUtils;
import com.ianglei.jdaily.xlist.XListView;

public class ListAdapter extends BaseAdapter
{
	private LayoutInflater mInflater;
	private ArrayList<ListeningItem> lists = new ArrayList<ListeningItem>();
	private SparseIntArray downloadedList = new SparseIntArray();
	private XListView listView;
	private static final String TAG = "ListAdapter";
	private ImageLoader imageLoader;
	private Context context;
	private Handler handler;
	private ViewHolder viewHolder = null;
	//private Activity activity = null;
	
	public ListAdapter(Context context, XListView listView, Handler handler) {
		//mInflater = LayoutInflater.from(context);
		this.listView = listView;
		this.context = context;
		this.handler = handler;
		//this.activity = (Activity)context;
	}
	
	public void addItem(String id, String title, String updated, String describe, String link, int learnedtimes)
	{
		ListeningItem item = new ListeningItem();
		item.setId(id);
		item.setTitle(title);
		item.setUpdated(updated);
		item.setDescribe(describe);
		item.setLink(link);
		item.setLearnedtimes(learnedtimes);
		lists.add(item);
		notifyDataSetChanged();
	}
	
	public void addList(ArrayList<ListeningItem> list)
	{
		this.lists.addAll(list);
		notifyDataSetChanged();
	}
	
    public void clear() {
        lists.clear();
        notifyDataSetChanged();
    }
	
	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return lists.size();
	}
	@Override
	public Object getItem(int position)
	{
		if (position >= getCount()) {
			return null;
		}
		return lists.get(position);
	}
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	class Mp3Task extends AsyncTask<ListeningItem, Integer, ListeningItem>
	{
		ListeningItem item;
		NumberCircleProgressBar bnp;
		
		public Mp3Task(NumberCircleProgressBar bnp)
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
			notifyDataSetChanged();

			ListeningDBHelper.updateInfoPath(item);

		}
	}
		
	
	
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.item_adapter, null);
			viewHolder = new ViewHolder();
			viewHolder.titleTextView = (TextView) convertView
					.findViewById(R.id.ItemTitle);
			viewHolder.coverImgView = (ImageView) convertView
					.findViewById(R.id.coverimg);
			viewHolder.descTextView = (TextView) convertView.findViewById(R.id.ItemDesc);
			viewHolder.listitem = (RelativeLayout)convertView.findViewById(R.id.listitem);
			viewHolder.downButton = (ImageView)convertView.findViewById(R.id.DownPlay);
			viewHolder.bnp = (NumberCircleProgressBar)convertView.findViewById(R.id.numbercircleprogress_bar);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
        viewHolder.listitem.setBackgroundColor(position % 2 == 0 ? convertView.getContext()
                .getResources()
                .getColor(R.color.white) : convertView.getContext().getResources().getColor(R.color.item_bg));
        
        viewHolder.downButton.setOnClickListener(new OnClickListener(){
	        @Override  
	        public void onClick(View v) {  
	        	viewHolder.downButton.setVisibility(View.GONE);
	        	viewHolder.bnp.setVisibility(View.VISIBLE);
	        	downloadedList.put(position, position);
	        	
				ListeningItem item = lists.get(position);
				
				Mp3Task task = new Mp3Task(viewHolder.bnp);
				task.execute(item);
	        }
        });
        
		ListeningItem item = lists.get(position);
		viewHolder.titleTextView.setText(item.getTitle());
		viewHolder.descTextView.setText(item.getDescribe());
		
		if(item.getMp3path() != null)
		{
			if(item.getMp3path().startsWith("http"))
			{
				viewHolder.downButton.setImageResource(R.drawable.download);
			}
			else {
				viewHolder.downButton.setVisibility(View.GONE);
			}
		}
		else
		{
			
		}

		AsynImageLoader loader = new AsynImageLoader();
		
		if(item.getCoverpath() == null)
		{
			//loader.showImageAsyn(viewHolder.coverImgView, item.getCoverpath(), R.drawable.pic_default);
			Log.i(TAG, "Cover image is null");
		}
		else { 
			if(item.getCoverpath().startsWith("http"))
			{
				loader.showImageAsyn(viewHolder.coverImgView, item.getCoverpath(), R.drawable.pic_default);
				String localPath = JUtils.getResourcePath(item.getCoverpath());
				item.setCoverpath(localPath);
			}
			else {
				//从本地加载
				//String localPath = JUtils.getResourcePath(item.getCoverpath());
				Bitmap bitmap = BitmapFactory.decodeFile(item.getCoverpath());
				viewHolder.coverImgView.setImageBitmap(bitmap);
			}
		}
		
		
		final int pos = position;
        viewHolder.listitem.setOnClickListener(new OnClickListener()
        {
            
            @Override
            public void onClick(View v)
            {
                if (DealSecondClickUtil.isFastDoubleClick(800))
                {
                    return;
                }
                
                if (lists != null && lists.size() > pos)
                {
                    Message message = Message.obtain();
                    message.what = 1;
                    message.obj = (ListeningItem)lists.get(pos);
                    handler.sendMessage(message);
                }
            }
        });
		
		return convertView;
	}
	
	static class ViewHolder {
		TextView titleTextView;
		TextView descTextView;
		ImageView coverImgView;
		ImageView downButton;
		NumberCircleProgressBar bnp;
		RelativeLayout listitem;
	}
	
	
}
