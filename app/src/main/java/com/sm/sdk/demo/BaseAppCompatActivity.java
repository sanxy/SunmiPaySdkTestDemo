package com.sm.sdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sm.sdk.demo.view.LoadingDialog;
import com.sm.sdk.demo.view.SwingCardHintDialog;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;

public abstract class BaseAppCompatActivity extends AppCompatActivity implements View.OnClickListener {

    private LoadingDialog loadDialog;
    private SwingCardHintDialog swingCardHintDlg;

    private Handler dlgHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setStatusBarColor();
        MyApplication.initLocaleLanguage();
    }

    public void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public void initToolbarBringBack() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Navigation Icon 要设定在 setSupportActionBar 才有作用 否则会出現 back button
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }

    public void initToolbarBringBack(int resId) {
        initToolbarBringBack(getString(resId));
    }

    public void initToolbarBringBack(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                v -> finish()
        );
    }

    public void showToast(int redId) {
        showToastOnUI(getString(redId));
    }


    public void showToast(String msg) {
        showToastOnUI(msg);
    }

    private void showToastOnUI(final String msg) {
        runOnUiThread(
                () -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        );
    }

    public void toastHint(int code) {
        if (code == 0) {
            showToast(R.string.success);
        } else {
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            String error = msg + ":" + code;
            showToast(error);
        }
    }

    protected void showLoadingDialog(int resId) {
        runOnUiThread(() -> _showLoadingDialog(getString(resId)));
    }

    protected void showLoadingDialog(final String msg) {
        runOnUiThread(() -> _showLoadingDialog(msg));
    }

    /** This method should be called in UI thread */
    private void _showLoadingDialog(final String msg) {
        if (loadDialog == null) {
            loadDialog = new LoadingDialog(this, msg);
        } else {
            loadDialog.setMessage(msg);
        }
        if (!loadDialog.isShowing()) {
            loadDialog.show();
        }
    }

    protected void dismissLoadingDialog() {
        runOnUiThread(
                () -> {
                    if (loadDialog != null && loadDialog.isShowing()) {
                        loadDialog.dismiss();
                    }
                    dlgHandler.removeCallbacksAndMessages(null);
                }
        );
    }

    protected void showSwingCardHintDialog() {
        runOnUiThread(
                () -> {
                    if (swingCardHintDlg == null) {
                        swingCardHintDlg = new SwingCardHintDialog(this);
                        swingCardHintDlg.setOwnerActivity(this);
                    }
                    if (swingCardHintDlg.isShowing() || isDestroyed()) {
                        return;
                    }
                    swingCardHintDlg.show();
                }
        );
    }

    protected void dismissSwingCardHintDialog() {
        runOnUiThread(
                () -> {
                    if (swingCardHintDlg != null) {
                        swingCardHintDlg.dismiss();
                    }
                }
        );
    }

    protected void openActivity(Class<? extends Activity> clazz) {
        Intent intent = new Intent(this, clazz);
        openActivity(intent, false);
    }

    protected void openActivity(Class<? extends Activity> clazz, boolean finishSelf) {
        Intent intent = new Intent(this, clazz);
        openActivity(intent, finishSelf);
    }

    protected void openActivity(Intent intent, boolean finishSelf) {
        startActivity(intent);
        if (finishSelf) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {

    }

}
