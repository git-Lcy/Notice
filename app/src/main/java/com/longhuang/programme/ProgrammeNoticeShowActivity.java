package com.longhuang.programme;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.longhuang.programme.imp.AlarmBroadcastReceiver;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.L;

import org.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProgrammeNoticeShowActivity extends BaseActivity {

    private TextView noticeInfo;
    private ImageButton noticeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programme_setting);

        noticeInfo = findViewById(R.id.notice_info);
        noticeBtn = findViewById(R.id.notice_btn);

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
        boolean vibrate = p.isVibrate();
        final Vibrator vibrator ;
        if (vibrate){
            vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
            if (vibrator.hasVibrator()){
                vibrator.vibrate(5*60*1000);
            }
        }else {
            vibrator=null;
        }

        String message = p.getMessage();
        noticeInfo.setText(message);
        final int repeat = p.getRepeatType();
        noticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (repeat==0){
                    p.setExecuted(true);
                }else {
                    String executeTime = p.getExecuteTime();//2017-11-08 13:18
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date date = null;
                    try {
                        date = format.parse(executeTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        finish();
                        return;
                    }
                    long executeTimer = date.getTime();
                    L.e("ProgrammeNoticeShowActivity","Programme date.getTime() = "+executeTime);
                    date.setTime(executeTimer+24*60*60*1000);
                    executeTime = format.format(date);
                    p.setExecuteTime(executeTime);
                    AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(ProgrammeNoticeShowActivity.this,AlarmBroadcastReceiver.class);
                    intent.putExtra("programmeId",p.getProgrammeId());
                    intent.putExtra("executeTime",p.getExecuteTime());
                    PendingIntent pendingIntent = PendingIntent
                            .getBroadcast(ProgrammeNoticeShowActivity.this,0,intent,0);
                    manager.setExact(AlarmManager.RTC_WAKEUP,executeTimer+24*60*60*1000,pendingIntent);

                }
                p.save();
                if (vibrator!=null ){
                    vibrator.cancel();
                }
                finish();
            }
        });
    }
}
