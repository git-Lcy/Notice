package com.longhuang.programme.utils;

import com.longhuang.programme.module.Programme;

import java.util.Comparator;

/**
 * Created by Administrator on 2017/11/20.
 */

public class ProgrammeComparator implements Comparator<Programme> {
    @Override
    public int compare(Programme o1, Programme o2) {
        if (o1.isExecuted() && !o2.isExecuted()){
            return -1;
        }
        if (!o1.isExecuted() && o2.isExecuted()){
            return 1;
        }
        return 0;
    }
}
