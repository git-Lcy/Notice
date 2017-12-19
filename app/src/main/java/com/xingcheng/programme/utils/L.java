package com.xingcheng.programme.utils;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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

    @TargetApi(19)
    public static String handleImage(Context context, Intent data) {
        String imagePath = null;
        Uri uri = data.getData();

        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 通过document id来处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                // 解析出数字id
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(context,MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            }
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(context,contentUri, null);
            }
        }
        else if ("content".equals(uri.getScheme())) {
            // 如果不是document类型的Uri，则使用普通方式处理
            imagePath = getImagePath(context,uri, null);
        }
        return imagePath;
    }

    private static String getImagePath(Context context,Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实图片路径
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }

        return path;
    }
}
