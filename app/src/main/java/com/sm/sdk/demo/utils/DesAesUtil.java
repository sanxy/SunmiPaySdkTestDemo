package com.sm.sdk.demo.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class DesAesUtil {
    private DesAesUtil() {
        throw new AssertionError();
    }

    /**
     * AES加密
     *
     * @param key  密钥
     * @param data 待加密的数据
     * @return 加密后的数据
     */
    public static byte[] aseEncrypt(byte[] key, byte[] data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密
     *
     * @param key  密钥
     * @param data 待解密的数据
     * @return 解密后的数据
     */
    public static byte[] aseDecrypt(byte[] key, byte[] data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 3Des加密
     *
     * @param key  密钥
     * @param data 待加密的数据
     * @return 加密后的数据
     */
    public static byte[] desEncrypt(byte[] key, byte[] data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 3Des解密
     *
     * @param key  密钥
     * @param data 待解密的数据
     * @return 解密后的数据
     */
    public static byte[] desDecrypt(byte[] key, byte[] data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "DESede");
            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
