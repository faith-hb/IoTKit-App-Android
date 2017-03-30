package com.cylan.jiafeigou.cache.db.module;

import com.cylan.entity.jniCall.JFGDPMsg;
import com.cylan.jiafeigou.dp.DataPoint;

import java.util.ArrayList;

/**
 * Created by yanzhendong on 2017/3/25.
 */

public interface IPropertyHolder {
    <V> V $(int msgId, V defaultValue);

    ArrayList<JFGDPMsg> getQueryParams();

    boolean setValue(int msgId, byte[] bytes, long version);

    boolean setValue(int msgId, DataPoint value);
}