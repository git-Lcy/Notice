package com.longhuang.programme.module;

import android.text.TextUtils;

import org.litepal.crud.DataSupport;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/11/14.
 */

public class Programme extends DataSupport {

    public boolean isSelected;

    private boolean isExecuted; //是否已经执行

    private int repeatType; //重复类型 0：不重复、1：每天、2：每两天。。。

    private String message;//提醒信息

    private String description;//详细说明

    private boolean isVibrate;//震动

    private boolean isRinging;//铃声

    private String date;//日期

    private String time;//日期

    private String timestamp;//时间戳


    public boolean isExecuted() {
        return isExecuted;
    }

    public void setExecuted(boolean executed) {
        isExecuted = executed;
    }
    public int getRepeatType() {
        return repeatType;
    }

    public void setRepeat(int repeat) {
        repeatType = repeat;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVibrate(boolean vibrate) {
        isVibrate = vibrate;
    }

    public void setRinging(boolean ringing) {
        isRinging = ringing;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRingingUrl(String ringingUrl) {
        this.ringingUrl = ringingUrl;
    }

    public String getMessage() {

        return message;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVibrate() {
        return isVibrate;
    }

    public boolean isRinging() {
        return isRinging;
    }


    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getRingingUrl() {
        return ringingUrl;
    }

    private String ringingUrl;

    public Programme(){
    }

    public String getTimeInfo(){
        if(TextUtils.isEmpty(time)) return "";
        String date = "";
        if (repeatType == 0){
            date = this.date+" ";
        }
        if (repeatType == 1){
            date = "每天 ";
        }
        if (repeatType == 2){
            date = "每2天 ";
        }
        if (repeatType == 3){
            date = "每3天 ";
        }
        if (repeatType == 4){
            date = "每4天 ";
        }
        if (repeatType == 5){
            date = "每5天 ";
        }
        if (repeatType == 6){
            date = "每6天 ";
        }
        if (repeatType == 7){
            date = "每7天 ";
        }
        return date+time;
    }
}
