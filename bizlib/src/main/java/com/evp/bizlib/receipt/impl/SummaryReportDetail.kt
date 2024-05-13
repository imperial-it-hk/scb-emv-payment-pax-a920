package com.evp.bizlib.receipt.impl

import android.view.Gravity
import com.evp.bizlib.R
import com.evp.bizlib.config.IConfigUtils
import com.evp.bizlib.data.entity.TransData
import com.evp.bizlib.receipt.ReceiptConst
import com.evp.commonlib.currency.CurrencyConverter
import com.evp.commonlib.utils.ConvertUtils
import com.evp.commonlib.utils.ConvertUtils.TIME_PATTERN_TRANS
import com.evp.commonlib.utils.ResourceUtil
import com.evp.poslib.gl.page.IPage
import java.util.*

class SummaryReportDetail {
    companion object {
        @JvmStatic
        fun buildReport(page: IPage, transDataList : List<TransData>, config: IConfigUtils): IPage {
            val acquirer = transDataList[0].acquirer
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


            val date = ConvertUtils.convert(fullDateTime, TIME_PATTERN_TRANS, ConvertUtils.DATE_PATTERN_PRINT)
            val time = ConvertUtils.convert(fullDateTime, TIME_PATTERN_TRANS, ConvertUtils.TIME_ONLY_PATTERN_PRINT)
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
            val traceNo = ResourceUtil.getString(R.string.report_batch) + " " + ConvertUtils.getPaddedNumber(1, 6)
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
                            .setText(ResourceUtil.getString(R.string.report_summary_report))
                            .setTextStyle(IPage.ILine.IUnit.BOLD)
                            .setFontSize(ReceiptConst.FONT_BIG)
                            .setGravity(Gravity.CENTER))

            //Line
            page.addLine()
                    .addUnit(page.createUnit()
                            .setText(ReceiptConst.RECIPT_LINE)
                            .setFontSize(ReceiptConst.FONT_SMALL)
                            .setGravity(Gravity.CENTER))

            for (i in 0 .. 5){
                val transName = when (i) {
                    0 -> R.string.receipt_trans_qr_visa
                    1 -> R.string.receipt_trans_qr_master
                    2 -> R.string.receipt_trans_qr_upi
                    3 -> R.string.receipt_thai_qr_payment
                    4 -> R.string.receipt_trans_alipay
                    5 -> R.string.receipt_trans_wechat
                    else -> 0
                }

                val saleList = when (i) {
                    0 -> transDataList.filter { it.fundingSource == "qrcs" && it.transType == "SALE" && it.paymentChannel == "VISA"}
                    1 -> transDataList.filter { it.fundingSource == "qrcs" && it.transType == "SALE" && it.paymentChannel == "Mastercard"}
                    2 -> transDataList.filter { it.fundingSource == "qrcs" && it.transType == "SALE" && it.paymentChannel == "UnionPay"}
                    3 -> transDataList.filter { it.fundingSource == "promptpay" && it.transType == "SALE" }
                    4 -> transDataList.filter { it.fundingSource == "alipay" && it.transType == "SALE" }
                    else -> transDataList.filter { it.fundingSource == "wechat" && it.transType == "SALE" }
                }

                //Sale
                val numberSale = saleList.size
                val amountSale = saleList.sumOf { it.amount.toInt() }
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

                val refundList = when (i) {
                    0 -> transDataList.filter { it.fundingSource == "qrcs" && it.transType == "REFUND" && it.paymentChannel == "VISA"}
                    1 -> transDataList.filter { it.fundingSource == "qrcs" && it.transType == "REFUND" && it.paymentChannel == "Mastercard"}
                    2 -> transDataList.filter { it.fundingSource == "qrcs" && it.transType == "REFUND" && it.paymentChannel == "UnionPay"}
                    3 -> transDataList.filter { it.fundingSource == "promptpay" && it.transType == "REFUND" }
                    4 -> transDataList.filter { it.fundingSource == "alipay" && it.transType == "REFUND" }
                    else -> transDataList.filter { it.fundingSource == "wechat" && it.transType == "REFUND" }
                }

                //Refunds
                val numberRefund = refundList.size
                val amountRefund = refundList.sumOf { it.amount.toInt() }
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

            val saleTotalList = transDataList.filter { it.transType == "SALE" }
            val numberTotalSale = saleTotalList.size
            val amountTotalSale = saleTotalList.sumOf { it.amount.toInt() }
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

            //Refunds
            val refundTotalList = transDataList.filter { it.transType == "REFUND" }
            val numberTotalRefund = refundTotalList.size
            val amountTotalRefund = refundTotalList.sumOf { it.amount.toInt() }
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