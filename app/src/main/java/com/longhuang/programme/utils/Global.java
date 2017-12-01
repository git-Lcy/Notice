package com.longhuang.programme.utils;

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
    public static boolean VOICE_ENABLE;
    public static  List<Programme> programmeList=new ArrayList<>();
    public static  List<Programme> programmeCache;
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
        p.setProgrammeId(String.valueOf(currentTime));
        p.setDate(format.format(date));
    }
    public static void findAll(){
        List<Programme> list  = DataSupport.findAll(Programme.class);
        if (list==null || list.size()==0) return;
        Collections.reverse(list);
        if (programmeList.size()>0) programmeList.clear();
        programmeList.addAll(list);
    }
    public static List<Programme> findSelectedDateProgramme(String date){
        List<Programme> programmeList = DataSupport.where("date",date).find(Programme.class);
        if (programmeList==null) programmeList = new ArrayList<>();
        Collections.reverse(programmeList);
        return programmeList;
    }
}
