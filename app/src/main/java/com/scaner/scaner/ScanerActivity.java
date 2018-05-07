package com.scaner.scaner;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.scaner.scaner.scaner.CameraManager;
import com.scaner.scaner.scaner.CaptureActivityHandler;
import com.scaner.scaner.scaner.DecodeThread;
import com.scaner.scaner.scaner.decoding.InactivityTimer;
import com.scaner.scaner.scaner.utils.AnimationToolUtils;
import com.scaner.scaner.scaner.utils.QrBarToolUtils;
import com.zjrb.core.common.base.BaseActivity;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 二维码扫描测试页面
 * Created by wanglinjie.
 * create time:2018/4/23  上午10:16
 */
public class ScanerActivity extends BaseActivity {

    private InactivityTimer inactivityTimer;

    /**
     * 扫描处理
     */
    private CaptureActivityHandler handler;

    /**
     * 整体根布局
     */
    private RelativeLayout mContainer = null;

    /**
     * 扫描框根布局
     */
    private RelativeLayout mCropLayout = null;

    /**
     * 扫描边界的宽度
     */
    private int mCropWidth = 0;

    /**
     * 扫描边界的高度
     */
    private int mCropHeight = 0;

    /**
     * 是否有预览
     */
    private boolean hasSurface;

    /**
     * 闪光灯开启状态
     */
    private boolean mFlashing = true;

//    /**
//     * 闪光灯 按钮
//     */
//    private ImageView mIvLight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaner_code);
        //界面控件初始化
        initView();
        //权限初始化
        initPermission();
        //扫描动画初始化
        initScanerAnimation();
        //初始化 CameraManager
        CameraManager.init(getApplicationContext());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    private void initView() {
//        mIvLight = (ImageView) findViewById(R.id.top_mask);
        mContainer = (RelativeLayout) findViewById(R.id.capture_containter);
        mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);
    }

    /**
     * 需要打开相机权限
     */
    private void initPermission() {
        //请求Camera权限 与 文件读写 权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initScanerAnimation() {
        ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
        AnimationToolUtils.ScaleUpDowm(mQrLineView);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            //Camera初始化
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    if (!hasSurface) {
                        hasSurface = true;
                        initCamera(holder);
                    }
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    hasSurface = false;

                }
            });
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void setCropWidth(int cropWidth) {
        mCropWidth = cropWidth;
        CameraManager.FRAME_WIDTH = mCropWidth;

    }

    private void setCropHeight(int cropHeight) {
        this.mCropHeight = cropHeight;
        CameraManager.FRAME_HEIGHT = mCropHeight;
    }

    public void btn(View view) {
        int viewId = view.getId();
//        if (viewId == R.id.top_mask) {
//            light();
//        }
//        else if (viewId == R.id.top_back) {
//            finish();
//        }
//        else if (viewId == R.id.top_openpicture) {
//            //TODO 打开相册页面 默认选择一张图片
//            Nav.with(this)
//                    .toPath("/core/MediaSelectActivity", 10);
//        }
    }

//    /**
//     * 闪光灯管理
//     */
//    private void light() {
//        if (mFlashing) {
//            mFlashing = false;
//            // 开闪光灯
//            CameraManager.get().openLight();
//        } else {
//            mFlashing = true;
//            // 关闪光灯
//            CameraManager.get().offLight();
//        }
//
//    }

//    /**
//     * 解码线程
//     */
//    private DecodeThread decodeThread;

    /**
     * 初始化相机
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(surfaceHolder);
            Point point = CameraManager.get().getCameraResolution();
            AtomicInteger width = new AtomicInteger(point.y);
            AtomicInteger height = new AtomicInteger(point.x);
            int cropWidth = mCropLayout.getWidth() * width.get() / mContainer.getWidth();
            int cropHeight = mCropLayout.getHeight() * height.get() / mContainer.getHeight();
            setCropWidth(cropWidth);
            setCropHeight(cropHeight);
        } catch (IOException | RuntimeException ioe) {
            return;
        }

//        //开始解码
//        if (handler == null) {
//            handler = new CaptureActivityHandler(this);
//            handler.setFinishOig(inactivityTimer);
//            decodeThread = new DecodeThread(handler);
//            decodeThread.start();
//            handler.setDecodeThread(decodeThread);
//        }
    }

//    /**
//     * 打开相册后回调
//     *
//     * @param requestCode
//     * @param resultCode
//     * @param data
//     */
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK) {
//            ContentResolver resolver = getContentResolver();
//            // 照片的原始资源地址
//            Uri originalUri = data.getData();
//            try {
//                // 使用ContentProvider通过URI获取原始图片
//                Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);
//
//                // 开始对图像资源解码
//                Result rawResult = QrBarToolUtils.decodeFromPhoto(photo);
//                if (rawResult != null) {
//                    initDialogResult(rawResult);
//                } else {
//                    Toast.makeText(getApplicationContext(), "图片识别失败", Toast.LENGTH_SHORT).show();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * 解析后处理
//     *
//     * @param result
//     */
//    private void initDialogResult(Result result) {
//        BarcodeFormat type = result.getBarcodeFormat();
//        String realContent = result.getText();
//
//        if (BarcodeFormat.QR_CODE.equals(type)) {
//            Toast.makeText(getApplicationContext(), "二维码扫描结果:" + realContent, Toast.LENGTH_SHORT).show();
//        } else if (BarcodeFormat.EAN_13.equals(type)) {
//            Toast.makeText(getApplicationContext(), "条形码扫描结果:" + realContent, Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getApplicationContext(), "扫描结果:" + realContent, Toast.LENGTH_SHORT).show();
//        }
//        //支持重新扫描
//        if (handler != null) {
//            // 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
//            handler.sendEmptyMessage(R.id.restart_preview);
//        }
//
//    }

}