package com.xingcheng.programme;

import android.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.xingcheng.programme.module.Programme;
import com.xingcheng.programme.utils.Global;
import com.xingcheng.programme.utils.L;

import java.util.Calendar;
import java.util.Locale;


public class ProgrammeMenuActivity extends BaseActivity implements View.OnClickListener{

    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private AlertDialog dialog;

    private TextView dateTimeView,repeatView,ringingView,ringing,vibrate;
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
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }

	//toolbar item点击监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.cancel:
                setResult(Global.EDIT_CANCEL);
                finish();
                break;
            case R.id.ok:
                mProgramme.setMessage(noticeEditView.getText().toString());
                setResult(Global.EDIT_SAVE);
                L.e("SAVE_EDIT",mProgramme.toString());
                finish();
                break;
        }
        return true;
    }
    private void initView(){
        mProgramme = Global.programme = new Programme();
        if (Global.programmeCache.size()!=0) mProgramme.setProgrammeInfo(Global.programmeCache.get(0).getProgramme());

        dateTimeView = findViewById(R.id.date_time_text);
        repeatView = findViewById(R.id.repeat_text);
        ringing = findViewById(R.id.ringing);
        vibrate = findViewById(R.id.vibrate);
        ringingView = findViewById(R.id.ringing_url);
        noticeEditView = findViewById(R.id.notice_edit);

        deleteNoticeView = findViewById(R.id.delete_notice);
    }
    private void initEvent(){
        repeat = mProgramme.getRepeatType();

        String info = mProgramme.getMessage();
        String date = mProgramme.getDate();
        String time = mProgramme.getTime();
        String ringUrl = mProgramme.getRingingUrl();

        String music = getMusic(ringUrl);

        repeatView.setText(repeat==0 ? "只提醒一次" : "每天" );
        ringing.setText(mProgramme.isRinging() ? "开" : "关");
        vibrate.setText(mProgramme.isVibrate() ? "开" : "关");
        noticeEditView.setText(TextUtils.isEmpty(info) ? "提醒" : info);

        ringingView.setText(music);

        if(!TextUtils.isEmpty(date) && !TextUtils.isEmpty(time)){
            dateTimeView.setText(date+" "+time);
        }

        dateTimeView.setOnClickListener(this);
        repeatView.setOnClickListener(this);
        ringing.setOnClickListener(this);
        vibrate.setOnClickListener(this);
        ringingView.setOnClickListener(this);
        deleteNoticeView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.date_time_text:
                if (dialog==null){
                    dialog = new AlertDialog.Builder(this).create();
                    dialog.show();
                    dialog.setContentView(R.layout.date_time_dialog);

                    mDatePicker = dialog.findViewById(R.id.date_picker);
                    mTimePicker = dialog.findViewById(R.id.time_picker);
                    Button okBtn = dialog.findViewById(R.id.time_ok);
                    okBtn.setOnClickListener(this);
                }else {
                    dialog.show();
                }

                break;
            case R.id.time_ok:
                int year = mDatePicker.getYear();
                int month = mDatePicker.getMonth();
                int day = mDatePicker.getDayOfMonth();
                int hour = mTimePicker.getCurrentHour();
                int minute = mTimePicker.getCurrentMinute();

                Calendar calendar = Calendar.getInstance(Locale.CHINA);

                calendar.set(year,month,day,hour,minute);
                long execute = calendar.getTimeInMillis();
                mProgramme.setExecuteTime(String.valueOf(execute));

                StringBuilder builder = new StringBuilder();
                builder.append(year).append("-");
                if (month<10) builder.append(0);
                builder.append(month).append("-");
                if (day<10) builder.append(0);
                builder.append(day);
                String date = builder.toString();
                mProgramme.setDate(date);
                builder.setLength(0);
                if (hour<10) builder.append(0);
                builder.append(hour).append(":");
                if (minute<10) builder.append(0);
                builder.append(minute);
                String time = builder.toString();
                mProgramme.setTime(time);

                dateTimeView.setText(date+" "+time);
                dialog.dismiss();
                break;
            case R.id.repeat_text:

                repeat = (mProgramme.getRepeatType()==0? 1 : 0);
                repeatView.setText(repeat==0 ? "只提醒一次" : "每天" );
                mProgramme.setRepeatType(repeat);
                break;
            case R.id.ringing:
                boolean isRing = !mProgramme.isRinging();
                ringing.setText(isRing ? "开" : "关");
                mProgramme.setRinging(isRing);
                break;
            case R.id.vibrate:
                boolean isVibrate = !mProgramme.isVibrate();
                vibrate.setText(isVibrate ? "开" : "关");
                mProgramme.setVibrate(isVibrate);
                break;
            case R.id.ringing_url:
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);

                pickIntent.setDataAndType(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio/*");
                startActivityForResult(pickIntent, Global.AUDIO_REQUEST_CODE);
                break;
            case R.id.delete_notice:
                setResult(Global.EDIT_DELETE);
                finish();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (data==null) return;
        String ringingPath = L.handleImage(this,data);

        mProgramme.setRingingUrl(ringingPath);
        String music = getMusic(ringingPath);
        ringingView.setText(music);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            setResult(Global.EDIT_CANCEL);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    private String getMusic(String musicPath){
        if (TextUtils.isEmpty(musicPath)) return "无";
        int index = musicPath.lastIndexOf("/")+1;
        if (index == 0 ) return "无";
        String music = musicPath.substring(index,musicPath.length());
        return music;
    }
}
