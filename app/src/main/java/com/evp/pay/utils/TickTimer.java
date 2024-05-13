/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2019-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date                  Author	                 Action
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.pay.utils;

import android.os.CountDownTimer;

/**
 * The type Tick timer.
 */
public class TickTimer {

    /**
     * The constant DEFAULT_TIMEOUT.
     */
    public static final int DEFAULT_TIMEOUT = 60;

    /**
     * The interface On tick timer listener.
     */
    public interface OnTickTimerListener {
        /**
         * On tick.
         *
         * @param leftTime the left time
         */
        void onTick(long leftTime);

        /**
         * On finish.
         */
        void onFinish();
    }

    private Timer timer;
    private OnTickTimerListener listener;

    private static class Timer extends CountDownTimer {

        private OnTickTimerListener listener;

        /**
         * Instantiates a new Timer.
         *
         * @param timeout      the timeout
         * @param tickInterval the tick interval
         */
        Timer(long timeout, long tickInterval) {
            super(timeout * 1000, tickInterval * 1000);
        }

        /**
         * Sets time count listener.
         *
         * @param listener the listener
         */
        void setTimeCountListener(OnTickTimerListener listener) {
            this.listener = listener;
        }

        @Override
        public void onFinish() {
            if (listener != null)
                listener.onFinish();
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (listener != null)
                listener.onTick(millisUntilFinished / 1000);
        }
    }

    /**
     * Instantiates a new Tick timer.
     *
     * @param listener the listener
     */
    public TickTimer(OnTickTimerListener listener) {
        this.listener = listener;
    }

    /**
     * start timing record
     */
    public void start() {
        if (timer != null) {
            timer.cancel();
        }
        updateTimer(DEFAULT_TIMEOUT);
        timer.start();
    }

    /**
     * start timing record
     *
     * @param timeout timeout
     */
    public void start(int timeout) {
        if (timer != null) {
            timer.cancel();
        }
        updateTimer(timeout);
        timer.start();
    }

    /**
     * stop timing record
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void updateTimer(int timeout) {
        timer = new Timer(timeout, 1);
        timer.setTimeCountListener(listener);
    }
}
