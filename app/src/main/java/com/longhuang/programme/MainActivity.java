package com.longhuang.programme;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.longhuang.programme.Imp.MyRecognizerListener;
import com.longhuang.programme.Imp.MySynthesizerListener;
import com.longhuang.programme.module.Programme;
import com.longhuang.programme.utils.Global;
import com.longhuang.programme.utils.L;

import java.lang.ref.WeakReference;


public class MainActivity extends BaseActivity  {

    private RecyclerView recyclerView;
    private CheckBox switchInput;
    private EditText textInput;
    private Button voiceInput;
    private Button sendMessage;

    private ProgrammeAdapter adapter;
    private MyHandler handle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

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
            case R.id.skin:

                break;
            case R.id.date:

                break;
            case R.id.add:
                if (adapter == null) break;
                int type = adapter.editType();
                if (type == -1) break;
                Intent intent = new Intent(this,ProgrammeMenuActivity.class);
                intent.putExtra("EditType",type);
                startActivity(intent);

                break;
            case R.id.delete:
                if (adapter == null) break;
                adapter.deleteSelected();
                break;
        }
        return true;
    }
    private void initView(){
        recyclerView = (RecyclerView) findViewById(R.id.programme_list);
        switchInput = (CheckBox)findViewById(R.id.switch_input_mode);
        textInput = (EditText)findViewById(R.id.text_input);
        voiceInput = (Button) findViewById(R.id.voice_input);
        sendMessage = (Button) findViewById(R.id.send_message);

    }


    private void initEvent(){
        handle = new MyHandler(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProgrammeAdapter(this);
        recyclerView.setAdapter(adapter );
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
                Programme programme = Global.saveProgramme(message);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm !=null ){
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                adapter.insertProgramme(programme);
            }
        });
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
                    Programme programme = Global.saveProgramme(resultInfo);
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
