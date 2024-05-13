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
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.dcc.DccUtils;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.FontCache;
import com.evp.commonlib.utils.ResourceUtil;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func3;

import java.util.Locale;

/**
 * This class works for generate Aid information
 */
@RouterService(interfaces = Func3.class,key = ReceiptConst.RECEIPT_DCC_RATE,singleton = true)
public class ReceiptDccRate implements Func3<TransData, Context, IConfigUtils, View> {

    @Override
    public View call(TransData transData, Context context, IConfigUtils configUtils) {
        PaxGLPage glPage = new PaxGLPage(context);
        IPage page = glPage.createPage();
        page.adjustLineSpace(-6);
        page.setTypeFace(FontCache.get(FontCache.FONT_NAME, BaseApplication.getAppContext()));

        String temp, temp2;
        Acquirer acquirer = transData.getAcquirer();

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Date time
        temp = ConvertUtils.convert(transData.getDateTime(), ConvertUtils.TIME_PATTERN_TRANS,
                ConvertUtils.TIME_PATTERN_PRINT);
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_SMALL));

        //Terminal & Merchant ID
                temp = ResourceUtil.getString(R.string.receipt_tid) + " " + acquirer.getTerminalId();
        temp2 = ResourceUtil.getString(R.string.receipt_mid) + " " + acquirer.getMerchantId();
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(temp2)
                        .setGravity(Gravity.END)
                        .setFontSize(ReceiptConst.FONT_SMALL));

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Local currency
        temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
        temp2 = CurrencyConverter.convert(0, transData.getCurrency());
        temp2 = temp2.replace("0.00", "");
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_dcc_local_currency))
                        .setFontSize(ReceiptConst.FONT_MEDIUM)
                        .setWeight(9.0f))
                .addUnit(page.createUnit()
                        .setText(temp2)
                        .setFontSize(ReceiptConst.FONT_MEDIUM)
                        .setGravity(Gravity.END)
                        .setWeight(4.0f));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_MEDIUM)
                        .setGravity(Gravity.END));

        //Foreign currency
        final Locale foreignLocale = CurrencyConverter.getLocaleFromCountryCode(transData.getDccCurrencyCode());
        temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getDccForeignAmount(), 0), foreignLocale);
        temp2 = CurrencyConverter.convert(0, foreignLocale);
        temp2 = temp2.replace("0.00", "");
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_just_currency))
                        .setFontSize(ReceiptConst.FONT_MEDIUM))
                .addUnit(page.createUnit()
                        .setText(temp2)
                        .setGravity(Gravity.END)
                        .setFontSize(ReceiptConst.FONT_MEDIUM));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_MEDIUM)
                        .setGravity(Gravity.END));

        //Ex. Rate
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_exchange_rate))
                        .setFontSize(ReceiptConst.FONT_MEDIUM))
                .addUnit(page.createUnit()
                        .setText(DccUtils.getExRateForPrint(transData.getDccExchangeRate()))
                        .setFontSize(ReceiptConst.FONT_MEDIUM)
                        .setGravity(Gravity.END));

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Ex. rate provided by ...
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(configUtils.getPrintString("dccFooterLine2Label"))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));

        return glPage.pageToView(page,384);
    }
}
