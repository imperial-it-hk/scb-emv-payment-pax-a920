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
 * 20190108  	         lixc                    Create
 * ===========================================================================================
 */
package com.evp.eemv.clss;

import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.IClssListener;
import com.evp.eemv.entity.AidParam;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Tools;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.Clss_TransParam;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.EMV_REVOCLIST;
import com.pax.jemv.clcommon.OutcomeParam;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.entrypoint.api.ClssEntryApi;
import com.pax.jemv.jcb.api.ClssJCBApi;

import java.util.Arrays;

class ClssProcJcb extends ClssProc {


    static {
        System.loadLibrary("F_JCB_LIB_PayDroid");
        System.loadLibrary("JNI_JCB_v100");
    }

    ClssProcJcb(IClssListener listener) {
        super(listener);
    }

    private int jcbFlow() {
        int ret = ClssJCBApi.Clss_CoreInit_JCB();
        LogUtils.i(TAG, "Clss_CoreInit_JCB = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }
        ret = ClssJCBApi.Clss_SetFinalSelectData_JCB(finalSelectData, finalSelectDataLen);
        LogUtils.i(TAG, "Clss_SetFinalSelectData_JCB = " + ret);


        for (Clss_PreProcInfo info : arrayPreProcInfo) {
            if (info != null && Arrays.equals(aid.getAid(), info.aucAID)) {
                setTransParamJcb(transParam, aid, info);
            }
        }

        ret = ClssJCBApi.Clss_InitiateApp_JCB(transactionPath);
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "ClssJCBApi.Clss_InitiateApp_JCB(transactionPath) error, ret = " + ret);
            return ret;
        }

        LogUtils.i(TAG, "ClssJCBApi transactionPath = " + transactionPath.path);

        ret = ClssJCBApi.Clss_ReadData_JCB();
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "ClssJCBApi.Clss_ReadData_JCB() error, ret = " + ret);
            return ret;
        }
        ret = appTransProc((byte) transactionPath.path);
        LogUtils.i(TAG, "appTransProc ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "appTransProc(transactionPath) error, ret = " + ret);
            return ret;
        }
        return ret;
    }

    @Override
    protected CTransResult processTrans() throws EmvException {
        int ret = jcbFlow();
        if (ret == RetCode.CLSS_RESELECT_APP) {
            ret = ClssEntryApi.Clss_DelCurCandApp_Entry();
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
            ret = RetCode.CLSS_TRY_AGAIN;
            throw new EmvException(ret);
        }

        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        CTransResult result = genTransResult();
        updateCVMResult(result);

        return result;

    }


    private CTransResult genTransResult() {
        byte[] szBuff = new byte[]{(byte) 0xDF, (byte) 0x81, 0x29};//Outcome Parameter
        ByteArray aucOutcomeParamSetJcb = new ByteArray();

        int ret = ClssJCBApi.Clss_GetTLVDataList_JCB(szBuff, (byte) 3, 24, aucOutcomeParamSetJcb);
        if (ret != RetCode.EMV_OK) {
            return new CTransResult(ETransResult.CLSS_OC_DECLINED);
        }

        switch (aucOutcomeParamSetJcb.data[0] & 0xF0) {
            case OutcomeParam.CLSS_OC_APPROVED:
                return new CTransResult(ETransResult.CLSS_OC_APPROVED);
            case OutcomeParam.CLSS_OC_ONLINE_REQUEST:
                return new CTransResult(ETransResult.CLSS_OC_ONLINE_REQUEST);
            case OutcomeParam.CLSS_OC_TRY_ANOTHER_INTERFACE:
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
            case OutcomeParam.CLSS_OC_DECLINED:
            default://CLSS_OC_END_APPLICATION
                return new CTransResult(ETransResult.CLSS_OC_DECLINED);
        }
    }

    private void updateCVMResult(CTransResult result) {
        byte[] szBuff = new byte[]{(byte) 0xDF, (byte) 0x81, 0x29};//Outcome Parameter
        ByteArray aucOutcomeParamSetJcb = new ByteArray();

        int ret = ClssJCBApi.Clss_GetTLVDataList_JCB(szBuff, (byte) 3, 24, aucOutcomeParamSetJcb);
        if (ret == RetCode.EMV_OK) {
            switch (aucOutcomeParamSetJcb.data[3] & 0xF0) {
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
                case OutcomeParam.CLSS_OC_NO_CVM:
                    result.setCvmResult(ECvmResult.NO_CVM);
                    LogUtils.i(TAG, "CVM = no cvm");
                    break;
                default:
                    result.setCvmResult(ECvmResult.NO_CVM);
                    LogUtils.i(TAG, " default CVM = no cvm");
                    break;
            }
        }
    }

    @Override
    protected void onAddCapkRevList(EMV_CAPK emvCapk, EMV_REVOCLIST emvRevoclist) {
        int ret = ClssJCBApi.Clss_AddCAPK_JCB(emvCapk);
        LogUtils.i(TAG, "set JCB capk ret :" + ret);
        ret = ClssJCBApi.Clss_AddRevocList_JCB(emvRevoclist);
        LogUtils.i(TAG, "set JCB revoclist ret :" + ret);
    }

    @Override
    protected String getTrack1() {
        ByteArray track = new ByteArray();
        int ret = getTlv(TagsTable.TRACK1, track);
        if (ret == RetCode.EMV_OK)
            return Tools.bcd2Str(track.data, track.length);
        return "";
    }

    @Override
    protected String getTrack2() {
        ByteArray track = new ByteArray();
        int ret;
        if (transactionPath.path == TransactionPath.CLSS_JCB_MAG) {
            ret = getTlv(TagsTable.TRACK2_1, track);
            if (ret != RetCode.EMV_OK) {
                ret = getTlv(TagsTable.TRACK2, track);
            }
        } else {
            ret = getTlv(TagsTable.TRACK2, track);
        }

        if (ret == RetCode.EMV_OK) {
            //AET-173
            return Tools.bcd2Str(track.data, track.length).split("F")[0];
        }
        return "";
    }

    @Override
    String getTrack3() {
        return "";
    }

    private int appTransProc(byte transPath) {
        int ret;
        byte ucExceptFileFlg = 0;

        if (transPath == TransactionPath.CLSS_JCB_EMV) {// 0x06)
            ClssJCBApi.Clss_DelAllRevocList_JCB();
            ClssJCBApi.Clss_DelAllCAPK_JCB();

            addCapkRevList();

            ret = ClssJCBApi.Clss_TransProc_JCB(ucExceptFileFlg);
            if (ret != RetCode.EMV_OK) {
                LogUtils.e(TAG, "EMV Clss_TransProc_JCB error, ret = " + ret);
                return ret;
            }

            ret = ClssJCBApi.Clss_CardAuth_JCB();
            LogUtils.i(TAG, "ClssJCBApi.Clss_CardAuth_JCB ret = " + ret);
        } else {// 0x05)
            ret = ClssJCBApi.Clss_TransProc_JCB(ucExceptFileFlg);
            LogUtils.i(TAG, "MAG or LEGACY ClssJCBApi.Clss_TransProc_JCB ret = " + ret);
        }

        ByteArray byteArray = new ByteArray();
        int iRet = ClssJCBApi.Clss_GetTLVDataList_JCB(new byte[]{(byte) 0x95}, (byte) 1, 10, byteArray);
        byte[] a = new byte[byteArray.length];
        System.arraycopy(byteArray.data, 0, a, 0, byteArray.length);
        String tvr = Tools.bcd2Str(a);
        LogUtils.i("Clss_TLV_JCB iRet 0x95", Integer.toString(iRet) + "");
        LogUtils.i("Clss_JCB TVR 0x95", tvr + "");
        return ret;
    }

    private void setTransParamJcb(Clss_TransParam clssTransParam, AidParam aid, Clss_PreProcInfo info) {
        byte[] tmp = Tools.str2Bcd(String.valueOf(clssTransParam.ulAmntAuth));
        byte[] amount = new byte[6];
        System.arraycopy(tmp, 0, amount, 6 - tmp.length, tmp.length);
        setTlv(TagsTable.AMOUNT, amount);

        tmp = Tools.str2Bcd(Long.toString(clssTransParam.ulAmntOther));
        amount = new byte[6];
        System.arraycopy(tmp, 0, amount, 6 - tmp.length, tmp.length);
        setTlv(TagsTable.AMOUNT_OTHER, amount);

        setTlv(TagsTable.TRANS_DATE, clssTransParam.aucTransDate);
        setTlv(TagsTable.TRANS_TIME, clssTransParam.aucTransTime);
        setTlv(TagsTable.TRANS_TYPE, new byte[]{clssTransParam.ucTransType});
        setTlv(TagsTable.ACQUIRER_ID, null);
        setTlv(TagsTable.MERCHANT_NAME_LOCATION, null);
        setTlv(TagsTable.MERCHANT_CATEGORY_CODE, Tools.str2Bcd("0000"));
        setTlv(TagsTable.COUNTRY_CODE, Tools.str2Bcd(cfg.getCountryCode()));
        setTlv(TagsTable.TERMINAL_TYPE, new byte[]{cfg.getTermType()});
        setTlv(TagsTable.CURRENCY_CODE, Tools.str2Bcd(cfg.getTransCurrCode()));
        setTlv(TagsTable.TRANS_CURRRENCY_EXPONENT, new byte[]{cfg.getTransCurrExp()});
        setTlv(TagsTable.TERMINAL_INTERCHANGE_PROFILE, aid.getJcbClssTermIntProfile());
        setTlv(TagsTable.TERMINAL_COMPATIBILITY_INDICATOR, new byte[]{aid.getJcbClssTermCompatIndicator()});
        setTlv(TagsTable.TERM_DEFAULT, aid.getTacDefault());
        setTlv(TagsTable.TERM_DENIAL, aid.getTacDenial());
        setTlv(TagsTable.TERM_ONLINE, aid.getTacOnline());
        setTlv(TagsTable.COMBINATION_OPTION, aid.getJcbClssCombinationOpt());
        setTlv(TagsTable.FLOOR_LIMIT, Tools.str2Bcd(Tools.getPaddedNumber(info.ulRdClssFLmt, 12)));
        setTlv(TagsTable.TRANS_LIMIT, Tools.str2Bcd(Tools.getPaddedNumber(info.ulRdClssTxnLmt, 12)));
        setTlv(TagsTable.TRANS_CVM_LIMIT, Tools.str2Bcd(Tools.getPaddedNumber(info.ulRdClssTxnLmt, 12)));
        setTlv(TagsTable.CVM_LIMIT, Tools.str2Bcd(Tools.getPaddedNumber(info.ulRdCVMLmt, 12)));
    }

    @Override
    public int setTlv(int tag, byte[] value) {
        byte[] bcdTag = Tools.int2ByteArray(tag);
        byte[] buf = new byte[bcdTag.length + 1 + (value != null ? value.length : 0)];

        System.arraycopy(bcdTag, 0, buf, 0, bcdTag.length);
        if (value != null) {
            buf[bcdTag.length] = (byte) value.length;
            System.arraycopy(value, 0, buf, bcdTag.length + 1, value.length);
        } else {
            buf[bcdTag.length] = 0x00;
        }
        return ClssJCBApi.Clss_SetTLVDataList_JCB(buf, buf.length);
    }

    @Override
    public int getTlv(int tag, ByteArray value) {
        byte[] bcdTag = Tools.int2ByteArray(tag);

        int ret = ClssJCBApi.Clss_GetTLVDataList_JCB(bcdTag, (byte) bcdTag.length, value.length, value);
        LogUtils.i(TAG, " getClssTlv_JCB  tag :" + tag
                + " value: " + Tools.bcd2Str(value.data).substring(0, 2 * value.length) + " ret :" + ret);
        return ret;
    }

    @Override
    protected CTransResult completeTrans(ETransResult transResult, byte[] tag91, byte[] tag71, byte[] tag72) {
        //do nothing
        return new CTransResult(ETransResult.CLSS_OC_APPROVED);
    }
}
