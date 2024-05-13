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
package com.evp.pay.constant;

import com.evp.payment.evpscb.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * temp workaround for store AD info, can be replaced with real-time data easily
 */
public class AdConstants {
    private static final Map<String, String> AD = new LinkedHashMap<>();
    private static final List<Integer> ERROR_AD = new ArrayList<>();
    static {
        AD.put("http://www.paxsz.com/testimg/about_banner.png", "http://www.pax.com.cn/index.aspx");
        AD.put("http://www.paxsz.com/Upload/banner/Banner_creative-15515726077.jpg", "http://www.pax.com.cn/bfcp_list_other.aspx?CateID=124");
        AD.put("http://www.paxsz.com/Upload/banner/Homepage_02-11164232229.jpg", "http://www.pax.com.cn/solutions_detail.aspx?id=675");
        ERROR_AD.add(R.drawable.banner1);
        ERROR_AD.add(R.drawable.banner2);
        ERROR_AD.add(R.drawable.banner3);
    }

    private AdConstants() {

    }

    /**
     * Gets ad.
     *
     * @return the ad
     */
    public static Map<String, String> getAd() {
        return AD;
    }

    /**
     * Gets error ad.
     *
     * @return the error ad
     */
    public static List<Integer> getErrorAd() {
        return ERROR_AD;
    }
}
