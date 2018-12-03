package com.scaner.scaner.scaner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.zjrb.core.utils.PathUtil;

import java.util.Hashtable;

/**
 * 图片二维码解析工具类
 * Created by wanglinjie.
 * create time:2018/4/19  下午3:19
 */

public class ImageScanerUtils {

    private volatile static ImageScanerUtils instance;

    private ImageScanerUtils() {
    }

    public static ImageScanerUtils get() {
        if (instance == null) {
            synchronized (com.zjrb.core.utils.ImageScanerUtils.class) {
                if (instance == null) {
                    instance = new ImageScanerUtils();
                }
            }
        }
        return instance;
    }

    /**
     * 校验二维码
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
    private Result result;

    public Result handleQRCodeFormBitmap(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl) || !DownloadUtil.get().isHttpUrl(imgUrl)) return null;
        try {
            DownloadUtil.get()
                    .setDir(PathUtil.getImagePath())
                    .setListener(new DownloadUtil.OnDownloadListener() {
                        @Override
                        public void onLoading(int progress) {

                        }

                        @Override
                        public void onSuccess(String path) {
                            if (!path.isEmpty()) {
                                result = null;
                                Bitmap bitmap = BitmapFactory.decodeFile(path);
                                result = handleQRCodeFormBitmap(bitmap);
                            }
                        }

                        //图片下载失败
                        @Override
                        public void onFail(String err) {
                        }
                    })
                    .download(imgUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


}
