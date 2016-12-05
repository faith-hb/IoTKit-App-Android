package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.MagBean;
import com.cylan.jiafeigou.utils.ContextUtils;
import com.cylan.jiafeigou.widget.FateLineView;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 创建者     谢坤
 * 创建时间   2016/8/5 11:23
 * 描述	      ${TODO}
 * <p/>
 * 更新者     $Author$
 * 更新时间   $Date$
 * 更新描述   ${TODO}
 */
public class MagActivityAdapter extends SuperAdapter<MagBean> {


    private static final int TYPE_COUNT = 2;

    private boolean currentState;             //true 为开， false为关

    private static final int TYPE_VISIBLE = 0;//正常显示类型

    private static final int TYPE_INVISIBLE = 1;//不显示类型

    public MagActivityAdapter(Context context, List<MagBean> items,
                              IMulItemViewType<MagBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    public void setCurrentState(boolean currentState){
        this.currentState = currentState;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int layoutPosition, MagBean item) {

        if (viewType == TYPE_VISIBLE) {
            //initVisible(holder, layoutPosition);
            handleVisibleState(holder, layoutPosition, item);
        } else if (viewType == TYPE_INVISIBLE) {
            //initInvisible(holder, layoutPosition);
        }
    }

    private void initVisible(SuperViewHolder holder, final int layoutPosition) {
        setupPosition2View(holder, R.id.tv_mag_live_day, layoutPosition);
        setupPosition2View(holder, R.id.tv_mag_live_time, layoutPosition);
        setupPosition2View(holder, R.id.iv_mag_live, layoutPosition);
    }

    private void initInvisible(SuperViewHolder holder, final int layoutPosition) {
        setupPosition2View(holder, R.id.flv_mag_live_invisible, layoutPosition);
    }

    private void setupPosition2View(SuperViewHolder holder, final int viewId, final int position) {
        final View view = holder.getView(viewId);
        if (view != null) {
            view.setTag(position);
        }
    }

    private void handleVisibleState(SuperViewHolder holder, int layoutPosition, MagBean bean) {
        //每条的第一个设置内外圈颜色
        ImageView view = (ImageView) holder.getView(R.id.iv_mag_live);
        if (bean.isFirst) {
            if (currentState){
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icon_dot_red));
            }else {
                view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icon_dot_green));
            }
        }else {
            view.setImageDrawable(getContext().getResources().getDrawable(R.drawable.icon_dot_gary));
        }

        if (layoutPosition == 0){
            if (checkIsToday(bean.getMagTime())){
                holder.setText(R.id.tv_mag_live_day, ContextUtils.getContext().getString(R.string.DOOR_TODAY));
            }else {
                holder.setText(R.id.tv_mag_live_day, getDate(bean.magTime) + ContextUtils.getContext().getString(R.string.MONTHS));
            }
        }else {
            if (checkSame(bean.magTime,getList().get(layoutPosition-1).magTime)){
                holder.setText(R.id.tv_mag_live_day, "");
            }else {
                holder.setText(R.id.tv_mag_live_day, getDate(bean.magTime) + ContextUtils.getContext().getString(R.string.MONTHS));
            }
        }

        if (bean.isOpen) {
            holder.setText(R.id.tv_mag_live_time, longToDate(bean.magTime) + " " + ContextUtils.getContext().getString(R.string.MAGNETISM_ON));
        } else {
            holder.setText(R.id.tv_mag_live_time, longToDate(bean.magTime) + " " + ContextUtils.getContext().getString(R.string.MAGNETISM_OFF));
        }
    }

    /**
     * 检测是否是今天
     * @param magTime
     * @return
     */
    public boolean checkIsToday(long magTime) {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date = new Date(magTime);
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            if (diffDay == 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * 检测是否和前一天相等
     * @return
     */
    public boolean checkSame(long thisTime,long lastTime) {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(thisTime);
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date = new Date(lastTime);
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            if (diffDay == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected IMulItemViewType<MagBean> offerMultiItemViewType() {
        return new IMulItemViewType<MagBean>() {
            @Override
            public int getViewTypeCount() {
                return TYPE_COUNT;
            }

            @Override
            public int getItemViewType(int position, MagBean magBean) {
                return magBean.visibleType; //0.正常显示 ，1.只显示一条时间线的虚线
            }

            @Override
            public int getLayoutId(int viewType) {
                return viewType == TYPE_VISIBLE ?
                        R.layout.activity_mag_live_item :
                        R.layout.activity_mag_live_item_invisible;
            }
        };
    }

    /**
     * 获得当前日期的方法
     *
     * @param magDate
     */
    public String getDate(long magDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M");
        String nowDate = sdf.format(new Date(magDate));
        return nowDate;
    }

    /**
     * long类型转换为时间值类型
     */
    public String longToDate(long lo) {
        Date date = new Date(lo);
        SimpleDateFormat sd = new SimpleDateFormat("HH:mm:ss");
        return sd.format(date);
    }

}
