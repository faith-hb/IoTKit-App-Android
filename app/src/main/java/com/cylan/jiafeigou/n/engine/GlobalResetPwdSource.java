package com.cylan.jiafeigou.n.engine;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.SmartcallActivity;
import com.cylan.jiafeigou.misc.AutoSignIn;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JError;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 作者：zsl
 * 创建时间：2017/3/14
 * 描述：
 */
public class GlobalResetPwdSource {
    private static GlobalResetPwdSource instance;
    private CompositeSubscription mSubscription;
    private Subscription clearSub;

    private Activity appCompatActivity;

    public static GlobalResetPwdSource getInstance() {
        if (instance == null) {
            instance = new GlobalResetPwdSource();
        }
        return instance;
    }

    public void register() {
        if (mSubscription == null) {
            mSubscription = new CompositeSubscription();
        }
        Subscription subscribe = RxBus.getCacheInstance().toObservable(RxEvent.PwdHasResetEvent.class)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pwdHasResetEvent -> {
                    AppLogger.d("收到密码已被修改通知" + BaseApplication.isBackground());
                    PreferencesUtils.putBoolean(JConstant.AUTO_SIGNIN_TAB, false);
                    PreferencesUtils.putBoolean(JConstant.AUTO_lOGIN_PWD_ERR, true);
                    PreferencesUtils.putBoolean(JConstant.SHOW_PASSWORD_CHANGED, true);
                    RxBus.getCacheInstance().removeAllStickyEvents();
                    clearPwd();
                    if (!BaseApplication.isBackground()) {
                        pwdResetedDialog(pwdHasResetEvent.code);
                    }
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
        mSubscription.add(subscribe);
    }

    public void unRegister() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }

        if (clearSub != null && !clearSub.isUnsubscribed()) {
            clearSub.unsubscribe();
            clearSub = null;
        }
    }

    public void currentActivity(Activity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
    }

    public void pwdResetedDialog(int code) {
        if (code == 16008 || code == 1007 || code == 16006) {
            AppLogger.d("pwdResetedDialog:" + code);
            if (appCompatActivity != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
                builder.setTitle(R.string.RET_ELOGIN_ERROR)
                        .setMessage(R.string.PWD_CHANGED)
                        .setCancelable(false)
                        .setPositiveButton(R.string.OK, (dialog1, which) -> {
                            dialog1.dismiss();
                            jump2LoginFragment();
                        })
                        .show();
            }
        }
    }

    private void jump2LoginFragment() {
        clearPwd();

        RxBus.getCacheInstance().postSticky(new RxEvent.ResultLogin(JError.StartLoginPage));
        RxBus.getCacheInstance().post(new RxEvent.LogOutByResetPwdTab(true));

        Intent intent = new Intent(ContextUtils.getContext(), SmartcallActivity.class);
        intent.putExtra("from_log_out", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ContextUtils.getContext().getApplicationContext().startActivity(intent);
    }

    public void clearPwd() {
        clearSub = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .subscribe(o -> {
                    AutoSignIn.getInstance().autoSave(BaseApplication.getAppComponent().getSourceManager().getJFGAccount().getAccount(), 1, "")
                            .doOnError(throwable -> AppLogger.e("err: " + throwable.getLocalizedMessage()))
                            .subscribe();
                }, throwable -> AppLogger.e("err:" + MiscUtils.getErr(throwable)));
    }
}
