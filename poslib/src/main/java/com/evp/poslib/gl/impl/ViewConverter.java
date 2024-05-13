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
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.evp.commonlib.utils.LogUtils;
import com.evp.poslib.gl.page.IPage;

import java.util.List;


//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
// from PaxGLPage
//

public class ViewConverter {
    private Context mContext;

    public ViewConverter(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public final View page2View(Context context, IPage page, List<IPage.ILine> lines, int pageWidth) {
        ScrollView scrollView = new ScrollView(context);
        scrollView.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams lpScrollView = new LinearLayout.LayoutParams(pageWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(lpScrollView);
        scrollView.setBackgroundColor(Color.WHITE);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(pageWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.WHITE);
        int width = 0;
        boolean isEmptyLine;
        int emptyCount;
        for (IPage.ILine line : lines) {
            emptyCount = 0;
            ++width;
            LinearLayout lineLayout = new LinearLayout(context);
            lineLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            lineLayout.setOrientation(LinearLayout.HORIZONTAL);
            lineLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

            for (IPage.ILine.IUnit unit : line.getUnits()) {
                float weight = unit.getWeight();
                Bitmap bitmap = unit.getBitmap();
                String text = unit.getText();
                TextView textView;
                if (text != null && text.length() > 0) {
                    textView = new TextView(context);
                    textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
                    if (page.getTypeFace() != null) {
                        try {
                            textView.setTypeface(page.getTypeFace());
                        } catch (Exception e) {
                            LogUtils.e("ViewConverter", "", e);
                        }
                    }

                    SpannableString ss = new SpannableString(unit.getText());
                    ss.setSpan(new AbsoluteSizeSpan(unit.getFontSize()), 0, ss.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.setSpan(new StyleSpan(unit.getTextStyle() & 0x0F), 0, ss.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    if ((unit.getTextStyle() & 0xF0) == IPage.ILine.IUnit.UNDERLINE) {
                        ss.setSpan(new UnderlineSpan(), 0, ss.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    textView.setText(ss);
                    textView.setTextColor(Color.BLACK);
                    textView.setTextSize((float) unit.getFontSize());
                    textView.setGravity(unit.getGravity());

                    lineLayout.addView(textView);
                } else if (bitmap != null) {
                    ImageView imageView = new ImageView(context);
                    LinearLayout.LayoutParams lpImageView = new LinearLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight(), 0.0F);
                    lpImageView.setMargins(0, 10, 0, 10);
                    imageView.setLayoutParams(lpImageView);
                    imageView.setScaleType(ImageView.ScaleType.CENTER);
                    lineLayout.setGravity(unit.getGravity());

                    imageView.setImageDrawable(new BitmapDrawable(getContext().getResources(), bitmap));
                    lineLayout.addView(imageView);
                }else {
                    emptyCount++;
                }
            }

            lineLayout.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
            linearLayout.addView(lineLayout);
            isEmptyLine = emptyCount == line.getUnits().size();
            if (width != 1 && !isEmptyLine) {
                int spaceAdjustment = page.getLineSpaceAdjustment();
                int topSpaceAdjustment;
                if ((topSpaceAdjustment = line.getTopSpaceAdjustment()) != '\uffff') {
                    spaceAdjustment = topSpaceAdjustment;
                }

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) lineLayout.getLayoutParams();
                layoutParams.topMargin = spaceAdjustment;
                lineLayout.setLayoutParams(layoutParams);
            }
        }

        scrollView.addView(linearLayout);
        return scrollView;
    }
}

