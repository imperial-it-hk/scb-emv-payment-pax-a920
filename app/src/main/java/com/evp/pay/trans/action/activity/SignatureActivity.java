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
 * 20200320              Joshua.Huang            Modify
 * ===========================================================================================
 */
package com.evp.pay.trans.action.activity;

import static com.evp.poslib.gl.convert.IConvert.EEndian.LITTLE_ENDIAN;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.currency.CurrencyConverter;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.ToastUtils;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;
import com.evp.poslib.gl.convert.ConvertHelper;
import com.evp.poslib.gl.impl.GL;
import com.evp.view.ElectronicSignatureView;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * The type Signature activity.
 */
public class SignatureActivity extends BaseActivityWithTickForAction {

    private ElectronicSignatureView mSignatureView;

    private RelativeLayout writeUserName = null;

    private Button clearBtn;
    private Button confirmBtn;

    private String amount;
    private String point;

    private boolean processing = false;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_signature;
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        amount = bundle.getString(EUIParamKeys.TRANS_AMOUNT.toString());
        point = bundle.getString(EUIParamKeys.TRANS_POINT.toString());
    }

    @Override
    protected String getTitleString() {
        return ConfigUtils.getInstance().getString("trans_signature");
    }

    @Override
    protected void initViews() {
        enableBackAction(false);

        TextView amountText = (TextView) findViewById(R.id.trans_amount_tv);

        //region OLS
        if (point != null) {
            LinearLayout linear_point = (LinearLayout) findViewById(R.id.trans_point_layout);
            linear_point.setVisibility(View.VISIBLE);

            TextView pointLabel = (TextView) findViewById(R.id.trans_point_lbl);
            pointLabel.setText(ConfigUtils.getInstance().getString("confirm_redeem_point"));

            TextView pointText = (TextView) findViewById(R.id.trans_point_tv);
            pointText.setText(point);

            TextView amountLabel = (TextView) findViewById(R.id.trans_amount_lbl);
            amountLabel.setText(ConfigUtils.getInstance().getString("confirm_credit_total"));
        } else
        //endregion
        {
            TextView amountLabel = (TextView) findViewById(R.id.trans_amount_lbl);
            amountLabel.setText(ConfigUtils.getInstance().getString("confirm_amount"));
            amount = CurrencyConverter.convert(Utils.parseLongSafe(amount, 0));
        }
        amountText.setText(amount);

        writeUserName = (RelativeLayout) findViewById(R.id.writeUserNameSpace);
        mSignatureView = new ElectronicSignatureView(SignatureActivity.this);


        mSignatureView.setSampleRate(5);
        mSignatureView.setBitmap(new Rect(0, 0, 474, 158), 10, Color.WHITE);
        writeUserName.addView(mSignatureView);

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

        clearBtn = (Button) findViewById(R.id.clear_btn);
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        confirmBtn.requestFocus();
    }

    @Override
    protected void setListeners() {
        clearBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);
    }

    @Override
    protected boolean onKeyBackDown() {
        ToastUtils.showMessage(R.string.err_not_allowed);
        return true;
    }

    @Override
    protected boolean onKeyDel() {
        onClearClicked();
        return true;
    }

    @Override
    public void onClickProtected(View v) {

        switch (v.getId()) {
            case R.id.clear_btn:
                onClearClicked();
                break;
            case R.id.confirm_btn:
                onOKClicked();
                break;
            default:
                break;
        }
    }

    /**
     * On clear clicked.
     */
    public void onClearClicked() {
        if (isProcessing()) {
            return;
        }
        setProcessFlag();
        mSignatureView.clear();
        clearProcessFlag();
    }

    /**
     * On ok clicked.
     */
    public void onOKClicked() {
        if (isProcessing()) {
            return;
        }
        setProcessFlag();
        if (!mSignatureView.getTouched()) {
            LogUtils.i("touch", "no touch");
            clearProcessFlag();
            return;
        }
        Bitmap bitmap = mSignatureView.save(true, 0);
        // 保存签名图片
        byte[] data = GL.getGL().getImgProcessing().bitmapToJbig(bitmap, Constants.rgb2MonoAlgo);

        if (data.length > 999) {
            ToastUtils.showMessage(R.string.signature_redo);
            setProcessFlag();
            mSignatureView.clear();
            clearProcessFlag();
            return;
        }
        clearProcessFlag();

        byte[] signPath = genSignPos();
        finish(new ActionResult(TransResult.SUCC, data, signPath));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == android.R.id.home || super.onOptionsItemSelected(item);
    }

    private byte[] genSignPos() {
        List<float[]> signPos = mSignatureView.getPathPos();
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        for (float[] i : signPos) {
            if (i[0] < 0 && i[1] < 0) {
                swapStream.write(ConvertHelper.getConvert().intToByteArray(0xFFFFFFFF, LITTLE_ENDIAN), 0, 4);
            } else {
                byte[] bytes = ConvertHelper.getConvert().shortToByteArray((short) i[0], LITTLE_ENDIAN);
                swapStream.write(bytes, 0, 2);
                bytes = ConvertHelper.getConvert().shortToByteArray((short) i[1], LITTLE_ENDIAN);
                swapStream.write(bytes, 0, 2);
            }
        }
        return swapStream.toByteArray();
    }

    /**
     * Sets process flag.
     */
    protected void setProcessFlag() {
        processing = true;
    }

    /**
     * Clear process flag.
     */
    protected void clearProcessFlag() {
        processing = false;
    }

    /**
     * Is processing boolean.
     *
     * @return the boolean
     */
    protected boolean isProcessing() {
        return processing;
    }
}