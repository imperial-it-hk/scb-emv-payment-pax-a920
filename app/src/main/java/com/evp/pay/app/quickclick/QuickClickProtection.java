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
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.pay.app.quickclick;

/**
 * a 500ms-quick-click-protection singleton
 */
public class QuickClickProtection extends AQuickClickProtection {
    private static QuickClickProtection quickClickProtection;

    private QuickClickProtection(long timeoutMs) {
        super(timeoutMs);
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static synchronized QuickClickProtection getInstance() {
        if (quickClickProtection == null) {
            quickClickProtection = new QuickClickProtection(600);
        }

        return quickClickProtection;
    }
}
