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
package com.evp.view.dialog;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.app.FinancialApplication;
import com.evp.pay.utils.TickTimer;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

import java.util.List;

/**
 * The type Custom alert dialog.
 */
public class CustomAlertDialog extends Dialog implements android.view.View.OnClickListener {

    private View mDialogView;
    private int mAlertType;
    private boolean mTouchToDimiss = false;

    private TickTimer tickTimer;

    /**
     * 标题 title TextView
     */
    private TextView mTitleTextView;
    /**
     * 内容 content TextView
     */
    private TextView mContentTextView;

    private TextView mNormalTextView;
    /**
     * 带有图片
     */
    private ImageView mImageView;
    /**
     * 带有EditText的输入框布局
     */
    private LinearLayout mPwdLayout;
    private EditText mEditText;
    private TextView mErrTipsView;

    /**
     * 圆形进度条Progress布局
     */
    private FrameLayout mProgressLayout;
    private ProgressHelper mProgressHelper;
    /**
     * 显示倒计时的TextView
     */
    private TextView mCountView;

    /**
     * 操作失败的布局
     */
    private FrameLayout mErrorFrame;
    private ImageView mErrorX;
    private Animation mErrorInAnim;
    private AnimationSet mErrorXInAnim;
    /**
     * 操作成功的布局
     */
    private FrameLayout mSuccessFrame;
    private SuccessTickView mSuccessTick;
    /**
     * 取消、确定按钮 布局
     */
    private LinearLayout mButtonLayout;
    private Button mConfirmButton;
    private Button mCancelButton;

    private String mTitleText;
    private String mContentText;
    private String mNormalText;

    private boolean mShowTitle;
    /**
     * 是否显示Cancel Button
     */
    private boolean mShowCancel;
    /**
     * 是否显示Content TextView
     */
    private boolean mShowContent;
    /**
     * 是否显示Confirm Button
     */
    private boolean mShowConfirm;
    private boolean mShowNormal;
    private boolean mShowImage;

    private View mSuccessLeftMask;
    private View mSuccessRightMask;

    private AnimationSet mSuccessLayoutAnimSet;
    private Animation mSuccessBowAnim;

    private String mConfirmText;
    private String mCancelText;

    /**
     * 为Button设置监听事件
     */
    private OnCustomClickListener mCancelClickListener;
    private OnCustomClickListener mConfirmClickListener;

    /**
     * The constant NORMAL_TYPE.
     */
    public static final int NORMAL_TYPE = 0;
    /**
     * The constant ERROR_TYPE.
     */
    public static final int ERROR_TYPE = 1;
    /**
     * The constant SUCCESS_TYPE.
     */
    public static final int SUCCESS_TYPE = 2;
    /**
     * The constant CUSTOM_ENTER_TYPE.
     */
    public static final int CUSTOM_ENTER_TYPE = 3;
    /**
     * The constant PROGRESS_TYPE.
     */
    public static final int PROGRESS_TYPE = 4;
    /**
     * The constant IMAGE_TYPE.
     */
    public static final int IMAGE_TYPE = 5;
    /**
     * The constant WARN_TYPE.
     */
    public static final int WARN_TYPE = 6;


    /**
     * The interface On custom click listener.
     */
    public interface OnCustomClickListener {
        /**
         * On click.
         *
         * @param alertDialog the alert dialog
         */
        void onClick(CustomAlertDialog alertDialog);
    }

    /**
     * Instantiates a new Custom alert dialog.
     *
     * @param context the context
     */
    public CustomAlertDialog(Context context) {
        this(context, NORMAL_TYPE);
    }

    /**
     * Instantiates a new Custom alert dialog.
     *
     * @param context   the context
     * @param alertType the alert type
     */
    public CustomAlertDialog(Context context, int alertType) {
        super(context, R.style.AlertDialog);
        this.mAlertType = alertType;
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        mProgressHelper = new ProgressHelper(context);

        mErrorInAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.error_frame_in);
        mErrorXInAnim = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(), R.anim.error_x_in);

        mSuccessBowAnim = OptAnimationLoader.loadAnimation(getContext(), R.anim.success_bow_roate);
        mSuccessLayoutAnimSet = (AnimationSet) OptAnimationLoader.loadAnimation(getContext(),
                R.anim.success_mask_layout);

    }

    /**
     * Instantiates a new Custom alert dialog.
     *
     * @param context   the context
     * @param alertType the alert type
     * @param timeout   the timeout
     */
    public CustomAlertDialog(Context context, int alertType, int timeout) {
        this(context, alertType);
        setTimeout(timeout);
    }

    /**
     * Instantiates a new Custom alert dialog.
     *
     * @param context       the context
     * @param alertType     the alert type
     * @param touchToDimiss the touch to dimiss
     * @param timeout       the timeout
     */
    public CustomAlertDialog(Context context, int alertType, boolean touchToDimiss, int timeout) {
        this(context, alertType, timeout);
        this.mTouchToDimiss = touchToDimiss;
    }


    @Override
    public void dismiss() {
        super.dismiss();
        if (tickTimer != null) {
            tickTimer.stop();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_dialog_layout);

        Window window = getWindow();
        if (window == null)
            return;
        mDialogView = window.getDecorView().findViewById(android.R.id.content);

        mNormalTextView = (TextView) findViewById(R.id.normal_text);

        mImageView = (ImageView) findViewById(R.id.custom_image);

        mPwdLayout = (LinearLayout) findViewById(R.id.input_edtxt_layout);
        mEditText = (EditText) findViewById(R.id.input_edtxt);
        mErrTipsView = (TextView) findViewById(R.id.input_err_tips);

        mProgressLayout = (FrameLayout) findViewById(R.id.progress_dialog);
        mProgressHelper.setProgressWheel((ProgressWheel) findViewById(R.id.progressWheel));
        mCountView = (TextView) findViewById(R.id.countView);

        mErrorFrame = (FrameLayout) findViewById(R.id.error_frame);
        mErrorX = (ImageView) mErrorFrame.findViewById(R.id.error_x);
        mSuccessFrame = (FrameLayout) findViewById(R.id.success_frame);
        mSuccessTick = (SuccessTickView) mSuccessFrame.findViewById(R.id.success_tick);
        mSuccessLeftMask = mSuccessFrame.findViewById(R.id.mask_left);
        mSuccessRightMask = mSuccessFrame.findViewById(R.id.mask_right);

        mButtonLayout = (LinearLayout) findViewById(R.id.button_layout);
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);

        mTitleTextView = (TextView) findViewById(R.id.title_text);
        mContentTextView = (TextView) findViewById(R.id.content_text);

        mConfirmButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);

        setTitleText(mTitleText);
        setContentText(mContentText);

        setCancelText(mCancelText);
        setConfirmText(mConfirmText);
        mConfirmButton.setBackground(new ColorDrawable(Color.parseColor(ConfigUtils.getInstance().getDeviceConf(ConfigConst.PRIMARY_COLOR))));

        changeAlertType(mAlertType, true);

    }

    /**
     * 获取标题Title
     *
     * @return title text
     */
    public String getTitleText() {
        return mTitleText;
    }

    /**
     * Sets timeout.
     *
     * @param time the time
     */
    public void setTimeout(int time) {
        // 当设置超时时间小于1，不显示倒计时
        if (time <= 0) {
            if (mCountView != null) {
                mCountView.setVisibility(View.GONE);
            }
            return;
        }
        if (tickTimer != null) {
            tickTimer.stop();
        }
        tickTimer = new TickTimer(new TickTimer.OnTickTimerListener() {
            @Override
            public void onTick(long leftTime) {
                String tick = leftTime + getContext().getString(R.string.sec);
                mCountView.setText(tick);
            }

            @Override
            public void onFinish() {
                CustomAlertDialog.super.dismiss();

                //AET-76
                if (mAlertType == IMAGE_TYPE && mCancelClickListener != null) {
                    mCancelClickListener.onClick(CustomAlertDialog.this);
                }

                String tick = 0 + getContext().getString(R.string.sec);
                mCountView.setText(tick);
                tickTimer.stop();
            }
        });
        tickTimer.start(time);
        Utils.wakeupScreen(time); // AET-97
    }

    /**
     * 设置标题Title
     *
     * @param text the text
     * @return title text
     */
    public CustomAlertDialog setTitleText(String text) {
        mTitleText = text;
        if (mTitleTextView != null && mTitleText != null) {
            showTitleText(true);
            mTitleTextView.setText(mTitleText);
        }
        return this;
    }

    /**
     * Sets image.
     *
     * @param resId the res id
     * @return the image
     */
    public CustomAlertDialog setImage(int resId) {
        if (mImageView != null && resId != 0) {
            showImage(true);
            mImageView.setImageDrawable(FinancialApplication.getApp().getResources().getDrawable(resId));
        }
        return this;
    }

    /**
     * 获取Normal Text 显示状态
     *
     * @return boolean
     */
    public boolean isShowNormalText() {
        return mShowNormal;
    }

    /**
     * Gets normal text.
     *
     * @return the normal text
     */
    public String getNormalText() {
        return mNormalText;
    }

    /**
     * Sets normal text.
     *
     * @param text the text
     * @return the normal text
     */
    public CustomAlertDialog setNormalText(String text) {
        mNormalText = text;
        if (mNormalTextView != null && mNormalText != null) {
            showNormalText(true);
            mNormalTextView.setText(mNormalText);
        }
        return this;
    }

    /**
     * Show normal text custom alert dialog.
     *
     * @param isShow the is show
     * @return the custom alert dialog
     */
    public CustomAlertDialog showNormalText(boolean isShow) {
        mShowNormal = isShow;
        if (mNormalTextView != null) {
            mNormalTextView.setVisibility(mShowNormal ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取内容Content
     *
     * @return content text
     */
    public String getContentText() {
        return mContentText;
    }

    /**
     * 设置内容Content
     *
     * @param text the text
     * @return content text
     */
    public CustomAlertDialog setContentText(String text) {
        mContentText = text;
        if (mContentTextView != null && mContentText != null) {
            showContentText(true);
            mContentTextView.setText(mContentText);
        }
        return this;
    }

    /**
     * 获取输入框EditText中输入的内容
     *
     * @return content edit text
     */
    public String getContentEditText() {
        return mEditText.getText().toString().trim();
    }

    /**
     * Is show title text boolean.
     *
     * @return the boolean
     */
    public boolean isShowTitleText() {
        return mShowTitle;
    }

    /**
     * Show title text custom alert dialog.
     *
     * @param isShow the is show
     * @return the custom alert dialog
     */
    public CustomAlertDialog showTitleText(boolean isShow) {

        mShowTitle = isShow;
        if (mTitleTextView != null) {
            mTitleTextView.setVisibility(mShowTitle ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取当前Content TextView的显示状态
     *
     * @return boolean
     */
    public boolean isShowContentText() {
        return mShowContent;
    }

    /**
     * 设置是否显示内容Content TextView
     *
     * @param isShow true :显示ContentTextView<br>               false :不显示ContentTextView
     * @return custom alert dialog
     */
    public CustomAlertDialog showContentText(boolean isShow) {
        mShowContent = isShow;
        if (mContentTextView != null) {
            mContentTextView.setVisibility(mShowContent ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取Cancel Button 显示状态
     *
     * @return boolean
     */
    public boolean isShowCancelButton() {
        return mShowCancel;
    }

    /**
     * 设置是否显示Cancel Button
     *
     * @param isShow true :显示Cancel Button<br>               false :不显示Cancel Button
     * @return custom alert dialog
     */
    public CustomAlertDialog showCancelButton(boolean isShow) {
        mShowCancel = isShow;
        if (mCancelButton != null) {
            mCancelButton.setVisibility(mShowCancel ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * Show err tips custom alert dialog.
     *
     * @param isShow the is show
     * @return the custom alert dialog
     */
    public CustomAlertDialog showErrTips(boolean isShow) {
        if (mErrTipsView != null) {
            mErrTipsView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
        }
        return this;
    }

    /**
     * Is show image boolean.
     *
     * @return the boolean
     */
    public boolean isShowImage() {
        return mShowImage;
    }

    /**
     * Show image custom alert dialog.
     *
     * @param isShow the is show
     * @return the custom alert dialog
     */
    public CustomAlertDialog showImage(boolean isShow) {
        mShowImage = isShow;
        if (mImageView != null) {
            mImageView.setVisibility(mShowImage ? View.VISIBLE : View.GONE);
        }
        return this;
    }

    /**
     * 获取Confirm Button 显示状态
     *
     * @return boolean
     */
    public boolean isShowConfirmButton() {
        return mShowConfirm;
    }

    /**
     * 设置是否显示Confirm Button
     *
     * @param isShow true :显示Confirm Button<br>               false :不显示Confirm Button
     * @return custom alert dialog
     */
    public CustomAlertDialog showConfirmButton(boolean isShow) {
        mShowConfirm = isShow;
        if (mConfirmButton == null){
            return this;
        }
        if (mShowConfirm){
            mConfirmButton.setVisibility(View.VISIBLE);
            mConfirmButton.requestFocus();
        }else {
            mConfirmButton.setVisibility(View.GONE);
            mConfirmButton.clearFocus();
        }
        return this;
    }

    /**
     * Gets cancel text.
     *
     * @return the cancel text
     */
    public String getCancelText() {
        return mCancelText;
    }

    /**
     * 设置Cancel Button 文本内容
     *
     * @param text the text
     * @return cancel text
     */
    public CustomAlertDialog setCancelText(String text) {
        mCancelText = text;
        if (mCancelButton != null && mCancelText != null) {
            showCancelButton(true);
            mCancelButton.setText(mCancelText);
        }
        return this;
    }

    /**
     * Gets confirm text.
     *
     * @return the confirm text
     */
    public String getConfirmText() {
        return mConfirmText;
    }

    /**
     * 设置Confirm Button 文本内容
     *
     * @param text the text
     * @return confirm text
     */
    public CustomAlertDialog setConfirmText(String text) {
        mConfirmText = text;
        if (mConfirmButton != null && mConfirmText != null) {
            mConfirmButton.setText(mConfirmText);
        }
        return this;
    }

    /**
     * Sets err tips text.
     *
     * @param text the text
     * @return the err tips text
     */
    public CustomAlertDialog setErrTipsText(String text) {
        if (mErrTipsView != null) {
            showErrTips(true);
            mErrTipsView.setText(text);
            ObjectAnimator animator = ObjectAnimator.ofFloat(mErrTipsView, "alpha", 0, 1);
            animator.setDuration(1000);
            animator.setRepeatCount(3);
            animator.setRepeatMode(ObjectAnimator.REVERSE);
            animator.start();
        }
        return this;
    }

    /**
     * Gets progress helper.
     *
     * @return the progress helper
     */
    public ProgressHelper getProgressHelper() {
        return mProgressHelper;
    }

    /**
     * AlerType
     *
     * @return aler type
     */
    public int getAlerType() {
        return mAlertType;
    }

    /**
     * Change alert type.
     *
     * @param alertType the alert type
     */
    public void changeAlertType(int alertType) {
        changeAlertType(alertType, false);
    }

    private void updateTouchListener() {
        if (mTouchToDimiss)
            mDialogView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    CustomAlertDialog.this.dismiss();
                    return true;
                }
            });
        else
            mDialogView.setOnTouchListener(null);
    }

    private void changeAlertView() {
        updateTouchListener();
        mConfirmButton.clearFocus();
        switch (mAlertType) {

            case NORMAL_TYPE:
                if (isShowConfirmButton()){
                    mConfirmButton.setVisibility(View.VISIBLE);
                    mConfirmButton.requestFocus();
                }
                break;
            case ERROR_TYPE:
                mErrorFrame.setVisibility(View.VISIBLE);
                break;
            case SUCCESS_TYPE:
                mSuccessFrame.setVisibility(View.VISIBLE);
                // initial rotate layout of success mask
                mSuccessLeftMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(0));
                mSuccessRightMask.startAnimation(mSuccessLayoutAnimSet.getAnimations().get(1));
                break;
            case CUSTOM_ENTER_TYPE:
                mPwdLayout.setVisibility(View.VISIBLE);
                mCancelButton.setVisibility(View.VISIBLE);
                mConfirmButton.setVisibility(View.VISIBLE);
                mConfirmButton.requestFocus();
                break;
            case PROGRESS_TYPE:
                mProgressLayout.setVisibility(View.VISIBLE);
                mConfirmButton.setVisibility(View.GONE);
                break;
            case IMAGE_TYPE:
                mImageView.setVisibility(View.VISIBLE);
                mTitleTextView.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.VISIBLE);
                mConfirmButton.setVisibility(View.VISIBLE);
                mConfirmButton.requestFocus();
                break;
            case WARN_TYPE:
                mImageView.setVisibility(View.VISIBLE);
                mTitleTextView.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.GONE);
                mConfirmButton.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void changeAlertType(int alertType, boolean fromCreate) {

        mAlertType = alertType;
        if (mDialogView == null) {
            return;
        }

        if (!fromCreate) {
            restore();
            changeAlertView();
            playAnimation();
        } else {
            changeAlertView();
        }

    }

    private void playAnimation() {
        if (mAlertType == ERROR_TYPE) {
            mErrorFrame.startAnimation(mErrorInAnim);
            mErrorX.startAnimation(mErrorXInAnim);
        } else if (mAlertType == SUCCESS_TYPE) {
            mSuccessTick.startTickAnim();
            mSuccessRightMask.startAnimation(mSuccessBowAnim);
        }
    }

    private void restore() {
        mPwdLayout.setVisibility(View.GONE);
        mProgressLayout.setVisibility(View.GONE);

        mErrorFrame.setVisibility(View.GONE);
        mSuccessFrame.setVisibility(View.GONE);
        mButtonLayout.setVisibility(View.GONE);
        mConfirmButton.setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.GONE);
        mNormalTextView.setVisibility(View.GONE);
        mErrTipsView.setVisibility(View.INVISIBLE);

        mErrorFrame.clearAnimation();
        mErrorX.clearAnimation();
        mSuccessTick.clearAnimation();
        mSuccessLeftMask.clearAnimation();
        mSuccessRightMask.clearAnimation();

    }

    @Override
    protected void onStart() {
        playAnimation();
    }

    @Override
    public void cancel() {
        dismiss();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mErrorFrame.clearAnimation();
        mErrorX.clearAnimation();
        mSuccessTick.clearAnimation();
        mSuccessLeftMask.clearAnimation();
        mSuccessRightMask.clearAnimation();
    }

    /**
     * Sets cancel click listener.
     *
     * @param listener the listener
     * @return the cancel click listener
     */
    public CustomAlertDialog setCancelClickListener(OnCustomClickListener listener) {
        mCancelClickListener = listener;
        return this;
    }

    /**
     * Sets confirm click listener.
     *
     * @param listener the listener
     * @return the confirm click listener
     */
    public CustomAlertDialog setConfirmClickListener(OnCustomClickListener listener) {
        mConfirmClickListener = listener;
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_button) {
            if (mCancelClickListener != null) {
                mCancelClickListener.onClick(CustomAlertDialog.this);
            } else {
                dismiss();
            }
        } else if (v.getId() == R.id.confirm_button) {
            if (mConfirmClickListener != null) {
                mConfirmClickListener.onClick(CustomAlertDialog.this);
            } else {
                dismiss();
            }
        }
    }

}
