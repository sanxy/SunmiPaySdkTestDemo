package com.sm.sdk.demo.card;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.card.wrapper.CheckCardCallbackV2Wrapper;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.CardType;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.bean.ApduRecvV2;
import com.sunmi.pay.hardware.aidlv2.bean.ApduSendV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;

import java.util.Arrays;
import java.util.regex.Pattern;

public class FELICAActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText apduCmd;
    private EditText apduLc;
    private EditText apduIndata;
    private EditText apduLe;
    private TextView mTvResultInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_felica);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_FELICA);
        apduCmd = findViewById(R.id.edit_command);
        apduLc = findViewById(R.id.edit_lc_length);
        apduIndata = findViewById(R.id.edit_data);
        apduLe = findViewById(R.id.edit_le_length);
        mTvResultInfo = findViewById(R.id.tv_info);
        findViewById(R.id.mb_check_card).setOnClickListener(this);
        findViewById(R.id.mb_send_apdu).setOnClickListener(this);
        apduCmd.setText("00000000");
        apduLe.setText("0100");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_check_card:
                mTvResultInfo.setText(null);
                checkCard();
                break;
            case R.id.mb_send_apdu:
                if (checkInputData()) {
                    sendApduByApduCommand();
                }
                break;
        }
    }

    private void checkCard() {
        try {
            int cardType = CardType.FELICA.getValue();
            MyApplication.mReadCardOptV2.checkCard(cardType, mCheckCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CheckCardCallbackV2 mCheckCardCallback = new CheckCardCallbackV2Wrapper() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            StringBuilder sb = new StringBuilder();
            for (String key : bundle.keySet()) {
                sb.append(key);
                sb.append(":");
                sb.append(bundle.get(key));
                sb.append("\n");
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            String msg = "findMagCard:" + sb;
            LogUtil.e(Constant.TAG, msg);
            setInfoText(msg);
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            String msg = "findICCard,atr:" + atr;
            LogUtil.e(Constant.TAG, msg);
            setInfoText(msg);
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            String msg = "findRFCard,uuid:" + uuid;
            LogUtil.e(Constant.TAG, msg);
            setInfoText(msg);
            setDefaultIndata(uuid);
        }

        @Override
        public void onError(int code, String message) throws RemoteException {
            String error = "onError:" + message + " -- " + code;
            LogUtil.e(Constant.TAG, error);
            showToast(error);
        }
    };

    /** 设置默认数据 */
    private void setDefaultIndata(String uuid) {
        if (uuid.length() >= 18) {
            uuid = uuid.substring(2, 18);
        }
        final String uuidStr = "1006" + uuid + "010009018000";
        runOnUiThread(() -> {
            String hexLc = Integer.toHexString(uuidStr.length() / 2);
            if (hexLc.length() % 2 != 0) {
                hexLc = "0" + hexLc;
            }
            apduLc.setText(hexLc);
            apduIndata.setText(uuidStr);
        });
    }

    /** Check input data */
    private boolean checkInputData() {
        int limitLen = 4;
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

    /** check whether src is hex format */
    private boolean checkHexValue(String src) {
        return Pattern.matches("[0-9a-fA-F]+", src);
    }

    private String formatStr(String format, Object... params) {
        return String.format(format, params);
    }

    /** 以ApduRecvV2方式发送ISO-7816标准的APDU */
    private void sendApduByApduCommand() {
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
            int code = MyApplication.mReadCardOptV2.apduCommand(AidlConstantsV2.CardType.FELICA.getValue(), send, recv);
            if (code < 0) {
                LogUtil.e(TAG, "apduCommand failed,code:" + code);
                showToast(getString(R.string.fail) + ":" + code);
            } else {
                showApduRecv(recv.outlen, recv.outData, recv.swa, recv.swb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 显示收到的APDU数据 */
    private void showApduRecv(int outLen, byte[] outData, byte swa, byte swb) {
        String swaStr = ByteUtil.bytes2HexStr(swa);
        String swbStr = ByteUtil.bytes2HexStr(swb);
        String outDataStr = ByteUtil.bytes2HexStr(Arrays.copyOf(outData, outLen));
        String temp = String.format("SWA:%s\nSWB:%s\noutData:%s", swaStr, swbStr, outDataStr);
        setInfoText(temp);
    }

    private void setInfoText(CharSequence msg) {
        runOnUiThread(() -> {
            mTvResultInfo.setText(msg);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCheckCard();
    }

    private void cancelCheckCard() {
        try {
            MyApplication.mReadCardOptV2.cardOff(CardType.FELICA.getValue());
            MyApplication.mReadCardOptV2.cancelCheckCard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
