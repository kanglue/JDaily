package com.ianglei.jdaily.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
import com.ianglei.jdaily.NumberProgressBar;
import com.ianglei.jdaily.R;
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
	private UpdateCallback updateCallback;
	//private Activity activity = null;
	
	public UpdateCallback getUpdateCallback() {
		return updateCallback;
	}

	public void setUpdateCallback(UpdateCallback updateCallback) {
		this.updateCallback = updateCallback;
	}

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
			viewHolder.bnp = (NumberProgressBar)convertView.findViewById(R.id.numberbar);
			
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

	        	downloadedList.put(position, position);
				ListeningItem item = lists.get(position);

	        	updateCallback.startProgress(item, position);
	        }
        });
        
		ListeningItem item = lists.get(position);
		viewHolder.titleTextView.setText(item.getTitle());
		viewHolder.descTextView.setText(item.getDescribe());
		
		if(downloadedList.get(position) > 0)
		{
			viewHolder.downButton.setVisibility(View.GONE);
			viewHolder.bnp.setVisibility(View.VISIBLE);
		}
		else if(downloadedList.get(position) == 0)
		{
			viewHolder.downButton.setVisibility(View.VISIBLE);
			viewHolder.bnp.setVisibility(View.GONE);
		}
		
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
				Log.i(TAG, "Title: " + item.getTitle() + " NetCover: " + item.getCoverpath());
				loader.showImageAsyn(viewHolder.coverImgView, item.getCoverpath(), R.drawable.pic_default);
				String localPath = JUtils.getResourcePath(item.getCoverpath());
				item.setCoverpath(localPath);
			}
			else {
				//从本地加载
				//String localPath = JUtils.getResourcePath(item.getCoverpath());
				Log.i(TAG, "Title: " + item.getTitle() + " LocalCover: " + item.getCoverpath());
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
		NumberProgressBar bnp;
		RelativeLayout listitem;
	}
	
	
}
