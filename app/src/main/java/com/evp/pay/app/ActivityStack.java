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
 * 20190108  	         Joshua Huang            Create
 * ===========================================================================================
 */
package com.evp.pay.app;

import android.app.Activity;

import com.evp.commonlib.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.Stack;


/**
 * Created by zhangyp on 2019/4/19
 */
public class ActivityStack {
    private static final String TAG = "ActivityStack";
    private WeakReference<Activity> sCurrentActivityWeakRef;
    private Stack<Activity> activities = new Stack<>();

    /**
     * Pop.
     *
     * @param activity the activity
     */
    public void pop(Activity activity) {
        if (activity != null) {
            remove(activity);
        }
    }

    /**
     * Pop.
     */
    public void pop() {
        pop(activities.peek());
    }

    /**
     * pop to the specific activity and finish it
     *
     * @param activity the target activity
     */
    public void popTo(Activity activity) {
        if (activity != null) {
            while (true) {
                Activity lastCurrent;
                try {
                    lastCurrent = activities.peek();
                } catch (Exception e) {
                    LogUtils.e(e);
                    lastCurrent = null;
                }
                if (lastCurrent == null || activity == lastCurrent) {
                    return;
                }
                remove(lastCurrent);
            }
        }
    }

    /**
     * pop to the specific activity and finish it
     *
     * @param clz the class of the target activity
     */
    public void popTo(Class clz) {
        if (clz != null) {
            while (true) {
                Activity lastCurrent = activities.peek();
                if (lastCurrent == null || clz == lastCurrent.getClass()) {
                    return;
                }
                remove(lastCurrent);
            }
        }
    }

    /**
     * get the top activity of the stack
     *
     * @return the top activity
     */
    public Activity top() {
        return sCurrentActivityWeakRef!=null?sCurrentActivityWeakRef.get():null;
    }

    /**
     * Sets top.
     *
     * @param activity the activity
     */
    public void setTop(Activity activity) {
        sCurrentActivityWeakRef = new WeakReference(activity);
    }

    /**
     * push an activity the the top
     *
     * @param activity the target activity
     */
    public void push(Activity activity) {
        if (activity != null) {
            activities.add(activity);
        }
    }

    /**
     * pop all activities from the stack and finish them
     */
    public void popAll() {
        if (!activities.isEmpty()) {
            while (true) {
                try {
                    Activity activity;
                    if (activities.peek() != null) {
                        activity = activities.peek();
                    } else {
                        break;
                    }
                    remove(activity);
                } catch (Exception e) {
                    LogUtils.e(e);
                    LogUtils.d(TAG, "", e);
                    break;
                }
            }
        }
    }

    /**
     * remove an activity from the stack and finish it
     *
     * @param activity the target activity
     */
    private void remove(Activity activity) {
        activity.finish();
        activities.remove(activity);
    }

    private static ActivityStack instance = null;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static ActivityStack getInstance() {
        if (instance == null) {
            instance = new ActivityStack();
        }
        return instance;
    }
}
