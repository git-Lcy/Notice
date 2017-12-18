package com.longhuang.programme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.longhuang.programme.imp.AlarmBroadcastReceiver;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.L;
import com.longhuang.programme.utils.ThreadPoolManager;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgrammeNoticeShowActivity extends BaseActivity {

    private TextView noticeInfo;
    private ImageView noticeBtn;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private PowerManager.WakeLock mWakelock;
    private StringBuilder mBuilder;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        InitArg();
    }
    private void InitArg(){

        //其他操作
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_programme_setting);

        noticeInfo = findViewById(R.id.notice_info);
        noticeBtn = findViewById(R.id.notice_btn);

        mBuilder = new StringBuilder();
        // 点击取消震动
        noticeInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vibrator!=null) vibrator.cancel();
            }
        });

        // id
        String id = getIntent().getStringExtra("programmeId");
        if (TextUtils.isEmpty(id)) {
            L.e("ProgrammeNoticeShowActivity","Programme id = null");
            finish();
            return;
        };

        final Programme p = DataSupport.where("programmeId = ?",id).findFirst(Programme.class);
        if (p==null) {
            L.e("ProgrammeNoticeShowActivity","Programme not found");
            finish();
            return;
        }
        L.e("ProgrammeNoticeShowActivity",p.toString());

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

                finish();
            }
        });

		
        final String ringUrl = p.getRingingUrl();// 铃声路径

		//铃声路径为空
		if (!p.isRinging() || TextUtils.isEmpty(ringUrl) || ringUrl.equals("无")) {
			noticeInfo.setText("无");
			return;
        }
		
		//铃声文件不存在
		File musicFile = new File(ringUrl);
		if (!musicFile.exists()){
			noticeInfo.setText("音乐文件不存在");
			return;
		}

		// 异步执行铃声播放
		ThreadPoolManager.getInstance().addTask(new Runnable() {
			@Override
			public void run() {

				mediaPlayer=new MediaPlayer();
				mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
				mBuilder.setLength(0);
				try {
					mediaPlayer.setDataSource(ringUrl);
					mediaPlayer.prepare();
					mediaPlayer.start();
					mBuilder.append("播放正常").append("\n");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					mBuilder.append("IllegalArgumentException-").append(e.getMessage()).append("\n");
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					mBuilder.append("IllegalStateException-").append(e.getMessage()).append("\n");
					e.printStackTrace();
				} catch (IOException e) {
					mBuilder.append("IOException-").append(e.getMessage()).append("\n");
					e.printStackTrace();
				} catch(Exception e){
					e.printStackTrace();
					mBuilder.append("Exception-").append(e.getMessage()).append("\n");
				}
				mHandler.sendEmptyMessage(0);
			}
		});
        
    }

    @Override
    public void onResume(){
        super.onResume();
        // 熄屏状态下唤醒屏幕
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);

        mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.SCREEN_DIM_WAKE_LOCK, "SimpleTimer");
        mWakelock.acquire();
    }
    @Override
    public void onStop(){
        super.onStop();
        if (mediaPlayer!=null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        if (mWakelock!=null)mWakelock.release();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if (vibrator!=null) vibrator.cancel();
        if (mediaPlayer!=null) mediaPlayer.release();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            noticeInfo.setText(mBuilder.toString());
        }
    };
}
