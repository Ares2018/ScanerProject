package com.scaner.scaner.scaner.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliya.view.fitsys.FitWindowsFrameLayout;
import com.google.zxing.Result;
import com.scaner.scaner.scaner.CameraManager;
import com.scaner.scaner.scaner.CaptureActivityHandler;
import com.scaner.scaner.scaner.DecodeThread;
import com.scaner.scaner.scaner.R;
import com.scaner.scaner.scaner.R2;
import com.scaner.scaner.scaner.decoding.InactivityTimer;
import com.scaner.scaner.scaner.interfaces.OnCloseLightListen;
import com.scaner.scaner.scaner.interfaces.OnScanerListener;
import com.scaner.scaner.scaner.ui.dialog.ScanerErrorDialog;
import com.scaner.scaner.scaner.utils.AnimationToolUtils;
import com.scaner.scaner.scaner.utils.QrBarToolUtils;
import com.zjrb.core.common.base.BaseFragment;
import com.zjrb.core.common.global.IKey;
import com.zjrb.core.common.permission.IPermissionCallBack;
import com.zjrb.core.common.permission.Permission;
import com.zjrb.core.common.permission.PermissionManager;
import com.zjrb.core.domain.MediaEntity;
import com.zjrb.core.nav.Nav;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 二维码扫描fragment
 * Created by wanglinjie.
 * create time:2018/5/3  上午9:24
 */

public class ScanerFragment extends BaseFragment implements OnScanerListener,
        ScanerErrorDialog.OnClickCallback,
        IPermissionCallBack,
        SurfaceHolder.Callback {

    @BindView(R2.id.capture_preview)
    SurfaceView surfaceView;
    @BindView(R2.id.capture_scan_line)
    ImageView mQrLineView;
    @BindView(R2.id.capture_crop_layout)
    RelativeLayout mCropLayout;
    @BindView(R2.id.iv_loading)
    ImageView mIcon;
    @BindView(R2.id.tv_toast)
    TextView mTextToast;
    @BindView(R2.id.ry_over)
    RelativeLayout mContainerOver;
    @BindView(R2.id.capture_containter)
    FitWindowsFrameLayout mContainer;

    private InactivityTimer inactivityTimer;

    /**
     * 扫描处理
     */
    private CaptureActivityHandler handler;

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
    private boolean hasSurface = false;
    private boolean isHasGranted = false;

    private OnCloseLightListen lightListen;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCloseLightListen) {
            lightListen = (OnCloseLightListen) context;
        }
    }

    public void setLightListen(OnCloseLightListen lightListen) {
        this.lightListen = lightListen;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.module_scaner_fragment_scaner_code, container, false);
        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        //扫描动画初始化
        AnimationToolUtils.ScaleUpDowm(mQrLineView);
        //初始化 CameraManager
        CameraManager.init(getContext().getApplicationContext());
        hasSurface = false;
        surfaceView.getHolder().addCallback(this);
        inactivityTimer = new InactivityTimer(getActivity());
        PermissionManager.get()
                .request(this, this, Permission.STORAGE_READE, Permission.STORAGE_WRITE, Permission.CAMERA);
    }


    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        //todo  wlj  surfaceview渲染时机异常，临时处理
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
    }

    @Override
    public void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
        dismissDialog();
        CameraManager.get().closeDriver();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isHasGranted) {
            initCamera(holder);
        }
        hasSurface = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (isHasGranted) {
            initCamera(holder);
        }
        hasSurface = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
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
            if (requestCode == 10) {
                ContentResolver resolver = getActivity().getContentResolver();
                // 照片的原始资源地址
                startLoadingAnim();
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
                            onSuccess(rawResult);
                        } else {
                            onFail();
                        }
                    } else {
                        onFail();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 100) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    onGranted(true);
                }
            }

        }

    }


    /**
     * 二维码扫描失败弹出dialog点击确定按钮
     *
     * @param v
     */
    @Override
    public void onOkClick(View v) {
        if (mContainerOver.getVisibility() == View.VISIBLE) {
            mContainerOver.setVisibility(View.GONE);
        }
        onReScaner();
    }

    /**
     * 扫描成功
     *
     * @param result
     */
    @Override
    public void onSuccess(Result result) {
        stopLoadingAnim();
        lightListen.closeLight();

        //链接
        if (result.getText().startsWith("http") || result.getText().startsWith("https")) {
            if (result.getText().contains("weixin.qq.com")) {
                Nav.with(getContext()).toPath("http://weixin.qq.com/");//链接
            } else {
                Nav.with(getContext()).toPath(result.getText());//链接
            }
            onReScaner();
        } else {
            //文本
            if (!TextUtils.isEmpty(result.getText())) {
                onReScaner();
                Bundle bundle = new Bundle();
                bundle.putString(IKey.SCANER_TEXT, result.getText());
                Nav.with(this).setExtras(bundle).toPath("/ui/ScanerResultActivity");
            } else {
                onFail();
            }
        }
    }

    private ScanerErrorDialog errDialog;

    /**
     * 扫描失败
     */
    @Override
    public void onFail() {
        stopLoadingAnim();
        errDialog = new ScanerErrorDialog(getContext());
        errDialog.setmOnClickCallback(this);
        errDialog.show();
    }

    /**
     * 关闭dialog
     */
    private void dismissDialog() {
        if (errDialog != null && errDialog.isShowing()) {
            errDialog.dismiss();
        }
    }

    /**
     * 重新进行二维码扫描
     */
    private void onReScaner() {
        if (handler != null) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }

    /**
     * 启动加载动画
     */
    private void startLoadingAnim() {
        if (mContainerOver.getVisibility() == View.GONE) {
            mContainerOver.setVisibility(View.VISIBLE);
        }

        Animation rotate = AnimationUtils.loadAnimation(getActivity(), R.anim.module_scaner_loading_rotate);
        if (rotate != null) {
            mIcon.startAnimation(rotate);
        } else {
            mIcon.setAnimation(rotate);
            mIcon.startAnimation(rotate);
        }
    }

    /**
     * 关闭加载动画
     */
    private void stopLoadingAnim() {
        mIcon.clearAnimation();
        if (mContainerOver.getVisibility() == View.VISIBLE) {
            mContainerOver.setVisibility(View.GONE);
        }
    }

    /**
     * 权限允许
     *
     * @param isAlreadyDef
     */
    @Override
    public void onGranted(boolean isAlreadyDef) {
        isHasGranted = true;
        if (hasSurface) {
            initCamera(surfaceView.getHolder());
        }
    }


    /**
     * 权限拒绝
     *
     * @param neverAskPerms
     */
    @Override
    public void onDenied(List<String> neverAskPerms) {
        isHasGranted = false;
        Nav.with(this).toPath("/authority", 100);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public void onElse(List<String> deniedPerms, List<String> neverAskPerms) {
        isHasGranted = false;
        Nav.with(this).toPath("/authority", 100);
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}
