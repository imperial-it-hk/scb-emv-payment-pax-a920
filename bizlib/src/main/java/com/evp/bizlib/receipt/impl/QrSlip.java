package com.evp.bizlib.receipt.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;

import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func7;

import java.util.ArrayList;
import java.util.List;

@RouterService(interfaces = Func7.class, key = ReceiptConst.QR_SLIP, singleton = true)
public class QrSlip implements Func7<Context, Bitmap, Bitmap, String, String, String, String, List<Bitmap>> {

    @Override
    public List<Bitmap> call(Context context, Bitmap qrBitmapStr, Bitmap brandBitmapStr, String amount, String currency, String date, String time) {
        List<Bitmap> bitmaps = new ArrayList<>();
        PaxGLPage glPage = new PaxGLPage(context);
        IPage page = glPage.createPage();

        //Payment Logo
        if (brandBitmapStr != null) {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setBitmap(brandBitmapStr)
                            .setGravity(Gravity.CENTER));
        }

        if (qrBitmapStr != null) {
            //Payment QR
            page.addLine()
                    .addUnit(page.createUnit()
                            .setBitmap(qrBitmapStr)
                            .setGravity(Gravity.CENTER));
        }

        // date & time
        String dateString = "DATE: " + date;
        String timeString = "TIME: " + time;
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(dateString)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(4.0f))
                .addUnit(page.createUnit()
                        .setText(timeString)
                        .setGravity(Gravity.END)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(6.0f));

        // Currency Amount
        String temp = "AMT: " + currency;
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(4.0f))
                .addUnit(page.createUnit()
                        .setText(amount)
                        .setGravity(Gravity.END)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(6.0f));

        page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));

        bitmaps.add(glPage.pageToBitmap(page, 384));
        return bitmaps;
    }
}
