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

import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.EmvImpl;
import com.evp.eemv.IClssListener;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Converter;
import com.evp.eemv.utils.Tools;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.Clss_ProgramID;
import com.pax.jemv.clcommon.Clss_VisaAidParam;
import com.pax.jemv.clcommon.DDAFlag;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.EMV_REVOCLIST;
import com.pax.jemv.clcommon.KernType;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.entrypoint.api.ClssEntryApi;
import com.pax.jemv.paywave.api.ClssWaveApi;

import java.util.Arrays;

class ClssProcVis extends ClssProc {

    private Clss_PreProcInfo clssPreProcInfo;
    private String track2;

    static {
        System.loadLibrary("F_WAVE_LIB_PayDroid");
        System.loadLibrary("JNI_WAVE_v100");
    }

    ClssProcVis(IClssListener listener) {
        super(listener);
    }

    private int init() {
        int ret = ClssWaveApi.Clss_CoreInit_Wave();
        LogUtils.i("clssWaveCoreInit", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        ret = ClssWaveApi.Clss_SetReaderParam_Wave(Converter.toClssReaderParam(cfg, aid));
        LogUtils.i("Clss_SetReaderParam_Wave", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        return ClssWaveApi.Clss_SetFinalSelectData_Wave(finalSelectData, finalSelectDataLen);
    }

    private int setTransParam() {
        byte[] cvmTypes = Converter.toCvmTypes(inputParam.getCvmReq());
        int ret = ClssWaveApi.Clss_SetVisaAidParam_Wave(
                new Clss_VisaAidParam(aid.getRdClssFLmt(), Tools.boolean2Byte(inputParam.isDomesticOnly()),
                        (byte) cvmTypes.length, cvmTypes, inputParam.getEnDDAVerNo()));
        LogUtils.i("clssWaveSetVisaAidParam", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        setTlv(0x9F5A, Tools.str2Bcd("123"));

        ByteArray proID = new ByteArray();
        ret = getTlv(TagsTable.PRO_ID, proID);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }

        for (Clss_PreProcInfo i : arrayPreProcInfo) {
            if (Arrays.equals(aid.getAid(), i.aucAID)) {
                clssPreProcInfo = i;
                break;
            }
        }

        if (clssPreProcInfo != null) {
            Clss_ProgramID clssProgramID = new Clss_ProgramID(clssPreProcInfo.ulRdClssTxnLmt, clssPreProcInfo.ulRdCVMLmt,
                    clssPreProcInfo.ulRdClssFLmt, clssPreProcInfo.ulTermFLmt, proID.data, (byte) proID.length,
                    clssPreProcInfo.ucRdClssFLmtFlg, clssPreProcInfo.ucRdClssTxnLmtFlg, clssPreProcInfo.ucRdCVMLmtFlg,
                    clssPreProcInfo.ucTermFLmtFlg, clssPreProcInfo.ucStatusCheckFlg, (byte) 0, new byte[4]);
            ret = ClssWaveApi.Clss_SetDRLParam_Wave(clssProgramID);
            LogUtils.i("clssWaveSetDRLParam", "ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                return ret;
            }
        }

        setTlv(0x9C, new byte[]{transParam.ucTransType});
        ret = ClssWaveApi.Clss_SetTransData_Wave(transParam, preProcInterInfo);
        LogUtils.i("clssWaveSetTransData", "ret = " + ret);
        return ret;
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

        ACType acType = new ACType();
        ret = ClssWaveApi.Clss_Proctrans_Wave(transactionPath, acType);
        LogUtils.i("CTLS_SPEED_TEST", "in ClssProcVis#CTransResult，after ClssWaveApi.Clss_Proctrans_Wave()");

        if (ret != RetCode.EMV_OK) {

            if (ret == RetCode.CLSS_RESELECT_APP) {
                throw new EmvException(ret);
            }

            //此处是On Device CVM 的处理，需要提示see phone
            else if (ret == RetCode.CLSS_REFER_CONSUMER_DEVICE) {
                if (listener != null) {
                    listener.onPromptRemoveCard();
                    ret = listener.onDisplaySeePhone();
                    if (ret != 0) {
                        return new CTransResult(ETransResult.ABORT_TERMINATED);
                    }
                    return new CTransResult(ETransResult.CLSS_OC_SEE_PHONE);
                }
            } else if (ret == RetCode.CLSS_USE_CONTACT) {
                //CLSS_OC_TRY_ANOTHER_INTERFACE is equal to CLSS_USE_CONTACT
                return new CTransResult(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE);
            }

            throw new EmvException(ret);
        }
        LogUtils.i("clssWaveProcTrans", "TransPath = " + transactionPath.path + ", ACType = " + acType.type);

        if (listener != null) {
            listener.onPromptRemoveCard();
        }

        CTransResult result = new CTransResult(ETransResult.ABORT_TERMINATED);
        if (!continueUpdateResult(acType, result)) {
            return result;
        }

        updateResult(result);
        return result;
    }

    private void updateResult(CTransResult result) throws EmvException {
        byte cvmType = ClssWaveApi.Clss_GetCvmType_Wave();
        LogUtils.i("clssWaveGetCvmType", "CVMType = " + cvmType);
        if (cvmType < 0) {
            if (cvmType == RetCode.CLSS_DECLINE) {
                result.setTransResult(ETransResult.CLSS_OC_DECLINED);
            }
            throw new EmvException(cvmType);
        }
        result.setCvmResult(Converter.convertCVM(cvmType));
    }

    private int processMSD() throws EmvException {
        byte msdType = ClssWaveApi.Clss_GetMSDType_Wave();
        LogUtils.i("clssWaveGetMSDType", "msdType = " + msdType);
        //get MSD track 2 data
        ByteArray waveGetTrack2List = new ByteArray();
        int ret = ClssWaveApi.Clss_nGetTrack2MapData_Wave(waveGetTrack2List);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        track2 = getTrack2FromTag57(Tools.bcd2Str(waveGetTrack2List.data, waveGetTrack2List.length));
        return RetCode.EMV_OK;
    }

    private int processQVSDC(ACType acType, CTransResult result) throws EmvException {
        int ret = ClssWaveApi.Clss_ProcRestric_Wave();
        LogUtils.i("clssWaveProcRestric", "ret = " + ret);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        if ((acType.type == com.pax.jemv.clcommon.ACType.AC_TC)
                && transParam.ucTransType != 0x20) { //no refund

            // TODO: Exception file check

            //according to EDC
            ClssWaveApi.Clss_DelAllRevocList_Wave();
            ClssWaveApi.Clss_DelAllCAPK_Wave();
            addCapkRevList();

            DDAFlag flag = new DDAFlag();
            ret = ClssWaveApi.Clss_CardAuth_Wave(acType, flag);
            LogUtils.i("clssWaveCardAuth", "ret = " + ret);
            return getCardAuthResult(ret, acType, flag, result);
        }
        return RetCode.EMV_OK;
    }

    private int processWAVE2(ACType acType, CTransResult result) throws EmvException {
        // TODO: Exception file check
        result.setTransResult(ETransResult.CLSS_OC_APPROVED);
        //according to EDC
        ClssWaveApi.Clss_DelAllRevocList_Wave();
        ClssWaveApi.Clss_DelAllCAPK_Wave();
        addCapkRevList();

        DDAFlag flag = new DDAFlag();
        int ret = ClssWaveApi.Clss_CardAuth_Wave(acType, flag);
        LogUtils.i("clssWaveCardAuth", "ret = " + ret);
        return getCardAuthResult(ret, acType, flag, result);
    }

    private boolean continueUpdateResult(ACType acType, CTransResult result) throws EmvException {
        if (acType.type == com.pax.jemv.clcommon.ACType.AC_AAC) {
            result.setTransResult(ETransResult.CLSS_OC_DECLINED);
            return false;
        }

        if (transactionPath.path == TransactionPath.CLSS_VISA_MSD
                || transactionPath.path == TransactionPath.CLSS_VISA_MSD_CVN17
                || transactionPath.path == TransactionPath.CLSS_VISA_MSD_LEGACY) {

            if (transactionPath.path == TransactionPath.CLSS_VISA_MSD) {
                processMSD();
            }
        } else if (transactionPath.path == TransactionPath.CLSS_VISA_QVSDC) {
            if (processQVSDC(acType, result) != RetCode.EMV_OK) {
                return false;
            }
        } else if (transactionPath.path == TransactionPath.CLSS_VISA_WAVE2
                && acType.type == com.pax.jemv.clcommon.ACType.AC_TC) {
            if (processWAVE2(acType, result) != RetCode.EMV_OK) {
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
        return ClssWaveApi.Clss_SetTLVData_Wave((short) tag, value, value.length);
    }

    @Override
    protected int getTlv(int tag, ByteArray value) {
        return ClssWaveApi.Clss_GetTLVData_Wave((short) tag, value);
    }

    @Override
    protected void onAddCapkRevList(EMV_CAPK emvCapk, EMV_REVOCLIST emvRevoclist) {
        ClssWaveApi.Clss_AddCAPK_Wave(emvCapk);
        ClssWaveApi.Clss_AddRevocList_Wave(emvRevoclist);
        LogUtils.i("ClssProc", "set VISA capk and revoclist");
    }

    @Override
    String getTrack1() {
        ByteArray waveGetTrack1List = new ByteArray();
        ClssWaveApi.Clss_nGetTrack1MapData_Wave(waveGetTrack1List);
        return Tools.bcd2Str(waveGetTrack1List.data, waveGetTrack1List.length);
    }

    @Override
    String getTrack2() {
        if (track2 == null) {
            ByteArray waveGetTrack2List = new ByteArray();
            getTlv(TagsTable.TRACK2, waveGetTrack2List);
            track2 = getTrack2FromTag57(Tools.bcd2Str(waveGetTrack2List.data, waveGetTrack2List.length));
        }
        return track2;
    }

    @Override
    String getTrack3() {
        return "";
    }

    @Override
    protected CTransResult completeTrans(ETransResult result, byte[] tag91, byte[] tag71, byte[] tag72) throws EmvException {
//        issScrCon(tag91, tag71, tag72);

        byte[] authData = new byte[16];        // authentication data from issuer
        int authDataLen = 0;

        if (tag91 != null && tag91.length > 0) {
            authDataLen = Math.min(tag91.length, 16);
            System.arraycopy(tag91, 0, authData, 0, authDataLen);
            LogUtils.i("saveRspICCData", "aucAuthData = " + Arrays.toString(authData) + "iAuthDataLen = " + authDataLen);
        }

        byte[] issuScript = EmvImpl.combine7172(tag71, tag72);

        if (issuScript == null)
            issuScript = new byte[0];

        int ret = clssCompleteTrans(authData, authDataLen, issuScript, issuScript.length);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }
        return new CTransResult(ETransResult.CLSS_OC_APPROVED);
    }

    private int clssCompleteTrans(byte[] authData, int authDataLen, byte[] issuerScript, int scriptLen) {
        ByteArray aucCTQ = new ByteArray();
        KernType kernType = new KernType();
        ByteArray sltData = new ByteArray();

        if (authDataLen == 0 && scriptLen == 0) {
            return RetCode.EMV_NO_DATA;
        }

        int ret = getTlv(TagsTable.CTQ, aucCTQ);
        if (ret != RetCode.EMV_OK) {
            return ret;
        }
        //Terminal Transaction Qualifiers: Byte 3 bit 1 is 1, Issuer Update Processing supported
        //Card Terminal Qualifiers: Byte 1 bit 7 is 1, Card supports Issuer Update Processing at the POS
        if ((clssPreProcInfo.aucReaderTTQ[2] & 0x80) == 0x80 && (aucCTQ.data[1] & 0x40) == 0x40) {
            ret = ClssEntryApi.Clss_FinalSelect_Entry(kernType, sltData);
            LogUtils.i("Clss_FinalSelect_Entry", "ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                return ret;
            }

            ret = ClssWaveApi.Clss_IssuerAuth_Wave(authData, authDataLen);
            LogUtils.i("clssWaveIssuerAuth", "ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                return ret;
            }

            ret = ClssWaveApi.Clss_IssScriptProc_Wave(issuerScript, scriptLen);
            LogUtils.i("clssWaveIssScriptProc", "ret = " + ret);
            if (ret != RetCode.EMV_OK) {
                return ret;
            }
        }

        return RetCode.EMV_OK;
    }

    /**
     * @return true if both terminal and card support Issuer Update Processing, false otherwise
     */
    @Override
    protected boolean supportIssuerScript() {

        ByteArray aucCTQ = new ByteArray();
        ByteArray aucTTQ = new ByteArray();

        int ret = getTlv(TagsTable.CTQ, aucCTQ);
        if (ret != RetCode.EMV_OK) {
            return true;
        }

        ret = getTlv(TagsTable.TTQ, aucTTQ);
        if (ret != RetCode.EMV_OK) {
            return true;
        }

        //Terminal Transaction Qualifiers: Byte 3 bit 1 is 1, Issuer Update Processing supported
        //Card Terminal Qualifiers: Byte 1 bit 7 is 1, Card supports Issuer Update Processing at the POS
        if ((aucTTQ.data[2] & 0x80) == 0x80 && (aucCTQ.data[1] & 0x40) == 0x40) {
            return true;
        }

        return false;
    }
}
