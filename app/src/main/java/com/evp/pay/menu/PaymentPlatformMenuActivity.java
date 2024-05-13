package com.evp.pay.menu;

import android.app.ActionBar;
import android.widget.LinearLayout;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;

import java.lang.ref.WeakReference;

public class PaymentPlatformMenuActivity extends BaseActivityWithTickForAction {
    private MenuPage menuPage;
    private WeakReference<PaymentPlatformMenuActivity> weakReference;

    private String transAmount = "";
    private String actionType = "";
    private String transactionType = "";
    private String title = "";

    public PaymentPlatformMenuActivity() {
        weakReference = new WeakReference<>(this);
    }

    MenuPage.OnProcessListener onProcessListener = new MenuPage.OnProcessListener() {
        @Override
        public void process(int index) {
            PaymentPlatformMenuActivity activity = weakReference.get();
            switch (index) {
                case 0:
                    activity.finish(new ActionResult(TransResult.SUCC, "alipay"));
                    break;
                case 1:
                    activity.finish(new ActionResult(TransResult.SUCC, "wechat"));
                    break;
                case 2:
                    activity.finish(new ActionResult(TransResult.SUCC, "promptpay"));
                    break;
                case 3:
                    activity.finish(new ActionResult(TransResult.SUCC, "linepay"));
                    break;
                case 4:
                    activity.finish(new ActionResult(TransResult.SUCC, "true_money"));
                    break;
                case 5:
                    activity.finish(new ActionResult(TransResult.SUCC, "shopee"));
                    break;
                case 6:
                    activity.finish(new ActionResult(TransResult.SUCC, "dolfin"));
                    break;
                case 7:
                    activity.finish(new ActionResult(TransResult.SUCC, "qrcs"));
                    break;
                default:
                    break;
            }
        }
    };

    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(PaymentPlatformMenuActivity.this, 9, 3)
                .addActionItem("Alipay", R.drawable.ic_payment_alipay)
                .addActionItem("Wechat", R.drawable.ic_payment_wechat)
                .addActionItem("PromptPay", R.drawable.ic_payment_promptpay)
                .addActionItem("LinePay", R.drawable.ic_payment_linepay)
                .addActionItem("TrueMoney", R.drawable.ic_payment_true_money)
                .addActionItem("Shopee", R.drawable.ic_payment_shopee)
                .addActionItem("Dolfin", R.drawable.ic_payment_shopee)
                .addActionItem("QRCS Visa", R.drawable.ic_payment_shopee)
                .addActionItem("QRCS Master", R.drawable.ic_payment_shopee);

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

        android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT);

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
