package com.sm.sdk.demo.card;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.regex.Pattern;

import sunmi.sunmiui.utils.LogUtil;

/**
 * This page show how to transmit apdu to RFC card.
 * <br/> Both apdus which comply with or not comply with ISO-7816
 * can be transmitted, the ISO-7816 standard apdu format is:
 * <br/>commad(4B) + Lc(1B,value is len) + indata(len B) + Le(1B)
 */
public class TransmitApduActivity extends BaseAppCompatActivity {

    private static final String TAG = "TransmitApduActivity";

    private EditText apdu;
    private Button checkCard;
    private Button sendApdu;
    private TextView result;
    private int cardType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit_apdu_layout);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_transmit_apdu);
        apdu = findViewById(R.id.apdu);
        checkCard = findViewById(R.id.check_card);
        sendApdu = findViewById(R.id.send_apdu);
        result = findViewById(R.id.result);
        checkCard.setOnClickListener(this);
        sendApdu.setOnClickListener(this);
    }

    private void initData() {
        apdu.setText("00A404000E315041592E5359532E444446303100");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_card:
                handleClearData();
                checkCard();
                break;
            case R.id.send_apdu:
                if (checkInputData()) {
                    transmitApdu();
                }
                break;
        }
    }

    /** 清除结果 */
    private void handleClearData() {
        result.setText("");
    }

    private CheckCardCallbackV2 mReadCardCallback = new CheckCardCallbackV2Wrapper() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            LogUtil.e(TAG, "findMagCard,bundle:" + bundle);
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            LogUtil.e(TAG, "findICCard, atr:" + atr);
            handleCheckCardSuccess("findICCard, atr:" + atr);
            cardType = AidlConstantsV2.CardType.IC.getValue();
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            LogUtil.e(TAG, "findRFCard, uuid:" + uuid);
            handleCheckCardSuccess("findRFCard, uuid:" + uuid);
            cardType = AidlConstantsV2.CardType.NFC.getValue();
            //If want to transmit apdu to Mifare or Felica card,
            //change cardType to corresponding value, eg:
            //cardType = AidlConstantsV2.CardType.MIFARE.getValue()
        }

        @Override
        public void onError(final int code, final String msg) throws RemoteException {
            LogUtil.e(TAG, "check card error,code:" + code + "message:" + msg);
            handleCheckCardFailed(code, msg);
        }
    };

    /** 刷卡 */
    private void checkCard() {
        try {
            //支持M1卡
            int allType = AidlConstants.CardType.NFC.getValue()
                    | AidlConstants.CardType.IC.getValue()
                    | AidlConstants.CardType.MIFARE.getValue()
                    | AidlConstants.CardType.FELICA.getValue();
            MyApplication.mReadCardOptV2.checkCard(allType, mReadCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCheckCardSuccess(String msg) {
        runOnUiThread(() -> {
            StringBuilder sb = new StringBuilder("----------------------- check card success -----------------------\n");
            sb.append(msg);
            sb.append("\n");
            result.setText(sb);
            result.setTag(sb);
            sendApdu.setEnabled(true);
        });
    }

    private void handleCheckCardFailed(int code, final String msg) {
        addText("check card error,code:" + code + ", message:" + msg + "\n");
    }

    private boolean checkInputData() {
        if (cardType != AidlConstantsV2.CardType.NFC.getValue()
                && cardType != AidlConstantsV2.CardType.MIFARE.getValue()
                && cardType != AidlConstantsV2.CardType.FELICA.getValue()
                && cardType != CardType.IC.getValue()
                && cardType != CardType.PSAM0.getValue()
                && cardType != CardType.SAM1.getValue()) {
            showToast("transmit apdu not support card type:" + cardType);
            return false;
        }
        String apduStr = apdu.getText().toString();
        if (TextUtils.isEmpty(apduStr)) {
            apdu.requestFocus();
            showToast("apdu should not be empty!");
            return false;
        }
        if (!Pattern.matches("[0-9a-fA-F]+", apduStr)) {
            apdu.requestFocus();
            showToast("apdu should hex characters!");
            return false;
        }
        return true;
    }

    /**
     * 透传APDU到卡片
     */
    private void transmitApdu() {
        byte[] send = ByteUtil.hexStr2Bytes(apdu.getText().toString());
        byte[] recv = new byte[260];
        try {
            int len = MyApplication.mReadCardOptV2.transmitApdu(cardType, send, recv);
            if (len < 0) {
                LogUtil.e(TAG, "transmitApdu failed,code:" + len);
                showToast(AidlErrorCodeV2.valueOf(len).getMsg());
            } else {
                LogUtil.e(TAG, "transmitApdu success,recv:" + ByteUtil.bytes2HexStr(recv));
                byte[] valid = Arrays.copyOf(recv, len);
                if (cardType == CardType.NFC.getValue() || cardType == CardType.IC.getValue()
                        || cardType == CardType.PSAM0.getValue() || cardType == CardType.SAM1.getValue()) {
                    // (NFC)received data contains swa,swb
                    byte[] outData = Arrays.copyOf(valid, valid.length - 2);
                    byte swa = valid[valid.length - 2];//swa
                    byte swb = valid[valid.length - 1];//swb
                    showApduRecv(true, outData, swa, swb);
                } else {
                    // (Mifare/Felica)received data not contains swa,swb
                    showApduRecv(false, valid, Byte.MIN_VALUE, Byte.MIN_VALUE);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示收到的APDU数据
     */
    private void showApduRecv(boolean hasSW, byte[] outData, byte swa, byte swb) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        SpannableString sb = new SpannableString("------------------- APDU Receive-------------------\n");
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(sb);

        ssb.append("outData:");
        ssb.append(ByteUtil.bytes2HexStr(outData));
        ssb.append("\n");
        if (hasSW) {
            ssb.append("SWA:");
            ssb.append(ByteUtil.bytes2HexStr(swa));
            ssb.append("\n");
            ssb.append("SWB:");
            ssb.append(ByteUtil.bytes2HexStr(swb));
            ssb.append("\n");
        }
        addText(ssb);
    }

    private void addText(CharSequence msg) {
        String preMsg = result.getTag().toString();
        runOnUiThread(() -> result.setText(TextUtils.concat(preMsg, msg)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cardOff(AidlConstantsV2.CardType.NFC.getValue());
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
