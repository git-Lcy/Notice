package com.longhuang.programme.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.longhuang.programme.BaseActivity;
import com.longhuang.programme.MainActivity;
import com.longhuang.programme.module.Programme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by lfy on 2017/11/25.
 */

public final class ActivityHandler extends Handler {
    public static final int MEG_START_LISTENING = 1;
    public static final int MSG_STOP_LISTENING = 2;

    public static final int MSG_PARSE_JSON= 5;

    public static final int TYPE_MAIN = 1;
    public static final int TYPE_EDIT = 2;

    private StringBuffer mBuffer;
    private SpeechRecognizer mRecognizer;
    private SpeechSynthesizer mSynthesizer;
    private SynthesizerListener mSynthesizerListener;
    private RecognizerListener mRecognizerListener;
    private int activityType;

    private WeakReference<BaseActivity> weakActivity;


    public ActivityHandler(final BaseActivity activity,int type,InitListener initListener){

        weakActivity = new WeakReference<>(activity);
        activityType = type;
        if (SpeechRecognizer.getRecognizer()==null){
            mRecognizer = SpeechRecognizer.createRecognizer(activity, initListener);
            mRecognizer.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT,"30000");
            mRecognizer.setParameter(SpeechConstant.VAD_BOS,"8000");
            mRecognizer.setParameter(SpeechConstant.VAD_EOS,"8000");
        }
        if (SpeechSynthesizer.getSynthesizer() ==null){
            mSynthesizer = SpeechSynthesizer.createSynthesizer(activity,initListener);
        }

        mRecognizerListener = new RecognizerListener(){

            @Override
            public void onVolumeChanged(int i, byte[] bytes) {}

            @Override
            public void onBeginOfSpeech() {}

            @Override
            public void onEndOfSpeech() {
                L.e("ActivityHandler","---------- onEndOfSpeech  ");
            }

            @Override
            public void onResult(RecognizerResult recognizerResult, boolean b) {
                if (mBuffer==null) mBuffer = new StringBuffer();

                final String result = recognizerResult.getResultString();
                if (TextUtils.isEmpty(result) || !b) return;
                L.e("ActivityHandler","---------- voiceResult = "+mBuffer);
                new Thread(){
                    @Override
                    public void run(){
                        JSONObject obj = null;
                        JSONObject ws = null;
                        JSONArray array = null;
                        try {
                            array = new JSONArray(result);
                            obj = new JSONObject(result);
                            ws = obj.getJSONObject("ws");
                            int length = array.length();
                            for (int i=0;i<length;i++){
                                JSONArray wsArray = array.getJSONObject(i).getJSONArray("ws");
                                int wsLength = wsArray.length();
                                for (int j=0;j<wsLength;j++){
                                    JSONArray cwArray = wsArray.getJSONObject(i).getJSONArray("cw");
                                    mBuffer.append(cwArray.getJSONObject(0).getString("w"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mBuffer.setLength(0);
                        }
                        sendEmptyMessage(MSG_PARSE_JSON);
                    }
                }.start();
            }

            @Override
            public void onError(SpeechError speechError) {
                SpeechSynthesizer.getSynthesizer().startSpeaking("对不起，我没听清楚",mSynthesizerListener);
            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {

            }
        };
        mSynthesizerListener = new SynthesizerListener() {
            @Override
            public void onSpeakBegin() {}

            @Override
            public void onBufferProgress(int i, int i1, int i2, String s) {}

            @Override
            public void onSpeakPaused() {}

            @Override
            public void onSpeakResumed() {}

            @Override
            public void onSpeakProgress(int i, int i1, int i2) {}

            @Override
            public void onCompleted(SpeechError speechError) {

            }

            @Override
            public void onEvent(int i, int i1, int i2, Bundle bundle) {}
        };

    }

    @Override
    public void handleMessage(Message msg){
        switch (msg.what){
            case MEG_START_LISTENING:
                if (weakActivity.get().isVoiceViewPressed())
                    mRecognizer.startListening(mRecognizerListener);
                break;
            case MSG_STOP_LISTENING:
                if (mRecognizer.isListening()){
                    mRecognizer.stopListening();
                }
                break;
            case MSG_PARSE_JSON:

                if (mBuffer.length()==0) {
                    SpeechSynthesizer.getSynthesizer().startSpeaking("对不起，我没听清楚",mSynthesizerListener);
                    return;
                }

                if(activityType==TYPE_MAIN){
                    Programme programme = new Programme();
                    programme.setMessage(mBuffer.toString());
                    programme.setTimestamp(String.valueOf(System.currentTimeMillis()));
                    programme.save();
                    ((MainActivity)weakActivity.get()).addProgramme(programme);
                }
                if(activityType == TYPE_EDIT){
                    //      ((MainActivity)weakActivity.get()).setMessage(result);
                }

                mRecognizer.cancel();
                mBuffer.setLength(0);
                break;
        }
    }


}