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
package com.evp.poslib.gl.imgprocessing;

import android.graphics.Bitmap;
import android.view.View;

import com.evp.poslib.gl.page.IPage;
import com.pax.gl.img.IRgbToMonoAlgorithm;

public interface IImgProcessing {

    byte[] bitmapToJbig(Bitmap bitmap, IRgbToMonoAlgorithm algo);

    Bitmap jbigToBitmap(byte[] jbig);

    byte[] bitmapToMonoDots(Bitmap bitmap, IRgbToMonoAlgorithm algo);

    byte[] bitmapToMonoBmp(Bitmap bitmap, IRgbToMonoAlgorithm algo);

    Bitmap scale(Bitmap bitmap, int w, int h);

    Bitmap generateBarCode(String contents, int width, int height, com.google.zxing.BarcodeFormat format);

    IPage createPage();

    Bitmap pageToBitmap(IPage page, int pageWidth);

    View pageToView(IPage page, int pageWidth);
}

/* Location:           D:\Android逆向助手_v2.2\PaxGL_V1.00.04_20170303.jar
 * Qualified Name:     com.pax.gl.imgprocessing.IImgProcessing
 * JD-Core Version:    0.6.0
 */