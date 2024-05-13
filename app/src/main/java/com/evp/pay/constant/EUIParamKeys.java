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
package com.evp.pay.constant;

import com.evp.pay.trans.action.ActionInputTransData;

/**
 * The enum Eui param keys.
 */
public enum EUIParamKeys {
    /**
     * 提示信息1
     */
    PROMPT_1,
    /**
     * 提示信息2
     */
    PROMPT_2,
    /**
     * 输入1数据类型, {@link ActionInputTransData.EInputType}
     */
    INPUT_TYPE,
    /**
     * 输入1数据最大长度
     */
    INPUT_MAX_LEN,
    /**
     * 输入数据最小长度
     */
    INPUT_MIN_LEN,
    /**
     * 显示内容
     */
    CONTENT,
    /**
     * transaction type
     */
    TRANS_TYPE,
    /**
     * 交易金额
     */
    TRANS_AMOUNT,
    /**
     * tip amount
     */
    TIP_AMOUNT,
    /**
     * 交易日期
     */
    TRANS_DATE,

    /**
     * 寻卡界面类型
     */
    SEARCH_CARD_UI_TYPE,

    /**
     * 是否可直接撤销最后一笔交易
     */
    GET_LAST_TRANS_UI,
    /**
     * 电子签名特征码
     */
    SIGN_FEATURE_CODE,

    /**
     * 列表1的值
     */
    ARRAY_LIST_1,
    /**
     * 列表2的值
     */
    ARRAY_LIST_2,

    /**
     * 导航栏抬头
     */
    NAV_TITLE,
    /**
     * 导航栏是否显示返回按钮
     */
    NAV_BACK,
    /**
     * 寻卡模式
     */
    CARD_SEARCH_MODE,
    /**
     * 寻卡界面显示授权码
     */
    AUTH_CODE,
    /**
     * 寻卡界面刷卡提醒
     */
    SEARCH_CARD_PROMPT,
    /**
     * 界面定时器
     */
    TIKE_TIME,
    /**
     * 卡号
     */
    PANBLOCK,
    /**
     * 凭密
     */
    SUPPORTBYPASS,
    /**
     * 输密类型
     */
    ENTERPINTYPE,
    /**
     * Options eui param keys.
     */
    OPTIONS,
    /**
     * Rsa pin key eui param keys.
     */
    RSA_PIN_KEY,
    /**
     * 输入内容自动补零
     */
    INPUT_PADDING_ZERO,
    /**
     * 交易查询界面支持交易
     */
    SUPPORT_DO_TRANS,
    /**
     * 原交易小费
     */
    ORI_TIPS,

    /**
     * has tip
     */
    HAS_TIP,

    /**
     * tip percent
     */
    TIP_PERCENT,
    /**
     * card mode
     */
    CARD_MODE,

    /**
     * acquirer name
     */
    ACQUIRER_NAME,

    /**
     * issuer name
     */
    ISSUER_NAME,

    /**
     * print bitmap
     */
    BITMAP,

    /**
     * from which activity
     */
    FROM_ACTIVITY,
    /**
     * Receipt QR code
     */
    RECEIPT_QR_CODE,

    /**
     * Receipt bytes eui param keys.
     */
    RECEIPT_BYTES,

    /**
     * Transdata eui param keys.
     */
    TRANSDATA,

    /**
     * Index of PIN encryption key
     */
    PIN_KEY_INDEX,

    /**
     * DCC domestic amount including currency char
     */
    DCC_DOMESTIC_AMOUNT,

    /**
     * DCC foreign amount including currency char
     */
    DCC_FOREIGN_AMOUNT,
    IS_QR_SCAN,
    /**
     * Installment mode
     */
    IPP_OLS_TYPE,
    TRANS_POINT,

    /**
     * Print type
     */
    IS_REPRINT,
    PRINT_TYPE,
    CURRENCY,
    FUNDING_SOURCE,
    IS_INVOKE,
    AUTO_SETTLE
}
