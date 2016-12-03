package com.cylan.jiafeigou.n.mvp.impl.cam;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.cylan.jiafeigou.BuildConfig;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JfgCmdInsurance;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.BaseBean;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.google.gson.Gson;

import java.util.Locale;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by cylan-hunt on 16-7-27.
 */
public class CamSettingPresenterImpl extends AbstractPresenter<CamSettingContract.View> implements
        CamSettingContract.Presenter {

    private Subscription subscription;

    private BeanCamInfo camInfoBean;
    private static final int[] periodResId = {R.string.MON_1, R.string.TUE_1,
            R.string.WED_1, R.string.THU_1,
            R.string.FRI_1, R.string.SAT_1, R.string.SUN_1};
    private static final int[] autoRecordMode = {
            R.string.RECORD_MODE,
            R.string.RECORD_MODE_1,
            R.string.RECORD_MODE_2
    };

    public CamSettingPresenterImpl(CamSettingContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        unSubscribe(subscription);
    }

    private void wrap(DeviceBean bean) {
        BaseBean baseBean = new BaseBean();
        baseBean.alias = bean.alias;
        baseBean.pid = bean.pid;
        baseBean.shareAccount = bean.shareAccount;
        baseBean.sn = bean.sn;
        baseBean.uuid = bean.uuid;
        BeanCamInfo info = new BeanCamInfo();
        if (bean.dataList == null && BuildConfig.DEBUG) {
            throw new IllegalArgumentException("list is null");
        }
        info.convert(baseBean, bean.dataList);
        this.camInfoBean = info;
    }

    @Override
    public void fetchCamInfo(final String uuid) {

        //查询设备列表
        unSubscribe(subscription);
        subscription = RxBus.getUiInstance().toObservableSticky(RxUiEvent.BulkDeviceList.class)
                .subscribeOn(Schedulers.computation())
                .filter(new Func1<RxUiEvent.BulkDeviceList, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.BulkDeviceList list) {
                        return getView() != null && list != null && list.allDevices != null;
                    }
                })
                .flatMap(new Func1<RxUiEvent.BulkDeviceList, Observable<DpMsgDefine.DpWrap>>() {
                    @Override
                    public Observable<DpMsgDefine.DpWrap> call(RxUiEvent.BulkDeviceList list) {
                        for (DpMsgDefine.DpWrap wrap : list.allDevices) {
                            if (TextUtils.equals(wrap.baseDpDevice.uuid, uuid)) {
                                return Observable.just(wrap);
                            }
                        }
                        return null;
                    }
                })
                .filter(new Func1<DpMsgDefine.DpWrap, Boolean>() {
                    @Override
                    public Boolean call(DpMsgDefine.DpWrap dpWrap) {
                        return dpWrap != null && dpWrap.baseDpDevice != null;
                    }
                })
                .flatMap(new Func1<DpMsgDefine.DpWrap, Observable<BeanCamInfo>>() {
                    @Override
                    public Observable<BeanCamInfo> call(DpMsgDefine.DpWrap dpWrap) {
                        BeanCamInfo info = new BeanCamInfo();
                        info.convert(dpWrap.baseDpDevice, dpWrap.baseDpMsgList);
                        camInfoBean = info;
                        AppLogger.i("BeanCamInfo: " + new Gson().toJson(info));
                        return Observable.just(info);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BeanCamInfo>() {
                    @Override
                    public void call(BeanCamInfo camInfoBean) {
                        //刷新
                        getView().onCamInfoRsp(camInfoBean);
                        if (camInfoBean.deviceBase != null
                                && !TextUtils.isEmpty(camInfoBean.deviceBase.shareAccount)) {
                            getView().isSharedDevice();
                        }
                    }
                });
        RxBus.getCacheInstance().post(new RxUiEvent.QueryBulkDevice());
    }

    @Override
    public String getDetailsSubTitle(Context context) {
        //sd卡状态
        if (camInfoBean.sdcardStorage != null) {
            if (camInfoBean.sdcardState && camInfoBean.sdcardStorage.err != 0) {
                //sd初始化失败时候显示
                return context.getString(R.string.NO_SDCARD);
            }
        }
        return TextUtils.isEmpty(camInfoBean.deviceBase.alias) ?
                camInfoBean.deviceBase.uuid : camInfoBean.deviceBase.alias;
    }

    @Override
    public String getAlarmSubTitle(Context context) {
        if (!camInfoBean.cameraAlarmFlag || camInfoBean.cameraAlarmInfo == null) {
            return getView().getContext().getString(R.string.MAGNETISM_OFF);
        }
        int day = camInfoBean.cameraAlarmInfo.day;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            if (((day >> (7 - 1 - i)) & 0x01) == 1) {
                //hit
                builder.append(context.getString(periodResId[i]));
                builder.append(",");
            }
        }
        if (builder.length() > 1)
            builder.replace(builder.length() - 1, builder.length(), "");
        if (day == 127) {//全天
            builder.setLength(0);
            builder.append(context.getString(R.string.HOURS));
        } else if (day == 124) {//工作日
            builder.setLength(0);
            builder.append(context.getString(R.string.WEEKDAYS));
        }
        builder.append(parse2Time(camInfoBean.cameraAlarmInfo.timeStart));
        builder.append("-");
        builder.append(parse2Time(camInfoBean.cameraAlarmInfo.timeEnd));
        return builder.toString();
    }

    @Override
    public String getAutoRecordTitle(Context context) {
        if (camInfoBean.deviceAutoVideoRecord > 2 || camInfoBean.deviceAutoVideoRecord < 0) {
            camInfoBean.deviceAutoVideoRecord = 0;
        }
        return context.getString(autoRecordMode[camInfoBean.deviceAutoVideoRecord]);
    }

    public static String parse2Time(int value) {
        return String.format(Locale.getDefault(), "%02d", value >> 8)
                + String.format(Locale.getDefault(), ":%02d", (((byte) value << 8) >> 8));
    }

    @Override
    public BeanCamInfo getCamInfoBean() {
        if (camInfoBean == null)
            camInfoBean = new BeanCamInfo();
        return camInfoBean;
    }

    @Override
    public void saveCamInfoBean(final BeanCamInfo camInfoBean, int id) {
        this.camInfoBean = camInfoBean;
        Observable.just(new Pair<>(camInfoBean, id))
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Pair<BeanCamInfo, Integer>>() {
                    @Override
                    public void call(Pair<BeanCamInfo, Integer> beanCamInfoIntegerPair) {
                        int id = beanCamInfoIntegerPair.second;
                        RxEvent.JFGAttributeUpdate update = new RxEvent.JFGAttributeUpdate();
                        update.uuid = camInfoBean.deviceBase.uuid;
                        update.o = beanCamInfoIntegerPair.first.getObject(id);
                        update.msgId = id;
                        update.version = System.currentTimeMillis();
                        RxBus.getCacheInstance().post(update);
                        JfgCmdInsurance.getCmd().robotSetData(camInfoBean.deviceBase.uuid,
                                DpUtils.getList(id,
                                        beanCamInfoIntegerPair.first.getByte(id)
                                        , System.currentTimeMillis()));
                        AppLogger.i("save bean Cam info");
                    }
                });
    }
}
