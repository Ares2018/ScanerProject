package com.scaner.scaner;

/*
  启动加速优化点：
    1、利用主题快速显示界面；
    2、异步初始化组件；
    3、梳理业务逻辑，延迟初始化组件、操作；
    4、正确使用线程；
    5、去掉无用代码、重复逻辑等。
 */

import android.app.Application;

import com.aliya.uimode.UiModeManager;
import com.core.glide.GlideMode;
import com.zjrb.core.db.ThemeMode;
import com.zjrb.core.utils.AppUtils;
import com.zjrb.core.utils.SettingManager;
import com.zjrb.core.utils.UIUtils;

/**
 * Application基类：封装了主线程、上下文、handler、looper、主线程Id
 *
 * @author a_liYa
 * @date 2016-3-2 下午6:18:42
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UIUtils.init(this);
        GlideMode.setProvincialTraffic(SettingManager.getInstance().isProvincialTraffic());
        ThemeMode.initTheme(R.style.AppTheme, R.style.CoreThemeNight_Base);
        UiModeManager.init(this, R.styleable.SupportUiMode);

        setTheme(ThemeMode.isNightMode() ? R.style.CoreThemeNight_Base : R.style.AppTheme);

        AppUtils.setChannel("sb");
    }
}
