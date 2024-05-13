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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.animation.Animation;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.evp.payment.evpscb.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * emv contactless light view
 */
public class ClssLight extends AppCompatImageView {

    /**
     * The constant OFF.
     */
    public static final int OFF = 0;
    /**
     * The constant ON.
     */
    public static final int ON = 1;
    /**
     * The constant BLINK.
     */
    public static final int BLINK = 2;

    /**
     * The interface Status.
     */
    @IntDef({OFF, ON, BLINK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATUS {
    }

    @DrawableRes
    private int[] statusSrc = new int[]{-1, -1};

    /**
     * Instantiates a new Clss light.
     *
     * @param context the context
     */
    public ClssLight(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Clss light.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public ClssLight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new Clss light.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public ClssLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimation();
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ClssLight);
        statusSrc[OFF] = array.getResourceId(R.styleable.ClssLight_offSrc, -1);
        statusSrc[ON] = array.getResourceId(R.styleable.ClssLight_onSrc, -1);
        array.recycle();
    }

    /**
     * change emv contactless view status
     *
     * @param status   status
     * @param blinking blinking
     */
    public void setStatus(@STATUS int status, final Animation blinking){
        if (status == BLINK) {
            setImageResource(statusSrc[ON]);
            startAnimation(blinking);
        } else {
            clearAnimation();
            setImageResource(statusSrc[status]);
        }
    }
}
