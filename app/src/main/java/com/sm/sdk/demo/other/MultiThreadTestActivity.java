package com.sm.sdk.demo.other;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.Constant;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidl.AidlConstants.EMV.TLVOpCode;
import com.sunmi.pay.hardware.aidlv2.emv.EMVOptV2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * This is a test page, please do not refer to any codes.
 * This page test multi thread call interfaces of SDKService.
 */
public class MultiThreadTestActivity extends BaseAppCompatActivity {

    private static final int TASK_COUNT = 10;
    private List<Button> buttons = new ArrayList<>();
    private final Thread[] threads = new Thread[TASK_COUNT];
    private CountDownLatch startLatch;
    private CountDownLatch stopLatch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_thread_test);
        initView();
    }

    private void initView() {
        initToolbarBringBack(R.string.card_test_MIFARE_Ultralight_ev1);

        Button btn = findViewById(R.id.init_task);
        btn.setOnClickListener(
                v -> initTask()
        );
        buttons.add(btn);
        btn = findViewById(R.id.start);
        btn.setOnClickListener(
                v -> startTest()
        );
        buttons.add(btn);
        btn = findViewById(R.id.stop);
        btn.setOnClickListener(
                v -> stopTest()
        );
        buttons.add(btn);

        enableButton(R.id.init_task);
    }

    /**
     * 初始化任务
     */
    private void initTask() {
        enableButton(R.id.start);
        startLatch = new CountDownLatch(1);
        stopLatch = new CountDownLatch(TASK_COUNT);
        for (int i = 0; i < TASK_COUNT / 2; i++) {
            threads[i] = new Thread(new ReadTlvTask(startLatch, stopLatch));
            threads[i].start();
        }
        for (int i = TASK_COUNT / 2; i < TASK_COUNT; i++) {
            threads[i] = new Thread(new WriteTlvTask(startLatch, stopLatch));
            threads[i].start();
        }
    }

    /**
     * 开始测试
     */
    private void startTest() {
        enableButton(R.id.stop);
        startLatch.countDown();
    }

    /**
     * 停止测试
     */
    private void stopTest() {
        try {
            enableButton(R.id.init_task);
            for (Thread thread : threads) {
                thread.interrupt();
            }
            stopLatch.await();
            LogUtil.e(Constant.TAG, "All threads quit, test finished");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换button使能状态
     */
    private void enableButton(int id) {
        for (Button btn : buttons) {
            btn.setEnabled(btn.getId() == id);
        }
    }

    /**
     * 读TLV数据任务
     */
    private static class ReadTlvTask implements Runnable {

        private static int count = 0;

        private final int id = count++;
        private CountDownLatch startLatch;
        private CountDownLatch stopLatch;
        private EMVOptV2 emvOptV2 = MyApplication.mEMVOptV2;

        private ReadTlvTask(CountDownLatch start, CountDownLatch stop) {
            startLatch = start;
            stopLatch = stop;
        }

        @Override
        public void run() {
            try {
                LogUtil.e(Constant.TAG, "ReadTlvTask" + id + " start");
                startLatch.await();
                String[] tag = {"5F2A", "5F36", "9F3C"};
                byte[] out = new byte[1024];
                int len = -1;
                while (!Thread.interrupted()) {
                    len = emvOptV2.getTlvList(TLVOpCode.OP_NORMAL, tag, out);
                    if (len < 0) {
                        LogUtil.e(Constant.TAG, "ReadTlvTask" + id + " error:" + len);
                        break;
                    }
                    String hex = ByteUtil.bytes2HexStr(Arrays.copyOf(out, len));
                    LogUtil.e(Constant.TAG, "ReadTlvTask" + id + " hex:" + hex);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(Constant.TAG, "quit ReadTlvTask" + id);
            } finally {
                stopLatch.countDown();
            }
        }
    }

    /**
     * 写TLV数据任务
     */
    private static class WriteTlvTask implements Runnable {
        private static int count = 0;

        private final int id = count++;
        private CountDownLatch startLatch;
        private CountDownLatch stopLatch;
        private EMVOptV2 emvOptV2 = MyApplication.mEMVOptV2;

        private WriteTlvTask(CountDownLatch start, CountDownLatch stop) {
            startLatch = start;
            stopLatch = stop;
        }

        @Override
        public void run() {
            try {
                LogUtil.e(Constant.TAG, "WriteTlvTask" + id + " start");
                startLatch.await();
                String[] tags = {"5F2A", "5F36", "9F3C"};
                String[] values = {"0643", "00", "0643"};
                while (!Thread.interrupted()) {
                    emvOptV2.setTlvList(TLVOpCode.OP_NORMAL, tags, values);
                    LogUtil.e(Constant.TAG, "WriteTlvTask" + id + " round completed.");
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(Constant.TAG, "quit WriteTlvTask" + id);
            } finally {
                stopLatch.countDown();
            }
        }
    }

}
