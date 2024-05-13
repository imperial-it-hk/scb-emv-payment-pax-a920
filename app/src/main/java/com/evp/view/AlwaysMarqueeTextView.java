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
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;

/**
 * always marquee textview
 */
public class AlwaysMarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {
    /**
     * init
     *
     * @param context context
     */
    public AlwaysMarqueeTextView(Context context) {
        super(context);
        setAlwaysMarquee();
    }

    /**
     * init
     *
     * @param context context
     * @param attrs   attrs
     */
    public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAlwaysMarquee();
    }

    /**
     * init
     *
     * @param context  context
     * @param attrs    attrs
     * @param defStyle defStyle
     */
    public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAlwaysMarquee();
    }

    private void setAlwaysMarquee() {
        setEllipsize(TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setSingleLine();
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}

