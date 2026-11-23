package com.celestered.earlyfixes;

/**
 * Plain stdout logging. The launch-tuning code runs inside Relauncher's own class loader
 * before the game's logging exists, so stdout (which ends up in the launcher log) is the
 * one channel that always works.
 */
final class Log {
    private static final String PREFIX = "[Celeste Early Fixes] ";

    private Log() {
    }

    static void info(String msg) {
        System.out.println(PREFIX + msg);
    }

    static void warn(String msg) {
        System.out.println(PREFIX + "WARN: " + msg);
    }
}
