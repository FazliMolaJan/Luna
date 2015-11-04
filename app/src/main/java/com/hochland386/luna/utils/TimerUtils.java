package com.hochland386.luna.utils;

import android.os.CountDownTimer;

import com.hochland386.luna.bus.TimerTimeoutEvent;

import de.greenrobot.event.EventBus;

/**
 * Created by vitaly on 10/17/15.
 * This class provides timers with callback and control interfaces
 */
public class TimerUtils {

//    Members
    private CountDownTimer mLocationTimer;

//    Make default constructor private
    private TimerUtils() {
    }

//    Singleton wrapper
    private static class Loader {
        static TimerUtils instance = new TimerUtils();
    }

//    Implements getInstance() method
    public static TimerUtils getInstance() {
        return Loader.instance;
    }

//    Public interface
    /**
     * Starts location timeout timer with default timeout and countdown interval values.
     * You can cancel this timer by calling cancelLocationTimeoutTimer();
     * If a timeout occurs TimerTimeoutEvent will be posted
     */
    public void startLocationTimeoutTimer() {
        mLocationTimer = new CountDownTimer(Constants.LOCATION_TIMEOUT, Constants.COUNT_DOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
//                Do nothing :)
            }

            @Override
            public void onFinish() {
                EventBus.getDefault().post(new TimerTimeoutEvent());
            }
        };
        mLocationTimer.start();
    }

    /**
     * Cancel location timeout timer.
     */
    public void cancelLocationTimeoutTimer() {
        mLocationTimer.cancel();
    }
}
