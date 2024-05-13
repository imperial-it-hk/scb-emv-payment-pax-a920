package com.evp.pay.trans.action.activity;

import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableRow;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.entity.Acquirer;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.pay.BaseActivityWithTickForAction;
import com.evp.pay.app.FinancialApplication;
import com.evp.payment.evpscb.R;

import java.util.ArrayList;
import java.util.List;

public class AcquirerSelectionMenuActivity extends BaseActivityWithTickForAction {
    Button cancelBtn;
    Button okBtn;
    int selectedPosition = -1;
    ArrayList<CheckBox> mCheckBoxes = new ArrayList<CheckBox>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_acquirer_menu;
    }

    @Override
    protected void initViews() {
        okBtn = (Button) findViewById(R.id.confirm);
        cancelBtn = (Button) findViewById(R.id.cancel);
        LinearLayout container = (LinearLayout) findViewById(R.id.checkboxLayout);
        List<Acquirer> acquirerList = FinancialApplication.getAcqManager().findAllAcquirers();

        okBtn.setBackgroundColor(Color.GRAY);
        okBtn.setClickable(false);

        TableRow.LayoutParams temp = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        temp.setMargins(5, 10, 5, 5);
        TableRow allRow = new TableRow(this);
        allRow.setId(0);
        allRow.setLayoutParams(temp);
        CheckBox allCheckBox = new CheckBox(this);
        allCheckBox.setOnClickListener(checkboxOnClick(allCheckBox));
        allCheckBox.setId(0);
        allCheckBox.setText("All Acquires");
        allCheckBox.setScaleY((float) 1.1);
        allCheckBox.setTextSize(25);
        allRow.addView(allCheckBox);
        container.addView(allRow);
        mCheckBoxes.add(allCheckBox);

        for (Acquirer acquirer : acquirerList) {
            TableRow row = new TableRow(this);
            row.setId(acquirer.getId().intValue());
            row.setLayoutParams(temp);
            CheckBox checkBox = new CheckBox(this);
            checkBox.setOnClickListener(checkboxOnClick(checkBox));
            checkBox.setId(acquirer.getId().intValue());
            checkBox.setScaleY((float) 1.1);
            checkBox.setText(acquirer.getName());
            checkBox.setTextSize(25);
            row.addView(checkBox);
            container.addView(row);
            mCheckBoxes.add(checkBox);
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
        return true;
    }

    @Override
    protected String getTitleString() {
        return getString(R.string.last_settlement);
    }

    @Override
    protected void setListeners() {
        cancelBtn.setOnClickListener(btnOnClick(cancelBtn));
        okBtn.setOnClickListener(btnOnClick(okBtn));
    }

    @Override
    protected void loadParam() {

    }

    View.OnClickListener checkboxOnClick(final Button button) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    for (int i = 0; i < mCheckBoxes.size(); i++) {
                        if (mCheckBoxes.get(i) == view) {
                            selectedPosition = i;
                            okBtn.setBackgroundColor(Color.parseColor("#0059A1"));
                            okBtn.setEnabled(true);
                        } else {
                            mCheckBoxes.get(i).setChecked(false);
                        }
                    }

                } else {
                    selectedPosition = -1;
                    okBtn.setBackgroundColor(Color.GRAY);
                    okBtn.setClickable(false);
                }
            }
        };
    }

    View.OnClickListener btnOnClick(final Button button) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.cancel) {
                    finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
                } else {
                    if (selectedPosition != -1) {
                        finish(new ActionResult(TransResult.SUCC, mCheckBoxes.get(selectedPosition).getText()));
                    }
                }
            }
        };
    }
}
