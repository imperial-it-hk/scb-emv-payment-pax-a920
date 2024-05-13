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
 * 20190108  	         huangjian               Create
 * ===========================================================================================
 */

package com.evp.eemv.clss;

import com.evp.bizlib.AppConstants;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.IClssListener;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Converter;
import com.evp.eemv.utils.Tools;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.CvmType;
import com.pax.jemv.clcommon.DDAFlag;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.EMV_REVOCLIST;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.qpboc.api.ClssPbocApi;
import com.pax.jemv.qpboc.model.Clss_PbocAidParam;
import com.pax.jemv.qpboc.model.Clss_PbocTornConfig;

import java.util.Arrays;

class ClssProcPBOC extends ClssProc {

    private static final String DEBIT1 = "A000000333010101";
    private static final String DEBIT2 = "A000000333010106";
    private static final String CREDIT1 = "A000000333010102";
    private static final String QCREDIT = "A000000333010103";


    private Clss_PreProcInfo clssPreProcInfo;
    private String track2;
    private boolean isNeedOffline = false;

    static {
        System.loadLibrary("F_QPBOC_LIB_PayDroid");
        System.loadLibrary("JNI_QPBOC_v100");
    }

    ClssProcPBOC(IClssListener listener) {
        super(listener);
    }


    void ClearExpiredTornLog() {
        ByteArray delTornFlag = new ByteArray();
        while (true) {
            int ret = ClssPbocApi.Clss_ClearTornLog_Pboc((byte) 2, delTornFlag);
            if (ret == RetCode.EMV_NO_DATA && delTornFlag.data[0] != 1) {
                return;
            }
        }
    }

    private int init() {
        ByteArray version = new ByteArray(16);
        ClssPbocApi.Clss_ReadVerInfo_Pboc(version);
        LogUtils.i(TAG, "pboc version " + new String(version.data));
        int ret = ClssPbocApi.Clss_CoreInit_Pboc();
        LogUtils.i(TAG, "clssPBOCCoreInit ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ClssPbocApi.Clss_SetQUICSFlag_Pboc((byte) 2);
        Clss_PbocTornConfig pbocTornConfig = new Clss_PbocTornConfig(300, (short) 3, (short) 1, new byte[4]);
        ret = ClssPbocApi.Clss_TornSetConfig_Pboc(pbocTornConfig);
        LogUtils.i(TAG, "Pboc_ornSetConfig ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ClearExpiredTornLog();


        ret = ClssPbocApi.Clss_SetReaderParam_Pboc(Converter.toClssReaderParam(cfg, aid));
        LogUtils.i(TAG, "PbocSetReaderParam ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }
        ret = ClssPbocApi.Clss_SetPbocAidParam_Pboc(new Clss_PbocAidParam(aid.getFloorLimit(), new byte[4]));
        LogUtils.i(TAG, "PbocSetPbocAidParam ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ret = ClssPbocApi.Clss_SetFinalSelectData_Pboc(finalSelectData, finalSelectDataLen);
        LogUtils.i(TAG, "PbocSetFinalSelectData ret = " + ret);
        return ret;
    }

    private int setTransParam() {
        int ret = ClssPbocApi.Clss_SetTransData_Pboc(transParam, preProcInterInfo);
        LogUtils.i(TAG, "PbocSetTransData ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        clssPreProcInfo = null;
        for (Clss_PreProcInfo i : arrayPreProcInfo) {
            if (Arrays.equals(aid.getAid(), i.aucAID)) {
                clssPreProcInfo = i;
                break;
            }
        }

        setTlv(TagsTable.APP_VER, aid.getVersion());

        return clssPreProcInfo == null ? RetCode.EMV_NO_APP : RetCode.EMV_OK;
    }

    private int wholeTornProcess(ACType acType) {
        byte[] tornBuff = new byte[4];
        int ret = ClssPbocApi.Clss_TornProcessing_Pboc((byte) 0, tornBuff);
        LogUtils.i(TAG, "tornBuff: " + Tools.bcd2Str(tornBuff));
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "Clss_TornProcessing_Pboc return " + ret);
            return ret;
        } else if (tornBuff[1] == 0) {
            ByteArray failFlag = new ByteArray(1);
            ret = ClssPbocApi.Clss_GetTornFailFlag_Pboc(failFlag);
            if (ret == RetCode.EMV_OK && failFlag.data[0] == 2) {
                ByteArray clearFlag = new ByteArray(1);
                clearFlag.data[0] = 1;
                while (clearFlag.data[0] == 1) {
                    ClssPbocApi.Clss_ClearTornLog_Pboc((byte) 0, clearFlag);
                }
            }
            return RetCode.EMV_OK;
        } else if (tornBuff[1] == 1) {
            isNeedOffline = true;
            return offlineTornProcess(acType);
        }
        return RetCode.EMV_OK;
    }

    private int offlineTornProcess(ACType acType) {
        int ret = 0;
        //according to EDC
        boolean isNeedSaveTorn = false;
        boolean isNeedDelTorn = false;
        ClssPbocApi.Clss_DelAllRevocList_Pboc();
        ClssPbocApi.Clss_DelAllCAPK_Pboc();
        addCapkRevList();

        DDAFlag flag = new DDAFlag();
        ret = ClssPbocApi.Clss_CardAuth_Pboc(acType, flag);
        if (ret != RetCode.EMV_OK) {
            if (ret == RetCode.CLSS_USE_CONTACT) {
                return ret;
            }
            ByteArray tornFailFlag = new ByteArray(1);
            int ret1 = ClssPbocApi.Clss_GetTornFailFlag_Pboc(tornFailFlag);
            if (ret1 == RetCode.EMV_OK) {
                if (tornFailFlag.data[0] == 2) { //Application should delete the fail torn log
                    isNeedDelTorn = true;
                } else if (tornFailFlag.data[0] == 1) {//There is a fail torn log deleted and the data of the deleted torn log are saved in the
                    isNeedSaveTorn = true;                            // TLV database.
                }
            }
            LogUtils.d(String.format("isNeedDelTorn:%b,isNeedSaveTorn:%b",isNeedDelTorn,isNeedSaveTorn));
            return ret;
        } else if (flag.flag == DDAFlag.FAIL) {
            return RetCode.CLSS_TERMINATE;
        }

        if (acType.type == ACType.AC_TC) {
            isNeedDelTorn = true;
        }

        ByteArray clearFlag = new ByteArray(1);
        clearFlag.data[0] = 1;
        while (clearFlag.data[0] == 1) {
            if (isNeedDelTorn) {
                ClssPbocApi.Clss_ClearTornLog_Pboc((byte) 0, clearFlag);
            }
        }

        return RetCode.EMV_OK;
    }

    private int currentTornProcess() {
        return 0;
    }

    @Override
    protected CTransResult processTrans() throws EmvException {
        int ret = init();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        ret = setTransParam();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        // TODO TornProcess
        ACType acType = new ACType();
        isNeedOffline = false;
        ret = wholeTornProcess(acType);
        if (isNeedOffline) {
            if (ret == RetCode.CLSS_USE_CONTACT) {
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
            } else if (ret == RetCode.CLSS_TERMINATE) {
                new CTransResult(ETransResult.ABORT_TERMINATED);
            }
            if (acType.type == ACType.AC_TC) {
                return new CTransResult(ETransResult.CLSS_OC_APPROVED);
            } else if (acType.type == ACType.AC_AAC) {
                return new CTransResult(ETransResult.CLSS_OC_DECLINED);
            } else if (acType.type == ACType.AC_ARQC) {
                return new CTransResult(ETransResult.CLSS_OC_ONLINE_REQUEST);
            }
        }

        ret = ClssPbocApi.Clss_Proctrans_Pboc(transactionPath, acType);
        LogUtils.i(TAG, "clssPbocmaiProcTrans ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            if (ret == RetCode.CLSS_LAST_CMD_ERR) {
                // TODO current tornProcess
                currentTornProcess();
                throw new EmvException(ret);
            }
            if (ret == RetCode.CLSS_REFER_CONSUMER_DEVICE) {
                return new CTransResult(ETransResult.CLSS_OC_TRY_AGAIN);
            }
            if (ret == RetCode.CLSS_USE_CONTACT) {
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
            }
        }
        LogUtils.i(TAG, "clssPbocTrans TransPath = " + transactionPath.path + ", ACType = " + acType.type);

        if (listener != null) {
            listener.onPromptRemoveCard();
        }

        CTransResult result = new CTransResult(ETransResult.ABORT_TERMINATED);
        if (!continueUpdateResult(acType, result)) {
            return result;
        }

        updateResult(result);
        LogUtils.i(TAG, "result ret = " + result.getCvmResult() + "  " + result.getTransResult());
        return result;
    }

    private void updateResult(CTransResult result) throws EmvException {
        CvmType cvmType = new CvmType();
        int ret = ClssPbocApi.Clss_GetCvmType_Pboc(cvmType);
        LogUtils.i(TAG, "clssPbocGetCvmType CVMType = " + cvmType.type);
        if (ret < 0) {
            if (ret == RetCode.CLSS_PARAM_ERR) {
                result.setTransResult(ETransResult.CLSS_OC_DECLINED);
            }
            throw new EmvException(ret);
        }

        result.setCvmResult(Converter.convertCVM((byte) CvmType.RD_CVM_CONSUMER_DEVICE));

        boolean needPin = false;
        ByteArray tmp = new ByteArray();
        getTlv(TagsTable.AID, tmp);
        String clssAid = Tools.bcd2Str(tmp.data, tmp.length);
        LogUtils.i(TAG, "Aid " + clssAid);
        if (isNeedPin(clssAid, cvmType)) {
            LogUtils.i(TAG, " NeedPin");
            needPin = true;
            result.setCvmResult(Converter.convertCVM((byte) CvmType.RD_CVM_ONLINE_PIN));
        }

        if (isNeedSig(clssAid, cvmType)) {
            LogUtils.i(TAG, " NeedSig");
            if (needPin) {
                result.setCvmResult(ECvmResult.ONLINE_PIN_SIG);
            } else {
                result.setCvmResult(Converter.convertCVM((byte) CvmType.RD_CVM_SIG));
            }
        }
    }

    private int processQVSDC(ACType acType, CTransResult result) throws EmvException {
        if ((acType.type == com.pax.jemv.clcommon.ACType.AC_TC)
                && transParam.ucTransType != 0x20) { //no refund
            //according to EDC
            ClssPbocApi.Clss_DelAllRevocList_Pboc();
            ClssPbocApi.Clss_DelAllCAPK_Pboc();
            addCapkRevList();

            DDAFlag flag = new DDAFlag();
            int ret = ClssPbocApi.Clss_CardAuth_Pboc(acType, flag);
            LogUtils.i(TAG, "clssPbocCardAuth ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                if (ret == RetCode.CLSS_USE_CONTACT) {
                    result.setTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
                }
                throw new EmvException(ret);
            } else if (flag.flag == DDAFlag.FAIL) {
                result.setTransResult(ETransResult.ABORT_TERMINATED);
                return RetCode.CLSS_FAILED;
            }
        }
        return RetCode.EMV_OK;
    }

    private int processVSDC(ACType acType, CTransResult result) throws EmvException {
        if ((acType.type == com.pax.jemv.clcommon.ACType.AC_TC)
                && transParam.ucTransType != 0x20) { //no refund
            //according to EDC
            ClssPbocApi.Clss_DelAllRevocList_Pboc();
            ClssPbocApi.Clss_DelAllCAPK_Pboc();
            addCapkRevList();

            DDAFlag flag = new DDAFlag();
            int ret = ClssPbocApi.Clss_CardAuth_Pboc(acType, flag);
            LogUtils.i(TAG, "clssPbocCardAuth ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                result.setTransResult(ETransResult.OFFLINE_DENIED);
                return ret;
            }
        }
        return RetCode.EMV_OK;
    }

    private boolean continueUpdateResult(ACType acType, CTransResult result) throws EmvException {
        if (acType.type == com.pax.jemv.clcommon.ACType.AC_AAC) {
            result.setTransResult(ETransResult.CLSS_OC_DECLINED);
            return false;
        }

        if (transactionPath.path == TransactionPath.CLSS_VISA_QVSDC) {
            if (processQVSDC(acType, result) != RetCode.EMV_OK) {
                return false;
            }
        } else if (transactionPath.path == TransactionPath.CLSS_VISA_VSDC) {
            if (processVSDC(acType, result) != RetCode.EMV_OK) {
                return false;
            }
        } else {
            return false;
        }

        if (acType.type == ACType.AC_TC) {
            result.setTransResult(ETransResult.CLSS_OC_APPROVED);
        } else if (acType.type == ACType.AC_ARQC) {
            result.setTransResult(ETransResult.CLSS_OC_ONLINE_REQUEST);
        }
        return true;
    }

    @Override
    protected int setTlv(int tag, byte[] value) {
        return ClssPbocApi.Clss_SetTLVData_Pboc((short) tag, value, value.length);
    }

    @Override
    protected int getTlv(int tag, ByteArray value) {
        return ClssPbocApi.Clss_GetTLVData_Pboc((short) tag, value);
    }

    @Override
    protected void onAddCapkRevList(EMV_CAPK emvCapk, EMV_REVOCLIST emvRevoclist) {
        ClssPbocApi.Clss_AddCAPK_Pboc(emvCapk);
        ClssPbocApi.Clss_AddRevocList_Pboc(emvRevoclist);
        LogUtils.i(TAG, "ClssProc set PBOC capk and revoclist");
    }

    @Override
    String getTrack1() {
        ByteArray pbocGetTrack1List = new ByteArray();
        ClssPbocApi.Clss_nGetTrack1MapData_Pboc(pbocGetTrack1List);
        return Tools.bcd2Str(pbocGetTrack1List.data, pbocGetTrack1List.length);
    }

    @Override
    String getTrack2() {
        if (track2 == null) {
            ByteArray pbocGetTrack2List = new ByteArray();
            getTlv(TagsTable.TRACK2, pbocGetTrack2List);
            track2 = getTrack2FromTag57(Tools.bcd2Str(pbocGetTrack2List.data, pbocGetTrack2List.length));
        }
        return track2;
    }

    @Override
    String getTrack3() {
        return "";
    }

    @Override
    protected CTransResult completeTrans(ETransResult result, byte[] tag91, byte[] tag71, byte[] tag72) throws EmvException {
        return new CTransResult(ETransResult.CLSS_OC_APPROVED);
    }

    private static boolean isDebit(String clssAid) {
        return DEBIT1.equals(clssAid) || DEBIT2.equals(clssAid);
    }

    private static boolean isCredit(String clssAid) {
        return CREDIT1.equals(clssAid) || QCREDIT.equals(clssAid);
    }

    private static boolean isTpn(String clssAid) {
        if(clssAid != null && clssAid.contains(AppConstants.TPN_CARD_RID)) {
            return true;
        }
        return false;
    }

    private boolean isAmountExceed() {
        LogUtils.i(TAG, "AmountExceed " + transParam.ulAmntAuth + " " + clssPreProcInfo.ulRdCVMLmt);
        return transParam.ulAmntAuth > clssPreProcInfo.ulRdCVMLmt;
    }

    private boolean isNeedPin(String clssAid, CvmType cvmType) {
        if (isCredit(clssAid)) {
            LogUtils.i(TAG, "Credit card");
            if (cvmType.type == CvmType.RD_CVM_ONLINE_PIN) {
                return isAmountExceed();
            }
        } else if (isDebit(clssAid)) {
            LogUtils.i(TAG, "Debit card");
            if (cvmType.type == CvmType.RD_CVM_ONLINE_PIN) {
                return true;
            }
        } else if(isTpn(clssAid)) {
            LogUtils.i(TAG, "TPN card");
            if (cvmType.type == CvmType.RD_CVM_ONLINE_PIN) {
                return isAmountExceed();
            }
        }
        return false;
    }

    private boolean isNeedSig(String clssAid, CvmType cvmType) {
        if (isCredit(clssAid)) {
            LogUtils.i(TAG, "Credit card");
            if (cvmType.type == CvmType.RD_CVM_SIG) {
                return isAmountExceed();
            }
        } else if (isDebit(clssAid)) {
            LogUtils.i(TAG, "Debit card");
            if (cvmType.type == CvmType.RD_CVM_SIG) {
                return true;
            }
        } else if (isTpn(clssAid)) {
            LogUtils.i(TAG, "TPN card");
            if (cvmType.type == CvmType.RD_CVM_SIG) {
                return isAmountExceed();
            }
        }
        return false;
    }

}
