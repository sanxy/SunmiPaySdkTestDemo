package com.sm.sdk.demo.etc;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.emv.TLV;
import com.sm.sdk.demo.emv.TLVUtil;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlErrorCodeV2;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class I2CDataExchangeActivity extends BaseAppCompatActivity {
    private static final String TAG = Constant.TAG;

    private EditText edtI2CAddr;
    private EditText edtSendData;
    private EditText edtExpRecvLen;
    private EditText edtTimeout;
    private TextView result;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etc_i2c_layout);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.etc_i2c_data_exchange);
        edtI2CAddr = findViewById(R.id.i2c_address);
        edtSendData = findViewById(R.id.send_data);
        edtExpRecvLen = findViewById(R.id.expect_recv_len);
        edtTimeout = findViewById(R.id.timeout);
        findViewById(R.id.mb_ok).setOnClickListener(this);
        result = findViewById(R.id.result);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mb_ok:
                updateResultText(null);
                if (checkInputData()) {
                    i2cDataExchange();
                }
                break;
        }
    }

    /** 更新显示值 */
    private void updateResultText(CharSequence value) {
        runOnUiThread(() -> result.setText(value));
    }

    private boolean checkInputData() {
        if (TextUtils.isEmpty(edtI2CAddr.getText())) {
            edtI2CAddr.requestFocus();
            showToast("I2C address shouldn't be empty!");
            return false;
        }
        if (!Pattern.matches("[0-9a-fA-F]+", edtI2CAddr.getText())) {
            edtI2CAddr.requestFocus();
            showToast("I2C address should be HEX value!");
            return false;
        }
        if (!TextUtils.isEmpty(edtSendData.getText()) && !Pattern.matches("[0-9a-fA-F]+", edtSendData.getText())) {
            edtSendData.requestFocus();
            showToast("Send data should be HEX value!");
            return false;
        }

        if (!Pattern.matches("[0-9a-fA-F]+", edtExpRecvLen.getText())) {
            edtExpRecvLen.requestFocus();
            showToast("Expect receive data length should be HEX value!");
            return false;
        }
        int expOutLen = Integer.parseInt(edtExpRecvLen.getText().toString(), 16);
        if (expOutLen < 0) {
            edtExpRecvLen.requestFocus();
            showToast("Expect receive data length should >=0!");
            return false;
        }

        if (TextUtils.isEmpty(edtTimeout.getText())) {
            showToast("Timeout time shouldn't be empty");
            return false;
        }
        if (!Pattern.matches("[0-9a-fA-F]+", edtTimeout.getText())) {
            edtTimeout.requestFocus();
            showToast("Timeout time should be HEX value!");
            return false;
        }
        int time = Integer.parseInt(edtTimeout.getText().toString(), 16);
        if (time < 0) {
            edtTimeout.requestFocus();
            showToast("Timeout time should >=0!");
            return false;
        }
        return true;
    }

    /**
     * 透传数据到ETC模块
     */
    private void i2cDataExchange() {
        try {
            //I2C模块返回格式: LL 10 04 mm 92 nn oo 2120010402D111010201020305000011B4A10401000508D4C1413132333435800101 2120010402D111010201020305000011B4A10401000508D4C1413132333435800101 2120010402D111010201020305000011B4A10401000508D4C1413132333435800101 yy
            //LL 数据长度+校验字节长度，这里是 6D
            //mm 包序号 ，为请求包序号
            //nn BIT0 是否有后续包，1--有 0--没有，其他位，保留
            //oo 终端执行状态，金逸提出的要求
            //yy 校验字符
            int addr = Integer.parseInt(edtI2CAddr.getText().toString(), 16);
            byte[] sendData = {};
            if (!TextUtils.isEmpty(edtSendData.getText())) {
                sendData = ByteUtil.hexStr2Bytes(edtSendData.getText().toString());
            }
            int expOutLen = Integer.parseInt(edtExpRecvLen.getText().toString(), 16);
            int timeout = Integer.parseInt(edtTimeout.getText().toString(), 16);
            byte[] recvBuff = new byte[1024];
            int len = MyApplication.mETCOptV2.i2cDataExchange(addr, sendData, expOutLen, timeout, recvBuff);
            if (len < 0) {
                String msg = "I2C data exchange failed,code:" + len + ",msg:" + AidlErrorCodeV2.valueOf(len).getMsg();
                LogUtil.e(TAG, msg);
                showToast(msg);
                return;
            }
            SpannableStringBuilder sb = new SpannableStringBuilder();
            sb.append("I2C Recv:");
            sb.append(ByteUtil.bytes2HexStr(Arrays.copyOf(recvBuff, len)));
            sb.append("\n");
            LogUtil.e(TAG, "I2C receive data:" + ByteUtil.bytes2HexStr(recvBuff, 0, len));
            //出参数据格式 LEN(1B,值为len)+bccData(len B,其中前(len-1)B为TLV数据，第len B为LRC)
            int tlvDataLen = ByteUtil.unsignedByte2Int(recvBuff, 0);
            if (len < tlvDataLen + 1) {
                String msg = "I2C receive data length error.";
                sb.append(msg);
                LogUtil.e(TAG, msg);
                updateResultText(sb);
                return;
            }
            byte[] tlvData = Arrays.copyOfRange(recvBuff, 1, tlvDataLen);//去掉末尾的LRC
            List<TLV> tlvList = TLVUtil.buildTLVList(tlvData);
            for (int i = 0, size = tlvList.size(); i < size; i++) {
                TLV tlv = tlvList.get(i);
                if ("10".equals(tlv.getTag())) {
                    byte[] value = ByteUtil.hexStr2Bytes(tlv.getValue());
                    len = value.length;
                    SpannableStringBuilder obuHeader = new SpannableStringBuilder();
                    obuHeader.append("\n是否有后续包：");
                    obuHeader.append(value[len - 2] > 0 ? "是" : "否");
                    obuHeader.append("\n终端执行状态：");
                    obuHeader.append(String.valueOf(value[len - 1] & 0xff));
                    StyleSpan span = new StyleSpan(Typeface.BOLD);
                    obuHeader.setSpan(span, 0, obuHeader.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.append(obuHeader);
                }
                if ("21".equals(tlv.getTag()) && !TextUtils.isEmpty(tlv.getValue())) {//一个OBU数据
                    Map<String, TLV> tlvMap = TLVUtil.buildTLVMap(tlv.getValue());//提取TLV数据
                    sb.append(getDeviceNo(tlvMap));
                    sb.append(getDeviceStatus(tlvMap));
                    sb.append(getAmount(tlvMap));
                    sb.append(getPlateColor(tlvMap));
                    sb.append(getPlateNo(tlvMap));
                    sb.append(getSignal(tlvMap));
                }
            }
            updateResultText(sb);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取设备编号 */
    private String getDeviceNo(Map<String, TLV> tlvMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(getString(R.string.etc_i2c_device_No));
        TLV tlv = tlvMap.get("01");//设备编号
        if (tlv != null && !TextUtils.isEmpty(tlv.getValue())) {
            sb.append(tlv.getValue());
        }
        return sb.toString();
    }

    /** 获取设备状态 */
    private String getDeviceStatus(Map<String, TLV> tlvMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(getString(R.string.etc_i2c_device_status));
        TLV tlv = tlvMap.get("02");//设备状态
        if (tlv != null && !TextUtils.isEmpty(tlv.getValue())) {
            //bit7: 0-卡片存在；1-卡片不存在
            //bit1：0-设备正常；1-设备失效
            //bit0：0-电量正常；1-设备低电
            int status = Integer.parseInt(tlv.getValue(), 16);
            sb.append((status & 0x01) != 0 ? getString(R.string.etc_i2c_dev_status_1) : getString(R.string.etc_i2c_dev_status_2));
            sb.append(",");
            sb.append((status & 0x02) != 0 ? getString(R.string.etc_i2c_dev_status_3) : getString(R.string.etc_i2c_dev_status_4));
            sb.append(",");
            sb.append((status & 0x80) != 0 ? getString(R.string.etc_i2c_dev_status_5) : getString(R.string.etc_i2c_dev_status_6));
        }
        return sb.toString();
    }

    /** 获取金额 */
    private String getAmount(Map<String, TLV> tlvMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(getString(R.string.etc_i2c_amount));
        TLV tlv = tlvMap.get("03");//金额
        if (tlv != null && !TextUtils.isEmpty(tlv.getValue())) {
            byte[] value = ByteUtil.hexStr2Bytes(tlv.getValue());
            //xxH
            //00：储值卡
            //01：记账卡
            //02：非法卡片
            if (value[0] == 0x00) { //00：储值卡
                sb.append(getString(R.string.etc_i2c_card_type_1));
                sb.append(",");
                //储值卡显示金额，单位：分
                int amount = ByteUtil.unsignedInt2IntBE(value, 1);
                sb.append(amount);
                sb.append(getString(R.string.etc_i2c_amount_unit));
            } else if (value[0] == 0x01) { //01：记账卡
                sb.append(getString(R.string.etc_i2c_card_type_2));
            } else if (value[0] == 0x02) {   //02：非法卡片
                sb.append(getString(R.string.etc_i2c_card_type_3));
            }
        }
        return sb.toString();
    }

    /** 获取车牌颜色 */
    private String getPlateColor(Map<String, TLV> tlvMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(getString(R.string.etc_i2c_plate_color));
        TLV tlv = tlvMap.get("04");//车牌颜色
        if (tlv != null && !TextUtils.isEmpty(tlv.getValue())) {
            int value = Integer.parseInt(tlv.getValue(), 16);
            //车牌颜色：00-蓝色，01-黄色，02-黑色，03-白色，04-渐变绿色，05-黄绿双拼色，06-蓝白渐变色，其他值查看新规定
            switch (value) {
                case 0x00:
                    sb.append(getString(R.string.etc_i2c_plate_color_1));
                    break;
                case 0x01:
                    sb.append(getString(R.string.etc_i2c_plate_color_2));
                    break;
                case 0x02:
                    sb.append(getString(R.string.etc_i2c_plate_color_3));
                    break;
                case 0x03:
                    sb.append(getString(R.string.etc_i2c_plate_color_4));
                    break;
                case 0x04:
                    sb.append(getString(R.string.etc_i2c_plate_color_5));
                    break;
                case 0x05:
                    sb.append(getString(R.string.etc_i2c_plate_color_6));
                    break;
                case 0x06:
                    sb.append(getString(R.string.etc_i2c_plate_color_7));
                    break;
                default:
                    sb.append("--");
                    break;
            }
        }
        return sb.toString();
    }

    /** 获取车牌号码 */
    private String getPlateNo(Map<String, TLV> tlvMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(getString(R.string.etc_i2c_plate_No));
        TLV tlv = tlvMap.get("05");//车牌号码
        if (tlv != null && !TextUtils.isEmpty(tlv.getValue())) {
            byte[] data = ByteUtil.hexStr2Bytes(tlv.getValue());
            sb.append(new String(data, Charset.forName("GBK")));
        }
        return sb.toString();
    }

    /** 获取信号强度 */
    private String getSignal(Map<String, TLV> tlvMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append(getString(R.string.etc_i2c_signal_level));
        TLV tlv = tlvMap.get("40");//信号强度
        if (tlv != null && !TextUtils.isEmpty(tlv.getValue())) {
            sb.append(Integer.parseInt(tlv.getValue()));
        }
        return sb.toString();
    }
}
