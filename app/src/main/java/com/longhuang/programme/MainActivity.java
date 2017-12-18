package com.longhuang.programme;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.longhuang.programme.imp.AlarmBroadcastReceiver;
import com.longhuang.programme.imp.MyRecognizerListener;
import com.longhuang.programme.imp.MySynthesizerListener;
import com.longhuang.programme.module.ExtraProgramme;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.Global;
import com.longhuang.programme.utils.L;
import com.longhuang.programme.utils.ThreadPoolManager;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends BaseActivity  {

    private LinearLayout mainSkin; // 背景
    private RecyclerView recyclerView;
    private CheckBox switchInput;
    private EditText textInput;
    private Button voiceInput;
    private Button sendMessage;
    private CheckBox calendar;
    private CalendarView mDatePicker;
    private ProgrammeAdapter adapter;
    private MyHandler handle;
    private String path;

    private String executeDate;
    private int selectYear,selectMonth,selectDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 添加toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //背景图片URL
        path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath()+"/skin.png";

        initView();
        initEvent();

        L.e("Main"," ---------------- getExternalFilesDir(PICTURES) = "+path);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar,menu);

        return true;
    }
    /*
     * toolbar 里面的item点击监听
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.skin://创建 AlertDialog 选择 图片、铃声
                final AlertDialog mDialog = new AlertDialog.Builder(this).create();
                mDialog.show();
                mDialog.setContentView(R.layout.image_select_dialog);
				
				//位于下方
                Window w = mDialog.getWindow();
                w.setBackgroundDrawableResource(android.R.color.transparent);
                w.setGravity(Gravity.BOTTOM);

                TextView camera = mDialog.findViewById(R.id.image_camera);
                TextView select = mDialog.findViewById(R.id.image_select);
                TextView music = mDialog.findViewById(R.id.ringing_select);
                final TextView cancel = mDialog.findViewById(R.id.image_cancel);

				//拍照
                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();

						//若已有背景图片择删除
                        File outputImage = new File(path);
                        try{
                            if (outputImage.exists()) outputImage.delete();
                            outputImage.createNewFile();
                        }catch (IOException e){
                            e.printStackTrace();
                        }
						// 获取图片uri
                        Uri imageUri;
                        if (Build.VERSION.SDK_INT>=24){
                            imageUri = FileProvider.getUriForFile(MainActivity.this,"com.longhuang.programme.fileprovide",outputImage);
                        }else {
                            imageUri = Uri.fromFile(outputImage);
                        }

                        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //下面这句指定调用相机拍照后的照片存储的路径
                        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(takeIntent, Global.CAMERA_REQUEST_CODE);
                    }
                });

				// 媒体库选择图片
                select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(pickIntent, Global.ALBUM_REQUEST_CODE);
                    }
                });

				//媒体库选择音频文件
                music.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);

                        pickIntent.setDataAndType(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio/*");
                        startActivityForResult(pickIntent, Global.AUDIO_REQUEST_CODE);
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
                break;
            case R.id.add://编辑按钮
                if (adapter == null) break;
                if (Global.programmeCache.size() > 1) break;//当有多个提醒被选中时，无法进入编辑界面
                Intent intent = new Intent(this,ProgrammeMenuActivity.class);
                startActivityForResult(intent,Global.CONFIG_REQUEST_CODE);
                break;
            case R.id.delete://删除选中的提醒
                if (adapter == null){
                    Global.clearCache();
                    break;
                }
                adapter.deleteSelected();
                break;
        }
        return true;
    }

	//初始化界面组件
    private void initView(){
        mainSkin = findViewById(R.id.main_skin);

        recyclerView =  findViewById(R.id.programme_list);
        switchInput = findViewById(R.id.switch_input_mode);
        textInput = findViewById(R.id.text_input);
        voiceInput =  findViewById(R.id.voice_input);
        sendMessage =  findViewById(R.id.send_message);
        mDatePicker = findViewById(R.id.date_picker);
        calendar = findViewById(R.id.calendar);
        mDatePicker.setVisibility(View.GONE);
    }

    private void initEvent(){
        Drawable drawable = BitmapDrawable.createFromPath(path);//从路径中读取图片
        if (drawable!=null) mainSkin.setBackground(drawable);//设置背景
        handle = new MyHandler(this);
        adapter = new ProgrammeAdapter(MainActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
		
		//日历监听选中日期
        mDatePicker.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                final StringBuilder dateBuilder = new StringBuilder();
                selectDay = dayOfMonth;
                selectMonth = month;
                selectYear = year;

                month++;
                dateBuilder.append(year).append("-");
                if (month<10) dateBuilder.append(0);
                dateBuilder.append(month).append("-");
                if (dayOfMonth<10) dateBuilder.append(0);
                dateBuilder.append(dayOfMonth);

                executeDate = dateBuilder.toString();

                L.e("Main","------- OnSelectedDayChange  date = "+executeDate);
                ThreadPoolManager.getInstance().addTask(showSelectRunnable);//同步显示选中日期下的提醒
            }
        });

		// 手动/语音切换按钮
        switchInput.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    voiceInput.setVisibility(View.GONE);
                    textInput.setVisibility(View.VISIBLE);
                }else {
                    textInput.setVisibility(View.GONE);
                    voiceInput.setVisibility(View.VISIBLE);
                }
            }
        });
		
		// 手动输入框添加焦点变更监听
        textInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!calendar.isChecked()) return;

                if (hasFocus) {
                    mDatePicker.setVisibility(View.GONE);
                }else {
                    mDatePicker.setVisibility(View.VISIBLE);
                }

            }
        });

		// 手动输入框添加内容变更监听
        textInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //TODO nothing
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendMessage.setEnabled(!(s.length()==0));
            }
            @Override
            public void afterTextChanged(Editable s) {
                //TODO nothing
            }
        });
		
		// 日历显示按钮点击监听
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendar.isChecked()){ // 打开日历时 列表显示当前日期提醒
                    mDatePicker.setVisibility(View.VISIBLE);
                    if (selectYear==0){
                        setDate();
                    }
                    ThreadPoolManager.getInstance().addTask(showSelectRunnable);
                }else { //关闭日历时 列表显示所有提醒
                    mDatePicker.setVisibility(View.GONE);
                    adapter.showAllProgramme();
                }
            }
        });
		
		// 语音输入触屏监听
        voiceInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!voiceInput.isEnabled()){
                    return true;
                }
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        voiceInput.setPressed(true);
                        handle.sendMessageDelayed(handle.obtainMessage(MyHandler.MEG_START_LISTENING),200);//200毫秒后开始录音
                        break;
                    case MotionEvent.ACTION_UP:

                        voiceInput.setPressed(false);
                        handle.sendEmptyMessage(MyHandler.MSG_STOP_LISTENING);
                }
                return true;
            }
        });

		// 发送按钮点击监听
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
		//		recyclerview.scrolltoposition(0);
                String message = textInput.getText().toString();
                textInput.setText("");
                Programme p = setTimer(message);
                textInput.clearFocus();
                ExtraProgramme programme = new ExtraProgramme(p);

				// 隐藏软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm !=null ){
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }

				//首位插入提醒
                adapter.insertProgramme(programme);
            }
        });
		
		// 连接数据库，获取所有提醒
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                Connector.getDatabase();
                Global.findAll();
                handle.sendEmptyMessage(-1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        L.e("main","onActivityResult() resultCode = "+resultCode);
        switch (requestCode){
            case Global.CONFIG_REQUEST_CODE: //编辑回调
                if (resultCode==Global.EDIT_SAVE){
                    Programme p = Global.getEditProgramme();
                    ThreadPoolManager.getInstance().addTask(new SetTimerRunnable(p));
                    if (Global.clearCache()){
                        adapter.notifyItemChanged(Global.index);
                    }else {
                        ExtraProgramme extra = new ExtraProgramme(p);
                        adapter.insertProgramme(extra);
                    }
                    p.save();
                    break;
                }
                if (resultCode == Global.EDIT_DELETE){
                    adapter.deleteSelected();
                    break;
                }
                if (Global.clearCache()) adapter.notifyDataSetChanged();
                break;
            case Global.CAMERA_REQUEST_CODE://拍照回调
                Drawable drawable = BitmapDrawable.createFromPath(path);
                if (drawable==null) break;

                mainSkin.setBackground(drawable);
                break;
            case Global.ALBUM_REQUEST_CODE://选择图片回调
                String imagePath = L.handleImage(this,data);
                Drawable dra = BitmapDrawable.createFromPath(imagePath);

                if (dra==null) break;
                corpImage(imagePath);
                mainSkin.setBackground(dra);
                break;
            case Global.AUDIO_REQUEST_CODE://选择铃声回调
                String musicPath = L.handleImage(this,data);
                if (TextUtils.isEmpty(musicPath)) break;

				//存储默认铃声路径
                SharedPreferences preferences = getSharedPreferences(Global.MUSIC_URL_NAME,Context.MODE_PRIVATE);
                preferences.edit().putString(Global.MUSIC_URL_NAME,musicPath);
                Global.MUSIC_URL = musicPath;
        }
    }

	//从提醒信息中识别 00:00 | 00.00
    private Programme setTimer(String message){
        final Programme p = new Programme(message);

        final String execute;
        final Calendar c = Calendar.getInstance(Locale.CHINA);
        if (!calendar.isChecked()) {
            setDate();
        }

        p.setDate(executeDate);//执行日期
        if (message.length()>3){

			//利用正则表达式匹配时间格式
            Pattern pattern = Pattern.compile("[0-2][0-9][:.][0-5][0-9]");
            final Matcher matcher = pattern.matcher(message);
            if (!matcher.find()){
                p.save();
                return p;
            }
            ThreadPoolManager.getInstance().addTask(new Runnable() {
                @Override
                public void run() {
                    String time = matcher.group();
                    time = time.replace(".",":");
                    String[] arr = time.split(":");

                    p.setTime(time);

                    int hour = Integer.valueOf(arr[0]);
                    int minute = Integer.valueOf(arr[1]);
                    if (hour>23) {
                        p.save();
                        return;
                    }
					
					//将日期时间转换成时间戳
                    c.set(selectYear,selectMonth,selectDay,hour,minute);
                    long executeTime = c.getTimeInMillis();
                    p.setExecuteTime(String.valueOf(executeTime));
                    p.save();

                    L.e("setTimer",String.valueOf(System.currentTimeMillis()));
                    L.e("setTimer",p.toString());

                    if (executeTime-System.currentTimeMillis() <5*1000) return;
					
					//添加闹钟
                    AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(MainActivity.this,AlarmBroadcastReceiver.class);
                    intent.putExtra("programmeId",p.getProgrammeId());
                    intent.putExtra("executeTime",p.getExecuteTime());
                    PendingIntent pendingIntent = PendingIntent
                            .getBroadcast(MainActivity.this,p.getRequestCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT);
                    manager.setExact(AlarmManager.RTC_WAKEUP,executeTime,pendingIntent);

                }
            });
        }else {
            p.save();
        }
        return p;
    }

	//获取当前的日期时间
    private void setDate(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        executeDate = format.format(date);
        selectYear = date.getYear()+1900;
        selectMonth = date.getMonth();
        selectDay = date.getDate();

    }

	// 从数据库中查找固定日期的提醒
    private Runnable showSelectRunnable = new Runnable() {
        @Override
        public void run() {
            List<Programme> list = DataSupport.where("date = ?",executeDate).find(Programme.class);
            if (list==null) list = new ArrayList<>();
            Collections.reverse(list);//反序
            List<ExtraProgramme> extra = new ArrayList<>();
            for (Programme p : list){
                extra.add(new ExtraProgramme(p));
            }
            adapter.setProgrammeSelectedDate(extra);
            handle.sendEmptyMessage(0);
        }
    };
    @Override
    public void onResume(){
        super.onResume();

		//获取默认铃声路径
        SharedPreferences preferences = getSharedPreferences(Global.MUSIC_URL_NAME,Context.MODE_PRIVATE);
        Global.MUSIC_URL = preferences.getString(Global.MUSIC_URL_NAME,"无");
    }

	//异步添加闹钟
    class SetTimerRunnable implements Runnable{
        public SetTimerRunnable(Programme p){
            this.p = p;
        }
        private Programme p;
        @Override
        public void run() {
            String executeTime = p.getExecuteTime();
            if (TextUtils.isEmpty(executeTime)) return;

            long executeTimer = Long.valueOf(p.getExecuteTime());
            if (executeTimer-System.currentTimeMillis() > 5*1000){
                AlarmManager manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(MainActivity.this,AlarmBroadcastReceiver.class);
                intent.putExtra("programmeId",p.getProgrammeId());
                intent.putExtra("executeTime",executeTime);
                PendingIntent pendingIntent = PendingIntent
                        .getBroadcast(MainActivity.this,p.getRequestCode(),intent,PendingIntent.FLAG_UPDATE_CURRENT);
                manager.setExact(AlarmManager.RTC_WAKEUP,executeTimer,pendingIntent);
                L.e("SetTimerRunnable",String.valueOf(System.currentTimeMillis()));
                L.e("SetTimerRunnable",p.toString());
            }
        }
    }

    static class MyHandler extends Handler  {
        public static final int MEG_START_LISTENING = 1;
        public static final int MSG_STOP_LISTENING = 2;

        private SpeechRecognizer speechRecognizer;//语音识别

        private MyRecognizerListener mRecognizerListener;
        private WeakReference<MainActivity> weakReference;
        public MyHandler (MainActivity activity){
            weakReference = new WeakReference<MainActivity>(activity);
            speechRecognizer = SpeechRecognizer.createRecognizer(activity, new InitListener() {
                @Override
                public void onInit(int i) {
                    if (i == ErrorCode.SUCCESS){
                        speechRecognizer.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT,"30000");
                        speechRecognizer.setParameter(SpeechConstant.VAD_BOS,"8000");
                        speechRecognizer.setParameter(SpeechConstant.VAD_EOS,"8000");
                        Global.VOICE_ENABLE = true;
                    }else {
                        L.toast(weakReference.get(),"语音功能不可用 -- ERROR = "+i);
                        weakReference.get().voiceInput.setEnabled(false);
                        Global.VOICE_ENABLE = false;
                    }
                }
            });
            SpeechSynthesizer.createSynthesizer(activity, new InitListener() {
                @Override
                public void onInit(int i) {
                    if (i != ErrorCode.SUCCESS){
                        L.toast(weakReference.get(),"语音功能不可用 -- ERROR = "+i);
                        weakReference.get().voiceInput.setEnabled(false);
                        Global.VOICE_ENABLE = false;
                    }
                }
            });
            mRecognizerListener = new MyRecognizerListener(this);
        }

        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case -1:
                    weakReference.get().adapter.notifyDataSetChanged();
                    break;
                case 0:
                    weakReference.get().adapter.notifyDataSetChanged();
                    break;
                case MEG_START_LISTENING:
                    if (weakReference.get().voiceInput.isPressed())
                        speechRecognizer.startListening(mRecognizerListener);
                    break;
                case MSG_STOP_LISTENING:
                    if (speechRecognizer.isListening()){
                        speechRecognizer.stopListening();
                    }
                    break;
                case MyRecognizerListener.MSG_PARSE_JSON:
                    speechRecognizer.stopListening();
                    if (weakReference.get().voiceInput.isPressed()){
                        weakReference.get().voiceInput.setPressed(false);
                    }
                    String resultInfo = (String)msg.obj;
                    Programme p = weakReference.get().setTimer(resultInfo);
                    ExtraProgramme programme = new ExtraProgramme(p);
                    weakReference.get().adapter.insertProgramme(programme);
                    break;
                case MyRecognizerListener.MSG_PARSE_ERR:
                    if (weakReference.get().voiceInput.isPressed()){
                        weakReference.get().voiceInput.setPressed(false);
                    }
                   if (SpeechSynthesizer.getSynthesizer() != null) SpeechSynthesizer.getSynthesizer().startSpeaking("对不起，我没听清楚",new MySynthesizerListener());
                    break;
            }
        }

    }
	
	//复制背景图片到指定目录
    private void corpImage(final String oldPath){
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                try {

                    int byteRead = 0;
                    File oldFile = new File(oldPath);
                    if (oldFile.exists()) {//文件存在时
                        InputStream inStream = new FileInputStream(oldPath);//读入原文件
                        FileOutputStream fs = new FileOutputStream(path);
                        byte[] buffer = new byte[1024];

                        while ((byteRead = inStream.read(buffer)) != -1) {

                            fs.write(buffer, 0, byteRead);
                        }
                        inStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        L.e("KeyEvent","---keyCode ="+keyCode );
        L.e("KeyEvent","---getAction ="+event.getAction() );
        if (keyCode==67 ){
            if (textInput.hasFocus()){
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm !=null ){
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                textInput.clearFocus();

                return true;
            }
        }

        return super.onKeyDown(keyCode,event);
    }
}
