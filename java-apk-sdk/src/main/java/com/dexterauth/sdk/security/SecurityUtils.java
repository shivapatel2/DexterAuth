package com.dexterauth.sdk.security;

import com.dexterauth.sdk.utils.Logger;
import com.dexterauth.sdk.utils.SimpleJson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Cryptographic and anti-tamper security routines for DexterAuth.
 */
public class SecurityUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    /**
     * Compute HMAC-SHA256 signature in hexadecimal format.
     */
    public static String hmacSha256(String key, String message) {
        if (key == null || message == null) return "";
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] bytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            Logger.error("Failed to compute HMAC-SHA256: " + e.getMessage());
            return "";
        }
    }

    /**
     * Compute SHA-256 hash of a string.
     */
    public static String sha256(String input) {
        if (input == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Generate a cryptographically secure random hexadecimal nonce.
     */
    public static String generateNonce(int length) {
        byte[] bytes = new byte[length / 2];
        SECURE_RANDOM.nextBytes(bytes);
        return bytesToHex(bytes);
    }

    /**
     * Constant-time string comparison to prevent timing side-channel attacks.
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * Signed Response Protocol (SRP) Verification.
     */
    public static boolean verifyResponseSignature(
            String responseBody,
            String requestNonce,
            String sigHeader,
            String nonceHeader,
            String clientSecret
    ) {
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            return true;
        }

        if (sigHeader == null || sigHeader.isEmpty() || nonceHeader == null || nonceHeader.isEmpty()) {
            Logger.warn("[SRP] Signature headers missing on secret-enabled app. Proceeding with caution.");
            return true;
        }

        if (!nonceHeader.equalsIgnoreCase(requestNonce)) {
            Logger.error("[SRP] Replay Attack Detected: Nonce mismatch! Expected " + requestNonce + ", Got " + nonceHeader);
            return false;
        }

        String canonicalBody = responseBody;
        try {
            JSONObject json = SimpleJson.parseObject(responseBody);
            canonicalBody = SimpleJson.toCanonicalString(json);
        } catch (Exception ignored) {}

        String payloadToVerify = canonicalBody + ":" + requestNonce;
        String expectedSig = hmacSha256(clientSecret, payloadToVerify);

        if (expectedSig.isEmpty()) {
            return false;
        }

        boolean isValid = constantTimeEquals(expectedSig.toLowerCase(), sigHeader.toLowerCase());
        if (!isValid) {
            Logger.error("[SRP] Response Signature Mismatch! Response may have been tampered with.");
        }
        return isValid;
    }

    /**
     * Compute SHA-256 checksum of an APK or local binary file.
     */
    public static String getFileChecksum(String filePath) {
        if (filePath == null || filePath.isEmpty()) return "UNKNOWN_HASH";
        try {
            File file = new File(filePath);
            if (!file.exists()) return "UNKNOWN_HASH";

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] byteArray = new byte[8192];
                int bytesCount;
                while ((bytesCount = fis.read(byteArray)) != -1) {
                    digest.update(byteArray, 0, bytesCount);
                }
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            return "UNKNOWN_HASH";
        }
    }

    /**
     * Basic anti-debugging verification.
     */
    public static void antiDebug() {
        try {
            Class<?> debugClass = Class.forName("android.os.Debug");
            java.lang.reflect.Method isDebuggerConnected = debugClass.getMethod("isDebuggerConnected");
            boolean connected = (boolean) isDebuggerConnected.invoke(null);
            if (connected) {
                Logger.error("Security violation: Debugger detected.");
            }
        } catch (Exception ignored) {}
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
