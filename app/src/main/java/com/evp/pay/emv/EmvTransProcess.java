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
package com.evp.pay.emv;

import com.evp.bizlib.card.TrackUtils;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.data.model.ETransType;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.IEmv;
import com.evp.eemv.IEmvListener;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EmvException;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.component.Component;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.settings.SysParam;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.exceptions.PiccDevException;

import java.util.Objects;

/**
 * The type Emv trans process.
 */
public class EmvTransProcess {

    private static final String TAG = "EmvTransProcess";

    private static final byte ONLINEPIN_CVM = (byte) 0x80;
    private static final byte SIGNATURE_CVM = 0x40;
    private static final byte CD_CVM = (byte) 0x80;
    private static final byte NO_CVM = 0x00;

    private static final String UNIONPAY_DEBITAID = "A000000333010101";
    private static final String UNIONPAY_CREDITAID = "A000000333010102";
    private static final String UNIONPAY_QUASICREDITAID = "A000000333010103";

    private IEmv emv;

    /**
     * Instantiates a new Emv trans process.
     *
     * @param emv the emv
     */
    public EmvTransProcess(IEmv emv) {
        this.emv = emv;
    }

    /**
     * Trans process c trans result.
     *
     * @param transData the trans data
     * @param listener  the listener
     * @return the c trans result
     * @throws EmvException the emv exception
     */
    public CTransResult transProcess(TransData transData, IEmvListener listener) throws EmvException {
        emv.setListener(listener);
        CTransResult result = emv.process(Component.toInputParam(transData));
        LogUtils.i(TAG, "EMV PROC:" + result.toString());
        return result;
    }

    /**
     * EMV init, set aid, capk and emv config
     */
    public void init() {
        try {
            emv.init();
            emv.setConfig(Component.getEmvConfig());
        } catch (EmvException e) {
            LogUtils.e(TAG, "", e);
        }
        emv.setAidParamList(FinancialApplication.getAidParamList());
        emv.setCapkList(FinancialApplication.getCapkList());
    }

    private static void emvOfflineApprovedCase(ETransResult result, IEmv emv, TransData transData) {
        try {
            emv.setTlv(0x8A, "Y1".getBytes());
        } catch (EmvException e) {
            LogUtils.w(TAG, "", e);
        }
        // set result
        transData.setEmvResult(result.name());
        // convert field 55 to transData
        ETransType eTransType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        byte[] f55 = EmvTags.getF55(emv, Objects.requireNonNull(eTransType), false);
        transData.setSendIccData(ConvertHelper.getConvert().bcdToStr(f55));
        Component.incTransNo();
    }

    private static void emvOnlineApprovedCase(ETransResult result, IEmv emv, TransData transData) {
        ETransType eTransType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        byte[] f55 = EmvTags.getF55(emv, Objects.requireNonNull(eTransType), false);
        transData.setSendIccData(ConvertHelper.getConvert().bcdToStr(f55));
        transData.setEmvResult(result.name());
    }

    private static void emvArqcCase(ETransResult result, IEmv emv, TransData transData) {
        saveCardInfoAndCardSeq(emv, transData);
        transData.setEmvResult(result.name());

        if (result == ETransResult.ARQC) {
            generateF55AfterARQC(emv, transData);
        }

        try {
            FinancialApplication.getDal().getPicc(EPiccType.INTERNAL).close();
        } catch (PiccDevException e) {
            LogUtils.e(TAG, "", e);
        }
    }

    private static void emvOfflineDeniedCase(ETransResult result, IEmv emv, TransData transData) {
        try {
            emv.setTlv(0x8A, "Z1".getBytes());
        } catch (EmvException e) {
            LogUtils.e(TAG, "", e);
        }
        Component.incTransNo();
        transData.setEmvResult(result.name());
    }

    private static void emvOnlineDenied(ETransResult result, IEmv emv, TransData transData) {
        //do nothing for now
    }

    private static void emvOnlineApprovedCardDeniedCase(ETransResult result, IEmv emv, TransData transData) {
        byte[] f55 = EmvTags.getF55forPosAccpDup(emv);
        if (f55.length > 0) {
            TransData dupTransData = GreendaoHelper.getTransDataHelper().findFirstDupRecord(transData.getAcquirer());
            if (dupTransData != null) {
                dupTransData.setDupIccData(ConvertHelper.getConvert().bcdToStr(f55));
                GreendaoHelper.getTransDataHelper().update(dupTransData);
            }

        }
    }

    /**
     * EMV result process
     *
     * @param result    {@link ETransResult}
     * @param emv       the emv
     * @param transData {@link TransData}
     */
    public static void emvTransResultProcess(ETransResult result, IEmv emv, TransData transData) {
        saveTvrTsi(emv, transData);
        if (result == ETransResult.OFFLINE_APPROVED) {
            emvOfflineApprovedCase(result, emv, transData);
        } else if (result == ETransResult.ONLINE_APPROVED) {
            emvOnlineApprovedCase(result, emv, transData);
        } else if (result == ETransResult.ARQC || result == ETransResult.SIMPLE_FLOW_END) {
            emvArqcCase(result, emv, transData);
        } else if (result == ETransResult.ONLINE_DENIED) {
            emvOnlineDenied(result, emv, transData);
        } else if (result == ETransResult.OFFLINE_DENIED) {
            emvOfflineDeniedCase(result, emv, transData);
        } else if (result == ETransResult.ONLINE_CARD_DENIED) {
            emvOnlineApprovedCardDeniedCase(result, emv, transData);
        }
    }

    /**
     * after ARQC, generate field 55
     *
     * @param transData {@link TransData}
     */
    private static void generateF55AfterARQC(IEmv emv, TransData transData) {
        ETransType transType = ConvertUtils.enumValue(ETransType.class,transData.getTransType());
        byte[] f55 = EmvTags.getF55(emv, Objects.requireNonNull(transType), false);
        transData.setSendIccData(ConvertHelper.getConvert().bcdToStr(f55));

        byte[] arqc = emv.getTlv(0x9F26);
        if (arqc != null && arqc.length > 0) {
            transData.setArqc(ConvertHelper.getConvert().bcdToStr(arqc));
        }

        byte[] f55Dup = EmvTags.getF55(emv, transType, true);
        if (f55Dup.length > 0) {
            transData.setDupIccData(ConvertHelper.getConvert().bcdToStr(f55Dup));
        }
    }

    /**
     * save track data
     *
     * @param emv       the emv
     * @param transData {@link TransData}
     */
    public static void saveCardInfoAndCardSeq(IEmv emv, TransData transData) {

        byte[] track2 = emv.getTlv(TagsTable.TRACK2);
        String strTrack2 = TrackUtils.getTrack2FromTag57(track2);
        transData.setTrack2(strTrack2);
        // card no
        String pan = TrackUtils.getPan(strTrack2);
        transData.setPan(pan);
        // exp date
        byte[] expDate = emv.getTlv(0x5F24);
        if (expDate != null && expDate.length > 0) {
            String temp = ConvertHelper.getConvert().bcdToStr(expDate);
            transData.setExpDate(temp.substring(0, 4));
        }
        byte[] cardSeq = emv.getTlv(0x5F34);
        if (cardSeq != null && cardSeq.length > 0) {
            String temp = ConvertHelper.getConvert().bcdToStr(cardSeq);
            transData.setCardSerialNo(temp.substring(0, 2));
        }

    }

    private static void saveTvrTsi(IEmv emv, TransData transData) {
        // TVR
        byte[] tvr = emv.getTlv(0x95);
        if (tvr != null && tvr.length > 0) {
            transData.setTvr(ConvertHelper.getConvert().bcdToStr(tvr));
        }
        // ATC
        byte[] atc = emv.getTlv(0x9F36);
        if (atc != null && atc.length > 0) {
            transData.setAtc(ConvertHelper.getConvert().bcdToStr(atc));
        }
        //
        // TSI
        byte[] tsi = emv.getTlv(0x9B);
        if (tsi != null && tsi.length > 0) {
            transData.setTsi(ConvertHelper.getConvert().bcdToStr(tsi));
        }
        // TC
        byte[] tc = emv.getTlv(0x9F26);
        if (tc != null && tc.length > 0) {
            transData.setTc(ConvertHelper.getConvert().bcdToStr(tc));
        }

        // AppLabel
        byte[] appLabel = emv.getTlv(0x50);
        if (appLabel != null && appLabel.length > 0) {
            transData.setEmvAppLabel(new String(appLabel));
        }
        // AppName
        byte[] issuerCodeTableIndex = emv.getTlv(TagsTable.ISSUER_CODE_TABLE_INDEX);
        if (issuerCodeTableIndex != null && issuerCodeTableIndex.length > 0 && issuerCodeTableIndex[0] == 0x01) {
            // AppName
            byte[] appName = emv.getTlv(TagsTable.APP_NAME);
            if (appName != null && appName.length > 0) {
                transData.setEmvAppName(new String(appName));
            }
        }

        // AID
        byte[] aid = emv.getTlv(0x4F);
        if (aid != null && aid.length > 0) {
            transData.setAid(ConvertHelper.getConvert().bcdToStr(aid));
        }

        //Cardholder name
        byte[] cardholderName = emv.getTlv(TagsTable.CARDHOLDER_NAME);
        if (cardholderName != null && cardholderName.length > 0) {
            transData.setCardholderName(new String(cardholderName));
        }
    }

    /**
     * @return true-no PIN false-unknown
     */
    private static boolean clssCDCVMProcss(IEmv emv) {
        if (SysParam.getInstance().getBoolean(R.string.QUICK_PASS_TRANS_CDCVM_FLAG)) {

            byte[] value = emv.getTlv(TagsTable.CTQ);
            if ((value[1] & CD_CVM) == CD_CVM && (value[0] & ONLINEPIN_CVM) != ONLINEPIN_CVM) {
                return true;
            }

        }

        return false;
    }

    /**
     * QPBOC need to check if online PIN is required, only foreign card need to check by value of tag 9F6C,
     * local card is default to have PIN
     *
     * @param emv the emv
     * @return true /false
     */
    public static boolean isQpbocNeedOnlinePin(IEmv emv) {
        if (!isCupOutSide(emv)) {
            return true;
        }

        byte[] value = emv.getTlv(TagsTable.CTQ);
        return (value[0] & ONLINEPIN_CVM) == ONLINEPIN_CVM;
    }

    /**
     * check if is a dual currency CUP card
     */
    private static boolean isCupOutSide(IEmv emv) {
        int[] tags = new int[]{0x9F51, 0xDF71}; // tag9F51：第一货币 tagDF71：第二货币
        int flag = 0;
        byte[] val = null;
        for (int tag : tags) {
            val = emv.getTlv(tag);
            if (val == null) {
                continue;
            }
            flag = 1; // 能获取到货币代码值
            if ("0156".equals(ConvertHelper.getConvert().bcdToStr(val))) {
                return false;
            }
        }

        return !(val == null && flag == 0);
    }

    /**
     * credit or debit
     *
     * @param aid aid
     */
    private static boolean isCredit(String aid) {
        if (UNIONPAY_DEBITAID.equals(aid)) { // debit
            return false;
        } else if (UNIONPAY_CREDITAID.equals(aid)) { // credit
            return true;
        } else {//  quasi credit
            return UNIONPAY_QUASICREDITAID.equals(aid);
        }
    }

    /**
     * Clss qps process boolean.
     *
     * @param emv       the emv
     * @param transData {@link TransData}
     * @return true -no PIN false-unknown
     */
    public static boolean clssQPSProcess(IEmv emv, TransData transData) {

        if (!SysParam.getInstance().getBoolean(R.string.QUICK_PASS_TRANS_PIN_FREE_SWITCH)) {
            return false;
        }
        TransData.EnterMode enterMode = transData.getEnterMode();
        if (enterMode != TransData.EnterMode.CLSS) {
            return false;
        }
        int limitAmount = SysParam.getInstance().getInt(R.string.QUICK_PASS_TRANS_PIN_FREE_AMOUNT);
        String amount = transData.getAmount().replace(".", "");
        // card type
        byte[] aid = emv.getTlv(0x4F);
        if (aid == null) {
            return false;
        }
        boolean isCredit = isCredit(ConvertHelper.getConvert().bcdToStr(aid));
        boolean pinFree;
        ETransType transType = ConvertUtils.enumValue(ETransType.class,transData.getTransType());
        if (ETransType.SALE.equals(transType)
                || ETransType.PREAUTH.equals(transType)) {
            pinFree = clssCDCVMProcss(emv);
            transData.setCDCVM(pinFree);
            if (pinFree) {
                return true;
            }
            if (!SysParam.getInstance().getBoolean(R.string.QUICK_PASS_TRANS_FLAG)) {
                return false;
            }

            if (isCupOutSide(emv)) { // 外卡
                // 贷记或准贷记卡: 小于免密限额则免输密码 借记卡：依据卡片与终端协商结果

                if (!isCredit) { // 借记卡
                    return false;
                }
                // 贷记卡或准贷记卡处理
                return (Integer.parseInt(amount) <= limitAmount);
            } else { // 内卡
                return (Integer.parseInt(amount) <= limitAmount);
            }
        }

        return false;

    }
}
