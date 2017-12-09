package com.longhuang.programme;

import android.app.AlarmManager;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.longhuang.programme.Imp.AlarmBroadcastReceiver;
import com.longhuang.programme.module.ExtraProgramme;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.Global;
import com.longhuang.programme.utils.ThreadPoolManager;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class ProgrammeMenuActivity extends BaseActivity implements View.OnClickListener{

    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private TextView repeatView,ringingView;
    private TextView deleteNoticeView;
    private EditText noticeEditView;

    private Programme mProgramme;
    private int repeat;
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
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.cancel:
                setResult(0);
                finish();
                break;
            case R.id.ok:
                int year = mDatePicker.getYear();
                int month = mDatePicker.getMonth()+1;
                int day = mDatePicker.getDayOfMonth();
                int hour = mTimePicker.getCurrentHour();
                int minute = mTimePicker.getCurrentMinute();
                Date date = new Date(year,month,day);
                date.setHours(hour);
                date.setMinutes(minute);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
                String executeTime = format.format(date);
                mProgramme.setExecuteTime(executeTime);
                long noticeTime = date.getTime();
                if (noticeTime-System.currentTimeMillis() > 60*1000){
                    AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    PendingIntent pendingIntent = PendingIntent
                            .getBroadcast(this,0,new Intent(AlarmBroadcastReceiver.ALARM_ACTION),0);
                    manager.setExact(AlarmManager.RTC_WAKEUP,noticeTime,pendingIntent);
                }else {
                    mProgramme.setExecuted(true);
                }
                setResult(0);
                finish();
                break;
        }
        return true;
    }
    private void initView(){
        mProgramme = Global.getProgrammeInfo();

        mDatePicker = findViewById(R.id.date_picker);
        mTimePicker = findViewById(R.id.time_picker);
        repeatView = findViewById(R.id.repeat);
        ringingView = findViewById(R.id.ringing);
        deleteNoticeView = findViewById(R.id.delete_notice);
        noticeEditView = findViewById(R.id.notice_edit);
    }
    private void initEvent(){
        repeat = mProgramme.getRepeatType();
        repeatView.setText(repeat==0 ? "只提醒一次" : "每天" );
        noticeEditView.setText(mProgramme.getMessage());
        String ringUrl = mProgramme.getRingingUrl();
        ringingView.setText(TextUtils.isEmpty(ringUrl)?"无" : ringUrl);

        repeatView.setOnClickListener(this);
        ringingView.setOnClickListener(this);
        deleteNoticeView.setOnClickListener(this);

        noticeEditView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                mProgramme.setMessage(noticeEditView.getText().toString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.repeat:
                repeat = repeat==0? 1 : 0;
                repeatView.setText(repeat==0 ? "只提醒一次" : "每天" );
                mProgramme.setRepeatType(repeat);
                break;
            case R.id.ringing:

                break;
            case R.id.delete_notice:
                setResult(0);
                finish();
                break;
        }
    }
}
