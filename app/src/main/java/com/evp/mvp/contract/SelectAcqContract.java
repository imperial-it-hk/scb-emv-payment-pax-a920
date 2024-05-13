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
 * 20190108  	         xieYb                   Create
 * ===========================================================================================
 */
package com.evp.mvp.contract;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.base.presenter.impl.BasePresenter;
import com.evp.base.view.IView;

import java.util.ArrayList;
import java.util.Map;

/**
 * select acquirer contact
 */
public interface SelectAcqContract {
    /**
     * The interface View.
     */
    interface View extends IView {
        /**
         * Show adapter data.
         *
         * @param myListArray the my list array
         */
//show adapter data
        void showAdapterData(ArrayList<Map<String, String>> myListArray);

        /**
         * On select acq check.
         */
//on select acquirer
        void onSelectAcqCheck();

        /**
         * Finish.
         *
         * @param actionResult the action result
         */
//finish
        void finish(ActionResult actionResult);

        /**
         * Show toast.
         *
         * @param message the message
         */
//show toast
        void showToast(int message);

        /**
         * Confirm btn change.
         *
         * @param confirm the confirm
         */
//confirm btn change
        void confirmBtnChange(boolean confirm);

        /**
         * Sets checked.
         *
         * @param checked the checked
         */
//set checked
        void setChecked(boolean checked);
    }

    /**
     * The type Presenter.
     */
    abstract static class Presenter extends BasePresenter<View> {
        /**
         * Init param.
         *
         * @param checkedAcqs the checked acqs
         */
//init checked acquirer
        public abstract void initParam(ArrayList<String> checkedAcqs);

        /**
         * Init adapter data.
         */
//init adapter data
        public abstract void initAdapterData();

        /**
         * Select acq check.
         *
         * @param isChecked the is checked
         */
//select acquirer checked
        public abstract void selectAcqCheck(boolean isChecked);

        /**
         * Finish 2 settle acq.
         */
//finish settle acquirer
        public abstract void finish2SettleAcq();

        /**
         * Sets .
         *
         * @param acquirer the acquirer
         */
//settle
        public abstract void settle(String acquirer);

        /**
         * On checked change.
         *
         * @param acquireName the acquire name
         * @param checked     the checked
         */
//on check changed
        public abstract void onCheckedChange(String acquireName, boolean checked);

        /**
         * Instantiates a new Presenter.
         *
         * @param context the context
         */
        public Presenter(Context context) {
            super(context);
        }
    }
}
