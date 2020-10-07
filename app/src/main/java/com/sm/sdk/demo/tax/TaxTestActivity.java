package com.sm.sdk.demo.tax;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sunmi.pay.hardware.aidl.AidlErrorCode;
import com.sunmi.pay.hardware.aidlv2.tax.TaxOptV2;

import java.util.Arrays;

import sunmi.sunmiui.utils.LogUtil;

/**
 * This page only used for test, please don't refer
 * any code of this page.
 */
public class TaxTestActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private TaxOptV2 taxOpt = MyApplication.mTaxOptV2;
    private TextView result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tax_layout);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.tax_test_page);
        findViewById(R.id.write_data).setOnClickListener(this);
        findViewById(R.id.read_data).setOnClickListener(this);
        result = findViewById(R.id.tv_data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.write_data:
                handleRWWriteData();
                break;
            case R.id.read_data:
                handleRWReadData();
                break;
        }
    }

    /** Read/Write-Write tax data */
    private void handleRWWriteData() {
        //Write data command：1B 1D 08 00 0B 04 06 00 11 11 03 02 0A 15 39 18
        result.setText(null);
        addTextData("Tax write data:");
        byte[] head = {0x1B, 0x1D};
        byte[] command = {0x08};
        byte[] dataLen = {0x00, 0x0B};
        byte[] data = {0x04, 0x06, 0x00, 0x11, 0x11, 0x03, 0x02, 0x0A, 0x15, 0x39, 0x18};
        byte[] send = ByteUtil.concatByteArrays(head, command, dataLen, data);
        byte[] out = new byte[2048];//Max out buffer length is 1030
        try {
            String msg = "Send >>" + ByteUtil.byte2PrintHex(send, 0, send.length);
            LogUtil.e(TAG, msg);
            addTextData(msg);
            int len = taxOpt.taxDataExchange(send, out);
            if (len < 0) {// Write data error
                msg = "Write data,code:" + len + ",msg:" + AidlErrorCode.valueOf(len).getMsg();
                LogUtil.e(TAG, msg);
                addTextData(msg);
            } else {// Write data success
                byte[] validLen = Arrays.copyOf(out, len);
                msg = "Receive <<" + ByteUtil.byte2PrintHex(validLen, 0, len);
                LogUtil.e(TAG, msg);
                addTextData(msg);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Read/Write-Read tax data */
    private void handleRWReadData() {
        //读数据命令：1B 1D 09 00 06
        result.setText(null);
        addTextData("Tax read data:");
        byte[] head = {0x1B, 0x1D};
        byte[] command = {0x09};
        byte[] dataLen = {0x00, 0x06};
        byte[] send = ByteUtil.concatByteArrays(head, command, dataLen);
        byte[] out = new byte[1030];//Max out buffer length is 1030
        try {
            String msg = "Send >>" + ByteUtil.byte2PrintHex(send, 0, send.length);
            LogUtil.e(TAG, msg);
            addTextData(msg);
            int len = taxOpt.taxDataExchange(send, out);
            if (len < 0) {// Read data error
                msg = "Read data error,code:" + len + ",msg:" + AidlErrorCode.valueOf(len).getMsg();
                LogUtil.e(TAG, msg);
                addTextData(msg);
            } else { // Read data success
                byte[] validLen = Arrays.copyOf(out, len);
                msg = "Receive <<" + ByteUtil.byte2PrintHex(validLen, 0, len);
                LogUtil.e(TAG, msg);
                addTextData(msg);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void addTextData(String data) {
        StringBuilder sb = new StringBuilder(result.getText());
        sb.append(data);
        sb.append("\n");
        result.setText(sb);
    }
}
