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
 * Date	                 Author	                Action
 * 20200109  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.poslib.print.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.view.Gravity;

import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.evp.poslib.print.entity.PrinterInfo;
import com.evp.poslib.print.exception.PrinterException;
import com.pax.baselink.api.BaseLinkApi;
import com.pax.baselink.api.BaseResp;

import java.util.ArrayList;
import java.util.List;

public class BePrinter extends ABtPrinter {
    private BaseLinkApi baseLinkApi;

    /**
     * init be print
     * @param context context
     * @param printerInfo printerInfo
     */
    public BePrinter(Context context, PrinterInfo printerInfo) {
        this.context = context.getApplicationContext();
        this.printerInfo = printerInfo;
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        baseLinkApi = BaseLinkApi.getInstance(context);
    }

    @Override
    public boolean connect() {
        String identifier = printerInfo.getIdentifier();
        long start = System.currentTimeMillis();
        LogUtils.d(TAG, "start connect to BePrinter:" + identifier + ",start time:" + start);
        //if connect when printer powered off, set timeout for shortest wait.
        boolean beConnect = baseLinkApi.btConnect(identifier, 10);
        long end = System.currentTimeMillis();
        LogUtils.d(TAG, "finish connect to BePrinter:" + identifier + ",end time:" + end);
        LogUtils.d(TAG, "total time:" + (end - start));
        return beConnect;
    }

    @Override
    public void disConnect() {
        baseLinkApi.btDisconnect();
    }

    /**
     * print bitmap
     * @param bitmap bitmap
     * @return print status
     * @throws PrinterException exception
     */
    @Override
    public int print(Bitmap bitmap) throws PrinterException {
        BaseResp<?> baseResp = BaseLinkApi.PvtPrinter.printImage(bitmap);
        short respCode = baseResp.getRespCode();
        if (respCode == BaseResp.SUCCESS) {
            return respCode;
        } else {
            throw new PrinterException(baseResp.respCode, baseResp.respMsg);
        }
    }

    /**
     * print single line
     * @param line single line
     * @return print status
     * @throws PrinterException exception
     */
    @Override
    public int print(String line) throws PrinterException {
        PaxGLPage iPaxGLPage = new PaxGLPage(context);
        IPage page = iPaxGLPage.createPage();
        IPage.ILine.IUnit unit = page.createUnit();
        unit.setGravity(Gravity.CENTER);
        unit.setText(line);
        List<IPage.ILine.IUnit> iUnits = new ArrayList<>();
        iUnits.add(unit);
        for (IPage.ILine.IUnit item : iUnits) {
            page.addLine().addUnit(item);
        }
        Bitmap bitmap = iPaxGLPage.pageToBitmap(page, 384);
        return print(bitmap);
    }
}
