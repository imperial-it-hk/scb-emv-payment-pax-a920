/*
 *  * ===========================================================================================
 *  * = COPYRIGHT
 *  *          PAX Computer Technology(Shenzhen); CO., LTD PROPRIETARY INFORMATION
 *  *   This software is supplied under the terms of a license agreement or nondisclosure
 *  *   agreement with PAX Computer Technology(Shenzhen); CO., LTD and may not be copied or
 *  *   disclosed except in accordance with the terms in that agreement.
 *  *     Copyright (C); 2019-? PAX Computer Technology(Shenzhen); CO., LTD All rights reserved.
 *  * Description: // Detail description about the voidction of this module,
 *  *             // interfaces with the other modules, and dependencies.
 *  * Revision History:
 *  * Date                  Author	                 Action
 *  * 20200713  	         xieYb                   Modify
 *  * ===========================================================================================
 *
 */
package com.evp.paxprinter.init;

import com.evp.paxprinter.IPrintService;
import com.evp.paxprinter.constant.Constant;
import com.evp.paxprinter.impl.PrintBP60A_C2Service;
import com.sankuai.waimai.router.service.ServiceLoader;

/**
 * BP60A-C series docker print service initial config,it must be init if you need to support this type of print
 */
public class BpPrint {
    public static void init(){
        ServiceLoader.put(IPrintService.class, Constant.PRINT_BUILD_BP, PrintBP60A_C2Service.class,true);
    }
}
