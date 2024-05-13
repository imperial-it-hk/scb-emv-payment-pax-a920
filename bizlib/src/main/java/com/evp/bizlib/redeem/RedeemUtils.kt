package com.evp.bizlib.redeem

import android.view.Gravity
import com.evp.bizlib.R
import com.evp.bizlib.data.entity.TransData
import com.evp.bizlib.data.model.ETransType
import com.evp.bizlib.onlinepacket.impl.PackRedeem
import com.evp.bizlib.receipt.ReceiptConst
import com.evp.commonlib.currency.CurrencyConverter
import com.evp.commonlib.utils.ConvertUtils
import com.evp.commonlib.utils.ResourceUtil
import com.evp.poslib.gl.page.IPage
import java.text.NumberFormat
import java.util.*

class RedeemUtils {

    enum class AttrType { n, an }

    enum class Subfield(val len: Int, val attr: AttrType) {
        TABLE_LEN(2, AttrType.n),
        LMIC(3, AttrType.an),
        ITEM_REDEEM_CNT(1, AttrType.n),
        ITEM_CODE(20, AttrType.an),
        ITEM_NAME(30, AttrType.an),
        ITEM_QTY(6, AttrType.n),
        ITEM_PRICE_PTS(6, AttrType.n),
        ITEM_PRICE_CASH(6, AttrType.n),
        ITEM_MIN_PTS(6, AttrType.n),
        ITEM_CONV_RATE(6, AttrType.n),
        ITEM_IS_FIXED(1, AttrType.an),
        ITEM_CAT_ID(6, AttrType.an),
        TXN_POOL_CNT(1, AttrType.n),
        TXN_CLUB_ID(3, AttrType.an),
        TXN_POOL_ID(3, AttrType.an),
        TXN_TYPE(3, AttrType.an),
        TXN_DESC(15, AttrType.an),
        TXN_CURRENCY(3, AttrType.an),
        TXN_DECIMAL(1, AttrType.n),
        TXN_SIGN(1, AttrType.an),
        TXN_QTY(6, AttrType.n),
        TXN_AMT(6, AttrType.n),
        TXN_DISC(6, AttrType.n),
        APP_POOL_CNT(1, AttrType.n),
        APP_CLUB_ID(3, AttrType.an),
        APP_POOL_ID(3, AttrType.an),
        APP_TYPE(3, AttrType.an),
        APP_DESC(15, AttrType.an),
        APP_CURRENCY(3, AttrType.an),
        APP_DECIMAL(1, AttrType.n),
        APP_SIGN(1, AttrType.an),
        APP_BAL(6, AttrType.n),
        APP_AMT(6, AttrType.n),
        APP_EXP(2, AttrType.n),
        DISCOUNT_AMT(6, AttrType.n),
        MESSAGE_CNT(1, AttrType.n),
        MESSAGE_TEXT(150, AttrType.an)
    }

    companion object {

        val f63_scb_response_redeem_items = arrayOf(
            Subfield.ITEM_CODE,
            Subfield.ITEM_NAME,
            Subfield.ITEM_QTY,
            Subfield.ITEM_PRICE_PTS,
            Subfield.ITEM_PRICE_CASH,
            Subfield.ITEM_MIN_PTS,
            Subfield.ITEM_CONV_RATE,
            Subfield.ITEM_IS_FIXED,
            Subfield.ITEM_CAT_ID
        )

        val f63_scb_response_txn_pool = arrayOf(
            Subfield.TXN_CLUB_ID,
            Subfield.TXN_POOL_ID,
            Subfield.TXN_TYPE,
            Subfield.TXN_DESC,
            Subfield.TXN_CURRENCY,
            Subfield.TXN_DECIMAL,
            Subfield.TXN_SIGN,
            Subfield.TXN_QTY,
            Subfield.TXN_AMT,
            Subfield.TXN_DISC
        )

        val f63_scb_response_app_pool = arrayOf(
            Subfield.APP_CLUB_ID,
            Subfield.APP_POOL_ID,
            Subfield.APP_TYPE,
            Subfield.APP_DESC,
            Subfield.APP_CURRENCY,
            Subfield.APP_DECIMAL,
            Subfield.APP_SIGN,
            Subfield.APP_BAL,
            Subfield.APP_AMT,
            Subfield.APP_EXP
        )

        val f63_scb_response_disp_msg = arrayOf(
            Subfield.MESSAGE_TEXT
        )


        private fun unpack(subFieldMap: HashMap<String, ByteArray?>, fieldData: ByteArray?, offset: Int, subFields: Array<Subfield>, count: Int): Int {
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

        private fun unpack(subFieldMap: HashMap<String, ByteArray?>, fieldData: ByteArray?, offset: Int, subField: Subfield): Int {
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
        fun getSubField(f63: HashMap<String, ByteArray?>, field: Subfield, idx: Int): String {
            val name = if (idx > 1)
                idx.toString() + field.name
            else
                field.name

            return f63[name]?.let {
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
            var redeemCnt: Int
            var txnPoolCnt: Int
            var appPoolCnt: Int
            var msgCnt: Int

            transData.field63?.let {
                if (transData.transType == ETransType.OLS_ENQUIRY.name) {
                    offset = unpack(f63, it, offset, Subfield.LMIC)

                    offset = unpack(f63, it, offset, Subfield.APP_POOL_CNT)
                    appPoolCnt = f63[Subfield.APP_POOL_CNT.name]?.get(0)?.toInt() ?: 0
                    offset = unpack(
                        f63,
                        it,
                        offset,
                        f63_scb_response_app_pool,
                        appPoolCnt
                    )

                    offset = unpack(f63, it, offset, Subfield.MESSAGE_CNT)
                    msgCnt = f63[Subfield.MESSAGE_CNT.name]?.get(0)?.toInt() ?: 0
                    offset = unpack(
                        f63,
                        it,
                        offset,
                        f63_scb_response_disp_msg,
                        msgCnt
                    )

                } else {
                    //offset = unpackReservedField(f63, it, offset, Subfield.TABLE_LEN)
                    offset = unpack(f63, it, offset, Subfield.LMIC)

                    if (transData.paymentPlan == PackRedeem.Plan.SPECIAL_REDEEM.ordinal) {
                        offset = unpack(f63, it, offset, Subfield.ITEM_REDEEM_CNT)
                        redeemCnt = f63[Subfield.ITEM_REDEEM_CNT.name]?.get(0)?.toInt() ?: 0
                        offset = unpack(
                            f63,
                            it,
                            offset,
                            f63_scb_response_redeem_items,
                            redeemCnt
                        )
                    }

                    offset = unpack(f63, it, offset, Subfield.TXN_POOL_CNT)
                    txnPoolCnt = f63[Subfield.TXN_POOL_CNT.name]?.get(0)?.toInt() ?: 0
                    offset = unpack(
                        f63,
                        it,
                        offset,
                        f63_scb_response_txn_pool,
                        txnPoolCnt
                    )

                    offset = unpack(f63, it, offset, Subfield.APP_POOL_CNT)
                    appPoolCnt = f63[Subfield.APP_POOL_CNT.name]?.get(0)?.toInt() ?: 0
                    offset = unpack(
                        f63,
                        it,
                        offset,
                        f63_scb_response_app_pool,
                        appPoolCnt
                    )

                    if (transData.paymentPlan == PackRedeem.Plan.SPECIAL_REDEEM.ordinal)
                        offset = unpack(f63, it, offset, Subfield.DISCOUNT_AMT)

                    offset = unpack(f63, it, offset, Subfield.MESSAGE_CNT)
                    msgCnt = f63[Subfield.MESSAGE_CNT.name]?.get(0)?.toInt() ?: 0
                    offset = unpack(
                        f63,
                        it,
                        offset,
                        f63_scb_response_disp_msg,
                        msgCnt
                    )
                }
            }
            return f63
        }

        @JvmStatic
        fun getPlanName(plan: Int) = when (plan) {
            PackRedeem.Plan.VOUCHER.ordinal -> ResourceUtil.getString(R.string.redeem_voucher)
            PackRedeem.Plan.POINT_CREDIT.ordinal -> ResourceUtil.getString(R.string.redeem_point_credit)
            PackRedeem.Plan.DISCOUNT.ordinal -> ResourceUtil.getString(R.string.redeem_discount)
            PackRedeem.Plan.SPECIAL_REDEEM.ordinal -> ResourceUtil.getString(R.string.redeem_product)
            else -> ""
        }

        @JvmStatic
        fun getPoolName(f63: HashMap<String, ByteArray?>): String {
            val appPoolCnt = getSubField(f63, Subfield.APP_POOL_CNT).toIntOrNull() ?: 0
            if (appPoolCnt == 0)
                return ""
            if (appPoolCnt > 1)
                return "SCB POINT BALANCE"
            return getSubField(f63, Subfield.APP_DESC, 1).uppercase(Locale.getDefault())
                .replace(" POOL", "")
        }

        private fun IPage.createTextUnit(text: String, font: Int = ReceiptConst.FONT_SMALL, align: Int = Gravity.LEFT, style: Int = IPage.ILine.IUnit.NORMAL): IPage.ILine.IUnit =
            this.createUnit().setText(text).setFontSize(font).setGravity(align).setTextStyle(style)

        @JvmStatic
        fun buildPoolBalance(page: IPage, f63: HashMap<String, ByteArray?>): IPage {
            val appPoolCnt = getSubField(f63, Subfield.APP_POOL_CNT).toIntOrNull() ?: 0
            if (appPoolCnt > 0) {
                for (i in 1..appPoolCnt) {
                    with(page) {
                        val poolName = getSubField(f63, Subfield.APP_DESC, i).uppercase(Locale.getDefault())
                            .replace(" POOL", "")
                        val poolBal = (getSubField(f63, Subfield.APP_BAL, i).toLongOrNull() ?: 0) * (if (getSubField(f63, Subfield.APP_SIGN, i) == "D") -1 else 1) / 100
                        addLine().addUnit(createTextUnit(ReceiptConst.RECIPT_LINE))
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_pool_id) + getSubField(f63, Subfield.APP_POOL_ID, i)))
                        if (appPoolCnt > 1)
                            addLine().addUnit(createTextUnit(poolName, ReceiptConst.FONT_MEDIUM, Gravity.LEFT, IPage.ILine.IUnit.BOLD))
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_balance_point), ReceiptConst.FONT_MEDIUM)).addUnit(createTextUnit(
                            NumberFormat.getIntegerInstance().format(poolBal), ReceiptConst.FONT_MEDIUM, Gravity.END
                        ))
                    }
                }
            }

            //prompt message
            val msgCnt = getSubField(f63, Subfield.MESSAGE_CNT).toIntOrNull() ?: 0
            if (msgCnt > 0) {
                with(page) {
                    addLine().addUnit(createTextUnit(" "))
                    for (i in 1..msgCnt) {
                        addLine().addUnit(createTextUnit(getSubField(f63, Subfield.MESSAGE_TEXT, i), align = Gravity.CENTER))
                    }
                }
            }

            return page
        }

        @JvmStatic
        fun buildBodyFull(page: IPage, transData: TransData): IPage {
            val f63 = RedeemUtils.unpack(transData)
            val transType = ConvertUtils.enumValue(ETransType::class.java, transData.transType)

            //redeem type
            var str = ""
            if (transType == ETransType.VOID)
                str = transType.transName + " "
            str += getPlanName(transData.paymentPlan)
            with(page) {
                addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_redeem)).setWeight(2.0f))
                    .addUnit(createTextUnit(str.uppercase(Locale.getDefault()), align = Gravity.END).setWeight(8.0f))
            }

            //credit total
            str = transData.formattedNetSaleAmt
            if (transType.isSymbolNegative)
                str = "-$str"
            val unitCreditTotal = page.createTextUnit(ResourceUtil.getString(R.string.receipt_credit_total), style = IPage.ILine.IUnit.BOLD).setWeight(4.0f)
            val unitAmount = page.createTextUnit(str, align = Gravity.END, style = IPage.ILine.IUnit.BOLD).setWeight(6.0f)
            page.addLine().addUnit(unitCreditTotal).addUnit(unitAmount)
            with(page) { addLine().addUnit(createTextUnit(ReceiptConst.RECIPT_LINE)) }

            //redeem specific product
            val redeemItemCnt = getSubField(f63, Subfield.ITEM_REDEEM_CNT).toIntOrNull() ?: 0
            if (redeemItemCnt > 0) {
                for (i in 1..redeemItemCnt) {
                    with(page) {
                        val unitRedeemCodeValue = createTextUnit(getSubField(f63, Subfield.ITEM_CODE, i).trim(), align = Gravity.END)
                        val unitRedeemName = createTextUnit(getSubField(f63, Subfield.ITEM_NAME, i).trim(), align = Gravity.END)
                        str = ConvertUtils.parseLongSafe(getSubField(f63, Subfield.ITEM_QTY, i), 0).toString()
                        val unitRedeemQty = createTextUnit(str, align = Gravity.END)
                        str = NumberFormat.getIntegerInstance().format(
                            ConvertUtils.parseLongSafe(
                                getSubField(f63, Subfield.TXN_QTY, i), 0) / 100)
                        val unitRedeemPts = createTextUnit(str, align = Gravity.END)
                        str = CurrencyConverter.convert(ConvertUtils.parseLongSafe(getSubField(f63, Subfield.TXN_AMT, i), 0), transData.currency)
                        val unitRedeemAmt = createTextUnit(str, align = Gravity.END)
                        str = CurrencyConverter.convert(ConvertUtils.parseLongSafe(getSubField(f63, Subfield.ITEM_PRICE_CASH, i), 0), transData.currency)
                        val unitCreditAmt = createTextUnit(str, align = Gravity.END)
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_reward_code))).addUnit(unitRedeemCodeValue)
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_redeem_name))).addUnit(unitRedeemName)
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_redeem_point))).addUnit(unitRedeemPts)
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_redeem_amount))).addUnit(unitRedeemAmt)
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_credit_amount))).addUnit(unitCreditAmt)
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_no_of_items))).addUnit(unitRedeemQty)
                    }
                }
            }

            //redeem voucher, discount, point
            if (transData.paymentPlan != PackRedeem.Plan.SPECIAL_REDEEM.ordinal) {
                val txnPoolCnt = getSubField(f63, Subfield.TXN_POOL_CNT).toIntOrNull() ?: 0
                for (i in 1..txnPoolCnt) {
                    with(page) {
                        var pts = ConvertUtils.parseLongSafe(getSubField(f63, Subfield.TXN_QTY, i), 0) / 100
                        var amt = ConvertUtils.parseLongSafe(getSubField(f63, Subfield.TXN_AMT, i), 0)
                        var credit = ConvertUtils.parseLongSafe(transData.amount, 0)
                        val discount = ConvertUtils.parseLongSafe(getSubField(f63, Subfield.TXN_DISC, i), 0)
                        if (transType.isSymbolNegative) {
                            pts = 0 - pts
                            amt = 0 - amt
                        }
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_redeem_point))).addUnit(createTextUnit(pts.toString(), align = Gravity.END))
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_redeem_amount))).addUnit(createTextUnit(
                            CurrencyConverter.convert(amt, transData.currency), align = Gravity.END
                        ))
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_credit_amount))).addUnit(createTextUnit(
                            CurrencyConverter.convert(credit, transData.currency), align = Gravity.END
                        ))
                        if (discount > 0) {
                            str = (discount / 100).toString() + if ((discount % 100) != 0L) (discount % 100).toString() else "" + "%"
                            addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_discount_percent))).addUnit(createTextUnit(str, align = Gravity.END))
                        }
                    }
                }
            }

            //balance
            val appPoolCnt = getSubField(f63, Subfield.APP_POOL_CNT).toIntOrNull() ?: 0
            if (appPoolCnt > 0) {
                with(page) { addLine().addUnit(createTextUnit(ReceiptConst.RECIPT_LINE)) }

                for (i in 1..appPoolCnt) {
                    with(page) {
                        val poolBal = (getSubField(f63, Subfield.APP_BAL, i).toLongOrNull() ?: 0) * (if (getSubField(f63, Subfield.APP_SIGN, i) == "D") -1 else 1) / 100
                        addLine().addUnit(createTextUnit(ResourceUtil.getString(R.string.receipt_pool_id) + getSubField(f63, Subfield.APP_POOL_ID, i)))
                        addLine().addUnit(createTextUnit(getSubField(f63, Subfield.APP_DESC, i))).addUnit(createTextUnit(
                            NumberFormat.getIntegerInstance().format(poolBal), align = Gravity.END
                        ))
                    }
                }
            }

            //prompt message
            val msgCnt = getSubField(f63, Subfield.MESSAGE_CNT).toIntOrNull() ?: 0
            if (msgCnt > 0) {
                with(page) {
                    addLine().addUnit(createTextUnit(" "))
                    for (i in 1..msgCnt) {
                        addLine().addUnit(createTextUnit(getSubField(f63, Subfield.MESSAGE_TEXT, i).trim(), align = Gravity.CENTER))
                    }
                }
            }

            return page
        }
    }
}
