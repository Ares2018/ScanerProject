package com.scaner.scaner.scaner.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.scaner.scaner.scaner.R;
import com.scaner.scaner.scaner.interfaces.OnReScanerListener;
import com.zjrb.core.common.listener.ICancelListener;
import com.zjrb.core.utils.UIUtils;

/**
 * 自定义加载中Dialog 双击back键取消操作
 * 二维码扫描加载中
 *
 * @author wanglinjie
 * @date 18/5/4 11:15.
 */
public class ScanerLoadingDialog extends Dialog implements View.OnTouchListener {

    private TextView mTvToast, mTvScanerError;
    private ImageView mIvIcon;

    // 撤销提醒内容
    private String cancelText = "双击撤销!";

    private ICancelListener mCancelListener;
    private OnReScanerListener reScanerListener;

    public static final String TEXT_SUCCESS = "成功";
    public static final String TEXT_FAILURE = "失败";
    private static final int FINISH_DELAYED = 1200;

    public void setCancelListener(ICancelListener cancelListener) {
        this.mCancelListener = cancelListener;
    }

    public ScanerLoadingDialog(Context context) {
        super(context, R.style.confirm_dialog);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = UIUtils.getScreenW();
        params.height = UIUtils.getScreenH();
        window.setAttributes(params);
//        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // 清除背景变暗
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    /**
     * 立刻关闭
     */
    public void finish() {
        try {
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see #finish(boolean, String)
     */
    public void finish(boolean isSuccess) {
        finish(isSuccess, isSuccess ? TEXT_SUCCESS : TEXT_FAILURE);
    }

    /**
     * @see #finish(String, int)
     */
    public void finish(boolean isSuccess, String text) {
        finish(text, isSuccess
                ? R.mipmap.module_core_icon_loading_success
                : R.mipmap.module_core_icon_loading_failure);
    }

    /**
     * 设置厨师textView显示内容
     *
     * @param s
     */
    public void setToastText(String s) {
        if (mTvToast != null && !TextUtils.isEmpty(s)) {
            mTvToast.setText(s);
        }
    }

    /**
     * 显示结果，延迟 {@link #FINISH_DELAYED} 时间后关闭
     *
     * @param text  结果显示的文本
     * @param resId 结果显示的图标资源
     */
    public void finish(String text, @DrawableRes int resId) {
        mTvToast.setText(text);
        mIvIcon.setImageResource(resId);
        mTvToast.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, FINISH_DELAYED);
    }


    private View contentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contentView = View.inflate(getContext(),
                R.layout.module_scaner_loading_alert_dialog, null);

        mIvIcon = (ImageView) contentView.findViewById(R.id.iv_icon);
        mTvToast = (TextView) contentView.findViewById(R.id.tv_toast);
        mTvScanerError = (TextView) contentView.findViewById(R.id.tv_scaner_error);
        mIvIcon.setImageResource(R.mipmap.module_core_ic_scaner_loading);
        setContentView(contentView);

        setCanceledOnTouchOutside(false);

    }

    public ImageView getIvIcon() {
        return mIvIcon;
    }

    public TextView getTvToast() {
        return mTvToast;
    }

    public TextView getTvScanerError() {
        return mTvScanerError;
    }

    private long clickTime;

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//            if ((System.currentTimeMillis() - clickTime) > 1000) {
//                T.showShortNow(getContext(), cancelText);
//                clickTime = System.currentTimeMillis();
//                return true;
//            } else {
//                if (mCancelListener != null) {
//                    T.hideLast();
//                    mCancelListener.onCancel();
//                }
//            }
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!(event.getX() >= -10 && event.getY() >= -10 && mTvScanerError.getVisibility() == View.VISIBLE)
                    || event.getX() >= contentView.getWidth() + 10
                    || event.getY() >= contentView.getHeight() + 20) {//如果点击位置在当前View外部则销毁当前视图,其中10与20为微调距离
                reScanerListener.onReScaner();
                finish();
            }
        }
        return true;
    }
}
