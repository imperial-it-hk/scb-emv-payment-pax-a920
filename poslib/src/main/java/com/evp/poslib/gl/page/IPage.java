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

package com.evp.poslib.gl.page;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// from PaxGLPage
//

import android.graphics.Bitmap;
import android.graphics.Typeface;

import java.util.List;

/**
 * this interface is used for generate receipt
 */
public interface IPage {

    ILine addLine();

    ILine.IUnit createUnit();

    List<ILine> getLines();

    Typeface getTypeFace();

    void setTypeFace(Typeface typeface);

    void adjustLineSpace(int spacingAdd);

    int getLineSpaceAdjustment();

    interface ILine {
        List<IUnit> getUnits();

        ILine addUnit(ILine.IUnit unit);

        ILine adjustTopSpace(int spacingAdd);

        int getTopSpaceAdjustment();

        interface IUnit {
            int NORMAL = Typeface.NORMAL;
            int BOLD = Typeface.BOLD;
            int ITALIC = Typeface.ITALIC;
            int BOLD_ITALIC = Typeface.BOLD_ITALIC;
            int UNDERLINE = 1 << 4;

            String getText();

            IUnit setText(String var1);

            Bitmap getBitmap();

            IUnit setBitmap(Bitmap bitmap);

            int getFontSize();

            IUnit setFontSize(int fontSize);

            int getGravity();

            IUnit setGravity(int gravity);

            IUnit setTextStyle(int textStyle);

            int getTextStyle();

            float getWeight();

            IUnit setWeight(float weight);
        }
    }
}
