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
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransTotal;

import java.util.ArrayList;

/**
 * settle contract
 */
public interface SettleContract {
    /**
     * The interface View.
     */
    interface View extends IView {
        /**
         * Sets curr acquirer content.
         *
         * @param acquirerName  the acquirer name
         * @param acquirer      the acquirer
         * @param saleAmt       the sale amt
         * @param refundAmt     the refund amt
         * @param voidSaleAmt   the void sale amt
         * @param voidRefundAmt the void refund amt
         * @param offlineAmt    the offline amt
         * @param total         the total
         */
//set acquirer content
        void setCurrAcquirerContent(String acquirerName,
                                    Acquirer acquirer,
                                    String saleAmt,
                                    String refundAmt,
                                    String voidSaleAmt,
                                    String voidRefundAmt,
                                    String offlineAmt,
                                    TransTotal total);

        /**
         * Finish.
         *
         * @param result the result
         */
//finish
        void finish(ActionResult result);

        /**
         * Print detail.
         *
         * @param isFailDetail the is fail detail
         */
//print detail
        void printDetail(Boolean isFailDetail);
    }

    /**
     * The type Presenter.
     */
    abstract class Presenter extends BasePresenter<View> {
        /**
         * Instantiates a new Presenter.
         *
         * @param context the context
         */
//init presenter
        public Presenter(Context context) {
            super(context);
        }

        /**
         * Init.
         *
         * @param selectAcqs the select acqs
         */
//init select acquirer
        public abstract void init(ArrayList<String> selectAcqs);

        /**
         * Do settlement.
         */
//do settle
        public abstract void doSettlement(boolean isThisAutoSettle);

        /**
         * Sets curr acquirer content.
         *
         * @param acquirerName the acquirer name
         */
//set current acquirer content
        public abstract void setCurrAcquirerContent(String acquirerName);

    }
}
