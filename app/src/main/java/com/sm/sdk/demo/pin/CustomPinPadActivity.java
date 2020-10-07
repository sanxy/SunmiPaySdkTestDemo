package com.sm.sdk.demo.pin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sm.sdk.demo.view.FixPasswordKeyboard;
import com.sm.sdk.demo.view.PasswordEditText;
import com.sm.sdk.demo.view.TitleView;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadConfigV2;
import com.sunmi.pay.hardware.aidlv2.bean.PinPadDataV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadListenerV2;
import com.sunmi.pay.hardware.aidlv2.pinpad.PinPadOptV2;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import static com.sm.sdk.demo.MyApplication.mEMVOptV2;

public class CustomPinPadActivity extends BaseAppCompatActivity {

    private int mWidth = 239;                       // 密码键盘单个item宽度
    private int mHeight = 130;                      // 密码键盘单个item高度
    private int mInterval = 1;                      // 线间隔
    private int[] mKeyboardCoordinate = {0, 661};   // 密码键盘第一个button左顶点位置（绝对位置）
    private int mCancelWidth = 112;                 // 取消键宽度
    private int mCancelHeight = 112;                // 取消键高度
    private int[] mCancelCoordinate = {0, 48};      // 取消键左顶点位置（绝对位置）

    private ImageView mBackView;
    private TextView tv_money;
    private TextView tv_card_num;
    private PasswordEditText mPasswordEditText;
    private FixPasswordKeyboard mFixPasswordKeyboard;

    private PinPadOptV2 mPinPadOptV2;

    public long amount = 1;
    public String cardNo = "";
    public String pinCipher = "";
    public PinPadConfigV2 customPinPadConfigV2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_pad_custom);
        initView();
    }

    private void initView() {
        Intent mIntent = getIntent();
        customPinPadConfigV2 = (PinPadConfigV2) mIntent.getSerializableExtra("PinPadConfigV2");
        cardNo = mIntent.getStringExtra("cardNo");

        mPinPadOptV2 = MyApplication.mPinPadOptV2;

        TitleView titleView = findViewById(R.id.title_view);
        TextView mTvTitle = titleView.getCenterTextView();
        mTvTitle.setText(getString(R.string.pin_pad_custom_keyboard));
        mBackView = titleView.getLeftImageView();
        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tv_money = findViewById(R.id.tv_money);
        tv_money.setText(longCent2DoubleMoneyStr(amount));

        tv_card_num = findViewById(R.id.tv_card_num);
        tv_card_num.setText(cardNo);

        mPasswordEditText = findViewById(R.id.passwordEditText);
        mFixPasswordKeyboard = findViewById(R.id.fixPasswordKeyboard);

        mHandler.sendEmptyMessage(EMV_SHOW_PIN_PAD);
    }

    public void initToolbarBringBack(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_back_white);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(
                v -> onBackPressed()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        screenFinancialModel();
    }

    @Override
    protected void onDestroy() {
        screenMonopoly(-1);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        importPinInputStatus(1);
        finish();
    }


    private static final int PIN_CLICK_CANCEL = 1;
    private static final int PIN_CLICK_NUMBER = 2;
    private static final int PIN_CLICK_CONFIRM = 3;
    private static final int PIN_ERROR = 4;
    private static final int EMV_SHOW_PIN_PAD = 10;

    private Looper mLooper = Looper.getMainLooper();
    private final Handler mHandler = new Handler(mLooper) {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PIN_CLICK_NUMBER:
                    showPasswordView(msg.arg1);
                    break;
                case PIN_CLICK_CONFIRM:

                    if (pinCipher != null && pinCipher.length() > 0) {
                        importPinInputStatus(0);
                    } else {
                        importPinInputStatus(2);
                    }

                    showToast("CONFIRM");
                    Intent mIntent = getIntent();
                    mIntent.putExtra("pinCipher", pinCipher);
                    setResult(0, mIntent);
                    finish();

                    break;
                case PIN_CLICK_CANCEL:
                    showToast("CANCEL");
                    importPinInputStatus(1);
                    finish();
                    break;
                case PIN_ERROR:
                    importPinInputStatus(3);
                    showToast("ERROR");
                    finish();
                    break;
                case EMV_SHOW_PIN_PAD:
                    initPinPad();
                    break;
            }
        }

    };

    private void initPinPad() {
        try {
            PinPadConfigV2 config = new PinPadConfigV2();
            config.setMaxInput(12);
            config.setMinInput(4);
            config.setPinPadType(1);
            config.setAlgorithmType(customPinPadConfigV2.getAlgorithmType());
            config.setPinType(customPinPadConfigV2.getPinType());
            config.setTimeout(customPinPadConfigV2.getTimeout());
            config.setOrderNumKey(customPinPadConfigV2.isOrderNumKey());
            config.setPinblockFormat(customPinPadConfigV2.getPinblockFormat());
            config.setKeySystem(customPinPadConfigV2.getKeySystem());
            config.setPinKeyIndex(customPinPadConfigV2.getPinKeyIndex());

            int length = cardNo.length();
            byte[] panBlock = cardNo.substring(length - 13, length - 1).getBytes("US-ASCII");
            config.setPan(panBlock);

            String result = mPinPadOptV2.initPinPad(config, mPinPadListener);
            getKeyboardCoordinate(result);

            mPasswordEditText.clearText();
            mFixPasswordKeyboard.setKeepScreenOn(true);
            mFixPasswordKeyboard.setKeyBoard(result);

            mPasswordEditText.setVisibility(View.VISIBLE);
            mFixPasswordKeyboard.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getKeyboardCoordinate(final String keyBoardText) {
        mFixPasswordKeyboard.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        mFixPasswordKeyboard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        TextView textView = mFixPasswordKeyboard.getKey_0();
                        textView.getLocationOnScreen(mKeyboardCoordinate);
                        mWidth = textView.getWidth();
                        mHeight = textView.getHeight();
                        mInterval = 1;
                        mBackView.getLocationOnScreen(mCancelCoordinate);
                        mCancelWidth = mBackView.getWidth();
                        mCancelHeight = mBackView.getHeight();
                        importPinPadData(keyBoardText);
                    }

                }
        );
    }

    private void importPinPadData(String text) {
        PinPadDataV2 pinPadData = new PinPadDataV2();
        pinPadData.numX = mKeyboardCoordinate[0];
        pinPadData.numY = mKeyboardCoordinate[1];
        pinPadData.numW = mWidth;
        pinPadData.numH = mHeight;
        pinPadData.lineW = mInterval;
        pinPadData.cancelX = mCancelCoordinate[0];
        pinPadData.cancelY = mCancelCoordinate[1];
        pinPadData.cancelW = mCancelWidth;
        pinPadData.cancelH = mCancelHeight;
        pinPadData.lineW = 0;
        pinPadData.rows = 5;
        pinPadData.clos = 3;
        keyMap(text, pinPadData);
        try {
            mPinPadOptV2.importPinPadData(pinPadData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PinPadListenerV2 mPinPadListener = new PinPadListenerV2.Stub() {

        @Override
        public void onPinLength(int len) throws RemoteException {
            LogUtil.e(Constant.TAG, "onPinLength len:" + len);
            mHandler.obtainMessage(PIN_CLICK_NUMBER, len, 0).sendToTarget();
        }

        @Override
        public void onConfirm(int status, byte[] pinBlock) throws RemoteException {
            LogUtil.e(Constant.TAG, "onConfirm status:" + status);
            if (pinBlock != null) {
                String hexStr = ByteUtil.bytes2HexStr(pinBlock);
                LogUtil.e(Constant.TAG, "hexStr:" + hexStr);
                boolean equals = TextUtils.equals("00", hexStr);
                if (equals) {
                    pinCipher = "";
                } else {
                    pinCipher = hexStr;
                }
            }

            mHandler.sendEmptyMessage(PIN_CLICK_CONFIRM);

        }

        @Override
        public void onCancel() throws RemoteException {
            LogUtil.e(Constant.TAG, "onCancel");
            mHandler.sendEmptyMessage(PIN_CLICK_CANCEL);
        }

        @Override
        public void onError(int code) throws RemoteException {
            LogUtil.e(Constant.TAG, "onError code:" + code);
            mHandler.obtainMessage(PIN_ERROR, code, 0).sendToTarget();
        }

    };

    private void showPasswordView(int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append("*");
        }
        mPasswordEditText.setText(sb);
    }

    /**
     * pinTyp PIN类型：0 联机PIN 1 脱机PIN
     * inputResult PIN输入结果 0:处理成功 1:PIN取消 2:PIN跳过 3:PIN故障
     */
    private void importPinInputStatus(int inputResult) {
        LogUtil.e(Constant.TAG, "importPinInputStatus:" + inputResult);
        try {
            mEMVOptV2.importPinInputStatus(customPinPadConfigV2.getPinType(), inputResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void keyMap(String str, PinPadDataV2 data) {
        data.keyMap = new byte[64];
        for (int i = 0, j = 0; i < 15; i++, j++) {
            if (i == 9) {
                data.keyMap[i] = 0x00;//no key
                j--;
            } else if (i == 11) {
                data.keyMap[i] = 0x00;//no key
                j--;
            } else if (i == 12) {
                data.keyMap[i] = 0x1B;//cancel
                j--;
            } else if (i == 13) {
                data.keyMap[i] = 0x0C;//clear
                j--;
            } else if (i == 14) {
                data.keyMap[i] = 0x0D;//confirm
                j--;
            } else {
                data.keyMap[i] = (byte) str.charAt(j);
            }
        }
    }

    /**
     * 将Long类型的钱（单位：分）转化成String类型的钱（单位：元）
     */
    public static String longCent2DoubleMoneyStr(long amount) {
        BigDecimal bd = new BigDecimal(amount);
        double doubleValue = bd.divide(new BigDecimal("100")).doubleValue();
        DecimalFormat df = new DecimalFormat("#0.00");
        return df.format(doubleValue);
    }

    /**
     * 屏幕独占金融模式
     */
    public static void screenFinancialModel() {
        int uid = MyApplication.getContext().getApplicationInfo().uid;
        try {
            MyApplication.mBasicOptV2.setScreenMode(uid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 屏幕独占
     */
    public static void screenMonopoly(int mode) {
        try {
            MyApplication.mBasicOptV2.setScreenMode(mode);
        } catch (Exception e) {
        }
    }
}
