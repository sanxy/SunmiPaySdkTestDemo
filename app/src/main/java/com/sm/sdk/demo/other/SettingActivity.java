package com.sm.sdk.demo.other;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.PreferencesUtil;
import com.sm.sdk.demo.utils.SettingUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import java.util.ArrayList;
import java.util.List;

public class SettingActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
    }

    @SuppressLint("CutPasteId")
    private void initView() {
        initToolbarBringBack(R.string.setting);
        // key partition
        View view = findViewById(R.id.key_partition);
        TextView txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.setting_key_partition);
        final TextView txtValueKeyPart = view.findViewById(R.id.value);
        txtValueKeyPart.setText(getKeyPartitionValue());
        Button btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setTag(SettingUtil.getSupportKeyPartition());
        btnSwitch.setOnClickListener(v -> {
            switchKeyPartition(v);
            txtValueKeyPart.setText(getKeyPartitionValue());
        });

        //psam channel
        view = findViewById(R.id.psam_channel);
        txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.setting_psam_channel);
        final TextView txtValuePasm = view.findViewById(R.id.value);
        txtValuePasm.setText(getPSAMChannel());
        btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setOnClickListener(v -> {
            switchPSAMChannel();
            txtValuePasm.setText(getPSAMChannel());
        });

        //Auto restore NFC
        view = findViewById(R.id.auto_restore_nfc);
        txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.setting_auth_restore_nfc);
        final TextView txtValueNfc = view.findViewById(R.id.value);
        txtValueNfc.setText(getAutoRestoreNfc());
        btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setTag(SettingUtil.getAutoRestoreNfc());
        btnSwitch.setOnClickListener(v -> {
            switchAutoRestoreNfc(v);
            txtValueNfc.setText(getAutoRestoreNfc());
        });

        //PinPad Mode
        view = findViewById(R.id.pinpad_mode);
        txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.setting_pinpad_mode);
        final TextView txtValuePinpadMode = view.findViewById(R.id.value);
        txtValuePinpadMode.setText(getPinPadMode());
        btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setOnClickListener(v -> {
            switchPinPadMode();
            txtValuePinpadMode.setText(getPinPadMode());
        });

        //PCD Param
        view = findViewById(R.id.pcd_param);
        txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.setting_pcd_param);
        TextView txtValue = view.findViewById(R.id.value);
        txtValue.setVisibility(View.INVISIBLE);
        btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setText(R.string.setting);
        btnSwitch.setOnClickListener(v -> {
            openActivity(PCDParamActivity.class);
        });

        //device shutdown
        view = findViewById(R.id.device_shutdown);
        txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.device_shutdown);
        view.findViewById(R.id.value).setVisibility(View.INVISIBLE);
        btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setText(R.string.ok);
        btnSwitch.setOnClickListener(v -> {
            deviceShutdown();
        });

        //device reboot
        view = findViewById(R.id.device_reboot);
        txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.device_reboot);
        view.findViewById(R.id.value).setVisibility(View.INVISIBLE);
        btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setText(R.string.ok);
        btnSwitch.setOnClickListener(v -> {
            deviceReboot();
        });

        //clear Aid
        view = findViewById(R.id.clear_aid);
        txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.setting_clear_aid);
        TextView txtAidCount = view.findViewById(R.id.value);
        txtAidCount.setText(getAidOrCapkCount(0));
        btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setText(R.string.ok);
        btnSwitch.setOnClickListener(v -> {
            clearAid();
            txtAidCount.setText(getAidOrCapkCount(0));
        });

        //clear Aid
        view = findViewById(R.id.clear_capk);
        txtName = view.findViewById(R.id.name);
        txtName.setText(R.string.setting_clear_capk);
        TextView txtCapkCount = view.findViewById(R.id.value);
        txtCapkCount.setText(getAidOrCapkCount(1));
        btnSwitch = view.findViewById(R.id.mb_switch);
        btnSwitch.setText(R.string.ok);
        btnSwitch.setOnClickListener(v -> {
            clearCapk();
            txtCapkCount.setText(getAidOrCapkCount(1));
        });
    }

    /** 获取密钥分区值 */
    private String getKeyPartitionValue() {
        boolean keyPartStatus = SettingUtil.getSupportKeyPartition();
        LogUtil.e(TAG, "get keyPartitionStatus:" + keyPartStatus);
        return getString(R.string.setting_value) + keyPartStatus;
    }

    /** 切换密钥分区配置 */
    private void switchKeyPartition(View view) {
        boolean keyPartStatus = !(Boolean) view.getTag();
        SettingUtil.setSupportKeyPartition(keyPartStatus);
        LogUtil.e(TAG, "supportKeyPartition:" + keyPartStatus);
        view.setTag(keyPartStatus);
    }

    /** 获取当前PSAM通道 */
    private String getPSAMChannel() {
        int channel = SettingUtil.getPSAMChannel();
        LogUtil.e(TAG, "current psam channel:" + channel);
        return getString(R.string.setting_value) + (channel < 0 ? "--" : String.valueOf(channel));
    }

    /** 切换PSAM通道 */
    private void switchPSAMChannel() {
        SettingUtil.switchPSAMChannel();
    }

    /** 获取是否自动恢复NFC */
    private String getAutoRestoreNfc() {
        boolean value = SettingUtil.getAutoRestoreNfc();
        LogUtil.e(TAG, "getAutoRestoreNfc:" + value);
        return getString(R.string.setting_value) + value;
    }

    /** 设置是否自动恢复NFC */
    private void switchAutoRestoreNfc(View view) {
        boolean value = !(Boolean) view.getTag();
        SettingUtil.setAutoRestoreNfc(value);
        LogUtil.e(TAG, "switchAutoRestoreNfc:" + value);
        view.setTag(value);
    }

    /** 设置是否自动恢复NFC */
    private String getPinPadMode() {
        String mode = PreferencesUtil.getPinPadMode();
        return TextUtils.isEmpty(mode) ? "--" : mode;
    }

    /** 切换PinPad模式 */
    private void switchPinPadMode() {
        String mode = PreferencesUtil.getPinPadMode();
        if (TextUtils.isEmpty(mode) || AidlConstantsV2.PinPadMode.MODE_NORMAL.equals(mode)) {//设置为美团
            mode = AidlConstantsV2.PinPadMode.MODE_MEITUAN;
        } else if (AidlConstantsV2.PinPadMode.MODE_MEITUAN.equals(mode)) {
            mode = AidlConstantsV2.PinPadMode.MODE_SILENT;
        } else if (AidlConstantsV2.PinPadMode.MODE_SILENT.equals(mode)) {
            mode = AidlConstantsV2.PinPadMode.MODE_NORMAL;
        }
        int code = SettingUtil.setPinPadMode(mode);
        LogUtil.e(TAG, "switchPinPadMode code:" + code);
        if (code >= 0) {
            PreferencesUtil.setPinPadMode(mode);
        }
    }

    /** 设备关机 */
    private void deviceShutdown() {
        try {
            MyApplication.mBasicOptV2.sysPowerManage(AidlConstantsV2.PowerManage.SYS_SHUTDOWN);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 设备重启 */
    private void deviceReboot() {
        try {
            MyApplication.mBasicOptV2.sysPowerManage(AidlConstantsV2.PowerManage.SYS_REBOOT);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询Aid或Capk个数
     *
     * @param type 0-查询Aid，1-查询Capk
     * @return Aid或Capk个数字符串
     */
    private String getAidOrCapkCount(int type) {
        int count = -1;
        try {
            List<String> list = new ArrayList<>();
            int code = MyApplication.mEMVOptV2.queryAidCapkList(type, list);
            if (code < 0) {
                LogUtil.e(TAG, "clear Aid failed,code:" + code);
            } else {
                count = list.size();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String typeStr = (type == 0 ? "Aid" : "Capk");
        return Utility.formatStr("%s count: %d", typeStr, count);
    }

    private void clearAid() {
        try {
            MyApplication.mEMVOptV2.deleteAid(null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void clearCapk() {
        try {
            MyApplication.mEMVOptV2.deleteCapk(null, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
