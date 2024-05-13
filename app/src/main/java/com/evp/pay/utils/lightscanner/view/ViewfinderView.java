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
 * 20190108  	         Steven.S                Create
 * ===========================================================================================
 */

package com.evp.pay.utils.lightscanner.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.ScreenMetricUtils;
import com.evp.pay.utils.lightscanner.tool.LightScannerManager;
import com.evp.payment.evpscb.R;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 * Created by Steven.S on 2018/5/22/0022.
 */
public class ViewfinderView extends View {
    /**
     * 刷新界面的时间
     */
    private static final long ANIMATION_DELAY = 10L;
    /**
     * 不透明度
     */
    private static final int OPAQUE = 0xFF;
    /**
     * 四个绿色边角对应的长度
     */
    private int screenRate;
    /**
     * 四个绿色边角对应的宽度
     */
    private static final int CORNER_WIDTH = 5;
    /**
     * 四条边线的宽度
     */
    private static final int BORDER_LINE_WIDTH = 1;
    /**
     * 扫描框中的中间线的宽度
     */
    private static final int MIDDLE_LINE_WIDTH = 6;
    /**
     * 扫描框中的中间线与扫描框左右的间隙
     */
    private static final int MIDDLE_LINE_PADDING = 5;
    /**
     * 中间那条线每次刷新移动的距离
     */
    private static final int SPEEN_DISTANCE= 5;
    /**
     * 手机的屏幕密度
     */
    private static float density;
    /**
     * 字体大小
     */
    private static final int TEXT_SIZE = 16;
    /**
     * 字体距扫描框上边的距离
     */
    private static final int TEXT_PADDING_TOP = 40;
    /**
     * 画笔对象的引用
     */
    private Paint paint;
    /**
     * 中间滑动线的最顶端位置
     */
    private int slideTop;
    /**
     * 中间滑动线最底端的位置
     */
//    private int slideBottom;

    private Bitmap resultBitmap;
    private final int maskColor;
    private final int resultColor;

    private boolean isFirst;

    /**
     * Instantiates a new Viewfinder view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        density = context.getResources().getDisplayMetrics().density;
        //将像素转换为dp
        screenRate = (int) (15 * density);

        paint = new Paint();
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        resultColor = resources.getColor(R.color.result_view);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Rect frame = getFramingRect();

        //初始化中间线滑动的最上边和最下边
        if(!isFirst){
            isFirst = true;
            slideTop = frame.top;
//            slideBottom = frame.bottom;
        }

        //获取屏幕的宽和高
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        paint.setColor(resultBitmap != null ? resultColor : maskColor);

        //画出扫描框外面的阴影部分，共四部分：扫描框的上面到屏幕的上面；扫描框的左边到屏幕
        //左边；扫描框的右边到屏幕右边；扫描框的下面到屏幕的下面；
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        if(resultBitmap != null){
            paint.setAlpha(OPAQUE);
            canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
        }else{
            //画扫描框边上的角，共8个部分
            paint.setColor(Color.GREEN);
            canvas.drawRect(frame.left, frame.top, frame.left + screenRate, frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top + screenRate, paint);
            canvas.drawRect(frame.right - screenRate, frame.top, frame.right, frame.top + CORNER_WIDTH, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right, frame.top + screenRate, paint);
            canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left + screenRate, frame.bottom, paint);
            canvas.drawRect(frame.left, frame.bottom - screenRate, frame.left + CORNER_WIDTH, frame.bottom, paint);
            canvas.drawRect(frame.right - screenRate, frame.bottom - CORNER_WIDTH, frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - screenRate, frame.right, frame.bottom, paint);

            //画四条边线
            paint.setColor(0X66FFFFFF);
            canvas.drawRect(frame.left, frame.top, frame.left + BORDER_LINE_WIDTH, frame.bottom, paint);
            canvas.drawRect(frame.left + BORDER_LINE_WIDTH, frame.top, frame.right, frame.top + BORDER_LINE_WIDTH, paint);
            canvas.drawRect(frame.left + BORDER_LINE_WIDTH, frame.bottom - BORDER_LINE_WIDTH, frame.right, frame.bottom, paint);
            canvas.drawRect(frame.right - BORDER_LINE_WIDTH, frame.top + BORDER_LINE_WIDTH, frame.right, frame.bottom - BORDER_LINE_WIDTH, paint);

            //绘制中间的线，每次刷新界面，中间的线往下移动SPEEN_DISTANCE
            slideTop += SPEEN_DISTANCE;
            if(slideTop >= frame.bottom - 18){
                slideTop = frame.top;
            }
            Rect lineRect = new Rect();
            lineRect.left = frame.left;
            lineRect.right = frame.right;
            lineRect.top = slideTop;
            lineRect.bottom = slideTop + 18;
            canvas.drawBitmap(((BitmapDrawable)(getResources().getDrawable(R.drawable.qrcode_scan_line))).getBitmap(), null, lineRect, paint );

            //画扫描框下面的字
            paint.setColor(Color.WHITE);
            paint.setTextSize(TEXT_SIZE * density);
            paint.setAlpha(0x40);
            paint.setTypeface(Typeface.create("System", Typeface.NORMAL));
            String text = getResources().getString(R.string.scan_text);
            float textWidth = paint.measureText(text);
            canvas.drawText(text, (width - textWidth)/2, (frame.top - (float)TEXT_PADDING_TOP * density), paint);

            //在非UI线程中刷新UI界面，每个ANIMATION_DELAY时间，刷新指定的范围
            //只刷新扫描框的内容，其他地方不刷新
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    /**
     * Draw viewfinder.
     */
    public void drawViewfinder(){
        resultBitmap = null;
        invalidate(); //刷新界面（UI线程）
    }

    /**
     * Draw a bitmap with the result Points highlighted instead of the live scanning display
     *
     * @param barcode the barcode
     */
    public void drawResultBitmap(Bitmap barcode){
        resultBitmap = barcode;
        invalidate(); //刷新界面（UI线程）
    }

    private Rect getFramingRect(){
        Point screenWH = ScreenMetricUtils.getPrintScreenPixel(FinancialApplication.getApp());
        int rectW = LightScannerManager.WIDTH;
        int rectH = LightScannerManager.HEIGHT;
        int realWidth = rectW > screenWH.x ? screenWH.x : rectW;
        int realHeight = rectH > screenWH.y ? screenWH.y : rectH;

        int left = (screenWH.x - realWidth)/2;
        int top = (screenWH.y - realHeight)/2;
        int right = screenWH.x - left;
        int bottom = screenWH.y - top;
        Rect rect = new Rect(left, top, right, bottom);

        return rect;
    }
}
