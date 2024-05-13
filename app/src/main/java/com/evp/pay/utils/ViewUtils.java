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

import static android.util.TypedValue.COMPLEX_UNIT_PX;
import static com.pax.dal.IDeviceInfo.MODULE_KEYBOARD;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;
import com.pax.dal.IDeviceInfo;

import java.lang.reflect.Method;

/**
 * The type View utils.
 */
public class ViewUtils {

    private ViewUtils() {
        //do nothing
    }

    private static Resources res;
    static {
        res = FinancialApplication.getApp().getResources();
    }

    /**
     * 生成每一行记录
     *
     * @param context the context
     * @param title   the title
     * @param value   the value
     * @return linear layout
     */
    public static LinearLayout genSingleLineLayout(Context context, String title, Object value) {
        String str = String.valueOf(value);
        float size = 0;
        if (title.length() > 1) {
            if (title.startsWith("\\*")) {
                size = context.getResources().getDimension(R.dimen.font_size_value);
                title = title.substring(2);
            }
            if (title.startsWith("\\-")) {
                size = context.getResources().getDimension(R.dimen.font_size_prompt);
                title = title.substring(2);
            }
        }

        if (str.isEmpty())
            return null;

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);

        if (title.isEmpty() && str.equals("-")) {
            View vw = new View(context);
            vw.setBackgroundColor(Color.GRAY);
            layout.addView(vw, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 2));
            return layout;
        }


        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setTextSize(COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.font_size_prompt));
        titleTv.setTextColor(context.getResources().getColor(android.R.color.primary_text_light));

        layout.addView(titleTv, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));

        TextView valueTv = new TextView(context);
        valueTv.setText(str);
        valueTv.setGravity(Gravity.END);
        valueTv.setTextSize(COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.font_size_value));


        if (size > 0) {
            titleTv.setTextSize(COMPLEX_UNIT_PX, size);
            valueTv.setTextSize(COMPLEX_UNIT_PX, size);
        }

        layout.addView(valueTv, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));

        return layout;
    }

    /**
     * 得到设备屏幕的宽度
     *
     * @param context the context
     * @return the screen width
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 得到设备屏幕的高度
     *
     * @param context the context
     * @return the screen height
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * Is screen orientation portrait boolean.
     *
     * @param context the context
     * @return the boolean
     */
    public static boolean isScreenOrientationPortrait(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Dp 2 px int.
     *
     * @param dpValue the dp value
     * @return the int
     */
    public static int dp2px(float dpValue){
        return (int) (dpValue*res.getDisplayMetrics().density+0.5f);
    }

    /**
     * Px 2 dp int.
     *
     * @param pxValue the px value
     * @return the int
     */
    public static int px2dp(float pxValue){
        return (int) (pxValue/res.getDisplayMetrics().density+0.5f);
    }

    /**
     * whether show system keyboard when EditText focused
     *
     * @param et   EditText
     * @param show whether show
     */
    public static void showInput(EditText et,boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            et.setShowSoftInputOnFocus(show);
            return;
        }
        Class<EditText> cls = EditText.class;
        Method method;
        try {
            method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(true);
            method.invoke(et, show);
        } catch (Exception e) {
            LogUtils.e("disableShowInput",e.getMessage());
        }
    }

    /**
     * disable system keyboard when EditText focused on Device with Physical keyboard
     *
     * @param et EditText
     */
    public static void configInput(EditText et) {
        IDeviceInfo.ESupported keyboradSupported = FinancialApplication.getDal().getDeviceInfo().getModuleSupported(MODULE_KEYBOARD);
        if (keyboradSupported != IDeviceInfo.ESupported.YES){
            return;
        }
        boolean enableKeyEvent = FinancialApplication.getDal().getSys().enableKeyEvent();
        if (!enableKeyEvent){
            return;
        }
        showInput(et,false);
    }

    /**
     * try get host activity from view.
     * views hosted on floating window like dialog     and toast will sure return null.
     *
     * @param view the view
     * @return host activity; or null if not available
     */
    public static Activity getActivityFromView(View view) {
        Context context = view.getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }
}
