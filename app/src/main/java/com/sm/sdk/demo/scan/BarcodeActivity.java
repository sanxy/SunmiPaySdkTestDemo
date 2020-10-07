package com.sm.sdk.demo.scan;


import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;
import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;

public class BarcodeActivity extends BaseAppCompatActivity {

    EditText edInfo;
    MaterialButton scan_barcode_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        initToolbarBringBack(R.string.other_scan_result);
        initView();
    }

    private void initView() {
        edInfo = findViewById(R.id.et_info);
        scan_barcode_start = findViewById(R.id.scan_barcode_start);
        scan_barcode_start.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_barcode_start:

                if (getScannerModel().equals(Constant.SCAN_MODEL_NONE_VALUE)) {
                    showToast(getString(R.string.scan_not_supported));
                    return;
                }

                try {
                    if (MyApplication.scanInterface != null) {
                        MyApplication.scanInterface.scan();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 获取扫码头类型
     *
     * @return
     */
    private String getScannerModel() {
        String scanModel = Constant.SCAN_MODEL_NONE_VALUE;

        if (MyApplication.scanInterface != null) {
            try {
                int scannerModel = MyApplication.scanInterface.getScannerModel();
                switch (scannerModel) {
                    case Constant.SCAN_MODEL_NONE:
                        scanModel = Constant.SCAN_MODEL_NONE_VALUE;
                        break;
                    case Constant.SCAN_MODEL_P2Lite:
                        scanModel = Constant.SCAN_MODEL_P2Lite_VALUE;
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return scanModel;
    }

}
