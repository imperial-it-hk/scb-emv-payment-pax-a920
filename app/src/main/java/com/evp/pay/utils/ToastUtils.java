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

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.annotation.UiThread;

import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;

/**
 * The type Toast utils.
 */
@UiThread
public class ToastUtils {

    /**
     * old message
     */
    private static String oldMsg;
    /**
     * Toast object
     */
    private static Toast toast = null;
    /**
     * first time
     */
    private static long oneTime = 0;
    /**
     * second time
     */
    private static long twoTime = 0;

    private ToastUtils() {
        //do nothing
    }

    /**
     * Show message.
     *
     * @param strId the str id
     */
    public static void showMessage(@StringRes int strId) {
        showMessage(FinancialApplication.getApp(), FinancialApplication.getApp().getString(strId));
    }

    /**
     * Show message.
     *
     * @param message the message
     */
    public static void showMessage(String message) {
        showMessage(FinancialApplication.getApp(), message);
    }

    /**
     * Show message.
     *
     * @param context the context
     * @param message the message
     */
    @UiThread
    public static void showMessage(Context context, String message) {
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflate.inflate(R.layout.toast_layout, null);
        TextView textView = (TextView) view.findViewById(R.id.message);
        if (toast == null) {
            textView.setText(message);
            toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);// set gravity center
            toast.setView(view);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();

            if (message.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = message;
                textView.setText(message);
                toast.setView(view);
                toast.show();
            }
        }

        oneTime = twoTime;
    }
}
