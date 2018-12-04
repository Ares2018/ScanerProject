//package com.scaner.scaner;
//
//import android.os.Bundle;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v7.app.AppCompatActivity;
//import android.view.View;
//import android.widget.ImageView;
//
//import com.scaner.scaner.scaner.CameraManager;
//import com.scaner.scaner.scaner.interfaces.OnCloseLightListener;
//import com.scaner.scaner.scaner.ui.ScanerFragment;
//import com.zjrb.core.common.base.BaseActivity;
//import com.zjrb.core.nav.Nav;
//import com.zjrb.core.utils.click.ClickTracker;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//
///**
// * 二维码扫描测试页面
// * Created by wanglinjie.
// * create time:2018/4/23  上午10:16
// */
//public class ScanerActivity2 extends AppCompatActivity implements OnCloseLightListener {
//    @BindView(R.id.iv_light)
//    ImageView ivLight;
//
//    /**
//     * 关闭闪光灯
//     */
//    private boolean mFlashing = true;
//
//    private ScanerFragment scanerFragment;
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_scaner);
//        ButterKnife.bind(this);
////        initPermission();
//        init();
//    }
//
//    private void init() {
//        scanerFragment = ScanerFragment.newInstance();
//        scanerFragment.setLightListen(this);
//        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.add(R.id.v_container, scanerFragment).commit();
//    }
//
//
////    /**
////     * 需要打开相机权限
////     */
////    private void initPermission() {
////        //请求Camera权限 与 文件读写 权限
////        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED &&
////                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
////            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
////        }
////    }
//
//    /**
//     * 闪光灯管理
//     */
//    private void light() {
//        if (mFlashing) {
//            mFlashing = false;
//            // 开闪光灯
//            CameraManager.get().openLight();
//            ivLight.setSelected(true);
//        } else {
//            mFlashing = true;
//            // 关闪光灯
//            CameraManager.get().offLight();
//            ivLight.setSelected(false);
//        }
//
//    }
//
//    @OnClick({R.id.iv_back, R.id.iv_album, R.id.iv_light})
//    public void onClick(View v) {
//        if (ClickTracker.isDoubleClick()) return;
//        //点击图集
//        if (v.getId() == R.id.iv_album) {
//            Nav.with(scanerFragment)
//                    .toPath("/core/MediaSelectActivity", 10);
//            //闪光灯
//        } else if (v.getId() == R.id.iv_light) {
//            light();
//        } else if (v.getId() == R.id.iv_back) {
//            finish();
//        }
//    }
//
//    /**
//     * 关闭闪光灯
//     */
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mFlashing = true;
//        // 关闪光灯
//        CameraManager.get().offLight();
//        ivLight.setSelected(false);
//    }
//
//    /**
//     * 关闭闪光灯
//     */
//    @Override
//    public void closeLight() {
//        if (!mFlashing) {
//            mFlashing = true;
//            // 关闪光灯
//            CameraManager.get().offLight();
//            ivLight.setSelected(false);
//        }
//    }
//}