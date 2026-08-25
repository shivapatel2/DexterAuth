package com.dexterauth.sdk.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Lightweight, zero-dependency JSON helper utilizing standard platform JSON parsers.
 */
public class SimpleJson {

    public static JSONObject parseObject(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            Logger.debug("SimpleJson parseObject error: " + e.getMessage());
            return new JSONObject();
        }
    }

    public static JSONArray parseArray(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new JSONArray();
        }
        try {
            return new JSONArray(jsonString);
        } catch (Exception e) {
            Logger.debug("SimpleJson parseArray error: " + e.getMessage());
            return new JSONArray();
        }
    }

    public static String getString(JSONObject json, String key, String defaultValue) {
        if (json == null || key == null || !json.has(key) || json.isNull(key)) {
            return defaultValue;
        }
        try {
            return json.optString(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(JSONObject json, String key, boolean defaultValue) {
        if (json == null || key == null || !json.has(key) || json.isNull(key)) {
            return defaultValue;
        }
        try {
            return json.optBoolean(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getInt(JSONObject json, String key, int defaultValue) {
        if (json == null || key == null || !json.has(key) || json.isNull(key)) {
            return defaultValue;
        }
        try {
            return json.optInt(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static JSONObject getObject(JSONObject json, String key) {
        if (json == null || key == null || !json.has(key) || json.isNull(key)) {
            return new JSONObject();
        }
        try {
            return json.optJSONObject(key) != null ? json.optJSONObject(key) : new JSONObject();
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public static JSONArray getArray(JSONObject json, String key) {
        if (json == null || key == null || !json.has(key) || json.isNull(key)) {
            return new JSONArray();
        }
        try {
            return json.optJSONArray(key) != null ? json.optJSONArray(key) : new JSONArray();
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    /**
     * Canonical sort of JSON keys to ensure identical cryptographic signature calculations.
     */
    public static String toCanonicalString(JSONObject json) {
        if (json == null) return "{}";
        try {
            List<String> keys = new ArrayList<>();
            Iterator<String> iterator = json.keys();
            while (iterator.hasNext()) {
                keys.add(iterator.next());
            }
            Collections.sort(keys);

            StringBuilder sb = new StringBuilder("{");
            for (int i = 0; i < keys.size(); i++) {
                String k = keys.get(i);
                Object val = json.get(k);
                if (i > 0) sb.append(",");
                sb.append("\"").append(escape(k)).append("\":");
                if (val instanceof JSONObject) {
                    sb.append(toCanonicalString((JSONObject) val));
                } else if (val instanceof String) {
                    sb.append("\"").append(escape((String) val)).append("\"");
                } else if (val instanceof Number || val instanceof Boolean) {
                    sb.append(val.toString());
                } else if (val == JSONObject.NULL || val == null) {
                    sb.append("null");
                } else {
                    sb.append("\"").append(escape(val.toString())).append("\"");
                }
            }
            sb.append("}");
            return sb.toString();
        } catch (Exception e) {
            return json.toString();
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
