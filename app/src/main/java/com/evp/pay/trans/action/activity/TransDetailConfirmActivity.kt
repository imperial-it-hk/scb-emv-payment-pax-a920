package com.evp.pay.trans.action.activity


import android.os.Bundle
import android.view.View
import android.widget.LinearLayout.LayoutParams
import com.evp.abl.core.ActionResult
import com.evp.bizlib.card.PanUtils
import com.evp.bizlib.onlinepacket.TransResult
import com.evp.pay.BaseActivityWithTickForAction
import com.evp.pay.constant.EUIParamKeys
import com.evp.pay.utils.ViewUtils
import com.evp.payment.evpscb.R
import com.evp.payment.evpscb.databinding.TransDetailConfirmBinding

class TransDetailConfirmActivity : BaseActivityWithTickForAction() {
    var navTitle: String? = null
    var navBack = false

    private var leftColumns = ArrayList<String>()
    private var rightColumns = ArrayList<String>()
    private var cardPAN: String? = null
    private var cardHolderName: String? = null

    private lateinit var binding: TransDetailConfirmBinding

    override fun loadParam() {
        val bundle: Bundle? = intent.extras
        navTitle = intent.getStringExtra(EUIParamKeys.NAV_TITLE.toString())
        navBack = intent.getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false)
        if (bundle != null) {
            leftColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString())
            rightColumns = bundle.getStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString())
            cardPAN = bundle.getString("cardPAN")
            cardHolderName = bundle.getString("cardHolderName")
        }
    }

    override fun getLayout(): View {
        binding = TransDetailConfirmBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun getLayoutId(): Int {
        return R.layout.trans_detail_confirm
    }

    override fun getTitleString(): String? {
        return navTitle
    }

    override fun initViews() {
        if (primaryColor != -1)
            binding.barPaymentLayout.setBackgroundColor(primaryColor)

        if (secondaryColor != -1)
            binding.barPaymentLayout.rootView.setBackgroundColor(secondaryColor)

        cardPAN?.let {
            if (it.isNotBlank()) {
                //val masked: String = it.substring(0, 6) + it.substring(it.length - 4).padStart(it.length - 6, '*')
                binding.transCardNumLbl.visibility = View.VISIBLE
                binding.transCardNumTv.visibility = View.VISIBLE
                binding.transCardNumTv.text = PanUtils.separateWithSpace(it)//masked.replace("....".toRegex(), "$0 ")
            }
        }

        cardHolderName?.let {
            if (it.isNotBlank()) {
                binding.transCardHolderLbl.visibility = View.VISIBLE
                binding.transCardHolderTv.visibility = View.VISIBLE
                binding.transCardHolderTv.text = it
            }
        }

        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        params.bottomMargin = 15

        for (i in leftColumns.indices) {
            ViewUtils.genSingleLineLayout(this, leftColumns[i], rightColumns[i])?.let { binding.detailLayout.addView(it, params) }
        }
    }

    override fun setListeners() {
        enableBackAction(navBack)
        binding.confirmBtn.setOnClickListener(this)
    }

    override fun onClickProtected(v: View) {
        if (v.id == R.id.confirm_btn) finish(ActionResult(TransResult.SUCC, null)) else finish(ActionResult(TransResult.ERR_USER_CANCEL, null))
    }

    override fun onKeyBackDown(): Boolean {
        finish(ActionResult(TransResult.ERR_USER_CANCEL, null))
        return true
    }
}
