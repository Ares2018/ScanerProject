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
import android.widget.LinearLayout;
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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author vondear
 */
public class ScanerActivity extends Activity {

    /**
     * 扫描结果监听
     */
//    private OnScanerListener mScanerListener;

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

//    /**
//     * 扫描成功后是否震动
//     */
//    private boolean vibrate = true;

    /**
     * 闪光灯开启状态
     */
    private boolean mFlashing = true;

    /**
     * 生成二维码 & 条形码 布局
     */
    private LinearLayout mLlScanHelp;

    /**
     * 闪光灯 按钮
     */
    private ImageView mIvLight;

//    /**
//     * 扫描结果显示框
//     */
//    private ScanerDialogSure rxDialogSure;

//    /**
//     * 设置扫描信息回调
//     */
//    public void setScanerListener(OnScanerListener scanerListener) {
//        mScanerListener = scanerListener;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        RxBarTool.setNoTitle(this);
        setContentView(R.layout.activity_scaner_code);
//        RxBarTool.setTransparentStatusBar(this);
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

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        SurfaceView surfaceView = findViewById(R.id.capture_preview);
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
//        mScanerListener = null;
        super.onDestroy();
    }

    private void initView() {
        mIvLight = findViewById(R.id.top_mask);
        mContainer = findViewById(R.id.capture_containter);
        mCropLayout = findViewById(R.id.capture_crop_layout);
        mLlScanHelp = findViewById(R.id.ll_scan_help);


    }

    private void initPermission() {
        //请求Camera权限 与 文件读写 权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void initScanerAnimation() {
        ImageView mQrLineView = findViewById(R.id.capture_scan_line);
        AnimationToolUtils.ScaleUpDowm(mQrLineView);
    }

    public int getCropWidth() {
        return mCropWidth;
    }

    public void setCropWidth(int cropWidth) {
        mCropWidth = cropWidth;
        CameraManager.FRAME_WIDTH = mCropWidth;

    }

    public int getCropHeight() {
        return mCropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.mCropHeight = cropHeight;
        CameraManager.FRAME_HEIGHT = mCropHeight;
    }

    public void btn(View view) {
        int viewId = view.getId();
        if (viewId == R.id.top_mask) {
            light();
        } else if (viewId == R.id.top_back) {
            finish();
        } else if (viewId == R.id.top_openpicture) {
//            RxPhotoTool.openLocalImage(mContext);
        }
    }

    private void light() {
        if (mFlashing) {
            mFlashing = false;
            // 开闪光灯
            CameraManager.get().openLight();
        } else {
            mFlashing = true;
            // 关闪光灯
            CameraManager.get().offLight();
        }

    }

    private DecodeThread decodeThread;
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

        if (handler == null) {
            handler = new CaptureActivityHandler(this);
            handler.setFinishOig(inactivityTimer);
            decodeThread = new DecodeThread(handler);
            decodeThread.start();
            handler.setDecodeThread(decodeThread);
        }
    }
    //========================================打开本地图片识别二维码 end=================================

    //--------------------------------------打开本地图片识别二维码 start---------------------------------
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ContentResolver resolver = getContentResolver();
            // 照片的原始资源地址
            Uri originalUri = data.getData();
            try {
                // 使用ContentProvider通过URI获取原始图片
                Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);

                // 开始对图像资源解码
                Result rawResult = QrBarToolUtils.decodeFromPhoto(photo);
                if (rawResult != null) {
//                    if (mScanerListener == null) {
                        initDialogResult(rawResult);
//                    } else {
//                        mScanerListener.onSuccess("From to Picture", rawResult);
//                    }
                } else {
//                    if (mScanerListener == null) {
                        Toast.makeText(getApplicationContext(), "图片识别失败", Toast.LENGTH_SHORT).show();
//                    } else {
//                        mScanerListener.onFail("From to Picture", "图片识别失败");
//                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //==============================================================================================解析结果 及 后续处理 end

    private void initDialogResult(Result result) {
        BarcodeFormat type = result.getBarcodeFormat();
        String realContent = result.getText();

//        if (rxDialogSure == null) {
//            //提示弹窗
//            rxDialogSure = new RxDialogSure(mContext);
//        }

        if (BarcodeFormat.QR_CODE.equals(type)) {
            Toast.makeText(getApplicationContext(), "二维码扫描结果:" + realContent, Toast.LENGTH_SHORT).show();
        } else if (BarcodeFormat.EAN_13.equals(type)) {
            Toast.makeText(getApplicationContext(), "条形码扫描结果:" + realContent, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "扫描结果:" + realContent, Toast.LENGTH_SHORT).show();
        }
        //支持重新扫描
        if (handler != null) {
            // 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
            handler.sendEmptyMessage(R.id.restart_preview);
        }

//        rxDialogSure.setContent(realContent);
//        rxDialogSure.setSureListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                rxDialogSure.cancel();
//            }
//        });
//        rxDialogSure.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                if (handler != null) {
//                    // 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
//                    handler.sendEmptyMessage(R.id.restart_preview);
//                }
//            }
//        });
//
//        if (!rxDialogSure.isShowing()) {
//            rxDialogSure.show();
//        }
//
//        RxSPTool.putContent(mContext, RxConstants.SP_SCAN_CODE, RxDataTool.stringToInt(RxSPTool.getContent(mContext, RxConstants.SP_SCAN_CODE)) + 1 + "");
    }

//    //扫描结果
//    public void handleDecode(Result result) {
//        inactivityTimer.onActivity();
//        //扫描成功之后的振动与声音提示
//        BeepToolUtils.playBeep(this, vibrate);
//
//        String result1 = result.getText();
//        Log.v("二维码/条形码 扫描结果", result1);
//        if (mScanerListener == null) {
//            Toast.makeText(getApplicationContext(), result1, Toast.LENGTH_SHORT).show();
//            initDialogResult(result);
//        } else {
//            mScanerListener.onSuccess("From to Camera", result);
//        }
//    }
//
//    public Handler getHandler() {
//        return handler;
//    }

}