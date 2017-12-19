package com.xingcheng.programme.imp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.xingcheng.programme.ProgrammeNoticeShowActivity;
import com.xingcheng.programme.module.Programme;
import com.xingcheng.programme.utils.L;

import org.litepal.crud.DataSupport;


/**
 * Created by lfy on 2017/12/9.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static final String ALARM_ACTION = "com.com.xingcheng.programme.ACTION_ALARM";
    @Override
    public void onReceive(Context context, Intent intent) {

        String id = intent.getStringExtra("programmeId");
        String time = intent.getStringExtra("executeTime");
        L.e("BroadcastReceiver","--- programmeId = "+id+"  ; time = "+time);
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(time)) return;

        boolean exist = DataSupport.isExist(Programme.class,"programmeId = ? and executeTime = ?",id,time);
        L.e("BroadcastReceiver","--- id = "+id + "   exist = "+exist);
        if (!exist ) {
            L.e("BroadcastReceiver","--- programmeId not found");
            return;
        }

        Intent noticeIntent = new Intent(context, ProgrammeNoticeShowActivity.class);
        noticeIntent.putExtra("programmeId",id);
    //    noticeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(noticeIntent);
    }
}
