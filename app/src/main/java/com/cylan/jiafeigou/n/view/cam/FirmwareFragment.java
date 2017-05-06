package com.cylan.jiafeigou.n.view.cam;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.base.view.JFGSourceManager;
import com.cylan.jiafeigou.cache.db.module.Device;
import com.cylan.jiafeigou.dp.DpMsgDefine;
import com.cylan.jiafeigou.dp.DpMsgMap;
import com.cylan.jiafeigou.misc.JFGRules;
import com.cylan.jiafeigou.n.base.BaseApplication;
import com.cylan.jiafeigou.n.base.IBaseFragment;
import com.cylan.jiafeigou.n.mvp.contract.cam.HardwareUpdateContract;
import com.cylan.jiafeigou.n.mvp.impl.cam.HardwareUpdatePresenterImpl;
import com.cylan.jiafeigou.rx.RxEvent;
import com.cylan.jiafeigou.support.log.AppLogger;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.utils.MiscUtils;
import com.cylan.jiafeigou.utils.NetUtils;
import com.cylan.jiafeigou.utils.ToastUtil;
import com.cylan.jiafeigou.widget.LoadingDialog;
import com.cylan.jiafeigou.widget.dialog.SimpleDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_207_DEVICE_VERSION;
import static com.cylan.jiafeigou.misc.JConstant.KEY_DEVICE_ITEM_UUID;

/**
 * 作者：zsl
 * 创建时间：2017/2/13
 * 描述：
 */
public class FirmwareFragment extends IBaseFragment<HardwareUpdateContract.Presenter> implements HardwareUpdateContract.View {

    @BindView(R.id.tv_hardware_now_version)
    TextView tvHardwareNowVersion;
    @BindView(R.id.tv_hardware_new_version)
    TextView tvHardwareNewVersion;
    @BindView(R.id.hardware_update_point)
    View hardwareUpdatePoint;
    @BindView(R.id.tv_download_soft_file)
    TextView tvDownloadSoftFile;
    @BindView(R.id.download_progress)
    ProgressBar downloadProgress;
    @BindView(R.id.ll_download_pg_container)
    LinearLayout llDownloadPgContainer;
    @BindView(R.id.tv_version_describe)
    TextView tvVersionDescribe;
    @BindView(R.id.tv_percent)
    TextView tvLoadingShow;

    private String uuid;
    private RxEvent.CheckDevVersionRsp checkDevVersion;
    private String fileSize;

    private OnUpdateListener listener;

    public interface OnUpdateListener {
        void onUpdateResult(boolean hasNew);
    }

    public void setOnUpdateListener(OnUpdateListener listener) {
        this.listener = listener;
    }

    public static FirmwareFragment newInstance(Bundle bundle) {
        FirmwareFragment fragment = new FirmwareFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.uuid = getArguments().getString(KEY_DEVICE_ITEM_UUID);
        this.checkDevVersion = (RxEvent.CheckDevVersionRsp) getArguments().getSerializable("version_content");
        basePresenter = new HardwareUpdatePresenterImpl(this, uuid, checkDevVersion);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hardware_update, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initView();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (listener != null) {
            listener.onUpdateResult(checkDevVersion.hasNew);
        }
    }

    private void initView() {
        tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
//        DpMsgDefine.DPPrimary<String> sVersion = BaseApplication.getAppComponent().getSourceManager().getValue(uuid, DpMsgMap.ID_207_DEVICE_VERSION);
//        String s = MiscUtils.safeGet(sVersion, "");
        JFGSourceManager manager = BaseApplication.getAppComponent().getSourceManager();
        Device device = manager.getDevice(uuid);
        tvHardwareNowVersion.setText(device.$(ID_207_DEVICE_VERSION, ""));
        tvHardwareNewVersion.setText(device.$(ID_207_DEVICE_VERSION, ""));

        // 有新版本
        if (checkDevVersion != null && checkDevVersion.hasNew) {
            tvHardwareNewVersion.setText(checkDevVersion.version);
            hardwareUpdatePoint.setVisibility(View.VISIBLE);
            tvVersionDescribe.setVisibility(View.VISIBLE);
            tvVersionDescribe.setText(checkDevVersion.tip);
            if (basePresenter != null) basePresenter.getFileSize(basePresenter.creatDownLoadBean());
        }
    }

    @Override
    public void setPresenter(HardwareUpdateContract.Presenter presenter) {

    }

    @OnClick({R.id.tv_download_soft_file, R.id.tv_toolbar_icon})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_toolbar_icon:
                getFragmentManager().popBackStack();
                break;

            case R.id.tv_download_soft_file:
                //TEST
//                handlerDownLoad();

                if (NetUtils.getNetType(getContext()) == -1) {
                    ToastUtil.showNegativeToast(getString(R.string.OFFLINE_ERR_1));
                    return;
                }

//                DpMsgDefine.DPNet net = BaseApplication.getAppComponent().getSourceManager().getValue(uuid, DpMsgMap.ID_201_NET);
                DpMsgDefine.DPNet net = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
                boolean show = net != null && JFGRules.isDeviceOnline(net);
                if (!show) {
                    ToastUtil.showNegativeToast(getString(R.string.NOT_ONLINE));
                    return;
                }

                if (checkDevVersion != null && !checkDevVersion.hasNew) {
                    ToastUtil.showPositiveToast(getString(R.string.NEW_VERSION));
                    return;
                }

                if (!tvDownloadSoftFile.getText().equals(getString(R.string.Tap1_Update))) {
                    handlerDownLoad();
                } else {
                    handlerUpdate();
                    AppLogger.d("handlerUpdate");
                }
                break;
        }
    }

    /**
     * 处理升级
     */
    private void handlerUpdate() {
        if (tvHardwareNewVersion.getText().equals(tvHardwareNowVersion.getText())) {
            ToastUtil.showPositiveToast(getString(R.string.NEW_VERSION));
            return;
        }

        //设备在线
//        DpMsgDefine.DPNet net = BaseApplication.getAppComponent().getSourceManager().getValue(uuid, DpMsgMap.ID_201_NET);
        DpMsgDefine.DPNet net = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
        String remoteSSid = net.ssid;
        if (localSSid.equals("DOG-" + uuid.substring(6))) {
            AppLogger.d("走直连");
            //直连AP
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.Tap1_UpdateFirmwareTips))
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        //开始升级
//                        basePresenter.startUpdate();
                        basePresenter.upgradePing();
//                        basePresenter.startCounting();
                        tvDownloadSoftFile.setEnabled(false);
                    })
                    .show();
            return;
        }
        if (!TextUtils.equals(localSSid, remoteSSid)) {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.setwifi_check, remoteSSid))
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(getString(R.string.CARRY_ON), (DialogInterface dialog, int which) -> {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    })
                    .show();
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.Tap1_UpdateFirmwareTips))
                    .setNegativeButton(getString(R.string.CANCEL), (DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton(getString(R.string.OK), (DialogInterface dialog, int which) -> {
                        //开始升级
//                        basePresenter.startUpdate();
                        basePresenter.upgradePing();
//                        basePresenter.startCounting();
                        tvDownloadSoftFile.setEnabled(false);
                    })
                    .show();
        }
    }

    /**
     * 处理下载
     */
    private void handlerDownLoad() {
        //wifi 网络
        int netType = NetUtils.getNetType(getContext());
        if (netType == 1) {
            //开始下载
//            basePresenter.startDownload(basePresenter.creatDownLoadBean());
            basePresenter.myDownLoad(checkDevVersion.url, checkDevVersion.version);
            AppLogger.d("url:" + checkDevVersion.url + ":name:" + checkDevVersion.version);
        } else if (netType == 0 || netType == 2 || netType == 3 || netType == 4) {
            Bundle bundle = new Bundle();
            bundle.putString(SimpleDialogFragment.KEY_LEFT_CONTENT, getString(R.string.CARRY_ON));
            bundle.putString(SimpleDialogFragment.KEY_RIGHT_CONTENT, getString(R.string.CANCEL));
            bundle.putString(SimpleDialogFragment.KEY_CONTENT_CONTENT, getString(R.string.Tap1_Firmware_DataTips));
            SimpleDialogFragment simpleDialogFragment = SimpleDialogFragment.newInstance(bundle);
            simpleDialogFragment.setAction((int id, Object value) -> {
                //开始下载
                if (id == R.id.tv_dialog_btn_left)
//                basePresenter.startDownload(basePresenter.creatDownLoadBean());
                    basePresenter.myDownLoad(checkDevVersion.url, checkDevVersion.version);
            });
            simpleDialogFragment.show(getFragmentManager(), "simpleDialogFragment");
        }
    }

    @Override
    public void handlerResult(int code) {
        switch (code) {
            case 1:
                ToastUtil.showNegativeToast(getString(R.string.Tap1_DownloadFirmwareFai));
                break;
            case 2:
                ToastUtil.showPositiveToast(getString(R.string.Tap1_FirmwareUpdateSuc));
                hardwareUpdatePoint.setVisibility(View.INVISIBLE);
                tvDownloadSoftFile.setEnabled(true);
                tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
                llDownloadPgContainer.setVisibility(View.GONE);
                tvVersionDescribe.setVisibility(View.GONE);
                tvHardwareNowVersion.setText(checkDevVersion.version);
                break;
            case 3:
                ToastUtil.showPositiveToast(getString(R.string.Tap1_FirmwareUpdateFai));
                tvDownloadSoftFile.setEnabled(true);
                tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
                llDownloadPgContainer.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onDownloadStart() {
        tvDownloadSoftFile.setEnabled(false);
        llDownloadPgContainer.setVisibility(View.VISIBLE);
        tvLoadingShow.setText("0.0MB" + "/" + fileSize);
        downloadProgress.setProgress(0);
    }

    @Override
    public void onDownloadFinish() {
        tvDownloadSoftFile.setEnabled(true);
        llDownloadPgContainer.setVisibility(View.GONE);
        //设备在线
//        DpMsgDefine.DPNet net = BaseApplication.getAppComponent().getSourceManager().getValue(uuid, DpMsgMap.ID_201_NET);
        DpMsgDefine.DPNet net = BaseApplication.getAppComponent().getSourceManager().getDevice(uuid).$(DpMsgMap.ID_201_NET, new DpMsgDefine.DPNet());
        String localSSid = NetUtils.getNetName(ContextUtils.getContext());
        String remoteSSid = net.ssid;
        if (TextUtils.equals(localSSid, remoteSSid)) {
//            basePresenter.startUpdate();
            basePresenter.upgradePing();
//            basePresenter.startCounting();
            tvDownloadSoftFile.setEnabled(false);
            AppLogger.d("same_net");
        } else {
            //不在同局域网
            tvDownloadSoftFile.setText(getString(R.string.Tap1_Update));
            AppLogger.d("different_net");
        }
    }

    @Override
    public void onDownloading(double percent, long downloadedLength) {
        tvDownloadSoftFile.setEnabled(false);
        llDownloadPgContainer.setVisibility(View.VISIBLE);
        tvLoadingShow.setText(String.format(getString(R.string.Tap1_FirmwareDownloading), MiscUtils.FormatSdCardSize(downloadedLength) + "/" + fileSize));
        downloadProgress.setProgress((int) (percent * 100));
    }

    @Override
    public void onDownloadErr(int reason) {
        AppLogger.d("downLoadErr:" + reason);
        handlerResult(reason);
    }

    @Override
    public void beginUpdate() {
        tvLoadingShow.setText(String.format(getString(R.string.Tap1_FirmwareUpdating), 0 + ""));
        llDownloadPgContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUpdateing(int percent) {
        llDownloadPgContainer.setVisibility(View.VISIBLE);
        tvLoadingShow.setText(String.format(getString(R.string.Tap1_FirmwareUpdating), percent + ""));
        downloadProgress.setProgress(percent);
    }

    @Override
    public void initFileSize(String size) {
        if (NetUtils.getNetType(getContext()) == -1) {
            tvDownloadSoftFile.setText(String.format(getString(R.string.Tap1a_DownloadInstall), fileSize));
            return;
        }
        if (TextUtils.isEmpty(size)) return;
        fileSize = size;
        tvDownloadSoftFile.setText(String.format(getString(R.string.Tap1a_DownloadInstall), size));
    }

    @Override
    public void showPingLoading() {
        LoadingDialog.showLoading(getFragmentManager(), getString(R.string.LOADING));
    }

    @Override
    public void hidePingLoading() {
        LoadingDialog.dismissLoading(getFragmentManager());
    }

    @Override
    public void deviceNoRsp() {
        ToastUtil.showNegativeToast(getString(R.string.UPDATE_DISCONNECT));
    }

}