package com.sm.sdk.demo.other;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.BuildConfig;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.system.BasicOptV2;

import java.lang.reflect.Method;

public class VersionActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_version);
        initToolbarBringBack(R.string.version);
        initView();
    }

    private void initView() {
        TextView tvInfo = findViewById(R.id.tv_info);
        String serviceVersion = "未知";
        try {
            PackageInfo pkgInfo = getPackageManager().getPackageInfo("com.sunmi.pay.hardware_v3", 0);
            serviceVersion = pkgInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            BasicOptV2 basicOptV2 = MyApplication.mBasicOptV2;
            String info = getString(R.string.other_version_device) + basicOptV2.getSysParam(AidlConstantsV2.SysParam.DEVICE_MODEL) + "\n";
            info += getString(R.string.other_version_rom) + getRomVersionName() + "\n";
            info += getString(R.string.other_version_sn) + basicOptV2.getSysParam(AidlConstantsV2.SysParam.SN) + "\n";
            info += getString(R.string.other_version_demo) + BuildConfig.VERSION_NAME + "\n";
            info += getString(R.string.other_version_service) + serviceVersion;
            tvInfo.setText(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("PrivateApi")
    private String getRomVersionName() {
        try {
            String filed = "ro.version.SunMi_VersionName".toLowerCase();
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class);
            return (String) get.invoke(clazz, filed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
