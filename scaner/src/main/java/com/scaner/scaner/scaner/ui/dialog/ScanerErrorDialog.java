package com.scaner.scaner.scaner.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.scaner.scaner.scaner.R;
import com.zjrb.core.utils.UIUtils;


/**
 * 二维码扫描失败对话框
 *
 * @author wanglinjie
 * @date 2018/5/4 14:30.
 */
public class ScanerErrorDialog extends android.app.AlertDialog implements View.OnClickListener {

    private View view;

    private Button btnOk;
    private OnClickCallback mOnClickCallback;

    public ScanerErrorDialog(Context context) {
        super(context, android.R.style.Theme_Dialog);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);
        configDialog();
    }

    private void configDialog() {
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = UIUtils.dip2px(170);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);

        //设置对话框居中
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //因为某些机型是虚拟按键的,所以要加上以下设置防止挡住按键.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }


    private void initView() {
        view = LayoutInflater.from(getContext()).inflate(
                R.layout.module_scaner_dialog_scaner_error, null);
        btnOk = (Button) view.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_ok) {
            if (mOnClickCallback != null) {
                mOnClickCallback.onOkClick(v);
            }
        }
        dismiss();
    }


    public interface OnClickCallback {

        void onOkClick(View v);

    }

}
