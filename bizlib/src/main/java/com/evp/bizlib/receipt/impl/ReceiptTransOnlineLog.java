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

import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func3;

import java.util.HashMap;

/**
 * This class works for generate Transaction Total information
 */
@RouterService(interfaces = Func3.class,key = ReceiptConst.RECEIPT_TRANSLOG,singleton = true)
public class ReceiptTransOnlineLog implements Func3<Context, HashMap<String, byte[]>,Boolean, Bitmap> {

    @Override
    public Bitmap call(Context context, HashMap<String, byte[]> map, Boolean isSend) {
        PaxGLPage glPage = new PaxGLPage(context);
        IPage page = glPage.createPage();

        if (isSend) {
            page.addLine().addUnit(page.createUnit().setText("Send data:").setFontSize(ReceiptConst.FONT_SMALL));
        } else {
            page.addLine().addUnit(page.createUnit().setText("Recv data:").setFontSize(ReceiptConst.FONT_SMALL));
        }

        generateBitmapSingleLine("H", map.get("h"), page);
        generateBitmapSingleLine("M", map.get("m"), page);

        for (int i = 0; i <= 64; ++i) {
            String tag = Integer.toString(i);
            generateBitmapSingleLine(tag, map.get(tag), page);

        }
        generateBitmapSingleLine("123", map.get("123"), page);
        page.addLine().addUnit(page.createUnit().setText("\n\n\n").setFontSize(ReceiptConst.FONT_SMALL));



        return glPage.pageToBitmap(page,384);
    }

    private void generateBitmapSingleLine(String tag, byte[] value, IPage page) {
        if (value == null || value.length == 0) {
            return;
        }
        String writeValue;
        String writeTag = tag;
        if ("52".equals(writeTag) || "55".equals(writeTag) || "56".equals(writeTag) || "62".equals(writeTag) || "63".equals(writeTag) || "123".equals(writeTag)) {
            writeValue = ConvertHelper.getConvert().bcdToStr(value);
        } else {
            writeValue = new String(value);
        }

        page.addLine().addUnit(page.createUnit().setText("[" + writeTag + "]:" + writeValue).setFontSize(ReceiptConst.FONT_SMALL));
    }
}
