package com.scaner.scaner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.google.zxing.Result;
import com.scaner.scaner.scaner.utils.ImageScanerUtils;
import com.zjrb.core.common.manager.ThreadManager;
import com.zjrb.core.ui.widget.ZBWebView;
import com.zjrb.core.utils.webjs.LongClickCallBack;

public class LongClickWebViewActivity extends AppCompatActivity implements LongClickCallBack {
    private ZBWebView webView;
    Result result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_click_web_view);
        webView = new ZBWebView(this, this);
        //这里借用翔哥的博客
        webView.loadUrl("http://blog.csdn.net/lmj623565791/article/details/50709663");//加载页面
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addContentView(webView, lp);
    }


    @Override
    public void onLongClickCallBack(String imgUrl) {
        //使用线程池
        ThreadManager.ThreadPoolProxy pool = ThreadManager.getSinglePool();
        pool.execute(new ScanerRunner(imgUrl));
    }

    public class ScanerRunner implements Runnable {
        private String imgUrl;

        public ScanerRunner(String imgUrl) {
            this.imgUrl = imgUrl;
        }

        @Override
        public void run() {
            ImageScanerUtils imgUtils = ImageScanerUtils.get();
            if (imgUtils != null) {
                result = imgUtils.handleQRCodeFormBitmap(imgUtils.getBitmap(imgUrl));
                Log.v("", "");

            }
        }
    }
}
