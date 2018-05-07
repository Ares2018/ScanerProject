package com.scaner.scaner.scaner;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.google.zxing.Result;
import com.scaner.scaner.scaner.decoding.InactivityTimer;
import com.scaner.scaner.scaner.interfaces.OnScanerListener;
import com.scaner.scaner.scaner.utils.BeepToolUtils;


/**
 * @author wanglinjie
 * @date 2018/4/17
 * 扫描消息转发
 */
public final class CaptureActivityHandler extends Handler {

    DecodeThread decodeThread = null;
    Activity activity = null;
    private OnScanerListener listener;

    private State state;

    public void setListener(OnScanerListener listener) {
        this.listener = listener;
    }

    public CaptureActivityHandler(Activity activity) {
        this.activity = activity;
        state = State.SUCCESS;
    }

    public void setDecodeThread(DecodeThread decodeThread) {
        this.decodeThread = decodeThread;
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    /**
     * 收到扫码消息
     *
     * @param message
     */
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
            inactivityTimer.onActivity();
            //扫描成功之后的振动与声音提示
            BeepToolUtils.playBeep(activity, true);
            Toast.makeText(activity.getApplicationContext(), ((Result) message.obj).getText(), Toast.LENGTH_SHORT).show();
            if (message.obj != null) {
                listener.onSuccess((Result) message.obj);
            }
        } else if (message.what == R.id.decode_failed) {
            //继续扫描
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
        }
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
