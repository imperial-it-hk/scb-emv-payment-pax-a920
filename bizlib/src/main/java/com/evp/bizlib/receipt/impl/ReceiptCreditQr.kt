package com.evp.bizlib.receipt.impl

import android.graphics.Bitmap
import android.view.Gravity
import com.evp.bizlib.R
import com.evp.bizlib.data.entity.TransData
import com.evp.bizlib.data.model.ETransType
import com.evp.bizlib.receipt.ReceiptConst
import com.evp.commonlib.currency.CurrencyConverter
import com.evp.commonlib.utils.ConvertUtils
import com.evp.commonlib.utils.ResourceUtil
import com.evp.poslib.gl.impl.GL
import com.evp.poslib.gl.page.IPage
import com.google.zxing.BarcodeFormat
import java.util.*

class ReceiptCreditQr {
    companion object {
        @JvmStatic
        fun buildFullReceipt(page: IPage, transData: TransData, isRePrint: Boolean, receiptNo: Int, logo: Bitmap, merchantName: String, merchantAddress: String): IPage {
            val transType = ConvertUtils.enumValue(ETransType::class.java, transData.transType)
            val transStatus = transData.transState
            val acquirer = transData.acquirer
            val isDisplayMinus = (transStatus == TransData.ETransStatus.VOIDED && transType == ETransType.SALE) ||
                    (transStatus == TransData.ETransStatus.NORMAL && transType == ETransType.VOID) ||
                    (transStatus == TransData.ETransStatus.REFUNDED && transType == ETransType.SALE) ||
                    (transStatus == TransData.ETransStatus.NORMAL && transType == ETransType.REFUND)
            val minus = if (isDisplayMinus) "-"  else ""

            //Logo
            page.addLine()
                    .addUnit(page.createUnit()
                            .setBitmap(logo)
                            .setGravity(Gravity.CENTER))
            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))

            //Merchant Name
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(merchantName)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))

            //Merchant Address
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(merchantAddress)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))
            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))

            //Terminal & Merchant ID
            val tid = ResourceUtil.getString(R.string.receipt_tid) + " " + acquirer.terminalId
            val merchantID = ResourceUtil.getString(R.string.receipt_mid) + " " + acquirer.merchantId
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(tid)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(4.0f))
                    .addUnit(page.createUnit()
                            .setText(merchantID)
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(6.0f))

            //Stan & Batch
            val stanNo = ResourceUtil.getString(R.string.receipt_stan_no) + " " + ConvertUtils.getPaddedNumber(transData.stanNo, 6)
            val batch = ResourceUtil.getString(R.string.receipt_batch_num_colon) + " " + ConvertUtils.getPaddedNumber(transData.batchNo, 6)
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(stanNo)
                            .setFontSize(ReceiptConst.FONT_SMALL))
                    .addUnit(page.createUnit()
                            .setText(batch)
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL))

            //Invoice / trace NO.
            val traceNo = ResourceUtil.getString(R.string.receipt_trans_no) + " " + ConvertUtils.getPaddedNumber(transData.traceNo, 6)
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(traceNo)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setTextStyle(IPage.ILine.IUnit.BOLD))

            //Line

            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))

            //Re-print
            if (isRePrint) {
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_print_again))
                                .setFontSize(ReceiptConst.FONT_BIG)
                                .setGravity(Gravity.CENTER))

                //Line
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ReceiptConst.RECIPT_LINE)
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER))
            }

            //Transaction Type
            val type = when (transType) {
                ETransType.VOID -> "VOID SALE"
                ETransType.REFUND -> "REFUND"
                else -> "SALE"
            }

            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(type.uppercase(Locale.getDefault()))
                            .setFontSize(ReceiptConst.FONT_BIG)
                            .setGravity(Gravity.CENTER)
                            .setTextStyle(IPage.ILine.IUnit.BOLD))

            //Card No & Card Type
            val cardNo = transData.merchantPan ?: "null"
            val cardType = transData.paymentChannel ?: "null"
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(cardNo)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setWeight(6.0f))
                    .addUnit(page.createUnit()
                            .setText("$cardType QR")
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setWeight(4.0f))

            //Date Time & APPROVE CODE
            val originAuthCode = transData.authCode ?: "null"
            val approveCode = ResourceUtil.getString(R.string.receipt_appr_code) + originAuthCode
            val dateTime = ConvertUtils.convert(transData.dateTime, ConvertUtils.TIME_PATTERN_TRANS, ConvertUtils.DATE_TIME_PATTERN_PRINT)
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(dateTime)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(5.0f))
                    .addUnit(page.createUnit()
                            .setText(approveCode)
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(5.0f))

            // Reference No & Application
            val refNo = ResourceUtil.getString(R.string.receipt_ref_no) + (transData.refNo ?: "null")
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(refNo)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(5.0f))


            // Merchant pan
            val merPan = ResourceUtil.getString(R.string.receipt_merchant_pan) + (transData.consumerPan ?: "null")
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(merPan)
                            .setFontSize(ReceiptConst.FONT_SMALL))

            // Transaction ID
            val tranId = ResourceUtil.getString(R.string.receipt_trans_id) + (transData.transactionId ?: "null")
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(tranId)
                            .setFontSize(ReceiptConst.FONT_SMALL))

            // QR ID
            val qrId = ResourceUtil.getString(R.string.receipt_qr_id) + (transData.qrCodeId ?: "null")
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(qrId)
                            .setFontSize(ReceiptConst.FONT_SMALL))

            page.addLine().addUnit(page.createUnit().setText(ReceiptConst.EMPTY_LINE))

            val amountThValue = CurrencyConverter.convert(ConvertUtils.parseLongSafe(minus + transData.amount, 0))
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.receipt_qr_amount))
                            .setFontSize(ReceiptConst.FONT_MEDIUM)
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setWeight(6.0f))
                    .addUnit(page.createUnit()
                            .setText(amountThValue)
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_MEDIUM)
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setWeight(4.0f))

            page.addLine().addUnit(page.createUnit().setText(ReceiptConst.EMPTY_LINE))
            page.addLine().addUnit(page.createUnit().setText(ReceiptConst.EMPTY_LINE))

            val isDisplayQrCode = true
            if (receiptNo == 0 && isDisplayQrCode && transType != ETransType.VOID) {

                //Line
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ReceiptConst.RECEIPT_SIGN_LINE)
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setTextStyle(IPage.ILine.IUnit.BOLD)
                                .setGravity(Gravity.CENTER))

                //Card holder name
                page.addLine()
                        .addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_card_holder_name))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER))


                page.addLine().addUnit(page.createUnit().setText(ReceiptConst.EMPTY_LINE))
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_scan_for_void))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER))

                val traceQrContext = ConvertUtils.getPaddedNumber(transData.traceNo, 6)
                val qrBitmap = GL.getGL().imgProcessing.generateBarCode(traceQrContext,100,100, BarcodeFormat.QR_CODE)
                qrBitmap?.let {
                    page.addLine()
                            .addUnit(page.createUnit()
                                    .setBitmap(qrBitmap)
                                    .setGravity(Gravity.CENTER))
                }

                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(traceQrContext)
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER))

                page.addLine().addUnit(page.createUnit().setText(ReceiptConst.EMPTY_LINE))
            }

            if (receiptNo > 0) {
                //Line
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ReceiptConst.RECIPT_LINE)
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setGravity(Gravity.CENTER))

                //Card holder name
                page.addLine().addUnit(page.createUnit()
                        .setText(ResourceUtil.getString(R.string.receipt_card_holder_name))
                        .setFontSize(ReceiptConst.FONT_SMALL)
                        .setGravity(Gravity.CENTER))
            }

            //I acknowledge...
            page.addLine().addUnit(page.createUnit()
                    .setText(ResourceUtil.getString(R.string.receipt_verify))
                    .setFontSize(ReceiptConst.FONT_SMALL)
                    .setGravity(Gravity.CENTER))

            //No Refund
            page.addLine().addUnit(page.createUnit()
                    .setText(ResourceUtil.getString(R.string.receipt_stub_no_refund))
                    .setFontSize(ReceiptConst.FONT_MEDIUM)
                    .setGravity(Gravity.CENTER))

            //Trusted transaction
            page.addLine().addUnit(page.createUnit()
                    .setText(ResourceUtil.getString(R.string.receipt_trusted_transaction))
                    .setFontSize(ReceiptConst.FONT_SMALL)
                    .setGravity(Gravity.CENTER))

            //Merchant Copy &&
            if (receiptNo == 0) {
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_stub_merchant))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER))
            } else if (receiptNo == 1) {
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(R.string.receipt_stub_customer))
                                .setFontSize(ReceiptConst.FONT_MEDIUM)
                                .setGravity(Gravity.CENTER))
            }

            val version = "< version >"
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(version)
                            .setFontSize(ReceiptConst.FONT_MEDIUM)
                            .setGravity(Gravity.CENTER))

            page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"))

            return page
        }

    }
}