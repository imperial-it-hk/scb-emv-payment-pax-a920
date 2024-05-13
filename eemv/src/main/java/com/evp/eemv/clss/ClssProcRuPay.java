/*
 * ============================================================================
 * = COPYRIGHT
 *               PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with PAX  Technology, Inc. and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2018-? PAX Technology, Inc. All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date	                 Author	                Action
 * 2018/11/28  	         xionggd           	Create/Add/Modify/Delete
 * ============================================================================
 */
package com.evp.eemv.clss;


import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.EmvImpl;
import com.evp.eemv.IClssListener;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Tools;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.EMV_REVOCLIST;
import com.pax.jemv.clcommon.OnlineResult;
import com.pax.jemv.clcommon.OutcomeParam;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.entrypoint.api.ClssEntryApi;
import com.pax.jemv.rupay.api.ClssRuPayApi;

import java.util.Arrays;

class ClssProcRuPay extends ClssProc {
    /**
     * from ISO8583 definition
     */
    private final static String BAD_TEMINAL_ID = "89";
    private Clss_PreProcInfo clss_preProcInfo;

    static {
        System.loadLibrary("F_RUPAY_LIB_PayDroid");
        System.loadLibrary("JNI_RUPAY_v100");
    }

    ClssProcRuPay(IClssListener listener) {
        super(listener);
    }

    /**
     * RuPay's core init and some parameters' setting.
     *
     * @return 0-success othes-fail
     */
    private int init() {
        int ret = ClssRuPayApi.Clss_CoreInit_RuPay();
        LogUtils.i(TAG, "Clss_CoreInit_RuPay = " + ret);

        return ret;
    }

    /**
     * set basic parameters of RuPay by Barret 20181128
     */
    private void clssBaseParameterSet() {
        //set terminal parameters
        setTlv(TagsTable.APP_VER, aid.getVersion());
        setTlv(TagsTable.TERM_DEFAULT, aid.getTacDefault());
        setTlv(TagsTable.TERM_DENIAL, aid.getTacDenial());
        setTlv(TagsTable.TERM_ONLINE, aid.getTacOnline());

        for (Clss_PreProcInfo info : arrayPreProcInfo) {
            if (info != null && Arrays.equals(aid.getAid(), info.aucAID)) {
                clss_preProcInfo = info;
            }
        }
        if (clss_preProcInfo != null) {
            setTlv(TagsTable.RUPAY_FLOOR_LIMIT, Tools.str2Bcd(Tools.getPaddedNumber(clss_preProcInfo.ulRdClssFLmt, 8)));
            setTlv(TagsTable.RUPAY_TRANS_LIMIT, Tools.str2Bcd(Tools.getPaddedNumber(clss_preProcInfo.ulRdClssTxnLmt, 12)));
            setTlv(TagsTable.RUPAY_CVM_LIMIT, Tools.str2Bcd(Tools.getPaddedNumber(clss_preProcInfo.ulRdCVMLmt, 12)));

        }

        //additional capability
        setTlv(TagsTable.ADDITIONAL_CAPABILITY, new byte[]{(byte) 0xF0, (byte) 0x00, (byte) 0xF0, (byte) 0xA0, (byte) 0x01});
        setTlv(TagsTable.TERMINAL_CAPABILITY, new byte[]{(byte) 0xE0, (byte) 0xE1, (byte) 0xC8});
        setTlv(TagsTable.ACQUIRER_ID, new byte[]{0x00, 0x00, 0x00, 0x12, 0x34, 0x56});
        setTlv(TagsTable.MERCHANT_CATEGORY_CODE, Tools.str2Bcd("0001"));
        setTlv(TagsTable.MERCHANT_NAME_LOCATION, new byte[]{0x00});
        //country code = 0356, india
        setTlv(TagsTable.COUNTRY_CODE, new byte[]{0x03, 0x56});
        setTlv(TagsTable.TERMINAL_TYPE, new byte[]{0x22});
        //currency code = 0356, india
        setTlv(TagsTable.CURRENCY_CODE, new byte[]{0x03, 0x56});
        setTlv(TagsTable.TRANS_CURRRENCY_EXPONENT, new byte[]{0x02});

        //set transaction parameters
        byte[] tmp = Tools.str2Bcd(String.valueOf(transParam.ulAmntAuth));
        byte[] amount = new byte[6];
        System.arraycopy(tmp, 0, amount, 6 - tmp.length, tmp.length);
        setTlv(TagsTable.AMOUNT, amount);

        if (transParam.ulAmntOther > 0) {
            tmp = Tools.str2Bcd(Long.toString(transParam.ulAmntOther));
            amount = new byte[6];
            System.arraycopy(tmp, 0, amount, 6 - tmp.length, tmp.length);
            setTlv(TagsTable.AMOUNT_OTHER, amount);
        }

        setTlv(TagsTable.TRANS_TYPE, new byte[]{transParam.ucTransType});
        setTlv(TagsTable.TRANS_DATE, transParam.aucTransDate);
        setTlv(TagsTable.TRANS_TIME, transParam.aucTransTime);

    }

    @Override
    CTransResult processTrans() throws EmvException {
        int ret = init();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        ret = selectApp();
        if (ret != RetCode.EMV_OK) {
            if (ret == RetCode.CLSS_RESELECT_APP) {
                throw new EmvException(RetCode.CLSS_TRY_AGAIN);
            } else {
                ret = RetCode.CLSS_FAILED;
                throw new EmvException(ret);
            }
        }


        //set basic parameters.
        clssBaseParameterSet();

        ret = processRuPay();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }
        if (listener != null) {
            listener.onPromptRemoveCard();
        }

        CTransResult result = genTransResult();
        updateCVMResult(result);

        return result;
    }

    /**
     * set application's data of final select.
     * if the return code is CLSS_RESELECT_APP, application should delete current application from condidate list.
     * and then go back to select the next application.
     *
     * @return 0-success others-fail
     */
    private int selectApp() {
        int ret;
        ret = ClssRuPayApi.Clss_SetFinalSelectData_RuPay(finalSelectData, finalSelectDataLen);
        LogUtils.d("RUPAYTEST", "ret = " + ret + ", Clss_SetFinalSelectData_RuPay, finalSelectData = " + Tools.bcd2Str(finalSelectData));
        if (ret == RetCode.CLSS_RESELECT_APP) {
            ret = ClssEntryApi.Clss_DelCurCandApp_Entry();
            if (ret != RetCode.EMV_OK) {
                LogUtils.e(TAG, "Clss_DelCurCandApp_Entry, ret = " + ret);
                return ret;
            }
            return RetCode.CLSS_RESELECT_APP;
        }

        LogUtils.i(TAG, "SetFinalSelectData_RuPay, ret = " + ret);
        return ret;
    }

    /**
     * transaction process flow of rupay.
     * if the return code is CLSS_RESELECT_APP, application should delete current application from condidate list.
     * and then go back to select the next application.
     *
     * @return 0-success others-fail
     */
    private int processRuPay() {
        int ret;
        byte exceptFileFlg;

        //init the select application of rupay.
        ret = ClssRuPayApi.Clss_InitiateApp_RuPay();
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "Clss_InitiateApp_RuPay, ret = " + ret);
            if (ret == RetCode.CLSS_RESELECT_APP) {
                ret = ClssEntryApi.Clss_DelCurCandApp_Entry();
                if (ret != RetCode.EMV_OK) {
                    LogUtils.e(TAG, "Clss_DelCurCandApp_Entry, ret = " + ret);
                    return ret;
                }
                return RetCode.CLSS_RESELECT_APP;
            }
            LogUtils.e(TAG, "Clss_InitiateApp_RuPay, ret = " + ret);
            return ret;
        }

        //read application's data
        ret = ClssRuPayApi.Clss_ReadData_RuPay();
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "Clss_ReadData_RuPay, ret = " + ret);
            return ret;
        }

        //delet all revocable list
        ClssRuPayApi.Clss_DelAllRevocList_RuPay();
        ClssRuPayApi.Clss_DelAllCAPK_RuPay();
        addCapkRevList();

        //offline data authentication
        ret = ClssRuPayApi.Clss_CardAuth_RuPay();
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "Clss_CardAuth_RuPay, ret = " + ret);
            return ret;
        }

        //exceptFileFlg is a input parameter.
        exceptFileFlg = 0;
        ret = ClssRuPayApi.Clss_TransProc_RuPay(exceptFileFlg);
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "Clss_TransProc_RuPay, ret = " + ret);
            return ret;
        }

        //start RuPay's transaction. it will do below actions
        //1.first terminal action analysis. 2.transaction recovery. 3.first card action analysis.
        ret = ClssRuPayApi.Clss_StartTrans_RuPay();
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "Clss_StartTrans_RuPay, ret = " + ret);
            if (ret == RetCode.CLSS_RESELECT_APP) {
                ret = ClssEntryApi.Clss_DelCurCandApp_Entry();
                if (ret != RetCode.EMV_OK) {
                    LogUtils.e(TAG, "Clss_DelCurCandApp_Entry, ret = " + ret);
                    return ret;
                }
                return RetCode.CLSS_RESELECT_APP;
            }
            LogUtils.e(TAG, "Clss_StartTrans_RuPay, ret = " + ret);
            return ret;
        }

        return RetCode.EMV_OK;
    }

    /**
     * generate transaction result through get tag(0xDF8129)
     *
     * @return CTransResult
     */
    private CTransResult genTransResult() {
        ByteArray aucOutcomeParamSet = new ByteArray();

        int ret = getTlv(TagsTable.LIST, aucOutcomeParamSet);
        if (ret != RetCode.EMV_OK) {
            return new CTransResult(ETransResult.CLSS_OC_DECLINED);
        }

        switch (aucOutcomeParamSet.data[0] & 0xF0) {
            case OutcomeParam.CLSS_OC_APPROVED:
                return new CTransResult(ETransResult.CLSS_OC_APPROVED);
            case OutcomeParam.CLSS_OC_DECLINED:
                return new CTransResult(ETransResult.CLSS_OC_DECLINED);
            case OutcomeParam.CLSS_OC_ONLINE_REQUEST:
                return new CTransResult(ETransResult.CLSS_OC_ONLINE_REQUEST);
            case OutcomeParam.CLSS_OC_TRY_ANOTHER_INTERFACE:
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
            default:
                return new CTransResult(ETransResult.CLSS_OC_END_APPLICATION);
        }
    }

    /**
     * update CVM result through get tag(0xDF8129), and set the CVM result.
     *
     * @param result:transaction result
     */
    private void updateCVMResult(CTransResult result) {
        ByteArray aucOutcomeParamSet = new ByteArray();

        int ret = getTlv(TagsTable.LIST, aucOutcomeParamSet);
        if (ret == RetCode.EMV_OK) {
            switch (aucOutcomeParamSet.data[3] & 0xF0) {
                case OutcomeParam.CLSS_OC_NO_CVM:
                    result.setCvmResult(ECvmResult.NO_CVM);
                    LogUtils.i(TAG, "CVM = no cvm");
                    break;
                case OutcomeParam.CLSS_OC_OBTAIN_SIGNATURE:
                    result.setCvmResult(ECvmResult.SIG);
                    LogUtils.i(TAG, "CVM = signature");
                    break;
                case OutcomeParam.CLSS_OC_ONLINE_PIN:
                    result.setCvmResult(ECvmResult.ONLINE_PIN);
                    LogUtils.i(TAG, "CVM = online pin");
                    break;
                case OutcomeParam.CLSS_OC_CONFIRM_CODE_VER:
                    result.setCvmResult(ECvmResult.OFFLINE_PIN);
                    LogUtils.i(TAG, "CVM = CLSS_OC_CONFIRM_CODE_VER");
                    break;
                default:
                    result.setCvmResult(ECvmResult.NO_CVM);
                    LogUtils.i(TAG, " default CVM = no cvm");
                    break;
            }
        }
    }

    @Override
    CTransResult completeTrans(ETransResult transResult, byte[] tag91, byte[] tag71,
                               byte[] tag72) throws EmvException {
        int onlineResult;
        ByteArray scriptRstOut = new ByteArray();
        ACType acTypeOut = new ACType();
        ByteArray authCode = new ByteArray();
        ByteArray respCode = new ByteArray();

        //authCode and respCode have been set in onlineProc().
        getTlv(TagsTable.AUTH_CODE, authCode);
        getTlv(TagsTable.AUTH_RESP_CODE, respCode);
        if (transResult == ETransResult.ABORT_TERMINATED) {
            onlineResult = OnlineResult.ONLINE_FAILED;
        } else if (transResult == ETransResult.ONLINE_APPROVED) {
            onlineResult = OnlineResult.ONLINE_APPROVE;
        } else if (!Arrays.equals(Arrays.copyOf(respCode.data, respCode.length), BAD_TEMINAL_ID.getBytes())) {
            onlineResult = OnlineResult.ONLINE_ABORT;
        } else {
            onlineResult = OnlineResult.ONLINE_DENIAL;
        }


        byte[] issueScript = EmvImpl.combine7172(tag71, tag72);


        int ret = ClssRuPayApi.Clss_CompleteTrans_RuPay(onlineResult, issueScript, issueScript.length, scriptRstOut, acTypeOut);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }
        return new CTransResult(ETransResult.ONLINE_APPROVED);
    }

    @Override
    protected void onAddCapkRevList(EMV_CAPK emvCapk, EMV_REVOCLIST emvRevoclist) {
        int ret = ClssRuPayApi.Clss_AddCAPK_RuPay(emvCapk);
        LogUtils.i(TAG, "Clss_AddCAPK_RuPay, ret = " + ret);
        ret = ClssRuPayApi.Clss_AddRevocList_RuPay(emvRevoclist);
        LogUtils.i(TAG, "Clss_AddRevocList_RuPay, ret = " + ret);
    }

    @Override
    int setTlv(int tag, byte[] value) {
        byte[] bcdTag = Tools.int2ByteArray(tag);
        byte[] buf = new byte[bcdTag.length + 1 + (value != null ? value.length : 0)];

        System.arraycopy(bcdTag, 0, buf, 0, bcdTag.length);
        if (value != null) {
            buf[bcdTag.length] = (byte) value.length;
            System.arraycopy(value, 0, buf, bcdTag.length + 1, value.length);
        } else {
            buf[bcdTag.length] = 0x00;
        }
        return ClssRuPayApi.Clss_SetTLVDataList_RuPay(buf, buf.length);

    }

    @Override
    int getTlv(int tag, ByteArray value) {
        byte[] bcdTag = Tools.int2ByteArray(tag);

        int ret = ClssRuPayApi.Clss_GetTLVDataList_RuPay(bcdTag, (byte) bcdTag.length, value.length, value);
        LogUtils.i(TAG, " getClssTlv_RUPAY  tag :" + tag
                + " value: " + Tools.bcd2Str(value.data).substring(0, 2 * value.length) + " ret :" + ret);
        return ret;
    }

    @Override
    String getTrack1() {
        ByteArray track = new ByteArray();
        int ret = getTlv(TagsTable.TRACK1, track);
        if (ret == RetCode.EMV_OK) {
            return Tools.bcd2Str(track.data, track.length);
        }

        return "";
    }

    @Override
    String getTrack2() {
        ByteArray track = new ByteArray();
        int ret;

        ret = getTlv(TagsTable.TRACK2, track);
        if (ret == RetCode.EMV_OK) {
            //AET-173
            return getTrack2FromTag57(Tools.bcd2Str(track.data, track.length));
        }
        return "";
    }

    @Override
    String getTrack3() {
        return "";
    }
}
