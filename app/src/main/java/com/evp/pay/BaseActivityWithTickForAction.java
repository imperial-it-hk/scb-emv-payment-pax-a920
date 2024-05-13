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
package com.evp.pay;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.evp.abl.core.AAction;
import com.evp.abl.core.ActionResult;
import com.evp.bizlib.onlinepacket.TransResult;
import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.trans.TransContext;
import com.evp.pay.utils.TickTimer;

/**
 * The type Base activity with tick for action.
 */
public abstract class BaseActivityWithTickForAction extends BaseActivity {
    /**
     * The Tick timer.
     */
    protected TickTimer tickTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tickTimer = new TickTimer(new TickTimer.OnTickTimerListener() {
            @Override
            public void onTick(long leftTime) {
                LogUtils.i(TAG, "onTick:" + leftTime);
            }

            @Override
            public void onFinish() {
                onTimerFinish();
            }
        });
        tickTimer.start();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tickTimer.stop();
    }

    /**
     * Finish.
     *
     * @param result the result
     */
    public void finish(ActionResult result) {
        tickTimer.stop();
        AAction action = TransContext.getInstance().getCurrentAction();
        if (action != null) {
            if (action.isFinished()) {
                return;
            }
            action.setFinished(true);
            quickClickProtection.start(); // AET-93
            action.setResult(result);
        } else {
            finish();
        }
    }

    @Override
    protected boolean onKeyBackDown() {
        finish(new ActionResult(TransResult.ERR_ABORTED, null));
        return true;
    }

    @Override
    protected boolean onKeyDel() {
        finish(new ActionResult(TransResult.ERR_ABORTED, null));
        return true;
    }

    @Override
    protected boolean onOptionsItemSelectedSub(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(new ActionResult(TransResult.ERR_USER_CANCEL, null));
            return true;
        }
        return super.onOptionsItemSelectedSub(item);
    }

    /**
     * On timer finish.
     */
    protected void onTimerFinish() {
        finish(new ActionResult(TransResult.ERR_TIMEOUT, null));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (tickTimer != null){
            tickTimer.start();
        }
    }
}
