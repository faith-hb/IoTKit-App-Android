package com.cylan.jiafeigou.n.mvp.impl;

import com.cylan.entity.JfgEnum;
import com.cylan.ex.JfgException;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.mvp.contract.login.ForgetPwdContract;
import com.cylan.jiafeigou.n.mvp.model.RequestResetPwdBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.PreferencesUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

;

/**
 * Created by cylan-hunt on 16-6-29.
 */
public class ForgetPwdPresenterImpl extends AbstractPresenter<ForgetPwdContract.View>
        implements ForgetPwdContract.Presenter {

    private Subscription subscription;
    private CompositeSubscription compositeSubscription;

    public ForgetPwdPresenterImpl(ForgetPwdContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void submitAccount(final String account) {
        subscription = Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        final boolean isPhoneNum = JConstant.PHONE_REG.matcher(account).find();
                        if (isPhoneNum) {
                            try {
                                BaseApplication.getAppComponent().getCmd()
                                        .sendCheckCode(account, JFGRules.getLanguageType(ContextUtils.getContext()), JfgEnum.SMS_TYPE.JFG_SMS_FORGOTPASS);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                BaseApplication.getAppComponent().getCmd().forgetPassByEmail(JFGRules.getLanguageType(ContextUtils.getContext()), account);
                            } catch (JfgException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    //计数10分钟3次
    @Override
    public boolean checkOverCount(String account) {
        int count = PreferencesUtils.getInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, 0);
        long first_time = PreferencesUtils.getLong(account + JConstant.KEY_FORGET_PWD_FRIST_GET_SMS, 0);

        if (count == 0) {
            PreferencesUtils.putLong(account + JConstant.KEY_FORGET_PWD_FRIST_GET_SMS, System.currentTimeMillis());
            PreferencesUtils.putInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, count + 1);
            return false;
        }

        if (count < 3) {
            if (System.currentTimeMillis() - first_time < 10 * 60 * 1000) {
                PreferencesUtils.putInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, count + 1);
            } else {
                PreferencesUtils.putInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, 0);
                PreferencesUtils.putLong(account + JConstant.KEY_FORGET_PWD_FRIST_GET_SMS, System.currentTimeMillis());
            }
            return false;
        } else {
            if (System.currentTimeMillis() - first_time < 10 * 60 * 1000) {
                return true;
            } else {
                PreferencesUtils.putInt(account + JConstant.KEY_FORGET_PWD_GET_SMS_COUNT, 0);
                PreferencesUtils.putLong(account + JConstant.KEY_FORGET_PWD_FRIST_GET_SMS, System.currentTimeMillis());
                return false;
            }
        }
    }

    @Override
    public void submitPhoneNumAndCode(final String account, final String code) {
        subscription = Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            PreferencesUtils.putString(JConstant.SAVE_TEMP_ACCOUNT, account);
                            PreferencesUtils.putString(JConstant.SAVE_TEMP_CODE, code);
                            BaseApplication.getAppComponent().getCmd().verifySMS(account, code, PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, AppLogger::e);
    }

    @Override
    public void start() {
        super.start();
        if (compositeSubscription != null && !compositeSubscription.isUnsubscribed()) {
            compositeSubscription.unsubscribe();
        } else {
            compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(checkIsRegBack());
            compositeSubscription.add(getForgetPwdByMailSub());
            compositeSubscription.add(getSmsCodeResultSub());
            compositeSubscription.add(checkSmsCodeBack());
            compositeSubscription.add(resetPwdBack());
        }
    }

    private Subscription getForgetPwdByMailSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ForgetPwdByMail.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ForgetPwdByMail>() {
                    @Override
                    public void call(RxEvent.ForgetPwdByMail forgetPwdByMail) {
                        RequestResetPwdBean bean = new RequestResetPwdBean();
                        bean.ret = JConstant.AUTHORIZE_MAIL;
                        bean.content = forgetPwdByMail.account;
                        getView().submitResult(bean);
                    }
                }, AppLogger::e);
    }

    private Subscription getSmsCodeResultSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.SmsCodeResult.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.SmsCodeResult>() {
                    @Override
                    public void call(RxEvent.SmsCodeResult smsCodeResult) {
                        if (smsCodeResult.error == 0) {
                            //store the token .
                            PreferencesUtils.putString(JConstant.KEY_REGISTER_SMS_TOKEN,
                                    smsCodeResult.token);
                            PreferencesUtils.putLong(JConstant.KEY_REGISTER_SMS_TOKEN_TIME, System.currentTimeMillis());
                            AppLogger.d("code:" + smsCodeResult.token);
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 短信验证码的回调
     *
     * @return
     */
    @Override
    public Subscription checkSmsCodeBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResultVerifyCode.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResultVerifyCode>() {
                    @Override
                    public void call(RxEvent.ResultVerifyCode resultVerifyCode) {
                        if (resultVerifyCode != null) {
                            getView().checkSmsCodeResult(resultVerifyCode.code);
                        }
                    }
                }, AppLogger::e);
    }

    /**
     * 重置密码
     *
     * @param newPassword
     */
    @Override
    public void resetPassword(String newPassword) {
        rx.Observable.just(newPassword)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        String account = PreferencesUtils.getString(JConstant.SAVE_TEMP_ACCOUNT);
                        String code = PreferencesUtils.getString(JConstant.SAVE_TEMP_CODE);
                        try {
                            BaseApplication.getAppComponent().getCmd().resetPassword(account, s, PreferencesUtils.getString(JConstant.KEY_REGISTER_SMS_TOKEN));
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e("resetPassword" + throwable.getLocalizedMessage());
                    }
                });
    }

    /**
     * 重置密码的回调
     *
     * @return
     */
    @Override
    public Subscription resetPwdBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.ResetPwdBack.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.ResetPwdBack>() {
                    @Override
                    public void call(RxEvent.ResetPwdBack resetPwdBack) {
                        if (resetPwdBack != null) {
                            getView().resetPwdResult(resetPwdBack.jfgResult.code);
                        }
                    }
                }, AppLogger::e);
    }

    @Override
    public void checkIsReg(String account) {
        rx.Observable.just(account)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        try {
                            BaseApplication.getAppComponent().getCmd().checkAccountRegState(s);
                        } catch (JfgException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AppLogger.e(throwable.getLocalizedMessage());
                    }
                });

    }

    @Override
    public Subscription checkIsRegBack() {
        return RxBus.getCacheInstance().toObservable(RxEvent.CheckRegsiterBack.class)
                .subscribeOn(Schedulers.newThread())
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxEvent.CheckRegsiterBack>() {
                    @Override
                    public void call(RxEvent.CheckRegsiterBack checkRegsiterBack) {
                        if (checkRegsiterBack != null && getView() != null)
                            getView().checkIsRegReuslt(checkRegsiterBack.jfgResult.code);
                    }
                }, AppLogger::e);
    }

    @Override
    public void stop() {
        super.stop();
        unSubscribe(compositeSubscription);
        unSubscribe(subscription);
    }
}
