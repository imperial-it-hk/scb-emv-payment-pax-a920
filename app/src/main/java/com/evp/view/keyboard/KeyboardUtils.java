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

import static android.content.Context.AUDIO_SERVICE;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.text.Editable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.evp.pay.app.FinancialApplication;

import java.util.List;

/**
 * The type Keyboard utils.
 */
public class KeyboardUtils implements KeyboardView.OnKeyboardActionListener {

    private final Keyboard mKeyboard;
    private Context mContext;
    private final EditText mEditText;

    /**
     * Instantiates a new Keyboard utils.
     *
     * @param context  the context
     * @param keyboard the keyboard
     * @param editText the edit text
     */
    public KeyboardUtils(Context context, Keyboard keyboard, EditText editText) {
        mContext = context;
        mKeyboard = keyboard;
        mEditText = editText;
    }

    /**
     * Bind.
     *
     * @param keyboardView  the keyboard view
     * @param keyboardUtils the keyboard utils
     */
    public static void bind(KeyboardView keyboardView, KeyboardUtils keyboardUtils) {
        keyboardView.setKeyboard(keyboardUtils.mKeyboard);
        keyboardView.setEnabled(true);
        keyboardView.setLongClickable(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(keyboardUtils);
    }

    @Override
    public void onPress(int primaryCode) {
        // do nothing
    }

    @Override
    public void onRelease(int primaryCode) {
        //do nothing
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Editable editable = mEditText.getText();
        int start = mEditText.getText().length();
        playClick(primaryCode);

        if (primaryCode <= -1000) {     // Sale QR, Scan, Card
            mEditText.onEditorAction(primaryCode);
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL || primaryCode == -90) {// cancel
            mEditText.onEditorAction(EditorInfo.IME_ACTION_NONE);
        } else if (primaryCode == Keyboard.KEYCODE_DONE || primaryCode == -92 || primaryCode == -93) {// done
            mEditText.onEditorAction(EditorInfo.IME_ACTION_DONE);
        } else if (primaryCode == Keyboard.KEYCODE_DELETE || primaryCode == -91) {// delete
            if (editable != null && editable.length() > 0 && start > 0) {
                editable.delete(start - 1, start);
            }
        } else if (0x0 <= primaryCode && primaryCode <= 0x7f) {
            editable.insert(start, Character.toString((char) primaryCode));
        } else if (primaryCode > 0x7f) {
            Keyboard.Key key = getKeyByKeyCode(primaryCode);
            if (key != null)
                editable.insert(start, key.label);
        }
    }

    private static void playClick(int keyCode) {
        AudioManager am = (AudioManager) FinancialApplication.getApp().getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 0:
                break;
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
                break;
        }
    }

    /**
     * Hide system keyboard.
     *
     * @param view    the view
     */
    public static void hideSystemKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) FinancialApplication.getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Show system keyboard.
     *
     * @param view    the view
     */
    public static void showSystemKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) FinancialApplication.getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    private Keyboard.Key getKeyByKeyCode(int primaryCode) {
        if (mKeyboard != null) {
            List<Keyboard.Key> keyList = mKeyboard.getKeys();
            for (Keyboard.Key key : keyList) {
                if (key.codes[0] == primaryCode) {
                    return key;
                }
            }
        }

        return null;
    }

    @Override
    public void onText(CharSequence text) {
        // do nothing
    }

    @Override
    public void swipeLeft() {
        // do nothing
    }

    @Override
    public void swipeRight() {
        // do nothing
    }

    @Override
    public void swipeDown() {
        // do nothing
    }

    @Override
    public void swipeUp() {
        // do nothing
    }
}
