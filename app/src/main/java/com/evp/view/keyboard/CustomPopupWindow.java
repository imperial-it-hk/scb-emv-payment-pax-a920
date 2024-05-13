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
package com.evp.view.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.PopupWindow;

/**
 * The type Custom popup window.
 */
public class CustomPopupWindow extends PopupWindow {

    private OnEnableDismissListener onEnableDismissListener;

    /**
     * Instantiates a new Custom popup window.
     *
     * @param context the context
     */
    public CustomPopupWindow(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Custom popup window.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CustomPopupWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Instantiates a new Custom popup window.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public CustomPopupWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * Instantiates a new Custom popup window.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     * @param defStyleRes  the def style res
     */
    public CustomPopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Instantiates a new Custom popup window.
     */
    public CustomPopupWindow() {
        this(null, 0, 0);
    }

    /**
     * Instantiates a new Custom popup window.
     *
     * @param contentView the content view
     */
    public CustomPopupWindow(View contentView) {
        this(contentView, 0, 0);
    }

    /**
     * Instantiates a new Custom popup window.
     *
     * @param width  the width
     * @param height the height
     */
    public CustomPopupWindow(int width, int height) {
        this(null, width, height);
    }

    /**
     * Instantiates a new Custom popup window.
     *
     * @param contentView the content view
     * @param width       the width
     * @param height      the height
     */
    public CustomPopupWindow(View contentView, int width, int height) {
        this(contentView, width, height, false);
    }

    /**
     * Instantiates a new Custom popup window.
     *
     * @param contentView the content view
     * @param width       the width
     * @param height      the height
     * @param focusable   the focusable
     */
    public CustomPopupWindow(View contentView, int width, int height, boolean focusable) {
        super(contentView, width, height, focusable);
    }

    @Override
    public void dismiss() {
        if (onEnableDismissListener != null && onEnableDismissListener.onEnableDismiss()) {
            super.dismiss();
        }
    }

    /**
     * Force dismiss.
     */
    public void forceDismiss() {
        super.dismiss();
    }

    /**
     * Listener that is called when this popup window is dismissed.
     */
    public interface OnEnableDismissListener {
        /**
         * Called when this popup window is dismissed.
         *
         * @return the boolean
         */
        boolean onEnableDismiss();
    }

    /**
     * Sets on enable dismiss listener.
     *
     * @param onEnableDismissListener the on enable dismiss listener
     */
    public void setOnEnableDismissListener(OnEnableDismissListener onEnableDismissListener) {
        this.onEnableDismissListener = onEnableDismissListener;
    }
}
