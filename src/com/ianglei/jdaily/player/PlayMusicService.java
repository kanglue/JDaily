package com.ianglei.jdaily.player;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.ianglei.jdaily.fragment.DetailActivity;
import com.ianglei.jdaily.model.ListeningItem;
import com.ianglei.jdaily.util.NotiFicationUtil;
/**
 * 播放音乐的service
 * @author hck
 *
 */
public class PlayMusicService extends Service implements
		OnBufferingUpdateListener, OnCompletionListener, OnPreparedListener,
		OnInfoListener, OnErrorListener {
	public static DetailActivity detailActivity;   //播放界面activity对象，用于调用实现更新UI
	private static boolean isPlay;   //判断是否在播放中的标记
	private static MediaPlayer player;   //MediaPlayer对象，播放音乐
	private static updateBar updateBar;  //更新seekbar的一个线程对象
	//private static updateSingWords updateWords;  //更新歌词的一个线程
	private static long wordTime;   //当前播放时间，更新歌词用
	private static int flag = 0; 
	public static boolean isPause;   //是否暂停标志
	private static PlayMusicService playMusicService;  //当前类对象，用于播放界面调用
	private static Handler handler2 = new Handler();  //更新歌词的handler
	public static ListeningItem item;   //存放歌曲的集合
	public static int playMusicId;   //音乐播放id
	private static String playTitle;
	private static String playUrl;   //音乐的播放url
	private static String type;    //是本地还是在线音乐标记
    private boolean isRandom;   //是否随机播放
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();
		player = new MediaPlayer();   //初始化MediaPlayer对象
		playMusicService = this;  //初始化playMusicServer对象
	}
	@Override
	public void onStart(Intent intent, int startId) {
		init(intent);  //初始化一些数据
		startPlay();  //开始播放视频
	}
	

	private void init(Intent intent) { //初始化数据，从intent获取播放界面传过来的数据
		playTitle = intent.getStringExtra("name");
		playUrl = intent.getStringExtra("music");  //获取传过来的type数据，用于判断是在线播放，还是本地本地播放。2这获取歌词不一样
		updateBar = new updateBar();   //初始化更新seekbar的线程
		
	}
	private static void startPlay() { //开始播放音乐
		try {
			player.reset();   //恢复原始状态
			player.setDataSource(playUrl);   //把播放地址丢给播放器
			player.prepareAsync();   //异步初始化
			player.setOnBufferingUpdateListener(playMusicService); //监听缓冲数据
			player.setOnPreparedListener(playMusicService);  //监听初始化
			player.setOnCompletionListener(playMusicService); //监听是否播放完了
			player.setOnErrorListener(playMusicService);   //监听出错信息
			player.setOnInfoListener(playMusicService);   //监听播放过程中，返回的信息

		} catch (Exception e) {
             Toast.makeText(playMusicService, "错误: "+e, Toast.LENGTH_LONG).show(); //出错时候，提示
             Log.e("hck", "PlayMusicserver startPlay: "+e.toString());
		}
	}
	static Handler handler = new Handler() {    //更新seekbar和播放时间的handler
		public void handleMessage(android.os.Message msg) {
			if (detailActivity != null && player != null && isPlay) { 
				detailActivity.updateSeekBar(player.getCurrentPosition());
				detailActivity.updateTime(player.getCurrentPosition());
			}
		};
	};

	class updateBar implements Runnable {  //更新seekbar和时间的线程
		@Override
		public void run() {
			Log.i("hck", "updateBar  run run");
			handler.sendEmptyMessage(1);
			handler.postDelayed(updateBar, 1000);
		}
	}


	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {  //播放过程中的一些返回信息
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:  //进入缓冲
			detailActivity.showBar();  //显示转圈圈
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:  //结束缓冲
			detailActivity.hideBar();  //隐藏圈圈
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {  //初始化播放回调方法
		player.start();  //初始化完成，调用start，开始播放音乐
		isPlay = true;
		isPause = false;
		refreshUI();   //更新PlayMusicActivity界面
		new Thread(updateBar).start();  //启动线程，更新seekbar
		
		showNotifi();  //在通知栏显示
	}
	public static void refreshUI() { 
		if (player != null && detailActivity != null) {
			detailActivity.refreshUI(player.getDuration());
			detailActivity.setIsplay(true);
		}
	}
	@Override
	public void onCompletion(MediaPlayer mp) { //一首播放完毕，继续下手

		playNextMusic();
	}
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {  //获取缓冲进度
		if (detailActivity != null && player != null) {
			detailActivity.updateSeekCach(mp.getDuration() * percent / 100);  //seekbar第2进度条
		}
	}
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) { //出错时候，会回调这里
		switch (what) {
		case MediaPlayer.MEDIA_ERROR_UNKNOWN :
			break;
		default:
			break;
		}
		return false;
	}

	public static void play() {  //播放
		if (player != null) {
			if (isPlay) { //如果在播放，则暂停
				player.pause();
				isPlay = false;
				isPause = true;
				detailActivity.setIsplay(false);
				hidenNotifi(); //应藏通知栏
             
			} else {  //播放
				if (player != null && !player.isPlaying()) {
					player.start();
					detailActivity.setIsplay(true);
					isPlay = true;
					isPause = false;
					showNotifi();
				}
			}
		}
	}

	public static void pause() { //暂停播放
		if (player != null) {
			player.pause();
			isPlay = false;
			isPause = true;
			if (detailActivity != null) {
				detailActivity.setIsplay(false);
			}
			hidenNotifi();
		}
	}

	public static void start() { //开始播放

		if (player != null && !player.isPlaying()) {
			player.start();
			isPlay = true;
			isPause = false;
			if (detailActivity != null) {
				detailActivity.setIsplay(true);
			}
			showNotifi();
		}
	}

	public static void seekTo(int size) {  //拖动时候，快进，快退

		if (player != null && isPlay) {
			player.seekTo(size);
		}
	}



	public static void playNextMusic() { //播放下一曲音乐
		if (isPlay) {
			player.stop();
		}
		reset();
		startPlay();
		showNotifi();
		//detailActivity.resetDate();
	}

	public static void playFontMusic() { //上一曲歌曲
		
		if (isPlay) {
			player.stop();
		}
		reset();
		startPlay();
		showNotifi();
		//detailActivity.resetDate();
	}

	private static void reset() { //移除相应线程
		isPlay = false;
		//handler2.removeCallbacks(updateWords);
		handler.removeCallbacks(updateBar);
		flag = 0;
	}

	private static void showNotifi() {  //通知栏显示
		if (playUrl != null) {
			NotiFicationUtil.showNotification(playMusicService, playTitle);
		} else {
			NotiFicationUtil.showNotification(playMusicService, playTitle);
		}
	}

	private static void hidenNotifi() {
		NotiFicationUtil.clearNotification(playMusicService);
	}

	@Override
	public void onDestroy() {  //销毁时候的一些数据清理
		super.onDestroy();

		if (isPause || !isPlay) {
			if (player != null) {
				player.stop();
				player.release();
				player = null;
				playMusicId = 0;
				handler.removeCallbacks(updateBar);
				this.stopSelf();
				flag = 0;
				wordTime = 0;
				playMusicId = 0;
			}
		}
	}

	public static void stop() { //停止了，移除线程，恢复一些初始数据
		if (player != null) {
			player.stop();
			player.release();
			player = null;
			handler.removeCallbacks(updateBar);
			flag = 0;
			wordTime = 0;
			playMusicService.stopSelf();
		}
	}

}
