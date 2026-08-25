package com.dexterauth.sdk.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Thread-safe logger for the DexterAuth SDK with Android Logcat and standard I/O support.
 */
public class Logger {

    public enum LogLevel {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3);

        private final int value;

        LogLevel(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final Logger INSTANCE = new Logger();
    private final Object lock = new Object();
    private String logFilePath = "logs/dexterauth_sdk.log";
    private boolean consolePrint = true;
    private LogLevel currentLevel = LogLevel.INFO;
    private boolean isAndroid = false;

    private Logger() {
        try {
            Class.forName("android.util.Log");
            isAndroid = true;
        } catch (ClassNotFoundException e) {
            isAndroid = false;
        }
    }

    public static Logger getInstance() {
        return INSTANCE;
    }

    public void init(String logFilePath, boolean consolePrint, LogLevel level) {
        synchronized (lock) {
            if (logFilePath != null && !logFilePath.isEmpty()) {
                this.logFilePath = logFilePath;
                try {
                    File file = new File(logFilePath);
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                } catch (Exception ignored) {}
            }
            this.consolePrint = consolePrint;
            if (level != null) {
                this.currentLevel = level;
            }
        }
    }

    public void setLevel(LogLevel level) {
        synchronized (lock) {
            if (level != null) {
                this.currentLevel = level;
            }
        }
    }

    public void setConsolePrint(boolean consolePrint) {
        synchronized (lock) {
            this.consolePrint = consolePrint;
        }
    }

    public void log(LogLevel level, String message) {
        if (level == null || level.getValue() < currentLevel.getValue()) {
            return;
        }

        synchronized (lock) {
            String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
            String levelStr = level.name();
            String formattedMessage = "[" + timeStr + "] [" + levelStr + "] [DexterAuth] " + message;

            if (consolePrint) {
                if (isAndroid) {
                    try {
                        switch (level) {
                            case DEBUG:
                                android.util.Log.d("DexterAuth", message);
                                break;
                            case INFO:
                                android.util.Log.i("DexterAuth", message);
                                break;
                            case WARN:
                                android.util.Log.w("DexterAuth", message);
                                break;
                            case ERROR:
                                android.util.Log.e("DexterAuth", message);
                                break;
                        }
                    } catch (Throwable ignored) {
                        System.out.println(formattedMessage);
                    }
                } else {
                    if (level == LogLevel.ERROR) {
                        System.err.println(formattedMessage);
                    } else {
                        System.out.println(formattedMessage);
                    }
                }
            }

            try {
                if (logFilePath != null && !logFilePath.isEmpty()) {
                    File file = new File(logFilePath);
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
                        out.println(formattedMessage);
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    public static void debug(String message) {
        getInstance().log(LogLevel.DEBUG, message);
    }

    public static void info(String message) {
        getInstance().log(LogLevel.INFO, message);
    }

    public static void warn(String message) {
        getInstance().log(LogLevel.WARN, message);
    }

    public static void error(String message) {
        getInstance().log(LogLevel.ERROR, message);
    }
}
