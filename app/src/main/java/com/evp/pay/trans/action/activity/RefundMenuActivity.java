package com.evp.pay.trans.action.activity;

import android.app.ActionBar;
import android.widget.LinearLayout;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.AppConstants;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.payment.evpscb.R;
import com.evp.view.MenuPage;

import java.lang.ref.WeakReference;

public class RefundMenuActivity extends BaseActivityWithTickForAction {
    private MenuPage menuPage;
    private WeakReference<RefundMenuActivity> weakReference;

    public RefundMenuActivity() {
        weakReference = new WeakReference<>(this);
    }

    MenuPage.OnProcessListener onProcessListener = new MenuPage.OnProcessListener() {
        @Override
        public void process(int index) {
            RefundMenuActivity activity = weakReference.get();
            switch (index) {
                case 0:
                    activity.finish(new ActionResult(TransResult.SUCC, AppConstants.SALE_TYPE_CARD));
                    break;
                case 1:
                    activity.finish(new ActionResult(TransResult.SUCC, "QR_PAYMENT"));
                    break;
                default:
                    break;
            }
        }
    };

    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(RefundMenuActivity.this, 8, 4)
                .addActionItem(AppConstants.SALE_TYPE_CARD, R.drawable.insert_card_pay)
                .addActionItem("QR Payment", R.drawable.ic_scan);

        menuPage = builder.create();
        menuPage.setOnProcessListener(onProcessListener);
        return menuPage;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.menu_layout;
    }

    @Override
    protected String getTitleString() {
        return "Refund";
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
    protected void loadParam() {

    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }
}
