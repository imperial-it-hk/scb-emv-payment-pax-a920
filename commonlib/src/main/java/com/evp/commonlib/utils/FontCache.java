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
 * 20200327  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.commonlib.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.ArrayMap;

/**
 * get cached Typeface
 */
public class FontCache {
    /**
     * name of printer font
     */
    public static final String FONT_NAME = "verdana.ttf";
    /**
     * font cache map
     */
    private static ArrayMap<String,Typeface> fontCache= new ArrayMap<String,Typeface>();

    /**
     * get cached Typeface
     * @param name font name
     * @param context context
     * @return Typeface
     */
    public static Typeface get(String name, Context context){
        Typeface tf = fontCache.get(name);
        if (tf == null){
            tf = Typeface.createFromAsset(context.getAssets(),name);
            fontCache.put(name,tf);
        }
        return tf;
    }
}
