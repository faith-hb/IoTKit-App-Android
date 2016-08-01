package com.cylan.jiafeigou.n.view.mag;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.PreferencesUtils;
import com.cylan.jiafeigou.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 创建者     谢坤
 * 创建时间   2016/7/26 15:51
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MagLiveFragment extends Fragment {

    @BindView(R.id.tv_information_facility_name)
    TextView mFacilityName;


    private MagLiveInformationFragment magLiveInformationFragment;
    private SwitchButton mSwBtn;
    /*private boolean isSwitch;*/

    public static MagLiveFragment newInstance(Bundle bundle) {
        MagLiveFragment fragment = new MagLiveFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        magLiveInformationFragment = MagLiveInformationFragment.newInstance(new Bundle());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_msglive_message,null);
        ButterKnife.bind(this,view);
        mSwBtn = (SwitchButton) view.findViewById(R.id.btn_switch);
        mSwBtn.setOnStateChangedListener(new SwitchButton.OnStateChangedListener(){
            @Override
            public void onStateChanged(boolean state) {
                if(true == state) {
                    Toast.makeText(getActivity(), "开关已打开"+state, Toast.LENGTH_SHORT).show();
                    /*isSwitch = true;*/
                }
                else {
                    Toast.makeText(getActivity(), "开关已关闭"+state, Toast.LENGTH_SHORT).show();
                    /*isSwitch = false;*/
                }
            }
        });
        return view;
    }

    /**
     * 点击回退到原来的activity
     */
    @OnClick(R.id.iv_msglive_back)
    public void onMessageBack(){
        getActivity().onBackPressed();
    }

    /**
     * 对switchButton所属的整个条目进行监听，点击之后。让switchButton进行滑动
     */
    @OnClick(R.id.rLayout_mag_live)
    public void onRelativeLayoutClick(){

    }

    /**
     * 点击进入设备信息的设置页面
     */
    @OnClick(R.id.lLayout_information_facility_name)
    public void onFacilityMessage(){
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right
                        , R.anim.slide_in_left, R.anim.slide_out_right)
                .add(R.id.fLayout_msg_information,magLiveInformationFragment,"MagLiveFragment")
                .addToBackStack("MagLiveFragment")
                .commit();

        /**
         * 接口回调，得到相应的text，并且赋值给当前fragment
         */
        magLiveInformationFragment.setListener(new MagLiveInformationFragment.OnMagLiveDataChangeListener() {
            @Override
            public void magLiveDataChange(String content) {
                mFacilityName.setText(content);
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        String editName = PreferencesUtils.getString(getActivity(),"magEditName","客厅摄像头");
        mFacilityName.setText(editName);
    }
}
