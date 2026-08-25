package com.dexterauth.sdk.security;

import com.dexterauth.sdk.utils.Logger;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.UUID;

/**
 * HWID extraction engine supporting both Android devices and desktop JVM environments.
 */
public class HwidManager {

    private static String cachedHwid = null;

    public static String getHwid(Object contextOrNull) {
        if (cachedHwid != null && !cachedHwid.isEmpty()) {
            return cachedHwid;
        }

        String rawId = null;

        // Try Android Context extraction
        if (contextOrNull != null) {
            try {
                rawId = getAndroidHwid(contextOrNull);
            } catch (Throwable t) {
                Logger.debug("Android HWID extraction error: " + t.getMessage());
            }
        }

        // Try standalone Android extraction without explicit context
        if (rawId == null || rawId.isEmpty() || rawId.equalsIgnoreCase("9774d56d682e549c")) {
            try {
                rawId = getAndroidSystemHwid();
            } catch (Throwable ignored) {}
        }

        // Fallback for standard JVM / Desktop
        if (rawId == null || rawId.isEmpty()) {
            try {
                rawId = getDesktopJvmHwid();
            } catch (Throwable ignored) {}
        }

        if (rawId == null || rawId.isEmpty()) {
            rawId = UUID.randomUUID().toString();
        }

        cachedHwid = SecurityUtils.sha256(rawId);
        return cachedHwid;
    }

    private static String getAndroidHwid(Object context) {
        try {
            Class<?> contextClass = context.getClass();
            Method getContentResolver = contextClass.getMethod("getContentResolver");
            Object contentResolver = getContentResolver.invoke(context);

            Class<?> settingsSecure = Class.forName("android.provider.Settings$Secure");
            Method getString = settingsSecure.getMethod("getString", Class.forName("android.content.ContentResolver"), String.class);
            String androidId = (String) getString.invoke(null, contentResolver, "android_id");

            Method getPackageName = contextClass.getMethod("getPackageName");
            String packageName = (String) getPackageName.invoke(context);

            String buildInfo = getAndroidBuildFingerprint();
            return (androidId != null ? androidId : "") + ":" + buildInfo + ":" + (packageName != null ? packageName : "");
        } catch (Throwable t) {
            return null;
        }
    }

    private static String getAndroidSystemHwid() {
        try {
            Class<?> buildClass = Class.forName("android.os.Build");
            String hardware = (String) buildClass.getField("HARDWARE").get(null);
            String board = (String) buildClass.getField("BOARD").get(null);
            String model = (String) buildClass.getField("MODEL").get(null);
            String brand = (String) buildClass.getField("BRAND").get(null);
            return brand + "_" + model + "_" + hardware + "_" + board;
        } catch (Throwable t) {
            return null;
        }
    }

    private static String getAndroidBuildFingerprint() {
        try {
            Class<?> buildClass = Class.forName("android.os.Build");
            String fingerprint = (String) buildClass.getField("FINGERPRINT").get(null);
            if (fingerprint != null && !fingerprint.isEmpty()) {
                return fingerprint;
            }
        } catch (Throwable ignored) {}
        return "android_generic";
    }

    private static String getDesktopJvmHwid() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("os.name", "")).append(";");
        sb.append(System.getProperty("os.arch", "")).append(";");
        sb.append(System.getProperty("user.name", "")).append(";");

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length > 0) {
                    for (byte b : mac) {
                        sb.append(String.format("%02X", b));
                    }
                    break;
                }
            }
        } catch (Throwable ignored) {}

        return sb.toString();
    }
}
