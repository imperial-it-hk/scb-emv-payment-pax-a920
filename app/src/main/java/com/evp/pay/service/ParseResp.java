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
package com.evp.pay.service;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.TransResultUtils;
import com.evp.payment.evpscb.R;
import com.evp.settings.SysParam;
import com.pax.unifiedsdk.message.BaseResponse;
import com.pax.unifiedsdk.message.PreAuthMsg;
import com.pax.unifiedsdk.message.PrintBitmapMsg;
import com.pax.unifiedsdk.message.RefundMsg;
import com.pax.unifiedsdk.message.ReprintTotalMsg;
import com.pax.unifiedsdk.message.ReprintTransMsg;
import com.pax.unifiedsdk.message.SaleMsg;
import com.pax.unifiedsdk.message.SettleMsg;
import com.pax.unifiedsdk.message.TransResponse;
import com.pax.unifiedsdk.message.VoidMsg;
import com.pax.unifiedsdk.sdkconstants.SdkConstants;

/**
 * The type Parse resp.
 */
public class ParseResp {

    private ParseResp() {
        //do nothing
    }

    /**
     * Generate base response.
     *
     * @param commandType the command type
     * @param result      the result
     * @return the base response
     */
    public static BaseResponse generate(int commandType, final ActionResult result) {

        switch (commandType) {
            case SdkConstants.PRE_AUTH:
                return updatePreAuth(new PreAuthMsg.Response(), result);
            case SdkConstants.SALE:
                return updateSale(new SaleMsg.Response(), result);
            case SdkConstants.VOID:
                return updateVoid(new VoidMsg.Response(), result);
            case SdkConstants.REFUND:
                return updateRefund(new RefundMsg.Response(), result);
            case SdkConstants.SETTLE:
                return updateSettle(new SettleMsg.Response(), result);
            case SdkConstants.REPRINT_TRANS:
                return updateReprintTrans(new ReprintTransMsg.Response(), result);
            case SdkConstants.REPRINT_TOTAL:
                return updateReprintTotal(new ReprintTotalMsg.Response(), result);
            case SdkConstants.PRINT_BITMAP:
                return updatePrintBitmap(new PrintBitmapMsg.Response(), result);
            default:
                return null;

        }
    }

    private static boolean updateBase(BaseResponse response, final ActionResult result) {
        response.setAppId(FinancialApplication.getApp().getPackageName());
        if (result == null) {
            response.setRspCode(TransResult.ERR_HOST_REJECT);
            response.setRspMsg(TransResultUtils.getMessage(TransResult.ERR_HOST_REJECT));
            return false;
        }

        if (result.getRet() != TransResult.SUCC) {
            response.setRspCode(result.getRet());
            response.setRspMsg(TransResultUtils.getMessage(result.getRet()));
            return false;
        }

        response.setRspMsg(TransResultUtils.getMessage(TransResult.SUCC));
        return result.getData() != null;
    }

    private static void updateTrans(TransResponse response, final ActionResult result) {
        if (!updateBase(response, result)) {
            return;
        }
        response.setMerchantName(SysParam.getInstance().getString(R.string.EDC_MERCHANT_NAME_EN));
        response.setMerchantId(FinancialApplication.getAcqManager().getCurAcq().getMerchantId());
        response.setTerminalId(FinancialApplication.getAcqManager().getCurAcq().getTerminalId());

        TransData transData = (TransData) result.getData();

        response.setCardNo(PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern()));
        response.setVoucherNo(transData.getTraceNo());
        response.setBatchNo(transData.getBatchNo());
        response.setIssuerName(transData.getIssuer().getName());
        response.setAcquirerName(transData.getAcquirer().getName());
        response.setRefNo(transData.getRefNo());
        response.setTransTime(transData.getDateTime());
        response.setAmount(transData.getAmount());
        response.setAuthCode(transData.getAuthCode());
        response.setCardType(enterMode2CardType(transData.getEnterMode()));
        response.setCardholderSignature(transData.getSignData());
        response.setSignaturePath(transData.getSignPath());
    }

    @TransResponse.CardType
    private static int enterMode2CardType(TransData.EnterMode enterMode) {
        switch (enterMode) {
            case MANUAL:
                return TransResponse.MANUAL;
            case SWIPE:
                return TransResponse.MAG;
            case INSERT:
                return TransResponse.ICC;
            case CLSS:
                return TransResponse.PICC;
            case FALLBACK:
                return TransResponse.FALLBACK;
            default:
                return TransResponse.NO_CARD;
        }
    }

    private static PreAuthMsg.Response updatePreAuth(PreAuthMsg.Response response, final ActionResult result) {
        updateTrans(response, result);
        return response;
    }

    private static SaleMsg.Response updateSale(SaleMsg.Response response, final ActionResult result) {
        updateTrans(response, result);
        return response;
    }

    private static VoidMsg.Response updateVoid(VoidMsg.Response response, final ActionResult result) {
        updateTrans(response, result);
        return response;
    }

    private static RefundMsg.Response updateRefund(RefundMsg.Response response, final ActionResult result) {
        updateTrans(response, result);
        return response;
    }

    private static SettleMsg.Response updateSettle(SettleMsg.Response response, final ActionResult result) {
        updateBase(response, result);
        return response;
    }

    private static ReprintTransMsg.Response updateReprintTrans(ReprintTransMsg.Response response, final ActionResult result) {
        updateBase(response, result);
        return response;
    }

    private static ReprintTotalMsg.Response updateReprintTotal(ReprintTotalMsg.Response response, final ActionResult result) {
        updateBase(response, result);
        return response;
    }

    private static PrintBitmapMsg.Response updatePrintBitmap(PrintBitmapMsg.Response response, final ActionResult result) {
        updateBase(response, result);
        return response;
    }
}
