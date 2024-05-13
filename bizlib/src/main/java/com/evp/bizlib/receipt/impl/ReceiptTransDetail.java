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
package com.evp.bizlib.receipt.impl;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.evp.bizlib.AppConstants;
import com.evp.bizlib.R;
import com.evp.bizlib.card.PanUtils;
import com.evp.bizlib.config.IConfigUtils;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.data.model.ETransType;
import com.evp.bizlib.dcc.DccUtils;
import com.evp.bizlib.installment.InstallmentUtils;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.bizlib.redeem.RedeemUtils;
import com.evp.bizlib.smallamt.SmallAmtUtils;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.sp.SharedPrefUtil;
import com.evp.commonlib.utils.ConvertUtils;
import com.evp.commonlib.utils.FontCache;
import com.evp.commonlib.utils.ResourceUtil;
import com.evp.poslib.gl.impl.GL;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func5;

import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;


@RouterService(interfaces = Func5.class, key = ReceiptConst.RECEIPT_TRANSDETAIL, singleton = true)
public class ReceiptTransDetail implements Func5<Context, TransData, Boolean, Integer, IConfigUtils, View> {

    @Override
    public View call(Context context, TransData transData, Boolean isRePrint, Integer receiptNo, IConfigUtils configUtils) {
        PaxGLPage glPage = new PaxGLPage(context);
        IPage page = glPage.createPage();
        page.adjustLineSpace(-6);
        page.setTypeFace(FontCache.get(FontCache.FONT_NAME, BaseApplication.getAppContext()));
        ETransType transType = ConvertUtils.enumValue(ETransType.class, transData.getTransType());
        TransData.ETransStatus transStatus = transData.getTransState();
        ETransType origTransType = ConvertUtils.enumValue(ETransType.class, transData.getOrigTransType());

        Acquirer acquirer = transData.getAcquirer();
        String temp, temp2;
        Bitmap logo = configUtils.getPrintResourceFile("headerLogo");
        Bitmap footerLogo = configUtils.getPrintResourceFile("footerLogo");
        String merchantName = configUtils.getPrintString("headerLine1Label");
        String merchantAddress = configUtils.getPrintString("headerLine2Label") + "\n" + configUtils.getPrintString("headerLine3Label");

        String fundingSource = transData.getFundingSource();
        if (AppConstants.FUNDING_SRC_ALIPAY.equals(fundingSource) || AppConstants.FUNDING_SRC_WECHAT.equals(fundingSource)) {
            page = ReceiptWalletQr.buildFullReceipt(page, transData, isRePrint, receiptNo, logo, merchantName, merchantAddress);
            return glPage.pageToView(page, 384);
        } else if (AppConstants.FUNDING_SRC_PROMPTPAY.equals(fundingSource)) {
            page = ReceiptPromptPay.buildFullReceipt(page, transData, isRePrint, receiptNo, logo, merchantName, merchantAddress);
            return glPage.pageToView(page, 384);
        } else if (AppConstants.FUNDING_SRC_QRCS.equals(fundingSource)) {
            page = ReceiptCreditQr.buildFullReceipt(page, transData, isRePrint, receiptNo, logo, merchantName, merchantAddress);
            return glPage.pageToView(page, 384);
        }

        //Logo
        page.addLine()
                .addUnit(page.createUnit()
                        .setBitmap(logo)
                        .setGravity(Gravity.CENTER));

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));


        //Merchant Name
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(merchantName)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Merchant Address
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(merchantAddress)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Terminal & Merchant ID
        temp = ResourceUtil.getString(R.string.receipt_tid) + " " + acquirer.getTerminalId();
        temp2 = ResourceUtil.getString(R.string.receipt_mid) + " " + acquirer.getMerchantId();
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(4.0f))
                .addUnit(page.createUnit()
                        .setText(temp2)
                        .setGravity(Gravity.END)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setWeight(6.0f));

        //Stan & Batch
        if (transType != ETransType.OLS_ENQUIRY) {
            temp = ResourceUtil.getString(R.string.receipt_stan_no) + " " + ConvertUtils.getPaddedNumber(transData.getStanNo(), 6);
            temp2 = ResourceUtil.getString(R.string.receipt_batch_num_colon) + " " + ConvertUtils.getPaddedNumber(transData.getBatchNo(), 6);
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(temp)
                            .setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit()
                            .setText(temp2)
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL));

            //Invoice / trace NO.
            temp = ResourceUtil.getString(R.string.receipt_trans_no) + " " + ConvertUtils.getPaddedNumber(transData.getTraceNo(), 6);
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(temp)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setTextStyle(IPage.ILine.IUnit.BOLD));

        }

        //Line
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(ReceiptConst.RECIPT_LINE)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));

        //Re-print
        if (isRePrint) {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_print_again))
                            .setFontSize(ReceiptConst.FONT_BIG)
                            .setGravity(Gravity.CENTER));

            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER));
        }

        String type = "";

        HashMap<String, byte[]> f63 = null;
        if (transType == ETransType.OLS_ENQUIRY) {
            f63 = RedeemUtils.unpack(transData);
            type = RedeemUtils.getPoolName(f63);
        } else if (transType != ETransType.REDEEM && origTransType != ETransType.REDEEM) {
            //Transaction Type
            temp = transStatus.equals(TransData.ETransStatus.NORMAL) ? "" : " (" + transStatus + ")";
            type = Objects.requireNonNull(transType).getTransName() + temp;
            if (transType == ETransType.VOID && origTransType != null) {
                type = String.format("%s(%s)", transType.getTransName(), origTransType.getTransName());
            }
        }

        if (!type.isEmpty())
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(type.toUpperCase())
                            .setFontSize(ReceiptConst.FONT_BIG)
                            .setGravity(Gravity.CENTER)
                            .setTextStyle(IPage.ILine.IUnit.BOLD));

        //PAN & Entry mode
        TransData.EnterMode enterMode = transData.getEnterMode();
        temp = (enterMode == TransData.EnterMode.QR) ? "" : transData.getIssuer() == null ? "" : PanUtils.maskCardNo(transData.getPan(), transData.getIssuer().getPanMaskPattern());
        String cardType;
        if (enterMode == TransData.EnterMode.MANUAL) {
            cardType = ResourceUtil.getString(R.string.receipt_entry_mode_manual);
        } else if (enterMode == TransData.EnterMode.CLSS) {
            cardType = ResourceUtil.getString(R.string.receipt_entry_mode_ctls);
        } else if (enterMode == TransData.EnterMode.SWIPE || enterMode == TransData.EnterMode.FALLBACK) {
            cardType = ResourceUtil.getString(R.string.receipt_entry_mode_swipe);
        } else {
            cardType = ResourceUtil.getString(R.string.receipt_entry_mode_chip);
        }
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setTextStyle(IPage.ILine.IUnit.BOLD))
                .addUnit(page.createUnit()
                        .setText(cardType)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END)
                        .setTextStyle(IPage.ILine.IUnit.BOLD));

        //Card type
        if (transData.getIssuer() != null) {
            type = transData.getIssuer().getName();
        }

        if (type != null && !type.isEmpty())
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_card_type))
                            .setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit()
                            .setText(type)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.END));

        //Date time & approval code
        temp = ConvertUtils.convert(transData.getDateTime(), ConvertUtils.TIME_PATTERN_TRANS,
                ConvertUtils.TIME_PATTERN_PRINT);
        String authCode = transType == ETransType.VOID ? transData.getOrigAuthCode() : transData.getAuthCode();
        if (!TextUtils.isEmpty(authCode)) {
            temp2 = ResourceUtil.getString(R.string.receipt_app_code) + " " + authCode;
        } else {
            temp2 = " ";
        }
        page.addLine()
                .addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_SMALL))
                .addUnit(page.createUnit()
                        .setText(temp2)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.END));

        //Ref. No.
        temp = transData.getRefNo();
        if (!TextUtils.isEmpty(temp)) {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_ref_no))
                            .setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit()
                            .setText(temp)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.END));
        }

        //EMV data
        if ((enterMode == TransData.EnterMode.INSERT || enterMode == TransData.EnterMode.CLSS)) {
            //Application name
            temp = transData.getEmvAppName();
            temp2 = transData.getEmvAppLabel();
            String appName = TextUtils.isEmpty(temp) ? temp2 : temp;
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_app_label))
                            .setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit()
                            .setText(appName)
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL));

            //AID
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText("AID: ")
                            .setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit()
                            .setText(transData.getAid())
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.END));

            //VOID and OFFLINE SALE does not doing EMV
            if(transType != ETransType.VOID && transType != ETransType.OFFLINE_SALE) {
                //TVR & TSI
                temp = "TVR: " + transData.getTvr();
                temp2 = "TSI: " + transData.getTsi();
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(temp)
                                .setFontSize(ReceiptConst.FONT_SMALL))
                        .addUnit(page.createUnit()
                                .setText(temp2)
                                .setGravity(Gravity.END)
                                .setFontSize(ReceiptConst.FONT_SMALL));

                //TC or ARQC
                if ("OFFLINE_APPROVED".equals(transData.getEmvResult())) {
                    temp = "TC: ";
                    temp2 = transData.getTc();
                } else {
                    temp = "ARQC: ";
                    temp2 = transData.getArqc();
                }
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(temp)
                                .setFontSize(ReceiptConst.FONT_SMALL))
                        .addUnit(page.createUnit()
                                .setText(temp2)
                                .setGravity(Gravity.END)
                                .setFontSize(ReceiptConst.FONT_SMALL));

                //Online or Offline approved
                if ("OFFLINE_APPROVED".equals(transData.getEmvResult())) {
                    temp = cardType + " OFFLINE";
                } else {
                    temp = cardType + " ONLINE";
                }
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(temp)
                                .setFontSize(ReceiptConst.FONT_SMALL));
            }

        }
        page.addLine().addUnit(page.createUnit().setText(" "));

        //DCC currency code
        if (acquirer.getName().equals(AppConstants.DCC_ACQUIRER)) {
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_currency_code))
                            .setFontSize(ReceiptConst.FONT_MEDIUM))
                    .addUnit(page.createUnit()
                            .setText(transData.getDccCurrencyCode())
                            .setFontSize(ReceiptConst.FONT_MEDIUM)
                            .setGravity(Gravity.END));
            page.addLine().addUnit(page.createUnit().setText(" "));
        }

        if (transType == ETransType.OLS_ENQUIRY) {
            page = RedeemUtils.buildPoolBalance(page, f63);
        } else if (transType == ETransType.REDEEM || origTransType == ETransType.REDEEM) {
            page = RedeemUtils.buildBodyFull(page, transData);
        } else {
            //base & tip
            if (transType.isAdjustAllowed()) {
                long base = ConvertUtils.parseLongSafe(transData.getAmount(), 0) - ConvertUtils.parseLongSafe(transData.getTipAmount(), 0);
                temp = CurrencyConverter.convert(base, transData.getCurrency());
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_amount_base))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setWeight(4.0f))
                        .addUnit(page.createUnit()
                                .setText(temp)
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.END)
                                .setWeight(9.0f));

                long tips = ConvertUtils.parseLongSafe(transData.getTipAmount(), 0);
                temp = CurrencyConverter.convert(tips, transData.getCurrency());
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_amount_tip))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setWeight(2.0f))
                        .addUnit(page.createUnit().setText(temp)
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.END)
                                .setWeight(3.0f));
            }

            // amount
            if (transType.isSymbolNegative()) {
                temp = CurrencyConverter.convert(-ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
                if (origTransType == ETransType.REFUND) {
                    temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
                }
            } else {
                temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getAmount(), 0), transData.getCurrency());
            }
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_amount))
                            .setFontSize(ReceiptConst.FONT_MEDIUM)
                            .setWeight(4.0f))
                    .addUnit(page.createUnit()
                            .setText(temp)
                            .setFontSize(ReceiptConst.FONT_MEDIUM)
                            .setGravity(Gravity.END)
                            .setWeight(9.0f));

            //DCC rate, currency amount and customer consent
            if (acquirer.getName().equals(AppConstants.DCC_ACQUIRER)) {
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_exchange_rate))
                                .setFontSize(ReceiptConst.FONT_MEDIUM))
                        .addUnit(page.createUnit()
                                .setText(DccUtils.getExRateForPrint(transData.getDccExchangeRate()))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.END));

                final Locale foreignLocale = CurrencyConverter.getLocaleFromCountryCode(transData.getDccCurrencyCode());
                if (transType.isSymbolNegative()) {
                    temp = CurrencyConverter.convert(-ConvertUtils.parseLongSafe(transData.getDccForeignAmount(), 0), foreignLocale);
                    if (origTransType == ETransType.REFUND) {
                        temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getDccForeignAmount(), 0), foreignLocale);
                    }
                } else {
                    temp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(transData.getDccForeignAmount(), 0), foreignLocale);
                }
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_transaction_currency))
                                .setFontSize(ReceiptConst.FONT_MEDIUM));
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(temp)
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.END));
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(" "));
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(configUtils.getPrintString("dccMiddleLine1Label"))
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER));
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(configUtils.getPrintString("dccMiddleLine2Label"))
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER));
            }
        }


        if (transType == ETransType.INSTALLMENT || origTransType == ETransType.INSTALLMENT) {
            page = InstallmentUtils.buildBodyFull(page, transData);
        }

        if (transType == ETransType.OLS_ENQUIRY) {
            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER));

            page.addLine().addUnit(page.createUnit()
                    .setText(ResourceUtil.getString(R.string.receipt_rewards_only))
                    .setFontSize(ReceiptConst.FONT_SMALL)
                    .setGravity(Gravity.CENTER));

            //Trusted transaction if TLE
            if (transData.getAcquirer().getTleEnabled()) {
                page.addLine().addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_trusted_transaction))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));
            }
        } else {
            //Signature
            if (transData.getHasPin()) {
                page.addLine().addUnit(page.createUnit().setText(" "));

                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_pin_entered))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER));

                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_qr_signature))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER));

            } else if(!transData.getHasPin() && transData.isSignFree()) {
                page.addLine().addUnit(page.createUnit().setText(" "));

                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_pin_or_sign))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER));

                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_not_required))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER));
            } else if(transData.isSignFree() || SmallAmtUtils.isTrxSmallAmt(transData)) {
                page.addLine().addUnit(page.createUnit().setText(" "));

                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_qr_signature))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER));

            } else {
                page.addLine().addUnit(page.createUnit().setText(" "));

                Bitmap signature = loadSignature(transData);
                if (signature != null) {
                    page.addLine().addUnit(page.createUnit()
                            .setBitmap(signature)
                            .setGravity(Gravity.CENTER));
                } else {
                    page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));
                }
            }

            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER));

            //Cardholder name
            temp = transData.getCardholderName();
            if (temp != null && temp.length() > 0 && !temp.equals("/")) {
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(temp)
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER));
            }

            //DCC currency code agreement
            if (acquirer.getName().equals(AppConstants.DCC_ACQUIRER)) {
                temp = configUtils.getPrintString("dccFooterLine1Label") + " " + CurrencyConverter.getCurrencySymbol(transData.getDccCurrencyCode()) + ".";
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(temp)
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER));

                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(configUtils.getPrintString("dccFooterLine2Label"))
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER));
            } else {
                //I acknowledge...
                temp = configUtils.getPrintString("footerLine1Label")
                        + "\n"
                        + configUtils.getPrintString("footerLine2Label")
                        + "\n"
                        + configUtils.getPrintString("footerLine3Label");
                page.addLine().addUnit(page.createUnit()
                        .setText(temp)
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));
            }

            //Trusted transaction if TLE
            if (transData.getAcquirer().getTleEnabled()) {
                page.addLine().addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_trusted_transaction))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER));
            }

            //Copy of who?
            if (receiptNo == 0) {
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_stub_merchant))
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER));
            } else if (receiptNo == 1) {
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_stub_user))
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER));
            } else {
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_stub_acquire))
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER));
            }
        }

        //Footer logo
        if(footerLogo.getWidth() > 1 && footerLogo.getHeight() > 1) {
            page.addLine().addUnit(page.createUnit().setText(" "));

            page.addLine()
                    .addUnit(page.createUnit()
                            .setBitmap(footerLogo)
                            .setGravity(Gravity.CENTER));
        }

        //Demo mode?
        SharedPrefUtil sharedPrefUtil = new SharedPrefUtil(BaseApplication.getAppContext());
        String comType = sharedPrefUtil.getString(ResourceUtil.getString(R.string.COMM_TYPE));
        if (ResourceUtil.getString(R.string.demo).equals(comType)) {
            page.addLine().addUnit(page.createUnit().setText(" "));
            page.addLine().addUnit(page.createUnit()
                    .setText(ResourceUtil.getString(R.string.demo_mode))
                    .setGravity(Gravity.CENTER)
                    .setFontSize(ReceiptConst.FONT_MEDIUM));
        }

        //App version
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            page.addLine().addUnit(page.createUnit().setText(" "));
            page.addLine().addUnit(page.createUnit()
                    .setText(version)
                    .setFontSize(ReceiptConst.FONT_SMALL)
                    .setGravity(Gravity.CENTER));
        } catch (Exception ignored) {
        }

        page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"));

        return glPage.pageToView(page, 384);
    }

    private Bitmap loadSignature(TransData transData) {
        byte[] signData = transData.getSignData();
        if (signData == null) {
            return null;
        }
        return GL.getGL().getImgProcessing().jbigToBitmap(signData);
    }
}
