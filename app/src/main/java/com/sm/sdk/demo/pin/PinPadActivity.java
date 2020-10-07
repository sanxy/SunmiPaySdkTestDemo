package com.sm.sdk.demo.pin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.emv.EmvUtil;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.utils.Utility;
import com.sunmi.pay.hardware.aidl.AidlConstants.PinBlockFormat;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadTextConfigV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;

import java.io.Serializable;

public class PinPadActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText txtConfirm;
    private EditText txtInputPin;
    private EditText txtInputOfflinePin;
    private EditText txtReinputOfflinePinFormat;

    private String cardNo;
    private EditText mEditCardNo;
    private EditText mEditTimeout;
    private EditText mEditKeyIndex;

    private TextView mTvInfo;

    private RadioGroup mRGKeyboard;
    private RadioGroup mRGIsOnline;
    private RadioGroup mRGKeyboardStyle;
    private RadioGroup mRGPikKeySystem;
    private RadioGroup mRGPinAlgorithmType;
    private SparseArray<CheckBox> rdoModeList;

    private static final int HANDLER_WHAT_INIT_PIN_PAD = 661;
    private static final int HANDLER_PIN_LENGTH = 662;
    private static final int HANDLER_CONFIRM = 663;
    private static final int HANDLER_WHAT_CANCEL = 664;
    private static final int HANDLER_ERROR = 665;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_WHAT_INIT_PIN_PAD:
                    initPinPad();
                    break;
                case HANDLER_WHAT_CANCEL:
                    showToast("user cancel");
                    break;
                case HANDLER_PIN_LENGTH:
                    showToast("inputting");
                    break;
                case HANDLER_CONFIRM:
                    showToast("click ok");
                    break;
                case HANDLER_ERROR:
                    showToast("error:" + msg.obj + " -- " + msg.arg1);
                    break;
            }
        }

    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad);
        initToolbarBringBack(R.string.pin_pad);
        initView();
        EmvUtil.setTerminalParam(EmvUtil.getConfig(EmvUtil.COUNTRY_CHINA));
    }

    private void initView() {
        rdoModeList = new SparseArray<>();
        rdoModeList.put(R.id.rdo_mode_normal, findViewById(R.id.rdo_mode_normal));
        rdoModeList.put(R.id.rdo_mode_long_press_to_clear, findViewById(R.id.rdo_mode_long_press_to_clear));
        rdoModeList.put(R.id.rdo_mode_silent, findViewById(R.id.rdo_mode_silent));
        rdoModeList.put(R.id.rdo_mode_green_led, findViewById(R.id.rdo_mode_green_led));
        for (int i = 0, size = rdoModeList.size(); i < size; i++) {
            rdoModeList.valueAt(i).setOnClickListener(this);
        }
        txtConfirm = findViewById(R.id.edit_txt_confirm);
        txtInputPin = findViewById(R.id.edit_txt_input_pin);
        txtInputOfflinePin = findViewById(R.id.edit_txt_input_offline_pin);
        txtReinputOfflinePinFormat = findViewById(R.id.edit_txt_reinput_offline_pin_fmt);
        mEditCardNo = findViewById(R.id.edit_card_no);
        mEditTimeout = findViewById(R.id.edit_timeout);
        mEditKeyIndex = findViewById(R.id.edit_key_index);

        mTvInfo = findViewById(R.id.tv_info);

        mRGKeyboard = findViewById(R.id.rg_keyboard);
        mRGIsOnline = findViewById(R.id.rg_is_online);
        mRGKeyboardStyle = findViewById(R.id.rg_keyboard_style);
        mRGPikKeySystem = findViewById(R.id.key_system);
        mRGPinAlgorithmType = findViewById(R.id.pin_type);

        rdoModeList.get(R.id.rdo_mode_normal).setChecked(true);
        findViewById(R.id.mb_set_mode).setOnClickListener(this);
        findViewById(R.id.mb_set_text).setOnClickListener(this);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        findViewById(R.id.call_custom_keyboard).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.rdo_mode_normal:
            case R.id.rdo_mode_long_press_to_clear:
            case R.id.rdo_mode_silent:
            case R.id.rdo_mode_green_led:
                onModeButtonClick(id);
                break;
            case R.id.mb_set_mode:
                onSetPinPadModeClick();
                break;
            case R.id.mb_set_text:
                initPinPadText();
                break;
            case R.id.mb_ok:
                mHandler.sendEmptyMessage(HANDLER_WHAT_INIT_PIN_PAD);
                break;
            case R.id.call_custom_keyboard:
                initCustomPinPad();
                break;
        }
    }

    private void onModeButtonClick(int id) {
        CheckBox normal = rdoModeList.get(R.id.rdo_mode_normal);
        if (id == R.id.rdo_mode_normal && normal.isChecked()) {
            for (int i = 0, size = rdoModeList.size(); i < size; i++) {
                if (rdoModeList.keyAt(i) != id) {
                    rdoModeList.valueAt(i).setChecked(false);
                }
            }
        } else if (rdoModeList.get(R.id.rdo_mode_normal).isChecked()) {
            rdoModeList.get(R.id.rdo_mode_normal).setChecked(false);
        }
    }

    /**
     * Set PinPad mode
     * the set value just valid for next time inputting PIN, after input PIN finished,
     * the set value is lost effect
     */
    private void onSetPinPadModeClick() {
        try {
            Bundle bundle = new Bundle();
            if (rdoModeList.get(R.id.rdo_mode_normal).isChecked()) {//Normal mode
                bundle.putInt("normal", 1);
            } else {
                if (rdoModeList.get(R.id.rdo_mode_long_press_to_clear).isChecked()) {
                    bundle.putInt("longPressToClear", 1);
                }
                if (rdoModeList.get(R.id.rdo_mode_silent).isChecked()) {
                    bundle.putInt("silent", 1);
                }
                if (rdoModeList.get(R.id.rdo_mode_green_led).isChecked()) {
                    bundle.putInt("greenLed", 1);
                }
            }
            int code = MyApplication.mPinPadOptV2.setPinPadMode(bundle);
            String msg = "Set PinPad mode " + (code == 0 ? "success" : "failed");
            LogUtil.e(TAG, msg);
            showToast(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Get current PinPad mode */
    private void getPinPadMode() {
        try {
            Bundle bundle = new Bundle();
            int code = MyApplication.mPinPadOptV2.getPinPadMode(bundle);
            if (code != 0) {
                LogUtil.e(TAG, "get PinPad mode failed.");
                return;
            }
            LogUtil.e(TAG, "get PinPad mode success.");
            int normal = bundle.getInt("normal");
            int longPressToClear = bundle.getInt("longPressToClear");
            int silent = bundle.getInt("silent");
            int greenLed = bundle.getInt("greenLed");
            LogUtil.e(TAG, Utility.formatStr("PinPad mode:\nnormal:%d\nlongPressToClear:%d\nsilent:%d\ngreenLed:%d",
                    normal, longPressToClear, silent, greenLed));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set PinPad text
     * <br/> Mostly the SDK built-in text is appropriate, client don't need to change it.
     * But if client want to customize the showing text, this method is helpful.
     * the set value just valid for next time inputting PIN, after input PIN finished,
     * the set value is lost effect
     */
    private void initPinPadText() {
        try {
            if (!checkInput(txtConfirm) || !checkInput(txtInputPin)
                    || !checkInput(txtInputOfflinePin) || !checkInput(txtReinputOfflinePinFormat)) {
                return;
            }
            String confirm = txtConfirm.getText().toString();
            String inputPin = txtInputPin.getText().toString();
            String inputOfflinePin = txtInputOfflinePin.getText().toString();
            String reInputOfflinePinFormat = txtReinputOfflinePinFormat.getText().toString();
            PinPadTextConfigV2 textConfigV2 = new PinPadTextConfigV2();
            textConfigV2.confirm = confirm;
            textConfigV2.inputPin = inputPin;
            textConfigV2.inputOfflinePin = inputOfflinePin;
            textConfigV2.reinputOfflinePinFormat = reInputOfflinePinFormat;
            MyApplication.mPinPadOptV2.setPinPadText(textConfigV2);
            showToast(R.string.success);
        } catch (RemoteException e) {
            e.printStackTrace();
            showToast(R.string.fail);
        }
    }

    private boolean checkInput(EditText edt) {
        String text = edt.getText().toString();
        if (TextUtils.isEmpty(text)) {
            showToast("PinPad text can't be empty!");
            edt.requestFocus();
            return false;
        }
        return true;
    }

    private void initPinPad() {
        try {
            PinPadConfigV2 pinPadConfig = initPinPadConfigV2();
            if (pinPadConfig != null) {
                MyApplication.mPinPadOptV2.initPinPad(pinPadConfig, mPinPadListener);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化自定义PINPAD
     */
    private void initCustomPinPad() {
        try {
            PinPadConfigV2 pinPadConfigV2 = initPinPadConfigV2();
            if (pinPadConfigV2 != null) {
                Intent mIntent = new Intent(this, CustomPinPadActivity.class);
                mIntent.putExtra("PinPadConfigV2", (Serializable) pinPadConfigV2);
                mIntent.putExtra("cardNo", cardNo);
                startActivityForResult(mIntent, 0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化PinPadConfigV2
     *
     * @return
     */
    private PinPadConfigV2 initPinPadConfigV2() {
        int keyIndex;
        try {
            String index = mEditKeyIndex.getText().toString();
            keyIndex = Integer.parseInt(index);
            if (keyIndex < 0 || keyIndex > 19) {
                showToast(R.string.pin_pad_key_index_hint);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.pin_pad_key_index_hint);
            return null;
        }

        int timeout;
        try {
            String time = mEditTimeout.getText().toString();
            timeout = Integer.parseInt(time) * 1000;
            if (timeout < 0 || timeout > 60000) {
                showToast(R.string.pin_pad_timeout_hint);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showToast(R.string.pin_pad_timeout_hint);
            return null;
        }

        cardNo = mEditCardNo.getText().toString();
        if (cardNo.trim().length() < 13 || cardNo.trim().length() > 19) {
            showToast(R.string.pin_pad_card_no_hint);
            return null;
        }

        try {
            PinPadConfigV2 pinPadConfig = new PinPadConfigV2();

            // 密码键盘类型 0：预置密码键盘(由服务实现样式统一的键盘)  1:调用方自己实现的密码键盘
            pinPadConfig.setPinPadType(mRGKeyboardStyle.getCheckedRadioButtonId() == R.id.rb_preset_keyboard ? 0 : 1);

            // pin类型标识(0是联机pin，1是脱机pin)
            pinPadConfig.setPinType(mRGIsOnline.getCheckedRadioButtonId() == R.id.rb_online_pin ? 0 : 1);

            // true:顺序键盘 false:乱序键盘
            pinPadConfig.setOrderNumKey(mRGKeyboard.getCheckedRadioButtonId() == R.id.rb_orderly_keyboard);

            int pinAlgType = 0;//3DES
            if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_sm4) {
                pinAlgType = 1;//SM4
            } else if (mRGPinAlgorithmType.getCheckedRadioButtonId() == R.id.rb_pin_type_aes) {
                pinAlgType = 2;//AES
                pinPadConfig.setPinblockFormat(PinBlockFormat.SEC_PIN_BLK_ISO_FMT4);
            }
            pinPadConfig.setAlgorithmType(pinAlgType);

            pinPadConfig.setKeySystem(mRGPikKeySystem.getCheckedRadioButtonId() == R.id.rb_key_system1 ? 0 : 1);

            // ascii格式转换成的byte 例如 “123456”.getBytes("us ascii")
            byte[] panBytes = cardNo.substring(cardNo.length() - 13, cardNo.length() - 1).getBytes("US-ASCII");
            pinPadConfig.setPan(panBytes);

            // 超时时间/毫秒
            pinPadConfig.setTimeout(timeout);

            // PIK索引(pin密钥索引)
            pinPadConfig.setPinKeyIndex(keyIndex);

            // 最大输入位数(最多允许输入12位)
            pinPadConfig.setMaxInput(12);

            // 最小输入位数
            pinPadConfig.setMinInput(0);

            return pinPadConfig;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private PinPadListenerV2 mPinPadListener = new PinPadListenerV2.Stub() {

        @Override
        public void onPinLength(int i) {
            LogUtil.e(Constant.TAG, "onPinLength:" + i);
            mHandler.obtainMessage(HANDLER_PIN_LENGTH, i, 0).sendToTarget();
        }

        @Override
        public void onConfirm(int i, byte[] bytes) {
            if (bytes != null) {
                String hexStr = ByteUtil.bytes2HexStr(bytes);
                LogUtil.e(Constant.TAG, "onConfirm:" + hexStr);
                runOnUiThread(
                        () -> mTvInfo.setText(getString(R.string.security_calc_result) + hexStr)
                );
                mHandler.obtainMessage(HANDLER_CONFIRM, bytes).sendToTarget();
            } else {
                mHandler.obtainMessage(HANDLER_CONFIRM).sendToTarget();
            }
        }

        @Override
        public void onCancel() {
            LogUtil.e(Constant.TAG, "onCancel");
            mHandler.sendEmptyMessage(HANDLER_WHAT_CANCEL);
        }

        @Override
        public void onError(int code) {
            LogUtil.e(Constant.TAG, "onError:" + code);
            String msg = AidlErrorCodeV2.valueOf(code).getMsg();
            mHandler.obtainMessage(HANDLER_ERROR, code, code, msg).sendToTarget();
        }

    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String pinCipher = data.getStringExtra("pinCipher");
            if (!TextUtils.isEmpty(pinCipher)) {
                mTvInfo.setText(getString(R.string.security_calc_result) + pinCipher);
            }
        }
    }
}
