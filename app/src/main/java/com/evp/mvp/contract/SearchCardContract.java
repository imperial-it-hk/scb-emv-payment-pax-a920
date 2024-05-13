/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen); CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen); CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C); 2019-? PAX Computer Technology(Shenzhen); CO., LTD All rights reserved.
 * Description: // Detail description about the voidction of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date                  Author	                 Action
 * 20190108  	         xieYb                   Modify
 * ===========================================================================================
 */
package com.evp.mvp.contract;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.base.presenter.impl.BasePresenter;
import com.evp.base.view.IView;
import com.evp.eventbus.CardDetectEvent;

/**
 * The interface Search card contract.
 */
public interface SearchCardContract {
    /**
     * The interface View.
     */
    interface View extends IView {
        /**
         * On edit card no error.
         */
        void onEditCardNoError();

        /**
         * On edit card no.
         */
        void onEditCardNo();

        /**
         * Show manual pwd err.
         */
        void showManualPwdErr();

        /**
         * Gets card no.
         *
         * @return the card no
         */
        String getCardNo();

        /**
         * Gets exp date.
         *
         * @return the exp date
         */
        String getExpDate();

        /**
         * finish with ActionResult.
         *
         * @param result the result
         */
        void finish(ActionResult result);

        /**
         * On edit date error.
         */
        void onEditDateError();

        /**
         * Icc card mag read ok.
         *
         * @param mode the mode
         */
        void iccCardMagReadOk(Byte mode);

        /**
         * Mag card mag exp date err.
         *
         * @param mode the mode
         */
        void magCardMagExpDateErr(Byte mode);

        /**
         * Mag card read ok.
         *
         * @param pan the pan
         */
        void magCardReadOk(String pan);

        /**
         * On icc detect ok.
         */
        void onIccDetectOk();

        /**
         * On picc detect ok.
         */
        void onPiccDetectOk();

        /**
         * On read card error.
         */
        void onReadCardError();

        /**
         * Go to adjust tip.
         */
        void goToAdjustTip();

        /**
         * Reset edit text.
         */
        void resetEditText();

        /**
         * On click back.
         */
        void onClickBack();


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
        public Presenter(Context context) {
            super(context);
        }

        /**
         * On timer finish.
         */
        public abstract void onTimerFinish();

        /**
         * Run input merchant pwd action.
         */
        public abstract void runInputMerchantPwdAction();

        /**
         * On verify manual pan.
         */
        public abstract void onVerifyManualPan();

        /**
         * Process manual exp date.
         */
        public abstract void processManualExpDate();

        /**
         * Init param.
         *
         * @param mode the mode
         */
        public abstract void initParam(Byte mode);

        /**
         * Run search card.
         */
        public abstract void runSearchCard();

        /**
         * Stop detect card.
         */
        public abstract void stopDetectCard();

        /**
         * On ok clicked.
         */
        public abstract void onOkClicked();

        /**
         * On header back clicked.
         */
        public abstract void onHeaderBackClicked();

        /**
         * On key back down.
         */
        public abstract void onKeyBackDown();

        public abstract void onReadCardOk(CardDetectEvent event);
    }
}
