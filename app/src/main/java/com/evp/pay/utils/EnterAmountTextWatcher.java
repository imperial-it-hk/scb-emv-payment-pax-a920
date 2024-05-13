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

import android.text.Editable;
import android.text.TextWatcher;

import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.device.Device;

/**
 * The type Enter amount text watcher.
 */
public class EnterAmountTextWatcher implements TextWatcher {

    private boolean mEditing;
    private long mPre = 0L;
    private String mPreStr;
    private boolean mIsForward = true;
    private OnTipListener fListener;

    private long mBaseAmount = 0L;

    private long maxValue = 999999999999L;

    /**
     * Instantiates a new Enter amount text watcher.
     */
    public EnterAmountTextWatcher() {
        mEditing = false;
    }

    /**
     * Instantiates a new Enter amount text watcher.
     *
     * @param baseAmount    the base amount
     * @param initTipAmount the init tip amount
     */
    public EnterAmountTextWatcher(long baseAmount, long initTipAmount) {
        mEditing = false;

        setAmount(baseAmount, initTipAmount);
    }

    /**
     * Sets amount.
     *
     * @param baseAmount the base amount
     * @param tipAmount  the tip amount
     */
    public void setAmount(long baseAmount, long tipAmount) {
        mBaseAmount = baseAmount;
        mPre = tipAmount;
        if (fListener != null) {
            fListener.onUpdateTipListener(mBaseAmount, mPre);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!mEditing) {
            mIsForward = (after >= count);
            mPreStr = s.toString();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //do nothing
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mEditing) {
            mEditing = true;
            String edit = s.toString().trim();
            long curr = mIsForward ? doForward(edit, mBaseAmount) : doBackward(edit, mBaseAmount);

            String str = CurrencyConverter.convert(curr);
            mPre = curr - mBaseAmount;
            updateEditable(s, str);
            mEditing = false;
        }
    }

    private void updateEditable(Editable s, String str) {
        try {
            s.replace(0, s.length(), str);
            if (fListener != null) {
                fListener.onUpdateTipListener(mBaseAmount, mPre);
            }
        } catch (NumberFormatException nfe) {
            s.clear();
            mPre = 0L;
        }
    }

    private long doForward(String edit, long currAmount) {
        long curr = currAmount;
        long lastDigit = 0L;
        int time = 0;
        if (!edit.isEmpty() && !mPreStr.isEmpty()) {
            int start = edit.indexOf(mPreStr);
            if (start != -1) {
                start += mPreStr.length();
                time = edit.length() - start;
            } else {
                start = 0;
            }
            if (time > 0) {
                try {
                    lastDigit = Long.parseLong(edit.substring(start).replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    time = 0;
                }
            }
        }

        time = (int) Math.pow(10, time);

        if ((curr + mPre * time + lastDigit > maxValue) ||
                (fListener != null && currAmount >= 0 && !fListener.onVerifyTipListener(currAmount, mPre * time + lastDigit))) { //AET-21
            Device.beepErr();
            curr += mPre;
        } else {
            curr += mPre * time + lastDigit;
        }

        return curr;
    }

    private long doBackward(String edit, long currAmount) {
        long curr = currAmount;
        if (0 == edit.length()) {
            mPre = 0L;
        }
        curr += mPre / 10;
        return curr;
    }

    /**
     * Gets max value.
     *
     * @return the max value
     */
    public long getMaxValue() {
        return maxValue;
    }

    /**
     * Sets max value.
     *
     * @param maxValue the max value
     */
    public void setMaxValue(long maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * Sets on tip listener.
     *
     * @param listener the listener
     */
    public void setOnTipListener(OnTipListener listener) {
        this.fListener = listener;
    }

    /**
     * The interface On tip listener.
     */
    public interface OnTipListener {
        /**
         * On update tip listener.
         *
         * @param baseAmount the base amount
         * @param tipAmount  the tip amount
         */
        void onUpdateTipListener(long baseAmount, long tipAmount);

        /**
         * On verify tip listener boolean.
         *
         * @param baseAmount the base amount
         * @param tipAmount  the tip amount
         * @return the boolean
         */
        boolean onVerifyTipListener(long baseAmount, long tipAmount);
    }
}
