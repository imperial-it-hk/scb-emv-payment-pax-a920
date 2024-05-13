/*
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
 * 20190110  	         xieYb                  Create
 */

package com.evp.poslib.gl.convert;


import com.evp.poslib.gl.impl.ConverterGLImp;
import com.evp.poslib.gl.impl.ConverterImp;

/**
 * convert utils
 */
public class ConvertHelper {
    public ConvertHelper() {
    }

    private static IConvert convert;

    /**
     * convert utils init
     * @param useGl whether use Gl convert utils
     */
    public static void init(boolean useGl) {
        if (useGl) {
            convert = new ConverterGLImp();
        } else {
            convert = new ConverterImp();
        }
    }

    public static IConvert getConvert() {
        return convert;
    }
}
