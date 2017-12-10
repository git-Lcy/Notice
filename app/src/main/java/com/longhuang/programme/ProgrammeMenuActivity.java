package com.longhuang.programme;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.longhuang.programme.Imp.AlarmBroadcastReceiver;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.Global;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class ProgrammeMenuActivity extends BaseActivity implements View.OnClickListener{

    private DatePicker mDatePicker;
    private TimePicker mTimePicker;

    private TextView dateTimeView,repeatView,ringingView;
    private TextView deleteNoticeView;
    private EditText noticeEditView;

    private Programme mProgramme;
    private int repeat;
    private long executeTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programme_menu);
        Toolbar bar = findViewById(R.id.tool_bar_menu);

        setSupportActionBar(bar);

        initView();
        initEvent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.cancel:
                setResult(Global.EDIT_CANCEL);
                finish();
                break;
            case R.id.ok:
                mProgramme.setMessage(noticeEditView.getText().toString());

                if (executeTimer-System.currentTimeMillis() > 60*1000){
                    AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    PendingIntent pendingIntent = PendingIntent
                            .getBroadcast(this,0,new Intent(AlarmBroadcastReceiver.ALARM_ACTION),0);
                    manager.setExact(AlarmManager.RTC_WAKEUP,executeTimer,pendingIntent);
                }else {
                    mProgramme.setExecuted(true);
                }
                setResult(Global.EDIT_SAVE);
                finish();
                break;
        }
        return true;
    }
    private void initView(){
        mProgramme = Global.getProgrammeInfo();
        executeTimer = 0;
        dateTimeView = findViewById(R.id.date_time_text);
        repeatView = findViewById(R.id.repeat_text);
        ringingView = findViewById(R.id.ringing_text);
        noticeEditView = findViewById(R.id.notice_edit);

        deleteNoticeView = findViewById(R.id.delete_notice);
    }
    private void initEvent(){
        repeat = mProgramme.getRepeatType();
        repeatView.setText(repeat==0 ? "只提醒一次" : "每天" );
        String info = mProgramme.getMessage();
        noticeEditView.setText(TextUtils.isEmpty(info) ? "提醒" : info);
        String ringUrl = mProgramme.getRingingUrl();
        ringingView.setText(TextUtils.isEmpty(ringUrl)?"无" : ringUrl);

        dateTimeView.setOnClickListener(this);
        repeatView.setOnClickListener(this);
        ringingView.setOnClickListener(this);
        deleteNoticeView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.date_time_text:
                final Dialog dialog = new Dialog(this);
                dialog.show();
                dialog.setContentView(R.layout.date_time_dialog);
                final DatePicker datePicker = dialog.findViewById(R.id.date_picker);
                final TimePicker timePicker = dialog.findViewById(R.id.time_picker);
                Button okBtn = dialog.findViewById(R.id.time_ok);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int year = datePicker.getYear();
                        int month = datePicker.getMonth()+1;
                        int day = datePicker.getDayOfMonth();
                        int hour = timePicker.getCurrentHour();
                        int minute = timePicker.getCurrentMinute();
                        Date date = new Date(year,month,day);
                        date.setHours(hour);
                        date.setMinutes(minute);
                        executeTimer = date.getTime();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        String executeTime = format.format(date);
                        mProgramme.setExecuteTime(executeTime);
                        dialog.dismiss();
                    }
                });

                break;
            case R.id.repeat_text:
                repeat = repeat==0? 1 : 0;
                repeatView.setText(repeat==0 ? "只提醒一次" : "每天" );
                mProgramme.setRepeatType(repeat);
                break;
            case R.id.ringing_text:

                break;
            case R.id.delete_notice:
                setResult(Global.EDIT_DELETE);
                finish();
                break;
        }
    }
}
