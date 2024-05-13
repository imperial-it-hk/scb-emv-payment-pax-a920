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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;

import com.evp.poslib.gl.impl.ViewConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * used for create Bitmap view
 */
public class PaxGLPage {
    private Context context;

    public PaxGLPage(Context context) {
        this.context = context;
    }

    public Bitmap pageToBitmap(IPage page, int pageWidth) {
        View view = pageToView(page, pageWidth);
        return view.getDrawingCache();
    }

    public View pageToView(IPage page, int pageWidth) {
        ViewConverter viewConverter = new ViewConverter(this.context);
        View view;
        view = viewConverter.page2View(viewConverter.getContext(), page, page.getLines(), pageWidth);
        view.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, pageWidth, view.getMeasuredHeight());
        view.buildDrawingCache();
        return view;
    }

    public IPage createPage() {
        return new Page();
    }

    private static class Page implements IPage {
        private Typeface typeface;
        private List<ILine> lines;
        private int spacingAdd;

        Page() {
            this.typeface = null;
            this.spacingAdd = 0;
            this.lines = new ArrayList<>();
        }

        @Override
        public ILine addLine() {
            Line theLastLine = new Line();
            this.lines.add(theLastLine);
            return theLastLine;
        }

        @Override
        public List<ILine> getLines() {
            return this.lines;
        }

        @Override
        public Typeface getTypeFace() {
            return this.typeface;
        }

        @Override
        public void setTypeFace(Typeface typeface) {
            this.typeface = typeface;
        }

        @Override
        public ILine.IUnit createUnit() {
            return new Unit();
        }

        @Override
        public void adjustLineSpace(int spacingAdd) {
            this.spacingAdd = spacingAdd;
        }

        @Override
        public int getLineSpaceAdjustment() {
            return this.spacingAdd;
        }
    }

    private static class Line implements IPage.ILine {
        private List<IUnit> units;
        private int lineSpace;

        private Line() {
            this.lineSpace = '\uffff';
            this.units = new ArrayList<>();
        }

        @Override
        public List<IUnit> getUnits() {
            return this.units;
        }

        @Override
        public IPage.ILine addUnit(IUnit unit) {
            this.units.add(unit);
            return this;
        }

        public IPage.ILine adjustTopSpace(int spacingAdd) {
            this.lineSpace = spacingAdd;
            return this;
        }

        public int getTopSpaceAdjustment() {
            return this.lineSpace;
        }
    }

    private static class Unit implements IPage.ILine.IUnit {
        private String text;
        private Bitmap bitmap;
        private int fontSize;
        private int gravity;
        private int textStyle;
        private float weight;

        private Unit() {
            this.textStyle = NORMAL;
            this.weight = 1.0F;
            this.fontSize = 24;
            this.gravity = Gravity.START;
            this.text = " ";
            this.bitmap = null;
        }

        public String getText() {
            return this.text;
        }

        public IPage.ILine.IUnit setText(String text) {
            this.text = text;
            this.bitmap = null;
            return this;
        }

        public Bitmap getBitmap() {
            return this.bitmap;
        }

        public IPage.ILine.IUnit setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.text = "";
            return this;
        }

        public int getFontSize() {
            return this.fontSize;
        }

        public IPage.ILine.IUnit setFontSize(int fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public int getGravity() {
            return this.gravity;
        }

        public IPage.ILine.IUnit setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public int getTextStyle() {
            return this.textStyle;
        }

        public IPage.ILine.IUnit setTextStyle(int textStyle) {
            this.textStyle = textStyle;
            return this;
        }

        public float getWeight() {
            return this.weight;
        }

        public IPage.ILine.IUnit setWeight(float weight) {
            this.weight = weight;
            return this;
        }
    }
}

