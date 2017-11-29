package com.longhuang.programme.utils;

import com.longhuang.programme.module.Programme;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/11/29.
 */

public class Global {
    public static boolean VOICE_ENABLE;

    public static Programme saveProgramme(String message){
        Programme programme = new Programme();
        programme.setMessage(message);
        saveProgramme(programme);
        programme.save();
        return programme;
    }
    public static void saveProgramme(Programme p){
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        p.setTimestamp(String.valueOf(currentTime));
        p.setDate(format.format(date));
    }
}
