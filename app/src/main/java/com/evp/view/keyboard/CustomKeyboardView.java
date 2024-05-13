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
package com.evp.view.keyboard;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.core.content.ContextCompat;

import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.payment.evpscb.R;

import java.lang.reflect.Method;
import java.util.List;

/**
 * The type Custom keyboard view.
 */
public class CustomKeyboardView extends KeyboardView {

    private Drawable mKeyBgDrawable;
    private Drawable mOpKeyBgDrawable;
    private Paint paint = new Paint();
    private Context mContext;
    private Rect rect;
    private int primaryColor;
    private int secondaryColor;

    /**
     * Instantiates a new Custom keyboard view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CustomKeyboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new Custom keyboard view.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public CustomKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initResources();
    }

    private void initResources() {
        mKeyBgDrawable = ContextCompat.getDrawable(mContext, R.drawable.btn_bg_dark);
        primaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.PRIMARY_COLOR));
        secondaryColor = Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.SECONDARY_COLOR));
        mOpKeyBgDrawable = new ColorDrawable(primaryColor);
        rect = new Rect();
    }

    @Override
    public void onDraw(Canvas canvas) {
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        for (Keyboard.Key key : keys) {
            canvas.save();

            int initDrawY = key.y;
            rect.left = key.x;
            rect.top = initDrawY;
            rect.right = key.x + key.width;
            rect.bottom = key.y + key.height;
            canvas.clipRect(rect);

            if (key.codes != null && key.codes.length != 0) {
                switch (key.codes[0]) {
                    case -1000:
                    case -1001:
                    case -1002:
                        drawIconText(canvas, key, initDrawY, rect);
                        break;
                    case -5:
                    case -4:
                    case -3:
                    case -92:
                        drawIcon(canvas, key, rect, true);
                        drawText(canvas, key, initDrawY, true);
                        break;
                    default:
                        drawIcon(canvas, key, rect, false);
                        drawText(canvas, key, initDrawY, false);
                        break;
                }
            } else {
                drawIcon(canvas, key, rect, false);
                drawText(canvas, key, initDrawY, false);
            }
            canvas.restore();
        }
    }

    private void drawIconText(Canvas canvas, Keyboard.Key key, int initDrawY, Rect rect) {
        Drawable drawable = mKeyBgDrawable;
        int[] state = key.getCurrentDrawableState();
        drawable.setState(state);
        drawable.setBounds(rect);
        drawable.draw(canvas);

        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(mContext.getResources().getDimension(R.dimen.font_button));
        paint.setColor(Color.BLACK);

        int textHeight = (int) (paint.getTextSize() - paint.descent());
        int minWidth = (int) (Math.min(key.width, key.height - textHeight) - mContext.getResources().getDimension(R.dimen.margin_icon));

        // middle
        Rect iconRect = new Rect();
        iconRect.left = key.x + (key.width - minWidth) / 2;
        iconRect.top = initDrawY;
        iconRect.right = key.x + key.width - (key.width - minWidth) / 2;
        iconRect.bottom = initDrawY + minWidth;

        Bitmap icon = null;
        if (key.codes[0] == -1000) {
            icon = ConfigUtils.getInstance().getResourceFile(ConfigUtils.getInstance().getDeviceConf(ConfigConst.KEYBOARD_ICON_FOR_CSB));
        } else if (key.codes[0] == -1001) {
            icon = ConfigUtils.getInstance().getResourceFile(ConfigUtils.getInstance().getDeviceConf(ConfigConst.KEYBOARD_ICON_FOR_BSC));
        } else if (key.codes[0] == -1002) {
            icon = ConfigUtils.getInstance().getResourceFile(ConfigUtils.getInstance().getDeviceConf(ConfigConst.KEYBOARD_ICON_FOR_CARD));
        }
        if (icon != null) {
            drawable = new BitmapDrawable(getResources(), icon);
            drawable.setBounds(iconRect);
            canvas.drawColor(secondaryColor);
            drawable.draw(canvas);
        } else {
            key.icon.setState(state);
            key.icon.setBounds(iconRect);
            canvas.drawColor(secondaryColor);
            key.icon.draw(canvas);
        }

        String label = null;
        if (key.codes[0] == -1000) {
            label = ConfigUtils.getInstance().getString("labelKeyboardIconForQrCsB");
        } else if (key.codes[0] == -1001) {
            label = ConfigUtils.getInstance().getString("labelKeyboardIconForQrBsC");
        } else if (key.codes[0] == -1002) {
            label = ConfigUtils.getInstance().getString("labelKeyboardIconForCard");
        }
        if (label != null) {
            canvas.drawText(
                    label,
                    key.x + (key.width / 2),
                    initDrawY + key.height - textHeight / 2,
                    paint
            );
        }
    }

    private void drawIcon(Canvas canvas, Keyboard.Key key, Rect rect, Boolean optionColor) {
        Drawable drawable = null;
        if (key.codes != null && key.codes.length != 0) {
            if (optionColor) {
                drawable = mOpKeyBgDrawable;
            } else {
                drawable = mKeyBgDrawable;
            }
        }

        if (drawable != null && null == key.icon) {
            int[] state = key.getCurrentDrawableState();
            drawable.setState(state);
            drawable.setBounds(rect);
            drawable.draw(canvas);
        }

        if (optionColor) {
            canvas.drawColor(primaryColor);
        }

        if (key.icon != null && optionColor) {
            key.icon.draw(canvas);
        }

        if (key.icon != null && !optionColor) {
            int[] state = key.getCurrentDrawableState();
            canvas.drawColor(secondaryColor);
            key.icon.setState(state);
            canvas.drawColor(secondaryColor);
            key.icon.setBounds(rect);
            canvas.drawColor(secondaryColor);
            key.icon.draw(canvas);
            Bitmap icon = null;
            if (key.codes[0] == -1000) {
                icon = ConfigUtils.getInstance().getResourceFile(ConfigUtils.getInstance().getDeviceConf(ConfigConst.KEYBOARD_ICON_FOR_CSB));
            } else if (key.codes[0] == -1001) {
                icon = ConfigUtils.getInstance().getResourceFile(ConfigUtils.getInstance().getDeviceConf(ConfigConst.KEYBOARD_ICON_FOR_BSC));
            } else if (key.codes[0] == -1002) {
                icon = ConfigUtils.getInstance().getResourceFile(ConfigUtils.getInstance().getDeviceConf(ConfigConst.KEYBOARD_ICON_FOR_CARD));
            }
            if (icon != null) {
                drawable = new BitmapDrawable(getResources(), icon);
                drawable.setBounds(rect);
                drawable.draw(canvas);
            } else {
                state = key.getCurrentDrawableState();
                key.icon.setState(state);
                key.icon.setBounds(rect);
                key.icon.draw(canvas);
            }
        }
    }

    private void drawText(Canvas canvas, Keyboard.Key key, int initDrawY, Boolean optionColor) {
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_key));
        paint.setColor(optionColor ? Color.WHITE : Color.BLACK);
        if (key.label != null && key.codes[0] != -5 && key.codes[0] != -4 && key.codes[0] != -3 && key.codes[0] != -92 && key.codes[0] != -91 && key.codes[0] != -93) {
            canvas.drawColor(secondaryColor);
        }
        if (key.label != null) {
            String label = key.label.toString();
            if (key.codes[0] == -93)
                label = ConfigUtils.getInstance().getString("buttonPay");
            canvas.drawText(
                    label,
                    key.x + (key.width / 2),
                    initDrawY + (key.height + paint.getTextSize() - paint.descent()) / 2,
                    paint
            );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        //AET-245 246
        //just ignore all multi-touch
        return me.getPointerCount() > 1 || onModifiedTouchEvent(me, true);
    }

    private boolean onModifiedTouchEvent(MotionEvent me, boolean possiblePoly) {
        try {
            Method method = getClass().getSuperclass().getDeclaredMethod("onModifiedTouchEvent", me.getClass(), boolean.class);
            method.setAccessible(true);
            return (boolean) method.invoke(this, me, possiblePoly);
        } catch (Exception e) {
            LogUtils.e("CKV", "", e);
        }
        return false;
    }

    /**
     * to resolve the problem that width use match_parent doesn't match_parent in Aries8
     *
     * @param widthMeasureSpec  CustomKeyboardView's width measureSpec which determined by it's parent and self LayoutParams
     * @param heightMeasureSpec CustomKeyboardView's height measureSpec which determined by it's parent and self LayoutParams
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, getMeasuredHeight());
    }
}
