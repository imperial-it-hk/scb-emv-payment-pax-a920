package com.evp.pay.trans.action.activity

import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.evp.abl.core.ActionResult
import com.evp.bizlib.onlinepacket.TransResult
import com.evp.commonlib.utils.LogUtils
import com.evp.pay.BaseActivityWithTickForAction
import com.evp.pay.constant.Constants
import com.evp.pay.constant.EUIParamKeys
import com.evp.pay.trans.InstallmentTrans
import com.evp.pay.trans.InstallmentTrans.InstallmentDetail
import com.evp.pay.trans.RedeemTrans
import com.evp.pay.trans.RedeemTrans.RedeemDetails
import com.evp.payment.evpscb.R
import com.evp.view.dialog.DialogUtils
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat

class ProductDetailsActivity : BaseActivityWithTickForAction() {
    companion object {
        const val maximumInstallmentAmount: Double = 9999999999.99
        const val maximumInstallmentTerms: Int = 60
        const val minimumInstallmentTerms: Int = 3
        const val maximumInstallmentProductCodeLen: Int = 9
        const val minimumInstallmentSerialnumber: Int = 0
        const val maximumRedemptionProductCodeLen: Int = 16
        const val constPromoType: Int = 3
        const val constAmountMaxChar: Int = 13
        const val constPaymentTermsMaxChar: Int = 2
    }

    private enum class Type { NONE, IPP_NORMAL, IPP_PROMO, REDEEM, REDEEM_SPECIAL }

    private var TAG: String = "ProductDetailsActivity"
    override fun getLayoutId(): Int {
        return R.layout.activity_product_details
    }

    override fun setListeners() {
        btn_cancel?.let {
            it.apply {
                this.setOnClickListener {
                    tickTimer.stop()
                    btn_ok?.isEnabled = false
                    this.isEnabled = false
                    setResult(TransResult.ERR_USER_CANCEL)
                    finish(ActionResult(TransResult.ERR_USER_CANCEL, null))
                }
            }
        }

        btn_ok?.let {
            it.apply {
                this.setOnClickListener {

                    tickTimer.stop()
                    this.isEnabled = false
                    btn_cancel?.isEnabled = false
                    val validation_result = OnValidateData()
                    if (validation_result != TransResult.SUCC) {
                        this.isEnabled = true
                        btn_cancel?.isEnabled = true
                    }
                }
            }
        }

        edtx_installment_amount?.let {
            it.apply {
                val currControl = this
                this.addTextChangedListener(object : TextWatcher {
                    private var decimalFormat: DecimalFormat = DecimalFormat("#,###.##")
                    override fun afterTextChanged(s: Editable?) {
                        try {
                            currControl.removeTextChangedListener(this)

                            if (currControl.text.toString().length <= constAmountMaxChar) {
                                s?.let {
                                    var currentText = it.toString()
                                        .replace(
                                            DecimalFormat("#,###.##").decimalFormatSymbols.groupingSeparator.toString(),
                                            ""
                                        )
                                        .replace(decimalFormat.decimalFormatSymbols.decimalSeparator.toString(), "")
                                        .trim()

                                    currentText = currentText.padStart(12, '0')
                                    currentText = "${currentText.substring(0, 10)}.${currentText.substring(10, 12)}"
                                    currentText = currentText.trimStart('0')
                                    if (currentText[0] == '.') {
                                        currentText = currentText.padStart(currentText.length + 1, '0')
                                    }

                                    val alteredText = currentText.toString()

                                    currControl.setText(alteredText)
                                    currControl.setSelection(currControl.text.length)
                                }
                            } else {
                                s?.let {
                                    val currentText = it.toString()
                                    val alteredText = currentText.substring(0, 12)

                                    currControl.setText(alteredText)
                                    currControl.setSelection(currControl.text.length)
                                }
                            }
                        } catch (ex: Exception) {
                            LogUtils.e(TAG, ex.toString())
                        } finally {
                            currControl.addTextChangedListener(this)
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })
            }
        }

    }

    private var productType = Type.NONE
    private var navTitle = ""

    override fun loadParam() {
        LogUtils.d(TAG, "loadParam--Start")

        navTitle = intent.getStringExtra(EUIParamKeys.NAV_TITLE.toString())
        val type: String = intent.getStringExtra(EUIParamKeys.IPP_OLS_TYPE.toString())

        when (type) {
            InstallmentTrans.Plan.NORMAL_IPP.toString(), InstallmentTrans.Plan.FIX_RATE_IPP.toString() ->
                productType = Type.IPP_NORMAL
            InstallmentTrans.Plan.SPECIAL_IPP.toString() ->
                productType = Type.IPP_PROMO
            RedeemTrans.Plan.SPECIAL_REDEEM.toString() ->
                productType = Type.REDEEM_SPECIAL
            else ->
                productType = Type.REDEEM
        }

        LogUtils.d(TAG, "\t\tInstallmentMode=" + productType)
        LogUtils.d(TAG, "loadParam--End")
    }

    private var txv_installment_amount: TextView? = null
    private var txv_payment_terms: TextView? = null
    private var txv_label_product_code: TextView? = null
    private var txv_label_serialnumber: TextView? = null
    private var edtx_installment_amount: EditText? = null
    private var edtx_payment_terms: EditText? = null
    private var edtx_label_product_code: EditText? = null
    private var edtx_label_serialnumber: TextInputEditText? = null
    private var linear_payment_terms: LinearLayout? = null
    private var linear_product_code: LinearLayout? = null
    private var linear_redeem_point: LinearLayout? = null
    private var linear_serialno: LinearLayout? = null

    private var txv_label_quantity: TextView? = null
    private var edtx_label_quantity: TextInputEditText? = null
    private var linear_quantity: LinearLayout? = null
    private var edtx_label_redeem: TextInputEditText? = null

    private var btn_ok: Button? = null
    private var btn_cancel: Button? = null
    private var edtx_custom_by_user = arrayListOf<EditText>()
    override fun initViews() {
        LogUtils.d(TAG, "InitView--Start")

        btn_ok = findViewById(R.id.btn_ok) as Button
        btn_cancel = findViewById(R.id.btn_cancel) as Button
        LogUtils.d(TAG, "\t\tButton is ok")

        linear_payment_terms = findViewById(R.id.linear_payment_terms) as LinearLayout
        linear_product_code = findViewById(R.id.linear_product_code) as LinearLayout
        linear_redeem_point = findViewById(R.id.linear_redeem_point) as LinearLayout
        linear_serialno = findViewById(R.id.linear_serialnumber) as LinearLayout
        linear_quantity = findViewById(R.id.linear_quanlity) as LinearLayout

        linear_product_code?.visibility = View.GONE
        linear_serialno?.visibility = View.GONE
        linear_quantity?.visibility = View.GONE
        linear_redeem_point?.visibility = View.GONE


        LogUtils.d(TAG, "\t\tLayout is ok")
/*
        // textview
        txv_installment_amount          = findViewById(R.id.txv_label_installment_amount) as TextView
 */
        txv_payment_terms = findViewById(R.id.txv_label_payment_terms) as TextView
        txv_label_product_code = findViewById(R.id.txv_label_product_code) as TextView
        txv_label_serialnumber = findViewById(R.id.txv_label_serialnumber) as TextView
        txv_label_quantity = findViewById(R.id.txv_label_quantity) as TextView
        LogUtils.d(TAG, "\t\tTextView is ok")

        // Edittext
//        edtx_installment_amount          = findViewById(R.id.edtx_installment_amount) as EditText
        edtx_payment_terms = findViewById(R.id.edtx_payment_terms) as EditText
        edtx_label_product_code = findViewById(R.id.edtx_product_code) as EditText
        edtx_label_serialnumber = findViewById(R.id.edtx_serialnumber) as TextInputEditText
        edtx_label_quantity = findViewById(R.id.edtx_quantity) as TextInputEditText
        edtx_label_redeem = findViewById(R.id.edtx_redeem_point) as TextInputEditText

        // Add to Edittext arraylist
        // edtx_custom_by_user.add(edtx_installment_amount as EditText)
        edtx_custom_by_user.add(edtx_payment_terms as EditText)
        if (productType == Type.IPP_PROMO) {
            edtx_custom_by_user.add(edtx_label_product_code as EditText)
            edtx_custom_by_user.add(edtx_label_serialnumber as EditText)
            linear_product_code?.visibility = View.VISIBLE
            linear_serialno?.visibility = View.VISIBLE
        }
        if (productType == Type.REDEEM_SPECIAL) {
            edtx_custom_by_user.add(edtx_label_product_code as EditText)
            edtx_custom_by_user.add(edtx_label_serialnumber as EditText)
            linear_payment_terms?.visibility = View.GONE
            linear_redeem_point?.visibility = View.GONE
            linear_product_code?.visibility = View.VISIBLE
            linear_quantity?.visibility = View.VISIBLE
        }
        if (productType == Type.REDEEM) {
            edtx_custom_by_user.add(edtx_label_product_code as EditText)
            edtx_custom_by_user.add(edtx_label_serialnumber as EditText)
            linear_payment_terms?.visibility = View.GONE
            linear_product_code?.visibility = View.GONE
            linear_quantity?.visibility = View.GONE
            linear_redeem_point?.visibility = View.VISIBLE
        }
        LogUtils.d(TAG, "\t\tEditText is ok")


        val requiredField = SpannableString(" *")
        requiredField.setSpan(ForegroundColorSpan(Color.RED), 0, requiredField.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txv_installment_amount?.append(requiredField)
        txv_payment_terms?.append(requiredField)
        txv_label_product_code?.append(requiredField)
        txv_label_quantity?.append(requiredField)

        LogUtils.d(TAG, "InitView--End")
    }

    override fun getTitleString(): String? {
        return navTitle
    }


    fun OnValidateData(): Int {
        LogUtils.d(TAG, "OnValidateData--Start")
        var istm_result = true
        var term_result = true
        var prod_result = true
        var snno_result = true
        var qty_result = true
        try {
            var term_val = ""
            var prod_val = ""
            var snno_val = ""
            var qty_val = ""

            if (linear_payment_terms?.visibility == View.VISIBLE) {
                term_result = false
                edtx_payment_terms?.text?.toString()?.toIntOrNull()?.let {
                    if (it in minimumInstallmentTerms..maximumInstallmentTerms) {
                        term_val = it.toString().padStart(2, '0')
                        term_result = true
                    }
                }
            }

            if (linear_product_code?.visibility == View.VISIBLE) {
                prod_result = false
                edtx_label_product_code?.text?.toString()?.let {
                    if (it.length <= maximumRedemptionProductCodeLen) {
                        prod_val = it
                        prod_result = true;
                    }
                }
            }


            if (linear_serialno?.visibility == View.VISIBLE) {
                snno_result = false
                edtx_label_serialnumber?.text?.toString()?.let {
                    if (it.length >= minimumInstallmentSerialnumber) {
                        snno_val = it
                        snno_result = true;
                    }
                }
            }

            if (linear_quantity?.visibility == View.VISIBLE) {
                qty_result = false
                edtx_label_quantity?.text?.toString()?.let {
                    if (it.length >= minimumInstallmentSerialnumber) {
                        qty_val = it
                        qty_result = true;
                    }
                }
            }

            if (linear_redeem_point?.visibility == View.VISIBLE) {
                qty_result = false
                edtx_label_redeem?.text?.toString()?.let {
                    if (it.length >= minimumInstallmentSerialnumber) {
                        qty_val = it
                        qty_result = true;
                    }
                }
            }

            LogUtils.d(TAG, "Validate-Summary--Start")
            if ((productType == Type.IPP_NORMAL && istm_result && term_result) ||
                (productType == Type.IPP_PROMO && istm_result && term_result && prod_result && snno_result)
            ) {
                val detail = InstallmentDetail(term_val, prod_val, snno_val)
                finish(ActionResult(TransResult.SUCC, detail))
                return TransResult.SUCC
            } else if (productType == Type.REDEEM_SPECIAL && prod_result && qty_result) {
                finish(ActionResult(TransResult.SUCC, RedeemDetails(prod_val, qty_val)))
                return TransResult.SUCC
            } else if (productType == Type.REDEEM) {
                finish(ActionResult(TransResult.SUCC, RedeemDetails("", qty_val)))
                return TransResult.SUCC
            } else {
                LogUtils.d(TAG, " >> VALIDATION FALIED")
                LogUtils.d(TAG, "\t\tMode           = " + productType)
                LogUtils.d(TAG, "\t\tPayment Amount = '" + edtx_installment_amount?.text + "' (" + istm_result + ")")
                LogUtils.d(TAG, "\t\tPayment Terms  = '" + edtx_payment_terms?.text + "' (" + term_result + ")")
                LogUtils.d(TAG, "\t\tProdcut Code   = '" + edtx_label_product_code?.text + "' (" + prod_result + ")")
                LogUtils.d(TAG, "\t\tSerial Number  = '" + edtx_label_serialnumber?.text + "' (" + snno_result + ")")
                DialogUtils.showErrMessage(this, navTitle, "Please correct data", null, Constants.FAILED_DIALOG_SHOW_TIME)
                return TransResult.ERR_DETAILS_VALIDATION_FAILED
            }
        } catch (e: Exception) {
            LogUtils.e(TAG, e)
        }
        LogUtils.d(TAG, "OnValidateData--End")
        return TransResult.ERR_INVALID_DETAILS
    }
}