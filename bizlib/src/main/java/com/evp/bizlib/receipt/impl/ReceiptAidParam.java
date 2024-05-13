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
import com.evp.bizlib.data.entity.EmvAid;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.commonlib.utils.ResourceUtil;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func2;

import java.util.ArrayList;
import java.util.List;

/**
 * This class works for generate Aid information
 */
@RouterService(interfaces = Func2.class,key = ReceiptConst.RECEIPT_AID,singleton = true)
public class ReceiptAidParam implements Func2<List<EmvAid>,Context, List<Bitmap>> {

    @Override
    public List<Bitmap> call(@NonNull List<EmvAid> aids, Context context) {
        List<Bitmap> bitmaps = new ArrayList<>();
        PaxGLPage glPage = new PaxGLPage(context);
        int offset = 0;
        for (EmvAid emvAid: aids){
            IPage page = glPage.createPage();
            if (offset == 0){
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText("\nAPP\n")
                                .setGravity(Gravity.CENTER));
            }else if (offset == aids.size()){
                page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));
            }
            //appName
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("App Name")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getAppName())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));
            //aid
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("AID")
                            .setWeight(1.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getAid())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //selFlag
            String temp = EmvAid.PART_MATCH == emvAid.getSelFlag() ? "Part match" : "Full match";
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("SelFlag")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(temp)
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //priority
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Priority")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(Integer.toString(emvAid.getPriority()))
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //targetPer
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Target%")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(Integer.toString(emvAid.getTargetPer()))
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //maxTargetPer
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Max Target%")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(Integer.toString(emvAid.getMaxTargetPer()))
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //floorLimitCheck
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Floor Limit Check")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(Integer.toString(emvAid.getFloorLimitCheckFlg()))
                            .setGravity(Gravity.END)
                            .setWeight(2.0f));

            //randTransSel
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("RandTransSel")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(getYesNo(emvAid.getRandTransSel()))
                            .setGravity(Gravity.END)
                            .setWeight(2.0f));

            //velocityCheck
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Velocity Check")
                            .setWeight(3.0f))
                    .addUnit(page.createUnit()
                            .setText(getYesNo(emvAid.getVelocityCheck()))
                            .setGravity(Gravity.END)
                            .setWeight(2.0f));

            //floorLimit
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Floor Limit")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(Long.toString(emvAid.getFloorLimit()))
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //threshold
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Threshold")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(Long.toString(emvAid.getThreshold()))
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //tacDenial
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("TAC Denial")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getTacDenial())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //tacOnline
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("TAC Online")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getTacOnline())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //tacDefault
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("TAC Default")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getTacDefault())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //acquirerId
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Acquirer Id")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getAcquirerId())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //dDOL
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("dDOL")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getDDOL())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            //tDOL
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("tDOL")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getTDOL())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));
            //version
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Version")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getVersion())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));
            //riskManageData
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("Risk Manage Data")
                            .setWeight(2.0f))
                    .addUnit(page.createUnit()
                            .setText(emvAid.getRiskManageData())
                            .setGravity(Gravity.END)
                            .setWeight(3.0f));

            page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));

            offset++;
            bitmaps.add(glPage.pageToBitmap(page,384));
        }
        offset = 0;
        return bitmaps;
    }

    /**
     * convert boolean value to yes or no
     * @param value boolean value
     * @return yes or no
     */
    private String getYesNo(boolean value) {
        return value ? ResourceUtil.getString(R.string.yes) : ResourceUtil.getString(R.string.no);
    }
}
