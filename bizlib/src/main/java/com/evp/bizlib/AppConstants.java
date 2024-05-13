package com.evp.bizlib;

public class AppConstants {
    /**
     * The constant for DCC acquirer name
     */
    public static final String DCC_ACQUIRER = "DCC";
    /**
     * The constant for TPN issuer name
     */
    public static final String TPN_ISSUER = "TPN";
    /**
     * The constant for TSC issuer name
     */
    public static final String TSC_ISSUER = "TSC";
    /**
     * The constant for MASTER CARD issuer name
     */
    public static final String MC_ISSUER = "MASTER";
    /**
     * The constant for UPI acquirer name
     */
    public static final String UPI_ACQUIRER = "UPI";
    /**
     * The constant for SCB acquirer name
     */
    public static final String SCB_ACQUIRER = "SCB";
    /**
     * The constant for QR acquirer name
     */
    public static final String QR_ACQUIRER = "SCB QR";
    /**
     * The constant for OLS acquirer name
     */
    public static final String OLS_ACQUIRER = "SCB OLS";
    /**
     * The constant for IPP acquirer name
     */
    public static final String IPP_ACQUIRER = "SCB IPP";

    /**
     * The constant for TPN AID
     */
    public static final byte[] TPN_AID = new byte[]{(byte)0xa0, 0x00, 0x00, 0x06, 0x77, 0x01, 0x01};
    /**
     * The constant for TSC AID
     */
    public static final byte[] TSC_AID = new byte[]{(byte)0xa0, 0x00, 0x00, 0x06, 0x77, 0x01, 0x01, 0x01};
    /**
     * The constant for MasterCard RID
     */
    public static final String MASTER_CARD_RID = "A000000004";
    /**
     * The constant for JCB RID
     */
    public static final String JCB_CARD_RID = "A000000065";
    /**
     * The constant for UPI RID
     */
    public static final String UPI_CARD_RID = "A000000333";
    /**
     * The constant for TPN & TSC RID
     */
    public static final String TPN_CARD_RID = "A000000677";
    /**
     * The constant for MasterCard AID
     */
    public static final String MASTER_CARD_AID = "A0000000041010";
    /**
     * The constant for Maestro AID
     */
    public static final String MAESTRO_AID = "A0000000043060";
    /**
     * Sale type for card payments
     */
    public static final String SALE_TYPE_CARD = "CARD";
    /**
     * Sale type for QR payments C scan B
     */
    public static final String SALE_TYPE_QR = "QR";
    /**
     * Sale type for QR payments B scan C
     */
    public static final String SALE_TYPE_SCAN = "SCAN";
    /**
     * Sale type for QR payments B scan C
     */
    public static final String FUNDING_SRC_ALIPAY = "alipay";
    /**
     * Sale type for QR payments B scan C
     */
    public static final String FUNDING_SRC_WECHAT = "wechat";
    /**
     * Sale type for QR payments B scan C
     */
    public static final String FUNDING_SRC_PROMPTPAY = "promptpay";
    /**
     * Sale type for QR payments B scan C
     */
    public static final String FUNDING_SRC_TRUE_MONEY = "true_money";
    /**
     * Sale type for QR payments B scan C
     */
    public static final String FUNDING_SRC_LINEPAY = "linepay";
    /**
     * Sale type for QR payments B scan C
     */
    public static final String FUNDING_SRC_SHOPEE = "shopee";
    /**
     * Sale type for QR payments B scan C
     */
    public static final String FUNDING_SRC_QRCS = "qrcs";
}
