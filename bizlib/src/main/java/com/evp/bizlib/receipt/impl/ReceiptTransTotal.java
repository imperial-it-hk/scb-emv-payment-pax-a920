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
import android.view.Gravity;
import android.view.View;

import com.evp.bizlib.R;
import com.evp.bizlib.config.IConfigUtils;
import com.evp.bizlib.data.entity.TransTotal;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.sp.SharedPrefUtil;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.FontCache;
import com.evp.commonlib.utils.ResourceUtil;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func4;

/**
 * This class works for generate Transaction Total information
 */
@RouterService(interfaces = Func4.class,key = ReceiptConst.RECEIPT_TRANSTOTAL,singleton = true)
public class ReceiptTransTotal implements Func4<Context, TransTotal, String, IConfigUtils, View> {

    @Override
    public View call(Context context,TransTotal transTotal,String title, IConfigUtils config) {
        PaxGLPage glPage = new PaxGLPage(context);
        IPage page = glPage.createPage();
        page.adjustLineSpace(-6);
        page.setTypeFace(FontCache.get(FontCache.FONT_NAME, BaseApplication.getAppContext()));
        String merchantName = config.getPrintString("headerLine1Label");
        String merchantAddress = config.getPrintString("headerLine2Label") + "\n" + config.getPrintString("headerLine3Label");

        //Logo
        page.addLine()
                .addUnit(page.createUnit()
                        .setBitmap(config.getPrintResourceFile("headerLogo"))
                        .setGravity(Gravity.CENTER));
        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Merchant Name
        if (!merchantName.isEmpty())
            page.addLine()
                .addUnit(page.createUnit()
                        .setText(merchantName)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Merchant Address
        if (!merchantAddress.isEmpty())
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

        // merchant ID
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_merchant_code))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(transTotal.getAcquirer().getMerchantId())
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        // terminal ID
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_terminal_code_space))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(transTotal.getAcquirer().getTerminalId())
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        //batch no
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_batch_num_space))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(ConvertUtils.getPaddedNumber(transTotal.getBatchNo(), 6))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        // date/time
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_date))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(ConvertUtils.convertCurrentTime(ConvertUtils.DATE_TIME_PATTERN_PRINT))
                        .setGravity(Gravity.END)
                        .setFontSize(ReceiptConst.FONT_SMALL));

        // acquirer name
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_card_acquirer))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(transTotal.getAcquirer().getName())
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

        // type/count/amount
        //AET-124
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_type))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(3f))
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_count))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER)
                        .setWeight(1.5f))
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_amount))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        // sale
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.trans_sale))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(3f))
                .addUnit(page.createUnit()
                        .setText(Long.toString(transTotal.getSaleTotalNum()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER)
                        .setWeight(1.5f))
                .addUnit(page.createUnit()
                        .setText(CurrencyConverter.convert(transTotal.getSaleTotalAmt()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));
        // offline

        // refund
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.trans_refund))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(3f))
                .addUnit(page.createUnit()
                        .setText(Long.toString(transTotal.getRefundTotalNum()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER)
                        .setWeight(1.5f))
                .addUnit(page.createUnit()
                        .setText(CurrencyConverter.convert(0-transTotal.getRefundTotalAmt()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        // AET-66
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.settle_total_void_sale))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(3f))
                .addUnit(page.createUnit()
                        .setText(Long.toString(transTotal.getSaleVoidTotalNum()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER)
                        .setWeight(1.5f))
                .addUnit(page.createUnit()
                        .setText(CurrencyConverter.convert(0-transTotal.getSaleVoidTotalAmt()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.settle_total_void_refund))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(3f))
                .addUnit(page.createUnit()
                        .setText(Long.toString(transTotal.getRefundVoidTotalNum()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER)
                        .setWeight(1.5f))
                .addUnit(page.createUnit()
                        .setText(CurrencyConverter.convert(transTotal.getRefundVoidTotalAmt()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.settle_total_offline))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(3f))
                .addUnit(page.createUnit()
                        .setText(Long.toString(transTotal.getOfflineTotalNum()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER)
                        .setWeight(1.5f))
                .addUnit(page.createUnit()
                        .setText(CurrencyConverter.convert(transTotal.getOfflineTotalAmt()))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        if (transTotal.getAcquirer().getName().equals("SCB OLS")) {
            long val;

            //Line
            page.addLine().addUnit(page.createUnit().setText(ReceiptConst.RECIPT_LINE).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.CENTER));
            page.addLine().addUnit(page.createUnit().setText(" "));

            page.addLine()
                    .addUnit(page.createUnit().setText("Discount% + Credit").setFontSize(ReceiptConst.FONT_SMALL));
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_count)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(Long.toString(transTotal.getRedeemDiscountNum())).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_points)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(Long.toString(transTotal.getRedeemDiscountPts() / 100)).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_amount)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(CurrencyConverter.convert(transTotal.getRedeemDiscountAmt())).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            page.addLine().addUnit(page.createUnit().setText(" "));

            page.addLine()
                    .addUnit(page.createUnit().setText("Specific Product").setFontSize(ReceiptConst.FONT_SMALL));
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_count)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(Long.toString(transTotal.getRedeemProductNum())).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_points)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(Long.toString(transTotal.getRedeemProductPts() / 100)).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_amount)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(CurrencyConverter.convert(transTotal.getRedeemProductAmt())).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            page.addLine().addUnit(page.createUnit().setText(" "));

            page.addLine().addUnit(page.createUnit().setText(ReceiptConst.RECIPT_LINE).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.CENTER));
            page.addLine()
                    .addUnit(page.createUnit().setText("GRAND TOTALS").setFontSize(ReceiptConst.FONT_SMALL));
            val = transTotal.getRedeemVoucherNum() + transTotal.getRedeemPointNum() + transTotal.getRedeemDiscountNum() + transTotal.getRedeemProductNum();
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_count)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(Long.toString(val)).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            val = transTotal.getRedeemVoucherPts() + transTotal.getRedeemPointPts() + transTotal.getRedeemDiscountPts() + transTotal.getRedeemProductPts();
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_points)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(Long.toString(val/ 100)).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            val = transTotal.getRedeemVoucherAmt() + transTotal.getRedeemPointAmt() + transTotal.getRedeemDiscountAmt() + transTotal.getRedeemProductAmt();
            page.addLine()
                    .addUnit(page.createUnit().setText(ResourceUtil.getString(R.string.receipt_amount)).setFontSize(ReceiptConst.FONT_SMALL).setWeight(3f))
                    .addUnit(page.createUnit().setText(CurrencyConverter.convert(val)).setFontSize(ReceiptConst.FONT_SMALL).setGravity(Gravity.END).setWeight(7f));
            page.addLine().addUnit(page.createUnit().setText(" "));

        }


        SharedPrefUtil sharedPrefUtil = new SharedPrefUtil(BaseApplication.getAppContext());
        String comType = sharedPrefUtil.getString(ResourceUtil.getString(R.string.COMM_TYPE));
        if (ResourceUtil.getString(R.string.demo).equals(comType)){
            page.addLine().addUnit(page.createUnit().setText(" "));
            page.addLine().addUnit(page.createUnit()
                    .setText(ResourceUtil.getString(R.string.demo_mode))
                    .setGravity(Gravity.CENTER));
        }
        page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));
        return glPage.pageToView(page, 384);
    }
}
