package com.scaner.scaner.scaner.utils;

import android.os.Environment;

import java.io.File;

/**
 * 路径工具类
 * Created by wangzhen on 2016/11/18.
 */

public class PathUtil {
    private static String APP_NAME = "news";
    //图片路径
    private static String PATH_IMAGE = APP_NAME + "/images";
    //头像路径
    private static String PATH_AVATAR = APP_NAME + "/images/avatar";
    //文件下载路径
    private static String PATH_DOWNLOAD = APP_NAME + "/download";

    /**
     * 获取图片存储路径
     *
     * @return 返回路径字符串 不包含文件名
     */
    public static String getImagePath() {
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + PATH_IMAGE);
        if (!path.exists()) {
            path.mkdirs();
        }
        return path.getAbsolutePath();
    }

    /**
     * 获取头像图片存储路径
     *
     * @return 返回路径字符串 不包含文件名
     */
    public static String getAvatarPath() {
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + PATH_AVATAR);
        if (!path.exists()) {
            path.mkdirs();
        }
        return path.getAbsolutePath();
    }

    /**
     * 获取下载存储路径
     *
     * @return
     */
    public static String getDownloadPath() {
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + PATH_DOWNLOAD);
        if (!path.exists()) {
            path.mkdirs();
        }
        return path.getAbsolutePath();
    }
}
