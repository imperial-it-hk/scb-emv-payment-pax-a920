package com.evp.pay.trans

import android.content.Context
import com.evp.abl.core.ActionResult
import com.evp.bizlib.AppConstants
import com.evp.bizlib.card.PanUtils
import com.evp.bizlib.data.entity.Acquirer
import com.evp.bizlib.data.entity.TransData
import com.evp.bizlib.data.local.GreendaoHelper
import com.evp.bizlib.data.model.ETransType
import com.evp.bizlib.onlinepacket.TransResult
import com.evp.bizlib.redeem.RedeemUtils
import com.evp.bizlib.redeem.RedeemUtils.Companion.getSubField
import com.evp.bizlib.redeem.RedeemUtils.Subfield.*
import com.evp.commonlib.currency.CurrencyConverter
import com.evp.commonlib.currency.CurrencyConverter.convertAmount
import com.evp.commonlib.utils.ConvertUtils
import com.evp.commonlib.utils.KeyUtils
import com.evp.commonlib.utils.LogUtils
import com.evp.config.ConfigUtils
import com.evp.pay.constant.Constants
import com.evp.pay.trans.action.*
import com.evp.pay.trans.component.Component.getPaddedNumber
import com.evp.payment.evpscb.R
import java.util.*

class RedeemTrans(
    context: Context,
    amount: String = "0",
    private var plan: Plan = Plan.VOUCHER,
    private var productCode: String,
    private var redeemQty: String,
    transListener: TransEndListener?
) : SaleTrans(context, ETransType.REDEEM, true, transListener) {

    val TAG = "RedeemTrans"

    enum class Plan { NO_REDEEM, VOUCHER, POINT_CREDIT, DISCOUNT, SPECIAL_REDEEM, ENQUIRY}
    enum class State { SELECT_PLAN, ENTER_AMOUNT, ENTER_POINT, ENTER_CODE, ENTER_QTY, CHECK_CARD, SCAN_CODE, INPUT_DETAILS, EMV_PROC, ENTER_PIN, MAG_ONLINE, CLSS_PREPROC, CLSS_PROC, TRANS_CONFIRM, SIGNATURE, USER_AGREEMENT, PRINT }

    constructor(context: Context, amount: String?, plan: String?, productCode: String?, redeemQty: String?, transListener: TransEndListener?) :
            this(context, amount?:"0", Plan.NO_REDEEM, productCode ?: "", redeemQty ?: "", transListener) {
                this.plan = when {
                    plan?.uppercase(Locale.getDefault())?.contains("DISCOUNT%") == true -> Plan.DISCOUNT
                    plan?.uppercase(Locale.getDefault())?.contains("SPECIFIC PRODUCT") == true -> Plan.SPECIAL_REDEEM
                    plan?.uppercase(Locale.getDefault())?.contains("VOUCHER") == true -> Plan.VOUCHER
                    plan?.uppercase(Locale.getDefault())?.contains("ENQUIRY") == true -> Plan.ENQUIRY
                    plan?.uppercase(Locale.getDefault())?.contains("POINT+CREDIT") == true -> Plan.POINT_CREDIT
                    else -> Plan.NO_REDEEM
                }
        this.amount = amount
        doInit()
    }

    init {
        this.amount = amount
        doInit()
    }

    data class RedeemDetails(
        var productCode: String = "",
        var redeemQty: String = ""
    )


    fun doInit() {
        this.title = getPlanName(plan.ordinal)

        if (plan == Plan.ENQUIRY)
            this.transType = ETransType.OLS_ENQUIRY

        when (plan) {
            Plan.ENQUIRY, Plan.VOUCHER, Plan.SPECIAL_REDEEM -> this.amount = "" //not display amount when wait card
            else -> {}
        }
    }

    override fun bindStateOnAction() {
        super.bindStateOnAction()

        transData.paymentPlan = plan.ordinal
        transData.productCode = productCode
        transData.redeemAmt = "0"
        transData.redeemPts = "0"
        transData.redeemQty = redeemQty
        transData.redeemType = ""
        transData.amount = amount

        // enter InstallmentDetials
        val inputDetails = ActionInputProductDetails { action ->
            (action as ActionInputProductDetails).setParam(currentContext, title, plan.toString())
        }
        bind(State.INPUT_DETAILS.toString(), inputDetails, true)

        val actionSelectPlan = ActionSelectItem { action ->
            val menuTexts = arrayOf(ConfigUtils.getInstance().getString("point_enquiry"), ConfigUtils.getInstance().getString("redeem_discount"), ConfigUtils.getInstance().getString("redeem_product"))
            (action as ActionSelectItem).setParam(currentContext, ConfigUtils.getInstance().getString("select_redemption_type"), menuTexts)
        }
        bind(State.SELECT_PLAN.toString(), actionSelectPlan, true)

        val enterPointAction = ActionInputTransData { action ->
            (action as ActionInputTransData).setParam(currentContext, title)
                .setInputLine(ConfigUtils.getInstance().getString("redeem_point"), ActionInputTransData.EInputType.NUM, 20, 1, false, false)
        }
        bind(State.ENTER_POINT.toString(), enterPointAction, true)

        val enterQuantityAction = ActionInputTransData { action ->
            (action as ActionInputTransData).setParam(currentContext, title)
                .setInputLine(ConfigUtils.getInstance().getString("redeem_quantity"), ActionInputTransData.EInputType.NUM, 20, 1, false, false)
        }
        bind(State.ENTER_QTY.toString(), enterQuantityAction, true)

        val enterCodeAction = ActionInputTransData { action ->
            (action as ActionInputTransData).setParam(currentContext, title)
                .setInputLine(ConfigUtils.getInstance().getString("installment_product_code"), ActionInputTransData.EInputType.NUM, 20, 1, false, true)
        }
        bind(State.ENTER_CODE.toString(), enterCodeAction, true)

        val actionScanQRCode = ActionScanQRCode { action -> (action as ActionScanQRCode).setParam(currentContext) }
        bind(State.SCAN_CODE.toString(), actionScanQRCode, false)

        // enter pin action
        val enterPinAction = ActionEnterPin { action ->
            (action as ActionEnterPin).setParam(
                currentContext, title,
                transData.pan, isSupportBypass, getString(R.string.prompt_pin),
                getString(R.string.prompt_no_pin), null, null,
                ActionEnterPin.EEnterPinType.ONLINE_PIN, KeyUtils.getTpkIndex(transData.acquirer.tleKeySetId)
            )
        }
        bind(SaleTrans.State.ENTER_PIN.toString(), enterPinAction, true)

        var transDetailConfirm = ActionTransDetailConfirm { action ->
            fun convertAmt(s: String?) = s?.let { CurrencyConverter.convert(ConvertUtils.parseLongSafe(it, 0), transData.currency) } ?: ""
            val map = LinkedHashMap<String, String>()
            val f63 = RedeemUtils.unpack(transData)
            val creditAmount = if (transData.amount.isNullOrBlank()) "0" else transData.amount

            map["\\-" + ConfigUtils.getInstance().getString("confirm_redeem")] = getPlanName(transData.paymentPlan).uppercase(Locale.getDefault())
            map["\\*" + ConfigUtils.getInstance().getString("confirm_credit_total")] = convertAmt(((getSubField(f63, TXN_AMT).toLongOrNull() ?: 0) + creditAmount.toLong()).toString())
            map["\\-"] = "-"
            if (transData.paymentPlan == Plan.SPECIAL_REDEEM.ordinal) {
                map["\\-" + ConfigUtils.getInstance().getString("confirm_reward_code")] = getSubField(f63, ITEM_CODE).trim()
                map["\\-" + ConfigUtils.getInstance().getString("confirm_reward_name")] = getSubField(f63, ITEM_NAME).trim()
                map["\\-" + ConfigUtils.getInstance().getString("confirm_redeem_point")] = ((getSubField(f63, TXN_QTY).toLongOrNull() ?: 0) / 100).toString()
                map["\\-" + ConfigUtils.getInstance().getString("confirm_redeem_amount")] = convertAmt(getSubField(f63, TXN_AMT))
                map["\\-" + ConfigUtils.getInstance().getString("confirm_credit_amount")] = convertAmt(creditAmount)
                map["\\-" + ConfigUtils.getInstance().getString("confirm_num_of_item")] = getSubField(f63, ITEM_QTY).toLongOrNull()?.toString()?:"0"
            }
            if (transData.paymentPlan == Plan.DISCOUNT.ordinal) {
                map["\\-" + ConfigUtils.getInstance().getString("confirm_redeem_point")] = ((getSubField(f63, TXN_QTY).toLongOrNull() ?: 0) / 100).toString()
                map["\\-" + ConfigUtils.getInstance().getString("confirm_discount_rate")] = convertAmount(getSubField(f63, TXN_DISC))
                map["\\-" + ConfigUtils.getInstance().getString("confirm_discount")] = convertAmt(getSubField(f63, TXN_AMT))
            }
            (action as ActionTransDetailConfirm).setParam(currentContext, title, map,
                PanUtils.maskCardNo(transData.pan, transData.issuer.panMaskPattern),
                transData.cardholderName)
        }
        bind(State.TRANS_CONFIRM.toString(), transDetailConfirm, false)

        // signature action
        val signatureAction = ActionSignature { action -> (action as ActionSignature).setParam(currentContext, transData.formattedNetSaleAmt, transData.formattedRedeemPts) }
        bind(SaleTrans.State.SIGNATURE.toString(), signatureAction)

        if (plan == Plan.ENQUIRY)
            gotoState(State.CLSS_PREPROC.toString())
        else
            gotoState(nextState(State.SELECT_PLAN.toString()))

    }

    fun nextState(gotoState: String): String {
        var nextState = gotoState

        if (nextState == State.SELECT_PLAN.toString()) {
            nextState = when (transData.paymentPlan) {
                Plan.DISCOUNT.ordinal, Plan.POINT_CREDIT.ordinal -> State.ENTER_AMOUNT.toString()
                Plan.VOUCHER.ordinal->State.ENTER_POINT.toString()
                Plan.SPECIAL_REDEEM.ordinal->State.ENTER_CODE.toString()
                else -> nextState
            }
        }

        if (nextState == State.ENTER_AMOUNT.toString())
            if (!transData.amount.isNullOrBlank() && transData.amount != "0")
                nextState = if (transData.paymentPlan == Plan.DISCOUNT.ordinal) State.CLSS_PREPROC.toString()
                            else State.ENTER_POINT.toString()

        if (nextState == State.ENTER_CODE.toString())
            if (!transData.productCode.isNullOrBlank())
                nextState = State.ENTER_QTY.toString()

        if (nextState == State.ENTER_POINT.toString() || nextState == State.ENTER_QTY.toString())
            if (!transData.redeemQty.isNullOrBlank())
                nextState = State.CLSS_PREPROC.toString()

        return nextState
    }

    override fun onActionResult(currentState: String?, result: ActionResult?) {
        LogUtils.i(TAG, "onActionResult : $currentState")
        var next = ""
        when (currentState) {

            State.SELECT_PLAN.toString() -> {
                (result?.data as? Int)?.let {
                    transData.paymentPlan = when (it) {
                        0 -> Plan.ENQUIRY.ordinal
                        1 -> Plan.DISCOUNT.ordinal
                        else -> Plan.SPECIAL_REDEEM.ordinal
                    }
                    plan = ConvertUtils.getEnum(Plan::class.java, transData.paymentPlan)
                    title = getPlanName(plan.ordinal)
                    when (plan) {
                        Plan.DISCOUNT, Plan.POINT_CREDIT -> next = nextState(State.ENTER_AMOUNT.toString())
                        Plan.VOUCHER -> next = nextState(State.ENTER_POINT.toString())
                        Plan.SPECIAL_REDEEM -> next = nextState(State.ENTER_CODE.toString())
                        Plan.ENQUIRY -> {
                            this.transType = ETransType.OLS_ENQUIRY
                            transData.transType = ETransType.OLS_ENQUIRY.name
                            transData.procCode = ETransType.OLS_ENQUIRY.procCode
                            this.amount = ""
                            next = nextState(State.CLSS_PREPROC.toString())
                        }
                        else -> {}
                    }
                }
            }

            State.ENTER_AMOUNT.toString() -> {// 输入交易金额后续处理
                transData.amount = result?.data.toString()
                if (plan == Plan.DISCOUNT) {
                    transData.paymentPlan = plan.ordinal
                    next = nextState(State.CLSS_PREPROC.toString())
                } else
                    next = nextState(State.ENTER_POINT.toString())
            }

            State.CHECK_CARD.toString() -> {
                /*
                FinancialApplication.getAcqManager().let {
                    it.curAcq = it.findAcquirer(ACQUIRER_OLS)
                    transData.acquirer = it.curAcq
                    transData.nii = transData.acquirer.nii
                    transData.batchNo = transData.acquirer.currBatchNo.toLong()
                }
                */
                if (transData.amount.isNullOrBlank())
                    transData.amount = "10" //dummy for emv process
                if (plan == Plan.ENQUIRY) { //no need sign for point enquiry
                    transData.signFree = true;
                }
                super.onActionResult(currentState, result)
            }

            State.MAG_ONLINE.toString() -> { //  subsequent processing of online
                if (transData.field63.isNotEmpty() && plan != Plan.ENQUIRY) {
                    unpackReservedField(transData)
                    GreendaoHelper.getTransDataHelper().update(transData)
                }
                super.onActionResult(currentState, result)
            }

            State.ENTER_QTY.toString(),
            State.ENTER_POINT.toString() -> {
                (result?.data as? String)?.let { transData.redeemQty = it }
                next = nextState(State.CLSS_PREPROC.toString())
            }

            State.ENTER_CODE.toString() -> {
                (result?.data as? String)?.let {
                    if (it == AppConstants.SALE_TYPE_SCAN)
                        next = nextState(State.SCAN_CODE.toString())
                    else
                        transData.productCode = it
                }
                if (!transData.productCode.isNullOrBlank())
                    next = nextState(State.ENTER_QTY.toString())
            }

            State.SCAN_CODE.toString() -> {
                if (result?.ret == TransResult.SUCC) {
                    (result.data as? String)?.let {
                        if (it.length > 20) {
                            transEnd(ActionResult(TransResult.ERR_INVALID_QR, null))
                            return
                        }
                        transData.productCode = it
                    }
                }
                if (!transData.productCode.isNullOrEmpty())
                    next = nextState(State.ENTER_QTY.toString())
            }

            State.INPUT_DETAILS.toString() -> {
                (result?.data as? RedeemDetails)?.let {
                    transData.productCode = it.productCode
                    transData.redeemQty = it.redeemQty
                }
                next = nextState(State.CLSS_PREPROC.toString())
            }

            State.TRANS_CONFIRM.toString() -> {
                next = nextState(State.SIGNATURE.toString())
            }

            else -> {
                super.onActionResult(currentState, result)
            }
        }

        if (next.isNotEmpty())
            gotoState(next)
    }

    companion object {
        fun unpackReservedField(transData: TransData) {
            val f63 = RedeemUtils.unpack(transData)
            if (transData.paymentPlan == Plan.SPECIAL_REDEEM.ordinal)
                transData.amount = getSubField(f63, ITEM_PRICE_CASH)
            transData.clubPoolId = getSubField(f63, TXN_CLUB_ID) + getSubField(f63, TXN_POOL_ID)
            transData.redeemType = getSubField(f63, TXN_SIGN)
            transData.redeemPts = getSubField(f63, TXN_QTY)
            transData.redeemAmt = getSubField(f63, TXN_AMT)
        }

        fun settleField63(acquirer: Acquirer): ByteArray {
            val transList = GreendaoHelper.getTransDataHelper().findTransData(listOf(ETransType.REDEEM.name), listOf(TransData.ETransStatus.VOIDED), acquirer);
            val transByPool = transList.groupBy { it.clubPoolId }
            var buf = Constants.OLS_VERSION +
                    getPaddedNumber(transList.size.toLong(), 4) +
                    getPaddedNumber(transList.map { it.amount.toLongOrNull()?:0 }.sum(), 14) +
                    getPaddedNumber(transByPool.size.toLong(), 2)

            transByPool.forEach { pool ->
                buf = buf + pool.key + "0000" + "43" + "00000000000000" +
                        getPaddedNumber(pool.value.size.toLong(), 4) +
                        getPaddedNumber(pool.value.map { it.redeemAmt.toLongOrNull()?:0 }.sum(), 14)
            }

            buf += "0000000000000000"
            LogUtils.d(TAG, "f63 $buf")
            return ConvertUtils.asciiToBin(buf)
        }

        fun getPlanName(plan: Int): String {
            return when (plan) {
                Plan.ENQUIRY.ordinal -> ConfigUtils.getInstance().getString("point_enquiry");
                Plan.VOUCHER.ordinal -> ConfigUtils.getInstance().getString("redeem_voucher");
                Plan.POINT_CREDIT.ordinal -> ConfigUtils.getInstance().getString("redeem_point_credit");
                Plan.DISCOUNT.ordinal -> ConfigUtils.getInstance().getString("redeem_discount");
                Plan.SPECIAL_REDEEM.ordinal -> ConfigUtils.getInstance().getString("redeem_product");
                else -> ""
            }
        }
    }
}

