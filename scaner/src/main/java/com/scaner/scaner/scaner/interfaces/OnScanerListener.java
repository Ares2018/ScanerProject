package com.scaner.scaner.scaner.interfaces;

import com.google.zxing.Result;

/**
 * @author wanglinjie
 * @date 2018/4/17
 * 扫描完成结果处理
 */

public interface OnScanerListener {
    void onSuccess(String type, Result result);

    void onFail(String type, String message);
}
