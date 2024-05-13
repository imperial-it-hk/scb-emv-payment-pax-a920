package com.evp.pay.trans.action.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.device.Device;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.constant.Constants;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.record.PrinterUtils;
import com.evp.pay.utils.QrCodeGenerartor;
import com.evp.payment.evpscb.R;
import com.google.zxing.WriterException;

import java.io.ByteArrayOutputStream;

public class QRCodeActivity extends BaseActivityWithTickForAction {
    private String title;
    private String qrCode;
    private Button cancelBtn;
    private Button printBtn;
    private Button okBtn;
    private Bitmap qrCodeBitmap;
    private ImageView paymentIcon;
    private TextView amountTextView;
    private String fundingSource;
    private String fundingSourceImagePath;
    private String amount;
    private String currency;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_q_r_code;
    }

    @Override
    protected void initViews() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.root);
        linearLayout.setBackgroundColor(secondaryColor);
        paymentIcon = (ImageView) findViewById(R.id.bar_payment_icon);
        amountTextView = (TextView) findViewById(R.id.title_textview);
        ImageView qrCodeImageView = (ImageView) findViewById(R.id.idIVQrcode);
        cancelBtn = (Button) findViewById(R.id.cancel_btn);
        cancelBtn.setText(ConfigUtils.getInstance().getString("buttonCancel"));
        printBtn = (Button) findViewById(R.id.print_btn);
        printBtn.setText(ConfigUtils.getInstance().getString("buttonPrint"));
        okBtn = (Button) findViewById(R.id.ok_btn);
        okBtn.setText(ConfigUtils.getInstance().getString("buttonOk"));
        if (qrCodeBitmap != null) {
            qrCodeImageView.setImageBitmap(qrCodeBitmap);
        }
        amountTextView.setText(title);
        paymentIcon.setImageBitmap(ConfigUtils.getInstance().getResourceFile(fundingSourceImagePath));


        if (primaryColor != -1) {
            paymentIcon.setBackground(new ColorDrawable(primaryColor));
            amountTextView.setBackground(new ColorDrawable(primaryColor));
        }
    }

    @Override
    protected String getTitleString() {
        return "";
    }

    @Override
    protected void setListeners() {
        cancelBtn.setOnClickListener(this);
        printBtn.setOnClickListener(this);
        okBtn.setOnClickListener(this);
    }

    @Override
    protected void loadParam() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            qrCode = extras.getString("qrcode");
        }
        try {
            qrCodeBitmap = new QrCodeGenerartor().encodeBitmap(qrCode);
        } catch (WriterException e) {
            finish(new ActionResult(TransResult.ERR_RECV, null));
        }

        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        fundingSource = getIntent().getStringExtra(EUIParamKeys.FUNDING_SOURCE.toString());
        fundingSourceImagePath = getIntent().getStringExtra("fundingSourceImagePath");
        currency = getIntent().getStringExtra("currency");
        amount = getIntent().getStringExtra("amount");
    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.cancel_btn:
                cancelTrans();
                break;
            case R.id.print_btn:
                print();
                break;
            case R.id.ok_btn:
                confirmAmount();
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    public void confirmAmount() {
        finish(new ActionResult(TransResult.SUCC, null));
        return;
    }

    public void cancelTrans() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return;
    }

    public void print() {
        if (qrCodeBitmap != null) {
            FinancialApplication.getApp().runInBackground(new Runnable() {
                @Override
                public void run() {
                    Bitmap logo;
                    switch(fundingSource) {
                        case AppConstants.FUNDING_SRC_LINEPAY:
                            logo = ConfigUtils.getInstance().getPrintResourceFile(ConfigConst.PRINT_LINEPAY_LOGO);
                            break;
                        case AppConstants.FUNDING_SRC_ALIPAY:
                            logo = ConfigUtils.getInstance().getPrintResourceFile(ConfigConst.PRINT_ALIPAY_LOGO);
                            break;
                        case AppConstants.FUNDING_SRC_SHOPEE:
                            logo = ConfigUtils.getInstance().getPrintResourceFile(ConfigConst.PRINT_SHOPEE_LOGO);
                            break;
                        case AppConstants.FUNDING_SRC_PROMPTPAY:
                            logo = ConfigUtils.getInstance().getPrintResourceFile(ConfigConst.PRINT_PROMPTPAY_LOGO);
                            break;
                        case AppConstants.FUNDING_SRC_QRCS:
                            logo = ConfigUtils.getInstance().getPrintResourceFile(ConfigConst.PRINT_QRCS_LOGO);
                            break;
                        case AppConstants.FUNDING_SRC_WECHAT:
                            logo = ConfigUtils.getInstance().getPrintResourceFile(ConfigConst.PRINT_WECHAT_LOGO);
                            break;
                        case AppConstants.FUNDING_SRC_TRUE_MONEY:
                            logo = ConfigUtils.getInstance().getPrintResourceFile(ConfigConst.PRINT_TRUE_MONEY_LOGO);
                            break;
                        default:
                            logo = ConfigUtils.getInstance().getPrintResourceFile("");
                            break;
                    }
                    PrinterUtils.printQrCode(
                            qrCodeBitmap,
                            logo,
                            amount,
                            currency,
                            Device.getTime(Constants.DATE_PATTERN),
                            Device.getTime(Constants.TIME_PATTERN),
                            QRCodeActivity.this
                    );
                }
            });
        }
        return;
    }

    private String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String result = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return result;
    }
}