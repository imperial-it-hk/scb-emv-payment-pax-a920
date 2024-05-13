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
 * 20190108  	         linhb                   Create
 * ===========================================================================================
 */
package com.evp.eemv.clss;

import com.evp.bizlib.AppConstants;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.IClssListener;
import com.evp.eemv.entity.AidParam;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.ClssTornLogRecord;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Converter;
import com.evp.eemv.utils.Tools;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.CLSS_TORN_LOG_RECORD;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.Clss_TransParam;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.EMV_REVOCLIST;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.device.model.ApduRespL2;
import com.pax.jemv.device.model.ApduSendL2;
import com.pax.jemv.paypass.api.ClssPassApi;
import com.pax.jemv.paypass.listener.ClssPassCBFunApi;
import com.pax.jemv.paypass.listener.IClssPassCBFun;

import java.util.Arrays;

class ClssProcMc extends ClssProc {

    private static final int ERR_INDICATION = 0xDF8115;
    private static final int USER_INTER_REQ = 0xDF8116;
    private ClssPassListener clssPassListener = new ClssPassListener();
    private ClssPassCBFunApi passCBFun = ClssPassCBFunApi.getInstance();

    static {
        System.loadLibrary("F_MC_LIB_PayDroid");
        System.loadLibrary("JNI_MC_v100_01");
    }

    ClssProcMc(IClssListener listener) {
        super(listener);
    }

    private int init() {
        int ret = ClssPassApi.Clss_CoreInit_MC((byte) 0x01);
        LogUtils.i(TAG, "Clss_CoreInit_MC = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ret = setParamMc("010104", 3);
        LogUtils.i(TAG, "Clss_SetParam_MC = " + ret);

        passCBFun.setICBFun(clssPassListener);
        ret = ClssPassApi.Clss_SetCBFun_SendTransDataOutput_MC();
        LogUtils.i(TAG, "Clss_SetCBFun_SendTransDataOutput_MC = " + ret);
        ret = ClssPassApi.Clss_SetFinalSelectData_MC(finalSelectData, finalSelectDataLen);
        LogUtils.i(TAG, "Clss_SetFinalSelectData_MC = " + ret);
        return ret;
    }

    private int setParamMc(String tlv, int len) {
        return ClssPassApi.Clss_SetParam_MC(Tools.str2Bcd(tlv), len);
    }

    private int selectApp() {
        for (Clss_PreProcInfo info : arrayPreProcInfo) {
            if (info != null && Arrays.equals(aid.getAid(), info.aucAID)) {
                setMcTermParam(transParam, aid, info);
            }
        }

        int ret = ClssPassApi.Clss_InitiateApp_MC();
        LogUtils.i(TAG, "Clss_InitiateApp_MC = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ret = ClssPassApi.Clss_ReadData_MC(transactionPath);    // PathTypeOut
        LogUtils.i(TAG, "Clss_ReadData_MC = " + ret);
        return ret;
    }

    private int processMChip(ACType acType) {
        ClssPassApi.Clss_DelAllRevocList_MC_MChip();
        ClssPassApi.Clss_DelAllCAPK_MC_MChip();
        addCapkRevList();

        if (tornLogRecords != null && !tornLogRecords.isEmpty()) {
            int tornSum = tornLogRecords.size();
            LogUtils.i(TAG, "ClssTornLog = " + tornSum);
            CLSS_TORN_LOG_RECORD[] records = new CLSS_TORN_LOG_RECORD[tornSum];
            for (int i = 0; i < tornSum; ++i) {
                records[i] = Converter.toClssTornLogRecord(tornLogRecords.get(i));
            }
            ClssPassApi.Clss_SetTornLog_MC_MChip(records, tornSum);
        }

        int ret = ClssPassApi.Clss_TransProc_MC_MChip(acType);
        LogUtils.d("EMVCLSS", "in ClssProMc#processMChip，after ClssPassApi.Clss_TransProc_MC_MChip");
        LogUtils.i(TAG, "Clss_TransProc_MC_MChip = " + ret + "  ACType = " + acType.type);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        CLSS_TORN_LOG_RECORD[] records = new CLSS_TORN_LOG_RECORD[5];
        int[] updateFlg = new int[2];
        ret = ClssPassApi.Clss_GetTornLog_MC_MChip(records, updateFlg);
        LogUtils.i(TAG, "Clss_GetTornLog_MC_MChip = " + ret);
        if (ret == RetCode.EMV_OK && updateFlg[1] == 1 && tornLogRecords != null) {
            tornLogRecords.clear();
            for (CLSS_TORN_LOG_RECORD i : records) {
                tornLogRecords.add(new ClssTornLogRecord(i));
            }
        }
        return RetCode.EMV_OK;
    }

    private int processMag(ACType acType) {
        int ret = ClssPassApi.Clss_TransProc_MC_Mag(acType);
        LogUtils.i(TAG, "Clss_TransProc_MC_Mag = " + ret + "  ACType = " + acType.type);
        return ret;
    }

    private CTransResult genTransResult() {

        LogUtils.i(TAG, Tools.bcd2Str(clssPassListener.outcomeParamSet.data));
        switch (clssPassListener.outcomeParamSet.data[0] & 0xF0) {
            case 0x10:
                return new CTransResult(ETransResult.CLSS_OC_APPROVED);
            case 0x30:
                return new CTransResult(ETransResult.CLSS_OC_ONLINE_REQUEST);
            case 0x60:
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
            case 0x20:
            default:
                return new CTransResult(ETransResult.CLSS_OC_DECLINED);
        }
    }

    private void updateCVMResult(CTransResult result) {
        switch (clssPassListener.outcomeParamSet.data[3] & 0x30) {
            case 0x10:
                result.setCvmResult(ECvmResult.SIG);
                LogUtils.i(TAG, "CVM = signature");
                break;
            case 0x20:
                result.setCvmResult(ECvmResult.ONLINE_PIN);
                LogUtils.i(TAG, "CVM = online pin");
                break;
            default:
                result.setCvmResult(ECvmResult.NO_CVM);
                LogUtils.i(TAG, "CVM = no cvm");
                break;
        }
    }

    @Override
    public CTransResult processTrans() throws EmvException {
        try {
            int ret = init();
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }

            ret = selectApp();
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }

            ACType acType = new ACType();
            if (transactionPath.path == TransactionPath.CLSS_MC_MCHIP) {// MChip = 6
                ret = processMChip(acType);
            } else if (transactionPath.path == TransactionPath.CLSS_MC_MAG) {// mag = 5
                ret = processMag(acType);
            }
            LogUtils.i("CTLS_SPEED_TEST", "inClssProcMc#processTrans After processMChip");

            /**
             * If ‘On device cardholder verification is supported' (TAG '82' Byte1 b2)in Application Interchange Profile is set and
             * 'On device cardholder verification supported'(TAG 'DF811B' Byte1,b6) in Kernel Configuration is set,
             * the kernel will return SEE PHONE in Message Identifier (byte1) of DF8116.
             */
            //the kernel will invoke this
//                clssPassListener.sendTransDataOutput((byte) 0x07);
            LogUtils.d("DF8115", clssPassListener.errIndication.data[0] + "");
            if (clssPassListener.userInterReqData.data[0] == 0x20) {//value of DF8116 is 00100000: SEE PHONE
                if (listener != null) {
                    listener.onPromptRemoveCard();
                    ret = listener.onDisplaySeePhone();
                    if (ret != 0) {
                        return new CTransResult(ETransResult.ABORT_TERMINATED);
                    }
                    return new CTransResult(ETransResult.CLSS_OC_SEE_PHONE);
                }
            }

            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }

            if (acType.type == ACType.AC_AAC) {
                LogUtils.i(TAG, "Card decline but try another interface offered to customer.");
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE); //MasterCard certification requirements
                //return new CTransResult(ETransResult.CLSS_OC_DECLINED);
            }

            LogUtils.i(TAG, "setDetData :" + Tools.bcd2Str(clssPassListener.outcomeParamSet.data));
            if (clssPassListener.outcomeParamSet.data[0] == 0x70 || clssPassListener.outcomeParamSet.data[1] != (byte) 0xF0) {
                return new CTransResult(ETransResult.CLSS_OC_TRY_AGAIN);
            }
            CTransResult result = genTransResult();
            updateCVMResult(result);

            if (listener != null) {
                listener.onPromptRemoveCard();
            }

            return result;
        } finally {
            passCBFun.setICBFun(null);  //fix leaks
        }
    }

    private int setEmptyTlv(int tag) {
        byte[] bcdTag = Tools.int2ByteArray(tag);
        return ClssPassApi.Clss_SetTLVDataList_MC(bcdTag, 0);
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
        return ClssPassApi.Clss_SetTLVDataList_MC(buf, buf.length);
    }

    private void setMcTermParam(Clss_TransParam clssTransParam, AidParam aid, Clss_PreProcInfo info) {
        byte[] tmp = Tools.str2Bcd(String.valueOf(clssTransParam.ulAmntAuth));
        byte[] amount = new byte[6];
        System.arraycopy(tmp, 0, amount, 6 - tmp.length, tmp.length);
        setTlv(TagsTable.AMOUNT, amount);

        tmp = Tools.str2Bcd(Long.toString(clssTransParam.ulAmntOther));
        amount = new byte[6];
        System.arraycopy(tmp, 0, amount, 6 - tmp.length, tmp.length);
        setTlv(TagsTable.AMOUNT_OTHER, amount);

        setTlv(TagsTable.TRANS_TYPE, new byte[]{clssTransParam.ucTransType});
        setTlv(TagsTable.TRANS_DATE, clssTransParam.aucTransDate);
        setTlv(TagsTable.TRANS_TIME, clssTransParam.aucTransTime);

        String amtStr = Tools.getPaddedNumber(info.ulRdClssFLmt, 12);
        setTlv(TagsTable.FLOOR_LIMIT, Tools.str2Bcd(amtStr));
        LogUtils.i(TAG, String.format("%s%s", "FLOOR_LIMIT", amtStr));

        amtStr = Tools.getPaddedNumber(info.ulRdClssTxnLmt, 12);
        setTlv(TagsTable.TRANS_LIMIT, Tools.str2Bcd(amtStr));
        LogUtils.i(TAG, String.format("%s%s", "TRANS_LIMIT", amtStr));

        amtStr = Tools.getPaddedNumber(info.ulRdClssTxnLmt, 12);
        setTlv(TagsTable.TRANS_CVM_LIMIT, Tools.str2Bcd(amtStr));
        LogUtils.i(TAG, String.format("%s%s", "TRANS_CVM_LIMIT", amtStr));

        amtStr = Tools.getPaddedNumber(info.ulRdCVMLmt, 12);
        setTlv(TagsTable.CVM_LIMIT, Tools.str2Bcd(amtStr));
        LogUtils.i(TAG, String.format("%s%s", "CVM_LIMIT", amtStr));

        setTlv(TagsTable.CVM_REQ, new byte[]{(byte) 0x20});
        setTlv(TagsTable.CVM_NO, new byte[]{(byte) 0x08});
        setTlv(TagsTable.MAG_CVM_REQ, new byte[]{(byte) 0x10});
        setTlv(TagsTable.MAG_CVM_NO, new byte[]{(byte) 0x00});

        setTlv(TagsTable.RRP_MAX_GRACE_PERIOD, new byte[]{0x00, 0x32});
        setTlv(TagsTable.RRP_MIN_GRACE_PERIOD, new byte[]{0x00, 0x14});
        setTlv(TagsTable.RRP_ACCURACY_THRESHOLD, new byte[]{0x01, 0x2C});
        setTlv(TagsTable.RRP_MISMATCH_THRESHOLD, new byte[]{0x32});
        setTlv(TagsTable.RRP_EXP_TIME_FOR_CAPDU, new byte[]{0x00, 0x12});
        setTlv(TagsTable.RRP_EXP_TIME_FOR_RAPDU, new byte[]{0x00, 0x18});

        setTlv(TagsTable.CARD_DATA, new byte[]{(byte) 0xE0});
        setTlv(TagsTable.DEF_UDOL,  new byte[]{(byte) 0x9F, 0x6A, 0x04});
        setTlv(TagsTable.SEC, new byte[]{(byte) 0x08});
        setTlv(TagsTable.MAX_TORN, new byte[]{(byte) 0x00});

        setTlv(TagsTable.TERMINAL_TYPE, new byte[]{cfg.getTermType()});
        setTlv(TagsTable.TERMINAL_CAPABILITY, aid.getTerminalCapabilities());
        setTlv(TagsTable.ADDITIONAL_CAPABILITY, Tools.str2Bcd(cfg.getExCapability()));
        setTlv(TagsTable.COUNTRY_CODE, Tools.str2Bcd(cfg.getCountryCode()));
        setTlv(TagsTable.CURRENCY_CODE, Tools.str2Bcd(cfg.getTransCurrCode()));

        final String strAid = Tools.bcd2Str(aid.getAid());
        if (AppConstants.MASTER_CARD_AID.equals(strAid)) {
            setTlv(TagsTable.KERNEL_CFG, new byte[]{(byte) 0x30});
        } else if (AppConstants.MAESTRO_AID.equals(strAid)) {
            setTlv(TagsTable.KERNEL_CFG, new byte[]{(byte) 0xB0});
        }
        setTlv(TagsTable.MAG_APP_VER, new byte[]{0x00, 0x01});
        setTlv(TagsTable.APP_VER, aid.getVersion());
        setTlv(TagsTable.TERM_DEFAULT, aid.getTacDefault());
        setTlv(TagsTable.TERM_DENIAL, aid.getTacDenial());
        setTlv(TagsTable.TERM_ONLINE, aid.getTacOnline());

        setTlv(TagsTable.MAX_LIFETIME_OF_TORN, new byte[]{0x00, 0x00});
        setTlv(TagsTable.KERNEL_ID, new byte[]{0x02});
        setTlv(TagsTable.INTER_DEV_NUM, Tools.str2Bcd("1122334455667788"));
        setTlv(TagsTable.MERCHANT_CATEGORY_CODE, Tools.str2Bcd("0001"));
        setTlv(TagsTable.DS_OPERATOR_ID, Tools.str2Bcd("7A45123EE59C7F40"));
        setTlv(TagsTable.TERM_RISC_MNGM, Tools.str2Bcd("2C00800000000000"));

        setTlv(TagsTable.ACCOUNT_TYPE, null);
        setTlv(TagsTable.ACQUIRER_ID, null);
        setTlv(TagsTable.MERCHANT_ID, null);
        setTlv(TagsTable.MERCHANT_NAME_LOCATION, null);
        setTlv(TagsTable.TERMINAL_ID, null);
        setTlv(TagsTable.MOB_SUP, null);
        setTlv(TagsTable.DS_AC_TYPE, null);
        setTlv(TagsTable.DS_INPUT_CARD, null);
        setTlv(TagsTable.DS_INPUT_TERMINAL, null);
        setTlv(TagsTable.DS_ODS_INFO, null);
        setTlv(TagsTable.DS_ODS_READER, null);
        setTlv(TagsTable.DS_ODS_TERMINAL, null);
        setEmptyTlv(TagsTable.FST_WRITE);
        setEmptyTlv(TagsTable.READ);
        setEmptyTlv(TagsTable.WIRTE_BEFORE_AC);
        setEmptyTlv(TagsTable.WIRTE_AFTER_AC);
        setEmptyTlv(TagsTable.TIMEOUT);
        setTagPresent(TagsTable.BALANCE_BEFORE_GAC, false);
        setTagPresent(TagsTable.BALANCE_AFTER_GAC, false);
        setTagPresent(TagsTable.MESS_HOLD_TIME, false);
        setTagPresent(TagsTable.DSVN, false);
        setTagPresent(TagsTable.PDE1, false);
        setTagPresent(TagsTable.UDE1, false);
        setTagPresent(TagsTable.HOLD_TIME, false);
        setTagPresent(TagsTable.MSG_HOLD_TIME, false);
    }

    private void setTagPresent(int tag, boolean present) {
        ClssPassApi.Clss_SetTagPresent_MC(Tools.int2ByteArray(tag), Tools.boolean2Byte(present));
    }

    @Override
    public int getTlv(int tag, ByteArray value) {
        byte[] bcdTag = Tools.int2ByteArray(tag);

        int ret = ClssPassApi.Clss_GetTLVDataList_MC(bcdTag, (byte) bcdTag.length, value.length, value);
        LogUtils.i(TAG, " getClssTlv_MC  tag :" + tag
                + " value: " + Tools.bcd2Str(value.data).substring(0, 2 * value.length) + " ret :" + ret);
        return ret;
    }

    @Override
    protected void onAddCapkRevList(EMV_CAPK emvCapk, EMV_REVOCLIST emvRevoclist) {
        int ret = ClssPassApi.Clss_AddCAPK_MC_MChip(emvCapk);
        LogUtils.i(TAG, "set MC capk ret :" + ret);
        ret = ClssPassApi.Clss_AddRevocList_MC_MChip(emvRevoclist);
        LogUtils.i(TAG, "set MC revoclist ret :" + ret);
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
        int ret = -1;
        if (transactionPath.path == TransactionPath.CLSS_MC_MCHIP) {
            ret = getTlv(TagsTable.TRACK2, track);
        } else if (transactionPath.path == TransactionPath.CLSS_MC_MAG) {
            ret = getTlv(TagsTable.TRACK2_1, track);
        }

        if (ret == RetCode.EMV_OK) {
            //AET-173
            return getTrack2FromTag57(Tools.bcd2Str(track.data, track.length));
        }
        return "";
    }

    @Override
    protected String getTrack3() {
        return "";
    }

    @Override
    protected CTransResult completeTrans(ETransResult transResult, byte[] tag91, byte[] tag71, byte[] tag72) {
        //do nothing
        return new CTransResult(ETransResult.CLSS_OC_APPROVED);
    }

    private class ClssPassListener implements IClssPassCBFun {
        ByteArray outcomeParamSet = new ByteArray(8);
        ByteArray userInterReqData = new ByteArray(22);
        ByteArray errIndication = new ByteArray(6);

        @Override
        public int sendDEKData(byte[] bytes, int i) {
            return 0;
        }

        @Override
        public int receiveDETData(ByteArray byteArray, byte[] bytes) {
            return 0;
        }

        @Override
        public int addAPDUToTransLog(ApduSendL2 apduSendL2, ApduRespL2 apduRespL2) {
            return 0;
        }

        @Override
        public int sendTransDataOutput(byte b) {
            if ((b & 0x01) != 0) {
                getTlv(TagsTable.LIST, outcomeParamSet);
            }

            if ((b & 0x04) != 0) {
                getTlv(USER_INTER_REQ, userInterReqData);
            }

            if ((b & 0x02) != 0) {
                getTlv(ERR_INDICATION, errIndication);
            }
            return RetCode.EMV_OK;
        }
    }
}
