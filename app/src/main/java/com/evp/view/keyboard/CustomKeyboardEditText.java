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
 * 20190108  	         Kim.L                   Create
 * ===========================================================================================
 */
package com.evp.view.keyboard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.text.InputType;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;

import com.evp.commonlib.utils.LogUtils;
import com.evp.pay.app.quickclick.QuickClickProtection;
import com.evp.pay.utils.TickTimer;
import com.evp.pay.utils.ViewUtils;
import com.evp.payment.evpscb.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * The type Custom keyboard edit text.
 */
public class CustomKeyboardEditText extends androidx.appcompat.widget.AppCompatEditText implements View.OnClickListener {

    private Keyboard mKeyboard;
    private CustomKeyboardView mKeyboardView;

    private boolean autoSize = false;
    private boolean keepKeyBoardOn = false;
    private int timeout = -1;
    private TickTimer tickTimer;

    private Window mWindow;
    private View mDecorView;
    private View mContentView;

    private CustomPopupWindow mKeyboardWindow;

    private boolean isNeedCustomKeyboard = true; // 是否启用自定义键盘
    /**
     * adjusted distance
     */
    private int mScrollDistance = 0;

    /**
     * the real height : screen height - guide height - status height
     */
    private int screenContentHeight = -1;

    private int maxFontSize = 0;

    private QuickClickProtection quickClickProtection = QuickClickProtection.getInstance();

    private OnClickListener mOnClickListener;

    /**
     * Instantiates a new Custom keyboard edit text.
     *
     * @param context the context
     */
    public CustomKeyboardEditText(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new Custom keyboard edit text.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public CustomKeyboardEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new Custom keyboard edit text.
     *
     * @param context      the context
     * @param attrs        the attrs
     * @param defStyleAttr the def style attr
     */
    public CustomKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initKeyboard(context, attrs);
        if (isNeedCustomKeyboard) {
            initAttributes(context);
        }
    }

    private void initKeyboard(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Keyboard);
        if (array.hasValue(R.styleable.Keyboard_xml)) {
            isNeedCustomKeyboard = true;
            int xmlId = array.getResourceId(R.styleable.Keyboard_xml, 0);
            mKeyboard = new Keyboard(context, xmlId);
            setAutoSize(array.getBoolean(R.styleable.Keyboard_autoSize, false));
            setKeepKeyBoardOn(array.getBoolean(R.styleable.Keyboard_keepKeyboardOn, false));
            setTimeout(array.getInt(R.styleable.Keyboard_timeout_sec, -1));

            mKeyboardView = (CustomKeyboardView) LayoutInflater.from(context).inflate(R.layout.custom_keyboard_view, null);
            KeyboardUtils.bind(mKeyboardView, new KeyboardUtils(context, mKeyboard, this));
            mKeyboardWindow = new CustomPopupWindow(mKeyboardView,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mKeyboardWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            mKeyboardWindow.setOutsideTouchable(true);
            mKeyboardWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    if (mScrollDistance > 0) {
                        int temp = mScrollDistance;
                        mScrollDistance = 0;
                        if (mContentView != null) {
                            mContentView.scrollBy(0, -temp);
                        }
                    }
                }
            });

            mKeyboardWindow.setOnEnableDismissListener(new CustomPopupWindow.OnEnableDismissListener() {
                @Override
                public boolean onEnableDismiss() {
                    return false;
                }
            });
        } else {
            isNeedCustomKeyboard = false;
        }

        tickTimer = new TickTimer(new TickTimer.OnTickTimerListener() {
            @Override
            public void onTick(long leftTime) {
                //do nothing
            }

            @Override
            public void onFinish() {
                onEditorAction(EditorInfo.IME_ACTION_NONE);
            }
        });


        // AET-65
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setText(getText());
            }
        });

        array.recycle();
    }

    private void initAttributes(Context context) {
        initScreenParams(context);
        setClickable(true);
        setLongClickable(false);
        setCursorVisible(false);
        setFocusable(true);
        setOnClickListener(this);
        setFocusableInTouchMode(true);
        setInputType(InputType.TYPE_NULL);
        setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        removeCopyAbility();

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(false);
                } else {
                    onClick(v);
                }
            }
        });
    }

    @Override
    public final void setOnClickListener(@Nullable OnClickListener l) {
        mOnClickListener = !this.equals(l) ? l : null;
        super.setOnClickListener(this);
    }

    @TargetApi(11)
    private void removeCopyAbility() {
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                //do nothing
            }
        });
    }

    private void initScreenParams(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(metrics);

        screenContentHeight = metrics.heightPixels - getStatusBarHeight(context);
    }

    /**
     *
     */
    private int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            Field field = clazz.getField("status_bar_height");
            int x = Integer.parseInt(field.get(object).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            LogUtils.w("CustomKeyboardEditText", "", e);
        }
        return statusBarHeight;
    }

    @Override
    public void onEditorAction(int actionCode) {
        if (timeout > 0)
            tickTimer.start(timeout); // AET-85
        if (quickClickProtection.isStarted()) {
            return;
        }
        quickClickProtection.start();
        if (actionCode == EditorInfo.IME_ACTION_NONE || actionCode == EditorInfo.IME_ACTION_DONE) {
            hideKeyboard(false);
        }
        super.onEditorAction(actionCode);
    }


    @Override
    public void onClick(View v) {
        if (quickClickProtection.isStarted()) {
            return;
        }
        quickClickProtection.start();

        if (isNeedCustomKeyboard) {
            hideSysInput();
            showKeyboard();
        }

        if (mOnClickListener != null) {
            mOnClickListener.onClick(v);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d("silly muhua", "in CustomKeyboardEditText, keycode=" + keyCode);
        if (!hasFocus()) {
            return super.onKeyDown(keyCode, event);
        }
        if (quickClickProtection.isStarted() &&
                (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ENTER)) {
            return true;
        }
        quickClickProtection.start();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            hideKeyboard(false);
            quickClickProtection.stop(); // need to stop the protection cuz it will stop the finish call from the activity
            onEditorAction(EditorInfo.IME_ACTION_NONE);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isInEditMode()) {
            return;
        }
        Activity activity = ViewUtils.getActivityFromView(this);
        if(activity == null){
            return;
        }
        mWindow = activity.getWindow();
        mDecorView = mWindow.getDecorView();
        mContentView = mWindow.findViewById(Window.ID_ANDROID_CONTENT);
        mContentView.setFocusableInTouchMode(true);

        if (isFocused()) {
            hideSysInput();
            showKeyboard();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        hideSysInput();
        hideKeyboard(true);

        mKeyboardWindow = null;
        mKeyboardView = null;
        mKeyboard = null;

        mDecorView = null;
        mContentView = null;
        mWindow = null;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (isAutoSize()) {
            TextPaint textPaint = getPaint();
            int width = getMeasuredWidth() - getPaddingEnd() - getPaddingStart(); //AET-20
            if (width > 0) {
                int size = updateTextSize(text, textPaint, width);
                setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
            }
        }
    }

    private int updateTextSize(CharSequence text, TextPaint textPaint, int width) {
        int size = maxFontSize > 0 ? maxFontSize : (int) (textPaint.getTextSize());
        if (size > maxFontSize)
            maxFontSize = size;

        while (true) {
            textPaint.setTextSize(size);
            float w = textPaint.measureText(text.toString());
            if (w > width) {
                size -= 2;
            } else {
                break;
            }
        }
        return size;
    }

    private static class SetSelectionRunnable implements Runnable {

        private WeakReference<CustomKeyboardEditText> weakRefEditText;

        /**
         * Instantiates a new Set selection runnable.
         *
         * @param customKeyboardEditText the custom keyboard edit text
         */
        public SetSelectionRunnable(CustomKeyboardEditText customKeyboardEditText) {
            this.weakRefEditText = new WeakReference<>(customKeyboardEditText);
        }

        @Override
        public void run() {
            CustomKeyboardEditText editText = weakRefEditText.get();
            if (null == editText) {
                return;
            }
            editText.setSelection(editText.getText().length());
        }
    }

    private void showKeyboard() {
        if (mKeyboardWindow != null && !mKeyboardWindow.isShowing() && mDecorView != null) {
            mKeyboardView.setKeyboard(mKeyboard);
            mKeyboardWindow.forceDismiss();
            mKeyboardWindow.showAtLocation(mDecorView, Gravity.BOTTOM, 0, 0);
            //mKeyboardWindow.update(); //bug on Android 7.0, it hardcode Gravity!!!!!
            if (timeout > 0)
                tickTimer.start(timeout);

            post(new SetSelectionRunnable(this));

            if (mContentView != null) {
                int[] pos = new int[2];
                getLocationOnScreen(pos);
                float height = ViewUtils.dp2px(240);

                Rect outRect = new Rect();
                mDecorView.getWindowVisibleDisplayFrame(outRect);

                int screen = screenContentHeight;
                mScrollDistance = (int) ((pos[1] + getMeasuredHeight() - outRect.top) - (screen - height));

                if (mScrollDistance > 0) {
                    mContentView.scrollBy(0, mScrollDistance);
                }
            }
        }
    }

    private void hideKeyboard(boolean force) {
        if (mKeyboardWindow != null && mKeyboardWindow.isShowing()) {
            if (force || !isKeepKeyBoardOn())
                mKeyboardWindow.forceDismiss();
            this.clearFocus();
            tickTimer.stop();
        }
    }

    private void hideSysInput() {
        if (getWindowToken() != null) {
            KeyboardUtils.hideSystemKeyboard(CustomKeyboardEditText.this);
        }
    }

    /**
     * Is auto size boolean.
     *
     * @return the boolean
     */
    public boolean isAutoSize() {
        return autoSize;
    }

    /**
     * Sets auto size.
     *
     * @param autoSize the auto size
     */
    public void setAutoSize(boolean autoSize) {
        this.autoSize = autoSize;
    }

    /**
     * Is keep key board on boolean.
     *
     * @return the boolean
     */
    public boolean isKeepKeyBoardOn() {
        return keepKeyBoardOn;
    }

    /**
     * Sets keep key board on.
     *
     * @param keepKeyBoardOn the keep key board on
     */
    public void setKeepKeyBoardOn(boolean keepKeyBoardOn) {
        this.keepKeyBoardOn = keepKeyBoardOn;
    }

    /**
     * Gets timeout.
     *
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets timeout.
     *
     * @param timeout the timeout
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
