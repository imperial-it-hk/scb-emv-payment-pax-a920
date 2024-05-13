package com.evp.bizlib.installment

import android.view.Gravity
import com.evp.bizlib.R
import com.evp.bizlib.data.entity.TransData
import com.evp.bizlib.onlinepacket.impl.PackInstall
import com.evp.bizlib.receipt.ReceiptConst
import com.evp.commonlib.currency.CurrencyConverter
import com.evp.commonlib.utils.ConvertUtils
import com.evp.commonlib.utils.ResourceUtil
import com.evp.poslib.gl.page.IPage

class InstallmentUtils {

    enum class AttrType { n, an }

    enum class Subfield(val len: Int, val attr: AttrType) {
        TABLE_LEN(2, AttrType.n),
        TAB_ID(2, AttrType.an),
        SUPPLIER(20, AttrType.an),
        PRODUCT(20, AttrType.an),
        MODEL(20, AttrType.an),
        RATE(4, AttrType.an),
        AMOUNT(12, AttrType.an),
        TOTAL_DUE(12, AttrType.an),
        MONTH_DUE(12, AttrType.an)
    }

    companion object {

        val ipp_details = arrayOf(
            Subfield.TABLE_LEN,
            Subfield.TAB_ID,
            Subfield.SUPPLIER,
            Subfield.PRODUCT,
            Subfield.MODEL,
            Subfield.RATE,
            Subfield.AMOUNT,
            Subfield.TOTAL_DUE,
            Subfield.MONTH_DUE
        )


        @JvmStatic
        fun unpack(subFieldMap: HashMap<String, ByteArray?>, fieldData: ByteArray?, offset: Int, subFields: Array<Subfield>, count: Int): Int {
            var off = offset

            if (fieldData != null) {
                val len = fieldData.size
                for (i in 1..count) {
                    if (off + getLength(subFields) <= len) {
                        for (f in subFields) {
                            val value = ByteArray(f.len)
                            val key = if (i > 1)
                                i.toString() + f.name
                            else
                                f.name
                            System.arraycopy(fieldData, off, value, 0, f.len)
                            subFieldMap[key] = value
                            off += f.len
                        }
                    }
                }
            }
            return off
        }

        fun unpack(subFieldMap: HashMap<String, ByteArray?>, fieldData: ByteArray?, offset: Int, subField: Subfield): Int {
            var off = offset
            if (fieldData != null) {
                val len = fieldData.size
                if (off + subField.len <= len) {
                    val value = ByteArray(subField.len)
                    System.arraycopy(fieldData, off, value, 0, subField.len)
                    subFieldMap[subField.name] = value
                    off += subField.len
                }
            }
            return off
        }

        private fun getLength(subFields: Array<Subfield>): Int {
            var len = 0
            for (f in subFields) {
                len += f.len
            }
            return len
        }

        @JvmStatic
        fun getSubField(f63: HashMap<String, ByteArray?>, field: Subfield): String {
            return f63[field.name]?.let {
                if (field.attr == AttrType.n)
                    ConvertUtils.binToAscii(it)
                else
                    String(it)
            } ?: ""
        }

        @JvmStatic
        fun unpack(transData: TransData): HashMap<String, ByteArray?> {
            val f63: HashMap<String, ByteArray?> = HashMap() //define empty hashmap
            var offset = 0
            transData.field63?.let {
                offset = unpack(f63, it, offset, ipp_details, 1)
            }
            return f63
        }

        private fun IPage.createTextUnit(text: String, font: Int = ReceiptConst.FONT_SMALL, align: Int = Gravity.LEFT, style: Int = IPage.ILine.IUnit.NORMAL): IPage.ILine.IUnit =
            this.createUnit().setText(text).setFontSize(font).setGravity(align).setTextStyle(style)

        @JvmStatic
        fun buildBodyFull(page: IPage, transData: TransData): IPage {
            var tmp: String
            val f63 = unpack(transData)

            val unitIppNormal = page.createTextUnit(ResourceUtil.getString(R.string.receipt_deejung_0), ReceiptConst.FONT_MEDIUM, Gravity.CENTER, IPage.ILine.IUnit.BOLD)
            val unitIppFixed = page.createTextUnit(ResourceUtil.getString(R.string.receipt_deejung), ReceiptConst.FONT_MEDIUM, Gravity.CENTER, IPage.ILine.IUnit.BOLD)
            val unitTerm = page.createTextUnit(ResourceUtil.getString(R.string.payment_term), ReceiptConst.FONT_SMALL, Gravity.LEFT)
            val unitSupplier = page.createTextUnit(ResourceUtil.getString(R.string.receipt_supplier), ReceiptConst.FONT_SMALL, Gravity.LEFT)
            val unitModel= page.createTextUnit(ResourceUtil.getString(R.string.receipt_model), ReceiptConst.FONT_SMALL, Gravity.LEFT)
            val unitProduct = page.createTextUnit(ResourceUtil.getString(R.string.receipt_product), ReceiptConst.FONT_SMALL, Gravity.LEFT)
            val unitRate = page.createTextUnit(ResourceUtil.getString(R.string.receipt_interest_per_month), ReceiptConst.FONT_SMALL, Gravity.LEFT).setWeight(8.0f)
            val unitAmount = page.createTextUnit(ResourceUtil.getString(R.string.receipt_interest_amount), ReceiptConst.FONT_SMALL, Gravity.LEFT)
            val unitTotalDue = page.createTextUnit(ResourceUtil.getString(R.string.receipt_total_due), ReceiptConst.FONT_SMALL, Gravity.LEFT)
            val unitMonthDue = page.createTextUnit(ResourceUtil.getString(R.string.receipt_monthly_due), ReceiptConst.FONT_SMALL, Gravity.LEFT)
            val unitSN = page.createTextUnit(ResourceUtil.getString(R.string.receipt_sn), ReceiptConst.FONT_SMALL, Gravity.LEFT)

            val unitTermVal = page.createTextUnit(transData.paymentTerm + ResourceUtil.getString(R.string.receipt_months), ReceiptConst.FONT_SMALL, Gravity.END)
            val unitSupplierVal = page.createTextUnit(getSubField(f63, Subfield.SUPPLIER).trim(), ReceiptConst.FONT_SMALL, Gravity.END)
            val unitProductVal = page.createTextUnit(getSubField(f63, Subfield.PRODUCT).trim(), ReceiptConst.FONT_SMALL, Gravity.END)
            val unitModelVal = page.createTextUnit(getSubField(f63, Subfield.MODEL).trim(), ReceiptConst.FONT_SMALL, Gravity.END)

            val rate = ConvertUtils.parseLongSafe(getSubField(f63, Subfield.RATE), 0)
            tmp = String.format("%01d.%02d", rate / 100, rate % 100)
            val unitRateVal = page.createTextUnit(tmp, ReceiptConst.FONT_SMALL, Gravity.END).setWeight(2.0f)

            tmp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(getSubField(f63,
                Subfield.TOTAL_DUE
            ), 0), transData.currency)
            val unitTotalDueVal = page.createTextUnit(tmp, ReceiptConst.FONT_SMALL, Gravity.END)

            tmp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(getSubField(f63,
                Subfield.MONTH_DUE
            ), 0), transData.currency)
            val unitMonthDueVal = page.createTextUnit(tmp, ReceiptConst.FONT_SMALL, Gravity.END)

            tmp = CurrencyConverter.convert(ConvertUtils.parseLongSafe(getSubField(f63,
                Subfield.AMOUNT
            ), 0), transData.currency)
            val unitAmountVal = page.createTextUnit(tmp, ReceiptConst.FONT_SMALL, Gravity.END)

            val unitSnVal = page.createTextUnit(transData.productSN?.trim()?:"", ReceiptConst.FONT_SMALL, Gravity.END)


            page.addLine().addUnit(page.createUnit().setText(" "))

            if (transData.paymentPlan == PackInstall.Plan.NORMAL_IPP.ordinal)
                page.addLine().addUnit(unitIppNormal)
            else
                page.addLine().addUnit(unitIppFixed)

            if (transData.paymentPlan == PackInstall.Plan.SPECIAL_IPP.ordinal) {
                page.addLine().addUnit(unitSupplier).addUnit(unitSupplierVal)
                page.addLine().addUnit(unitProduct).addUnit(unitProductVal)
                page.addLine().addUnit(unitModel).addUnit(unitModelVal)
                page.addLine().addUnit(unitSN).addUnit(unitSnVal)
            }

            page.addLine().addUnit(unitTerm).addUnit(unitTermVal)

            page.addLine().addUnit(unitRate).addUnit(unitRateVal)
            page.addLine().addUnit(unitAmount).addUnit(unitAmountVal)

            page.addLine().addUnit(unitTotalDue).addUnit(unitTotalDueVal)
            page.addLine().addUnit(unitMonthDue).addUnit(unitMonthDueVal)
            return page
        }
    }
}
