package com.scaner.scaner.scaner.interfaces;

/**
 * 图片下载接口，暴露给外界调用
 * Created by wanglinjie.
 * create time:2018/12/4  下午2:11
 */
public interface OnDownLoadListener {
    void onDownLoadImgSuccess(String path);

    void onDownLoadImgFail(String err);
}
