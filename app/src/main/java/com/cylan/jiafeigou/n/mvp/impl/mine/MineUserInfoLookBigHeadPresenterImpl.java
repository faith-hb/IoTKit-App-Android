package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.cylan.jiafeigou.misc.JConstant;
import com.cylan.jiafeigou.n.mvp.contract.mine.MineUserInfoLookBigHeadContract;
import com.cylan.jiafeigou.utils.PreferencesUtils;

/**
 * 作者：zsl
 * 创建时间：2016/9/2
 * 描述：
 */
public class MineUserInfoLookBigHeadPresenterImpl implements MineUserInfoLookBigHeadContract.Presenter {

    private MineUserInfoLookBigHeadContract.View view;
    private Context context;

    public MineUserInfoLookBigHeadPresenterImpl(MineUserInfoLookBigHeadContract.View view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void loadImage(ImageView imageView) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
