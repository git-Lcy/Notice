package com.xingcheng.programme;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
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
import com.xingcheng.programme.imp.AlarmBroadcastReceiver;
import com.xingcheng.programme.imp.MyRecognizerListener;
import com.xingcheng.programme.imp.MySynthesizerListener;
import com.xingcheng.programme.module.ExtraProgramme;
import com.xingcheng.programme.module.Programme;
import com.xingcheng.programme.utils.Global;
import com.xingcheng.programme.utils.L;
import com.xingcheng.programme.utils.ThreadPoolManager;

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

    private int width,height;
    private LinearLayoutManager manager;
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

                        File outputImage = new File(path);
                        Uri imageUri;
                        Intent intent = new Intent();
                        if (Build.VERSION.SDK_INT>=24){
                            outputImage.getParentFile().mkdirs();
                            imageUri = FileProvider.getUriForFile(MainActivity.this,"com.xingcheng.programme.fileprovider",outputImage);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                        }else {
                            try {
                                outputImage.createNewFile();
                            } catch (IOException e) {
                                outputImage.mkdirs();
                            }

                            imageUri = Uri.fromFile(outputImage);
                        }
                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);//设置Action为拍照
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//将拍取的照片保存到指定URI

                        startActivityForResult(intent, Global.CAMERA_REQUEST_CODE);
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
        //获取默认铃声路径
        SharedPreferences preferences = getSharedPreferences(Global.MUSIC_URL_NAME,Context.MODE_PRIVATE);
        Global.MUSIC_URL = preferences.getString(Global.MUSIC_URL_NAME,"无");

        Drawable drawable = BitmapDrawable.createFromPath(path);//从路径中读取图片
        if (drawable!=null) mainSkin.setBackground(drawable);//设置背景

        handle = new MyHandler(this);
        adapter = new ProgrammeAdapter(MainActivity.this);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
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
                moveToPosition();//列表显示最顶端
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
                startPhotoZoom(path);
                break;
            case Global.ALBUM_REQUEST_CODE://选择图片回调
                if (data==null) break;
                String imagePath = L.handleImage(this,data);
                startPhotoZoom(imagePath);
                break;
            case Global.AUDIO_REQUEST_CODE://选择铃声回调
                if (data==null) break;
                String musicPath = L.handleImage(this,data);
                if (TextUtils.isEmpty(musicPath)) {
                    L.toast(this,"选择铃声失败");
                    break;
                }

				//存储默认铃声路径
                SharedPreferences preferences = getSharedPreferences(Global.MUSIC_URL_NAME,Context.MODE_PRIVATE);
                preferences.edit().putString(Global.MUSIC_URL_NAME,musicPath).apply();

                Global.MUSIC_URL = musicPath;
                break;
            case Global.PHOTO_REQUEST_CUT://图片裁减回调
                Drawable drawable = BitmapDrawable.createFromPath(path);
                if (drawable==null) break;
                mainSkin.setBackground(drawable);
                break;
        }
    }

	//从提醒信息中识别 00:00 | 00.00
    private Programme setTimer(String message){
        final Programme p = new Programme(message);

    //    final String execute;
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

                    if (executeTime-System.currentTimeMillis() <3*1000) return;
					
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
        if (width==0){
            width = mainSkin.getMeasuredWidth();
            height = mainSkin.getMeasuredHeight();
        }
        L.e("Main","///// width = "+width+" ;  height = "+height);
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
                    if (weakReference.get().width==0){
                        weakReference.get().width = weakReference.get().mainSkin.getMeasuredWidth();
                        weakReference.get().height = weakReference.get().mainSkin.getMeasuredHeight();
                    }
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
                    weakReference.get().moveToPosition();
                    break;
                case MyRecognizerListener.MSG_PARSE_ERR://语音解析失败
                    if (weakReference.get().voiceInput.isPressed()){
                        weakReference.get().voiceInput.setPressed(false);
                    }
                   if (SpeechSynthesizer.getSynthesizer() != null) SpeechSynthesizer.getSynthesizer().startSpeaking("对不起，我没听清楚",new MySynthesizerListener());
                    break;
            }
        }

    }

    // RecyclerView回到顶部
    private void moveToPosition() {

        int firstItem = manager.findFirstVisibleItemPosition();
        int lastItem = manager.findLastVisibleItemPosition();
        if (0 <= firstItem) {
            recyclerView.scrollToPosition(0);
        } else if (0 <= lastItem) {
            int top = recyclerView.getChildAt(0 - firstItem).getTop();
            recyclerView.scrollBy(0, top);
        } else {
            recyclerView.scrollToPosition(0);
        }

    }

    //图片裁减
    private void startPhotoZoom(String selectPath) {
        Uri imageUri;
        Uri outputUri;
        File file=new File(path);
        Intent intent = new Intent("com.android.camera.action.CROP");//调用系统裁剪
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //android7.0及以上
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //通过FileProvider创建一个content类型的Uri
            imageUri=FileProvider.getUriForFile(this, "com.xingcheng.programme.fileprovider", new File(selectPath));//通过FileProvider创建一个content类型的Uri
            outputUri = Uri.fromFile(new File(path));
        } else {
            imageUri = Uri.fromFile(file);
            outputUri = Uri.fromFile(new File(path));
        }
        intent.setDataAndType(imageUri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");
        intent.putExtra("scale",true);//缩放
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);//输出路径
        intent.putExtra("noFaceDetection", true); // 取消人脸识别

        //宽高比
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);

        startActivityForResult(intent, Global.PHOTO_REQUEST_CUT);
    }

    //监听按键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if (keyCode==KeyEvent.KEYCODE_DEL ){ //删除键 Key code constant: Backspace key.

            //隐藏软键盘
            if (textInput.hasFocus()){
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm !=null ){
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }

                textInput.clearFocus();//清除焦点

                return true;
            }
        }

        return super.onKeyDown(keyCode,event);
    }
}
