package com.sm.sdk.demo.card;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

final class UltralightEV1Helper {

    private static final String TAG = "UltralightEV1Helper";

    private static final byte ULC_Read = 0x30;
    private static final byte ULC_Write = (byte) 0xa2;
    private static final byte ULC_ComWrite = (byte) 0xa0;
    private static final byte ULC_Auth = 0x1a;
    private static final byte ULC_GetVersion = 0x60;
    private static final byte ULC_FastRead = 0x3a;
    private static final byte ULC_FastCnt = 0x39;
    private static final byte ULC_IncrCnt = (byte) 0xa5;
    private static final byte ULC_PwdAuth = 0x1b;
    private static final byte ULC_ReadSig = 0x3c;
    private static final byte ULC_CheakTear = 0x3e;
    private static final byte ULC_VCSL = 0x4b;

    private static final byte[] DivKey = {0x3a, 0x3c, (byte) 0xBf, 0x15, 0x7c, 0x66,
            (byte) 0xA2, 0x75, (byte) 0x92, (byte) 0xA0, 0x23, 0x6A, (byte) 0xFD, 0x61,
            (byte) 0xA0, 0x55, 0x29, 0x46, 0x03, 0x3C, 0x37, 0x75, (byte) 0xCC,
            0x54, 0x67, 0x18, 0x46, 0x64, (byte) 0xD2, (byte) 0x98, 0x55, (byte) 0xB8};

    private static final byte[] DivIV = {0x2F, 0x68, (byte) 0xEB, 0x4B, 0x1E, (byte) 0xCC,
            (byte) 0x84, 0x2F, 0x39, 0x34, (byte) 0x62, (byte) 0xFD, 0x11, (byte) 0xE3, 0x26, 0x21};

    private static final byte[] DivData = {0x34, 0x51, 0x28, 0x69, 0x59, 0x76, 0x14, (byte) 0x93};


    private BaseAppCompatActivity activity;

    UltralightEV1Helper(BaseAppCompatActivity activity) {
        this.activity = activity;
    }

    /**
     * 获取认证数据
     */
    byte[] getAuthData(byte[] uuid) {
        try {
            byte[] send = {ULC_GetVersion};
            byte[] out = new byte[260];
            int len = MyApplication.mReadCardOptV2.transmitApdu(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                LogUtil.e(TAG, "connect to card failed,errCode:" + len);
                activity.showToast(AidlErrorCodeV2.valueOf(len).getMsg());
                return null;
            }

            byte[] validData = Arrays.copyOf(out, 8);
            if (len != 8 || out[2] != 0x03 || out[4] != 1) {
                LogUtil.e(TAG, "MF0ULX1 get version failed,len:" + len + " outData:" + ByteUtil.bytes2HexStr(validData));
                activity.showToast("get version failed.");
                return null;
            }
            SecretKeySpec keySpec = new SecretKeySpec(DivKey, "AES_256");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(DivIV));
            byte[] divData = new byte[16];
            divData[0] = 7;
            System.arraycopy(uuid, 0, divData, 1, 7);
            System.arraycopy(DivData, 0, divData, 8, 8);
            return cipher.doFinal(divData);
        } catch (Exception e) {
            LogUtil.e(TAG, "MF0ULX1 get auth data failed:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * MifareUtralight ev1 认证
     */
    boolean authenticate(byte[] authData) {
        try {
            byte[] send = new byte[5];
            send[0] = ULC_PwdAuth;
            System.arraycopy(authData, 4, send, 1, 4);
            byte[] out = new byte[260];
            int len = MyApplication.mReadCardOptV2.transmitApdu(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                LogUtil.e(TAG, "authenticate failed,errCode:" + len);
                activity.showToast(AidlErrorCodeV2.valueOf(len).getMsg());
                return false;
            }
            if (len != 2 || out[0] != authData[8] || out[1] != authData[9]) {
                LogUtil.e(TAG, "authenticate failed,len:" + len + " outData:" + ByteUtil.bytes2HexStr(Arrays.copyOf(out, len)));
                return false;
            }
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "MF0ULX1 authenticate failed:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读数据
     */
    int[] readData() {
        try {
            byte[] send = {ULC_FastRead, 4, 15};
            byte[] out = new byte[260];
            int len = MyApplication.mReadCardOptV2.transmitApdu(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                LogUtil.e(TAG, "read data failed,errCode:" + len);
                activity.showToast(AidlErrorCodeV2.valueOf(len).getMsg());
                return new int[0];
            }
            if (len != 48) {
                LogUtil.e(TAG, "read data failed,len:" + len);
                return new int[0];
            }
            byte[] valid = Arrays.copyOf(out, len);
            LogUtil.e(TAG, "readData outData:" + ByteUtil.bytes2HexStr(valid));
            ByteBuffer buf = ByteBuffer.wrap(valid);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            int[] result = new int[buf.remaining() / 4];
            for (int i = 0; i < result.length; i++) {
                result[i] = buf.getInt();
            }
            return result;
        } catch (Exception e) {
            LogUtil.e(TAG, "MF0ULX1 read data failed:" + e.getMessage());
            e.printStackTrace();
        }
        return new int[0];
    }


    /**
     * 写一页数据
     */
    boolean writePage(int page, int value) {
        try {
            if (page < 0 || page > 19) {//页码[0,19]
                return false;
            }
            byte[] cmd = {ULC_Write};
            byte[] pageT = {(byte) page};
            byte[] valueT = ByteUtil.int2BytesLE(value);
            byte[] send = ByteUtil.concatByteArrays(cmd, pageT, valueT);
            byte[] out = new byte[260];
            int len = MyApplication.mReadCardOptV2.transmitApdu(CardType.MIFARE.getValue(), send, out);
            if (len < 0) {
                LogUtil.e(TAG, "write data failed,errCode:" + len);
                activity.showToast(AidlErrorCodeV2.valueOf(len).getMsg());
                return false;
            }
            if (len != 1 || out[0] != 0xa) {
                LogUtil.e(TAG, "write data failed,len:" + len + " outData:" + ByteUtil.bytes2HexStr(out));
                return false;
            }
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "write data failed:" + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    boolean write(int index, int value) {
        if (index < 0 || index >= 12) {
            return false;
        }
        return writePage(index + 4, value);
    }

    boolean writePW(int index, int value) {
        if (index < 16 || index >= 20) {
            return false;
        }
        return writePage(index, value);
    }

    /**
     * 初始化数据
     */
    boolean initialize(byte[] authData) {
        for (int count = 0; count < 12; count++) {
            if (!writePage(count + 4, 0)) {
                return false;
            }
        }
        int AuthPass = ByteUtil.unsignedInt2IntLE(authData, 4);
        int AuthResp = ByteUtil.unsignedShort2IntLE(authData, 8);
        if (!writePage(18, AuthPass)) {
            return false;
        }
        if (!writePage(19, AuthResp)) {
            return false;
        }
        if (!writePage(17, 0x0580)) {
            return false;
        }
        if (!writePage(16, 0)) {
            return false;
        }
        return true;
    }

}
