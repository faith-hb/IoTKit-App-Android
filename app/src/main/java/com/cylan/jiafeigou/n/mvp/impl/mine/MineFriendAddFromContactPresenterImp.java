package com.cylan.jiafeigou.n.mvp.impl.mine;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cylan.jiafeigou.n.mvp.contract.mine.MineFriendAddFromContactContract;
import com.cylan.jiafeigou.n.mvp.impl.AbstractPresenter;
import com.cylan.jiafeigou.n.mvp.model.SuggestionChatInfoBean;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者：zsl
 * 创建时间：2016/9/6
 * 描述：
 */
public class MineFriendAddFromContactPresenterImp extends AbstractPresenter<MineFriendAddFromContactContract.View> implements MineFriendAddFromContactContract.Presenter {

    private Subscription contactSubscriber;
    private ArrayList<SuggestionChatInfoBean> filterDateList;

    public MineFriendAddFromContactPresenterImp(MineFriendAddFromContactContract.View view) {
        super(view);
        view.setPresenter(this);
    }

    @Override
    public void start() {
        initContactData();
    }

    @Override
    public void stop() {
        if (contactSubscriber != null) {
            contactSubscriber.unsubscribe();
        }
    }

    @Override
    public void initContactData() {

        contactSubscriber = Observable.just(null)
                .subscribeOn(Schedulers.newThread())
                .map(new Func1<Object, ArrayList<SuggestionChatInfoBean>>() {
                    @Override
                    public ArrayList call(Object o) {
                        return getAllContactList();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<SuggestionChatInfoBean>>() {
                    @Override
                    public void call(ArrayList<SuggestionChatInfoBean> arrayList) {
                        handlerDataResult(arrayList);
                    }
                });
    }

    /**
     * desc：处理获取到的联系人数据
     * @param arrayList
     */
    private void handlerDataResult(ArrayList<SuggestionChatInfoBean> arrayList) {
        if (arrayList != null && arrayList.size() != 0 && getView() != null){
            getView().initContactRecycleView(arrayList);
        }else {
            getView().showNoContactView();
        }
    }

    @NonNull
    public ArrayList<SuggestionChatInfoBean> getAllContactList() {
        ArrayList<SuggestionChatInfoBean> list = new ArrayList<SuggestionChatInfoBean>();
        Cursor cursor = null;
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        // 这里是获取联系人表的电话里的信息  包括：名字，名字拼音，联系人id,电话号码；
        // 然后在根据"sort-key"排序
        cursor = getView().getContext().getContentResolver().query(
                uri,
                new String[]{"display_name", "sort_key", "contact_id",
                        "data1"}, null, null, "sort_key");

        if (cursor.moveToFirst()) {
            do {
                SuggestionChatInfoBean contact = new SuggestionChatInfoBean("", 1, "");
                String contact_phone = cursor
                        .getString(cursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String name = cursor.getString(0);
                contact.setContent(contact_phone);
                contact.setName(name);
                if (name != null)
                    list.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    @Override
    public void addContactItem(SuggestionChatInfoBean bean) {

    }

    @Override
    public void filterPhoneData(String filterStr) {
        filterDateList = new ArrayList<>();
        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = getAllContactList();
        } else {
            filterDateList.clear();
            for (SuggestionChatInfoBean s : getAllContactList()) {
                String phone = s.getContent();
                String name = s.getName();
                if (phone.replace(" ", "").contains(filterStr) || name.contains(filterStr)) {
                    filterDateList.add(s);
                }
            }
        }
        handlerDataResult(filterDateList);
    }

}