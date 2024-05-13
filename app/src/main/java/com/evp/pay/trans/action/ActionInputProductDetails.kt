package com.evp.pay.trans.action

import android.content.Context
import android.content.Intent
import com.evp.abl.core.AAction
import com.evp.pay.constant.EUIParamKeys
import com.evp.pay.trans.action.activity.ProductDetailsActivity

class ActionInputProductDetails(listener : ActionStartListener? ) : AAction(listener) {
    var context: Context? = null
    var title: String? = null
    var infoType: String? = null

    fun setParam(context: Context?, title: String?, infoType: String) {
        this.context = context
        this.title = title
        this.infoType = infoType
    }

    override fun process() {
        context?.let {
            var intent = Intent(it, ProductDetailsActivity::class.java)
            .putExtra(EUIParamKeys.NAV_TITLE.toString(), title)
            .putExtra(EUIParamKeys.NAV_BACK.toString(), true)
            .putExtra(EUIParamKeys.IPP_OLS_TYPE.toString(), infoType)
            it.startActivity(intent)
        }
    }
}