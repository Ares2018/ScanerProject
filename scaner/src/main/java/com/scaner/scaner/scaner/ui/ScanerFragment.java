package com.scaner.scaner.scaner.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.Result;
import com.scaner.scaner.scaner.CameraManager;
import com.scaner.scaner.scaner.CaptureActivityHandler;
import com.scaner.scaner.scaner.DecodeThread;
import com.scaner.scaner.scaner.R;
import com.scaner.scaner.scaner.decoding.InactivityTimer;
import com.scaner.scaner.scaner.interfaces.OnReScanerListener;
import com.scaner.scaner.scaner.interfaces.OnScanerListener;
import com.scaner.scaner.scaner.ui.dialog.ScanerErrorDialog;
import com.scaner.scaner.scaner.ui.dialog.ScanerLoadingDialog;
import com.scaner.scaner.scaner.utils.AnimationToolUtils;
import com.scaner.scaner.scaner.utils.QrBarToolUtils;
import com.zjrb.core.common.base.BaseFragment;
import com.zjrb.core.domain.MediaEntity;
import com.zjrb.core.nav.Nav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 二维码扫描fragment
 * Created by wanglinjie.
 * create time:2018/5/3  上午9:24
 */

public class ScanerFragment extends BaseFragment implements OnScanerListener, ScanerErrorDialog.OnClickCallback, OnReScanerListener {

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
     * 扫描动画布局
     */
    private ImageView mQrLineView = null;

    private SurfaceView surfaceView = null;

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
     * 私有构造器
     */
    private void ScanerFragment() {

    }

    /**
     * 创建实例
     *
     * @return 实例对象
     */
    public static ScanerFragment newInstance() {
        ScanerFragment fragment = new ScanerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.activity_scaner_code, container, false);
        initView(v);
        //扫描动画初始化
        initScanerAnimation(v);
        //初始化 CameraManager
        CameraManager.init(getContext().getApplicationContext());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(getActivity());
        return v;
    }


    private void initView(View v) {
        mContainer = (RelativeLayout) v.findViewById(R.id.capture_containter);
        mCropLayout = (RelativeLayout) v.findViewById(R.id.capture_crop_layout);
        mQrLineView = (ImageView) v.findViewById(R.id.capture_scan_line);
        surfaceView = (SurfaceView) v.findViewById(R.id.capture_preview);
    }

    /**
     * 初始化扫描动画
     *
     * @param v
     */
    private void initScanerAnimation(View v) {
        AnimationToolUtils.ScaleUpDowm(mQrLineView);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
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
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        dismissDialog();
    }


    private void setCropWidth(int cropWidth) {
        mCropWidth = cropWidth;
        CameraManager.FRAME_WIDTH = mCropWidth;

    }

    private void setCropHeight(int cropHeight) {
        this.mCropHeight = cropHeight;
        CameraManager.FRAME_HEIGHT = mCropHeight;
    }

    /**
     * 解码线程
     */
    private DecodeThread decodeThread;
    private ScanerLoadingDialog scanerLoadingDialog;

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

        //开始解码
        if (handler == null) {
            handler = new CaptureActivityHandler(getActivity());
            handler.setListener(this);
            handler.setFinishOig(inactivityTimer);
            decodeThread = new DecodeThread(handler);
            decodeThread.start();
            handler.setDecodeThread(decodeThread);
        }
    }

    private Uri originalUri;

    /**
     * 打开相册后回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            scanerLoadingDialog = new ScanerLoadingDialog(getContext());
            scanerLoadingDialog.show();
            ContentResolver resolver = getActivity().getContentResolver();
            // 照片的原始资源地址

            if (data != null) {
                ArrayList<MediaEntity> list = data.getParcelableArrayListExtra("key_data");
                if (list != null && !list.isEmpty()) {
                    originalUri = list.get(0).getUri();
                }
            }

            try {
                // 使用ContentProvider通过URI获取原始图片
                if (originalUri != null) {
                    Bitmap photo = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                    // 开始对图像资源解码
                    Result rawResult = QrBarToolUtils.decodeFromPhoto(photo);
                    if (rawResult != null) {
                        initDialogResult(rawResult);
                    } else {
                        setErrDialogStat(scanerLoadingDialog);
                    }
                } else {
                    setErrDialogStat(scanerLoadingDialog);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 图片扫码错误状态显示
     *
     * @param scanerLoadingDialog
     */
    private void setErrDialogStat(ScanerLoadingDialog scanerLoadingDialog) {
        if (scanerLoadingDialog != null) {
            scanerLoadingDialog.getIvIcon().setVisibility(View.GONE);
            scanerLoadingDialog.getTvScanerError().setVisibility(View.VISIBLE);
            scanerLoadingDialog.getTvScanerError().setText("未找到二维码");
            scanerLoadingDialog.getTvToast().setText("轻触屏幕继续扫描");
        }
    }

    /**
     * 解析后处理
     *
     * @param result
     */
    private void initDialogResult(Result result) {
        if (result.getText().startsWith("http") || result.getText().startsWith("https")) {
            Nav.with(this).toPath(result.getText());//文本
        } else {
            Nav.with(this).toPath("/ui/ScanerResultActivity");
        }
        //支持重新扫描
        if (handler != null) {
            // 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
            handler.sendEmptyMessage(R.id.restart_preview);
        }

    }

    /**
     * 二维码扫描失败弹出dialog点击确定按钮
     *
     * @param v
     */
    @Override
    public void onOkClick(View v) {
        if (scanerLoadingDialog != null && scanerLoadingDialog.isShowing()) {
            scanerLoadingDialog.dismiss();
        }
        if (handler != null) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }

    /**
     * 扫描成功
     *
     * @param result
     */
    @Override
    public void onSuccess(Result result) {
        scanerLoadingDialog.dismiss();
        //链接
        if (result.getText().startsWith("http") || result.getText().startsWith("https")) {
            Nav.with(this).toPath(result.getText());//文本
        } else {
            Nav.with(this).toPath("/ui/ScanerResultActivity");
        }
        if (handler != null) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }

    private ScanerErrorDialog errDialog;

    /**
     * 扫描失败
     */
    @Override
    public void onFail() {
        errDialog = new ScanerErrorDialog(getContext());
        errDialog.show();
    }

    /**
     * 关闭dialog
     */
    private void dismissDialog() {
        if (scanerLoadingDialog != null && scanerLoadingDialog.isShowing()) {
            scanerLoadingDialog.dismiss();
        }
        if (errDialog != null && errDialog.isShowing()) {
            errDialog.dismiss();
        }
    }

    /**
     * 重新进行二维码扫描
     */
    @Override
    public void onReScaner() {
        if (handler != null) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }
}
