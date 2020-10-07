package com.sm.sdk.demo.other;

import android.widget.EditText;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;

public class DeviceCommActivity extends BaseAppCompatActivity {

//    private DeviceCommOptV2 mDeviceCommOptV2;
    private int commType = 0;
    private TextView tvOutputLog;
    private EditText etData;
    private boolean isConnect = false;

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_device_comm);
//        initToolbarBringBack(R.string.device_comm);
////        mDeviceCommOptV2 = MyApplication.mDeviceCommOptV2;
//        isConnect = mDeviceCommOptV2 != null;
//        initView();
//    }
//
//    private void initView() {
//        etData = this.findViewById(R.id.et_send_data);
//        tvOutputLog = this.findViewById(R.id.tv_output_log);
//        tvOutputLog.setMovementMethod(ScrollingMovementMethod.getInstance());
//        RadioGroup radioGroup = findViewById(R.id.radio_group);
//        radioGroup.setOnCheckedChangeListener(
//                (group, checkedId) -> {
//                    LogUtil.e("lxy", "group:" + group);
//                    switch (checkedId) {
//                        case R.id.rb_usb:
//                            commType = 0;
//                            break;
//                        case R.id.rb_serial_port:
//                            commType = 1;
//                            break;
//                        case R.id.rb_wifi:
//                            commType = 2;
//                            break;
//                        case R.id.rb_bluetooth:
//                            commType = 3;
//                            break;
//                    }
//                }
//        );
//    }
//
//    private DeviceCommCallbackV2 deviceCommCallbackV2 = new DeviceCommCallbackV2.Stub() {
//        @Override
//        public void onCommOpen(int commType, int code, String msg) {
//            tvOutputLog.append("open: commType:" + commType + ", code:" + code + ", msg: " + msg + "\n");
//        }
//
//        @Override
//        public void onSendData(int commType, int code, String msg) {
//            tvOutputLog.append("SendData: commType:" + commType + ", code:" + code + ", msg: " + msg + "\n");
//
//        }
//
//        @Override
//        public void onReceiveData(int commType, int code, byte[] data) {
//            tvOutputLog.append("ReceiveData: commType:" + commType + ", code:" + code + ", data: " + new String(data) + "\n");
//
//        }
//
//        @Override
//        public void onCommClosed(int commType, int code, String msg) {
//            tvOutputLog.append("Close: commType:" + commType + ", code:" + code + ", msg: " + msg + "\n");
//        }
//    };
//
//    public void open(View view) {
//        if (!isConnect) return;
//        try {
//            mDeviceCommOptV2.openComm(commType, null, deviceCommCallbackV2);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void close(View view) {
//        if (!isConnect) return;
//        try {
//            mDeviceCommOptV2.closeComm(commType);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void check(View view) {
//        if (!isConnect) return;
//        try {
//            boolean commEnabled = mDeviceCommOptV2.isCommEnabled(commType);
//            tvOutputLog.append("Check:" + commEnabled + "\n");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void send(View view) {
//        if (!isConnect) return;
//        String data = etData.getText().toString();
//        if (TextUtils.isEmpty(data)) {
//            tvOutputLog.append("data is null!!!!\n");
//            return;
//        }
//        try {
//            mDeviceCommOptV2.sendData(commType, data.getBytes());
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//    }


}
