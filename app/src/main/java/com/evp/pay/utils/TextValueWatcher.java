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
package com.evp.pay.utils;

import android.text.Editable;
import android.text.TextWatcher;

import com.evp.device.Device;

/**
 * The type Text value watcher.
 *
 * @param <T> the type parameter
 */
public class TextValueWatcher<T> implements TextWatcher {

    private final T minValue;
    private final T maxValue;
    private boolean mEditing;
    private OnCompareListener compareListener;
    private OnTextChangedListener textChangedListener;

    /**
     * Instantiates a new Text value watcher.
     *
     * @param minValue the min value
     * @param maxValue the max value
     */
    public TextValueWatcher(T minValue, T maxValue) {
        mEditing = false;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mEditing) {
            mEditing = true;
            String temp = s.toString();
            if (temp.isEmpty())
                temp = "0";
            if (compareListener != null && compareListener.onCompare(temp, minValue, maxValue)) {
                if (textChangedListener != null)
                    textChangedListener.afterTextChanged(temp);
            } else {
                Device.beepErr();
                s.replace(0, s.length(), temp, 0, temp.length() - 1);
            }
            mEditing = false;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //do nothing
    }

    /**
     * Sets on compare listener.
     *
     * @param listener the listener
     */
    public void setOnCompareListener(OnCompareListener listener) {
        this.compareListener = listener;
    }

    /**
     * The interface On compare listener.
     */
    public interface OnCompareListener {
        /**
         * On compare boolean.
         *
         * @param value the value
         * @param min   the min
         * @param max   the max
         * @return the boolean
         */
        boolean onCompare(String value, Object min, Object max);
    }

    /**
     * Sets on text changed listener.
     *
     * @param listener the listener
     */
    public void setOnTextChangedListener(OnTextChangedListener listener) {
        this.textChangedListener = listener;
    }

    /**
     * The interface On text changed listener.
     */
    public interface OnTextChangedListener {
        /**
         * After text changed.
         *
         * @param value the value
         */
        void afterTextChanged(String value);
    }

}
