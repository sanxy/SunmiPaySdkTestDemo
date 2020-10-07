package com.sm.sdk.demo.card;

import android.content.Context;
import android.content.Intent;
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
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.ApduRecvV2;
import com.sunmi.pay.hardware.aidlv2.bean.ApduSendV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.regex.Pattern;

import sunmi.sunmiui.utils.LogUtil;

/**
 * This page show how to send/receive apdu to IC/RFC card.
 * <br/>The sent/received apdu should comply with ISO-7816 standard,format is:
 * <br/>commad(4B) + Lc(1B,value is len) + indata(len B) + Le(1B)
 */
public class NormalApduActivity extends BaseAppCompatActivity {
    private static final String TAG = "NormalApduActivity";

    private EditText apduCmd;
    private EditText apduLc;
    private EditText apduIndata;
    private EditText apduLe;
    private Button checkCard;
    private Button sendApdu;
    private TextView result;
    private int cardType;

    public static void start(Context context) {
        Intent starter = new Intent(context, NormalApduActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_apdu_layout);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_apdu);

        apduCmd = findViewById(R.id.apdu_cmd);
        apduLc = findViewById(R.id.apdu_lc);
        apduIndata = findViewById(R.id.apdu_indata);
        apduLe = findViewById(R.id.apdu_le);
        checkCard = findViewById(R.id.check_card);
        sendApdu = findViewById(R.id.send_apdu);
        result = findViewById(R.id.result);
        checkCard.setOnClickListener(this);
        sendApdu.setOnClickListener(this);
    }

    private void initData() {
        apduCmd.setText("00A40400");
        apduLc.setText("0E");
        apduIndata.setText("315041592E5359532E4444463031");
        apduLe.setText("00");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.check_card:
                handleClearData();
                checkCard();
                break;
            case R.id.send_apdu:
//                if (checkInputData(true)) {
//                    sendApduByApduSendV2();
//                }
                if (checkInputData(false)) {
                    sendApduByBytes();
                }
                break;
        }
    }

    /** 清除结果 */
    private void handleClearData() {
        result.setText("");
    }

    /** 刷卡 */
    private void checkCard() {
        try {
            //支持M1卡
            int allType = AidlConstants.CardType.NFC.getValue() | AidlConstants.CardType.IC.getValue();
            MyApplication.mReadCardOptV2.checkCard(allType, mReadCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        }

        @Override
        public void onError(final int code, final String msg) throws RemoteException {
            LogUtil.e(TAG, "check card error,code:" + code + "message:" + msg);
            handleCheckCardFailed(code, msg);
        }
    };

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

    private boolean checkInputData(boolean isApduCmd) {
        int limitLen = isApduCmd ? 4 : 2;
        String command = apduCmd.getText().toString();
        String lc = apduLc.getText().toString();
        String indata = apduIndata.getText().toString();
        String le = apduLe.getText().toString();

        if (command.length() != 8 || !checkHexValue(command)) {
            apduCmd.requestFocus();
            showToast("command should be 8 hex characters!");
            return false;
        }
        if (lc.length() > limitLen || !checkHexValue(lc)) {
            apduLc.requestFocus();
            showToast(formatStr("Lc should less than %d hex characters!", limitLen));
            return false;
        }
        int lcValue = Integer.parseInt(lc, 16);
        if (lcValue < 0 || lcValue > 256) {
            apduLc.requestFocus();
            showToast("Lc value should in [0,0x0100]");
            return false;
        }
        if (indata.length() != lcValue * 2 || (indata.length() > 0 && !checkHexValue(indata))) {
            apduIndata.requestFocus();
            showToast("indata value should lc*2 hex characters!");
            return false;
        }
        if (le.length() > limitLen || !checkHexValue(le)) {
            apduLe.requestFocus();
            showToast(formatStr("Le should less than %d hex characters!", limitLen));
            return false;
        }
        int leValue = Integer.parseInt(le, 16);
        if (leValue < 0 || leValue > 256) {
            apduLe.requestFocus();
            showToast("Le value should in [0,0x0100]");
            return false;
        }
        return true;
    }

    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

    private String formatStr(String format, Object... params) {
        return String.format(format, params);
    }

    /** 以ApduRecvV2方式发送ISO-7816标准的APDU */
    private void sendApduByApduSendV2() {
        String command = apduCmd.getText().toString();
        String lc = apduLc.getText().toString();
        String indata = apduIndata.getText().toString();
        String le = apduLe.getText().toString();
        ApduSendV2 send = new ApduSendV2();
        send.command = ByteUtil.hexStr2Bytes(command);
        send.lc = Short.parseShort(lc, 16);
        send.dataIn = ByteUtil.hexStr2Bytes(indata);
        send.le = Short.parseShort(le, 16);
        try {
            ApduRecvV2 recv = new ApduRecvV2();
            int code = MyApplication.mReadCardOptV2.apduCommand(cardType, send, recv);
            if (code < 0) {
                LogUtil.e(TAG, "apduCommand failed,code:" + code);
                showToast(AidlErrorCodeV2.valueOf(code).getMsg());
            } else {
                LogUtil.e(TAG, "apduCommand success,recv:" + recv);
                showApduRecv(recv.outlen, recv.outData, recv.swa, recv.swb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 以字节数组方式发送ISO-7816标准的APDU */
    private void sendApduByBytes() {
        String command = apduCmd.getText().toString();
        String lc = apduLc.getText().toString();
        String indata = apduIndata.getText().toString();
        String le = apduLe.getText().toString();
        byte[] send = ByteUtil.concatByteArrays(
                ByteUtil.hexStr2Bytes(command),
                ByteUtil.hexStr2Bytes(lc),
                ByteUtil.hexStr2Bytes(indata),
                ByteUtil.hexStr2Bytes(le)
        );
        try {
            byte[] recv = new byte[260];
            int code = MyApplication.mReadCardOptV2.smartCardExchange(cardType, send, recv);
            if (code < 0) {
                LogUtil.e(TAG, "smartCardExchange failed,code:" + code);
                showToast(AidlErrorCodeV2.valueOf(code).getMsg());
            } else {
                LogUtil.e(TAG, "smartCardExchange success,recv:" + ByteUtil.bytes2HexStr(recv));
                int outlen = ByteUtil.unsignedShort2IntBE(recv, 0);
                byte[] outdata = {};
                if (outlen > 0) {
                    outdata = Arrays.copyOfRange(recv, 2, 2 + outlen);
                }
                byte swa = recv[2 + outlen];
                byte swb = recv[2 + outlen + 1];
                showApduRecv(outlen, outdata, swa, swb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 显示收到的APDU数据 */
    private void showApduRecv(int outLen, byte[] outData, byte swa, byte swb) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        SpannableString sb = new SpannableString("------------------- APDU Receive-------------------\n");
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(sb);

        ssb.append("outLen:");
        ssb.append(String.format("%02X", outLen));
        ssb.append("\n");
        ssb.append("outData:");
        ssb.append(ByteUtil.bytes2HexStr(outData));
        ssb.append("\n");
        ssb.append("SWA:");
        ssb.append(ByteUtil.bytes2HexStr(swa));
        ssb.append("\n");
        ssb.append("SWB:");
        ssb.append(ByteUtil.bytes2HexStr(swb));
        ssb.append("\n");
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
