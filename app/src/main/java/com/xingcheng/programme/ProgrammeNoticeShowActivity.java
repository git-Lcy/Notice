package com.xingcheng.programme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xingcheng.programme.imp.AlarmBroadcastReceiver;
import com.xingcheng.programme.module.Programme;
import com.xingcheng.programme.utils.Global;
import com.xingcheng.programme.utils.L;
import com.xingcheng.programme.utils.ThreadPoolManager;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgrammeNoticeShowActivity extends Activity{

    private TextView noticeInfo;
    private ImageView noticeBtn;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private PowerManager.WakeLock mWakelock;
    private StringBuilder mBuilder;


    @Override
    public void onRestart(){
        super.onRestart();
        L.e("ProgrammeNoticeShowActivity","--onRestart");
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        L.e("ProgrammeNoticeShowActivity","--onRestoreInstanceState");
    }

    @Override
    public void onStart(){
        super.onStart();
        L.e("ProgrammeNoticeShowActivity","--onStart");
    }
    @Override
    public void onPause(){
        super.onPause();
        L.e("ProgrammeNoticeShowActivity","--onPause");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.e("ProgrammeNoticeShowActivity","--onCreate");
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_programme_setting);

        String id = getIntent().getStringExtra("programmeId");

        noticeInfo = findViewById(R.id.notice_info);
        noticeBtn = findViewById(R.id.notice_btn);


        mBuilder = new StringBuilder();//显示错误日志用

        // 点击取消震动
        noticeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vibrator!=null) vibrator.cancel();
            }
        });


        if (TextUtils.isEmpty(id)) {
            L.e("ProgrammeNoticeShowActivity","Programme id = null");
            finish();
            return;
        };

        //数据库获取提醒信息
        final Programme p = DataSupport.where("programmeId = ?",id).findFirst(Programme.class);
        if (p==null) {
            L.e("ProgrammeNoticeShowActivity","Programme not found");
            finish();
            return;
        }
        L.e("ProgrammeNoticeShowActivity",p.toString());

        // 熄屏状态下唤醒屏幕
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        try {
            mWakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP, getClass().getName());
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return;
        }
        if (mWakelock!=null) mWakelock.acquire();

        String message = p.getMessage();
        noticeInfo.setText(message);
        // 震动
        if (p.isVibrate()){
            vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

            if (vibrator.hasVibrator()){
                long [] pattern = {400,800,400,800,400,800,400,800,400,800,400,800};   // 停止 开启 停止 开启
                vibrator.vibrate(pattern,0);
            }
        }


        final int repeat = p.getRepeatType();//重复

        //确定按钮
        noticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeat==1){ // 重复则设置第二天的闹钟
                    long executeTimer = Long.valueOf(p.getExecuteTime())+24*60*60*1000;//执行时间
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Date d = new Date(executeTimer);
                    String executeTime = String.valueOf(executeTimer);
                    String date = format.format(d);
                    p.setDate(date);
                    p.setExecuteTime(executeTime);

                    //添加闹钟
                    AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(ProgrammeNoticeShowActivity.this,AlarmBroadcastReceiver.class);
                    intent.putExtra("programmeId",p.getProgrammeId());
                    intent.putExtra("executeTime",executeTime);
                    PendingIntent pendingIntent = PendingIntent
                            .getBroadcast(ProgrammeNoticeShowActivity.this,p.getRequestCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    manager.setExact(AlarmManager.RTC_WAKEUP,executeTimer,pendingIntent);

                    p.save();//数据保存

                }

                if (mediaPlayer!=null){
                    mediaPlayer.release();
                    mediaPlayer=null;
                }
                finish();
            }
        });

        mHandler.postDelayed(releaseRunnable,120*1000);//两分钟后关闭提醒
		
        final String ringUrl = p.getRingingUrl();// 铃声路径

		//铃声路径为空
		if (!p.isRinging() || TextUtils.isEmpty(ringUrl) || ringUrl.equals("无")) {
            Toast.makeText(this, "无铃声", Toast.LENGTH_SHORT).show();
            return;
        }
		
		//铃声文件不存在
		File musicFile = new File(ringUrl);
		if (!musicFile.exists()){
            Toast.makeText(this, "铃声不存在", Toast.LENGTH_SHORT).show();
			return;
		}
        mediaPlayer=new MediaPlayer();
		// 异步执行铃声播放
		ThreadPoolManager.getInstance().addTask(new Runnable() {
			@Override
			public void run() {
                mediaPlayer.setLooping(true);//重复
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Toast.makeText(ProgrammeNoticeShowActivity.this,
                                "铃声播放出错 what:"+what+" ; extra:"+extra, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
				mBuilder.setLength(0);
				try {
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置播放类型
                    mediaPlayer.setVolume(1.0f,1.0f);//音量
					mediaPlayer.setDataSource(ringUrl);//铃声文件
					mediaPlayer.prepare();//准备
					mediaPlayer.start();//开始播放
				} catch (IllegalArgumentException e) {

					mBuilder.append("IllegalArgumentException-").append(e.getMessage()).append("\n");
                    mHandler.sendEmptyMessage(0);
					e.printStackTrace();
				} catch (IllegalStateException e) {

					mBuilder.append("IllegalStateException-").append(e.getMessage()).append("\n");
                    mHandler.sendEmptyMessage(0);
					e.printStackTrace();
				} catch (IOException e) {
					mBuilder.append("IOException-").append(e.getMessage()).append("\n");
                    mHandler.sendEmptyMessage(0);
					e.printStackTrace();
				} catch(Exception e){
					e.printStackTrace();
					mBuilder.append("Exception-").append(e.getMessage()).append("\n");
                    mHandler.sendEmptyMessage(0);
				}

			}
		});
        
    }

    @Override
    public void onResume(){
        super.onResume();
        L.e("ProgrammeNoticeShowActivity","-- onResume()");
    }
    @Override
    public void onStop(){
        super.onStop();
        L.e("ProgrammeNoticeShowActivity","-- onStop()");
    }
    @Override
    public void onDestroy(){
        L.e("ProgrammeNoticeShowActivity","-- onDestroy()");
        //释放资源
        if (mediaPlayer!=null){
            mediaPlayer.release();
            mediaPlayer=null;
        }
        if (mWakelock!=null)mWakelock.release();
        if (vibrator!=null) vibrator.cancel();
        if (mHandler!=null){
            mHandler.removeCallbacks(releaseRunnable);
            mHandler.removeMessages(0);
        }
        super.onDestroy();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            noticeInfo.setText(mBuilder.toString());//显示铃声错误日志
        }
    };


    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_POWER){
            if (mediaPlayer!=null){
                mediaPlayer.release();
                mediaPlayer=null;
            }
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    private Runnable releaseRunnable = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer!=null ){

                mediaPlayer.release();
                mediaPlayer=null;
            }
            if (vibrator!=null) vibrator.cancel();
            finish();
        }
    };

}
