package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MediaBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * Created by chen on 6/6/16.
 */


public class HomeWonderAdapter extends SuperAdapter<MediaBean> {


    private WonderfulItemClickListener deviceItemClickListener;
    private WonderfulItemLongClickListener deviceItemLongClickListener;


    public HomeWonderAdapter(Context context, List<MediaBean> items,
                             IMulItemViewType<MediaBean> mulItemViewType) {
        super(context, items, mulItemViewType);

    }

    public void setWonderfulItemClickListener(WonderfulItemClickListener deviceItemClickListener) {
        this.deviceItemClickListener = deviceItemClickListener;
    }

    public void setWonderfulItemLongClickListener(WonderfulItemLongClickListener deviceItemLongClickListener) {
        this.deviceItemLongClickListener = deviceItemLongClickListener;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MediaBean item) {
        initClickListener(holder, viewType, layoutPosition);
        handleState(holder, item);
    }

    private void initClickListener(SuperViewHolder holder, final int viewType, final int layoutPosition) {
        setupPosition2View(holder, R.id.rLayout_wonderful_item_wonder, layoutPosition);
        setupPosition2View(holder, R.id.tv_wonderful_item_share, layoutPosition);
        setupPosition2View(holder, R.id.tv_wonderful_item_delete, layoutPosition);

        holder.setOnClickListener(R.id.rLayout_wonderful_item_wonder, deviceItemClickListener);
        holder.setOnClickListener(R.id.tv_wonderful_item_share, deviceItemClickListener);
        holder.setOnClickListener(R.id.tv_wonderful_item_delete, deviceItemClickListener);
        holder.setOnLongClickListener(R.id.rLayout_wonderful_item_wonder, deviceItemLongClickListener);
    }

    private void setupPosition2View(SuperViewHolder holder, final int viewId, final int position) {
        final View view = holder.getView(viewId);
        if (view != null) {
            view.setTag(position);
        }
    }

    private void handleState(SuperViewHolder holder, MediaBean bean) {


        //时间
        holder.setText(R.id.tv_wonderful_item_date, bean.timeInStr);

        //图标
        holder.setBackgroundResource(R.id.iv_wonderful_item_content, R.drawable.bg_home_title_daytime);

        //来自摄像头
        holder.setText(R.id.tv_wonderful_item_device_name, bean.srcUrl);
    }

    @Override
    protected IMulItemViewType<MediaBean> offerMultiItemViewType() {
        return new IMulItemViewType<MediaBean>() {
            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public int getItemViewType(int position, MediaBean mediaBean) {
                return mediaBean.mediaType;//0:image view  1:videoView
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == 0 ?
                        R.layout.layout_item_vedio_wonderful :
                        R.layout.layout_item_picture_wonderful;
            }
        };
    }

    public interface WonderfulItemClickListener extends View.OnClickListener {

    }

    public interface WonderfulItemLongClickListener extends View.OnLongClickListener {

    }

//    public interface WonderfulItemShareListener extends View.OnClickListener {
//    }
//
//    public interface WonderfulItemDeleteListener extends View.OnClickListener {
//    }
}
