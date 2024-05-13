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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.eemv.utils;

import androidx.annotation.NonNull;

import com.evp.bizlib.data.entity.EmvAid;
import com.evp.bizlib.data.entity.EmvCapk;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.tpn.TpnUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.entity.AidParam;
import com.evp.eemv.entity.CandList;
import com.evp.eemv.entity.Capk;
import com.evp.eemv.entity.ClssInputParam;
import com.evp.eemv.entity.ClssTornLogRecord;
import com.evp.eemv.entity.Config;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.EKernelType;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.pax.jemv.clcommon.CLSS_TORN_LOG_RECORD;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.Clss_ReaderParam;
import com.pax.jemv.clcommon.CvmType;
import com.pax.jemv.clcommon.EMV_APPLIST;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.OnlineResult;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Converter {

    private Converter() {

    }

    public static EMV_APPLIST toEMVApp(AidParam aidParam) {
        EMV_APPLIST appList = new EMV_APPLIST();
        appList.appName = aidParam.getAppName().getBytes();
        appList.aid = aidParam.getAid();
        appList.aidLen = (byte) appList.aid.length;
        appList.selFlag = aidParam.getSelFlag();
        appList.priority = aidParam.getPriority();
        appList.floorLimit = aidParam.getFloorLimit();
        appList.floorLimitCheck = (byte) aidParam.getFloorLimitCheckFlg();
        appList.threshold = aidParam.getThreshold();
        appList.targetPer = aidParam.getTargetPer();
        appList.maxTargetPer = aidParam.getMaxTargetPer();
        appList.randTransSel = Tools.boolean2Byte(aidParam.getRandTransSel());
        appList.velocityCheck = Tools.boolean2Byte(aidParam.getVelocityCheck());
        appList.tacDenial = aidParam.getTacDenial();
        appList.tacOnline = aidParam.getTacOnline();
        appList.tacDefault = aidParam.getTacDefault();
        appList.acquierId = aidParam.getAcquirerId();
        appList.dDOL = aidParam.getdDol();
        appList.tDOL = aidParam.gettDol();
        appList.version = aidParam.getVersion();
        appList.riskManData = aidParam.getRiskManData();
        return appList;
    }

    public static CandList toCandList(EMV_APPLIST emvAppList) {
        CandList candList = new CandList();
        candList.setAid(emvAppList.aid);
        candList.setAidLen(emvAppList.aidLen);
        candList.setPriority(emvAppList.priority);
        try {
            candList.setAppName(new String(emvAppList.appName, "GB2312"));
        } catch (UnsupportedEncodingException e) {
            LogUtils.e(e);
        }
        return candList;
    }

    public static EMV_CAPK toEMVCapk(Capk capk) {
        EMV_CAPK emvCapk = new EMV_CAPK();
        emvCapk.rID = capk.getRid();
        emvCapk.keyID = capk.getKeyID();
        emvCapk.hashInd = capk.getHashInd();
        emvCapk.arithInd = capk.getArithInd();
        emvCapk.modul = capk.getModul();
        emvCapk.modulLen = (short) capk.getModul().length;
        emvCapk.exponent = capk.getExponent();
        emvCapk.exponentLen = (byte) capk.getExponent().length;
        emvCapk.expDate = capk.getExpDate();
        emvCapk.checkSum = capk.getCheckSum();
        return emvCapk;
    }

    /**
     * 1st param of EMVApi#EMVCompleteTrans only accept 3 values:
     * ONLINE_APPROVE, ONLINE_DENIAL, and ONLINE_FAILED,
     * reference to the API doc of JNI_EMV_LIB_v102
     *
     * @param procResult
     * @return
     */
    public static int toOnlineResult(ETransResult procResult) {
        switch (procResult) {
            case ONLINE_APPROVED:
                return OnlineResult.ONLINE_APPROVE;
            case ONLINE_DENIED:
                return OnlineResult.ONLINE_DENIAL;
            // add this branch to avoid AET-146: EMVApi#EMVCompleteTrans returns -30, emv param error
            case ABORT_TERMINATED:
                return OnlineResult.ONLINE_FAILED;
            default:
                return -1;
        }
    }

    public static CLSS_TORN_LOG_RECORD toClssTornLogRecord(ClssTornLogRecord clssTornLogRecord) {
        String pan = clssTornLogRecord.getPan();
        return new CLSS_TORN_LOG_RECORD(Tools.str2Bcd(pan),
                (byte) pan.length(),
                Tools.boolean2Byte(clssTornLogRecord.getPanSeqFlg()),
                clssTornLogRecord.getPanSeq(),
                clssTornLogRecord.getTornData(),
                clssTornLogRecord.getTornDataLen());
    }

    public static Clss_ReaderParam toClssReaderParam(Config cfg, AidParam aid) {
        Clss_ReaderParam readerParam = new Clss_ReaderParam();

        readerParam.ulReferCurrCon = cfg.getReferCurrCon();
        readerParam.aucMchNameLoc = cfg.getMerchName().getBytes();
        readerParam.usMchLocLen = (short) cfg.getMerchName().length();
        readerParam.aucMerchCatCode = Tools.str2Bcd(cfg.getMerchCateCode());
        readerParam.aucMerchantID = Tools.str2Bcd(cfg.getMerchId());
        readerParam.acquierId = cfg.getAcquirerId();

        readerParam.aucTmID = Tools.str2Bcd(cfg.getTermId());
        readerParam.ucTmType = cfg.getTermType();
        readerParam.aucTmCap = aid.getTerminalCapabilities();
        readerParam.aucTmCapAd = Tools.str2Bcd(cfg.getExCapability());
        readerParam.aucTmCntrCode = Tools.str2Bcd(cfg.getCountryCode());
        readerParam.aucTmTransCur = Tools.str2Bcd(cfg.getTransCurrCode());
        readerParam.ucTmTransCurExp = cfg.getTransCurrExp();
        readerParam.aucTmRefCurCode = Tools.str2Bcd(cfg.getReferCurrCode());
        readerParam.ucTmRefCurExp = cfg.getReferCurrExp();
        return readerParam;
    }

    public static Clss_PreProcInfo genClssPreProcInfo(AidParam aid, ClssInputParam inputParam) {
        byte kernelType = EKernelType.DEF.getKernelType();
        if(TpnUtils.isThisTpnOrTscAid(aid.getAid())) {
            kernelType = EKernelType.PBOC.getKernelType();
        }
        return new Clss_PreProcInfo(aid.getFloorLimit(), aid.getRdClssTxnLmt(), aid.getRdCVMLmt(), aid.getRdClssFLmt(),
                aid.getAid(), (byte) aid.getAid().length, kernelType,
                Tools.boolean2Byte(inputParam.isCrypto17Flg()),
                (byte) inputParam.getAmtZeroNoAllowedFlg(),
                Tools.boolean2Byte(inputParam.isStatusCheckFlg()),
                Tools.str2Bcd(inputParam.getReaderTTQ()),
                (byte) aid.getFloorLimitCheckFlg(),
                (byte) aid.getRdClssTxnLmtFlag(),
                (byte) aid.getRdCVMLmtFlag(),
                (byte) aid.getRdClssFLmtFlag(),
                new byte[2]);
    }

    public static byte[] toCvmTypes(List<ECvmResult> cvmResultList) {

        byte[] list = new byte[cvmResultList.size()];
        for (int i = 0; i < cvmResultList.size(); ++i) {
            list[i] = toCvmType(cvmResultList.get(i));
        }
        return list;
    }

    public static byte toCvmType(ECvmResult cvm) {
        switch (cvm) {
            case CONSUMER_DEVICE:
                return CvmType.RD_CVM_CONSUMER_DEVICE;
            case NO_CVM:
                return CvmType.RD_CVM_NO;
            case OFFLINE_PIN:
                return CvmType.RD_CVM_OFFLINE_PIN;
            case ONLINE_PIN:
                return CvmType.RD_CVM_ONLINE_PIN;
            case REQ_ONLINE_PIN:
                return CvmType.RD_CVM_REQ_ONLINE_PIN;
            case REQ_SIG:
                return CvmType.RD_CVM_REQ_SIG;
            case SIG:
                return CvmType.RD_CVM_SIG;
            default:
                return -1;
        }
    }

    public static ECvmResult convertCVM(byte result) throws EmvException {
        switch (result) {
            case CvmType.RD_CVM_NO:
                return ECvmResult.NO_CVM;
            case CvmType.RD_CVM_ONLINE_PIN:
                return ECvmResult.ONLINE_PIN;
            case CvmType.RD_CVM_SIG:
                return ECvmResult.SIG;
            case CvmType.RD_CVM_CONSUMER_DEVICE:
                return ECvmResult.CONSUMER_DEVICE;
            case CvmType.RD_CVM_OFFLINE_PIN:
                return ECvmResult.OFFLINE_PIN;
            case CvmType.RD_CVM_REQ_ONLINE_PIN:
                return ECvmResult.REQ_ONLINE_PIN;
            case CvmType.RD_CVM_REQ_SIG:
                return ECvmResult.REQ_SIG;
            default:
                throw new EmvException(EEmvExceptions.EMV_ERR_INVALID_PARA);
        }
    }

    /***************************
     * EmvAidParam to AidParam
     ***********************************/
    @NonNull
    public static List<AidParam> toAidParams() {
        List<AidParam> list = new LinkedList<>();

        List<EmvAid> aidList = GreendaoHelper.getEmvAidHelper().loadAll();
        if (aidList == null) {
            return new ArrayList<>(0);
        }
        for (EmvAid emvAidParam : aidList) {
            AidParam aidParam = new AidParam();
            aidParam.setAppName(emvAidParam.getAppName());
            aidParam.setAid(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getAid()));
            aidParam.setSelFlag((byte) emvAidParam.getSelFlag());
            aidParam.setPriority((byte) emvAidParam.getPriority());
            aidParam.setRdCVMLmt(emvAidParam.getRdCVMLmt());
            aidParam.setRdClssTxnLmt(emvAidParam.getRdClssTxnLmt());
            aidParam.setRdClssFLmt(emvAidParam.getRdClssFLmt());
            aidParam.setRdClssFLmtFlag(emvAidParam.getRdClssFLmtFlg());
            aidParam.setRdClssTxnLmtFlag(emvAidParam.getRdClssTxnLmtFlg());
            aidParam.setRdCVMLmtFlag(emvAidParam.getRdCVMLmtFlg());
            aidParam.setFloorLimit(emvAidParam.getFloorLimit());
            aidParam.setFloorLimitCheckFlg(emvAidParam.getFloorLimitCheckFlg());
            aidParam.setThreshold(emvAidParam.getThreshold());
            aidParam.setTargetPer((byte) emvAidParam.getTargetPer());
            aidParam.setMaxTargetPer((byte) emvAidParam.getMaxTargetPer());
            aidParam.setRandTransSel(emvAidParam.getRandTransSel());
            aidParam.setVelocityCheck(emvAidParam.getVelocityCheck());
            aidParam.setTacDenial(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getTacDenial()));
            aidParam.setTacOnline(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getTacOnline()));
            aidParam.setTacDefault(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getTacDefault()));
            if (emvAidParam.getAcquirerId() != null) {
                aidParam.setAcquirerId(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getAcquirerId()));
            }
            if (emvAidParam.getDDOL() != null) {
                aidParam.setdDol(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getDDOL()));
            }
            if (emvAidParam.getTDOL() != null) {
                aidParam.settDol(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getTDOL()));
            }
            aidParam.setVersion(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getVersion()));
            if (emvAidParam.getRiskManageData() != null) {
                aidParam.setRiskManData(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getRiskManageData()));
            }
            if(emvAidParam.getJcbClssTermIntProfile() != null) {
                aidParam.setJcbClssTermIntProfile(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getJcbClssTermIntProfile()));
            }
            aidParam.setJcbClssTermCompatIndicator((byte)emvAidParam.getJcbClssTermCompatIndicator());
            if(emvAidParam.getJcbClssCombinationOpt() != null) {
                aidParam.setJcbClssCombinationOpt(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getJcbClssCombinationOpt()));
            }
            aidParam.setTerminalCapabilities(ConvertHelper.getConvert().strToBcdPaddingLeft(emvAidParam.getTerminalCapabilities()));
            aidParam.setEnableClss(emvAidParam.getEnableClss());
            list.add(aidParam);
        }
        return list;
    }

    /********************************
     * EmvCapk to Capk
     *******************************/
    @NonNull
    public static List<Capk> toCapk() {
        List<Capk> list = new LinkedList<>();

        List<EmvCapk> capkList = GreendaoHelper.getEmvCapkHelper().loadAll();
        if (capkList == null) {
            return new ArrayList<>(0);
        }
        for (EmvCapk readCapk : capkList) {
            if (readCapk.getModule() == null || readCapk.getExponent() == null) {
                continue;
            }
            Capk capk = new Capk();
            capk.setRid(ConvertHelper.getConvert().strToBcdPaddingLeft(readCapk.getRID()));
            capk.setKeyID((byte) readCapk.getKeyID());
            capk.setHashInd((byte) readCapk.getHashInd());
            capk.setArithInd((byte) readCapk.getArithInd());
            capk.setModul(ConvertHelper.getConvert().strToBcdPaddingLeft(readCapk.getModule()));
            capk.setExponent(ConvertHelper.getConvert().strToBcdPaddingLeft(readCapk.getExponent()));
            capk.setExpDate(ConvertHelper.getConvert().strToBcdPaddingLeft(readCapk.getExpDate()));
            capk.setCheckSum(ConvertHelper.getConvert().strToBcdPaddingLeft(readCapk.getCheckSum()));
            list.add(capk);
        }
        return list;
    }
}
