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
 * 20190108  	         XuShuang                Create
 * ===========================================================================================
 */

package com.evp.mvp.contract;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.base.presenter.impl.BasePresenter;
import com.evp.base.view.IView;
import com.evp.pay.trans.action.ActionEnterPin;
import com.pax.dal.exceptions.PedDevException;

import org.jetbrains.annotations.NotNull;

/**
 * enter pin contract
 */
public interface EnterPinContract {
    /**
     * The interface View.
     */
    interface View extends IView {
        /**
         * Action finish.
         *
         * @param result the result
         */
        void actionFinish(@NotNull ActionResult result);

        /**
         * Show fling notice.
         */
//show fling notice
        void showFlingNotice();

        /**
         * Show error dialog.
         *
         * @param e the e
         */
//show error dialog
        void showErrorDialog(@NotNull PedDevException e);

        /**
         * Gets text.
         *
         * @return the text
         */
//get text
        String getText();

        /**
         * Sets text.
         *
         * @param temp the temp
         */
//set text
        void setText(String temp);
    }

    /**
     * The type Presenter.
     */
    abstract static class Presenter extends BasePresenter<View> {
        /**
         * Instantiates a new Presenter.
         *
         * @param context the context
         */
        public Presenter(Context context) {
            super(context);
        }

        /**
         * Start detect finger r.
         *
         * @param panBlock      the pan block
         * @param supportBypass the support bypass
         * @param enterPinType  the enter pin type
         */
        public abstract void startDetectFingerR(String panBlock, boolean supportBypass, @NotNull ActionEnterPin.EEnterPinType enterPinType, int pinKeyIndex);
    }
}
