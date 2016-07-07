package com.cylan.jiafeigou.n.view.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.BaseFullScreenFragmentActivity;
import com.cylan.jiafeigou.n.mvp.impl.bind.BindDevicePresenterImpl;
import com.cylan.jiafeigou.n.view.bind.BindCameraFragment;
import com.cylan.jiafeigou.n.view.bind.BindDoorBellFragment;
import com.cylan.jiafeigou.n.view.bind.BindScanFragment;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BindDeviceActivity extends BaseFullScreenFragmentActivity {

    @BindView(R.id.imgV_top_bar_left)
    ImageView imgVTopBarLeft;
    @BindView(R.id.imgV_top_bar_center)
    TextView tvTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bind_device);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        ButterKnife.bind(this);
        initTopBar();
    }

    private void initTopBar() {
        ViewUtils.setViewMarginStatusBar(fLayoutTopBarContainer);
    }

    @OnClick(R.id.imgV_top_bar_left)
    public void onClose() {
        onBackPressed();
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

    @OnClick({R.id.v_to_scan_qrcode, R.id.v_to_bind_camera, R.id.v_to_bind_doorbell, R.id.v_to_bind_cloud_album})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.v_to_scan_qrcode: {
                ViewUtils.deBounceClick(findViewById(R.id.v_to_scan_qrcode));
                Bundle bundle = new Bundle();
                BindScanFragment fragment = BindScanFragment.newInstance(bundle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(android.R.id.content, fragment)
                        .addToBackStack("BindScanFragment")
                        .commit();
                break;
            }
            case R.id.v_to_bind_camera: {
                ViewUtils.deBounceClick(findViewById(R.id.v_to_bind_camera));
                Bundle bundle = new Bundle();
                BindCameraFragment fragment = BindCameraFragment.newInstance(bundle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out
                                , 0, R.anim.slide_out_right)
                        .replace(android.R.id.content, fragment)
                        .addToBackStack("BindCameraFragment")
                        .commit();
                new BindDevicePresenterImpl(fragment);
                break;
            }
            case R.id.v_to_bind_doorbell: {
                ViewUtils.deBounceClick(findViewById(R.id.v_to_bind_doorbell));
                Bundle bundle = new Bundle();
                BindDoorBellFragment fragment = BindDoorBellFragment.newInstance(bundle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(android.R.id.content, fragment)
                        .addToBackStack("BindDoorBellFragment")
                        .commit();
                new BindDevicePresenterImpl(fragment);
                break;
            }
            case R.id.v_to_bind_cloud_album:
                ViewUtils.deBounceClick(findViewById(R.id.v_to_bind_cloud_album));
                Bundle bundle = new Bundle();
                BindScanFragment fragment = BindScanFragment.newInstance(bundle);
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_down_out
                                , R.anim.slide_in_left, R.anim.slide_out_right)
                        .replace(android.R.id.content, fragment)
                        .addToBackStack("BindScanFragment")
                        .commit();
                break;
        }
    }
}