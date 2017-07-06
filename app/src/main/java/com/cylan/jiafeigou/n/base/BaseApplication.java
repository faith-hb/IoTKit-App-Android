package com.cylan.jiafeigou.n.base;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.cylan.jiafeigou.base.injector.component.AppComponent;
import com.cylan.jiafeigou.base.injector.component.DaggerAppComponent;
import com.cylan.jiafeigou.base.injector.module.AppModule;
import com.cylan.jiafeigou.n.engine.GlobalResetPwdSource;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.block.log.PerformanceUtils;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.utils.ProcessUtils;
import com.danikula.videocache.HttpProxyCacheServer;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by hunt on 16-5-14.
 */
public class BaseApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static final String TAG = "BaseApplication";

    //    private DaemonClient mDaemonClient;
    private static AppComponent appComponent;
    private static int viewCount = 0;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        mDaemonClient = new DaemonClient(createDaemonConfigurations());
//        mDaemonClient.onAttachBaseContext(base);
    }


//    private DaemonConfigurations createDaemonConfigurations() {
//        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
//                getPackageName() + ":process1",
//                DaemonService1.class.getCanonicalName(),
//                DaemonReceiver1.class.getCanonicalName());
//        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
//                getPackageName() + ":process2",
//                DaemonService2.class.getCanonicalName(),
//                DaemonReceiver2.class.getCanonicalName());
//        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
//        //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
//        return new DaemonConfigurations(configuration1, configuration2, listener);
//    }


//    private class MyDaemonListener implements DaemonConfigurations.DaemonListener {
//        @Override
//        public void onPersistentStart(Context context) {
//            AppLogger.d("onPersistentStart");
//        }
//
//        @Override
//        public void onDaemonAssistantStart(Context context) {
//            AppLogger.d("onDaemonAssistantStart");
//        }
//
//        @Override
//        public void onWatchDaemonDaed() {
//            AppLogger.d("onWatchDaemonDaed");
//        }
//    }


    @Override
    public void onCreate() {
        super.onCreate();
        //这是主进程
        if (TextUtils.equals(ProcessUtils.myProcessName(this), getApplicationContext().getPackageName())) {
            viewCount = 0;
            //设计师不需要这个固定通知栏.20170531
//            startService(new Intent(this, WakeupService.class));
            PreferencesUtils.init(getApplicationContext());
            PerformanceUtils.startTrace("appInit");
            //Dagger2 依赖注入
            appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();

            //每一个新的进程启动时，都会调用onCreate方法。
            //Dagger2 依赖注入,初始化全局资源
            registerActivityLifecycleCallbacks(this);
//            startService(new Intent(this, DataSourceService.class));
            GlobalResetPwdSource.getInstance().register();
            PerformanceUtils.stopTrace("appInit");
            PerformanceUtils.startTrace("app2SmartCall");
//            Schedulers.io().createWorker().schedule(() -> appComponent.getInitializationManager().initialization());
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AppLogger.d("进程已被销毁!!!!");
        appComponent.getInitializationManager().clean();
        GlobalResetPwdSource.getInstance().unRegister();

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
                //should release some resource
                Log.d(TAG, "onTrimMemory: " + level);
                RxBus.getCacheInstance().post(new RxEvent.AppHideEvent());
                break;
        }
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory: ");
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        AppLogger.i("life:onActivityCreated: " + activity.getClass().getSimpleName() + " " + savedInstanceState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        AppLogger.i("life:onActivityStarted " + activity.getClass().getSimpleName());
        viewCount++;
        GlobalResetPwdSource.getInstance().currentActivity(activity);
        cancelReportTask();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        AppLogger.i("life:onActivityResumed " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        AppLogger.i("life:onActivityPaused " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        AppLogger.i("life:onActivityStopped " + activity.getClass().getSimpleName());
        viewCount--;
        if (viewCount == 0)
            prepareReportTask();
    }

    private Subscription reportTask;

    /**
     * 退出后台3分钟,将向sdkReport网络状态
     */
    private void prepareReportTask() {
        if (getAppComponent().getCmd() != null) {
            if (reportTask != null) reportTask.unsubscribe();
            reportTask = Observable.just("report")
                    .subscribeOn(Schedulers.newThread())
                    .delay(3, TimeUnit.MINUTES)
                    .subscribe(ret -> {
                        AppLogger.d("timeout for report");
//                        getAppComponent().getCmd().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_NETWORK_CONNECTED);
                    }, throwable -> {
                        AppLogger.d("timeout for report");
//                        getAppComponent().getCmd().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_NETWORK_CONNECTED);
                    });
        }
    }

    private void cancelReportTask() {
        if (reportTask != null) reportTask.unsubscribe();
//        getAppComponent().getCmd().reportEnvChange(JfgEnum.ENVENT_TYPE.ENV_ONTOP);
    }

    public static boolean isBackground() {
        return viewCount == 0;
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
//        AppLogger.i("life:onActivitySaveInstanceState " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
//        AppLogger.i("life:onActivityDestroyed " + activity.getClass().getSimpleName());
    }

    public static HttpProxyCacheServer getProxy() {
        return appComponent.getHttpProxyCacheServer();
    }

    public static AppComponent getAppComponent() {
        return appComponent;
    }

    public static boolean isOnline() {
        return appComponent.getSourceManager().isOnline();
    }
}
