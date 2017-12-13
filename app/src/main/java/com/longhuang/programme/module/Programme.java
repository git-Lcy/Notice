package com.longhuang.programme.module;

import android.text.TextUtils;

import org.litepal.crud.DataSupport;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/11/14.
 */

public class Programme extends DataSupport {


    private boolean isExecuted; //是否已经执行

    private int repeatType; //重复类型 0：不重复、1：每天

    private String message;//提醒信息

    private String description;//详细说明

    private boolean isVibrate;//震动

    private boolean isRinging;//铃声

    private String date;//创建日期 2017-11-08

    private String executeTime;//执行时间 2017-11-08 13:18

    private String programmeId;//唯一标识 时间戳

    private String ringingUrl;

    public String getExecuteTime() {
        return executeTime;
    }

    public void setExecuteTime(String time) {
        this.executeTime = time;
    }
    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public void setExecuted(boolean executed) {
        isExecuted = executed;
    }
    public int getRepeatType() {
        return repeatType;
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

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getDate() {
        return date;
    }

    public String getRingingUrl() {
        return ringingUrl;
    }


    public Programme(){
    }
    public void setProgrammeInfo(Programme p){
        repeatType = p.repeatType;
        message = p.message;
        description = p.description;
        isVibrate = p.isVibrate;
        isRinging = p.isRinging;
        executeTime = p.executeTime;
        ringingUrl = p.ringingUrl;
    }
    public void clear(){
        repeatType = 0;
        message = "";
        description = "";
        isVibrate = false;
        isRinging = false;
        executeTime = "";
        ringingUrl = "";
        programmeId = "";
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("{ ")
                .append("isExecuted : ").append(isExecuted).append(" , ")
                .append("repeatType : ").append(repeatType).append(" , ")
                .append("message : ").append(message==null? "":message).append(" , ")
                .append("isVibrate : ").append(isVibrate).append(" , ")
                .append("isRinging : ").append(isRinging).append(" , ")
                .append("date : ").append(date == null ? "" : date).append(" , ")
                .append("executeTime : ").append(executeTime==null ? "":executeTime).append(" , ")
                .append("programmeId : ").append(programmeId==null ? "":programmeId).append(" , ")
                .append("ringingUrl : ").append(ringingUrl==null ? "":ringingUrl);

        return builder.toString();
    }

}
