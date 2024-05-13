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

import androidx.annotation.NonNull;

import com.evp.bizlib.R;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.ResourceUtil;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func3;

import java.util.ArrayList;
import java.util.List;

/**
 * This class works for generate Transaction Failed List information,The receipt was divided into
 * Title information part and content part.
 * For content part,every bitmap consist of 18 item TransData.
 */
@RouterService(interfaces = Func3.class,key = ReceiptConst.RECEIPT_TRANS_FAILED_LIST)
public class ReceiptTransFailedList implements Func3<Context,List<TransData>,String, List<Bitmap>> {
    private List<Bitmap> bitmaps;
    private PaxGLPage glPage;
    private IPage page;
    private String title;
    private Acquirer acquirer;
    
    @Override
    public List<Bitmap> call(Context context, @NonNull List<TransData> failedTransList, String title) {
        this.title = title;
        bitmaps = new ArrayList<>();
        glPage = new PaxGLPage(context);
        acquirer = failedTransList.get(0).getAcquirer();
        page = glPage.createPage();
        List<TransData> failedList = new ArrayList<>();
        List<TransData> rejectList = new ArrayList<>();

        for (TransData data : failedTransList) {
            if (data.getOfflineSendState() == null) {
                continue;
            }
            if (data.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_ERR_SEND) {
                failedList.add(data);
            }
            if (data.getOfflineSendState() == TransData.OfflineStatus.OFFLINE_ERR_RESP) {
                rejectList.add(data);
            }
        }
        // title
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(title)
                        .setFontSize(ReceiptConst.FONT_BIG)
                        .setGravity(Gravity.CENTER));
        generateFailedMainInfo();
        generateTransData(failedList,false);

        page = glPage.createPage();
        generateRejectMainInfo(page);
        generateTransData(rejectList,true);
        return bitmaps;
    }

    private void generateRejectMainInfo(IPage page) {
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_reject_trans_details))
                        .setFontSize(ReceiptConst.FONT_SMALL));

        generateTranInfo(page);
        bitmaps.add(glPage.pageToBitmap(page,384));
    }

    private void generateTranInfo(IPage page) {
        // transaction information
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("VOUCHER")
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(2.0f))
                .addUnit(page.createUnit()
                        .setText("TYPE")
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER)
                        .setWeight(1.0f))
                .addUnit(page.createUnit()
                        .setText("AMOUNT")
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText("CARD FALSE")
                        .setFontSize(ReceiptConst.FONT_SMALL));
    }

    private void generateTransData(List<TransData> list,boolean isPrintEmptyLine) {
        int offset = 0;
        int dataLen = list.size();
        for (TransData transData : list) {
            if (offset % 18 == 0){
                page = glPage.createPage();
            }
            offset++;

            String type = transData.getTransType();
            ETransType transType = ConvertUtils.enumValue(ETransType.class,transData.getTransType());
            if(transType == ETransType.OFFLINE_SALE){
                type = transType.getTransName();
            }

            // transaction FALSE/transaction type/amount
            String temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());

            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6))
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(type)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER)
                            .setWeight(1.0f))
                    .addUnit(page.createUnit()
                            .setText(temp)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            // card FALSE/auth code
            temp = transData.getPan();
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(temp)
                            .setFontSize(ReceiptConst.FONT_SMALL));
            if (offset == dataLen && isPrintEmptyLine){
                page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));
            }
            if (offset == dataLen || (offset % 18 == 0)){
                bitmaps.add(glPage.pageToBitmap(page,384));
            }
        }
    }

    private void generateFailedMainInfo() {
        // merchant ID
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_merchant_code))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(2.0f))
                .addUnit(page.createUnit()
                        .setText(acquirer.getMerchantId())
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        // terminal ID/operator ID
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_terminal_code_space))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(acquirer.getTerminalId())
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

        // data/time
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_date))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(2.0f))
                .addUnit(page.createUnit()
                        .setText(ConvertUtils.convertCurrentTime(ConvertUtils.TIME_PATTERN_DISPLAY))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_failed_trans_details))
                        .setFontSize(ReceiptConst.FONT_SMALL));

        generateTranInfo(page);
        bitmaps.add(glPage.pageToBitmap(page,384));
    }

    /**
     * generate title view
     */
    private void generateTitleView() {
        // title
        page.addLine().addUnit(page.createUnit().setText(title).setFontSize(ReceiptConst.FONT_BIG).setGravity(Gravity.CENTER));

        //  merchant ID
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_merchant_code))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(2.0f))
                .addUnit(page.createUnit()
                        .setText(acquirer.getMerchantId())
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        // terminal ID/operator ID
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_terminal_code))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(acquirer.getTerminalId())
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
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(2.0f))
                .addUnit(page.createUnit()
                        .setText(ConvertUtils.convertCurrentTime(ConvertUtils.TIME_PATTERN_DISPLAY))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setWeight(3.0f));

        // transaction information
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_voucher))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_date))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_type))
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_amount))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_card_no))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(2.0f))
                .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_auth_code))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        bitmaps.add(glPage.pageToBitmap(page,384));
    }
}
