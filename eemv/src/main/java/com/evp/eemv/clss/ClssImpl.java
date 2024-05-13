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
package com.evp.eemv.clss;

import com.evp.bizlib.tpn.TpnUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.EmvBase;
import com.evp.eemv.IClss;
import com.evp.eemv.IClssListener;
import com.evp.eemv.entity.AidParam;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.ClssInputParam;
import com.evp.eemv.entity.ClssTornLogRecord;
import com.evp.eemv.entity.InputParam;
import com.evp.eemv.enums.ECvmResult;
import com.evp.eemv.enums.EFlowType;
import com.evp.eemv.enums.EKernelType;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Converter;
import com.evp.eemv.utils.Tools;
import com.pax.dal.exceptions.PedDevException;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.Clss_PreProcInfo;
import com.pax.jemv.clcommon.Clss_PreProcInterInfo;
import com.pax.jemv.clcommon.Clss_TransParam;
import com.pax.jemv.clcommon.KernType;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.clcommon.TransactionPath;
import com.pax.jemv.entrypoint.api.ClssEntryApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClssImpl extends EmvBase implements IClss {

    private static final String TAG = "ClssImpl";

    private KernType kernType;
    private List<Clss_PreProcInfo> preProcInfos = new ArrayList<>();
    private Clss_TransParam transParam;
    private ClssInputParam inputParam;
    private List<ClssTornLogRecord> tornLogRecords;

    private IClssListener listener;
    private ClssProc clssProc = null;

    static {
//        System.loadLibrary("F_PUBLIC_LIB_PayDroid");//EmvBase中已加载
        System.loadLibrary("F_ENTRY_LIB_PayDroid");
        System.loadLibrary("JNI_ENTRY_v103");
    }

    public ClssImpl() {
        kernType = new KernType();
    }

    @Override
    public IClss getClss() {
        return this;
    }

    @Override
    public EKernelType getKernelType() {
        switch (kernType.kernType) {
            case KernType.KERNTYPE_VIS:
                return EKernelType.VIS;
            case KernType.KERNTYPE_MC:
                return EKernelType.MC;
            case KernType.KERNTYPE_AE:
                return EKernelType.AE;
            case KernType.KERNTYPE_JCB:
                return EKernelType.JCB;
            case KernType.KERNTYPE_ZIP:
                return EKernelType.ZIP;
            case KernType.KERNTYPE_EFT:
                return EKernelType.EFT;
            case KernType.KERNTYPE_FLASH:
                return EKernelType.FLASH;
            case KernType.KERNTYPE_PBOC:
                return EKernelType.PBOC;
            case KernType.KERNTYPE_RUPAY:
                return EKernelType.KERNTYPE_RUPAY;
            case KernType.KERNTYPE_DEF:
            default:
                return EKernelType.DEF;
        }
    }

    @Override
    public void init() throws EmvException {
        super.init();
        if (ClssEntryApi.Clss_CoreInit_Entry() != RetCode.EMV_OK) {
            throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
        }
    }

    private void addApp() {
        preProcInfos.clear();
        for (AidParam i : aidParamList) {
            if(i.isEnableClss()) {
                byte kernelType = (byte) KernType.KERNTYPE_DEF;
                if (TpnUtils.isThisTpnOrTscAid(i.getAid())) {
                    kernelType = (byte) KernType.KERNTYPE_PBOC;
                }
                ClssEntryApi.Clss_AddAidList_Entry(i.getAid(), (byte) i.getAid().length,
                        i.getSelFlag(), kernelType);
                Clss_PreProcInfo clssPreProcInfo = Converter.genClssPreProcInfo(i, inputParam);
                preProcInfos.add(clssPreProcInfo);
                ClssEntryApi.Clss_SetPreProcInfo_Entry(clssPreProcInfo);
            }
        }
    }

    @Override
    public void preTransaction(ClssInputParam inputParam) throws EmvException {
        this.inputParam = inputParam;
        addApp();

        long ulAmtAuth = Long.parseLong(inputParam.getAmount());
        LogUtils.i("amount", "amount = " + ulAmtAuth);
        String date = inputParam.getTransDate();
        String time = inputParam.getTransTime();
        transParam = new Clss_TransParam(ulAmtAuth, 0,
                Integer.parseInt(inputParam.getTransTraceNo()),
                inputParam.getTag9CValue(), Tools.str2Bcd(date.substring(2)), Tools.str2Bcd(time));
        //This function must be called before Clss_PreTransProc_Entry and only if entry library is used for PayPass application
        int ret = ClssEntryApi.Clss_SetMCVersion_Entry((byte) 0x03);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }
        ret = ClssEntryApi.Clss_PreTransProc_Entry(transParam);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }
    }

    @Override
    public CTransResult process(InputParam inputParam) throws EmvException {
        int ret = ClssEntryApi.Clss_AppSlt_Entry(0, 0);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }
        LogUtils.i("CTLS_SPEED_TEST", "Before CTLS startTransaction");
        CTransResult result = startTransaction();
        LogUtils.i("CTLS_SPEED_TEST", "After CTLS startTransaction");
        if (result.getTransResult() == ETransResult.CLSS_OC_TRY_AGAIN) {
            if (null != listener) {
                listener.onPromptRetry();
            }
            return result;
        }
        if (result.getTransResult() == ETransResult.CLSS_OC_SEE_PHONE
                || result.getTransResult() == ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE) {
            return result;
        }

        updateCardInfo();


        /**
         * Sale and PreAuth are {@value EFlowType.COMPLETE} EMV flow,
         * other transactions are {@value EFlowType.SIMPLE} EMV flow
         */
        if (inputParam.getFlowType() == EFlowType.SIMPLE) {
            return new CTransResult(ETransResult.SIMPLE_FLOW_END);
        }


        if (result.getTransResult() != ETransResult.CLSS_OC_ONLINE_REQUEST) {
            return result;
        }

        cvmResult(result.getCvmResult());

        ETransResult transResult;
        try {
            listener.onDcc();

            transResult = onlineProc(result);
        } catch (PedDevException e) {
            throw new EmvException(e.getErrModule(), e.getErrCode(), e.getErrMsg());
        }
        if (ETransResult.ONLINE_DENIED == transResult) {
            throw new EmvException(EEmvExceptions.EMV_ERR_DENIAL);
        } else if(ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE == transResult) {
            throw new EmvException(EEmvExceptions.EMV_ERR_CLSS_USE_CONTACT);
        } else if (ETransResult.ONLINE_APPROVED != transResult) {
            throw new EmvException(EEmvExceptions.EMV_ERR_ONLINE_TRANS_ABORT);
        }

        CTransResult cTransResult = completeTransaction(transResult);
        result.setTransResult(cTransResult.getTransResult());
        return result;
    }

    private void updateCardInfo() throws EmvException {
        if (clssProc != null) {
            clssProc.updateCardInfo();
            return;
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
    }

    private CTransResult startTransaction() throws EmvException {
        while (true) {
            ByteArray daArray = new ByteArray();
            if (continueSelectKernel(daArray)) {
                continue;
            }

            Clss_PreProcInterInfo clssPreProcInterInfo = new Clss_PreProcInterInfo();
            int ret = ClssEntryApi.Clss_GetPreProcInterFlg_Entry(clssPreProcInterInfo);
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }

            ByteArray finalSelectData = new ByteArray();
            ret = ClssEntryApi.Clss_GetFinalSelectData_Entry(finalSelectData);
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }

            AidParam aid = selectApp(daArray);
            if (aid == null) {
                throw new EmvException(EEmvExceptions.EMV_ERR_NO_APP);
            }

            if(TpnUtils.isThisTpnOrTscAid(aid.getAid())) {
                LogUtils.i(TAG, "TPN or TSC application for CTLS selected.");
            }

            cfg.setAcquirerId(aid.getAcquirerId());
            try {
                clssProc = ClssProc.generate(kernType.kernType, listener)
                        .setAid(aid)
                        .setCapkList(capkList)
                        .setFinalSelectData(finalSelectData.data, finalSelectData.length)
                        .setTransParam(transParam)
                        .setConfig(cfg)
                        .setInputParam(inputParam)
                        .setPreProcInfo(preProcInfos.toArray(new Clss_PreProcInfo[0]))
                        .setPreProcInterInfo(clssPreProcInterInfo)
                        .setTornLogRecord(tornLogRecords);
                return clssProc.processTrans();
            } catch (EmvException e) {
                LogUtils.w(TAG, "", e);
                if (e.getErrCode() != RetCode.CLSS_RESELECT_APP) {
                    throw e;
                }
            } catch (IllegalArgumentException e) {
                LogUtils.e(TAG, "", e);
                throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
            }

            //to re-select app
            ret = ClssEntryApi.Clss_DelCurCandApp_Entry();
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
        }
    }

    private boolean continueSelectKernel(ByteArray daArray) throws EmvException {
        kernType = new KernType();

        int ret = ClssEntryApi.Clss_FinalSelect_Entry(kernType, daArray);// output parameter?
        LogUtils.i("clssEntryFinalSelect", "ret = " + ret + ", Kernel Type = " + kernType.kernType);
        if (ret == RetCode.EMV_RSP_ERR || ret == RetCode.EMV_APP_BLOCK
                || ret == RetCode.ICC_BLOCK || ret == RetCode.CLSS_RESELECT_APP) {
            ret = ClssEntryApi.Clss_DelCurCandApp_Entry();
            if (ret != RetCode.EMV_OK) {
                // 候选列表为空，进行相应错误处理，退出
                throw new EmvException(ret);
            }
            return true;
        } else if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }
        return false;
    }

    private AidParam selectApp(ByteArray daArray) {
        AidParam aid = null;
        String da = Tools.bcd2Str(daArray.data, daArray.length);
        for (AidParam i : aidParamList) {
            if (da.contains(Tools.bcd2Str(i.getAid()))) {
                aid = i;
                break;
            }
        }

        return aid;
    }

    private CTransResult completeTransaction(ETransResult transResult) throws EmvException {
        byte[] value91 = getTlv(0x91);
        byte[] value71 = getTlv(0x71);
        byte[] value72 = getTlv(0x72);
        //not ask for 2nd tap if no script returned from Issuer
        if (!need2ndTap() || (value91 == null && value71 == null && value72 == null)) {
            return new CTransResult(ETransResult.ONLINE_APPROVED);
        }
        if (detect2ndTap()) {
            if (clssProc != null) {
                return clssProc.completeTrans(transResult, value91, value71, value72);
            }
            throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
        }
        return new CTransResult(ETransResult.ABORT_TERMINATED);
    }

    private boolean detect2ndTap() throws EmvException {
        if (listener != null) {
            return listener.onDetect2ndTap();
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
    }

    @Override
    public byte[] getTlvSub(int tag) {
        if (clssProc != null) {
            ByteArray value = new ByteArray();
            if (clssProc.getTlv(tag, value) == RetCode.EMV_OK) {
                return Arrays.copyOf(value.data, value.length);
            }
        }
        return null;
    }

    @Override
    public void setTlvSub(int tag, byte[] value) throws EmvException {
        if (clssProc != null) {
            clssProc.setTlv(tag, value);
            return;
        }
        throw new EmvException(EEmvExceptions.EMV_ERR_NO_KERNEL);
    }

    @Override
    public void setTornLogRecords(List<ClssTornLogRecord> tornLogRecords) {
        this.tornLogRecords = tornLogRecords;
    }

    @Override
    public List<ClssTornLogRecord> getTornLogRecords() {
        return tornLogRecords;
    }

    @Override
    public void setListener(IClssListener listener) {
        this.listener = listener;
    }

    @Override
    public String getVersion() {
        return "1.00.00";
    }

    private boolean need2ndTap() {
        if (listener != null) {
            if (listener.onCheckDemoMode()) {
                return false;
            }
        }
        if (!clssProc.supportIssuerScript()) {
            return false;
        }

        return (getKernelType() != EKernelType.MC
                && getTransPath() != TransactionPath.CLSS_VISA_MSD
                && getKernelType() != EKernelType.PBOC
                && getTransPath() != TransactionPath.CLSS_DPAS_MAG
                && getTransPath() != TransactionPath.CLSS_DPAS_ZIP
                && getTransPath() != TransactionPath.CLSS_JCB_MAG
                && getTransPath() != TransactionPath.CLSS_JCB_LEGACY);
    }

    private int getTransPath() {
        if (clssProc != null) {
            return clssProc.getTransPath();
        }
        return TransactionPath.CLSS_PATH_NORMAL;
    }

    private void cvmResult(ECvmResult result) throws EmvException {
        if (listener == null) {
            throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
        }
        int ret = listener.onCvmResult(result);
        if (ret != EEmvExceptions.EMV_OK.getErrCodeFromBasement() && ret != EEmvExceptions.EMV_ERR_NO_PASSWORD.getErrCodeFromBasement()) {
            throw new EmvException(ret);
        }
    }

    private ETransResult onlineProc(CTransResult result) throws EmvException, PedDevException {
        if (listener == null) {
            throw new EmvException(EEmvExceptions.EMV_ERR_LISTENER_IS_NULL);
        }
        EOnlineResult ret = listener.onOnlineProc(result);
        if (ret == EOnlineResult.APPROVE) {
            return ETransResult.ONLINE_APPROVED;
        } else if (ret == EOnlineResult.ABORT) {
            return ETransResult.ABORT_TERMINATED;
        } else if (ret == EOnlineResult.TRY_OTHER_INTERFACE) {
            return ETransResult.CLSS_OC_TRY_ANOTHER_INTERFACE;
        } else {
            return ETransResult.ONLINE_DENIED;
        }
    }
}
