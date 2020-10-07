package com.sm.sdk.demo.other;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.widget.Toolbar;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.CacheHelper;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MainActivity;
import com.sm.sdk.demo.R;

public class LanguageActivity extends BaseAppCompatActivity {

    private int mCurrentLanguage;

    private RadioButton mRbAuto;
    private RadioButton mRbZH_CN;
    private RadioButton mRbEN_US;
    private RadioButton mRbJA_JP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_language);
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.other_language);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                v -> onBackPressed()
        );

        mRbAuto = findViewById(R.id.rb_auto);
        mRbZH_CN = findViewById(R.id.rb_zh_cn);
        mRbEN_US = findViewById(R.id.rb_en_us);
        mRbJA_JP = findViewById(R.id.rb_ja_jp);

        findViewById(R.id.item_auto).setOnClickListener(this);
        findViewById(R.id.item_zh_cn).setOnClickListener(this);
        findViewById(R.id.item_en_us).setOnClickListener(this);
        findViewById(R.id.item_ja_jp).setOnClickListener(this);

        reset();
        mCurrentLanguage = CacheHelper.getCurrentLanguage();
        switch (mCurrentLanguage) {
            case Constant.LANGUAGE_ZH_CN:
                mRbZH_CN.setChecked(true);
                break;
            case Constant.LANGUAGE_EN_US:
                mRbEN_US.setChecked(true);
                break;
            case Constant.LANGUAGE_JA_JP:
                mRbJA_JP.setChecked(true);
                break;
            default:
                mRbAuto.setChecked(true);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (CacheHelper.getCurrentLanguage() != mCurrentLanguage) {
            MainActivity.reStart(this);
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.item_auto:
                reset();
                mRbAuto.setChecked(true);
                CacheHelper.saveCurrentLanguage(Constant.LANGUAGE_AUTO);
                break;
            case R.id.item_zh_cn:
                reset();
                mRbZH_CN.setChecked(true);
                CacheHelper.saveCurrentLanguage(Constant.LANGUAGE_ZH_CN);
                break;
            case R.id.item_en_us:
                reset();
                mRbEN_US.setChecked(true);
                CacheHelper.saveCurrentLanguage(Constant.LANGUAGE_EN_US);
                break;
            case R.id.item_ja_jp:
                reset();
                mRbJA_JP.setChecked(true);
                CacheHelper.saveCurrentLanguage(Constant.LANGUAGE_JA_JP);
                break;
        }
    }

    private void reset() {
        mRbAuto.setChecked(false);
        mRbZH_CN.setChecked(false);
        mRbEN_US.setChecked(false);
        mRbJA_JP.setChecked(false);
    }


}
