package com.sm.sdk.demo.utils;

import android.os.Build;

public final class DeviceUtil {
    private DeviceUtil() {
        throw new AssertionError("create instance of DeviceUtil is prohibited");
    }

    /** 获取设备型号 */
    public static String getModel() {
        return Build.MODEL;
    }

    /** 是否是P1N */
    public static boolean isP1N() {
        String model = Build.MODEL.toLowerCase();
        return model.matches("p1n(-.+)?");
    }

    /** 是否是P1_4G */
    public static boolean isP14G() {
        String model = Build.MODEL.toLowerCase();
        return model.matches("p1_4g(-.+)?");
    }

    /** 是否是P2lite */
    public static boolean isP2Lite() {
        String model = Build.MODEL.toLowerCase();
        return model.matches("p2lite(-.+)?");
    }

    /** 是否是P2_PRO */
    public static boolean isP2Pro() {
        String model = Build.MODEL.toLowerCase();
        return model.matches("p2_pro(-.+)?");
    }

    /** 是否是P2 */
    public static boolean isP2() {
        String model = Build.MODEL.toLowerCase();
        return model.matches("p2(-.+)?");
    }

    /** 是否是V2_PRO */
    public static boolean isV2Pro() {
        String model = Build.MODEL.toLowerCase();
        return model.matches("v2_pro(-.+)?");
    }

    /** 是否是V1S */
    public static boolean isV1s() {
        String model = Build.MODEL.toLowerCase();
        return model.matches("v1s(-.+)?");
    }
}
