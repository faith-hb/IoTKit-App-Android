package com.cylan.jiafeigou.n.view.cam;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.utils.ViewUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WarnEffectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WarnEffectFragment extends Fragment {

    @BindView(R.id.imgV_top_bar_center)
    TextView imgVTopBarCenter;
    @BindView(R.id.fLayout_top_bar_container)
    FrameLayout fLayoutTopBarContainer;
    @BindView(R.id.rb_warn_effect_silence)
    RadioButton rbWarnEffectSilence;
    @BindView(R.id.rb_warn_effect_dog_)
    RadioButton rbWarnEffectDog;
    @BindView(R.id.rb_warn_effect_waring)
    RadioButton rbWarnEffectWaring;
    @BindView(R.id.tv_warn_repeat_mode)
    TextView tvWarnRepeatMode;
    @BindView(R.id.lLayout_warn_repeat_mode)
    LinearLayout lLayoutWarnRepeatMode;
    @BindView(R.id.rg_warn_effect)
    RadioGroup rgWarnEffect;

    public WarnEffectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param args Parameter 1.
     * @return A new instance of fragment WarnEffectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WarnEffectFragment newInstance(Bundle args) {
        WarnEffectFragment fragment = new WarnEffectFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_warn_effect, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        imgVTopBarCenter.setText("设备提示音");
        ViewUtils.setViewPaddingStatusBar(fLayoutTopBarContainer);
        initCheckMode();
    }

    private void initCheckMode() {

        rgWarnEffect.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_warn_effect_silence:
                        break;
                    case R.id.rb_warn_effect_dog_:
                        break;
                    case R.id.rb_warn_effect_waring:
                        break;
                }
            }
        });
    }

    @OnClick({R.id.imgV_top_bar_center, R.id.lLayout_warn_repeat_mode})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgV_top_bar_center:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.lLayout_warn_repeat_mode:
                break;
        }
    }
}