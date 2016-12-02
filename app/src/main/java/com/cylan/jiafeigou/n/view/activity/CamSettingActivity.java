package com.cylan.jiafeigou.n.view.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.CamSettingContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.CamSettingPresenterImpl;
import com.cylan.jiafeigou.n.mvp.model.BeanCamInfo;
import com.cylan.jiafeigou.n.mvp.model.DeviceBean;
import com.cylan.jiafeigou.n.view.cam.DeviceInfoDetailFragment;
import com.cylan.jiafeigou.n.view.cam.SafeProtectionFragment;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ViewUtils;
import com.cylan.jiafeigou.widget.SettingItemView0;
import com.cylan.jiafeigou.widget.SettingItemView1;
import com.kyleduo.switchbutton.SwitchButton;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_BUNDLE;

public class CamSettingActivity extends BaseFullScreenFragmentActivity<CamSettingContract.Presenter>
        implements CamSettingContract.View {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.sv_setting_device_detail)
    SettingItemView0 svSettingDeviceDetail;
    @BindView(R.id.sv_setting_device_wifi)
    SettingItemView0 svSettingDeviceWifi;
    @BindView(R.id.sv_setting_device_mobile_network)
    SettingItemView1 svSettingDeviceMobileNetwork;
    @BindView(R.id.sv_setting_safe_protection)
    SettingItemView0 svSettingSafeProtection;
    @BindView(R.id.sv_setting_device_auto_record)
    SettingItemView0 svSettingDeviceAutoRecord;
    @BindView(R.id.sv_setting_device_delay_capture)
    SettingItemView0 svSettingDeviceDelayCapture;
    @BindView(R.id.sv_setting_device_standby_mode)
    SettingItemView1 svSettingDeviceStandbyMode;
    @BindView(R.id.sv_setting_device_indicator)
    SettingItemView1 svSettingDeviceIndicator;
    @BindView(R.id.sv_setting_device_rotatable)
    SettingItemView1 svSettingDeviceRotate;
    @BindView(R.id.tv_setting_unbind)
    TextView tvSettingUnbind;
    @BindView(R.id.lLayout_setting_item_container)
    LinearLayout lLayoutSettingItemContainer;
    @BindView(R.id.sbtn_setting_110v)
    SettingItemView0 sbtnSetting110v;

    private WeakReference<DeviceInfoDetailFragment> informationWeakReference;
    private WeakReference<SafeProtectionFragment> safeProtectionFragmentWeakReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        basePresenter = new CamSettingPresenterImpl(this);
        setContentView(R.layout.activity_cam_setting);
        ButterKnife.bind(this);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        initTopBar();
        initStandbyBtn();
        init110VVoltageBtn();
        initLedIndicatorBtn();
        initMobileNetBtn();
        initRotateBtn();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bundle bundle = getIntent().getBundleExtra(KEY_DEVICE_ITEM_BUNDLE);
        Parcelable p = bundle.getParcelable(KEY_DEVICE_ITEM_BUNDLE);
        if (p != null && p instanceof DeviceBean) {
            if (basePresenter != null)
                basePresenter.fetchCamInfo(((DeviceBean) p).uuid);
        } else {
            AppLogger.d("o is null");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initTopBar() {
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
    }

    @Override
    public void onBackPressed() {
        if (checkExtraChildFragment()) {
            return;
        } else if (checkExtraFragment())
            return;
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }

    /**
     * 待机模式按钮,关联到其他按钮
     */
    private void initStandbyBtn() {
        ((SwitchButton) svSettingDeviceStandbyMode.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        BeanCamInfo info = basePresenter.getCamInfoBean();
                        info.cameraStandbyFlag = isChecked;
                        basePresenter.saveCamInfoBean(info, DpMsgMap.ID_508_CAMERA_STANDBY_FLAG);
                        AppLogger.i("save id:" + DpMsgMap.ID_508_CAMERA_STANDBY_FLAG);
                        AppLogger.i("save value:" + info.cameraStandbyFlag);
                        enableAllItem(lLayoutSettingItemContainer, !isChecked);
                    }
                });
    }

    private void initMobileNetBtn() {
        ((SwitchButton) svSettingDeviceMobileNetwork.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        BeanCamInfo info = basePresenter.getCamInfoBean();
                        info.deviceMobileNetPriority = isChecked;
                        basePresenter.saveCamInfoBean(info, DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
                        AppLogger.i("save id:" + DpMsgMap.ID_217_DEVICE_MOBILE_NET_PRIORITY);
                        AppLogger.i("save value:" + info.deviceMobileNetPriority);
                    }
                });
    }

    private void init110VVoltageBtn() {
        ((SwitchButton) sbtnSetting110v.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        BeanCamInfo info = basePresenter.getCamInfoBean();
                        info.deviceVoltage = isChecked;
                        basePresenter.saveCamInfoBean(info, DpMsgMap.ID_216_DEVICE_VOLTAGE);
                        AppLogger.i("save id:" + DpMsgMap.ID_216_DEVICE_VOLTAGE);
                        AppLogger.i("save value:" + info.deviceVoltage);
                    }
                });
    }

    private void initLedIndicatorBtn() {
        ((SwitchButton) svSettingDeviceIndicator.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        BeanCamInfo info = basePresenter.getCamInfoBean();
                        info.ledIndicator = isChecked;
                        basePresenter.saveCamInfoBean(info, DpMsgMap.ID_209_LED_INDICATOR);
                        AppLogger.i("save id:" + DpMsgMap.ID_209_LED_INDICATOR);
                        AppLogger.i("save value:" + info.ledIndicator);
                    }
                });
    }

    private void initRotateBtn() {
        ((SwitchButton) svSettingDeviceRotate.findViewById(R.id.btn_item_switch))
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        BeanCamInfo info = basePresenter.getCamInfoBean();
                        info.deviceCameraRotate = isChecked;
                        basePresenter.saveCamInfoBean(info, DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE);
                        AppLogger.i("save id:" + DpMsgMap.ID_304_DEVICE_CAMERA_ROTATE);
                        AppLogger.i("save value:" + info.deviceCameraRotate);
                    }
                });
    }

    @OnClick(R.id.imgV_top_bar_center)
    public void onBackClick() {
        finish();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            overridePendingTransition(R.anim.slide_in_left_without_interpolator, R.anim.slide_out_right_without_interpolator);
        }
    }

    @OnClick({R.id.sv_setting_device_detail,
            R.id.tv_setting_unbind,
            R.id.sv_setting_safe_protection
    })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sv_setting_device_detail: {
                initInfoDetailFragment();
                DeviceInfoDetailFragment fragment = informationWeakReference.get();
                fragment.setCallBack(new IBaseFragment.CallBack() {
                    @Override
                    public void callBack(Object t) {
                        basePresenter.fetchCamInfo(basePresenter.getCamInfoBean().deviceBase.uuid);
                    }
                });
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_DEVICE_ITEM_BUNDLE, basePresenter.getCamInfoBean());
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, fragment);
            }
            break;
            case R.id.tv_setting_unbind:
                break;
            case R.id.sv_setting_device_auto_record:

                break;
            case R.id.sv_setting_safe_protection: {
                Bundle bundle = new Bundle();
                bundle.putParcelable(KEY_DEVICE_ITEM_BUNDLE, basePresenter.getCamInfoBean());
                initSafeProtectionFragment();
                Fragment fragment = safeProtectionFragmentWeakReference.get();
                fragment.setArguments(bundle);
                loadFragment(android.R.id.content, safeProtectionFragmentWeakReference.get());
            }
            break;
        }
    }

    /**
     * 开启待机模式的时候,其余所有选项都不能点击.
     * 递归调用
     *
     * @param viewGroup
     * @param enable
     */
    private void enableAllItem(ViewGroup viewGroup, boolean enable) {
        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view.getId() == R.id.sv_setting_device_standby_mode) {
                continue;
            }
            if (view instanceof ViewGroup) {
                enableAllItem((ViewGroup) view, enable);
            }
            view.setEnabled(enable);
        }
    }

    /**
     * 用来加载fragment的方法。
     */
    private void loadFragment(int id, Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                //如果需要动画，可以把动画添加进来
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(id, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
    }

    private void initInfoDetailFragment() {
        //should load
        if (informationWeakReference == null || informationWeakReference.get() == null) {
            informationWeakReference = new WeakReference<>(DeviceInfoDetailFragment.newInstance(null));
        }
    }

    private void initSafeProtectionFragment() {
        //should load
        if (safeProtectionFragmentWeakReference == null || safeProtectionFragmentWeakReference.get() == null) {
            safeProtectionFragmentWeakReference = new WeakReference<>(SafeProtectionFragment.newInstance(new Bundle()));
        }
    }

    @Override
    public void onCamInfoRsp(BeanCamInfo camInfoBean) {
        svSettingDeviceDetail.setTvSubTitle(basePresenter.getDetailsSubTitle(getContext()));
        svSettingDeviceWifi.setTvSubTitle(camInfoBean.net != null && camInfoBean.net.ssid != null ? camInfoBean.net.ssid : "");
        svSettingDeviceMobileNetwork.setSwitchButtonState(camInfoBean.deviceMobileNetPriority);
        svSettingDeviceIndicator.setSwitchButtonState(camInfoBean.ledIndicator);
        svSettingDeviceRotate.setSwitchButtonState(camInfoBean.deviceCameraRotate);
        svSettingDeviceStandbyMode.setSwitchButtonState(camInfoBean.cameraStandbyFlag);
        svSettingSafeProtection.setTvSubTitle(basePresenter.getAlarmSubTitle(getContext()));
        svSettingDeviceAutoRecord.setEnabled(basePresenter.getCamInfoBean().deviceAutoVideoRecord != 0);
    }

    @Override
    public void isSharedDevice() {

    }

    @Override
    public void setPresenter(CamSettingContract.Presenter basePresenter) {
        this.basePresenter = basePresenter;
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }
}
