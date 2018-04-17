package com.scaner.scaner.scaner.interfaces;

import com.google.zxing.Result;

/**
 * 扫描结果
 * @author Vondear
 * @date 2017/9/22
 */

public interface OnScanerListener {
    void onSuccess(String type, Result result);

    void onFail(String type, String message);
}
