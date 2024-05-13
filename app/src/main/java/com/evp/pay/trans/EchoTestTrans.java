package com.evp.pay.trans;

import android.content.Context;

import com.evp.abl.core.ActionResult;
import com.evp.bizlib.data.model.ETransType;
import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.trans.action.ActionEchoTest;

public class EchoTestTrans  extends BaseTrans {

    public EchoTestTrans(Context context, TransEndListener transListener) {
        super(context, ETransType.ECHO, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        ActionEchoTest amountAction = new ActionEchoTest(action -> ((ActionEchoTest) action)
                .setParam(
                        getCurrentContext(),
                        ConfigUtils.getInstance().getString(ConfigConst.LABEL_TRANS_ECHO)
                )
        );
        bind(State.TEST_ECHO.toString(), amountAction, true);

        gotoState(State.TEST_ECHO.toString());
    }

    enum State {
        TEST_ECHO
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        transEnd(result);
    }
}
