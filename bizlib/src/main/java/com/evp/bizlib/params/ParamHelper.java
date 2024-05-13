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
 * 20200318  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.bizlib.params;

import com.evp.bizlib.R;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.sp.SharedPrefUtil;
import com.evp.commonlib.utils.ResourceUtil;
import com.pax.dal.entity.EPedType;
import com.pax.dal.entity.EReaderType;

/**
 * business parameters
 */
public class ParamHelper {
    private static SharedPrefUtil defaultSp;
    static {
        defaultSp = new SharedPrefUtil(BaseApplication.getAppContext());
    }

    /**
     * Whether Internal Contactless Mode
     * @return Mode status
     */
    public static boolean isClssInternal(){
        String currentClssMode = defaultSp.getString(ResourceUtil.getString(R.string.EDC_CLSS_MODE));
        String internal = BaseApplication.getAppContext().getResources().getStringArray(R.array.edc_clss_mode_entries)[0];
        return internal.equals(currentClssMode);
    }
    /**
     * Whether External Contactless Mode
     * @return Mode status
     */
    public static boolean isClssExternal(){
        String currentClssMode = defaultSp.getString(ResourceUtil.getString(R.string.EDC_CLSS_MODE));
        String external = BaseApplication.getAppContext().getResources().getStringArray(R.array.edc_clss_mode_entries)[1];
        return external.equals(currentClssMode);
    }
    /**
     * Whether Support Both Internal and External Contactless Mode
     * @return Mode status
     */
    public static boolean isClssBothSupport(){
        String currentClssMode = defaultSp.getString(ResourceUtil.getString(R.string.EDC_CLSS_MODE));
        String clssBoth = BaseApplication.getAppContext().getResources().getStringArray(R.array.edc_clss_mode_entries)[2];
        return clssBoth.equals(currentClssMode);
    }
    /**
     * Whether Internal Contactless Detected
     * @return Mode status
     */
    public static boolean isClssInternalResult(){
        return defaultSp.getInt(ResourceUtil.getString(R.string.RESULT_READER_TYPE)) == EReaderType.PICC.ordinal();
    }
    /**
     * Whether External Contactless Detected
     * @return Mode status
     */
    public static boolean isClssExternalResult(){
        return defaultSp.getInt(ResourceUtil.getString(R.string.RESULT_READER_TYPE)) == EReaderType.PICCEXTERNAL.ordinal();
    }
    /**
     * Whether Internal Ped Mode
     * @return Mode status
     */
    public static boolean isInternalPed(){
        String currentPedMode = defaultSp.getString(ResourceUtil.getString(R.string.EDC_PED_MODE));
        String internal = BaseApplication.getAppContext().getResources().getStringArray(R.array.edc_ped_mode_entries)[0];
        return internal.equals(currentPedMode);
    }
    /**
     * Whether External TypeA Ped Mode
     * @return Mode status
     */
    public static boolean isExternalTypeAPed(){
        String currentPedMode = defaultSp.getString(ResourceUtil.getString(R.string.EDC_PED_MODE));
        String externalTypeA = BaseApplication.getAppContext().getResources().getStringArray(R.array.edc_ped_mode_entries)[1];
        return externalTypeA.equals(currentPedMode);
    }
    /**
     * Whether External TypeB Ped Mode
     * @return Mode status
     */
    public static boolean isExternalTypeBPed(){
        String currentPedMode = defaultSp.getString(ResourceUtil.getString(R.string.EDC_PED_MODE));
        String externalTypeB = BaseApplication.getAppContext().getResources().getStringArray(R.array.edc_ped_mode_entries)[2];
        return externalTypeB.equals(currentPedMode);
    }
    /**
     * Whether External TypeC Ped Mode
     * @return Mode status
     */
    public static boolean isExternalTypeCPed(){
        String currentPedMode = defaultSp.getString(ResourceUtil.getString(R.string.EDC_PED_MODE));
        String externalTypeC = BaseApplication.getAppContext().getResources().getStringArray(R.array.edc_ped_mode_entries)[3];
        return externalTypeC.equals(currentPedMode);
    }

    /**
     * Current Ped
     * @return EPedType
     */
    public static EPedType getCurrentPed(){
        EPedType pedType = EPedType.INTERNAL;
        if (isExternalTypeAPed()){
            pedType = EPedType.EXTERNAL_TYPEA;
        }else if (isExternalTypeBPed()){
            pedType = EPedType.EXTERNAL_TYPEB;
        }else if (isExternalTypeCPed()){
            pedType = EPedType.EXTERNAL_TYPEC;
        }
        return  pedType;
    }

    /**
     * Master Key Index
     * @return index value
     */
    public static int getMkIndex() {
        return defaultSp.getInt(BaseApplication.getAppContext().getResources().getString(R.string.MK_INDEX), 0);
    }

}
