package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
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
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CtrCodeAndMultiApduActivity extends BaseAppCompatActivity {
    private static final String TAG = "TransmitApduActivity";

    private EditText activeCtr;
    private EditText dataExCtr;
    private List<EditText> apduList;
    private Button checkCard;
    private Button sendApdu;
    private TextView result;
    private int cardType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardactive_and_data_ex_ctrcode_test_layout);
        initView();
        initData();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_ctr_code_multi_apdu_test);
        apduList = new ArrayList<>();
        activeCtr = findViewById(R.id.active_ctr_code);
        dataExCtr = findViewById(R.id.data_ex_ctr_code);
        apduList.add(findViewById(R.id.apdu_1));
        apduList.add(findViewById(R.id.apdu_2));
        apduList.add(findViewById(R.id.apdu_3));
        apduList.add(findViewById(R.id.apdu_4));
        apduList.add(findViewById(R.id.apdu_5));
        apduList.add(findViewById(R.id.apdu_6));
        apduList.add(findViewById(R.id.apdu_7));
        apduList.add(findViewById(R.id.apdu_8));
        apduList.add(findViewById(R.id.apdu_9));
        checkCard = findViewById(R.id.check_card);
        sendApdu = findViewById(R.id.send_apdu);
        result = findViewById(R.id.result);
        checkCard.setOnClickListener(this);
        sendApdu.setOnClickListener(this);
    }

    private void initData() {
        for (int i = 0; i < 5; i++) {
            apduList.get(i).setText(String.format("00840000%02X", (i + 4)));
        }
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
                    transmitMultiApdusWithCtrCode();
//                    testTransmitApduWithCtrCode();
                }
                break;
        }
    }

    /** 清除结果 */
    private void handleClearData() {
        result.setText(null);
    }

    /** 刷卡 */
    private void checkCard() {
        try {
            if (TextUtils.isEmpty(activeCtr.getText())) {
                showToast("激活配置不能为空");
                return;
            }
            //支持M1卡
            int allType = AidlConstants.CardType.NFC.getValue()
                    | AidlConstants.CardType.IC.getValue()
                    | AidlConstants.CardType.PSAM0.getValue()
                    | AidlConstants.CardType.SAM1.getValue();
            int ctrCode = Integer.parseInt(activeCtr.getText().toString(), 16);
            MyApplication.mReadCardOptV2.checkCardEx(allType, ctrCode, 0, mReadCardCallback, 60);
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
        public void findICCardEx(Bundle info) throws RemoteException {
            super.findICCardEx(info);
            String atr = info.getString("atr");
            LogUtil.e(TAG, "findICCard, atr:" + atr);
            handleCheckCardSuccess("findICCard, atr:" + atr);
            cardType = info.getInt("cardType");
        }

        @Override
        public void findRFCardEx(Bundle info) throws RemoteException {
            super.findRFCardEx(info);
            String uuid = info.getString("uuid");
            LogUtil.e(TAG, "findRFCard, uuid:" + uuid);
            handleCheckCardSuccess("findRFCard, uuid:" + uuid);
            cardType = info.getInt("cardType");
            //If want to transmit apdu to Mifare or Felica card,
            //change cardType to corresponding value, eg:
            //cardType = AidlConstantsV2.CardType.MIFARE.getValue()
        }

        @Override
        public void onErrorEx(Bundle info) throws RemoteException {
            super.onErrorEx(info);
            int code = info.getInt("code");
            String msg = info.getString("message");
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

    private boolean checkInputData() {
        if (TextUtils.isEmpty(dataExCtr.getText())) {
            showToast("数据交互配置不能为空");
            return false;
        }
        if (cardType != AidlConstantsV2.CardType.NFC.getValue()
                && cardType != CardType.IC.getValue()
                && cardType != CardType.PSAM0.getValue()
                && cardType != CardType.SAM1.getValue()) {
            showToast("transmit apdu not support card type:" + cardType);
            return false;
        }
        int count = 0;
        for (int i = 0; i < apduList.size(); i++) {
            EditText editText = apduList.get(i);
            String apduStr = editText.getText().toString();
            if (TextUtils.isEmpty(apduStr)) {
                continue;
            }
            if (!Pattern.matches("[0-9a-fA-F]+", apduStr)) {
                editText.requestFocus();
                showToast("apdu should hex characters!");
                return false;
            }
            count++;
        }
        if (count == 0) {
            showToast("Input at least one APDU!");
            return false;
        }
        if (count > 8) {
            showToast("too much APDUs, transmit APDUs must no more than 8 in once time");
            return false;
        }
        return true;
    }

    /** Transmit multi(0~8) APDUs to card */
    private void transmitMultiApdusWithCtrCode() {
        try {
            List<String> recvList = new ArrayList<>();
            List<String> sendList = new ArrayList<>();
            for (EditText edtText : apduList) {
                String apdu = edtText.getText().toString();
                if (!TextUtils.isEmpty(apdu)) {
                    sendList.add(apdu);
                }
            }
            int ctrCode = Integer.parseInt(dataExCtr.getText().toString(), 16);
            int code = MyApplication.mReadCardOptV2.transmitMultiApdus(cardType, ctrCode, sendList, recvList);
            if (code < 0) {
                LogUtil.e(TAG, "transmitApdu failed,code:" + code);
                showToast(AidlErrorCodeV2.valueOf(code).getMsg());
                return;
            }
            StringBuilder sb = new StringBuilder("------------------- APDU Receive-------------------");
            if (cardType == CardType.NFC.getValue() || cardType == CardType.IC.getValue()
                    || cardType == CardType.PSAM0.getValue() || cardType == CardType.SAM1.getValue()) {
                // (NFC)received data contains swa,swb
                for (String recv : recvList) {
                    int len = recv.length();
                    sb.append("\noutData:");
                    sb.append(recv.substring(0, len - 4));
                    sb.append("\nSWA:");
                    sb.append(recv.substring(len - 4, len - 2));
                    sb.append("\nSWB:");
                    sb.append(recv.substring(len - 2, len));
                }
            } else {
                // (Mifare/Felica)received data not contains swa,swb
                for (String recv : recvList) {
                    sb.append("\noutData:");
                    sb.append(recv);
                }
            }
            setText(sb);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Transmit one APDU to card */
    private void testTransmitApduWithCtrCode() {
        try {
            byte[] recv = new byte[260];
            byte[] send = ByteUtil.hexStr2Bytes(apduList.get(0).getText().toString());
            int ctrCode = Integer.parseInt(dataExCtr.getText().toString(), 16);
            int len = MyApplication.mReadCardOptV2.transmitApduExx(cardType, ctrCode, send, recv);
            if (len < 0) {
                LogUtil.e(TAG, "transmitApdu failed,code:" + len);
                showToast(AidlErrorCodeV2.valueOf(len).getMsg());
                return;
            }
            StringBuilder sb = new StringBuilder("------------------- APDU Receive-------------------");
            if (cardType == CardType.NFC.getValue() || cardType == CardType.IC.getValue()
                    || cardType == CardType.PSAM0.getValue() || cardType == CardType.SAM1.getValue()) {
                // (NFC)received data contains swa,swb
                sb.append("\noutData:");
                sb.append(ByteUtil.bytes2HexStr(recv, 0, len - 2));
                sb.append("\nSWA:");
                sb.append(ByteUtil.bytes2HexStr(recv[len - 2]));
                sb.append("\nSWB:");
                sb.append(ByteUtil.bytes2HexStr(recv[len - 1]));
            } else {
                // (Mifare/Felica)received data not contains swa,swb
                sb.append("\noutData:");
                sb.append(ByteUtil.bytes2HexStr(recv, 0, len));
            }
            setText(sb);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void setText(CharSequence msg) {
        runOnUiThread(() -> result.setText(msg));
    }

    private void addText(CharSequence msg) {
        runOnUiThread(() -> {
            CharSequence preMsg = result.getText();
            result.setText(TextUtils.concat(msg, "\n", preMsg));
        });
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
