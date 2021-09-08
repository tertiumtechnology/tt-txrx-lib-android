package com.tertiumtechnology.txrxlib.rw;

import android.os.Parcel;
import android.os.Parcelable;

public class TxRxTimestamps implements Parcelable {

    public static final Creator<TxRxTimestamps> CREATOR = new Creator<TxRxTimestamps>() {
        @Override
        public TxRxTimestamps createFromParcel(Parcel in) {
            return new TxRxTimestamps(in);
        }

        @Override
        public TxRxTimestamps[] newArray(int size) {
            return new TxRxTimestamps[size];
        }
    };
    private long beginWriteTime;
    private long endWriteTime;
    private long beginNotifyTime;
    private long endNotifyTime;

    protected TxRxTimestamps(Parcel in) {
        beginWriteTime = in.readLong();
        endWriteTime = in.readLong();
        beginNotifyTime = in.readLong();
        endNotifyTime = in.readLong();
    }

    public TxRxTimestamps() {
        super();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getBeginNotifyTime() {
        return beginNotifyTime;
    }

    public void setBeginNotifyTime(long beginNotifyTime) {
        this.beginNotifyTime = beginNotifyTime;
    }

    public long getBeginWriteTime() {
        return beginWriteTime;
    }

    public void setBeginWriteTime(long beginWriteTime) {
        this.beginWriteTime = beginWriteTime;
    }

    public long getEndNotifyTime() {
        return endNotifyTime;
    }

    public void setEndNotifyTime(long endNotifyTime) {
        this.endNotifyTime = endNotifyTime;
    }

    public long getEndWriteTime() {
        return endWriteTime;
    }

    public void setEndWriteTime(long endWriteTime) {
        this.endWriteTime = endWriteTime;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(beginWriteTime);
        dest.writeLong(endWriteTime);
        dest.writeLong(beginNotifyTime);
        dest.writeLong(endNotifyTime);
    }
}
