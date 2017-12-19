package com.xingcheng.programme.imp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechError;
import com.xingcheng.programme.utils.L;
import com.xingcheng.programme.utils.ThreadPoolManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/11/29.
 */

public class MyRecognizerListener implements RecognizerListener {
    public static final int MSG_PARSE_JSON= 5;
    public static final int MSG_PARSE_ERR= 8;

    private JSONArray jsonArray;
    private Handler handler;
    private StringBuffer mBuffer;
    public MyRecognizerListener(Handler handler){
        this.handler = handler;
        mBuffer = new StringBuffer();
        jsonArray = new JSONArray();
    }
    @Override
    public void onVolumeChanged(int i, byte[] bytes) {}

    @Override
    public void onBeginOfSpeech() {}

    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onResult(RecognizerResult recognizerResult, boolean b) {
        final String json = recognizerResult.getResultString();
        L.e("ActivityHandler","---------- b = "+b);
        L.e("ActivityHandler","---------- voiceResult = "+json);
        if (!b && TextUtils.isEmpty(json)) return;
        JSONObject object = null;
        try {
            object = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        jsonArray.put(object);
        if (b) ThreadPoolManager.getInstance().addTask(jsonRunnable);
    }


    @Override
    public void onError(SpeechError speechError) {
        handler.sendEmptyMessage(MSG_PARSE_ERR);
    }

    @Override
    public void onEvent(int i, int i1, int i2, Bundle bundle) {}

    private Runnable jsonRunnable = new Runnable() {
        @Override
        public void run() {
            JSONObject obj = null;
            JSONArray ws = null;
            JSONArray array = null;
            int jsonLength = jsonArray.length();
            try {
                for (int i=0;i<jsonLength;i++){
                    ws = jsonArray.getJSONObject(i).getJSONArray("ws");
                    int wsLength = ws.length();
                    for (int j=0;j<wsLength;j++){
                        JSONArray cwArray = ws.getJSONObject(j).getJSONArray("cw");
                        mBuffer.append(cwArray.getJSONObject(0).getString("w"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mBuffer.setLength(0);
                return;
            }
            if (mBuffer.length() == 0){
                handler.sendEmptyMessage(MSG_PARSE_ERR);
            }else {
                Message msg = handler.obtainMessage(MSG_PARSE_JSON,mBuffer.toString());
                msg.sendToTarget();
            }
            mBuffer.setLength(0);
           jsonArray = new JSONArray();
        }
    };
}
