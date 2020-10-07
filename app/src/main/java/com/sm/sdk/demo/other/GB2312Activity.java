package com.sm.sdk.demo.other;

import android.os.Bundle;
import android.os.RemoteException;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sunmi.peripheral.printer.SunmiPrinterService;

import java.util.Random;

import sunmi.sunmiui.utils.LogUtil;

public class GB2312Activity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gb2312);
        EditText editText = this.findViewById(R.id.et_gb2312);
        initToolbarBringBack("GB2312");
        new Thread(() -> {
            showLoadingDialog(getString(R.string.handling)+"...");
            String str = getStr();
//            StringBuilder stringBuilder = new StringBuilder();
//            Random random = new Random();
//            for (int i = 0; i < 10; i++) {
//                int i1 = random.nextInt(str.length());
//                stringBuilder.append(str.substring(i1, i1 + 1));
//            }
            runOnUiThread(() -> editText.setText("叁轷垌眢潴物ㄢ苦⊙垛"));
//
//            LogUtil.e("lxy",stringBuilder.toString());
            SunmiPrinterService sunmiPrinterService = MyApplication.sunmiPrinterService;
            try {
                sunmiPrinterService.printTextWithFont("叁轷垌眢潴物ㄢ苦⊙垛","",30,null);
                sunmiPrinterService.lineWrap(6, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            dismissLoadingDialog();
        }).start();
    }


    private String getStr() {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0xA0; i < 0xF7; i++) {
                for (int j = 0xA1; j < 0xFF; j++) {
                    byte[] bytes = new byte[2];
                    bytes[0] = (byte) i;
                    bytes[1] = (byte) j;
                    sb.append(new String(bytes, "gb2312"));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
