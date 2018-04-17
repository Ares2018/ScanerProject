package com.scaner.scaner.scaner.utils;

import android.app.Activity;
import android.util.Log;

import com.google.zxing.Result;
import com.scaner.scaner.scaner.decoding.InactivityTimer;

/**
 * Created by wanglinjie.
 * create time:2018/4/16  下午5:00
 */

public class ScanerHelpUtils {

    private InactivityTimer inactivityTimer;
    private boolean vibrate = true;
    private Result result;
    private Activity act;

    private ScanerHelpUtils(Builder builder) {
        inactivityTimer = builder.inactivityTimer;
        vibrate = builder.vibrate;
        result = builder.result;
        act = builder.act;
    }


    public void handleDecode(Builder builder) {
        inactivityTimer.onActivity();
        //扫描成功之后的振动与声音提示
        BeepToolUtils.playBeep(builder.act, vibrate);

        String result1 = result.getText();
        Log.v("二维码/条形码 扫描结果", result1);
//        if (mScanerListener == null) {
//            Toast.makeText(getApplicationContext(), result1, Toast.LENGTH_SHORT).show();
//            initDialogResult(result);
//        } else {
//            mScanerListener.onSuccess("From to Camera", result);
//        }
    }

    public static final class Builder {
        private InactivityTimer inactivityTimer;
        private boolean vibrate;
        private Result result;
        private Activity act;

        public Builder() {
        }

        public Builder inactivityTimer(InactivityTimer val) {
            inactivityTimer = val;
            return this;
        }

        public Builder vibrate(boolean val) {
            vibrate = val;
            return this;
        }

        public Builder result(Result val) {
            result = val;
            return this;
        }

        public Builder ctx(Activity val) {
            act = val;
            return this;
        }

        public ScanerHelpUtils build() {
            return new ScanerHelpUtils(this);
        }
    }
}
