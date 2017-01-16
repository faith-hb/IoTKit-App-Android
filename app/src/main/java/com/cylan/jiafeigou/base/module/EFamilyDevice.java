package com.cylan.jiafeigou.base.module;

import android.os.Parcel;

import com.cylan.annotation.DPProperty;
import com.cylan.jiafeigou.dp.DpMsgDefine;

import static com.cylan.jiafeigou.dp.DpMsgMap.ID_201_NET;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_401_BELL_CALL_STATE;
import static com.cylan.jiafeigou.dp.DpMsgMap.ID_402_BELL_VOICE_MSG;

/*
 *  @项目名：  JFGAndroid 
 *  @包名：    com.cylan.jiafeigou.base.module
 *  @文件名:   EFamilyDevice
 *  @创建者:   yanzhendong
 *  @创建时间:  2017/1/8 11:36
 *  @描述：    TODO
 */
public class EFamilyDevice extends JFGDevice {
    @DPProperty(msgId = ID_201_NET)
    public DpMsgDefine.DPNet net;
    @DPProperty(msgId = ID_401_BELL_CALL_STATE)
    public DpMsgDefine.DPSet<DpMsgDefine.DPBellCallRecord> bell_call_state;
    @DPProperty(msgId = ID_402_BELL_VOICE_MSG)
    public DpMsgDefine.DPPrimary<Integer> bell_voice_msg;

    EFamilyDevice() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(this.net, flags);
        dest.writeParcelable(this.bell_call_state, flags);
    }

    EFamilyDevice(Parcel in) {
        super(in);
        this.net = in.readParcelable(DpMsgDefine.DPNet.class.getClassLoader());
        this.bell_call_state = in.readParcelable(DpMsgDefine.DPBellCallRecord.class.getClassLoader());
    }

    public static final Creator<EFamilyDevice> CREATOR = new Creator<EFamilyDevice>() {
        @Override
        public EFamilyDevice createFromParcel(Parcel source) {
            return new EFamilyDevice(source);
        }

        @Override
        public EFamilyDevice[] newArray(int size) {
            return new EFamilyDevice[size];
        }
    };
}