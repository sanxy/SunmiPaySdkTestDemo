package com.sm.sdk.demo.security;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.Security;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.Arrays;

/**
 * This page show how to use the RSA related interfaces.
 */
public class RSATestActivity extends BaseAppCompatActivity {
    private static final String TAG = "RSATestActivity";

    private TextView result;
    private String signature;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsa_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.security_rsa_test);
        findViewById(R.id.gen_key).setOnClickListener(this);
        findViewById(R.id.get_rsa_key).setOnClickListener(this);
        findViewById(R.id.rsa_pub_enc_pvt_dec).setOnClickListener(this);
        findViewById(R.id.rsa_pvt_enc_pub_dec).setOnClickListener(this);
        findViewById(R.id.rsa_signing).setOnClickListener(this);
        findViewById(R.id.rsa_verify_signature).setOnClickListener(this);
        findViewById(R.id.save_cipher_key_with_rsa).setOnClickListener(this);
        findViewById(R.id.save_rsa_key).setOnClickListener(this);
        result = findViewById(R.id.result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gen_key:
                generateRSAKey();
                break;
            case R.id.get_rsa_key:
                getRSAKeys();
                break;
            case R.id.rsa_pub_enc_pvt_dec:
                rsaPublicKeyEncryptPrivateKeyDecrypt();
                break;
            case R.id.rsa_pvt_enc_pub_dec:
                rsaPrivateKeyEncryptPublicKeyDecrypt();
                break;
            case R.id.rsa_signing:
                rsaSigning();
                break;
            case R.id.rsa_verify_signature:
                rsaVerifySignature();
                break;
            case R.id.save_cipher_key_with_rsa:
//                saveCipherTextKeyRSA();
                break;
            case R.id.save_rsa_key:
//                saveRSAKey();
                break;
        }
    }

    /** 生成公私玥对 */
    private void generateRSAKey() {
        try {
            result.setText(null);
            int code = MyApplication.mSecurityOptV2.generateRSAKeys(0, 1, 2048, "00000003");
            if (code == 0) {
                showToast("Generate RSA keys success");
            } else {
                showToast("Generate RSA keys failed");
                LogUtil.e(TAG, "Generate RSA keys failed, code:" + code);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 获取公私玥 */
    @SuppressLint("SetTextI18n")
    private void getRSAKeys() {
        try {
            result.setText(null);
            byte[] dataOut = new byte[1024];
            int len = MyApplication.mSecurityOptV2.getRSAPublicKey(0, dataOut);
            if (len < 0) {
                LogUtil.e(TAG, "Get RSA public key failed:" + len);
                return;
            }
            String pubKey = ByteUtil.bytes2HexStr(Arrays.copyOf(dataOut, len));
            result.setText("Public key:\n" + pubKey);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 公钥加密私钥解密 */
    @SuppressLint("SetTextI18n")
    private void rsaPublicKeyEncryptPrivateKeyDecrypt() {
        try {
            result.setText(null);
            String data = "\"I have something to tell you, white mouse,\" she said. \"Mr. Behrman died of pneumonia to-day in the hospital."
                    + " He was ill only two days. The janitor found him the morning of the first day in his room downstairs helpless with pain."
                    + " His shoes and clothing were wet through and icy cold. They couldn't imagine where he had been on such a dreadful night."
                    + " And then they found a lantern, still lighted, and a ladder that had been dragged from its place, and some scattered brushes,"
                    + " and a palette with green and yellow colours mixed on it, and - look out the window, dear, at the last ivy leaf on the wall."
                    + " Didn't you wonder why it never fluttered or moved when the wind blew? Ah, darling, it's Behrman's masterpiece - he painted it"
                    + " there the night that the last leaf fell.";
            byte[] dataOut = new byte[1024];
            int len = MyApplication.mSecurityOptV2.dataEncryptRSA(AidlConstantsV2.Security.RSA_TRANSFORMATION_4, 0, data.getBytes(), dataOut);
            if (len < 0) {
                LogUtil.e(TAG, "RSA public key encrypt data failed:" + len);
                return;
            }
            byte[] validOut = Arrays.copyOf(dataOut, len);
            String encOut = ByteUtil.bytes2HexStr(validOut);
            len = MyApplication.mSecurityOptV2.dataDecryptRSA(AidlConstantsV2.Security.RSA_TRANSFORMATION_4, 1, validOut, dataOut);
            if (len < 0) {
                LogUtil.e(TAG, "RSA private key decrypt data failed:" + len);
                return;
            }
            validOut = Arrays.copyOf(dataOut, len);
            String decOut = new String(validOut);
            result.setText("RSA public key encrypt/private key decrypt:"
                    + "\nBefore Encrypt:\n" + data
                    + "\nAfter encrypt:\n" + encOut
                    + "\nAfter decrypt:\n" + decOut);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 私钥加密公钥解密 */
    @SuppressLint("SetTextI18n")
    private void rsaPrivateKeyEncryptPublicKeyDecrypt() {
        try {
            result.setText(null);
            String data = "\"I have something to tell you, white mouse,\" she said. \"Mr. Behrman died of pneumonia to-day in the hospital."
                    + " He was ill only two days. The janitor found him the morning of the first day in his room downstairs helpless with pain."
                    + " His shoes and clothing were wet through and icy cold. They couldn't imagine where he had been on such a dreadful night."
                    + " And then they found a lantern, still lighted, and a ladder that had been dragged from its place, and some scattered brushes,"
                    + " and a palette with green and yellow colours mixed on it, and - look out the window, dear, at the last ivy leaf on the wall."
                    + " Didn't you wonder why it never fluttered or moved when the wind blew? Ah, darling, it's Behrman's masterpiece - he painted it"
                    + " there the night that the last leaf fell.";
            byte[] dataOut = new byte[1024];
            int len = MyApplication.mSecurityOptV2.dataEncryptRSA(AidlConstantsV2.Security.RSA_TRANSFORMATION_4, 1, data.getBytes(), dataOut);
            if (len < 0) {
                LogUtil.e(TAG, "RSA private key encrypt data failed:" + len);
                return;
            }
            byte[] validOut = Arrays.copyOf(dataOut, len);
            String encOut = ByteUtil.bytes2HexStr(validOut);
            len = MyApplication.mSecurityOptV2.dataDecryptRSA(AidlConstantsV2.Security.RSA_TRANSFORMATION_4, 0, validOut, dataOut);
            if (len < 0) {
                LogUtil.e(TAG, "RSA public key decrypt data failed:" + len);
                return;
            }
            validOut = Arrays.copyOf(dataOut, len);
            String decOut = new String(validOut);
            result.setText("RSA private key encrypt/public key decrypt:"
                    + "\nBefore Encrypt:\n" + data
                    + "\nAfter encrypt:\n" + encOut
                    + "\nAfter decrypt:\n" + decOut);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 移除RSA密钥 */
    private void removeRSAKey() {
        try {
            //remove public key
            MyApplication.mSecurityOptV2.removeRSAKey(0);
            //remove private key
            MyApplication.mSecurityOptV2.removeRSAKey(1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** RSA private key signing */
    @SuppressLint("SetTextI18n")
    private void rsaSigning() {
        try {
            result.setText(null);
            byte[] hash = calcApkFileHash();
            String hexHash = ByteUtil.bytes2HexStr(hash);
            LogUtil.e(TAG, "RSA signing apk hash:" + hexHash);
            byte[] dataOut = new byte[1024];
            String rasSingAlg = AidlConstantsV2.Security.RSA_SIGN_ALG_5;
            int len = MyApplication.mSecurityOptV2.signingRSA(rasSingAlg, 1, hash, dataOut);
            if (len < 0) {
                LogUtil.e(TAG, "RSA signing data error, code:" + len);
                return;
            }
            byte[] signData = Arrays.copyOf(dataOut, len);
            signature = ByteUtil.bytes2HexStr(signData);
            LogUtil.e(TAG, "RSA signature:" + signature);
            result.setText("RSA signing:"
                    + "\nAPK hash:\n" + hexHash
                    + "\nsignature:\n" + signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** RSA verify signature */
    private void rsaVerifySignature() {
        try {
            result.setText(null);
            String rasSingAlg = AidlConstantsV2.Security.RSA_SIGN_ALG_5;
            byte[] hash = calcApkFileHash();
            String hexHash = ByteUtil.bytes2HexStr(hash);
            LogUtil.e(TAG, "RSA signing apk hash:" + hexHash);
            byte[] dataOut = new byte[1024];
            int len = MyApplication.mSecurityOptV2.getRSAPublicKey(0, dataOut);
            if (len < 0) {
                LogUtil.e(TAG, "Get RSA public key failed:" + len);
                return;
            }
            byte[] pubKey = Arrays.copyOf(dataOut, len);
            byte[] signData = ByteUtil.hexStr2Bytes(signature);
            int code = MyApplication.mSecurityOptV2.verifySignatureRSA(rasSingAlg, pubKey, hash, signData);
            String msg = "RSA verify signature " + (code == 0 ? "success" : "failed");
            LogUtil.e(TAG, msg);
            result.setText(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 计算APK文件的hash值 */
    private byte[] calcApkFileHash() {
        try {
            PackageManager pkgManager = getPackageManager();
            ApplicationInfo appInfo = pkgManager.getApplicationInfo(getPackageName(), 0);
            String file = appInfo.sourceDir;
            if (TextUtils.isEmpty(file)) {
                return new byte[0];
            }
            File f = new File(file);
            if (!f.exists() || !f.isFile()) {
                return new byte[0];
            }
            byte[] buff = new byte[1024];
            int len = 0;
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            FileInputStream ins = new FileInputStream(f);
            while ((len = ins.read(buff)) != -1) {
                digest.update(buff, 0, len);
            }
            return digest.digest();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

//    /** Save ciphertext key with decrypt key is a RSA private key */
//    private void saveCipherTextKeyRSA() {
//        try {
//            byte[] rawKey = ByteUtil.hexStr2Bytes("F2914D44BC2AF055F3D013FD4A923F0E");
//            byte[] kcv = ByteUtil.hexStr2Bytes("3D4716C16E5D6D50");
//            byte[] dataOut = new byte[1024];
//
//            // 1.encrypt a key with RSA public key
//            int len = MyApplication.mSecurityOptV2.dataEncryptRSA(Security.RSA_TRANSFORMATION_4, 0, rawKey, dataOut);
//            if (len < 0) {
//                LogUtil.e(TAG, "RSA private key encrypt data failed:" + len);
//                return;
//            }
//
//            //2.save ciphertext key with decrypt key is RSA private key
//            byte[] ciphertextkey = Arrays.copyOf(dataOut, len);
//            int code = MyApplication.mSecurityOptV2.saveCiphertextKeyRSA(Security.KEY_TYPE_REC, ciphertextkey, kcv, Security.KEY_ALG_TYPE_3DES, 2,
//                    1, Security.RSA_TRANSFORMATION_4);
//            if (code < 0) {
//                LogUtil.e(TAG, "saveCiphertextKeyRSA failed:" + code);
//                return;
//            }
//
//            //test the saved key
//            byte[] dataIn = ByteUtil.hexStr2Bytes("3132333435363738");
//            code = MyApplication.mSecurityOptV2.dataEncrypt(2, dataIn, Security.DATA_MODE_ECB, null, dataOut);
//            if (code < 0) {
//                LogUtil.e(TAG, "dataEncrypt failed:" + code);
//                return;
//            }
//            byte[] validOut = Arrays.copyOf(dataOut, dataIn.length);
//            String validOutStr = ByteUtil.bytes2HexStr(validOut);
//            if ("C391856B28E61F2F".equals(validOutStr)) {
//                showToast("success");
//                LogUtil.e(TAG, "saveCipherTextKeyRSA success");
//            } else {
//                showToast("failed");
//                LogUtil.e(TAG, "saveCipherTextKeyRSA failed");
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * Test save RSA key as following steps:
//     * <br/> click [Generate RSA keys] button
//     * <br/> click [Save RSA key] button
//     */
//    private void saveRSAKey() {
//        try {
//            byte[] pubKey = ByteUtil.hexStr2Bytes("30820120300D06092A864886F70D010101050" +
//                    "00382010D00308201080282010100D6498E8B18F388FAD44BF1DC6CF8888B3BF963" +
//                    "8B89401D1F651C051BB3058EE1A4AD71283363B9F896A5A2DBC9F2AB2DAC63A98CC" +
//                    "37C55A30E7AF88C21E3BB92A5F5669B396B453B55005BCD4E61D465A9A4D74AC9DF" +
//                    "8A71F800F5CBF33234091707AC926F4E27D546F8B3F0A1BE92C3A3A2E7F9640BCD5" +
//                    "FFA802D13279062979D6516F2F1D2B01F417D995A854206DEC352344C6FC80BB3E6" +
//                    "48324D5436837F9E39CFA0166FC5588A3DED4FCC50BAC53DEA6AD43FD17050C7D0C" +
//                    "F5C62FC835D9469BFA88D30584D008AFC1EB6D642745BB32CBBC6F2F35E430BD26C" +
//                    "95A983B77DCF0F15169F3A72A97B86820E6D4B68E092EFB0A01D6C24469D6ABF5D0D61E1020103");
//            byte[] pvtKey = ByteUtil.hexStr2Bytes("308204BD020100300D06092A864886F70D0101" +
//                    "010500048204A7308204A30201000282010100D6498E8B18F388FAD44BF1DC6CF888" +
//                    "8B3BF9638B89401D1F651C051BB3058EE1A4AD71283363B9F896A5A2DBC9F2AB2DAC" +
//                    "63A98CC37C55A30E7AF88C21E3BB92A5F5669B396B453B55005BCD4E61D465A9A4D7" +
//                    "4AC9DF8A71F800F5CBF33234091707AC926F4E27D546F8B3F0A1BE92C3A3A2E7F964" +
//                    "0BCD5FFA802D13279062979D6516F2F1D2B01F417D995A854206DEC352344C6FC80B" +
//                    "B3E648324D5436837F9E39CFA0166FC5588A3DED4FCC50BAC53DEA6AD43FD17050C7" +
//                    "D0CF5C62FC835D9469BFA88D30584D008AFC1EB6D642745BB32CBBC6F2F35E430BD2" +
//                    "6C95A983B77DCF0F15169F3A72A97B86820E6D4B68E092EFB0A01D6C24469D6ABF5D" +
//                    "0D61E102010302820101008EDBB45CBB4D05FC8D87F692F3505B077D50ED07B0D568" +
//                    "BF98BD58BD2203B4966DC8F61ACCED26A5B9C3C1E7DBF71CC91D97C65DD7A839175E" +
//                    "FCA5B2C1427D0C6EA399BCD0F22E278E003D3389968D991BC33A31DBEA5C4BFAAB4E" +
//                    "87F776CD5B64AFC8619F896FE384A5CD4B167F0C826D174550ED5D339551AAC8B76F" +
//                    "B5970E85F6269AD75F0B8BCA4408046A99A2DC4CA98EBD12068C84F04610E6E4190F" +
//                    "E259C679B1DA302F27E014DE5BB4B4087DC6A1FBF69ED094E40A54FB54D14B1E1AF1" +
//                    "6E169E3F77D9490BBB4B16DAB1E2EBBA2D5AD4C00FD72AF3A3343E28F12BFEEEDAE2" +
//                    "F738C5C2E0ACC3C3C476BC5E49E7BDA06DD171DDFC1D224D43F982D5CB02818100EC" +
//                    "52F8F2FBE0250DC8BFC8971C806F73B7C7D89B59005FB53024E40F86E342E6D6ED11" +
//                    "2D6FE71BF60BB42D57BA2DA115CDD8A84B938A12387AD59C7F6C14E76759D3E9DF52" +
//                    "D67E95F84F71A34A5F046D9AC518BD1DDBC97E6DE44349BAC94A104DECA92AB78C2C" +
//                    "4A394314F92569FBBB5C47418E624188D565BDD5449F96698502818100E820E417B2" +
//                    "E3F9BFC957C4BCC8DB2320988C05957BBDD9374DBA34E3772DA8C540A307E7DF4062" +
//                    "A6AE6A726E83150CF2C61EC896BE0E7EC23D7BB9DDBCF6EECDD070B3DBDB2613C976" +
//                    "A299D9246C69A529AA0BBF88FF671F67B2C0C59D7677A8C99A117789EA69D76D12CB" +
//                    "E236E8C23EA8AF3D7D83B11654B52C2194C732B7AD028181009D8CA5F752956E0930" +
//                    "7FDB0F68559FA27A853B123B559523756DED5FAF422C99E49E0B739FEF67F95D22C8" +
//                    "E526C9160E893B1ADD0D06B6D051E3BDAA480DEF9A3BE29BEA3739A9B95034F66CDC" +
//                    "3F584911D8BB28BE9286544942D7867C86316033F31B71CFB2C8317B820DFB6E46A7" +
//                    "D23D84D65EEC2BB08E43D3E383150EF103028181009AC0980FCC97FBD530E52DD330" +
//                    "92176B105D590E527E90CF8926CDECFA1E7083806CAFEFEA2AEC6F1EF1A19F020E08" +
//                    "A1D969DB0F295EFF2C28FD2693D34F49DE8AF5CD3D3CC40D30F9C1BBE61848466E1B" +
//                    "C6B27FB0AA44BF9A772B2E68F9A51B311160FA5BF19BE4F361DD4179F0817F1B1F7E" +
//                    "53AD20B98DCE1D6BB884CC7A73028180213E67C4ACA17C294F3D1926A7FEDE6CA68D" +
//                    "DB3B6B46A5A479BAF0D5A1861AF87EFA26FD2547DC0FE693B9563A3B42F9FB19252B" +
//                    "8466B8CFE065AE58D16BE0F3BC4EE04763EB7B43BE9B766D28747ECDBEB67471310E" +
//                    "8D5B14477A9C8D3F21E042E7B26679DFA05932BDBCD299CC35E62EF41ADA9EA3E7336A50702DBECFA42C");
//            //save RSA public key, keyType is 0
//            byte[] dataOut = new byte[2048];
//            int code = MyApplication.mSecurityOptV2.saveRSAKey(0, pubKey, 2);
//            if (code < 0) {
//                LogUtil.e(TAG, "save RSA public key failed:" + code);
//                return;
//            }
//            showToast("Save RSA Keys success");
//
//            //save RSA private key, keyType is 1
//            code = MyApplication.mSecurityOptV2.saveRSAKey(1, pvtKey, 3);
//            if (code < 0) {
//                LogUtil.e(TAG, "save RSA public key failed:" + code);
//                return;
//            }
//            // Test RSA public key encrypt private key decrypt
//            String data = "\"I have something to tell you, white mouse,\" she said. \"Mr. Behrman died of pneumonia to-day in the hospital."
//                    + " He was ill only two days. The janitor found him the morning of the first day in his room downstairs helpless with pain."
//                    + " His shoes and clothing were wet through and icy cold. They couldn't imagine where he had been on such a dreadful night."
//                    + " And then they found a lantern, still lighted, and a ladder that had been dragged from its place, and some scattered brushes,"
//                    + " and a palette with green and yellow colours mixed on it, and - look out the window, dear, at the last ivy leaf on the wall."
//                    + " Didn't you wonder why it never fluttered or moved when the wind blew? Ah, darling, it's Behrman's masterpiece - he painted it"
//                    + " there the night that the last leaf fell.";
//            int len = MyApplication.mSecurityOptV2.dataEncryptRSA(AidlConstantsV2.Security.RSA_TRANSFORMATION_4, 2, data.getBytes(), dataOut);
//            if (len < 0) {
//                LogUtil.e(TAG, "RSA private key encrypt data failed:" + len);
//                return;
//            }
//            byte[] encryptOut = Arrays.copyOf(dataOut, len);
//            len = MyApplication.mSecurityOptV2.dataDecryptRSA(AidlConstantsV2.Security.RSA_TRANSFORMATION_4, 3, encryptOut, dataOut);
//            if (len < 0) {
//                LogUtil.e(TAG, "RSA private key decrypt data failed:" + len);
//                return;
//            }
//            byte[] validOut = Arrays.copyOf(dataOut, len);
//            String decOut = new String(validOut);
//            if (!TextUtils.equals(decOut, data)) {
//                throw new RuntimeException("RSA public key encrypt result not identical!");
//            }
//            showToast("RSA public key encryption private key decryption with saved keys success");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }

}
