/*
 *  * ===========================================================================================
 *  * = COPYRIGHT
 *  *          PAX Computer Technology(Shenzhen); CO., LTD PROPRIETARY INFORMATION
 *  *   This software is supplied under the terms of a license agreement or nondisclosure
 *  *   agreement with PAX Computer Technology(Shenzhen); CO., LTD and may not be copied or
 *  *   disclosed except in accordance with the terms in that agreement.
 *  *     Copyright (C); 2019-? PAX Computer Technology(Shenzhen); CO., LTD All rights reserved.
 *  * Description: // Detail description about the voidction of this module,
 *  *             // interfaces with the other modules, and dependencies.
 *  * Revision History:
 *  * Date                  Author	                 Action
 *  * 20200713  	         xieYb                   Modify
 *  * ===========================================================================================
 *
 */
package com.evp.bizlib.receipt.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;

import com.evp.bizlib.R;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.config.IConfigUtils;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.bizlib.redeem.RedeemUtils;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.FontCache;
import com.evp.commonlib.utils.ResourceUtil;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func4;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * This class works for generate Transaction List information,The receipt was divided into
 * Title information part and content part.
 * For content part,every bitmap consist of 6 item TransData.
 */
@RouterService(interfaces = Func4.class, key = ReceiptConst.RECEIPT_TRANSLIST)
public class ReceiptTransList implements Func4<Context, List<TransData>, String, IConfigUtils, View> {
    private List<Bitmap> bitmaps;
    private PaxGLPage glPage;
    private IPage page;
    private String title;
    private String merchantName;
    private String merchantAddress;
    private List<TransData> transDataList;
    private Acquirer acquirer;

    @Override
    public View call(Context context, @NonNull List<TransData> transDataList, String title, IConfigUtils configUtils) {
        this.transDataList = transDataList;
        this.title = title;
        this.merchantName = configUtils.getPrintString("headerLine1Label");
        this.merchantAddress = configUtils.getPrintString("headerLine2Label") + "\n" + configUtils.getPrintString("headerLine3Label");
        bitmaps = new ArrayList<>();
        glPage = new PaxGLPage(context);
        acquirer = transDataList.get(0).getAcquirer();
        page = glPage.createPage();
        page.adjustLineSpace(-6);
        page.setTypeFace(FontCache.get(FontCache.FONT_NAME, BaseApplication.getAppContext()));
        generateTitleView(configUtils);
        generateContentView();
        return glPage.pageToView(page, 384);
    }

    /**
     * generate title view
     */
    private void generateTitleView(IConfigUtils configUtils) {

        //Logo
        page.addLine()
                .addUnit(page.createUnit()
                        .setBitmap(configUtils.getPrintResourceFile("headerLogo"))
                        .setGravity(Gravity.CENTER));
        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        if (!merchantName.isEmpty() || !merchantAddress.isEmpty()) {
            //Merchant Name
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(merchantName)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER));

            //Merchant Address
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(merchantAddress)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER));
            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER));
        }

        //  merchant ID
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_merchant_code))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(acquirer.getMerchantId())
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        // terminal ID/operator ID
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_terminal_code))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(acquirer.getTerminalId())
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        // batch FALSE
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_batch_num_space))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(ConvertUtils.getPaddedNumber(acquirer.getCurrBatchNo(), 6))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        // date/time
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_date))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(ConvertUtils.convertCurrentTime(ConvertUtils.DATE_TIME_PATTERN_PRINT))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        // title
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(title)
                        .setFontSize(ReceiptConst.FONT_BIG)
                        .setGravity(Gravity.CENTER));

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        // transaction information
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_trace))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_date))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));
        //region OLS
        if (acquirer.getName().equals("SCB OLS")) {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_type))
                            .setFontSize(ReceiptConst.FONT_SMALL));
        } else
        //endregion
        {
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_type))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_amount))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));
        }
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_card_no))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(2.0f))
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_auth_code))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));
        //region OLS
        if (acquirer.getName().equals("SCB OLS")) {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_points))
                            .setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_amount))
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.END));
        }
        //endregion

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));
        //bitmaps.add(glPage.pageToBitmap(page, 384));
    }

    /**
     * generate content view,every bitmap contains up to 6 transData
     */
    private void generateContentView() {
        int offset = 0;
        int dataLen = transDataList.size();
        for (TransData transData : transDataList) {
            if (offset % 6 == 0) {
                //page = glPage.createPage();
            }
            offset++;
            String type = transData.getTransType();
            ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
            ETransType orgTransType = ConvertUtils.enumValue(ETransType.class, transData.getOrigTransType());
            if (transType == ETransType.OFFLINE_SALE) {
                type = transType.getTransName();
            }
            //region OLS
            else if (transType == ETransType.REDEEM) {
                type = RedeemUtils.getPlanName(transData.getPaymentPlan());
            }
            //endregion

            ETransType origTransType = ConvertUtils.enumValue(ETransType.class, transData.getOrigTransType());
            if (origTransType != null) {
                type = String.format("%s(%s)", Objects.requireNonNull(transType).name(), origTransType.name());
            }
            // AET-18
            // transaction FALSE/transaction type/amount
            String temp;
            if (Objects.requireNonNull(transType).isSymbolNegative()) {
                temp = CurrencyConverter.convert(0 - ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
                if (origTransType == ETransType.REFUND) {
                    temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
                }
            } else {
                temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
            }
            String temp2 = ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6);
            String date = ConvertUtils.convert(transData.getDateTime(), ConvertUtils.TIME_PATTERN_TRANS,
                    ConvertUtils.TIME_PATTERN_DISPLAY);
            //AET-125
            page.addLine()
                    .addUnit(page.createUnit().setText(temp2).setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit().setText(date).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(3.0f));
            //region OLS
            if (transType == ETransType.REDEEM || orgTransType == ETransType.REDEEM) {
                page.addLine()
                        .addUnit(page.createUnit().setText(type).setFontSize(ReceiptConst.FONT_SMALL));
            } else
            //endregion
            {
            	page.addLine()
                    .addUnit(page.createUnit().setText(type).setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit().setText(temp).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END));
            }

            // card FALSE/auth code
            if (transData.getIssuer() != null) {
                temp = PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern());
            }
            temp2 = transData.getAuthCode() == null ? "" : transData.getAuthCode();

            page.addLine()
                    .addUnit(page.createUnit().setText(temp).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3.0f))
                    .addUnit(page.createUnit().setText(temp2).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END));

            if (transType == ETransType.REDEEM || orgTransType == ETransType.REDEEM) {
                if (Objects.requireNonNull(transType).isSymbolNegative()) {
                    temp = "-" + transData.getFormattedNetSaleAmt();
                    temp2 = "-" + transData.getFormattedRedeemPts();
                } else {
                    temp = transData.getFormattedNetSaleAmt();
                    temp2 = transData.getFormattedRedeemPts();
                }
                page.addLine()
                        .addUnit(page.createUnit().setText(temp2).setFontSize(ReceiptConst.FONT_SMALL))
                        .addUnit(page.createUnit().setText(temp).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END));
            }

            page.addLine().addUnit(page.createUnit().setText(" "));
            if (offset == dataLen) {
                page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));
            }
            if (offset == dataLen || (offset % 6 == 0)) {
               // bitmaps.add(glPage.pageToBitmap(page, 384));
            }
        }
    }
}
