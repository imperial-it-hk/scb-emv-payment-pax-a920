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

import com.evp.bizlib.data.entity.EmvCapk;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func2;

import java.util.ArrayList;
import java.util.List;

/**
 * This class works for generate Capk information
 */
@RouterService(interfaces = Func2.class,key = ReceiptConst.RECEIPT_CAPK,singleton = true)
public class ReceiptCapk implements Func2<List<EmvCapk>,Context, List<Bitmap>> {

    @Override
    public List<Bitmap> call(List<EmvCapk> capks, Context context) {
        List<Bitmap> bitmaps = new ArrayList<>();
        PaxGLPage glPage = new PaxGLPage(context);
        int offset = 0;
        for (EmvCapk capk: capks){
            IPage page = glPage.createPage();
            if (offset == 0){
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText("\nCAPK\n")
                                .setGravity(Gravity.CENTER));
            }else if (offset == capks.size()){
                page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));
            }
            //RID
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("RID")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(capk.getRID())
                            .setGravity(Gravity.END)
                            .setWeight(7.0f));

            //KeyID
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Key ID")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(Integer.toString(capk.getKeyID()))
                            .setGravity(Gravity.END)
                            .setWeight(7.0f));

            //HashInd
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Hash Index")
                            .setWeight(4.0f))
                    .addUnit(page.createUnit()
                            .setText(Integer.toString(capk.getHashInd()))
                            .setGravity(Gravity.END)
                            .setWeight(6.0f));

            //arithInd
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Arith Index")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(Integer.toString(capk.getArithInd()))
                            .setGravity(Gravity.END)
                            .setWeight(7.0f));

            //module
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Modules")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(capk.getModule())
                            .setGravity(Gravity.END)
                            .setWeight(7.0f));

            //Exponent
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Exponent")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(capk.getExponent())
                            .setGravity(Gravity.END)
                            .setWeight(7.0f));

            //expDate
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("ExpDate")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(capk.getExpDate())
                            .setGravity(Gravity.END)
                            .setWeight(7.0f));

            //checkSum
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Checksum")
                            .setWeight(4.0f))
                    .addUnit(page.createUnit()
                            .setText(capk.getCheckSum())
                            .setGravity(Gravity.END)
                            .setWeight(6.0f));

            page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));

            offset++;
            bitmaps.add(glPage.pageToBitmap(page,384));
        }
        offset = 0;
        return bitmaps;
    }
}
