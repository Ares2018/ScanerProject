package com.scaner.scaner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.zxing.Result;
import com.scaner.scaner.scaner.interfaces.OnDownLoadListener;
import com.scaner.scaner.scaner.utils.ImageScanerUtils;

import port.WebviewCBHelper;
import scanerhelp.WebviewUtils;

/**
 * Created by wanglinjie.
 * create time:2018/11/19  下午4:56
 */
public class WebViewImpl extends WebviewCBHelper {
    //如果不绑定对象，则属于正常的webview加载链接
    @Override
    public String getWebViewJsObject() {
        return "test";
    }

    //需要支持长按识别二维码
    @Override
    public boolean isNeedScanerImg() {
        return true;
    }

    @Override
    public void OnScanerImg(final String imgUrl, boolean isStream) {
        super.OnScanerImg(imgUrl, isStream);
        ImageScanerUtils imgUtils = ImageScanerUtils.get().setBeepId(R.raw.beep);
        if (!isStream) {
            if (imgUtils != null) {
                imgUtils.handleQRCodeFormBitmap(imgUrl, new OnDownLoadListener() {
                    @Override
                    public void onDownLoadImgSuccess(String path) {
                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        Result result = ImageScanerUtils.get().handleQRCodeFormBitmap(bitmap);
                        //链接是二维码
                        if (result != null) {
                            onLongClickCallBack(imgUrl, true);
                        } else {//不是二维码
                            onLongClickCallBack(imgUrl, false);
                        }

                    }

                    @Override
                    public void onDownLoadImgFail(String err) {
                        Log.e("WLJ", "WLJ,err=" + err);
                    }
                });
            }
        } else {
            imgUtils.handleQRCodeFormBitmap(WebviewUtils.get().getBitmap(imgUrl));
        }

    }

    @Override
    public void onLongClickCallBack(String imgUrl, boolean isScanerImg) {
        super.onLongClickCallBack(imgUrl, isScanerImg);
        if (isScanerImg) {
            //是二维码图片
        } else {//非二维码图片

        }
    }

}
