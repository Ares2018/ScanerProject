package com.scaner.scaner.scaner.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.scaner.scaner.scaner.interfaces.OnDownLoadListener;

import java.util.Hashtable;

/**
 * 图片二维码解析工具类
 * Created by wanglinjie.
 * create time:2018/4/19  下午3:19
 */

final public class ImageScanerUtils {
    private int beepID = -1;
    private volatile static ImageScanerUtils instance;

    private ImageScanerUtils() {
    }

    public static ImageScanerUtils get() {
        if (instance == null) {
            synchronized (ImageScanerUtils.class) {
                if (instance == null) {
                    instance = new ImageScanerUtils();
                }
            }
        }
        return instance;
    }

    public ImageScanerUtils setBeepId(int id) {
        beepID = id;
        return this;
    }

    public int getBeepID() {
        return beepID;
    }

    /**
     * 耗时，校验二维码
     *
     * @param bitmap
     * @return
     */
    public Result handleQRCodeFormBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        //获取图片宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] data = new int[width * height];
        bitmap.getPixels(data, 0, width, 0, 0, width, height);
        //耗时
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();

        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        Result result = null;
        try {
            try {
                result = reader.decode(bitmap1, hints);
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }

        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 校验二维码,支持解析url链接
     * 先将图片进行下载
     *
     * @param imgUrl 图片地址
     * @return
     */
    public void handleQRCodeFormBitmap(String imgUrl, final OnDownLoadListener listener) {
        if (TextUtils.isEmpty(imgUrl) || !DownloadUtil.get().isHttpUrl(imgUrl)) return;
        downImg(imgUrl, listener);
    }

    /**
     * 下载图片
     *
     * @param imgSrc
     * @param listener
     */
    private void downImg(String imgSrc, final OnDownLoadListener listener) {
        try {
            DownloadUtil.get()
                    .setDir(PathUtil.getImagePath())
                    .setListener(new DownloadUtil.OnDownloadListener() {
                        @Override
                        public void onLoading(int progress) {

                        }

                        //下载成功
                        @Override
                        public void onSuccess(String path) {
                            if (!path.isEmpty()) {
                                listener.onDownLoadImgSuccess(path);
//                                result = null;
//                                Bitmap bitmap = BitmapFactory.decodeFile(path);
//                                result = handleQRCodeFormBitmap(bitmap);
                            }
                        }

                        //图片下载失败
                        @Override
                        public void onFail(String err) {
                            listener.onDownLoadImgFail(err);
                        }
                    })
                    .download(imgSrc);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
