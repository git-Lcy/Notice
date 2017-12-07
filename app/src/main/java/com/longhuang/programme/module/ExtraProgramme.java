package com.longhuang.programme.module;

/**
 * Created by lfy on 2017/12/7.
 */

public class ExtraProgramme extends Programme {

    public boolean isSelected;

    public ExtraProgramme(){}
    public ExtraProgramme(Programme mProgramme){
        super(mProgramme);
        isSelected = false;
    }

}
