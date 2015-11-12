package com.hochland386.luna.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vitaly on 10/28/15.
 */
public class DailyWeather extends Weather implements Parcelable {

    //    DailyWeather-specific members
    private long mTimeStamp;
    private String mConditionGroup;

    //    Constructor
    public DailyWeather(int temperature, int humidity, int pressure, String summary, long timeStamp, String conditionGroup) {
        super(temperature, humidity, pressure, summary);
        mTimeStamp = timeStamp;
        mConditionGroup = conditionGroup;
    }

    //    Override default constructor
    public DailyWeather() {
        super();
        mTimeStamp = 0;
        mConditionGroup = "Clear";
    }

//    DailyWeather-specific getters

    /**
     * Returns timestamp value in seconds
     *
     * @return long timeStamp
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Returns conditionGroup value
     *
     * @return String conditionGroup
     */
    public String getConditionGroup() {
        return mConditionGroup;
    }

//    Implements Parcelable interface methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getTemperature());
        dest.writeInt(getHumidity());
        dest.writeInt(getPressure());
        dest.writeString(getSummary());
        dest.writeLong(mTimeStamp);
        dest.writeString(mConditionGroup);
    }

//    Private constructor for Parcel data
    private DailyWeather(Parcel in) {
        this(
                in.readInt(),
                in.readInt(),
                in.readInt(),
                in.readString(),
                in.readLong(),
                in.readString()
        );
    }

    public static final Creator<DailyWeather> CREATOR = new Creator<DailyWeather>() {
        @Override
        public DailyWeather createFromParcel(Parcel source) {
            return new DailyWeather(source);
        }

        @Override
        public DailyWeather[] newArray(int size) {
            return new DailyWeather[size];
        }
    };
}
