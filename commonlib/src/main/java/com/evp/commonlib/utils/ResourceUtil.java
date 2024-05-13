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
 * 20190725  	         xieYb                  Create
 * ===========================================================================================
 */

package com.evp.commonlib.utils;

import android.annotation.TargetApi;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.evp.commonlib.application.BaseApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

public class ResourceUtil {

    private static Resources res;

    static {
        res = BaseApplication.getAppContext().getResources();
    }

    private ResourceUtil() {
        //do nothing
    }

    /**
     * get color value by resource id
     *
     * @param resId resource id defined in colors.xml
     * @return color value in hex
     */
    public static int getColor(int resId) {
        return ContextCompat.getColor(BaseApplication.getAppContext(), resId);
    }

    /**
     * get string value by resource id
     *
     * @param resId resource id defined in strings.xml
     * @return string value
     */
    public static String getString(int resId) {
        return res.getString(resId);
    }

    /**
     * get Drawable by resource id
     *
     * @param resId Drawable resource id
     * @return Drawable
     */
    public static Drawable getDrawable(int resId) {
        return ContextCompat.getDrawable(BaseApplication.getAppContext(), resId);
    }

    /**
     * convert dimension size to raw pixels
     *
     * @param dpValue size in dimension
     * @return size in raw pixels
     */
    public static int dp2px(float dpValue) {
        return (int) (dpValue * res.getDisplayMetrics().density + 0.5f);
    }

    /**
     * convert raw pixels size to dimension
     *
     * @param pxValue size in raw pixels
     * @return value in dimension
     */
    public static int px2dp(float pxValue) {
        return (int) (pxValue / res.getDisplayMetrics().density + 0.5f);
    }

    /**
     * get size in raw pixels by resource id defined in dimens.xml
     *
     * @param resId resource id defined in dimens.xml
     * @return size in raw pixels
     */
    public static int getDimens(int resId) {
        return res.getDimensionPixelSize(resId);
    }

    /**
     * get jsonString from asset file
     *
     * @param fileName fileName in asset
     * @return jsonString
     */
    public static String getAssetFileString(String fileName) {
        StringBuilder builder = new StringBuilder();
        try (InputStreamReader reader = new InputStreamReader(BaseApplication.getAppContext().getAssets().open(fileName));
             BufferedReader bufferedReader = new BufferedReader(reader);
        ) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            LogUtils.e("",e);
        }
        return builder.toString();
    }

    /**
     * get resource by Locale(only works on api 17 or higher)
     *
     * @param resId  resource id
     * @param locale locale
     * @return string value
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static String getStringByLocalPlus17(int resId, Locale locale) {
        Configuration configuration = new Configuration(res.getConfiguration());
        configuration.setLocale(locale);
        return BaseApplication.getAppContext().createConfigurationContext(configuration).getResources().getString(resId);
    }

    public static boolean isScreenOrientationPortrait() {
        return res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static Bitmap getImageFromAssetsFile(String fileName){
        Bitmap image = null;
        AssetManager am = res.getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            LogUtils.e("",e);
        }

        return image;
    }
}
