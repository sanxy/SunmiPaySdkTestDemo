package com.sm.sdk.demo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import com.sm.sdk.demo.MyApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


/**
 * 本地缓存工具类
 */
public final class PreferencesUtil {
    private PreferencesUtil() {
        throw new AssertionError("create instance of PreferencesUtil is prohibited");
    }

    private static final String PREFERENCE_FILE_NAME = "sdkdemo_pref"; // 缓存文件名
    private static final String KEY_PINPAD_MODE = "key_pinpad_mode"; //密码键盘模式

    /** 获取密码键盘模式 */
    public static String getPinPadMode() {
        SharedPreferences pref = MyApplication.context.getSharedPreferences(
                PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        return pref.getString(KEY_PINPAD_MODE, "");
    }

    /** 设置密码键盘模式 */
    public static void setPinPadMode(String mode) {
        SharedPreferences pref = MyApplication.context.getSharedPreferences(
                PREFERENCE_FILE_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(KEY_PINPAD_MODE, mode).apply();
    }


    /** 将可序列化对象转换成Base64字符串 */
    private static String object2String(Serializable obj) {
        // 创建字节输出流
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            // 创建对象输出流,封装字节流
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            // 将对象写入字节流
            oos.writeObject(obj);
            // 将字节流编码成base64的字符串
            return new String(Base64.encode(bos.toByteArray(), 0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(bos);
            IOUtil.close(oos);
        }
        return null;
    }

    /** 将Base64字符串转换成可序列化对象 */
    @SuppressWarnings("unchecked")
    private static <T extends Serializable> T string2Object(String base64Str) {
        if (TextUtils.isEmpty(base64Str)) {
            return null;
        }
        // 读取字节
        byte[] bytes = Base64.decode(base64Str.getBytes(), 0);
        // 封装到字节读取流
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            // 封装到对象读取流
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            // 读取对象
            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtil.close(ois);
            IOUtil.close(bis);
        }
        return null;
    }

}
