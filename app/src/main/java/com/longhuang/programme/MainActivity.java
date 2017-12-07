package com.longhuang.programme;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.longhuang.programme.Imp.MyRecognizerListener;
import com.longhuang.programme.Imp.MySynthesizerListener;
import com.longhuang.programme.module.ExtraProgramme;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.Global;
import com.longhuang.programme.utils.L;
import com.longhuang.programme.utils.ThreadPoolManager;

import org.litepal.crud.DataSupport;
import org.litepal.parser.LitePalContentHandler;
import org.litepal.tablemanager.Connector;
import org.litepal.util.Const;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends BaseActivity  {

    private LinearLayout mainSkin;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        path = getFilesDir().getPath()+"/skin.png";
        initView();
        initEvent();

        L.e("Main"," ---------------- getFilesDir() = "+getFilesDir().getPath());
        L.e("Main"," ---------------- getFilesDir().getAbsolutePath() = "+getFilesDir().getAbsolutePath());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.toolbar,menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.skin:
                final AlertDialog mDialog = new AlertDialog.Builder(this).create();
                mDialog.show();
                mDialog.setContentView(R.layout.image_select_dialog);
                Window w = mDialog.getWindow();
                w.setBackgroundDrawableResource(android.R.color.transparent);
                w.setGravity(Gravity.BOTTOM);
                TextView camera = mDialog.findViewById(R.id.image_camera);
                TextView select = mDialog.findViewById(R.id.image_select);
                final TextView cancel = mDialog.findViewById(R.id.image_cancel);
                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //下面这句指定调用相机拍照后的照片存储的路径
                        takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse(path));
                        startActivityForResult(takeIntent, Global.CAMERA_REQUEST_CODE);
                    }
                });
                select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent albumIntent = new Intent(Intent.ACTION_PICK,null);
                        Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型"
                        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(pickIntent, Global.ALBUM_REQUEST_CODE);
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });
                break;
            case R.id.add:
                if (adapter == null) break;
                if (Global.programmeCache.size() > 1) break;
                Intent intent = new Intent(this,ProgrammeMenuActivity.class);
                startActivityForResult(intent,Global.CONFIG_REQUEST_CODE);
                break;
            case R.id.delete:
                if (adapter == null) break;
                adapter.deleteSelected();
                break;
        }
        return true;
    }
    private void initView(){
        mainSkin = findViewById(R.id.main_skin);
        recyclerView = (RecyclerView) findViewById(R.id.programme_list);
        switchInput = (CheckBox)findViewById(R.id.switch_input_mode);
        textInput = (EditText)findViewById(R.id.text_input);
        voiceInput = (Button) findViewById(R.id.voice_input);
        sendMessage = (Button) findViewById(R.id.send_message);
        mDatePicker = findViewById(R.id.date_picker);
        calendar = findViewById(R.id.calendar);
        mDatePicker.setVisibility(View.GONE);
    }
    private void initEvent(){
        handle = new MyHandler(this);
        adapter = new ProgrammeAdapter(MainActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        mDatePicker.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                final StringBuilder dateBuilder = new StringBuilder();
                month++;
                dateBuilder.append(year).append("-");
                if (month<10) dateBuilder.append(0);
                dateBuilder.append(month).append("-");
                if (dayOfMonth<10) dateBuilder.append(0);
                dateBuilder.append(dayOfMonth);
                L.e("Main","------- OnSelectedDayChange  date = "+dateBuilder.toString());
                ThreadPoolManager.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        List<Programme> list = DataSupport.where("date = ?",dateBuilder.toString()).find(Programme.class);
                        if (list==null) list = new ArrayList<>();
                        Collections.reverse(list);
                        List<ExtraProgramme> extra = new ArrayList<>();
                        for (Programme p : list){
                            extra.add(new ExtraProgramme(p));
                        }
                        adapter.setProgrammeSelectedDate(extra);
                        handle.sendEmptyMessage(0);
                    }
                });
            }
        });
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

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendar.isChecked()){
                    mDatePicker.setVisibility(View.VISIBLE);
                }else {
                    mDatePicker.setVisibility(View.GONE);
                    adapter.showAllProgramme();
                }
            }
        });

        voiceInput.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!voiceInput.isEnabled()){
                    return true;
                }
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        voiceInput.setPressed(true);
                        handle.sendMessageDelayed(handle.obtainMessage(MyHandler.MEG_START_LISTENING),200);
                        break;
                    case MotionEvent.ACTION_UP:

                        voiceInput.setPressed(false);
                        handle.sendEmptyMessage(MyHandler.MSG_STOP_LISTENING);
                }
                return true;
            }
        });
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = textInput.getText().toString();
                textInput.setText("");
                ExtraProgramme programme = Global.saveProgramme(message);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm !=null ){
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                adapter.insertProgramme(programme);
            }
        });

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
    public void onActivityResult(int requestCode,int resultCode,Intent date){
        super.onActivityResult(requestCode,resultCode,date);
        switch (requestCode){
            case Global.CONFIG_REQUEST_CODE:

                break;
            case Global.CAMERA_REQUEST_CODE:

                
                Drawable drawable = BitmapDrawable.createFromPath(path);
                if (drawable==null) break;

                mainSkin.setBackground(drawable);
                break;
            case Global.ALBUM_REQUEST_CODE:
                Uri uri = date.getData();
                Drawable dra = Drawable.createFromPath(uri.getPath());
                if (dra==null) break;
                mainSkin.setBackground(dra);
                break;
        }
    }

    static class MyHandler extends Handler  {
        public static final int MEG_START_LISTENING = 1;
        public static final int MSG_STOP_LISTENING = 2;

        private SpeechRecognizer speechRecognizer;

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
                    ExtraProgramme programme = Global.saveProgramme(resultInfo);
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
}
