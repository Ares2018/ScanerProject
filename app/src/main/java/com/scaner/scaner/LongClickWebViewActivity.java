package com.scaner.scaner;

import android.os.Bundle;

import com.google.zxing.Result;
import com.scaner.scaner.scaner.utils.ImageScanerUtils;
import com.zjrb.core.common.base.BaseActivity;
import com.zjrb.core.common.manager.ThreadManager;
import com.zjrb.core.nav.Nav;
import com.zjrb.core.ui.widget.ZBWebView;
import com.zjrb.core.utils.webjs.LongClickCallBack;

/**
 * 长按webview二维码图片测试页面
 */
public class LongClickWebViewActivity extends BaseActivity implements LongClickCallBack {
    private ZBWebView webView;
    Result result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_click_web_view);
        webView = (ZBWebView) findViewById(R.id.web_view);
        webView.loadUrl("http://blog.csdn.net/lmj623565791/article/details/50709663");//加载页面
        webView.setLongClickCallBack(this);
    }


    @Override
    public void onLongClickCallBack(String imgUrl, boolean var2) {
        //使用线程池处理二维码扫描
        ThreadManager.ThreadPoolProxy pool = ThreadManager.getSinglePool();
        pool.execute(new ScanerRunner(imgUrl));
    }

    //异步处理扫描事宜
    private class ScanerRunner implements Runnable {
        private String imgUrl;

        public ScanerRunner(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        @Override
        public void run() {
            ImageScanerUtils imgUtils = ImageScanerUtils.get().setBeepId(R.raw.beep);
            if (imgUtils != null) {
                result = imgUtils.handleQRCodeFormBitmap(imgUrl);
                Nav.with(LongClickWebViewActivity.this).toPath(result.getText());
            }
        }
    }
}
