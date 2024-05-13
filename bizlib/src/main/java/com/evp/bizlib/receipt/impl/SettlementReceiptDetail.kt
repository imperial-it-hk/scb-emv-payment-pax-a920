package com.evp.bizlib.receipt.impl

import android.view.Gravity
import com.evp.bizlib.R
import com.evp.bizlib.config.IConfigUtils
import com.evp.bizlib.data.entity.TransTotal
import com.evp.bizlib.receipt.ReceiptConst
import com.evp.commonlib.currency.CurrencyConverter
import com.evp.commonlib.utils.ConvertUtils
import com.evp.commonlib.utils.ResourceUtil
import com.evp.poslib.gl.page.IPage
import java.util.*

class SettlementReceiptDetail {
    companion object {
        @JvmStatic
        fun buildReceipt(page: IPage, transTotal: TransTotal, config: IConfigUtils): IPage {
            val acquirer = transTotal.acquirer
            val logo = config.getPrintResourceFile("headerLogo")
            val currencyCode = "THB "
            val merchantName = config.getPrintString("headerLine1Label")
            val merchantAddress = config.getPrintString("headerLine2Label") + "\n" + config.getPrintString("headerLine3Label")

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

            //Terminal
            val tid = ResourceUtil.getString(R.string.report_tid) + acquirer.terminalId
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(tid)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(4.0f))
            // Merchant ID
            val merchantID = ResourceUtil.getString(R.string.report_mid) + acquirer.merchantId
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(merchantID)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(6.0f))


            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1
            val dateLocal = year.toString() + month.toString().padStart(2, '0') +
                    calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            val timeLocal = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0') +
                    calendar.get(Calendar.MINUTE).toString().padStart(2, '0') +
                    calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
            val fullDateTime = dateLocal + timeLocal


            val date = ConvertUtils.convert(fullDateTime, ConvertUtils.TIME_PATTERN_TRANS, ConvertUtils.DATE_PATTERN_PRINT)
            val time = ConvertUtils.convert(fullDateTime, ConvertUtils.TIME_PATTERN_TRANS, ConvertUtils.TIME_ONLY_PATTERN_PRINT)
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(date)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(4.0f))
                    .addUnit(page.createUnit()
                            .setText(time)
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(6.0f))

            //Batch
            val traceNo = ResourceUtil.getString(R.string.report_batch) + " " + ConvertUtils.getPaddedNumber(transTotal.batchNo.toLong(), 6)
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(traceNo)
                            .setFontSize(ReceiptConst.FONT_SMALL))

            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))

            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.report_settlement))
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setFontSize(ReceiptConst.FONT_BIG)
                            .setGravity(Gravity.CENTER))

            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.report_successful))
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setFontSize(ReceiptConst.FONT_BIG)
                            .setGravity(Gravity.CENTER))

            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))

            for (i in 0..5) {
                val transName = when (i) {
                    0 -> R.string.receipt_trans_qr_visa
                    1 -> R.string.receipt_trans_qr_master
                    2 -> R.string.receipt_trans_qr_upi
                    3 -> R.string.receipt_thai_qr_payment
                    4 -> R.string.receipt_trans_alipay
                    5 -> R.string.receipt_trans_wechat
                    else -> 0
                }

                val numberSale = when (i) {
                    0 -> transTotal.qrsQrcsVisaSaleTotalNum
                    1 -> transTotal.qrsQrcsMasterSaleTotalNum
                    2 -> transTotal.qrsQrcsUpiSaleTotalNum
                    3 -> transTotal.qrsTag30SaleTotalNum
                    4 -> transTotal.qrsAlipaySaleTotalNum
                    else -> transTotal.qrsWechatSaleTotalNum
                }
                val amountSale = when (i) {
                    0 -> transTotal.qrsQrcsVisaSaleTotalAmt
                    1 -> transTotal.qrsQrcsMasterSaleTotalAmt
                    2 -> transTotal.qrsQrcsUpiSaleTotalAmt
                    3 -> transTotal.qrsTag30SaleTotalAmt
                    4 -> transTotal.qrsAlipaySaleTotalAmt
                    else -> transTotal.qrsWechatSaleTotalAmt
                }
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(ResourceUtil.getString(transName))
                                .setFontSize(ReceiptConst.FONT_SMALL))
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText("  " + ResourceUtil.getString(R.string.report_sales))
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setWeight(6.0f))
                        .addUnit(page.createUnit()
                                .setText(numberSale.toString())
                                .setGravity(Gravity.END)
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setWeight(4.0f))
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(currencyCode + CurrencyConverter.convertAmount(amountSale.toString()))
                                .setGravity(Gravity.END)
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setWeight(4.0f))

                val numberRefund = when (i) {
                    0 -> transTotal.qrsQrcsVisaRefundTotalNum
                    1 -> transTotal.qrsQrcsMasterRefundTotalNum
                    2 -> transTotal.qrsQrcsUpiRefundTotalNum
                    3 -> transTotal.qrsTag30RefundTotalNum
                    4 -> transTotal.qrsAlipayRefundTotalNum
                    else -> transTotal.qrsWechatRefundTotalNum
                }
                val amountRefund = when (i) {
                    0 -> transTotal.qrsQrcsVisaRefundTotalAmt
                    1 -> transTotal.qrsQrcsMasterRefundTotalAmt
                    2 -> transTotal.qrsQrcsUpiRefundTotalAmt
                    3 -> transTotal.qrsTag30RefundTotalAmt
                    4 -> transTotal.qrsAlipayRefundTotalAmt
                    else -> transTotal.qrsWechatRefundTotalAmt
                }
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText("  " + ResourceUtil.getString(R.string.report_refunds))
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setWeight(6.0f))
                        .addUnit(page.createUnit()
                                .setText(numberRefund.toString())
                                .setGravity(Gravity.END)
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setWeight(4.0f))
                page.addLine()
                        .addUnit(page.createUnit()
                                .setText(currencyCode + CurrencyConverter.convertAmount(amountRefund.toString()))
                                .setGravity(Gravity.END)
                                .setFontSize(ReceiptConst.FONT_SMALL)
                                .setWeight(4.0f))
            }

            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))

            //Sale
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.report_grand_total))
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setWeight(6.0f))

            val numberTotalSale = transTotal.saleTotalNum
            val amountTotalSale = transTotal.saleTotalAmt
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.report_sales))
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setWeight(6.0f))
                    .addUnit(page.createUnit()
                            .setText(numberTotalSale.toString())
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(4.0f))
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(currencyCode + CurrencyConverter.convertAmount(amountTotalSale.toString()))
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(4.0f))

            val numberTotalRefund = transTotal.refundTotalNum
            val amountTotalRefund = transTotal.refundTotalAmt
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ResourceUtil.getString(R.string.report_refunds))
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setWeight(6.0f))
                    .addUnit(page.createUnit()
                            .setText(numberTotalRefund.toString())
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(4.0f))
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(currencyCode + CurrencyConverter.convertAmount(amountTotalRefund.toString()))
                            .setGravity(Gravity.END)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setWeight(4.0f))

            page.addLine().addUnit(page.createUnit().setText("\n\n\n\n"))
            return page
        }

    }
}