package com.cylan.jiafeigou.n.view.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.cylan.entity.jniCall.JFGFriendAccount;
import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.n.mvp.model.RelAndFriendBean;
import com.cylan.superadapter.IMulItemViewType;
import com.cylan.superadapter.SuperAdapter;
import com.cylan.superadapter.internal.SuperViewHolder;

import java.util.List;


public class ShareToFriendsAdapter extends SuperAdapter<RelAndFriendBean> {

    private OnShareCheckListener listener;
    public interface OnShareCheckListener{
        void onCheck(boolean isCheck,SuperViewHolder holder,RelAndFriendBean item);
    }

    public void setOnShareCheckListener(OnShareCheckListener listener){
        this.listener = listener;
    }

    public ShareToFriendsAdapter(Context context, List<RelAndFriendBean> items, IMulItemViewType<RelAndFriendBean> mulItemViewType) {
        super(context, items, mulItemViewType);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, final int layoutPosition, final RelAndFriendBean item) {
        //TODO 如果没有备注名就显示别人昵称或者账号
        holder.setText(R.id.tv_friend_name,(item.markName==null || item.markName.equals(""))?item.alids:item.markName);
        holder.setText(R.id.tv_friend_account,item.account);
        CheckBox checkBox = (CheckBox) holder.itemView.findViewById(R.id.checkbox_is_share_check);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                item.isCheckFlag = isChecked?1:2;
                listener.onCheck(isChecked,holder,item);
            }
        });
    }

    @Override
    protected IMulItemViewType<RelAndFriendBean> offerMultiItemViewType() {
        return new IMulItemViewType<RelAndFriendBean>() {
            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public int getItemViewType(int position, RelAndFriendBean jfgFriendAccount) {
                return 0;
            }

            @Override
            public int getLayoutId(int viewType) {
                return R.layout.fragment_mine_share_to_friend_items;
            }
        };
    }
}