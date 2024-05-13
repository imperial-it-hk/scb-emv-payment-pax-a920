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
package com.evp.view.dialog;

import android.content.Context;

import com.evp.payment.evpscb.R;

/**
 * The type Progress helper.
 */
public class ProgressHelper {
    private ProgressWheel mProgressWheel;
    private boolean mToSpin;
    private float mSpinSpeed;
    private int mBarWidth;
    private int mBarColor;
    private int mRimWidth;
    private int mRimColor;
    private boolean mIsInstantProgress;
    private float mProgressVal;
    private int mCircleRadius;

    /**
     * Instantiates a new Progress helper.
     *
     * @param ctx the ctx
     */
    public ProgressHelper(Context ctx) {
        mToSpin = true;
        mSpinSpeed = 0.75f;
        mBarWidth = ctx.getResources().getDimensionPixelSize(R.dimen.common_circle_width) + 1;
        mBarColor = ctx.getResources().getColor(R.color.primary);
        mRimWidth = 0;
        mRimColor = 0x00000000;
        mIsInstantProgress = false;
        mProgressVal = -1;
        mCircleRadius = ctx.getResources().getDimensionPixelOffset(R.dimen.progress_circle_radius);
    }

    /**
     * Gets progress wheel.
     *
     * @return the progress wheel
     */
    public ProgressWheel getProgressWheel() {
        return mProgressWheel;
    }

    /**
     * Sets progress wheel.
     *
     * @param progressWheel the progress wheel
     */
    public void setProgressWheel(ProgressWheel progressWheel) {
        mProgressWheel = progressWheel;
        updatePropsIfNeed();
    }

    private void updatePropsIfNeed() {
        if (mProgressWheel != null) {
            if (!mToSpin && mProgressWheel.isSpinning()) {
                mProgressWheel.stopSpinning();
            } else if (mToSpin && !mProgressWheel.isSpinning()) {
                mProgressWheel.spin();
            }
            if (Float.compare(mSpinSpeed, mProgressWheel.getSpinSpeed()) != 0) {
                mProgressWheel.setSpinSpeed(mSpinSpeed);
            }
            if (mBarWidth != mProgressWheel.getBarWidth()) {
                mProgressWheel.setBarWidth(mBarWidth);
            }
            if (mBarColor != mProgressWheel.getBarColor()) {
                mProgressWheel.setBarColor(mBarColor);
            }
            if (mRimWidth != mProgressWheel.getRimWidth()) {
                mProgressWheel.setRimWidth(mRimWidth);
            }
            if (mRimColor != mProgressWheel.getRimColor()) {
                mProgressWheel.setRimColor(mRimColor);
            }
            if (Float.compare(mProgressVal, mProgressWheel.getProgress()) != 0) {
                if (mIsInstantProgress) {
                    mProgressWheel.setInstantProgress(mProgressVal);
                } else {
                    mProgressWheel.setProgress(mProgressVal);
                }
            }
            if (mCircleRadius != mProgressWheel.getCircleRadius()) {
                mProgressWheel.setCircleRadius(mCircleRadius);
            }
        }
    }

    /**
     * Reset count.
     */
    public void resetCount() {
        if (mProgressWheel != null) {
            mProgressWheel.resetCount();
        }
    }

    /**
     * Is spinning boolean.
     *
     * @return the boolean
     */
    public boolean isSpinning() {
        return mToSpin;
    }

    /**
     * Spin.
     */
    public void spin() {
        mToSpin = true;
        updatePropsIfNeed();
    }

    /**
     * Stop spinning.
     */
    public void stopSpinning() {
        mToSpin = false;
        updatePropsIfNeed();
    }

    /**
     * Gets progress.
     *
     * @return the progress
     */
    public float getProgress() {
        return mProgressVal;
    }

    /**
     * Sets progress.
     *
     * @param progress the progress
     */
    public void setProgress(float progress) {
        mIsInstantProgress = false;
        mProgressVal = progress;
        updatePropsIfNeed();
    }

    /**
     * Sets instant progress.
     *
     * @param progress the progress
     */
    public void setInstantProgress(float progress) {
        mProgressVal = progress;
        mIsInstantProgress = true;
        updatePropsIfNeed();
    }

    /**
     * Gets circle radius.
     *
     * @return the circle radius
     */
    public int getCircleRadius() {
        return mCircleRadius;
    }

    /**
     * Sets circle radius.
     *
     * @param circleRadius units using pixel
     */
    public void setCircleRadius(int circleRadius) {
        mCircleRadius = circleRadius;
        updatePropsIfNeed();
    }

    /**
     * Gets bar width.
     *
     * @return the bar width
     */
    public int getBarWidth() {
        return mBarWidth;
    }

    /**
     * Sets bar width.
     *
     * @param barWidth the bar width
     */
    public void setBarWidth(int barWidth) {
        mBarWidth = barWidth;
        updatePropsIfNeed();
    }

    /**
     * Gets bar color.
     *
     * @return the bar color
     */
    public int getBarColor() {
        return mBarColor;
    }

    /**
     * Sets bar color.
     *
     * @param barColor the bar color
     */
    public void setBarColor(int barColor) {
        mBarColor = barColor;
        updatePropsIfNeed();
    }

    /**
     * Gets rim width.
     *
     * @return the rim width
     */
    public int getRimWidth() {
        return mRimWidth;
    }

    /**
     * Sets rim width.
     *
     * @param rimWidth the rim width
     */
    public void setRimWidth(int rimWidth) {
        mRimWidth = rimWidth;
        updatePropsIfNeed();
    }

    /**
     * Gets rim color.
     *
     * @return the rim color
     */
    public int getRimColor() {
        return mRimColor;
    }

    /**
     * Sets rim color.
     *
     * @param rimColor the rim color
     */
    public void setRimColor(int rimColor) {
        mRimColor = rimColor;
        updatePropsIfNeed();
    }

    /**
     * Gets spin speed.
     *
     * @return the spin speed
     */
    public float getSpinSpeed() {
        return mSpinSpeed;
    }

    /**
     * Sets spin speed.
     *
     * @param spinSpeed the spin speed
     */
    public void setSpinSpeed(float spinSpeed) {
        mSpinSpeed = spinSpeed;
        updatePropsIfNeed();
    }
}
