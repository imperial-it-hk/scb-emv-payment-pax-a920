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

import com.evp.bizlib.params.ParamHelper;
import com.evp.bizlib.tpn.TpnUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.entity.AidParam;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.CandList;
import com.evp.eemv.entity.Capk;
import com.evp.eemv.entity.InputParam;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.EFlowType;
import com.evp.eemv.enums.EOnlineResult;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EEmvExceptions;
import com.evp.eemv.exception.EmvException;
import com.evp.eemv.utils.Converter;
import com.evp.eemv.utils.Tools;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.pax.jemv.clcommon.ACType;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.clcommon.EMV_APPLIST;
import com.pax.jemv.clcommon.EMV_CAPK;
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.device.DeviceManager;
import com.pax.jemv.emv.api.EMVApi;
import com.pax.jemv.emv.api.EMVCallback;
import com.pax.jemv.emv.model.EmvEXTMParam;
import com.pax.jemv.emv.model.EmvMCKParam;
import com.pax.jemv.emv.model.EmvParam;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class EmvImpl extends EmvBase {
    private static final String TAG = "EmvImpl";
    public static boolean isTimeOut = false;
    private EMVCallback emvCallback;
    private EmvTrans paxEmvTrans;
    private EmvParam emvParam;
    private EmvMCKParam mckParam;

    static {
//        System.loadLibrary("F_PUBLIC_LIB_PayDroid");//EmvBase中已加载
        System.loadLibrary("F_EMV_LIBC_PayDroid");
        System.loadLibrary("F_EMV_LIB_PayDroid");
        System.loadLibrary("JNI_EMV_v103");
    }

    private class Callback implements EMVCallback.EmvCallbackListener {
        private boolean isFirstCall = true;
        private int tmpRemainCount = 0;

        @Override
        public void emvWaitAppSel(int tryCnt, EMV_APPLIST[] list, int appNum) {
            //TPN and TSC logic for automatic application selection
            int sizeOfArray = Math.min(list.length, appNum);
            for (int i = 0; i < sizeOfArray; ++i) {
                if(TpnUtils.isThisTpnOrTscAid(list[i].aid, list[i].aidLen)) {
                    emvCallback.setCallBackResult(i);
                    return;
                }
            }
            //Standard EMV logic
            CandList[] candLists = new CandList[list.length];
            int size = Math.min(list.length, appNum);
            for (int i = 0; i < size; ++i) {
                candLists[i] = Converter.toCandList(list[i]);
            }
            try {
                int idx = paxEmvTrans.waitAppSelect(tryCnt, candLists);
                if (idx >= 0)
                    emvCallback.setCallBackResult(idx);
                else {
                    emvCallback.setCallBackResult(RetCode.EMV_USER_CANCEL);
                }
            } catch (EmvException e) {
                LogUtils.w(TAG, "", e);
                emvCallback.setCallBackResult(e.getErrCode());
            }
        }

        @Override
        public void emvInputAmount(long[] amt) {
            Amount amount = paxEmvTrans.getAmount();
            if (amount != null) {
                amt[0] = Long.parseLong(amount.getAmount());
                if (amt.length > 1) {
                    if (amount.getCashBackAmt() == null || amount.getCashBackAmt().isEmpty()) {
                        amt[1] = 0;
                    } else {
                        amt[1] = Long.parseLong(amount.getCashBackAmt());
                    }
                }
            }
            emvCallback.setCallBackResult(RetCode.EMV_OK);
        }

        @Override
        public void emvGetHolderPwd(int tryFlag, int remainCnt, byte[] pin) {
            //目前EMV内核中Verify命令中,响应中的SW2='Cx',x代表还可以重新验证的次数，十六进制表示，最大值为15,超过15次的情况，导致remainCnt会有问题
            if (this.isFirstCall) {
                this.tmpRemainCount = remainCnt;
                this.isFirstCall = false;
            }
            boolean isOnline = (null == pin);
            if (isOnline) {
                LogUtils.i("log", "emvGetHolderPwd pin is null, tryFlag" + tryFlag + " remainCnt:" + remainCnt);
            } else {
                LogUtils.i("log", "emvGetHolderPwd pin is not null, tryFlag" + tryFlag + " remainCnt:" + remainCnt);
            }

            int result;
            try {
                if (isOnline) {
                    result = paxEmvTrans.cardHolderPwd(true, remainCnt, pin);
                } else {
                    result = paxEmvTrans.cardHolderPwd(false, this.tmpRemainCount, pin);
                    this.tmpRemainCount--;
                }

            } catch (EmvException e) {
                LogUtils.e(TAG, e);
                result = e.getErrCode();
            }

            emvCallback.setCallBackResult(result);
        }

        @Override
        public void emvAdviceProc() {
            //do nothing
        }

        @Override
        public void emvVerifyPINOK() {
            //do nothing
        }

        @Override
        public int emvUnknowTLVData(short tag, ByteArray data) {
            LogUtils.i("EMV", "emvUnknowTLVData tag: " + Integer.toHexString(tag) + " data:" + data.data.length);
            switch (tag) {
                case 0x9A:
                    byte[] date = new byte[7];
                    DeviceManager.getInstance().getTime(date);
                    System.arraycopy(date, 1, data.data, 0, 3);
                    break;
                case (short) 0x9F1E:
                    byte[] sn = new byte[10];
                    DeviceManager.getInstance().readSN(sn);
                    System.arraycopy(sn, 0, data.data, 0, Math.min(data.data.length, sn.length));
                    break;
                case (short) 0x9F21:
                    byte[] time = new byte[7];
                    DeviceManager.getInstance().getTime(time);
                    System.arraycopy(time, 4, data.data, 0, 3);
                    break;
                case (short) 0x9F37:
                    byte[] random = new byte[4];
                    DeviceManager.getInstance().getRand(random, 4);
                    System.arraycopy(random, 0, data.data, 0, data.data.length);
                    break;
                case (short) 0xFF01:
                    Arrays.fill(data.data, (byte) 0x00);
                    break;
                default:
                    return RetCode.EMV_PARAM_ERR;
            }
            data.length = data.data.length;
            return RetCode.EMV_OK;
        }

        @Override
        public void certVerify() {
            emvCallback.setCallBackResult(RetCode.EMV_OK);
        }

        @Override
        public int emvSetParam() {
            return RetCode.EMV_OK;
        }

        @Override
        public int emvVerifyPINfailed(byte[] bytes) {
            return 0;
        }

        @Override
        public int cRFU2() {
            return 0;
        }
    }

    public EmvImpl() {
        super();
        emvParam = new EmvParam();
        mckParam = new EmvMCKParam();
        mckParam.extmParam = new EmvEXTMParam();

        paxEmvTrans = new EmvTrans();
    }

    @Override
    public void init() throws EmvException {
        super.init();
        emvCallback = EMVCallback.getInstance();
        emvCallback.setCallbackListener(new Callback());
        int ret = EMVCallback.EMVCoreInit();
        if (ret == RetCode.EMV_OK) {
            EMVCallback.EMVSetCallback();
            EMVCallback.EMVGetParameter(emvParam);
            EMVCallback.EMVGetMCKParam(mckParam);
            paramToConfig();
            return;
        }

        throw new EmvException(EEmvExceptions.EMV_ERR_FILE);
    }

    private void paramToConfig() {
        cfg.setCapability(Tools.bcd2Str(emvParam.capability));
        cfg.setCountryCode(Tools.bcd2Str(emvParam.countryCode));
        cfg.setExCapability(Tools.bcd2Str(emvParam.exCapability));
        cfg.setForceOnline(Tools.byte2Boolean(emvParam.forceOnline));
        cfg.setGetDataPIN(Tools.byte2Boolean(emvParam.getDataPIN));
        cfg.setMerchCateCode(Tools.bcd2Str(emvParam.merchCateCode));
        cfg.setReferCurrCode(Tools.bcd2Str(emvParam.referCurrCode));
        cfg.setReferCurrCon(emvParam.referCurrCon);
        cfg.setReferCurrExp(emvParam.referCurrExp);
        cfg.setSurportPSESel(Tools.byte2Boolean(emvParam.surportPSESel));
        cfg.setTermType(emvParam.terminalType);
        cfg.setTransCurrCode(Tools.bcd2Str(emvParam.transCurrCode));
        cfg.setTransCurrExp(emvParam.transCurrExp);
        cfg.setTransType(emvParam.transType);
        cfg.setTermId(Arrays.toString(emvParam.termId));
        cfg.setMerchId(Arrays.toString(emvParam.merchId));
        cfg.setMerchName(Arrays.toString(emvParam.merchName));

        cfg.setBypassPin(Tools.byte2Boolean(mckParam.ucBypassPin));
        cfg.setBatchCapture(mckParam.ucBatchCapture);

        cfg.setTermAIP(Tools.bcd2Str(mckParam.extmParam.aucTermAIP));
        cfg.setBypassAllFlag(Tools.byte2Boolean(mckParam.extmParam.ucBypassAllFlg));
        cfg.setUseTermAIPFlag(Tools.byte2Boolean(mckParam.extmParam.ucUseTermAIPFlg));
    }

    private void configToParam() {
        emvParam.capability = Tools.str2Bcd(cfg.getCapability()); //Dummy value which will be set after app. selection
        emvParam.countryCode = Tools.str2Bcd(cfg.getCountryCode());
        emvParam.exCapability = Tools.str2Bcd(cfg.getExCapability());
        emvParam.forceOnline = Tools.boolean2Byte(cfg.getForceOnline());
        emvParam.getDataPIN = Tools.boolean2Byte(cfg.getGetDataPIN());
        emvParam.merchCateCode = Tools.str2Bcd(cfg.getMerchCateCode());
        emvParam.referCurrCode = Tools.str2Bcd(cfg.getReferCurrCode());
        emvParam.referCurrCon = cfg.getReferCurrCon();
        emvParam.referCurrExp = cfg.getReferCurrExp();
        emvParam.surportPSESel = Tools.boolean2Byte(cfg.getSurportPSESel());
        emvParam.terminalType = cfg.getTermType();
        emvParam.transCurrCode = Tools.str2Bcd(cfg.getTransCurrCode());
        emvParam.transCurrExp = cfg.getTransCurrExp();
        emvParam.transType = cfg.getTransType();
        emvParam.termId = cfg.getTermId().getBytes();
        emvParam.merchId = cfg.getMerchId().getBytes();
        emvParam.merchName = cfg.getMerchName().getBytes();

        mckParam.ucBypassPin = Tools.boolean2Byte(cfg.getBypassPin());
        mckParam.ucBatchCapture = cfg.getBatchCapture();

        mckParam.extmParam.aucTermAIP = Tools.str2Bcd(cfg.getTermAIP());
        mckParam.extmParam.ucBypassAllFlg = Tools.boolean2Byte(cfg.getBypassAllFlag());
        mckParam.extmParam.ucUseTermAIPFlg = Tools.boolean2Byte(cfg.getUseTermAIPFlag());
    }

    @Override
    public byte[] getTlvSub(int tag) {
        ByteArray byteArray = new ByteArray();
        if (EMVCallback.EMVGetTLVData((short) tag, byteArray) == RetCode.EMV_OK) {
            return Arrays.copyOfRange(byteArray.data, 0, byteArray.length);
        }
        return null;
    }

    @Override
    public void setTlvSub(int tag, byte[] value) throws EmvException {
        int ret = EMVCallback.EMVSetTLVData((short) tag, value, value.length);
        if (ret != EEmvExceptions.EMV_OK.getErrCodeFromBasement()) {
            throw new EmvException(ret);
        }
    }

    // Run callback
    // Parameter settings, loading aid,
    // select the application, application initialization,read application data, offline data authentication,
    // terminal risk management,cardholder authentication, terminal behavior analysis,
    // issuing bank data authentication, execution script
    @Override
    public CTransResult process(InputParam inputParam) throws EmvException {
        configToParam();
        EMVCallback.EMVSetParameter(emvParam);
        int ret = EMVCallback.EMVSetMCKParam(mckParam);
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        if (ParamHelper.isInternalPed()) {
            //use PCI verify offline PIN interface
            EMVCallback.EMVSetPCIModeParam((byte) 1, "0,4,5,6,7,8,9,10,11,12\0".getBytes(), inputParam.getPciTimeout());
        } else {
            EMVCallback.EMVSetPCIModeParam((byte) 0, "0,4,5,6,7,8,9,10,11,12\0".getBytes(), inputParam.getPciTimeout());
        }

        for (AidParam i : aidParamList) {
            ret = EMVCallback.EMVAddApp(Converter.toEMVApp(i));
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
        }

        ret = EMVCallback.EMVAppSelect(0, Long.parseLong(inputParam.getTransTraceNo()));   //callback emvWaitAppSel
        LogUtils.i(TAG, "EMV Processing - EMVAppSelect return: " + Integer.toString(ret));
        if (ret != RetCode.EMV_OK) {
            if (ret == RetCode.EMV_DATA_ERR || ret == RetCode.ICC_RESET_ERR || ret == RetCode.EMV_NO_APP
                    || ret == RetCode.ICC_CMD_ERR || ret == RetCode.EMV_RSP_ERR || ret == RetCode.ICC_RSP_6985) {
                throw new EmvException(EEmvExceptions.EMV_ERR_FALL_BACK);
            }
            throw new EmvException(ret);
        }

        //Set correct terminal capabilities per AID configuration
        boolean isPinAllowed = paxEmvTrans.isPinAllowedForTransaction(); //Some transactions does not want PIN entry
        byte[] aid = getTlv(TagsTable.AID);
        final String aidStr = ConvertHelper.getConvert().bcdToStr(aid);
        LogUtils.i(TAG, "EMV Processing - AID: " + aidStr);
        AidParam aidParam = null;
        for (AidParam i : aidParamList) {
            String iAidStr = ConvertHelper.getConvert().bcdToStr(i.getAid());
            if(aidStr.contains(iAidStr)) {
                if(!isPinAllowed) {
                    byte[] capabilities = i.getTerminalCapabilities();
                    capabilities[1] = 0x08; //No CVM required
                    i.setTerminalCapabilities(capabilities);
                }
                aidParam = i;
                break;
            }
        }
        if(aidParam == null) {
            throw new EmvException(EEmvExceptions.EMV_ERR_NO_APP);
        }
        setTlv(TagsTable.TERMINAL_CAPABILITY, aidParam.getTerminalCapabilities());

        ret = EMVCallback.EMVReadAppData(); //callback emvInputAmount
        LogUtils.i(TAG, "EMV Processing - EMVReadAppData return: " + Integer.toString(ret));
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        ByteArray pan = new ByteArray();
        EMVCallback.EMVGetTLVData((byte) 0x5A, pan);
        String filtPan = Tools.bcd2Str(pan.data, pan.length);
        int indexF = filtPan.indexOf('F');

        byte[] panAgain = getTlv(0x5A);
        LogUtils.hex(TAG, "PAN: ", panAgain);

        LogUtils.i(TAG, "EMV Processing - Got PAN. Length of PAN is: " + Integer.toString(pan.length));

        if (pan.length > 0 && pan.data != null) {
            ret = paxEmvTrans.confirmCardNo(filtPan.substring(0, indexF != -1 ? indexF : filtPan.length()));
            LogUtils.i(TAG, "EMV Processing - confirmCardNo returns: " + Integer.toString(ret));
            if (ret != RetCode.EMV_OK) {
                throw new EmvException(ret);
            }
        }

        if (EmvImpl.isTimeOut) {
            LogUtils.e(TAG, "EMV Processing - TIMEOUT happens");
            return new CTransResult(ETransResult.ABORT_TERMINATED);
        }

        addCapkIntoEmvLib(); // ignore return value for some case which the card doesn't has the capk index

        ret = EMVCallback.EMVCardAuth();
        LogUtils.i(TAG, "EMV Processing - EMVCardAuth returns: " + Integer.toString(ret));
        if (ret != RetCode.EMV_OK) {
            if (ret == RetCode.ICC_RSP_6985) {
                throw new EmvException(EEmvExceptions.EMV_ERR_FALL_BACK);
            }
            throw new EmvException(ret);
        }

        int var0 = 0;
        byte[] var1 = new byte[2];
        ret = EMVApi.EMVGetDebugInfo(var0, var1);
        LogUtils.e("EMVGetDebugInfo", Integer.toString(ret));


        /**
         * Sale and PreAuth are {@value EFlowType.COMPLETE} EMV flow,
         * other transactions are {@value EFlowType.SIMPLE} EMV flow
         */
        if (inputParam.getFlowType() == EFlowType.SIMPLE) {
            return new CTransResult(ETransResult.SIMPLE_FLOW_END);
        }

        ret = paxEmvTrans.additionalProcess();
        if (ret != RetCode.EMV_OK) {
            throw new EmvException(ret);
        }

        LogUtils.i(TAG, "EMV Processing - transaction amount is: " + inputParam.getAmount());
        LogUtils.i(TAG, "EMV Processing - transaction aditional amount is: " + inputParam.getCashBackAmount());

        byte[] tvr = getTlv(0x95);
        LogUtils.hex(TAG, "TVR before trx: ", tvr);

        ACType acType = new ACType();
        if (inputParam.getAmount().length() > 10) {
            LogUtils.d(TAG, "process: " + inputParam.getAmount().length());
            EMVCallback.EMVSetAmount(ConvertHelper.getConvert().strToBcdPaddingRight(inputParam.getAmount()),
                    ConvertHelper.getConvert().strToBcdPaddingRight(inputParam.getCashBackAmount()));
        }
        ret = EMVCallback.EMVStartTrans(Long.parseLong(inputParam.getAmount()),
                Long.parseLong(inputParam.getCashBackAmount()), acType);
        LogUtils.i(TAG, "EMV Processing - EMVStartTrans returns: " + Integer.toString(ret));

        byte[] tsi = getTlv(0x9b);
        LogUtils.hex(TAG, "TSI: ", tsi);

        byte[] tvr2 = getTlv(0x95);
        LogUtils.hex(TAG, "TVR after trx: ", tvr2);

        byte[] appVersion = getTlv(0x9f08);
        LogUtils.hex(TAG, "App version card: ", appVersion);

        byte[] appVersion2 = getTlv(0x9f09);
        LogUtils.hex(TAG, "App version terminal: ", appVersion2);

        byte[] cvmList = getTlv(0x8e);
        LogUtils.hex(TAG, "CVM List: ", cvmList);

        byte[] cvmResult = getTlv(0x9f34);
        LogUtils.hex(TAG, "CVM Result: ", cvmResult);

        byte[] panAgain2 = getTlv(0x5A);
        LogUtils.hex(TAG, "PAN: ", panAgain2);

        paxEmvTrans.setCvmResult(cvmResult);

        if (ret != RetCode.EMV_OK) {
            if (ret == RetCode.ICC_RSP_6985) {
                throw new EmvException(EEmvExceptions.EMV_ERR_FALL_BACK);
            }
            throw new EmvException(ret);
        }

        if (acType.type == ACType.AC_TC) {
            LogUtils.i(TAG, "EMV Processing - Transaction result after 1st gen. AC is -> OFFLINE APPROVED");
            return new CTransResult(ETransResult.OFFLINE_APPROVED);
        } else if (acType.type == ACType.AC_AAC) {
            LogUtils.i(TAG, "EMV Processing - Transaction result after 1st gen. AC is -> OFFLINE DECLINED");
            return new CTransResult(ETransResult.OFFLINE_DENIED);
        }
        LogUtils.i(TAG, "EMV Processing - Transaction result after 1st gen. AC is -> GO ONLINE FOR AUTHORIZATION");

        paxEmvTrans.dccProcess();

        ETransResult result = onlineProc();
        byte[] script = combine7172(getTlv(0x71), getTlv(0x72));

        /**
         * script cannot be null when invoking EMVCallback.EMVCompleteTrans, otherwise ret could be
         * RetCode.EMV_PARAM_ERR
         */

        if (script == null) {
            script = new byte[0];
        }
        /**
         * //AET-146
         * Whatever value it returns from onlineProc(), 2nd GAC should be performed as per EMV Book 3
         * The right way to fix AET-146 is to map ABORT_TERMINATED to ONLINE_FAILED so that
         * EMVApi#EMVCompleteTrans will not return -30: emv param error
         *
         * should ensure script.length will not throw Null Pointer Exception
         * If Field 39 responsed from host is not 00, EMVCallback.EMVCompleteTrans will return -11(EMV DENIAL)
         *
         * 1st param of EMVApi#EMVCompleteTrans only accept 3 values:
         * ONLINE_APPROVE, ONLINE_DENIAL, and ONLINE_FAILED,
         * reference to the API doc of JNI_EMV_LIB_v102
         */
        LogUtils.i(TAG, "EMV Processing - Online result is: " + result);
        ret = EMVCallback.EMVCompleteTrans(Converter.toOnlineResult(result), script, script.length, acType);
        LogUtils.i(TAG, "EMV Processing - EMVCompleteTrans returns: " + Integer.toString(ret));
        //handle online result after second GAC
        if (ETransResult.ONLINE_DENIED == result) {
            throw new EmvException(EEmvExceptions.EMV_ERR_DENIAL);
        } else if (ETransResult.ONLINE_APPROVED != result && acType.type != ACType.AC_TC) {
            throw new EmvException(EEmvExceptions.EMV_ERR_ONLINE_TRANS_ABORT);
        }
        if (ret != RetCode.EMV_OK) {
            ByteArray scriptResult = new ByteArray();
            //If the return code is not EMV_OK, EMVGetScriptResult must be called
            int ret2 = EMVCallback.EMVGetScriptResult(scriptResult);
            LogUtils.i(TAG, "EMV Processing - EMVGetScriptResult returns: " + Integer.toString(ret2));
            return new CTransResult(ETransResult.ONLINE_CARD_DENIED);
        }

        if (acType.type == ACType.AC_TC) {
            if (result == ETransResult.ABORT_TERMINATED) {
                LogUtils.i(TAG, "EMV Processing - Transaction result after 2nd gen. AC is -> APPROVED OFFLINE");
                return new CTransResult(ETransResult.OFFLINE_APPROVED);
            } else {
                LogUtils.i(TAG, "EMV Processing - Transaction result after 2nd gen. AC is -> APPROVED");
                return new CTransResult(ETransResult.ONLINE_APPROVED);
            }
        } else if (acType.type == ACType.AC_AAC) {
            LogUtils.i(TAG, "EMV Processing - Transaction result after 2nd gen. AC is -> DECLINED BY CARD");
            return new CTransResult(ETransResult.ONLINE_CARD_DENIED);
        }

        ETransResult transResult = Tools.getEnum(ETransResult.class, ret - 1);
        if (transResult == null) {
            throw new EmvException(EEmvExceptions.EMV_ERR_UNKNOWN.getErrCodeFromBasement());
        }
        return new CTransResult(transResult);
    }

    /**
     * The Backend might return
     * I. 71, 72,  combine 71 and 72, say T1L1V1T2L2V2, in which T1 is TAG71 and T2 is TAG 72
     * II. 71 only, return TLV, in wich T is TAG 71
     * III. 72 only, return TLV, in wich T is TAG 72
     * IV. no script, return new byte[0]
     *
     * @param f71 value of 71
     * @param f72 value of 72
     * @return
     */
    public static byte[] combine7172(byte[] f71, byte[] f72) {

        boolean f71Empty = (null == f71 || f71.length <= 0);
        boolean f72Empty = (null == f72 || f72.length <= 0);

        if (f71Empty && f72Empty) {
            LogUtils.i(TAG, "EMV Processing - NO ISSUER SCRIPTS 71 & 72");
            return new byte[0];
        }

        if (f71Empty && !f72Empty) {
            LogUtils.i(TAG, "EMV Processing - only ISSUER SCRIPT 72 presents");
            return createTLVByTV((byte) 0x72, f72);
        }

        if (!f71Empty && f72Empty) {
            LogUtils.i(TAG, "EMV Processing - only ISSUER SCRIPT 71 presents");
            return createTLVByTV((byte) 0x71, f71);
        }

        LogUtils.i(TAG, "EMV Processing - both ISSUER SCRIPTS presents");
        return mergeByteArrays(createTLVByTV((byte) 0x71, f71), createTLVByTV((byte) 0x72, f72));
    }

    private static byte[] mergeByteArrays(byte[] byteArr1, byte[] byteArr2) {
        if (null == byteArr1 || byteArr1.length <= 0) {
            return byteArr2;
        }
        if (null == byteArr2 || byteArr2.length <= 0) {
            return byteArr1;
        }
        byte[] result = Arrays.copyOf(byteArr1, byteArr1.length + byteArr2.length);
        System.arraycopy(byteArr2, 0, result, byteArr1.length, byteArr2.length);
        return result;
    }

    private static byte[] createTLVByTV(byte tag, byte[] value) {
        if (null == value || value.length <= 0) {
            return new byte[0];
        }
        ByteBuffer bb = ByteBuffer.allocate(value.length + 3);
        bb.put(tag);
        if (value.length > 127) {//need two bytes to indicate length
            bb.put((byte) 0x81);
        }
        bb.put((byte) value.length);
        bb.put(value, 0, value.length);

        int len = bb.position();
        bb.position(0);

        byte[] tlv = new byte[len];
        bb.get(tlv, 0, len);

        return tlv;
    }

    @Override
    public void setListener(IEmvListener listener) {
        paxEmvTrans.setEmvListener(listener);
    }

    @Override
    public String getVersion() {
        ByteArray byteArray = new ByteArray();
        EMVCallback.EMVReadVerInfo(byteArray);
        return Arrays.toString(byteArray.data);
    }

    public void clear() throws EmvException {
        emvCallback.setCallbackListener(null);
    }

    private int addCapkIntoEmvLib() {
        int ret;
        ByteArray dataList = new ByteArray();
        ret = EMVCallback.EMVGetTLVData((short) 0x4F, dataList);
        if (ret != RetCode.EMV_OK) {
            ret = EMVCallback.EMVGetTLVData((short) 0x84, dataList);
        }
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "EMV Processing - get AID FAILED. Retunt code is: " + Integer.toString(ret));
            return ret;
        }

        byte[] rid = new byte[5];
        System.arraycopy(dataList.data, 0, rid, 0, 5);
        byte[] aid = new byte[dataList.length]; //AID has variable length from 5 to 16
        System.arraycopy(dataList.data, 0, aid, 0, dataList.length);

        LogUtils.i(TAG, "EMV Processing - selected RID is: " + Tools.bcd2Str(rid));
        LogUtils.i(TAG, "EMV Processing - selected AID is: " + Tools.bcd2Str(aid));

        ret = EMVCallback.EMVGetTLVData((short) 0x8F, dataList);
        if (ret != RetCode.EMV_OK) {
            LogUtils.e(TAG, "EMV Processing - get CAPK index FAILED. Retunt code is: " + Integer.toString(ret));
            return ret;
        }

        boolean foundCapk = false;
        byte keyId = dataList.data[0];
        for (Capk capk : capkList) {
            if (Tools.bytes2String(capk.getRid()).equals(new String(rid)) && capk.getKeyID() == keyId) {
                EMV_CAPK emvCapk = Converter.toEMVCapk(capk);
                ret = EMVCallback.EMVAddCAPK(emvCapk);
                LogUtils.i(TAG, "EMV Processing - EMVAddCAPK returns: " + Integer.toString(ret));
                foundCapk = true;
                LogUtils.i(TAG, "EMV Processing - selected CAPK RID: " + Tools.bcd2Str(rid) + ", KeyID: " + String.format("%02X ", keyId));
            }
        }
        if(!foundCapk)
            LogUtils.e(TAG, "EMV Processing - missing CAPK! Required CAPK RID: " + Tools.bcd2Str(rid) + ", KeyID: " + String.format("%02X ", keyId));

        return ret;
    }

    private ETransResult onlineProc() throws EmvException {
        EOnlineResult ret;
        ret = paxEmvTrans.onlineProc();
        if (ret == EOnlineResult.APPROVE) {
            LogUtils.i(TAG, "EMV Processing - Host decision about transaction is -> APPROVED");
            return ETransResult.ONLINE_APPROVED;
        } else if (ret == EOnlineResult.ABORT) {
            LogUtils.i(TAG, "EMV Processing - Host decision about transaction is -> ABORT / TERMINATED / NO RESPONSE FROM HOST");
            return ETransResult.ABORT_TERMINATED;
        } else {
            LogUtils.i(TAG, "EMV Processing - Host decision about transaction is -> DECLINED");
            return ETransResult.ONLINE_DENIED;
        }
    }
}

/* Location:           E:\Linhb\projects\Android\PaxEEmv_V1.00.00_20170401\lib\PaxEEmv_V1.00.00_20170401.jar
 * Qualified Name:     com.pax.eemv.EmvImpl
 * JD-Core Version:    0.6.0
 */