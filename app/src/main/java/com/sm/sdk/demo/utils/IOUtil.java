package com.sm.sdk.demo.utils;


import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class IOUtil {
    private IOUtil() {
        throw new AssertionError("create IOUtil instance is forbidden");
    }

    /**
     * 关闭IO对象
     *
     * @param src 源IO对象
     */
    public static void close(Closeable src) {
        if (src != null) {
            try {
                src.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将异常对象转换成字符串
     *
     * @param e 异常对象
     * @return 转换后字符串
     */
    public static String exception2String(Exception e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /** 休眠指定的时间 */
    public static void delay(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
