package com.cylan.jiafeigou.n.mvp.contract.cloud;

import com.cylan.jiafeigou.n.mvp.BasePresenter;
import com.cylan.jiafeigou.n.mvp.BaseView;

/**
 * 作者：zsl
 * 创建时间：2016/9/26
 * 描述：
 */
public interface CloudLiveCallInContract {

    interface View extends BaseView<Presenter> {
    }


    interface Presenter extends BasePresenter {
        void setVideoTalkFinishFlag(boolean isFinish);

        void setVideoTalkFinishResultData(String data);

        void bindService();
    }
}