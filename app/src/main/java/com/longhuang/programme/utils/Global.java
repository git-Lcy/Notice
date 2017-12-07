package com.longhuang.programme.utils;

import android.net.Uri;
import android.text.TextUtils;

import com.longhuang.programme.module.ExtraProgramme;
import com.longhuang.programme.module.Programme;

import org.litepal.crud.DataSupport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/11/29.
 */

public class Global {
    public static final int CONFIG_REQUEST_CODE = 1;
    public static final int CAMERA_REQUEST_CODE = 2;
    public static final int ALBUM_REQUEST_CODE = 3;

    public static boolean VOICE_ENABLE;
    private static  List<Programme> programmes=new ArrayList<>();
    public static  List<ExtraProgramme> programmeList=new ArrayList<>();
    public static  List<ExtraProgramme> programmeCache=new ArrayList<>();
    public static ExtraProgramme saveProgramme(String message){
        Programme programme = new Programme();
        programme.setMessage(message);
        saveProgramme(programme);
        programme.save();
        return new ExtraProgramme(programme);
    }
    public static void saveProgramme(Programme p){
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        p.setProgrammeId(String.valueOf(currentTime));
        p.setDate(format.format(date));
    }
    public static void findAll(){
         programmes = DataSupport.findAll(Programme.class);
        if (programmes==null || programmes.size()==0) return;
        Collections.reverse(programmes);
        if (programmeList.size()>0) programmeList.clear();
        for (Programme p : programmes){
            programmeList.add(new ExtraProgramme(p));
        }

    }
    public static Programme getChangeItem(ExtraProgramme extra){
        String id = extra.getProgrammeId();
        for (Programme p : programmes){
            if (TextUtils.equals(id,p.getProgrammeId())){
                return p;
            }
        }
        return new Programme(extra);
    }
    public static List<ExtraProgramme> findSelectedDateProgramme(String date){
        List<Programme> programmeList = DataSupport.where("date",date).find(Programme.class);
        if (programmeList==null) programmeList = new ArrayList<>();
        Collections.reverse(programmeList);
        List<ExtraProgramme> p = new ArrayList<>();
        for (Programme programme : programmeList){
            p.add(new ExtraProgramme(programme));
        }
        return p;
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
