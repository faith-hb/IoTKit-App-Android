package com.cylan.jiafeigou.n.mvp.impl.bind;

import android.text.TextUtils;

import com.cylan.entity.jniCall.JFGResult;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.dp.DpUtils;
import com.cylan.jiafeigou.misc.JResultEvent;
import com.cylan.jiafeigou.misc.SimulatePercent;
import com.cylan.jiafeigou.misc.bind.UdpConstant;
import com.cylan.jiafeigou.n.mvp.contract.bind.SubmitBindingInfoContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.rx.RxBus;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.rx.RxHelper;
import com.cylan.jiafeigou.rx.RxUiEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.utils.ListUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by cylan-hunt on 16-11-12.
 */

public class SubmitBindingInfoContractImpl extends
        AbstractPresenter<SubmitBindingInfoContract.View>
        implements SubmitBindingInfoContract.Presenter,
        SimulatePercent.OnAction {

    private boolean success = false;

    private SimulatePercent simulatePercent;
    private CompositeSubscription compositeSubscription;
    private UdpConstant.UdpDevicePortrait portrait;

    public SubmitBindingInfoContractImpl(SubmitBindingInfoContract.View view,
                                         UdpConstant.UdpDevicePortrait portrait) {
        super(view);
        view.setPresenter(this);
        this.portrait = portrait;
        simulatePercent = new SimulatePercent();
        simulatePercent.setOnAction(this);
    }

    @Override
    public void startCounting() {
        if (simulatePercent != null)
            simulatePercent.start();
    }

    @Override
    public void endCounting() {
        if (simulatePercent != null)
            simulatePercent.stop();
    }

    @Override
    public void start() {
        unSubscribe(compositeSubscription);
        compositeSubscription = new CompositeSubscription();
        compositeSubscription.add(robotSyncDataSub());
        compositeSubscription.add(bindTimeoutSub());
        compositeSubscription.add(bindResultSub());
        compositeSubscription.add(monitorBulkDeviceList());
        //查询
        RxBus.getCacheInstance().post(new RxUiEvent.QueryBulkDevice());
    }

    /**
     * 绑定结果:通过{@link com.cylan.jiafeigou.n.engine.DataSourceService#OnResult(JFGResult)}
     * {@link com.cylan.jiafeigou.misc.JResultEvent#JFG_RESULT_BINDDEV}
     *
     * @return
     */
    private Subscription bindResultSub() {
        return RxBus.getCacheInstance().toObservableSticky(RxEvent.BindDeviceEvent.class)
                .filter(new Func1<RxEvent.BindDeviceEvent, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.BindDeviceEvent bindDeviceEvent) {
                        return getView() != null
                                && bindDeviceEvent.jfgResult.event == JResultEvent.JFG_RESULT_BINDDEV;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<RxEvent.BindDeviceEvent, Object>() {
                    @Override
                    public Object call(RxEvent.BindDeviceEvent bindDeviceEvent) {
                        if (simulatePercent != null)
                            simulatePercent.boost();
                        success = true;
                        AppLogger.i("bind success");
                        RxBus.getCacheInstance().removeStickyEvent(RxEvent.BindDeviceEvent.class);
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("bindResultSub"))
                .subscribe();
    }

    /**
     * 客户端登陆成功后,会批量查询设备.
     *
     * @return
     */
    private Subscription monitorBulkDeviceList() {
        return RxBus.getUiInstance().toObservableSticky(RxUiEvent.BulkDeviceList.class)
                .filter(new Func1<RxUiEvent.BulkDeviceList, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.BulkDeviceList deviceList) {
                        return getView() != null
                                && deviceList != null
                                && !ListUtils.isEmpty(deviceList.allDevices);
                    }
                })
                .flatMap(new Func1<RxUiEvent.BulkDeviceList, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(RxUiEvent.BulkDeviceList deviceList) {
                        AppLogger.i("monitorBulkDeviceList: " + deviceList.allDevices);
                        final int count = deviceList.allDevices.size();
                        for (int i = 0; i < count; i++) {
                            DpMsgDefine.DpWrap wrap = deviceList.allDevices.get(i);
                            if (wrap == null || wrap.baseDpDevice == null) continue;
                            if (portrait != null && TextUtils.equals(portrait.cid,
                                    wrap.baseDpDevice.uuid)) {
                                //hit the binding cid
                                return Observable.just(true);
                            }
                        }
                        return Observable.just(false);
                    }
                })
                .retry(new RxHelper.RxException<>("SubmitBindingInfoContractImpl"))
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                    }
                });
    }

    /**
     * 开始接受绑定结果
     *
     * @return
     */
    private Subscription bindTimeoutSub() {
        return RxBus.getUiInstance().toObservable(RxUiEvent.SingleDevice.class)
                .filter(new Func1<RxUiEvent.SingleDevice, Boolean>() {
                    @Override
                    public Boolean call(RxUiEvent.SingleDevice singleDevice) {
                        boolean filter = portrait != null
                                && singleDevice.dpMsg != null
                                && singleDevice.dpMsg.baseDpDevice != null
                                && TextUtils.equals(portrait.cid, singleDevice.dpMsg.baseDpDevice.uuid);
                        AppLogger.i(TAG + ":filter: " + filter);
                        return filter;
                    }
                })
                .flatMap(new Func1<RxUiEvent.SingleDevice, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(RxUiEvent.SingleDevice singleDevice) {
                        for (DpMsgDefine.DpMsg msg : singleDevice.dpMsg.baseDpMsgList) {
                            if (msg.msgId == DpMsgMap.ID_201_NET) {
                                AppLogger.i(TAG + " msg: " + msg);
                                if (msg.o instanceof DpMsgDefine.MsgNet) {
                                    return Observable.just(((DpMsgDefine.MsgNet) msg.o).net);
                                }
                            }
                        }
                        return Observable.just(-1);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(90, TimeUnit.SECONDS, Observable.just(-1)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .filter(new RxHelper.Filter<>("bind timeout:", !success))
                        .map(new Func1<Integer, Integer>() {
                            @Override
                            public Integer call(Integer state) {
                                AppLogger.i(TAG + " bind timeout: " + state);
                                getView().bindState(state);
                                return null;
                            }
                        }))
                .retry(new RxHelper.RxException<>("bingResultMonitor"))
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer state) {
                        AppLogger.i("bind success: " + state);
                        success = true;
                        if (simulatePercent != null)
                            simulatePercent.boost();
                    }
                });
    }

    /**
     * 某些设备上线下面,各种状态变化,都是通过{@link com.cylan.jiafeigou.rx.RxEvent.JFGRobotSyncData}
     *
     * @return
     */
    private Subscription robotSyncDataSub() {
        return RxBus.getCacheInstance().toObservable(RxEvent.JFGRobotSyncData.class)
                .subscribeOn(Schedulers.newThread())
                .filter(new Func1<RxEvent.JFGRobotSyncData, Boolean>() {
                    @Override
                    public Boolean call(RxEvent.JFGRobotSyncData jfgRobotSyncData) {
                        boolean filter = portrait != null && TextUtils.equals(portrait.cid, jfgRobotSyncData.identity);
                        AppLogger.i("filter: " + filter);
                        return filter;
                    }
                })
                .flatMap(new Func1<RxEvent.JFGRobotSyncData, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(RxEvent.JFGRobotSyncData jfgRobotSyncData) {
                        if (jfgRobotSyncData.dataList != null) {
                            DpMsgDefine.MsgNet net = DpUtils.getMsg(jfgRobotSyncData.dataList,
                                    DpMsgMap.ID_201_NET,
                                    DpMsgDefine.MsgNet.class);
                            AppLogger.i("yes hit net: " + net);
                            if (net != null)
                                return Observable.just(net.net);
                        }
                        return null;
                    }
                })
                .retry(new RxHelper.RxException<>("robotSyncDataSub"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        getView().bindState(integer != null ? integer : -1);
                    }
                });
    }

    @Override
    public void stop() {
        if (simulatePercent != null)
            simulatePercent.stop();
        unSubscribe(compositeSubscription);
    }

    @Override
    public void actionDone() {
        Observable.just(null)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object integer) {
                        AppLogger.i("actionDone: " + integer);
                        getView().bindState(1);
                    }
                });
    }

    @Override
    public void actionPercent(int percent) {
        Observable.just(percent)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return getView() != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        getView().onCounting(integer);
                    }
                });
    }

}