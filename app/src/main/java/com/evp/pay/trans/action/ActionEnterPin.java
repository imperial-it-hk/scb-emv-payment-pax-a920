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
import com.evp.bizlib.card.PanUtils;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.trans.action.activity.EnterPinActivity;

/**
 * The type Action enter pin.
 */
public class ActionEnterPin extends AAction {
    private Context context;
    private String title;
    private String pan;
    private String header;
    private String subHeader;
    private String totalAmount;
    private String tipAmount;
    private boolean isSupportBypass;
    private EEnterPinType enterPinType;
    private int pinKeyIndex;

    /**
     * derived classes must call super(listener) to set
     *
     * @param listener {@link ActionStartListener}
     */
    public ActionEnterPin(ActionStartListener listener) {
        super(listener);
    }

    /**
     * 脱机pin时返回的结果
     *
     * @author Steven.W
     */
    public static class OfflinePinResult {
        /**
         * The Resp out.
         */
// SW1 SW2
        byte[] respOut;
        /**
         * The Ret.
         */
        int ret;

        /**
         * Get resp out byte [ ].
         *
         * @return the byte [ ]
         */
        public byte[] getRespOut() {
            return respOut;
        }

        /**
         * Sets resp out.
         *
         * @param respOut the resp out
         */
        public void setRespOut(byte[] respOut) {
            this.respOut = respOut;
        }

        /**
         * Gets ret.
         *
         * @return the ret
         */
        public int getRet() {
            return ret;
        }

        /**
         * Sets ret.
         *
         * @param ret the ret
         */
        public void setRet(int ret) {
            this.ret = ret;
        }
    }

    /**
     * Sets param.
     *
     * @param context       the context
     * @param title         the title
     * @param pan           the pan
     * @param supportBypass the support bypass
     * @param header        the header
     * @param subHeader     the sub header
     * @param totalAmount   the total amount
     * @param tipAmount     the tip amount
     * @param enterPinType  the enter pin type
     */
    public void setParam(Context context, String title, String pan, boolean supportBypass, String header,
                         String subHeader, String totalAmount, String tipAmount, EEnterPinType enterPinType,
                         int pinKeyIndex) {
        this.context = context;
        this.title = title;
        this.pan = pan;
        this.isSupportBypass = supportBypass;
        this.header = header;
        this.subHeader = subHeader;
        this.totalAmount = totalAmount;
        this.tipAmount = tipAmount; //AET-81
        this.enterPinType = enterPinType;
        this.pinKeyIndex = pinKeyIndex;
    }

    /**
     * The enum E enter pin type.
     */
    public enum EEnterPinType {
        /**
         * Online pin e enter pin type.
         */
        ONLINE_PIN, // 联机pin
        /**
         * Offline plain pin e enter pin type.
         */
        OFFLINE_PLAIN_PIN, // 脱机明文pin
        /**
         * Offline cipher pin e enter pin type.
         */
        OFFLINE_CIPHER_PIN, // 脱机密文pin
        /**
         * Offline pci mode e enter pin type.
         */
        OFFLINE_PCI_MODE, //JEMV PCI MODE, no callback for offline pin
    }
    /**
     * action process
     */
    @Override
    protected void process() {
        Intent intent = new Intent(context, EnterPinActivity.class);
        intent.putExtra(EUIParamKeys.NAV_TITLE.toString(), title);
        intent.putExtra(EUIParamKeys.PROMPT_1.toString(), header);
        intent.putExtra(EUIParamKeys.PROMPT_2.toString(), subHeader);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), totalAmount);
        intent.putExtra(EUIParamKeys.TIP_AMOUNT.toString(), tipAmount); //AET-81
        intent.putExtra(EUIParamKeys.ENTERPINTYPE.toString(), enterPinType);
        intent.putExtra(EUIParamKeys.PANBLOCK.toString(), PanUtils.getPanBlock(pan, PanUtils.X9_8_WITH_PAN));
        intent.putExtra(EUIParamKeys.SUPPORTBYPASS.toString(), isSupportBypass);
        intent.putExtra(EUIParamKeys.PIN_KEY_INDEX.toString(), pinKeyIndex);
        context.startActivity(intent);
    }

}
