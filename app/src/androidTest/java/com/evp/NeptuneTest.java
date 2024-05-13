/*
 *
 *  ============================================================================
 *  PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *  This software is supplied under the terms of a license agreement or nondisclosure
 *  agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *  disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2019 -? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 *  Description:
 *  Revision History:
 *  Date	             Author	                Action
 *  20190409   	     ligq           	Create/Add/Modify/Delete
 *  ============================================================================
 *
 */
package com.evp;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.evp.poslib.neptune.Sdk;
import com.pax.dal.IDAL;
import com.pax.dal.IDalCommManager;
import com.pax.dal.entity.ERoute;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * The type Neptune test.
 */
@RunWith(AndroidJUnit4.class)
public class NeptuneTest {
    /**
     * Test set route.
     */
    @Test
    public void testSetRoute() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        IDAL dal = Sdk.getInstance().getDal(appContext);
        IDalCommManager commManager = dal.getCommManager();
        commManager.setRoute("202.82.149.138", ERoute.WIFI);
    }
}
