package com.cylan.jiafeigou.n.view.mine;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineRelativesAndFriendListShareDevicesToContract;
import com.cylan.jiafeigou.n.mvp.impl.mine.MineRelativesAndFriendListShareDevicesPresenterImp;
import com.cylan.jiafeigou.n.view.adapter.ChooseShareDeviceAdapter;
import com.cylan.jiafeigou.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineRelativesAndFriendsListShareDevicesFragment extends Fragment implements MineRelativesAndFriendListShareDevicesToContract.View {

    @BindView(R.id.iv_mine_friends_share_devices_back)
    ImageView ivMineFriendsShareDevicesBack;
    @BindView(R.id.iv_mine_friends_share_devices_ok)
    ImageView ivMineFriendsShareDevicesOk;
    @BindView(R.id.iv_mine_personal_mailbox_bind_disable)
    ImageView ivMinePersonalMailboxBindDisable;
    @BindView(R.id.rcy_share_device_list)
    RecyclerView rcyShareDeviceList;

    private MineRelativesAndFriendListShareDevicesToContract.Presenter presenter;

    public static MineRelativesAndFriendsListShareDevicesFragment newInstance() {
        return new MineRelativesAndFriendsListShareDevicesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine_relativeandfriend_share_devices, container, false);
        ButterKnife.bind(this, view);
        initPresenter();
        initRecycleView();
        return view;
    }

    private void initPresenter() {
        presenter = new MineRelativesAndFriendListShareDevicesPresenterImp(this);
    }

    /**
     * desc：初始化列表
     */
    private void initRecycleView() {
        if (presenter.checkListEmpty(presenter.getDeviceData())){
            ivMinePersonalMailboxBindDisable.setImageDrawable(getResources().getDrawable(R.drawable.icon_finish_disable));
        }
        rcyShareDeviceList.setLayoutManager(new LinearLayoutManager(getContext()));
        ChooseShareDeviceAdapter chooseShareDeviceAdapter =  new ChooseShareDeviceAdapter(presenter.getDeviceData());
        rcyShareDeviceList.setAdapter(chooseShareDeviceAdapter);
    }

    @Override
    public void setPresenter(MineRelativesAndFriendListShareDevicesToContract.Presenter presenter) {

    }

    @OnClick({R.id.iv_mine_friends_share_devices_back, R.id.iv_mine_friends_share_devices_ok})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_mine_friends_share_devices_back:
                getFragmentManager().popBackStack();
                break;
            case R.id.iv_mine_friends_share_devices_ok:
                ToastUtil.showToast(getContext(), "分享成功。。。");
                break;
        }
    }

}