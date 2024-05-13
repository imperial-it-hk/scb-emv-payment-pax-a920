package com.evp.bizlib.receipt.impl;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.evp.bizlib.config.IConfigUtils;
import com.evp.bizlib.data.entity.TransData;
import com.evp.bizlib.receipt.ReceiptConst;
import com.evp.commonlib.application.BaseApplication;
import com.evp.commonlib.utils.FontCache;
import com.evp.poslib.gl.page.IPage;
import com.evp.poslib.gl.page.PaxGLPage;
import com.sankuai.waimai.router.annotation.RouterService;
import com.sankuai.waimai.router.method.Func3;

import java.util.List;


/**
 * This class works for generate Transaction Detail information
 */
@RouterService(interfaces = Func3.class, key = ReceiptConst.AUDIT_REPORT, singleton = true)
public class AuditReport implements Func3<Context, List<TransData>, IConfigUtils, View> {
    /**
     * Generate receipt view
     *
     * @param context   ApplicatinContext
     * @param transDataList transaction data for generate view
     * @return receipt View
     */
    @Override
    public View call(Context context, @NonNull List<TransData> transDataList, IConfigUtils configUtils) {
        PaxGLPage glPage = new PaxGLPage(context);
        IPage page = glPage.createPage();
        page.adjustLineSpace(-6);
        page.setTypeFace(FontCache.get(FontCache.FONT_NAME, BaseApplication.getAppContext()));

        page = AuditReportDetail.buildReport(page, transDataList, configUtils);
        return glPage.pageToView(page, 384);

    }
}
