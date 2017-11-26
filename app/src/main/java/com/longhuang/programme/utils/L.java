package com.longhuang.programme.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by lfy on 2017/11/19.
 */

public class L {
    private static final boolean DEBUG = true;
    private static Toast toast;

    public static void e(String TAG,String info){
        if (DEBUG)Log.e(TAG,info);
    }
    public static void d(String TAG,String info){
        if (DEBUG)Log.d(TAG,info);
    }

    public static void toast(Context context,String info){
        if (toast==null) {
            toast = Toast.makeText(context,info,Toast.LENGTH_SHORT);
        }else {
            toast.setText(info);
        }
        toast.show();
    }
}
