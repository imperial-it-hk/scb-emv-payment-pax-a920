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
package com.evp.pay.trans.transmit;

import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.trans.component.Component;
import com.evp.pay.utils.ResponseCode;
import com.evp.pay.utils.TransResultUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.pax.dal.exceptions.PedDevException;

import java.util.Objects;

/**
 * The type Transmit.
 */
public class Transmit {
    private static final String TAG = Transmit.class.getSimpleName();

    private Online online = new Online();

    /**
     * Transmit int.
     *
     * @param transData the trans data
     * @param listener  the listener
     * @return the int
     * @throws PedDevException the ped dev exception
     */
    public int transmit(TransData transData, TransProcessListener listener) throws PedDevException {
        int ret = 0;
        int i = 0;
        ETransType transType = ConvertUtils.enumValue(ETransType.class,transData.getTransType());
        // 处理冲正
        if (Objects.requireNonNull(transType).isDupSendAllowed()) {
            ret = sendReversal(transData.getAcquirer(), listener);
            if (ret != 0) {
                i = 3;
            }
        }

        if (listener != null) {
            listener.onUpdateProgressTitle(transType.getTransName());
        }


        // 只有平台返回密码错时， 才会下次循环
        for (int j = i; j < 3; j++) {
            if (j != 0) {
                // 输入密码
                if (listener != null) {
                    ret = listener.onInputOnlinePin(transData);
                    if (ret != 0) {
                        return TransResult.ERR_ABORTED;
                    }
                } else {
                    return TransResult.ERR_HOST_REJECT;
                }

                //In case online transmit failed then STAN can be different
                transData.setStanNo(Component.getStanNo());
            }
            if (listener != null) {
                listener.onUpdateProgressTitle(transType.getTransName());
            }

            ret = online.online(transData, listener);
            if (ret == TransResult.SUCC) {
                ResponseCode responseCode = FinancialApplication.getRspCode().parse(transData.getResponseCode());
                String retCode = responseCode.getCode();

                if ("00".equals(retCode)) {
                    // write transaction record
                    transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
                    transData.setDupReason("");
                    //Void will be updated in SaleVoidTrans.java
                    if(transType != ETransType.VOID) {
                        GreendaoHelper.getTransDataHelper().update(transData);
                    }
                    return TransResult.SUCC;
                } else {
                    GreendaoHelper.getTransDataHelper().deleteDupRecord(transData.getAcquirer());
                    if ("55".equals(retCode)) {
                        if (listener != null) {
                            listener.onShowErrMessage(
                                    Utils.getString(R.string.err_password_reenter),
                                    Constants.FAILED_DIALOG_SHOW_TIME, false);
                        }
                        continue;
                    }
                    if (listener != null) {
                        listener.onShowErrMessage(
                                Utils.getString(R.string.prompt_err_code) + retCode
                                        + "\n"
                                        + responseCode.getMessage(), Constants.FAILED_DIALOG_SHOW_TIME, false);
                    }
                    return TransResult.ERR_HOST_REJECT;
                }
            }

            break;
        }
        if (i == 3) {
            return TransResult.ERR_ABORTED;
        }

        return ret;
    }

    /**
     * 冲正处理
     *
     * @param listener the listener
     * @return int
     * @throws PedDevException the ped dev exception
     */
    public int sendReversal(Acquirer acquirer, TransProcessListener listener) throws PedDevException {
        TransData dupTransData = GreendaoHelper.getTransDataHelper().findFirstDupRecord(acquirer);
        if (dupTransData == null) {
            return TransResult.SUCC;
        }
        int ret = 0;

        ETransType transType = ConvertUtils.enumValue(ETransType.class,dupTransData.getTransType());
        if (transType == ETransType.VOID) {
            dupTransData.setOrigAuthCode(dupTransData.getOrigAuthCode());
        } else {
            dupTransData.setOrigAuthCode(dupTransData.getAuthCode());
        }

        dupTransData.setAcquirer(acquirer);
        dupTransData.setBatchNo(acquirer.getCurrBatchNo());
        dupTransData.setNii(acquirer.getNii());
        dupTransData.setHeader("");
        dupTransData.setTransState(TransData.ETransStatus.NORMAL);

        int retry = SysParam.getInstance().getInt(R.string.EDC_REVERSAL_RETRY);
        if (listener != null) {
            listener.onUpdateProgressTitle(Utils.getString(R.string.prompt_reverse));
        }

        for (int i = 0; i < retry; i++) {
            //AET-126
            dupTransData.setReversalStatus(TransData.ReversalStatus.REVERSAL);
            ret = online.online(dupTransData, listener);
            if (ret == TransResult.SUCC) {
                String retCode = dupTransData.getResponseCode();
                // 冲正收到响应码12或者25的响应码，应默认为冲正成功
                if ("00".equals(retCode) || "12".equals(retCode) || "25".equals(retCode)) {
                    GreendaoHelper.getTransDataHelper().deleteDupRecord(acquirer);
                    return TransResult.SUCC;
                }
                dupTransData.setReversalStatus(TransData.ReversalStatus.PENDING);
                dupTransData.setDupReason(TransData.DUP_REASON_OTHERS);
                GreendaoHelper.getTransDataHelper().update(dupTransData);
                continue;
            }
            if (ret == TransResult.ERR_CONNECT || ret == TransResult.ERR_PACK || ret == TransResult.ERR_SEND) {
                if (listener != null) {
                    listener.onShowErrMessage(
                            TransResultUtils.getMessage(ret),
                            Constants.FAILED_DIALOG_SHOW_TIME, false);
                }

                return TransResult.ERR_ABORTED;
            }
            if (ret == TransResult.ERR_RECV) {
                dupTransData.setReversalStatus(TransData.ReversalStatus.PENDING);
                dupTransData.setDupReason(TransData.DUP_REASON_NO_RECV);
                GreendaoHelper.getTransDataHelper().update(dupTransData);
                break;
            }
        }
        if (listener != null) {
            listener.onShowErrMessage(Utils.getString(R.string.err_reverse),
                    Constants.FAILED_DIALOG_SHOW_TIME, false);
        }
        if (ret != TransResult.ERR_RECV) //no clear reversal if no response from host
            GreendaoHelper.getTransDataHelper().deleteDupRecord(acquirer);
        return ret;
    }

    /**
     * 脱机交易上送
     *
     * @param listener     the listener
     * @return int
     * @throws PedDevException the ped dev exception
     */
    public int sendOfflineTrans(TransProcessListener listener) throws PedDevException {
        int ret = new TransOnline().offlineTransSend(listener);
        if (ret != TransResult.SUCC && ret != TransResult.ERR_ABORTED && listener != null) {
            listener.onShowErrMessage(
                    TransResultUtils.getMessage(ret),
                    Constants.FAILED_DIALOG_SHOW_TIME, false);
        }
        return ret;
    }

    public EOnlineResult dccOnlineProc(TransData transData, TransProcessListener listener) throws PedDevException {
        final ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());

        if (listener != null) {
            listener.onUpdateProgressTitle(transType.getTransName());
        }

        int ret = new Online().online(transData, listener);
        final String respCode = transData.getResponseCode();
        LogUtils.i(TAG, "DCC Online  ret = " + ret + ", response code: " + respCode);
        if (ret == TransResult.SUCC && "00".equals(respCode)) {
            return EOnlineResult.APPROVE;
        }

        if (listener != null) {
            listener.onHideProgress();
        }

        return EOnlineResult.ABORT;
    }
}
