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
 * 20190108  	         Steven.S                Create
 * ===========================================================================================
 */

package com.evp.pay.utils.lightscanner.tool;

import android.app.Activity;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Finishes an activity after a period of inactivity.
 * Created by Steven.S on 2018/5/24/0024.
 */
public final class InactivityTimer {
    private int INACTIVITY_DELAY_SECONDS = 2 * 60;
    private final ScheduledExecutorService inactivityTimer =
            Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
    private final Activity activity;
    private ScheduledFuture<?> inactivityFuture = null;

    /**
     * Instantiates a new Inactivity timer.
     *
     * @param activity the activity
     */
    public InactivityTimer(Activity activity){
        this.activity = activity;
        onActivity();
    }

    /**
     * Instantiates a new Inactivity timer.
     *
     * @param activity the activity
     * @param timeout  the timeout
     */
    public InactivityTimer(Activity activity, int timeout){
        this.activity = activity;
        this.INACTIVITY_DELAY_SECONDS = timeout;
        onActivity();
    }

    /**
     * On activity.
     */
    public void onActivity(){
        cancel();
        inactivityFuture = inactivityTimer.schedule(new FinishListener(activity),
                INACTIVITY_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private void cancel(){
        if(inactivityFuture != null){
            inactivityFuture.cancel(true);
            inactivityFuture = null;
        }
    }

    /**
     * Shutdown.
     */
    public void shutdown(){
        cancel();
        inactivityTimer.shutdown();
    }

    private static final class  DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable runnable){
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        }
    }
}
