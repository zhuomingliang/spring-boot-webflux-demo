package com.demo.utils;

/**
 * @author Jimmy
 * @description 用于关闭各种连接，缺啥补啥
 **/
public class CloseUtil extends ch.qos.logback.core.util.CloseUtil {
    public static void close(AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                assert true; // avoid an empty catch
            }
        }
    }
}
