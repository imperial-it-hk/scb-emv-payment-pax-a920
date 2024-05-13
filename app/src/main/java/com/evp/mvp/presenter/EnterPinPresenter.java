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
package com.evp.mvp.presenter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.SystemClock;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.bizlib.params.ParamHelper;
import com.evp.bizlib.ped.PedHelper;
import com.evp.device.Device;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eventbus.EmvCallbackEvent;
import com.evp.mvp.contract.EnterPinContract;
import com.evp.pay.app.ActivityStack;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.action.ActionEnterPin;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.neptune.Sdk;
import com.evp.view.dialog.DialogUtils;
import com.pax.dal.IPed;
import com.pax.dal.entity.EKeyCode;
import com.pax.dal.entity.RSAPinKey;
import com.pax.dal.exceptions.EPedDevException;
import com.pax.dal.exceptions.PedDevException;

import org.jetbrains.annotations.NotNull;

/**
 * The type Enter pin presenter.
 */
public class EnterPinPresenter extends EnterPinContract.Presenter {
    private boolean landscape;
    private RSAPinKey rsaPinKey;
    private String header;
    private String subHeader;
    private static final byte ICC_SLOT = 0x00;
    private static final String OFFLINE_EXP_PIN_LEN = "0,4,5,6,7,8,9,10,11,12";
    private IPed ped;

    /**
     * Instantiates a new Enter pin presenter.
     *
     * @param context the context
     */
    public EnterPinPresenter(Context context) {
        super(context);
    }

    @Override
    public void startDetectFingerR(String panBlock, boolean supportBypass, @NotNull ActionEnterPin.EEnterPinType enterPinType, int pinKeyIndex) {
        FinancialApplication.getApp().runInBackground(new DetectFingerRunnable(panBlock, supportBypass, enterPinType, pinKeyIndex));
    }

    /**
     * Init params.
     *
     * @param landscape the landscape
     * @param rsaPinKey the rsa pin key
     * @param prompt1   the prompt 1
     * @param prompt2   the prompt 2
     */
    public final void initParams(boolean landscape, RSAPinKey rsaPinKey, String prompt1, String prompt2) {
        this.landscape = landscape;
        this.rsaPinKey = rsaPinKey;
        this.header = prompt1;
        this.subHeader = prompt2;
    }

    private final class DetectFingerRunnable implements Runnable {
        private final String panBlock;
        private final boolean supportBypass;
        private final ActionEnterPin.EEnterPinType enterPinType;
        private final int pinKeyIndex;

        /**
         * Instantiates a new Detect finger runnable.
         *
         * @param panBlock      the pan block
         * @param supportBypass the support bypass
         * @param enterPinType  the enter pin type
         */
        public DetectFingerRunnable(String panBlock, boolean supportBypass, ActionEnterPin.EEnterPinType enterPinType, int pinKeyIndex) {
            this.panBlock = panBlock;
            this.supportBypass = supportBypass;
            this.enterPinType = enterPinType;
            this.pinKeyIndex = pinKeyIndex;
        }

        @Override
        public void run() {
            ped = PedHelper.getPed();
            if (ped == null) {
                DialogUtils.showErrMessage(ActivityStack.getInstance().top(), null, "Can't Get PED Module", new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        proxyView.actionFinish(new ActionResult(TransResult.ERR_ABORTED, null));
                    }
                }, Constants.FAILED_DIALOG_SHOW_TIME);
                return;
            }
            //AET-226
            //workaround:get the touch event from by native code which may
            Sdk.TouchEvent nte = Sdk.getInstance().getTouchEvent();
            while (nte.detect(200)) {
                proxyView.showFlingNotice();
                SystemClock.sleep(500);
            }
            if (enterPinType == ActionEnterPin.EEnterPinType.ONLINE_PIN) {
                enterOnlinePin(panBlock, supportBypass, pinKeyIndex);
            } else if (enterPinType == ActionEnterPin.EEnterPinType.OFFLINE_CIPHER_PIN) {
                enterOfflineCipherPin();
            } else if (enterPinType == ActionEnterPin.EEnterPinType.OFFLINE_PLAIN_PIN) {
                enterOfflinePlainPin();
            } else if (enterPinType == ActionEnterPin.EEnterPinType.OFFLINE_PCI_MODE) {
                enterOfflinePCIMode();
            }
        }

        private void enterOnlinePin(final String panBlock, final boolean supportBypass, final int pinKeyIndex) {
            FinancialApplication.getApp().runInBackground(new OnlinePinRunnable(panBlock, supportBypass, pinKeyIndex));
        }
    }

    private IPed.IPedInputPinListener pedInputPinListener = new IPed.IPedInputPinListener() {

        @Override
        public void onKeyEvent(final EKeyCode key) {
            String temp;
            if (key == EKeyCode.KEY_CLEAR) {
                temp = "";
            } else if (key == EKeyCode
                    .KEY_ENTER || key == EKeyCode.KEY_CANCEL) {
                // do nothing
                return;
            } else {
                temp = proxyView.getText();
                temp += "*";
            }
            proxyView.setText(temp);
        }
    };

    private IPed.IPedInputPinListener inputPCIPinListener = new IPed.IPedInputPinListener() {

        @Override
        public void onKeyEvent(final EKeyCode key) {
            //AET-148
            String temp = proxyView.getText();
            if (key == EKeyCode.KEY_CLEAR) {
                temp = "";
            } else if (key == EKeyCode.KEY_CANCEL) {
                ped.setInputPinListener(null);
                proxyView.actionFinish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                return;
            } else if (key == EKeyCode.KEY_ENTER) {
                if (temp.length() > 3 || temp.length() == 0) {
                    ped.setInputPinListener(null);
                    proxyView.actionFinish(new ActionResult(TransResult.SUCC, null));
                    return;
                }
            } else {
                temp += "*";
            }
            proxyView.setText(temp);
        }
    };

    /**
     * Enter offline cipher pin.
     */
    public void enterOfflineCipherPin() {
        FinancialApplication.getApp().runInBackground(new OfflineCipherPinRunnable());
    }

    /**
     * Enter offline plain pin.
     */
    public void enterOfflinePlainPin() {
        FinancialApplication.getApp().runInBackground(new OfflinePlainPinRunnable());
    }

    /**
     * Enter offline pci mode.
     */
    public void enterOfflinePCIMode() {
        try {
            ped.setIntervalTime(1, 1);
            ped.setInputPinListener(inputPCIPinListener);
            FinancialApplication.getApp().doEvent(new EmvCallbackEvent(EmvCallbackEvent.Status.OFFLINE_PIN_ENTER_READY));
        } catch (PedDevException e) {
            ActionEnterPin.OfflinePinResult offlinePinResult = new ActionEnterPin.OfflinePinResult();
            offlinePinResult.setRet(e.getErrCode());
            proxyView.actionFinish(new ActionResult(TransResult.ERR_ABORTED, offlinePinResult));
        }
    }

    private class OnlinePinRunnable implements Runnable {
        private final String onlinePanBlock;
        private final boolean isSupportBypass;
        private final int pinKeyIndex;

        /**
         * Instantiates a new Online pin runnable.
         *
         * @param panBlock      the pan block
         * @param supportBypass the support bypass
         */
        OnlinePinRunnable(final String panBlock, final boolean supportBypass, final int pinKeyIndex) {
            this.onlinePanBlock = panBlock;
            this.isSupportBypass = supportBypass;
            this.pinKeyIndex = pinKeyIndex;
            ped.setInputPinListener(pedInputPinListener);
        }

        @Override
        public void run() {
            try {
                if (ParamHelper.isInternalPed()){
                    ped.setIntervalTime(1, 1);//仅支持内置类型
                }else if (ParamHelper.isExternalTypeAPed()){
                    ped.clearScreen();
                    ped.showStr((byte) 0x00, (byte) 0x00,header);
                    int length = subHeader.length();
                    if (length > 18){
                        ped.showStr((byte) 0x00, (byte) 0x04, subHeader.substring(0,18));
                        ped.showStr((byte) 0x00, (byte) 0x05, subHeader.substring(18));
                    }else {
                        ped.showStr((byte) 0x00, (byte) 0x04, subHeader);
                    }
                }
                byte[] pinData = PedHelper.getPinBlock(pinKeyIndex, onlinePanBlock, isSupportBypass, landscape);
                if (pinData == null || pinData.length == 0)
                    proxyView.actionFinish(new ActionResult(TransResult.SUCC, null));
                else {
                    proxyView.actionFinish(new ActionResult(TransResult.SUCC, ConvertHelper.getConvert().bcdToStr(pinData)));
                }
            } catch (final PedDevException e) {
                handleException(e);
            } finally {
                //no memory leak
                ped.setInputPinListener(null);
            }
        }

        private void handleException(final PedDevException e) {
            if (e.getErrCode() == EPedDevException.PED_ERR_INPUT_CANCEL.getErrCodeFromBasement()) {
                proxyView.actionFinish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
            } else {

                FinancialApplication.getApp().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Device.beepErr();
                        proxyView.showErrorDialog(e);
                    }
                });
            }
        }
    }

    private abstract class OfflinePinRunnable implements Runnable {
        /**
         * Instantiates a new Offline pin runnable.
         */
        OfflinePinRunnable() {
            ped.setInputPinListener(pedInputPinListener);
        }

        @Override
        public void run() {
            try {
                ped.setIntervalTime(1, 1);
                ped.setKeyboardLayoutLandscape(landscape);
                byte[] resp = callPed();
                ActionEnterPin.OfflinePinResult offlinePinResult = new ActionEnterPin.OfflinePinResult();
                offlinePinResult.setRet(EEmvExceptions.EMV_OK.getErrCodeFromBasement());
                offlinePinResult.setRespOut(resp);
                proxyView.actionFinish(new ActionResult(TransResult.SUCC, offlinePinResult));
            } catch (PedDevException e) {
                handleException(e);
            } finally {
                //no memory leak
                ped.setInputPinListener(null);
            }
        }

        private void handleException(PedDevException e) {
            if (e.getErrCode() == EPedDevException.PED_ERR_INPUT_CANCEL.getErrCodeFromBasement()) {
                proxyView.actionFinish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
            } else {
                ActionEnterPin.OfflinePinResult offlinePinResult = new ActionEnterPin.OfflinePinResult();
                offlinePinResult.setRet(e.getErrCode());
                proxyView.actionFinish(new ActionResult(TransResult.ERR_ABORTED, offlinePinResult));
            }
        }

        /**
         * Call ped byte [ ].
         *
         * @return the byte [ ]
         * @throws PedDevException the ped dev exception
         */
        abstract byte[] callPed() throws PedDevException;
    }

    private class OfflineCipherPinRunnable extends OfflinePinRunnable {
        @Override
        byte[] callPed() throws PedDevException {
            return ped.verifyCipherPin(ICC_SLOT, OFFLINE_EXP_PIN_LEN, rsaPinKey, (byte) 0x00, 60 * 1000);
        }
    }

    private class OfflinePlainPinRunnable extends OfflinePinRunnable {
        @Override
        byte[] callPed() throws PedDevException {
            return ped.verifyPlainPin(ICC_SLOT, OFFLINE_EXP_PIN_LEN, (byte) 0x00, 60 * 1000);
        }
    }

}
