package com.xingcheng.programme.utils;


import com.xingcheng.programme.module.ExtraProgramme;
import com.xingcheng.programme.module.Programme;

import org.litepal.crud.DataSupport;

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
