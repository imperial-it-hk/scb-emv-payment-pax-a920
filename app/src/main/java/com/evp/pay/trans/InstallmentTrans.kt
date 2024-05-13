package com.evp.pay.trans

import android.content.Context
import com.evp.abl.core.ActionResult
import com.evp.bizlib.AppConstants
import com.evp.bizlib.card.PanUtils
import com.evp.bizlib.data.model.ETransType
import com.evp.bizlib.installment.InstallmentUtils
import com.evp.bizlib.installment.InstallmentUtils.Companion.getSubField
import com.evp.bizlib.installment.InstallmentUtils.Subfield.*
import com.evp.bizlib.onlinepacket.TransResult
import com.evp.bizlib.onlinepacket.TransResult.SUCC
import com.evp.commonlib.currency.CurrencyConverter
import com.evp.commonlib.utils.ConvertUtils
import com.evp.commonlib.utils.LogUtils
import com.evp.config.ConfigUtils
import com.evp.pay.trans.action.*
import kotlinx.serialization.Serializable
import java.util.*

class InstallmentTrans(
    context: Context,
    amount: String = "0",
    private var plan: Plan = Plan.NORMAL_IPP,
    private var paymentTerm: String,
    private var productCode: String,
    private var productSN: String,
    transListener: TransEndListener?
) : SaleTrans(context, ETransType.INSTALLMENT, false, transListener) {

    private val TAG = "InstallmentTrans"

    enum class Plan { NO_IPP, NORMAL_IPP, FIX_RATE_IPP, SPECIAL_IPP, PROMO_FIXED_RATE_IPP }
    enum class State { ENTER_AMOUNT, SELECT_PLAN, ENTER_IPP_TERM, ENTER_CODE, INPUT_DETAILS, SCAN_CODE, CLSS_PREPROC, TRANS_CONFIRM, SIGNATURE, PRINT }

    constructor(
        context: Context,
        amount: String?,
        plan: String?,
        paymentTerm: String?,
        productCode: String?,
        productSN: String?,
        transListener: TransEndListener?
    ) :
            this(
                context,
                amount ?: "0",
                Plan.NORMAL_IPP,
                paymentTerm ?: "",
                productCode ?: "",
                productSN ?: "",
                transListener
            ) {
        this.plan = when {
            plan?.uppercase(Locale.getDefault())?.contains("DEJUNG_0%") == true -> Plan.NORMAL_IPP
            plan?.uppercase(Locale.getDefault())?.contains("DEJUNG_SPECIAL") == true -> Plan.SPECIAL_IPP
            plan?.uppercase(Locale.getDefault())?.contains("DEJUNG") == true -> Plan.FIX_RATE_IPP
            else -> Plan.NO_IPP
        }
    }

    @Serializable
    data class InstallmentDetail(
            var paymentTerm: String = "00",
            var productCode: String = "",
            var productSN: String = ""
    )



    init {
        this.amount = amount
    }

    private fun nextState(gotoState: String): String {
        var nextState = gotoState

        if (nextState == State.ENTER_AMOUNT.toString())
            if (!amount.isNullOrBlank() && amount != "0")
                nextState = State.SELECT_PLAN.toString()

        if (nextState == State.SELECT_PLAN.toString())
            if (transData.paymentPlan != Plan.NO_IPP.ordinal)
                nextState = State.ENTER_IPP_TERM.toString()

        if (nextState == State.ENTER_IPP_TERM.toString())
            if (!transData.paymentTerm.isNullOrEmpty())
                nextState = State.ENTER_CODE.toString()

        if (nextState == State.ENTER_CODE.toString())
            if (transData.paymentPlan != Plan.SPECIAL_IPP.ordinal || !transData.productCode.isNullOrEmpty())
                nextState = State.CLSS_PREPROC.toString()

        return nextState
    }

    override fun bindStateOnAction() {
        super.bindStateOnAction()

        title = when (plan) {
            Plan.NORMAL_IPP -> ConfigUtils.getInstance().getString("installment_normal")
            Plan.FIX_RATE_IPP -> ConfigUtils.getInstance().getString("installment_fix_rate")
            Plan.SPECIAL_IPP -> ConfigUtils.getInstance().getString("installment_special")
            Plan.PROMO_FIXED_RATE_IPP -> ConfigUtils.getInstance().getString("installment_promo")
            else -> ConfigUtils.getInstance().getString("trans_installment")
        }

        transData.amount = amount
        transData.paymentPlan = plan.ordinal
        transData.paymentTerm = paymentTerm
        transData.productCode = productCode
        transData.productSN = productSN

        // enter InstallmentDetials
        val installmentDetails = ActionInputProductDetails { action ->
            (action as ActionInputProductDetails).setParam(currentContext, title, plan.toString())
        }
        bind(State.INPUT_DETAILS.toString(), installmentDetails, true)

        val enterIppTermAction = ActionInputTransData { action ->
            (action as ActionInputTransData).setParam(currentContext, title)
                    .setInputLine(ConfigUtils.getInstance().getString("prompt_input_ipp_term"), ActionInputTransData.EInputType.NUM, 2, 1, false, false)
        }
        bind(State.ENTER_IPP_TERM.toString(), enterIppTermAction, true)

        val enterCodeAction = ActionInputTransData { action ->
            (action as ActionInputTransData).setParam(currentContext, title)
                    .setInputLine(ConfigUtils.getInstance().getString("installment_product_code"), ActionInputTransData.EInputType.NUM, 20, 1, false, true)
        }
        bind(State.ENTER_CODE.toString(), enterCodeAction, true)

        val actionScanQRCode = ActionScanQRCode { action -> (action as ActionScanQRCode).setParam(currentContext) }
        bind(State.SCAN_CODE.toString(), actionScanQRCode, false)

        val actionSelectPlan = ActionSelectItem { action ->
            val menuTexts = arrayOf(ConfigUtils.getInstance().getString("installment_normal"), ConfigUtils.getInstance().getString("installment_fix_rate"), ConfigUtils.getInstance().getString("installment_special"))
            (action as ActionSelectItem).setParam(currentContext, ConfigUtils.getInstance().getString("select_installment_type"), menuTexts)
        }
        bind(State.SELECT_PLAN.toString(), actionSelectPlan, true)

        var transDetailConfirm = ActionTransDetailConfirm { action ->
            fun convertAmt(s: String?) = s?.let { CurrencyConverter.convert(ConvertUtils.parseLongSafe(it, 0), transData.currency) } ?: ""
            val map = LinkedHashMap<String, String>()
            val f63 = InstallmentUtils.unpack(transData)

            map["\\*" + ConfigUtils.getInstance().getString("confirm_amount")] = convertAmt(transData.amount)
            map["\\-" + ConfigUtils.getInstance().getString("confirm_payment_term")] = transData.paymentTerm + " " + ConfigUtils.getInstance().getString("confirm_months")
            val rate = ConvertUtils.parseLongSafe(getSubField(f63, RATE), 0)
            map["\\-" + ConfigUtils.getInstance().getString("confirm_monthly_rate")] = String.format("%01d.%02d", rate / 100, rate % 100)
            map["\\-" + ConfigUtils.getInstance().getString("confirm_interest_amount")] = convertAmt(getSubField(f63, AMOUNT))
            map["\\-" + ConfigUtils.getInstance().getString("confirm_total_due")] = convertAmt(getSubField(f63, TOTAL_DUE))
            map["\\-" + ConfigUtils.getInstance().getString("confirm_monthly_due")] = convertAmt(getSubField(f63, MONTH_DUE))
            (action as ActionTransDetailConfirm).setParam(currentContext, title, map,
                PanUtils.maskCardNo(transData.pan, transData.issuer.panMaskPattern),
                transData.cardholderName)
        }
        bind(State.TRANS_CONFIRM.toString(), transDetailConfirm, false)

        gotoState(nextState(State.ENTER_AMOUNT.toString()))
    }


    override fun onActionResult(currentState: String?, result: ActionResult?) {
        LogUtils.i(TAG, "onActionResult : $currentState")
        var next = ""
        when (currentState) {
            State.ENTER_AMOUNT.toString() -> {// 输入交易金额后续处理
                transData.amount = result?.data.toString()
                next = nextState(State.SELECT_PLAN.toString())
            }

            State.SELECT_PLAN.toString() -> {
                (result?.data as? Int)?.let {
                    transData.paymentPlan = it + 1
                    plan = ConvertUtils.getEnum(Plan::class.java, transData.paymentPlan)
                    if (transData.paymentPlan != 0)
                        next = nextState(State.ENTER_IPP_TERM.toString())
                }
            }

            State.ENTER_IPP_TERM.toString() -> {
                (result?.data as? String)?.let {
                    transData.paymentTerm = it
                }
                if (!transData.paymentTerm.isNullOrEmpty())
                    next = nextState(State.ENTER_CODE.toString())
            }

            State.ENTER_CODE.toString() -> {
                (result?.data as? String)?.let {
                    if (it == AppConstants.SALE_TYPE_SCAN)
                        next = nextState(State.SCAN_CODE.toString())
                    else
                        transData.productCode = it
                }
                if (!transData.productCode.isNullOrEmpty())
                    next = nextState(State.CLSS_PREPROC.toString())
            }

            State.SCAN_CODE.toString() -> {
                if (result?.ret == SUCC) {
                    (result.data as? String)?.let {
                        if (it.length > 9) {
                            transEnd(ActionResult(TransResult.ERR_INVALID_QR, null))
                            return
                        }
                        transData.productCode = it
                    }
                }
                if (!transData.productCode.isNullOrEmpty())
                    next = nextState(State.CLSS_PREPROC.toString())
            }

            State.INPUT_DETAILS.toString() -> {
                (result?.data as? InstallmentDetail)?.let {
                    transData.productCode = it.productCode
                    transData.paymentTerm = it.paymentTerm
                    transData.productSN = it.productSN
                    next = nextState(State.CLSS_PREPROC.toString())
                }
            }

            State.TRANS_CONFIRM.toString() -> {
                next = nextState(State.SIGNATURE.toString())
            }

            else -> {
                super.onActionResult(currentState, result)
                return
            }
        }
        if (next.isNotEmpty())
            gotoState(next)
        else
            transEnd(result)
    }
}

