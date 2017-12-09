package com.longhuang.programme.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.longhuang.programme.module.ExtraProgramme;
import com.longhuang.programme.module.Programme;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2017/11/29.
 */

public class Global {
    public static final int CONFIG_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int ALBUM_REQUEST_CODE = 3;

    public static boolean VOICE_ENABLE;
    private static final Programme programme = new Programme();
    public static  List<ExtraProgramme> programmeList=new ArrayList<>();// 所有提醒列表
    public static  List<ExtraProgramme> programmeCache=new ArrayList<>();// 被选中提醒列表

    public static Programme getProgrammeInfo(){
        if (programmeCache.size()==0){
            programme.clear();

        }else {
            programme.setProgrammeInfo(programmeCache.get(0).getProgramme());
        }
        return programme;
    }

    public static Programme setProgrammeInfo(){
        Programme p;
        if (programmeCache.size()==0){
            p = new Programme();
            p.setProgrammeInfo(programme);
            saveProgramme(p);

        }else {
            p = programmeCache.get(0).getProgramme();
            p.setProgrammeInfo(programme);
        }
        p.save();
        return programme;
    }
    /**
     * 创建提醒对象 Programme
     * @param message 提醒信息
     *
     */
    public static Programme saveProgramme(String message){
        Programme programme = new Programme();
        programme.setMessage(message);
        saveProgramme(programme);
        programme.save();
        return programme;
    }

    // 查看
    public static Programme getCacheProgramme(){
        if (programmeCache.size()==0){
            return saveProgramme("提醒");
        }
        return programmeCache.get(0).getProgramme();
    }

    /**
     * 为新创建的 p 设置日期及唯一
     * @param p 要操作的对象
     */
    public static void saveProgramme(Programme p){
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 //       SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        p.setProgrammeId(String.valueOf(currentTime));
        p.setDate(format.format(date));
    }

    /*
     * 从数据库中获取所有提醒信息
     */
    public static void findAll(){
         List<Programme> programmes = DataSupport.findAll(Programme.class);
        if (programmes==null || programmes.size()==0) return;
        Collections.reverse(programmes);
        if (programmeList.size()>0) programmeList.clear();
        for (Programme p : programmes){
            programmeList.add(new ExtraProgramme(p));
        }

    }


    /**
     * 将设置界面添加的新提醒保存至数据库
     * @param extra 需要保存的提醒
     */
    public static  void editSave( ExtraProgramme extra){
        final Programme programme = extra.getProgramme();
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {

                programme.save();
            }
        });

    }
    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
 /*   public void startCropActivity(Uri uri) {
        UCrop.of(uri, mDestinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(512, 512)
                .withTargetActivity(CropActivity.class)
                .start(mActivity, this);
    }
    */

}
