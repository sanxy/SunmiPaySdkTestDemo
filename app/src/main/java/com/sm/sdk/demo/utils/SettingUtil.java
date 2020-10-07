package com.sm.sdk.demo.utils;

import android.text.TextUtils;

import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import org.json.JSONObject;

public final class SettingUtil {
    private static final String TAG = Constant.TAG;
    private static final String KEY_BUZZER = "buzzer";
    private static final String KEY_SUPPORT_KEY_PARTITION = "supportKeyPartition";
    private static final String KEY_PSAM_CHANNEL = "psamChannel";
    private static final String KEY_AUTO_RESTORE_NFC = "autoRestoreNfc";
    private static final String KEY_MAX_ONLINE_TIME = "maxOnlineTime";
    private static final int DEFAULT_CHANNEL_COUNT = 2;

    private SettingUtil() {
        throw new AssertionError("Create instance of SettingUtil is prohibited");
    }

    /** 获取是否支持密钥分区 */
    public static boolean getSupportKeyPartition() {
        try {
            String jsonStr = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.RESERVED);
            if (!TextUtils.isEmpty(jsonStr)) {
                JSONObject jobj = new JSONObject(jsonStr);
                if (jobj.has(KEY_SUPPORT_KEY_PARTITION)) {
                    LogUtil.e(TAG, "has keypartition");
                    return jobj.getBoolean(KEY_SUPPORT_KEY_PARTITION);
                }
            }
            if (DeviceUtil.isP1N() || DeviceUtil.isP14G()) {//P1N/P14G默认不支持分区
                return false;
            }
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "SettingUtil getSupportKeyPartition:" + e);
            e.printStackTrace();
        }
        return true;
    }

    /** 设置是否支持密钥分区 */
    public static void setSupportKeyPartition(boolean enable) {
        try {
            JSONObject jobj = new JSONObject();
            String jsonStr = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.RESERVED);
            if (!TextUtils.isEmpty(jsonStr)) {
                jobj = new JSONObject(jsonStr);
            }
            jobj.put(KEY_SUPPORT_KEY_PARTITION, enable);
            MyApplication.mBasicOptV2.setSysParam(AidlConstantsV2.SysParam.RESERVED, jobj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * V1S获取PSAM通道
     * <br/>V1S包含两个PSAM通道：通道1和通道2
     *
     * @return >0-当前PSAM通道，<0-无指定的通道
     */
    public static int getPSAMChannel() {
        int channel = -1;
        try {
            String jsonStr = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.RESERVED);
            if (!TextUtils.isEmpty(jsonStr)) {
                JSONObject jobj = new JSONObject(jsonStr);
                if (jobj.has(KEY_PSAM_CHANNEL)) {
                    channel = jobj.getInt(KEY_PSAM_CHANNEL);
                    LogUtil.e(TAG, "has psam channel:" + channel);
                    return channel;
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "SettingUtil getPSAMChannel:" + e);
            e.printStackTrace();
        }
        return channel;
    }

    /**
     * V1S切换PSAM通道
     * <br/>V1S包含两个PSAM通道：通道1和通道2
     */
    public static void switchPSAMChannel() {
        try {
            int curChannel = -1;
            JSONObject jobj = new JSONObject();
            String jsonStr = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.RESERVED);
            if (!TextUtils.isEmpty(jsonStr)) {
                jobj = new JSONObject(jsonStr);
                if (jobj.has(KEY_PSAM_CHANNEL)) {
                    LogUtil.e(TAG, "has keypartition");
                    curChannel = jobj.getInt(KEY_PSAM_CHANNEL);
                }
            }
            if (curChannel < 0) {
                curChannel = 1;
            } else if (curChannel < DEFAULT_CHANNEL_COUNT) {
                curChannel++;
            } else if (curChannel == DEFAULT_CHANNEL_COUNT) {
                curChannel = -1;
            }
            jobj.put(KEY_PSAM_CHANNEL, curChannel);
            MyApplication.mBasicOptV2.setSysParam(AidlConstantsV2.SysParam.RESERVED, jobj.toString());
            LogUtil.e(TAG, "switch psam channel to " + curChannel + " success");
        } catch (Exception e) {
            LogUtil.e(TAG, "SettingUtil getSupportKeyPartition:" + e);
            e.printStackTrace();
        }
    }

    /** 获取是否自动恢复NFC功能 （当连续读卡NFC功能异常时，是否自动恢复） */
    public static boolean getAutoRestoreNfc() {
        boolean autoRestoreNfc = false;
        try {
            String jsonStr = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.RESERVED);
            if (TextUtils.isEmpty(jsonStr)) {
                return false;
            }
            JSONObject jobj = new JSONObject(jsonStr);
            if (!jobj.has(KEY_AUTO_RESTORE_NFC)) {
                return false;
            }
            autoRestoreNfc = jobj.getBoolean(KEY_AUTO_RESTORE_NFC);
            LogUtil.e(TAG, "autoRestoreNfc:" + autoRestoreNfc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return autoRestoreNfc;
    }

    /**
     * Set whether auto restore the NFC function. Currently,this config is only used on V2Pro.
     * <br/> if set autoRestoreNfc as true, when NFC read card occurred exception,
     * SDK will try to restore NFC function by close NFC and reopen it.
     */
    public static void setAutoRestoreNfc(boolean enable) {
        try {
            JSONObject jobj = new JSONObject();
            String jsonStr = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.RESERVED);
            if (!TextUtils.isEmpty(jsonStr)) {
                jobj = new JSONObject(jsonStr);
            }
            jobj.put(KEY_AUTO_RESTORE_NFC, enable);
            MyApplication.mBasicOptV2.setSysParam(AidlConstantsV2.SysParam.RESERVED, jobj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set SDK built-in PinPad mode
     *
     * @param mode the PinPad mode, refer to {@link AidlConstantsV2.PinPadMode}
     * @return code >=0-success, <0-failed
     */
    public static int setPinPadMode(String mode) {
//        int code = -1;
//        try {
//            code = MyApplication.mBasicOptV2.setSysParam(AidlConstantsV2.SysParam.PINPAD_MODE, mode);
//            if (code < 0) {
//                LogUtil.e(TAG, "setPinPadMode failed,code:" + code);
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        return code;
        return 0;
    }

    /**
     * Set EMV max online tme
     *
     * @param maxOnlineTime max online time, unit: s
     *                      <br/> (1) if set maxOnlineTime<=60s, SDK use default value 60s.
     *                      <br/> (2) if set maxOnlineTime>60, SDK use set value.
     *                      <br/>Note: The set maxOnlineTime should be a int value, not a String.
     * @return code >=0-success, <0-failed
     */
    public static int setEmvMaxOnlineTime(int maxOnlineTime) {
        int code = -1;
        try {
            JSONObject jobj = new JSONObject();
            String jsonStr = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.RESERVED);
            if (!TextUtils.isEmpty(jsonStr)) {
                jobj = new JSONObject(jsonStr);
            }
            jobj.put(KEY_MAX_ONLINE_TIME, maxOnlineTime);
            code = MyApplication.mBasicOptV2.setSysParam(AidlConstantsV2.SysParam.RESERVED, jobj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * Enable/Disable buzzer beep when check card success
     * <br/> SDK default action is beep the buzzer when check card success
     *
     * @param enable true-enable, false-disable
     */
    public static void setBuzzerEnable(boolean enable) {
        try {
            JSONObject jobj = new JSONObject();
            String jsonStr = MyApplication.mBasicOptV2.getSysParam(AidlConstantsV2.SysParam.RESERVED);
            if (!TextUtils.isEmpty(jsonStr)) {
                jobj = new JSONObject(jsonStr);
            }
            jobj.put(KEY_BUZZER, enable ? 1 : 0);
            MyApplication.mBasicOptV2.setSysParam(AidlConstantsV2.SysParam.RESERVED, jobj.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
