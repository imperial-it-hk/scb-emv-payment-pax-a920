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
package com.evp.settings.spupgrader;

import com.evp.settings.SharedPref;

/**
 * The type Upgrade 1 to 2.
 */
/*
Called from DbUpgrader by reflection
 */
public class Upgrade1To2 extends SpUpgrader {

    @Override
    public void upgrade(SharedPref sp) {
        sp.putString("TEST", "sd"); // FIXME should be SysParam Tag
    }
}
