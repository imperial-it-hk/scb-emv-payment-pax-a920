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
package com.evp.pay.emv.clss;

import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.local.GreendaoHelper;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.eemv.IClss;
import com.evp.eemv.IClssListener;
import com.evp.eemv.entity.CTransResult;
import com.evp.eemv.entity.Config;
import com.evp.eemv.entity.TagsTable;
import com.evp.eemv.enums.ETransResult;
import com.evp.eemv.exception.EmvException;
import com.evp.pay.trans.component.Component;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.impl.GL;
import com.pax.gl.pack.ITlv;
import com.pax.gl.pack.exception.TlvException;

import java.util.List;

/**
 * The type Clss trans process.
 */
public class ClssTransProcess {

    private static final String TAG = "ClssTransProcess";

    private IClss clss;

    /**
     * Instantiates a new Clss trans process.
     *
     * @param clss the clss
     */
    public ClssTransProcess(IClss clss) {
        this.clss = clss;
    }

    /**
     * Gen clss config config.
     *
     * @return the config
     */
    public static Config genClssConfig() {
        Config cfg = Component.getEmvConfig();
        cfg.setUnpredictableNumberRange("0060");
        cfg.setSupportOptTrans(true);
        cfg.setTransCap("D8B04000");
        cfg.setDelayAuthFlag(false);
        return cfg;
    }

    /**
     * Trans process c trans result.
     *
     * @param transData the trans data
     * @param listener  the listener
     * @return the c trans result
     * @throws EmvException the emv exception
     */
    public CTransResult transProcess(TransData transData, IClssListener listener) throws EmvException {
        clss.setListener(listener);
        CTransResult result = clss.process(Component.toInputParam(transData));
        LogUtils.i(TAG, "clss PROC:" + result.getCvmResult() + " " + result.getTransResult());
        return result;
    }

    /**
     * Clss trans result process.
     *
     * @param result    the result
     * @param clss      the clss
     * @param transData the trans data
     */
    public static void clssTransResultProcess(CTransResult result, IClss clss, TransData transData) {
        updateEmvInfo(clss, transData);
        List<ClssDE55Tag> clssDE55TagList = ClssDE55Tag.genClssDE55Tags(clss.getKernelType());

        if (result.getTransResult() == ETransResult.CLSS_OC_ONLINE_REQUEST) {
            try {
                clss.setTlv(TagsTable.CRYPTO, ConvertHelper.getConvert().strToBcdPaddingLeft("80"));
            } catch (EmvException e) {
                LogUtils.w(TAG, "", e);
                transData.setEmvResult(ETransResult.ABORT_TERMINATED.name());
                return;
            }

            // prepare online DE55 data
            if (setStdDe55(clss, result, transData, clssDE55TagList) != 0) {
                transData.setEmvResult(ETransResult.ABORT_TERMINATED.name());
            }
        } else if (result.getTransResult() == ETransResult.CLSS_OC_APPROVED) {
            // save for upload
            setStdDe55(clss, result, transData, clssDE55TagList);
            transData.setOfflineSendState(TransData.OfflineStatus.OFFLINE_NOT_SENT);
            transData.setReversalStatus(TransData.ReversalStatus.NORMAL);
            GreendaoHelper.getTransDataHelper().insert(transData);

            // increase trans no.
            Component.incTransNo();
        }
    }

    private static void updateEmvInfo(IClss clss, TransData transData) {
        //AppLabel
        byte[] value = clss.getTlv(TagsTable.APP_LABEL);
        if (value != null) {
            transData.setEmvAppLabel(new String(value));
        }

        //TVR
        value = clss.getTlv(TagsTable.TVR);
        if (value != null) {
            transData.setTvr(ConvertHelper.getConvert().bcdToStr(value));
        }

        //TSI
        value = clss.getTlv(TagsTable.TSI);
        if (value != null) {
            transData.setTsi(ConvertHelper.getConvert().bcdToStr(value));
        }

        //ATC
        value = clss.getTlv(TagsTable.ATC);
        if (value != null) {
            transData.setAtc(ConvertHelper.getConvert().bcdToStr(value));
        }

        //AppCrypto
        value = clss.getTlv(TagsTable.APP_CRYPTO);
        if (value != null) {
            transData.setArqc(ConvertHelper.getConvert().bcdToStr(value));
        }

        //AppName
        byte[] issuerCodeTableIndex = clss.getTlv(TagsTable.ISSUER_CODE_TABLE_INDEX);
        if (issuerCodeTableIndex != null && issuerCodeTableIndex.length > 0 && issuerCodeTableIndex[0] == 0x01) {
            //AppName
            value = clss.getTlv(TagsTable.APP_NAME);
            if (value != null) {
                transData.setEmvAppName(new String(value));
            }
        }

        //AID
        value = clss.getTlv(TagsTable.CAPK_RID);
        if (value != null) {
            transData.setAid(ConvertHelper.getConvert().bcdToStr(value));
        }

        //TC
        value = clss.getTlv(TagsTable.APP_CRYPTO);
        if (value != null) {
            transData.setTc(ConvertHelper.getConvert().bcdToStr(value));
        }

        //Cardholder name
        value = clss.getTlv(TagsTable.CARDHOLDER_NAME);
        if (value != null) {
            transData.setCardholderName(new String(value));
        }
    }

    // set ADVT/TIP bit 55
    private static int setStdDe55(IClss clss, CTransResult result, TransData transData, List<ClssDE55Tag> clssDE55TagList) {
        ITlv tlv = GL.getGL().getPacker().getTlv();
        ITlv.ITlvDataObjList list = tlv.createTlvDataObjectList();

        for (ClssDE55Tag i : clssDE55TagList) {
            ITlv.ITlvDataObj tag = tlv.createTlvDataObject();
            int tmpTag = i.getEmvTag();
            byte[] tmpValue = clss.getTlv(i.getEmvTag());
            //Amount other has to be always set
            if(tmpTag == TagsTable.AMOUNT_OTHER && (tmpValue == null || tmpValue.length <= 0)) {
                tmpValue = new byte[6];
            }
            //Skip PAN sequence number when it's not personalized in card
            if(tmpTag == TagsTable.PAN_SEQ_NO && (tmpValue == null || tmpValue.length <= 0)) {
                continue;
            }
            tag.setTag(tmpTag);
            tag.setValue(tmpValue);
            list.addDataObj(tag);
        }

        byte[] f55Data;
        try {
            f55Data = tlv.pack(list);
        } catch (TlvException e) {
            LogUtils.e(TAG, "", e);
            return TransResult.ERR_PACK;
        }

        if (f55Data.length > 255) {
            return TransResult.ERR_PACKET;
        }
        transData.setSendIccData(ConvertHelper.getConvert().bcdToStr(f55Data));
        return TransResult.SUCC;
    }
}
