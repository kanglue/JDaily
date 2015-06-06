package com.ianglei.jdaily.fragment;

import java.io.BufferedOutputStream;
import java.io.File;
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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.ianglei.jdaily.R;
import com.ianglei.jdaily.db.ListeningDBHelper;
import com.ianglei.jdaily.model.ListeningItem;
import com.ianglei.jdaily.player.PlayMusicService;
import com.ianglei.jdaily.util.JUtils;
import com.ianglei.jdaily.util.StringUtils;

public class DetailActivity extends Activity implements OnClickListener,
OnSeekBarChangeListener
{
	private static final String TAG = "DetailActivity";
	
	private String transcript;
	private TextView transcriptTextView;
	private LinearLayout loadingBar; 
	private ProgressBar downloadBar;
	private RelativeLayout playerLayout;
	private Button playButton;
//	private ImageButton pauseBtn;
//	private ImageButton stopBtn;
	public static String mp3PathToPlay;
    /* 定义进度条 */  
    public static SeekBar seekBar = null;
    
	//private View bar;   //转圈圈的iew
    
    private boolean isPlay;  //是否在播放
    private TextView playTime;  //当前播放时间
	private TextView allTime;  //总的播放时间
	private static int nowBarSize;  //当前seekbar进度
	private static int seekBarSize;  //seekbar的总大小
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		

		
		Intent intent = getIntent();
		ListeningItem item = (ListeningItem)intent.getParcelableExtra("currentpos");
		
		init(item);
		
		if(transcript == null)
		{
			TranscriptTask task = new TranscriptTask();
			task.execute(item);
		}
		else {
			loadingBar.setVisibility(View.GONE);
			transcriptTextView.setText(transcript);
		}
		
		boolean isExist = new File(item.getMp3path()).exists();
		
		if(null == item.getMp3path() || item.getMp3path().startsWith("http") || !isExist)
		{
			Mp3Task task = new Mp3Task();
			task.execute(item);
		}
		else {
			playerLayout.setVisibility(View.VISIBLE);
			startPlay(item);
		}
		
		listererTelephony(); //来电话监听
	}
	
	private void init(ListeningItem item) {
		
		
		TextView titleTextView = (TextView)findViewById(R.id.ItemTitle);
		transcriptTextView = (TextView)findViewById(R.id.Transcript);
		ImageView coverImgView = (ImageView)findViewById(R.id.coverimg);
		loadingBar = (LinearLayout)findViewById(R.id.pb);
		downloadBar = (ProgressBar)findViewById(R.id.download_progress);
				
		playerLayout = (RelativeLayout)findViewById(R.id.player);
		playerLayout.setVisibility(View.GONE);
		playButton = (Button)findViewById(R.id.play);
		playButton.setOnClickListener(this);
		
		playTime = (TextView) findViewById(R.id.play_time);
		allTime = (TextView) findViewById(R.id.all_time);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setEnabled(false);

		//bar = findViewById(R.id.pb);
		PlayMusicService.detailActivity = this;
		
		//从本地加载图片
		Bitmap bitmap = BitmapFactory.decodeFile(item.getCoverpath());
		coverImgView.setImageBitmap(bitmap);
		
		titleTextView.setText(item.getTitle());
		
		//从数据库中查询脚本
		ListeningDBHelper.queryDetailById(item);
		transcript = item.getTranscript();
		
		
	}
	
	private void listererTelephony() {
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(new MobliePhoneStateListener(),
				PhoneStateListener.LISTEN_CALL_STATE);
	}
	private class MobliePhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				PlayMusicService.start();
				isPlay = true;
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				PlayMusicService.pause();
				isPlay = false;
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				PlayMusicService.pause();
				isPlay = false;
				break;
			default:
				break;
			}
		}
	}
	
	class Mp3Task extends AsyncTask<ListeningItem, Integer, String>
	{
		ListeningItem item;
		
		@Override
		protected String doInBackground(ListeningItem... params)
		{
			Document doc;
			String localPath = null;
			String localMp3Path = null;
			
			try
			{
				item = params[0];
				doc = Jsoup.connect(params[0].getLink()).timeout(45000).get();

				//MP3 and pdf has no fixed order
				Element aElement1 = doc.select("a.download").get(0);
				Element aElement2 = doc.select("a.download").get(1);
				
				String[] urls = new String[2];
				
				urls[1] = aElement1.attr("href");
				urls[2] = aElement2.attr("href");
								
				for(String s : urls)
				{
				    localPath = JUtils.getResourcePath(s);
				    if(localPath.endsWith("mp3"))
				    {
				    	localMp3Path = localPath;
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
			playerLayout.setVisibility(View.VISIBLE);
			
			item.setMp3path(result);
			ListeningDBHelper.updateMp3Path(item);
			
			startPlay(item);
		}
	}
	
	private void startPlay(ListeningItem item) {

			PlayMusicService.stop();//播放之前，先停止播放，不管有没有在播放，都先停止
			intent = new Intent();
			intent.putExtra("name", item.getTitle());
			intent.putExtra("music", item.getMp3path());
			intent.setClass(this, PlayMusicService.class); //启动service，播放音乐
			startService(intent);

	}
	
	public void updateSeekBar(int size) {   //更新seekbar的方法
		seekBar.setProgress(size);
	}
	public void updateSeekCach(int size) { //更新seekbar第2进度条的方法
		seekBar.setSecondaryProgress(size);
	}
	
	public void setIsplay(boolean isPlay) {  //改变当前播放状态数据标志
		if (isPlay) {
			this.isPlay = true;
		} else {
			this.isPlay = false;
		}
	}
	public void refreshUI(int size) {     //音乐初始化 后，调用这里
		allTime.setText(StringUtils.generateTime(size));  //显示播放总时间
		playButton.setBackgroundResource(R.drawable.pause);//设置播放按钮的背景
		seekBar.setMax(size); //设置seekbar的总大小
		seekBar.setEnabled(true); //可以拖动seekbar了
	}
	public void updateTime(int nowPlayTime) { //更新播放时间
		playTime.setText(StringUtils.generateTime(nowPlayTime));
	}
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.play) {     //播放按钮
			play();
		} 
//		else if (v.getId() == R.id.next) { //下一去
//			PlayMusicService.playNextMusic();
//			id += 1;
//			if (id >= beans.size()) {
//				id = 0;
//			}
//			setName(id);
//		} else if (v.getId() == R.id.font) {//上一曲
//			id -= 1;
//			PlayMusicService.playFontMusic();
//			if (id < 0) {
//				id = beans.size() - 1;
//			}
//			setName(id);
		
	}

	private void play() {
		if (isPlay) {
			seekBar.setEnabled(false);
			playButton.setBackgroundResource(R.drawable.play);
		} else {
			seekBar.setEnabled(true);
			playButton.setBackgroundResource(R.drawable.pause);
		}
		PlayMusicService.play();
	}
	
	public void hideBar() { //隐藏圈圈
		loadingBar.setVisibility(View.GONE);
	}
	public void showBar() {//显示圈圈
		loadingBar.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!isPlay && intent != null) {
			stopService(intent);
		}
		if (PlayMusicService.isPause) {
			PlayMusicService.stop();
		}
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {   //拖动seekbar时候，调用这里

		if (fromUser) {
			PlayMusicService.start();
			PlayMusicService.seekTo(progress);  //播放跳转到拖动位置
		}
	}
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { //开始拖动seekbar
		PlayMusicService.pause();
		nowBarSize = seekBar.getProgress();
	}
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { //拖动seekbar结束
		seekBarSize = seekBar.getProgress();
		if (nowBarSize >= seekBarSize) {
			//PlayMusicService.setFlag();
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
			playerLayout.setVisibility(View.VISIBLE);
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
