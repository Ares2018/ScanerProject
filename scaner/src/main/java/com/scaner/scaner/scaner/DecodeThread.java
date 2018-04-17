package com.scaner.scaner.scaner;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CountDownLatch;

/**
 * @author Vondear
 *         描述: 解码线程
 */
public final class DecodeThread extends Thread {

    private final CountDownLatch handlerInitLatch;
    CaptureActivityHandler mHandler;
    private Handler handler;

    public DecodeThread(CaptureActivityHandler mHandler) {
        this.mHandler = mHandler;
        handlerInitLatch = new CountDownLatch(1);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(mHandler);
        handlerInitLatch.countDown();
        Looper.loop();
    }

}
