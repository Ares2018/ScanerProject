package com.scaner.scaner.scaner.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scaner.scaner.scaner.R;
import com.scaner.scaner.scaner.R2;
import com.zjrb.core.common.base.BaseActivity;
import com.zjrb.core.common.base.toolbar.TopBarFactory;
import com.zjrb.core.common.global.IKey;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 二维码扫描结果页面
 * Created by wanglinjie.
 * create time:2018/4/23  上午10:16
 */
public class ScanerResultActivity extends BaseActivity {

    @BindView(R2.id.tv_text_result)
    TextView tvTextResult;

    private String scanerText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaner_result);
        ButterKnife.bind(this);
        initArgs(savedInstanceState);
        setResultText();
    }


    @Override
    protected View onCreateTopBar(ViewGroup view) {
        return TopBarFactory.createScanerResultTopBar(view, this, "扫描结果").getView();
    }


    /**
     * 获取传递数据
     *
     * @param savedInstanceState
     */
    private void initArgs(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            scanerText = savedInstanceState.getString(IKey.SCANER_TEXT);
        } else {
            Intent intent = this.getIntent();
            scanerText = intent.getStringExtra(IKey.SCANER_TEXT);
        }

    }

//    /**
//     * @param intent 获取传递数据
//     */
//    private void getIntentData(Intent intent) {
//        if (intent != null) {
//            Uri data = intent.getData();
//            if (data != null) {
//                if (data.getQueryParameter(IKey.SCANER_TEXT) != null) {
//                    scanerText = data.getQueryParameter(IKey.SCANER_TEXT);
//                }
//            }
//
//
//        }
//    }

    /**
     * 显示扫描结果
     */
    private void setResultText() {
        if (!TextUtils.isEmpty(scanerText)) {
            tvTextResult.setText(scanerText);
        }
    }


}