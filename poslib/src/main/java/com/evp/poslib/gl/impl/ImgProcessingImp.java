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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.poslib.gl.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.evp.poslib.gl.imgprocessing.IImgProcessing;
import com.evp.poslib.gl.page.IPage;
import com.pax.gl.img.IRgbToMonoAlgorithm;
import com.pax.gl.img.ImgProcessing;


class ImgProcessingImp implements IImgProcessing {

    private ImgProcessing imgProcessing;
    private PaxGLPage paxGLPage;

    ImgProcessingImp(Context context) {
        imgProcessing = ImgProcessing.getInstance(context);
        paxGLPage = new PaxGLPage(context);
    }

    @Override
    public byte[] bitmapToJbig(Bitmap bitmap, IRgbToMonoAlgorithm algo) {
        return imgProcessing.bitmapToJbig(bitmap, algo);
    }

    @Override
    public Bitmap jbigToBitmap(byte[] jbig) {
        return imgProcessing.jbigToBitmap(jbig);
    }

    @Override
    public byte[] bitmapToMonoDots(Bitmap bitmap, IRgbToMonoAlgorithm algo) {
        return imgProcessing.bitmapToMonoDots(bitmap, algo);
    }

    @Override
    public byte[] bitmapToMonoBmp(Bitmap bitmap, IRgbToMonoAlgorithm algo) {
        return imgProcessing.bitmapToMonoBmp(bitmap, algo);
    }

    @Override
    public Bitmap scale(Bitmap bitmap, int w, int h) {
        return imgProcessing.scale(bitmap, w, h);
    }

    @Override
    public Bitmap generateBarCode(String contents, int width, int height, com.google.zxing.BarcodeFormat format) {
        return imgProcessing.generateBarCode(contents, width, height, format);
    }

    @Override
    public IPage createPage() {
        return paxGLPage.createPage();
    }

    @Override
    public Bitmap pageToBitmap(IPage page, int pageWidth) {
        return paxGLPage.pageToBitmap(page, pageWidth);
    }

    @Override
    public View pageToView(IPage page, int pageWidth) {
        return paxGLPage.pageToView(page, pageWidth);
    }
}
