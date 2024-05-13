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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */

package com.evp.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.evp.commonlib.utils.LogUtils;
import com.evp.device.Device;
import com.evp.eventbus.NoticeSwipe;
import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;
import com.pax.dal.exceptions.PiccDevException;

/**
 * emv contactless light view list
 */
public class ClssLightsView extends LinearLayout {

    private ClssLight[] lights = new ClssLight[4];

    private AlphaAnimation blinking;
    private class LedThread extends Thread{
        /**
         * The Index.
         */
        protected volatile int index;
        @Override
        public void run() {
            try {
                while (index != -1) {
                    Device.setPiccLedWithException(index, ClssLight.ON);
                    SystemClock.sleep(300);
                    Device.setPiccLedWithException(index, ClssLight.OFF);
                    SystemClock.sleep(300);
                }
            } catch (PiccDevException e) {
                LogUtils.e("ClssLightsView", "", e);
                if (e.getErrCode() == NoticeSwipe.FUNC_SEARCH_CLOSED) {
                    FinancialApplication.getApp().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setLights(3, ClssLight.OFF);
                        }
                    });
                }
            }
        }
    }

    private LedThread ledThread = null;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Device.setPiccLed(-1, ClssLight.OFF);
        if (ledThread != null && ledThread.isAlive()){
            ledThread.interrupt();
            ledThread = null;
        }
    }

    /**
     * Instantiates a new Clss lights view.
     *
     * @param context the context
     */
    public ClssLightsView(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Clss lights view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public ClssLightsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new Clss lights view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public ClssLightsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater mInflater = LayoutInflater.from(context);
        View myView = mInflater.inflate(R.layout.clss_light_layout, null);
        LayoutParams parentParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        parentParams.setLayoutDirection(HORIZONTAL);
        addView(myView, parentParams);

        init();
    }

    private void init() {
        blinking = new AlphaAnimation(1, 0);
        blinking.setDuration(500);
        blinking.setRepeatCount(Animation.INFINITE);
        blinking.setRepeatMode(Animation.REVERSE);

        lights[0] = (ClssLight) findViewById(R.id.light1);
        lights[1] = (ClssLight) findViewById(R.id.light2);
        lights[2] = (ClssLight) findViewById(R.id.light3);
        lights[3] = (ClssLight) findViewById(R.id.light4);
    }

    /**
     * set Light status
     *
     * @param index  index
     * @param status status
     */
    public void setLights(final @IntRange(from = -1, to = 3) int index, @ClssLight.STATUS int status) {
        if (ledThread == null) {
            ledThread = new LedThread();
            ledThread.index = index;
            ledThread.start();
        }else {
            ledThread.index = index;
        }
        for (int i = 0; i < lights.length; ++i) {
            if (index == i) {
                lights[i].setStatus(status, blinking);
            } else {
                lights[i].setStatus(ClssLight.OFF, null);
            }
        }
    }
}
