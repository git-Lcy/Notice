package com.xingcheng.programme;

import com.iflytek.cloud.SpeechUtility;

/**
 *
 * Created by lfy on 2017/11/25.
 */

public class Application extends org.litepal.LitePalApplication{
    private static final String APP_URL = "appid=5a16c90a";
    @Override
    public void onCreate(){
        super.onCreate();
        SpeechUtility.createUtility(Application.this, APP_URL);
    }
}
