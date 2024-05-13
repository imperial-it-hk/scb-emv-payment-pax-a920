package com.evp.settings;

import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.evp.config.ConfigConst;
import com.evp.config.ConfigUtils;
import com.evp.pay.BaseActivity;
import com.evp.pay.constant.EUIParamKeys;
import com.evp.pay.utils.Utils;
import com.evp.payment.evpscb.R;

import java.util.HashMap;
import java.util.Map;

public class SelectLanguageActivity extends BaseActivity {
    private Map<String, String> languageMap;
    private String navTitle;
    private String currentLanguage;
    private Button selectBtn;
    private String selectedLang;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_select_language;
    }

    @Override
    protected void initViews() {
        LinearLayout linearLayout = findViewById(R.id.root);
        linearLayout.setBackgroundColor(secondaryColor);
        RadioGroup radioGroup = findViewById(R.id.language_gp);
        selectBtn = findViewById(R.id.select_language_btn);
        selectBtn.setBackgroundColor(primaryColor);
        selectBtn.setText(ConfigUtils.getInstance().getString("labelConfirm"));
        selectBtn.setOnClickListener(view -> {
            SysParam.getInstance().setLanguage(selectedLang);
            ConfigUtils.getInstance().reloadLanguage();
            Utils.restart();
        });
        int counter = 0;
        for (final Map.Entry<String, String> language : languageMap.entrySet()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(counter++);
            radioButton.setText(language.getValue());
            radioButton.setOnClickListener(view -> selectedLang = language.getKey());
            radioButton.setChecked(currentLanguage.equals(language.getKey()));
            radioGroup.addView(radioButton);
        }
    }

    protected String getTitleString() {
        return navTitle;
    }

    @Override
    protected void setListeners() {
        enableBackAction(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyBackDown() {
        return super.onKeyBackDown();
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        currentLanguage = SysParam.getInstance().getLanguage();
        String[] languageList = ConfigUtils.getInstance().getDeviceConf(ConfigConst.SUPPORTED_LANGUAGES).split(",");
        languageMap = new HashMap<>();
        for (String language : languageList) {
            languageMap.put(language, ConfigUtils.getInstance().getString(ConfigConst.LABEL_SUPPORTED_LANGUAGES, language));
        }
    }
}
