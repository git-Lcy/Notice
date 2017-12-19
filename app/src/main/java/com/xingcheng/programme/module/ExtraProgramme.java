package com.xingcheng.programme.module;

/**
 * Created by lfy on 2017/12/7.
 */

public class ExtraProgramme { //extends Programme {

    public boolean isSelected;

    private Programme mProgramme;

    public ExtraProgramme(Programme mProgramme){
        this.mProgramme = mProgramme;
        isSelected = false;
    }
    public Programme getProgramme(){
        return mProgramme;
    }

}
