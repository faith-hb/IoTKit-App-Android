package com.cylan.jiafeigou.support.splash;

import android.Manifest;

import com.cylan.jiafeigou.base.wrapper.BasePresenter;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.tbruyelle.rxpermissions.RxPermissions;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by yanzhendong on 2017/7/5.
 */

public class SplashPresenter extends BasePresenter<SplashContact.View> implements SplashContact.Presenter {

    @Override
    public void initPermissions() {
        new RxPermissions(mView.getActivityContext())
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .observeOn(Schedulers.io())
                .map(permission -> {
                    if (permission.granted) {
                        BaseApplication.getAppComponent().getInitializationManager().initialization();//在这里做初始化
                    }
                    return permission;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(permission -> {
                    if (permission.granted) {
                        mView.onEnterLoginActivity();
                    } else {
                        mView.onExitApp();
                    }
                }, e -> {
                    e.printStackTrace();
                    AppLogger.e(e.getMessage());
                });
    }
}
