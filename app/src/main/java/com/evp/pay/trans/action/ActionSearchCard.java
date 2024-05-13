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
package com.evp.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.evp.abl.core.AAction;
import com.evp.bizlib.data.entity.Issuer;
import com.evp.bizlib.params.ParamHelper;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.SearchCardActivity;
import com.pax.dal.entity.EReaderType;

/**
 * The type Action search card.
 */
public class ActionSearchCard extends AAction {
    private Context context;
    private byte mode;
    private String title;
    private String amount;
    private String date;
    private String searchCardPrompt;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionSearchCard(ActionStartListener listener) {
        super(listener);
    }


    /**
     * The type Card information.
     */
    public static class CardInformation {
        private byte searchMode;
        private String track1;
        private String track2;
        private String track3;
        private String pan;
        private String expDate;
        private Issuer issuer;
        private boolean thisIsFallback;

        /**
         * Instantiates a new Card information.
         *
         * @param mode   the mode
         * @param track1 the track 1
         * @param track2 the track 2
         * @param track3 the track 3
         * @param pan    the pan
         * @param issuer the issuer
         */
        public CardInformation(byte mode, String track1, String track2, String track3, String pan, Issuer issuer, boolean thisIsFallback) {
            this.searchMode = mode;
            this.track1 = track1;
            this.track2 = track2;
            this.track3 = track3;
            this.pan = pan;
            this.issuer = issuer;
            this.thisIsFallback = thisIsFallback;
        }

        /**
         * Instantiates a new Card information.
         *
         * @param mode the mode
         */
        public CardInformation(byte mode) {
            this.searchMode = mode;
        }

        /**
         * Instantiates a new Card information.
         *
         * @param mode    the mode
         * @param pan     the pan
         * @param expDate the exp date
         * @param issuer  the issuer
         */
        public CardInformation(byte mode, String pan, String expDate, Issuer issuer) {
            this.searchMode = mode;
            this.pan = pan;
            this.expDate = expDate;
            this.issuer = issuer;
        }

        /**
         * Gets search mode.
         *
         * @return the search mode
         */
        public byte getSearchMode() {
            return searchMode;
        }

        /**
         * Gets track 1.
         *
         * @return the track 1
         */
        public String getTrack1() {
            return track1;
        }

        /**
         * Gets track 2.
         *
         * @return the track 2
         */
        public String getTrack2() {
            return track2;
        }

        /**
         * Gets track 3.
         *
         * @return the track 3
         */
        public String getTrack3() {
            return track3;
        }

        /**
         * Gets pan.
         *
         * @return the pan
         */
        public String getPan() {
            return pan;
        }

        /**
         * Gets exp date.
         *
         * @return the exp date
         */
        public String getExpDate() {
            return expDate;
        }

        /**
         * Gets issuer.
         *
         * @return the issuer
         */
        public Issuer getIssuer() {
            return issuer;
        }

        /**
         * Gets fallback status.
         *
         * @return the fallback indicator
         */
        public boolean getIsThisFallback() { return thisIsFallback; }
    }

    /**
     * 设置参数
     *
     * @param context          ：上下文
     * @param title            the title
     * @param mode             ：读卡模式
     * @param amount           ：交易模式
     * @param date             the date
     * @param searchCardPrompt the search card prompt
     */
    public void setParam(Context context, String title, byte mode, String amount, String date, String searchCardPrompt) {
        this.context = context;
        this.title = title;
        this.mode = mode;
        this.amount = amount;
        this.date = date;
        this.searchCardPrompt = searchCardPrompt;
    }

    /**
     * action process
     */
    @Override
    protected void process() {
        Intent intent = new Intent(context, SearchCardActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.NAV_BACK.toString(), true);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        if (ParamHelper.isClssInternal()){
            mode = (byte) (mode & (~EReaderType.PICCEXTERNAL.getEReaderType()));
        }else if (ParamHelper.isClssExternal()){
            mode = (byte) (mode & (~EReaderType.PICC.getEReaderType()));
        }
        intent.putExtra(EUIParamKeys.CARD_SEARCH_MODE.toString(), mode);
        intent.putExtra(EUIParamKeys.TRANS_DATE.toString(), date);
        intent.putExtra(EUIParamKeys.SEARCH_CARD_PROMPT.toString(), searchCardPrompt);
        context.startActivity(intent);
    }

}
