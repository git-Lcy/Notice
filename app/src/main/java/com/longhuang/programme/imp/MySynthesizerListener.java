package com.longhuang.programme.imp;


import android.os.Bundle;

import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SynthesizerListener;

/**
 * Created by Administrator on 2017/11/29.
 */

public class MySynthesizerListener implements SynthesizerListener {

    private OnSpeakCompleted mOnSpeakCompleted;

    public MySynthesizerListener(){}
    public MySynthesizerListener(OnSpeakCompleted completed){
        this.mOnSpeakCompleted = completed;
    }
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
        if (mOnSpeakCompleted!=null) mOnSpeakCompleted.onCompleted();
    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {}

    public interface OnSpeakCompleted{
        void onCompleted();
    }
}
