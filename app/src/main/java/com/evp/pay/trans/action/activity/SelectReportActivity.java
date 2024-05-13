package com.evp.pay.trans.action.activity;

import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.trans.model.PrintType;
import com.evp.payment.evpscb.R;
import com.evp.settings.NewSpinnerAdapter;

import java.util.List;

public class SelectReportActivity extends BaseActivityWithTickForAction {
    private Acquirer acquirer = null;
    private NewSpinnerAdapter<Acquirer> adapter;

    Button selectSummary;
    Button selectAudit;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_report_type;
    }

    @Override
    protected void initViews() {
        selectSummary = (Button) findViewById(R.id.summary_report);
        selectAudit = (Button) findViewById(R.id.audit_report);
        Spinner spinner = (Spinner) findViewById(R.id.trans_history_acq_list);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.root);
        constraintLayout.setBackgroundColor(secondaryColor);

        if (primaryColor != -1) {
            selectSummary.setBackground(new ColorDrawable(primaryColor));
            selectAudit.setBackground(new ColorDrawable(primaryColor));
        }
        selectSummary.setText(ConfigUtils.getInstance().getString("buttonSummaryReport"));
        selectAudit.setText(ConfigUtils.getInstance().getString("buttonAuditReport"));

        if (adapter.getCount() > 0) {
            spinner.setVisibility(View.VISIBLE);
            spinner.setAdapter(adapter);

            spinner.setSelection(adapter.getListInfo().indexOf(acquirer));
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    Acquirer newAcquirer = adapter.getListInfo().get(pos);
                    if (!newAcquirer.getId().equals(acquirer.getId())) {
                        acquirer = newAcquirer;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Another interface callback
                }
            });
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }


    protected String getTitleString() {
        return getString(R.string.select_report_type);
    }

    @Override
    protected void setListeners() {
        selectSummary.setOnClickListener(this);
        selectAudit.setOnClickListener(this);
    }

    @Override
    protected void loadParam() {
        List<Acquirer> listAcquirers = FinancialApplication.getAcqManager().findAllAcquirers();
        if (acquirer == null) {
            for (Acquirer acq : listAcquirers) {
                if (acq.getName().equals(FinancialApplication.getAcqManager().getCurAcq().getName())) {
                    acquirer = acq;
                    break;
                }
            }
        }

        adapter = new NewSpinnerAdapter<>(this);
        adapter.setListInfo(listAcquirers);
        adapter.setOnTextUpdateListener(new NewSpinnerAdapter.OnTextUpdateListener() {
            @Override
            public String onTextUpdate(final List<?> list, int position) {
                return ((Acquirer) list.get(position)).getName();
            }
        });

    }

    @Override
    public void onClickProtected(View v) {
        switch (v.getId()) {
            case R.id.summary_report:
                finish(new ActionResult(TransResult.SUCC, PrintType.SUMMARY_REPORT, acquirer));
                break;
            case R.id.audit_report:
                finish(new ActionResult(TransResult.SUCC, PrintType.AUDIT_REPORT, acquirer));
                break;
            default:
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
                break;
        }
    }
}
