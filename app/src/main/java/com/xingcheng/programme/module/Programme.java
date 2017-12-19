package com.xingcheng.programme.module;

import com.xingcheng.programme.utils.Global;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/11/14.
 */

public class Programme extends DataSupport {

    private int requestCode;//添加闹钟请求码

    private String executeTime; //执行时间 long

    private int repeatType; //重复类型 0：不重复、1：每天

    private String message;//提醒信息

    private boolean isVibrate;//震动

    private boolean isRinging;//铃声

    private String date;//执行日期 2017-11-08

    private String time;//执行时间 13:18

    private String programmeId;//唯一标识 时间戳

    private String ringingUrl;

    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    public int getRepeatType() {
        return repeatType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String executeTime) {
        this.executeTime = executeTime;
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

    public boolean isVibrate() {
        return isVibrate;
    }

    public boolean isRinging() {
        return isRinging;
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public String getDate() {
        return date;
    }

    public String getRingingUrl() {
        return ringingUrl;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public Programme(){
        programmeId = String.valueOf(System.currentTimeMillis());
        isRinging = true;
        isVibrate = true;
        int i = 0;
        boolean result;
        do {
            result = Global.alarmRequestCodes.contains(i);
            if (result) i++;
        }while (result);
        requestCode = i;
        Global.alarmRequestCodes.add(i);

        ringingUrl = Global.MUSIC_URL;
    }

    public Programme(String message){
        this();
        this.message = message;

    }


    public void setProgrammeInfo(Programme p){
        requestCode = p.requestCode;
        repeatType = p.repeatType;
        message = p.message;
        ringingUrl = p.ringingUrl;
        executeTime = p.executeTime;
        isRinging = p.isRinging;
        isVibrate = p.isVibrate;
        date = p.date;
        time = p.time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("{ ")
                .append("requestCode : ").append(requestCode).append(" , ")
                .append("repeatType : ").append(repeatType).append(" , ")
                .append("message : ").append(message==null? "":message).append(" , ")
                .append("isVibrate : ").append(isVibrate).append(" , ")
                .append("isRinging : ").append(isRinging).append(" , ")
                .append("date : ").append(date == null ? "" : date).append(" , ")
                .append("time : ").append(time == null ? "" : time).append(" , ")
                .append("executeTime : ").append(executeTime == null ? "" : executeTime).append(" , ")
                .append("programmeId : ").append(programmeId==null ? "":programmeId).append(" , ")
                .append("ringingUrl : ").append(ringingUrl==null ? "":ringingUrl)
        .append(" }");
        return builder.toString();
    }

}
