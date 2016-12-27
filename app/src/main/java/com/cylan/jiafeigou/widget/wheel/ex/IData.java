package com.cylan.jiafeigou.widget.wheel.ex;

import com.cylan.entity.jniCall.JFGVideo;

import java.util.ArrayList;

/**
 * Created by cylan-hunt on 16-12-19.
 */

public interface IData {
    void flattenData(ArrayList<JFGVideo> list);

    long[] getTimeArray(int leftIndex, int maxCount);

    /**
     * 延展过后的最小时间
     *
     * @return
     */
    long getFlattenMinTime();

    /**
     * 延展过后的最大时间
     *
     * @return
     */
    long getFlattenMaxTime();

    /**
     * 数据总量
     *
     * @return
     */
    int getDataCount();

    /**
     * 根据时间找出{整点,半小时}
     *
     * @param time
     * @return
     */
    int getBottomType(long time);

    String getDateInFormat(long time);

    ArrayList<JFGVideo> getMaskList(long start, long end);

    /**
     * 快速滑动,或者滑动停止后,有一个自动定位的需求,---a--A----b---B---c----C--d--
     *
     * @param time
     * @return
     */
    long getNextFocusTime(long time, int considerDirection);

    boolean isHotRect(long time);
}