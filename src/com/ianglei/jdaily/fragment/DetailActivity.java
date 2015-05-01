package com.ianglei.jdaily.fragment;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ianglei.jdaily.R;
import com.ianglei.jdaily.db.ListeningDBHelper;
import com.ianglei.jdaily.model.ListeningItem;
import com.ianglei.jdaily.player.PlayerConstants;
import com.ianglei.jdaily.player.PlayerService;
import com.ianglei.jdaily.util.JUtils;

public class DetailActivity extends Activity
{
	private static final String TAG = "DetailActivity";
	
	private String transcript;
	private TextView transcriptTextView;
	private ProgressBar loadingBar; 
	private ProgressBar downloadBar;
	private LinearLayout layout;
	private ImageButton playBtn;
	private ImageButton pauseBtn;
	private ImageButton stopBtn;
	public static String mp3PathToPlay;
	/* 声明音量管理器 */  
    private AudioManager audioManager = null;
    /* 定义进度条 */  
    public static SeekBar seekBar = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		Intent intent = getIntent();
		ListeningItem item = (ListeningItem)intent.getParcelableExtra("currentpos");
		
		TextView titleTextView = (TextView)findViewById(R.id.ItemTitle);
		transcriptTextView = (TextView)findViewById(R.id.Transcript);
		ImageView coverImgView = (ImageView)findViewById(R.id.coverimg);
		loadingBar = (ProgressBar)findViewById(R.id.ProgressBar);
		downloadBar = (ProgressBar)findViewById(R.id.download_progress);
		
		layout = (LinearLayout)findViewById(R.id.player);
		playBtn = (ImageButton)findViewById(R.id.play);
		pauseBtn = (ImageButton)findViewById(R.id.pause);
		stopBtn = (ImageButton)findViewById(R.id.stop);
		
		Bitmap bitmap = BitmapFactory.decodeFile(item.getCoverpath());
		coverImgView.setImageBitmap(bitmap);
		
		titleTextView.setText(item.getTitle());
		
		ListeningDBHelper.queryDetailById(item);
		transcript = item.getTranscript();
		
		if(transcript == null)
		{
			TranscriptTask task = new TranscriptTask();
			task.execute(item);
		}
		else {
			transcriptTextView.setText(transcript);
		}
		
		if(null == item.getMp3path())
		{
			Mp3Task task = new Mp3Task();
			task.execute(item);
		}
		else {
			
			layout.setVisibility(View.VISIBLE);
			PreparetoPlay();
		}
		

	}
	
	public void playMusic(int action) {  
        Intent intent = new Intent();  
        intent.putExtra("MSG", action);
        intent.putExtra("PATH", mp3PathToPlay);
        intent.setClass(DetailActivity.this, PlayerService.class);  
          
        /* 启动service service要在AndroidManifest.xml注册如：<service></service>*/  
          
        startService(intent);  
    }
	
	/* 拖放进度监听 ，别忘了Service里面还有个进度条刷新*/  
	class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {  
	  
	    @Override  
	    public void onProgressChanged(SeekBar seekBar, int progress,  
	            boolean fromUser) {  
	        /*假设改变源于用户拖动*/  
	        if (fromUser) {  
	            PlayerService.mMediaPlayer.seekTo(progress);// 当进度条的值改变时，音乐播放器从新的位置开始播放  
	        }  
	    }  
	  
	    @Override  
	    public void onStartTrackingTouch(SeekBar seekBar) {  
	        PlayerService.mMediaPlayer.pause(); // 开始拖动进度条时，音乐暂停播放  
	    }  
	  
	    @Override  
	    public void onStopTrackingTouch(SeekBar seekBar) {  
	        PlayerService.mMediaPlayer.start(); // 停止拖动进度条时，音乐开始播放  
	    }  
	}  
	
	private void PreparetoPlay()
	{
		playBtn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v)
			{
				playMusic(PlayerConstants.PAUSE);  
				
			}
		});
		
		stopBtn.setOnClickListener(new OnClickListener() 
		{  
	        
	        @Override  
	        public void onClick(View v) {  
	            Intent intent = new Intent();  
	            intent.setClass(DetailActivity.this, PlayerService.class);  
	            stopService(intent);//停止Service  
	            try {  
	                DetailActivity.this.finish();//关闭当前Activity  
	            } catch (Throwable e) {  
	                e.printStackTrace();  
	            }  
	          
	              
	        }  
	    });  
	
		/* 播放进度监听 */  
	    seekBar.setOnSeekBarChangeListener(new SeekBarChangeEvent());  
	    /*退出后再次进去程序时，进度条保持持续更新*/  
	    if(PlayerService.mMediaPlayer!=null){  
	        //设置进度条最大值  
	        DetailActivity.seekBar.setMax(PlayerService.mMediaPlayer.getDuration());  
	        seekBar.setProgress(PlayerService.mMediaPlayer.getCurrentPosition());  
	    }  
	}

	
	class Mp3Task extends AsyncTask<ListeningItem, Integer, String>
	{
		ListeningItem item;
		
		@Override
		protected String doInBackground(ListeningItem... params)
		{
			Document doc;
			String localMp3Path;
			try
			{
				item = params[0];
				doc = Jsoup.connect(params[0].getLink()).timeout(45000).get();

				Element mp3Element = doc.select("a.download").get(0);
				String mp3url = mp3Element.attr("href");
				
				localMp3Path = JUtils.getResourcePath(mp3url);
				
				try
				{
					URL url = new URL(mp3url);
					HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setRequestMethod("GET");
					//urlConnection.setDoOutput(true);
					urlConnection.connect();
					
					BufferedOutputStream fileOutput = new BufferedOutputStream(new FileOutputStream(localMp3Path));
					
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
				
				item.setMp3path(localMp3Path);
				mp3PathToPlay = localMp3Path;
				
				
				return localMp3Path;
				
			} catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			downloadBar.setProgress(values[0].intValue());
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			downloadBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			downloadBar.setVisibility(View.GONE);
			layout.setVisibility(View.VISIBLE);
			
			PreparetoPlay();
		}
	}
	
		
	
	
	
	
	class TranscriptTask extends AsyncTask<ListeningItem, Integer, String>
	{
		ListeningItem item;
		
		@Override
		protected String doInBackground(ListeningItem... params)
		{
			item = params[0];
			Document doc;
			try
			{
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
				return transcript;
			} catch (IOException e)
			{
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(String result)
		{
			super.onPostExecute(result);
			loadingBar.setVisibility(View.GONE);
			transcriptTextView.setText(result);
			item.setTranscript(result);
			ListeningDBHelper.updateTranscript(item);
		}
		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			loadingBar.setVisibility(View.VISIBLE);
		}
	}
	
	
}
