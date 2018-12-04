package com.scaner.scaner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.commonwebview.webview.CommonWebView;
import com.google.zxing.Result;

/**
 * 长按webview二维码图片测试页面
 */
public class LongClickWebViewActivity extends AppCompatActivity {
    private CommonWebView webView;
    private WebViewImpl webImpl;
    Result result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_long_click_web_view);
        webView = findViewById(R.id.web_view);
        //绑定对象
        webImpl = new WebViewImpl();
        webImpl.setJsObject(new JsInterfaceImp(webImpl.getWebViewJsObject()));
        webView.setHelper(webImpl);

        webView.loadUrl("http://blog.csdn.net/lmj623565791/article/details/50709663");//加载页面
    }


}
