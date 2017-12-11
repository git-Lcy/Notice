package com.longhuang.programme.imp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.longhuang.programme.ProgrammeNoticeShowActivity;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.L;

import org.litepal.crud.DataSupport;


/**
 * Created by lfy on 2017/12/9.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static final String ALARM_ACTION = "com.longhuang.programme.ACTION_ALARM";
    @Override
    public void onReceive(Context context, Intent intent) {

        String id = intent.getStringExtra("programmeId");

        Programme p = DataSupport.where("programmeId = ?",id).findFirst(Programme.class);

        L.e("BroadcastReceiver","--- id = "+id);
        if (p==null) {
            L.e("BroadcastReceiver","--- programmeId not found");
            return;
        }
        L.e("BroadcastReceiver","--- name = "+p.getMessage());
        Intent noticeIntent = new Intent(context, ProgrammeNoticeShowActivity.class);
        noticeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(noticeIntent);
    }
}
