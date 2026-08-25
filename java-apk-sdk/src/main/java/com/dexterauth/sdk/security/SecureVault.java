package com.dexterauth.sdk.security;

import java.nio.charset.StandardCharsets;

/**
 * Secure In-Memory Credential Vault.
 * Protects sensitive application credentials (App Name, Owner ID, App Secret, Version)
 * from static reverse-engineering, decompilation tools (JADX, IDA, Ghidra, Apktool),
 * and binary string-dumps using dynamic polymorphic rolling XOR masking.
 */
public class SecureVault {

    // Obfuscated credential byte arrays (Zero plain text literals in bytecode)
    private static final byte[] ENC_NAME = new byte[] { 10, (byte) 236, (byte) 178, 18, (byte) 250, 65, 43 };
    private static final byte[] ENC_OWNER_ID = new byte[] { 31, (byte) 235, 3, (byte) 188, (byte) 210, 36, (byte) 249, (byte) 197, 99, (byte) 201, 50, 101, (byte) 142, 61, 94, (byte) 166, 123, 27, (byte) 255, 77, (byte) 191, (byte) 210, 15, (byte) 185, (byte) 138, 120, (byte) 196, (byte) 204, 96, (byte) 214, 56, 87, (byte) 135, 32, 20, (byte) 174 };
    private static final byte[] ENC_SECRET = new byte[] { (byte) 162, (byte) 213, 33, (byte) 195, (byte) 207, 109, (byte) 203, 62, 85, (byte) 133, 113, 65, (byte) 170, 71, 78, (byte) 186, 2, (byte) 235, (byte) 214, 118, (byte) 169, (byte) 150, 124, (byte) 157, 102, 108, (byte) 135, 114, 82, (byte) 160, 125, 76, (byte) 235, 78, (byte) 225, (byte) 130, 14, (byte) 164, (byte) 220, 40, (byte) 197, (byte) 204, 103, (byte) 215, 50, 0, (byte) 134, 125, 22, (byte) 251, 68, (byte) 185, (byte) 233, 6, (byte) 237, (byte) 136, 32, (byte) 169, (byte) 145, 105, (byte) 206, 50, 60, (byte) 221 };
    private static final byte[] ENC_VERSION = new byte[] { 13, (byte) 184, (byte) 192 };
    private static final byte[] ENC_API_URL = new byte[] { 53, (byte) 195, 101, 27, (byte) 182, 37, 86, (byte) 252, 76, (byte) 247, (byte) 136, 21, (byte) 244, (byte) 154, 61, (byte) 203, (byte) 145, 47, (byte) 159, 104, 10, (byte) 210, 54, 18, (byte) 189, 78, (byte) 174, (byte) 173, 7, (byte) 160, (byte) 138, 47, (byte) 244, (byte) 146, 63, (byte) 223, 42 };

    private static final int SEED_NAME = 0x4B;
    private static final int SEED_OWNER_ID = 0x7E;
    private static final int SEED_SECRET = 0x93;
    private static final int SEED_VERSION = 0x3C;
    private static final int SEED_API_URL = 0x5D;

    /**
     * Decrypt dynamically in memory at runtime.
     */
    public static String decrypt(byte[] data, int seed) {
        if (data == null || data.length == 0) return "";
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            int mask = (seed + (i * 0x5A)) & 0xFF;
            result[i] = (byte) ((data[i] & 0xFF) ^ mask);
        }
        return new String(result, StandardCharsets.UTF_8);
    }

    public static String getAppName() {
        return decrypt(ENC_NAME, SEED_NAME);
    }

    public static String getOwnerId() {
        return decrypt(ENC_OWNER_ID, SEED_OWNER_ID);
    }

    public static String getAppSecret() {
        return decrypt(ENC_SECRET, SEED_SECRET);
    }

    public static String getAppVersion() {
        return decrypt(ENC_VERSION, SEED_VERSION);
    }

    public static String getApiUrl() {
        return decrypt(ENC_API_URL, SEED_API_URL);
    }

    /**
     * Helper to encrypt custom string literals for developers.
     */
    public static byte[] encrypt(String text, int seed) {
        if (text == null) return new byte[0];
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        byte[] enc = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            int mask = (seed + (i * 0x5A)) & 0xFF;
            enc[i] = (byte) ((bytes[i] & 0xFF) ^ mask);
        }
        return enc;
    }
}
