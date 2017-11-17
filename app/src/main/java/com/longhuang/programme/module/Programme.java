package com.longhuang.programme.module;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/11/14.
 */

public class Programme  {

    private String message;//提醒信息

    private String description;//详细说明

    private boolean isVibrate;//震动

    private boolean isRinging;//铃声

    private boolean isPilotLamp;//指示灯

    private String time;//提醒时间

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

    public void setPilotLamp(boolean pilotLamp) {
        isPilotLamp = pilotLamp;
    }

    public void setTime(String time) {
        this.time = time;
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

    public boolean isPilotLamp() {
        return isPilotLamp;
    }

    public String getTime() {
        return time;
    }

    public String getRingingUrl() {
        return ringingUrl;
    }

    private String ringingUrl;

    public Programme(){
    }

}
