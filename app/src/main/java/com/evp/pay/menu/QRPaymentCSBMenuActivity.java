package com.evp.pay.menu;

import android.app.ActionBar;
import android.widget.LinearLayout;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;

import java.lang.ref.WeakReference;

public class QRPaymentCSBMenuActivity extends BaseActivityWithTickForAction {
    private MenuPage menuPage;
    private WeakReference<QRPaymentCSBMenuActivity> weakReference;

    private String transAmount = "";
    private String actionType = "";
    private String transactionType = "";
    private String title = "";

    public QRPaymentCSBMenuActivity() {
        weakReference = new WeakReference<>(this);
    }

    MenuPage.OnProcessListener onProcessListener = new MenuPage.OnProcessListener() {
        @Override
        public void process(String index) {
            QRPaymentCSBMenuActivity activity = weakReference.get();
            String fundingSource, fundingSourceImage;
            switch(index) {
                case ConfigConst.TRANSACTION_ALIPAY:
                    fundingSource = AppConstants.FUNDING_SRC_ALIPAY;
                    fundingSourceImage = ConfigUtils.getInstance().getDeviceConf(ConfigConst.BANNER_FOR_ALIPAY);
                    break;
                case ConfigConst.TRANSACTION_WECHAT:
                    fundingSource = AppConstants.FUNDING_SRC_WECHAT;
                    fundingSourceImage = ConfigUtils.getInstance().getDeviceConf(ConfigConst.BANNER_FOR_WECHAT);
                    break;
                case ConfigConst.TRANSACTION_PROMPTPAY:
                    fundingSource = AppConstants.FUNDING_SRC_PROMPTPAY;
                    fundingSourceImage = ConfigUtils.getInstance().getDeviceConf(ConfigConst.BANNER_FOR_PROMPTPAY);
                    break;
                case ConfigConst.TRANSACTION_SHOPEE:
                    fundingSource = AppConstants.FUNDING_SRC_SHOPEE;
                    fundingSourceImage = ConfigUtils.getInstance().getDeviceConf(ConfigConst.BANNER_FOR_SHOPEE);
                    break;
                case ConfigConst.TRANSACTION_QRCS:
                    fundingSource = AppConstants.FUNDING_SRC_QRCS;
                    fundingSourceImage = ConfigUtils.getInstance().getDeviceConf(ConfigConst.BANNER_FOR_QRCS);
                    break;
                case ConfigConst.TRANSACTION_TRUE_MONEY:
                    fundingSource = AppConstants.FUNDING_SRC_TRUE_MONEY;
                    fundingSourceImage = ConfigUtils.getInstance().getDeviceConf(ConfigConst.BANNER_FOR_TRUE_MONEY);
                    break;
                case ConfigConst.TRANSACTION_LINEPAY:
                    fundingSource = AppConstants.FUNDING_SRC_LINEPAY;
                    fundingSourceImage = ConfigUtils.getInstance().getDeviceConf(ConfigConst.BANNER_FOR_LINEPAY);
                    break;
                default:
                    fundingSource = "";
                    fundingSourceImage = "";
                    break;
            }
            activity.finish(new ActionResult(TransResult.SUCC, fundingSource, fundingSourceImage));
        }

        @Override
        public void process(int index) {
            LogUtils.i(TAG, "NOT IN USE!!!");
        }
    };

    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(QRPaymentCSBMenuActivity.this, 9, 3);
        ConfigUtils.getInstance().getMenu(builder, ConfigConst.MenuType.QR_CSB_MENU);
        menuPage = builder.create();
        menuPage.setOnProcessListener(onProcessListener);
        return menuPage;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.menu_layout;
    }

    @Override
    protected void initViews() {
        LinearLayout llContainer = (LinearLayout) findViewById(R.id.ll_container);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);
        llContainer.setBackgroundColor(secondaryColor);
        llContainer.addView(createMenuPage(), params);
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    public void loadParam() {

    }
}

