package com.scaner.scaner.scaner;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.zxing.Result;
import com.scaner.scaner.scaner.decoding.InactivityTimer;
import com.scaner.scaner.scaner.utils.BeepToolUtils;


/**
 * @author vondear
 *         描述: 扫描消息转发
 */
public final class CaptureActivityHandler extends Handler {

    DecodeThread decodeThread = null;
    Activity activity = null;
    private State state;

    public CaptureActivityHandler(Activity activity) {
        this.activity = activity;
        state = State.SUCCESS;
    }

    public void setDecodeThread(DecodeThread decodeThread) {
        this.decodeThread = decodeThread;
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.auto_focus) {
            if (state == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }
        } else if (message.what == R.id.restart_preview) {
            restartPreviewAndDecode();
        } else if (message.what == R.id.decode_succeeded) {
            state = State.SUCCESS;
//            activity.handleDecode((Result) message.obj);// 解析成功，回调
            inactivityTimer.onActivity();
            //扫描成功之后的振动与声音提示
            BeepToolUtils.playBeep(activity, true);
            //TODO WLJ 打印扫描结果
            Toast.makeText(activity.getApplicationContext(), ((Result) message.obj).getText(), Toast.LENGTH_SHORT).show();

        } else if (message.what == R.id.decode_failed) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        }
        //TODO  WLJ
        // 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
//        sendEmptyMessage(R.id.restart_preview);

    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
        removeMessages(R.id.decode);
        removeMessages(R.id.auto_focus);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
        }
    }

    private enum State {
        PREVIEW, SUCCESS, DONE
    }

    private InactivityTimer inactivityTimer;

    /**
     * 设置扫描后参数
     */
    public void setFinishOig(InactivityTimer inactivityTimer) {
        this.inactivityTimer = inactivityTimer;
    }

}
