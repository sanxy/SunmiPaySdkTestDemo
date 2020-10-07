package com.sm.sdk.demo.emv;

import android.os.Bundle;
import android.os.RemoteException;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.ThreadPoolUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVCandidateV2;
import com.sunmi.pay.hardware.aidlv2.bean.EMVTransDataV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVListenerV2;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import sunmi.sunmiui.utils.LogUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This page show how to read the India RuPay card.
 * If your card not an india RuPay card, please do
 * not refer to any code of this page.
 */
public class RuPayCardActivity extends BaseAppCompatActivity {

    private TextView tvUUID;
    private TextView tvAtr;
    private TextView tvCardNo;
    private TextView tvExpireDate;
    private TextView tvCardHolder;

    private EMVOptV2 emvOptV2 = MyApplication.mEMVOptV2;
    private int carType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rupaycard_layout);
        initData();
        intiView();
    }

    private void initData() {
        // The india country
        ThreadPoolUtil.executeInCachePool(
                () -> {
                    EmvUtil.initKey();
                    EmvUtil.initAidAndRid();
                    Map<String, String> map = EmvUtil.getConfig(EmvUtil.COUNTRY_INDIA);
                    EmvUtil.setTerminalParam(map);
                }
        );
    }

    private void intiView() {
        initToolbarBringBack(R.string.emv_read_rupay_card);

        findViewById(R.id.read_card).setOnClickListener(
                v -> checkCard()
        );
        tvUUID = findViewById(R.id.tv_uuid);
        tvAtr = findViewById(R.id.tv_atr);
        tvCardNo = findViewById(R.id.tv_card_no);
        tvExpireDate = findViewById(R.id.tv_expire_date);
        tvCardHolder = findViewById(R.id.tv_cardholder);
    }

    private void checkCard() {
        try {
            MyApplication.mEMVOptV2.initEmvProcess(); // clear all TLV data
            showLoadingDialog("swipe card or insert card");
            int cardType = AidlConstantsV2.CardType.NFC.getValue() | AidlConstantsV2.CardType.IC.getValue();
            MyApplication.mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {

        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(Constant.TAG, "findMagCard:" + bundle);
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(Constant.TAG, "findICCard:" + atr);
            carType = AidlConstantsV2.CardType.IC.getValue();
            runOnUiThread(
                    () -> {
                        tvUUID.setText(R.string.card_uuid);
                        String text = getString(R.string.card_atr) + atr;
                        tvAtr.setText(text);
                    }
            );
            transactProcess();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(Constant.TAG, "findRFCard:" + uuid);
            carType = AidlConstantsV2.CardType.NFC.getValue();
            runOnUiThread(
                    () -> {
                        String text = getString(R.string.card_uuid) + uuid;
                        tvUUID.setText(text);
                        tvAtr.setText(R.string.card_atr);
                    }
            );
            transactProcess();
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
            dismissLoadingDialog();
        }

    };

    private void transactProcess() {
        LogUtil.e(Constant.TAG, "transactProcess");
        try {
            EMVTransDataV2 emvTransData = new EMVTransDataV2();
            emvTransData.amount = "1";
            emvTransData.flowType = 0x02;
            emvTransData.cardType = carType;
            MyApplication.mEMVOptV2.transactProcess(emvTransData, mEMVListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initEmvTlvData() {
        try {
            // set normal tlv data
            String[] tags = {
                    "5F2A", "5F36"
            };
            String[] values = {
                    "0356", "02"
            };
            emvOptV2.setTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tags, values);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private EMVListenerV2 mEMVListener = new EMVListenerV2.Stub() {

        @Override
        public void onWaitAppSelect(List<EMVCandidateV2> appNameList, boolean isFirstSelect) throws RemoteException {
            LogUtil.e(Constant.TAG, "onWaitAppSelect isFirstSelect:" + isFirstSelect);
            emvOptV2.importAppSelect(0);
        }

        @Override
        public void onAppFinalSelect(String tag9F06value) throws RemoteException {
            LogUtil.e(Constant.TAG, "onAppFinalSelect tag9F06value:" + tag9F06value);
            initEmvTlvData();
            emvOptV2.importAppFinalSelectStatus(0);
        }

        @Override
        public void onConfirmCardNo(String cardNo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onConfirmCardNo cardNo:" + cardNo);
            emvOptV2.importCardNoStatus(0);
            runOnUiThread(
                    () -> {
                        String text = getString(R.string.card_NO) + cardNo;
                        tvCardNo.setText(text);
                    }
            );
        }

        @Override
        public void onRequestShowPinPad(int pinType, int remainTime) throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestShowPinPad pinType:" + pinType + " remainTime:" + remainTime);
        }

        @Override
        public void onRequestSignature() throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestSignature");
        }

        @Override
        public void onCertVerify(int certType, String certInfo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onCertVerify certType:" + certType + " certInfo:" + certInfo);
        }

        @Override
        public void onOnlineProc() throws RemoteException {
            LogUtil.e(Constant.TAG, "onOnlineProcess");
        }

        @Override
        public void onCardDataExchangeComplete() throws RemoteException {
            LogUtil.e(Constant.TAG, "onCardDataExchangeComplete");
        }

        @Override
        public void onTransResult(int code, String desc) throws RemoteException {
            dismissLoadingDialog();
            LogUtil.e(Constant.TAG, "onTransResult code:" + code + " desc:" + desc);
            LogUtil.e(Constant.TAG, "***************************************************************");
            LogUtil.e(Constant.TAG, "****************************End Process************************");
            LogUtil.e(Constant.TAG, "***************************************************************");
            getExpireDateAndCardholderName();
        }

        @Override
        public void onConfirmationCodeVerified() throws RemoteException {
            dismissLoadingDialog();
            LogUtil.e(Constant.TAG, "onConfirmationCodeVerified");
        }

        @Override
        public void onRequestDataExchange(String cardNo) throws RemoteException {
            LogUtil.e(Constant.TAG, "onRequestDataExchange,cardNo:" + cardNo);
            emvOptV2.importDataExchangeStatus(0);
        }
    };

    private void getExpireDateAndCardholderName() throws RemoteException {
        byte[] out = new byte[64];
        String[] tags = {
                "5F24", "5F20"
        };
        int len = emvOptV2.getTlvList(AidlConstantsV2.EMV.TLVOpCode.OP_NORMAL, tags, out);
        if (len > 0) {
            byte[] bytesOut = Arrays.copyOf(out, len);
            String hexStr = ByteUtil.bytes2HexStr(bytesOut);
            Map<String, TLV> map = TLVUtil.buildTLVMap(hexStr);
            TLV tlv5F24 = map.get("5F24"); // expire date
            TLV tlv5F20 = map.get("5F20"); // cardholder name
            String expireDate = "";
            String cardholder = "";
            if (tlv5F24 != null && tlv5F24.getValue() != null) {
                expireDate = tlv5F24.getValue();
            }
            if (tlv5F20 != null && tlv5F20.getValue() != null) {
                String value = tlv5F20.getValue();
                byte[] bytes = ByteUtil.hexStr2Bytes(value);
                cardholder = new String(bytes);
            }
            final String finalExpireDate = expireDate;
            final String finalCardholder = cardholder;
            runOnUiThread(
                    () -> {
                        String text = getString(R.string.card_expire_date) + finalExpireDate;
                        tvExpireDate.setText(text);
                        text = getString(R.string.cardholder_name) + finalCardholder;
                        tvCardHolder.setText(text);
                    }
            );
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cardOff(AidlConstantsV2.CardType.IC.getValue());
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
