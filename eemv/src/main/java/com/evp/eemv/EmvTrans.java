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
 * 20190108  	         Linhb                   Create
 * ===========================================================================================
 */

package com.evp.eemv;

import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.entity.Amounts;
import com.evp.eemv.entity.CandList;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.pax.dal.exceptions.PedDevException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class EmvTrans {
    private IEmvListener listener;

    EmvTrans() {
        //do nothing
    }

    void setEmvListener(IEmvListener listener) {
        this.listener = listener;
    }

    public Amount getAmount() {
        Amount amt = new Amount();
        if (listener != null) {
            Amounts emvAmounts = listener.onGetAmounts();
            try {
                amt.setAmount(emvAmounts.getTransAmount());
                amt.setCashBackAmt(emvAmounts.getCashBackAmount());
            } catch (EmvException e) {
                LogUtils.w("EmvTrans", "", e);
            }
        }
        return amt;
    }

    int waitAppSelect(int tryCnt, CandList[] appList) throws EmvException {
        if (listener != null) {
            List<CandList> candLists = new ArrayList<>(Arrays.asList(appList));
            return listener.onWaitAppSelect(tryCnt < 1, candLists);
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    int confirmCardNo(String pan) throws EmvException {
        if (listener != null) {
            return listener.onConfirmCardNo(pan);
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    int cardHolderPwd(boolean bOnlinePin, int leftTimes, byte[] pinData) throws EmvException {
        if (listener != null) {
            return listener.onCardHolderPwd(bOnlinePin, leftTimes, pinData);
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    EOnlineResult onlineProc() throws EmvException {
        if (listener != null) {
            try {
                return listener.onOnlineProc();
            } catch (PedDevException e) {
                throw new EmvException(e.getErrModule(), e.getErrCode(), e.getErrMsg());
            }
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    public boolean chkExceptionFile() {
        return listener != null && listener.onChkExceptionFile();
    }

    public void setCvmResult(byte[] cvmResult) {
        if (listener != null) {
            listener.setCvmResult(cvmResult);
        }
    }

    int dccProcess() throws EmvException {
        if (listener != null) {
            try {
                return listener.onDcc();
            } catch (PedDevException e) {
                throw new EmvException(e.getErrModule(), e.getErrCode(), e.getErrMsg());
            }
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    int additionalProcess() throws EmvException {
        if (listener != null) {
            return listener.onAdditionalProcess();
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    boolean isPinAllowedForTransaction() throws EmvException {
        if (listener != null) {
            return listener.isPinAllowedForTransaction();
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.EmvTrans
 * JD-Core Version:    0.6.0
 */