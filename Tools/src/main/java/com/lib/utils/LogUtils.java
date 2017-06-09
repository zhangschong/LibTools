package com.lib.utils;


import android.util.Log;

/**
 * $desc
 */

public class LogUtils {
    private final static boolean IS_DEBUG = true;

    public static void e(String tag, Object... msgs) {
        Log.e(tag, createMsg(msgs));
    }

    public static void w(String tag, Object... msgs) {
        if (!IS_DEBUG) {
            return;
        }
        Log.w(tag, createMsg(msgs));
    }

    public static void d(boolean isDebug, String tag, Object... msgs) {
        if (!(isDebug && IS_DEBUG)) {
            return;
        }
        Log.d(tag, createMsg(msgs));
    }

    private static String createMsg(Object... msgs) {
        if (msgs.length > 1) {
            StringBuilder builder = new StringBuilder();
            for (Object msg : msgs) {
                builder.append(msg);
            }
            return builder.toString();
        } else {
            return msgs[0].toString();
        }
    }
}
