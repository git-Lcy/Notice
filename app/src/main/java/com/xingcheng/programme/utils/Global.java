package com.xingcheng.programme.utils;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;

import com.xingcheng.programme.module.ExtraProgramme;
import com.xingcheng.programme.module.Programme;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2017/11/29.
 */

public class Global {
    public static final int CONFIG_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int ALBUM_REQUEST_CODE = 3;
    public static final int AUDIO_REQUEST_CODE = 4;
    public static final int PHOTO_REQUEST_CUT = 5;

    public static final int EDIT_SAVE = 0;
    public static final int EDIT_CANCEL = 1;
    public static final int EDIT_DELETE = 2;

    public static final String MUSIC_URL_NAME = "music_url";
    public static boolean VOICE_ENABLE;
    public static int index;
    public static Programme programme ;
    public static  List<ExtraProgramme> programmeList=new ArrayList<>();// 所有提醒列表
    public static  List<ExtraProgramme> programmeCache=new ArrayList<>();// 被选中提醒列表
    public static ArrayList<Integer> alarmRequestCodes = new ArrayList<>();
    public static String MUSIC_URL;


    public static Programme getEditProgramme(){
        if (programmeCache.size()==0){
            return programme;
        }
        Programme p = programmeCache.get(0).getProgramme();
        p.setProgrammeInfo(programme);
        index = programmeList.indexOf(programmeCache.get(0));
        return p;
    }


    /*
     * 从数据库中获取所有提醒信息
     */
    public static void findAll(){
         List<Programme> programmes = DataSupport.findAll(Programme.class);
        if (programmes==null || programmes.size()==0) return;
        Collections.reverse(programmes);

        if (alarmRequestCodes.size()!=0) alarmRequestCodes.clear();
        if (programmeList.size()>0) programmeList.clear();
        for (Programme p : programmes){
            programmeList.add(new ExtraProgramme(p));
            alarmRequestCodes.add(p.getRequestCode());
            L.e("Global"," ------ Programme id = "+p.getProgrammeId());
        }

    }


    public static boolean clearCache(){
        if (programmeCache.size()==0) return false;
        for (ExtraProgramme p : programmeCache){
            p.isSelected = false;
        }
        programmeCache.clear();
        return true;
    }


    @SuppressLint("SdCardPath")
    public static void saveBitmap(String path,Bitmap bitmap) {
        File tempFile = new File(path);
        if (tempFile.exists()) {
            tempFile.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
