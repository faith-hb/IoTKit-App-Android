package com.cylan.jiafeigou.n.mvp.model;

import java.io.Serializable;

/**
 * 作者：zsl
 * 创建时间：2017/1/11
 * 描述：
 */
public class CloudLiveCallOutBean implements Serializable {

    public String getVideoLength() {
        return videoLength;
    }

    public void setVideoLength(String videoLength) {
        this.videoLength = videoLength;
    }


    public boolean isHasConnet() {
        return hasConnet;
    }

    public void setHasConnet(boolean hasConnet) {
        this.hasConnet = hasConnet;
    }

    public String getVideoTime() {
        return videoTime;
    }

    public void setVideoTime(String videoTime) {
        this.videoTime = videoTime;
    }

    public String getUserIcon() {
        return userIcon;
    }

    public void setUserIcon(String userIcon) {
        this.userIcon = userIcon;
    }

    public String videoTime;
    public boolean hasConnet;
    public String videoLength;
    public String userIcon;

}