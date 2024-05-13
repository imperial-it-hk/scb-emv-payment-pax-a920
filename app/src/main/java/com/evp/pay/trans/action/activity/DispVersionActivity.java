/*
 * ===========================================================================================
 * = COPYRIGHT
 *          PAX Computer Technology(Shenzhen) CO., LTD PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or nondisclosure
 *   agreement with PAX Computer Technology(Shenzhen) CO., LTD and may not be copied or
 *   disclosed except in accordance with the terms in that agreement.
 *     Copyright (C) 2019-? PAX Computer Technology(Shenzhen) CO., LTD All rights reserved.
 * Description: // Detail description about the function of this module,
 *             // interfaces with the other modules, and dependencies.
 * Revision History:
 * Date                  Author	                 Action
 * 20190108  	         Steven.W                Create
 * ===========================================================================================
 */
package com.evp.pay.trans.action.activity;

import static com.pax.jemv.clcommon.RetCode.EMV_OK;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.TickTimer;
import com.evp.payment.evpscb.R;
import com.evp.poslib.PosLibVersion;
import com.pax.jemv.amex.api.ClssAmexApi;
import com.pax.jemv.clcommon.ByteArray;
import com.pax.jemv.dpas.api.ClssDPASApi;
import com.pax.jemv.emv.api.EMVApi;
import com.pax.jemv.entrypoint.api.ClssEntryApi;
import com.pax.jemv.jcb.api.ClssJCBApi;
import com.pax.jemv.paypass.api.ClssPassApi;
import com.pax.jemv.paywave.api.ClssWaveApi;
import com.pax.jemv.qpboc.api.ClssPbocApi;
import com.pax.jemv.rupay.api.ClssRuPayApi;

/**
 * The type Disp version activity.
 */
public class DispVersionActivity extends BaseActivityWithTickForAction {
    static {
        System.loadLibrary("F_DEVICE_LIB_PayDroid");
        System.loadLibrary("F_PUBLIC_LIB_PayDroid");

        System.loadLibrary("F_AE_LIB_PayDroid");
        System.loadLibrary("JNI_AE_v101");

        System.loadLibrary("F_DPAS_LIB_PayDroid");
        System.loadLibrary("JNI_DPAS_v100");

        System.loadLibrary("F_EMV_LIBC_PayDroid");
        System.loadLibrary("F_EMV_LIB_PayDroid");
        System.loadLibrary("JNI_EMV_v103");

        System.loadLibrary("F_ENTRY_LIB_PayDroid");
        System.loadLibrary("JNI_ENTRY_v103");

        System.loadLibrary("F_JCB_LIB_PayDroid");
        System.loadLibrary("JNI_JCB_v100");

        System.loadLibrary("F_MC_LIB_PayDroid");
        System.loadLibrary("JNI_MC_v100_01");

        System.loadLibrary("F_QPBOC_LIB_PayDroid");
        System.loadLibrary("JNI_QPBOC_v100");

        System.loadLibrary("F_RUPAY_LIB_PayDroid");
        System.loadLibrary("JNI_RUPAY_v100");

        System.loadLibrary("F_WAVE_LIB_PayDroid");
        System.loadLibrary("JNI_WAVE_v100");

    }
    private Button btnConfirm;

    private String navTitle;
    private String prompt;
    private String content;
    private int tickTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tickTimer.start(tickTime);
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        prompt = bundle.getString(EUIParamKeys.PROMPT_1.toString());
        content = bundle.getString(EUIParamKeys.CONTENT.toString());
        tickTime = bundle.getInt(EUIParamKeys.TIKE_TIME.toString(), TickTimer.DEFAULT_TIMEOUT);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_disp_version_layout;
    }

    @Override
    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void initViews() {
        enableBackAction(true);
        TextView appVersion = (TextView) findViewById(R.id.app_version_value);
        TextView aeVersion = (TextView) findViewById(R.id.ae_version_value);
        TextView dpasVersion = (TextView) findViewById(R.id.dpas_version_value);
        TextView emvVersion = (TextView) findViewById(R.id.emv_version_value);
        TextView entryVersion = (TextView) findViewById(R.id.entry_version_value);
        TextView jcbVersion = (TextView) findViewById(R.id.jcb_version_value);
        TextView mcVersion = (TextView) findViewById(R.id.mc_version_value);
        TextView qpbocVersion = (TextView) findViewById(R.id.qpboc_version_value);
        TextView rupayVersion = (TextView) findViewById(R.id.rupay_version_value);
        TextView waveVersion = (TextView) findViewById(R.id.wave_version_value);

        TextView neptuneVersion = (TextView) findViewById(R.id.neptune_version_value);
        TextView glCommVersion = (TextView) findViewById(R.id.gl_comm_version_value);
        TextView glExprinterVersion = (TextView) findViewById(R.id.gl_exprinter_version_value);
        TextView glPackerVersion = (TextView) findViewById(R.id.gl_packer_version_value);
        TextView glImprocessingVersion = (TextView) findViewById(R.id.gl_imgprocessing_version_value);
        TextView glUtilsVersion = (TextView) findViewById(R.id.gl_utils_version_value);
        TextView glBaiFuTongVersion = (TextView) findViewById(R.id.gl_baifutong_version_value);
        TextView baseLinkApiVersion = (TextView) findViewById(R.id.baselink_api_version_value);
        appVersion.setText(content);
        ByteArray version = new ByteArray();
        int i = EMVApi.EMVReadVerInfo(version);
        if (i ==EMV_OK){
            emvVersion.setText(new String(version.data));
        }
        version = new ByteArray();
        i = ClssDPASApi.Clss_ReadVerInfo_DPAS(version);
        if (i ==EMV_OK){
            dpasVersion.setText(new String(version.data));
        }
        version = new ByteArray();
        i = ClssAmexApi.Clss_ReadVerInfo_AE(version);
        if (i ==EMV_OK){
            aeVersion.setText(new String(version.data));
        }
        version = new ByteArray();
        i =ClssEntryApi.Clss_ReadVerInfo_Entry(version);
        if (i ==EMV_OK){
            entryVersion.setText(new String(version.data));
        }
        version = new ByteArray();
        i =ClssJCBApi.Clss_ReadVerInfo_JCB(version);
        if (i ==EMV_OK){
            jcbVersion.setText(new String(version.data));
        }

        version = new ByteArray();
        i = ClssPassApi.Clss_ReadVerInfo_MC(version);
        if (i ==EMV_OK){
            mcVersion.setText(new String(version.data));
        }

        version = new ByteArray();
        i = ClssPbocApi.Clss_ReadVerInfo_Pboc(version);
        if (i ==EMV_OK){
            qpbocVersion.setText(new String(version.data));
        }

        version = new ByteArray();
        i = ClssRuPayApi.Clss_ReadVerInfo_RuPay(version);
        if (i ==EMV_OK){
            rupayVersion.setText(new String(version.data));
        }

        version = new ByteArray();
        i = ClssWaveApi.Clss_ReadVerInfo_Wave(version);
        if (i ==EMV_OK){
            waveVersion.setText(new String(version.data));
        }
        neptuneVersion.setText(PosLibVersion.getNeptuneLiteApiVersion());
        neptuneVersion.setBackgroundColor(secondaryColor);
        glCommVersion.setText(PosLibVersion.getGlCommVersion());
        glCommVersion.setBackgroundColor(secondaryColor);
        glExprinterVersion.setText(PosLibVersion.getGlExPrinterVersion());
        glExprinterVersion.setBackgroundColor(secondaryColor);
        glPackerVersion.setText(PosLibVersion.getGlPackerVersion());
        glPackerVersion.setBackgroundColor(secondaryColor);
        glImprocessingVersion.setText(PosLibVersion.getGlImprocessingVersion());
        glImprocessingVersion.setBackgroundColor(secondaryColor);
        glUtilsVersion.setText(PosLibVersion.getGlUtilsVersion());
        glUtilsVersion.setBackgroundColor(secondaryColor);
        glBaiFuTongVersion.setText(PosLibVersion.getGlBaiFuTongVersion());
        glBaiFuTongVersion.setBackgroundColor(secondaryColor);
        baseLinkApiVersion.setText(PosLibVersion.getBaseLinkApiVersion());
        baseLinkApiVersion.setBackgroundColor(secondaryColor);
        btnConfirm = (Button) findViewById(R.id.confirm_btn);
        btnConfirm.setBackgroundColor(primaryColor);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.root);
        linearLayout.setBackgroundColor(secondaryColor);
    }

    @Override
    protected void setListeners() {
        btnConfirm.setOnClickListener(this);

    }

    @Override
    public void onClickProtected(View v) {
        if (v.getId() == R.id.confirm_btn)
            finish(new ActionResult(TransResult.SUCC, null));
    }

    @Override
    protected boolean onOptionsItemSelectedSub(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(new ActionResult(TransResult.ERR_ABORTED, null));
            return true;
        }
        return super.onOptionsItemSelectedSub(item);
    }
}
