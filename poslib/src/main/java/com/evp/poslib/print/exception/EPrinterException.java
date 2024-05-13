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
 * 20200109  	         xieYb                  Create
 * ===========================================================================================
 */
package com.evp.poslib.print.exception;

import com.pax.dal.utils.Utils;

public enum EPrinterException {
    NEPTUNE_ERROR(200, "neptune error", "neptune异常"),
    PRINTER_ERROR(201, "printer error", "打印机错误"),
    PRINTER_CANCEL(202, "printer cancel", "打印取消"),
    PRINTER_BUSY(1, "Printer busy", "打印机忙"),
    PRINTER_OUT_OF_PAPER(2, "Printer out of paper", "打印机缺纸"),
    PRINTER_PACKET_FORMAT_ERROR(3, "Print packet format error", "打印数据包格式错"),
    PRINTER_FAULT(4, "Printer failure", "打印机故障"),
    PRINTER_OVERHEAT(8, "Printer overheats", "打印机过热"),
    PRINTER_VOLTAGE_LOW(9, "Printer voltage too low", "打印机电压过低"),
    PRINTER_NOT_COMPLETE(-16, "Printing is not complete", "打印未完成"),
    PRINTER_CUTTER_ERROR(-6, "Printer cutter error", "切刀异常"),
    PRINTER_COVER_OPEN(-5, "Printer cover opening error", "开盖错误"),
    PRINTER_PACKET_TOO_LONG(-2, "Printer packet too long", "数据包过长"),
    PRINTER_UNKNOW_ERROR(-1000, "Unknow Error", "未知错误");
    private int errCodeFromBasement;
    private String errMsgCn;
    private String errMsgEn;

    /**
     * init exception info
     * @param errCodeFromBasement error code
     * @param errMsgEn english error message
     * @param errMsnCn chinese error message
     */
    private EPrinterException(int errCodeFromBasement, String errMsgEn, String errMsnCn) {
        this.errCodeFromBasement = errCodeFromBasement;
        this.errMsgCn = errMsnCn;
        this.errMsgEn = errMsgEn;
    }

    /**
     * get error code
     * @return error code
     */
    public int getErrCodeFromBasement() {
        return this.errCodeFromBasement;
    }

    /**
     * get error message
     * @return error message
     */
    public String getErrMsg() {
        return Utils.isZh() ? this.errMsgCn : this.errMsgEn;
    }
}
