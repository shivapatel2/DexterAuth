package com.dexterauth.sdk;

import com.dexterauth.sdk.models.ApiResponse;
import com.dexterauth.sdk.models.AppInfo;
import com.dexterauth.sdk.models.SubscriptionInfo;
import com.dexterauth.sdk.models.UserData;
import com.dexterauth.sdk.security.HwidManager;
import com.dexterauth.sdk.security.SecurityUtils;
import com.dexterauth.sdk.utils.Logger;
import com.dexterauth.sdk.utils.SimpleJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Official DexterAuth Java & Android Client SDK.
 * Handles initialization, secure licensing, authentication, and session management.
 */
public class api {

    public String name = "";
    public String ownerid = "";
    public String version = "";
    public String secret = "";
    public String api_url = "https://dexterauth.onrender.com/api/v2/client/";
    public String hash_to_check = "";
    public String session_token = "";
    public boolean initialized = false;
    public String hwid_method = "default";
    public String last_message = "";
    public boolean debug = false;
    public boolean auto_update_enabled = false;

    public UserData user_data = new UserData();
    public AppInfo app_info = new AppInfo();
    public ApiResponse response = new ApiResponse();

    private Object androidContext = null;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public api(String name, String ownerid, String secret, String version) {
        this(name, ownerid, secret, version, "https://dexterauth.onrender.com/api/v2/client/", "");
    }

    public api(String name, String ownerid, String secret, String version, String api_url) {
        this(name, ownerid, secret, version, api_url, "");
    }

    public api(String name, String ownerid, String secret, String version, String api_url, String hash_to_check) {
        this.name = name != null ? name : "";
        this.ownerid = ownerid != null ? ownerid : "";
        this.secret = secret != null ? secret : "";
        this.version = version != null ? version : "1.0";
        if (api_url != null && !api_url.trim().isEmpty()) {
            this.api_url = api_url.endsWith("/") ? api_url : api_url + "/";
        }
        this.hash_to_check = hash_to_check != null ? hash_to_check : "";
    }

    public void setContext(Object context) {
        this.androidContext = context;
    }

    public String getHWID() {
        return HwidManager.getHwid(this.androidContext);
    }

    /**
     * Synchronous SDK Initialization.
     */
    public boolean init() {
        SecurityUtils.antiDebug();

        try {
            JSONObject payload = new JSONObject();
            payload.put("app_id", ownerid);
            payload.put("name", name);
            payload.put("version", version);

            if (hash_to_check != null && !hash_to_check.isEmpty()) {
                payload.put("hash", hash_to_check);
                if (secret != null && !secret.isEmpty()) {
                    String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                    String nonce = SecurityUtils.generateNonce(32);
                    String dataToSign = hash_to_check + ":" + timestamp + ":" + nonce;
                    payload.put("hash_signature", SecurityUtils.hmacSha256(secret, dataToSign));
                    payload.put("hash_timestamp", timestamp);
                    payload.put("hash_nonce", nonce);
                }
            }

            JSONObject jsonRes = doRequest("init", payload);
            if (jsonRes != null && "success".equalsIgnoreCase(SimpleJson.getString(jsonRes, "status", ""))) {
                JSONObject appInfoObj = SimpleJson.getObject(jsonRes, "app_info");
                app_info.name = SimpleJson.getString(appInfoObj, "name", name);
                app_info.version = SimpleJson.getString(appInfoObj, "version", version);
                app_info.hwid_method = SimpleJson.getString(appInfoObj, "hwid_method", "default");
                app_info.status = SimpleJson.getString(appInfoObj, "status", "ENABLED");
                app_info.customer_panel = SimpleJson.getString(appInfoObj, "customer_panel", "");
                app_info.developer_mode = SimpleJson.getBoolean(appInfoObj, "developer_mode", false);
                app_info.hash_check_enabled = SimpleJson.getBoolean(appInfoObj, "hash_check_enabled", false);
                app_info.paused = SimpleJson.getBoolean(appInfoObj, "paused", false);

                session_token = SimpleJson.getString(jsonRes, "session_token", session_token);
                hwid_method = app_info.hwid_method;
                initialized = true;

                response.success = true;
                response.message = SimpleJson.getString(jsonRes, "message", "Initialized successfully");
                response.app_info = app_info;
                response.session_token = session_token;
                last_message = response.message;
                Logger.info("DexterAuth Initialized successfully for " + name);
                return true;
            } else {
                String errMsg = jsonRes != null ? SimpleJson.getString(jsonRes, "message", "Failed to initialize") : "Network/Handshake Error";
                initialized = false;
                response.success = false;
                response.message = errMsg;
                last_message = errMsg;
                Logger.error("DexterAuth Init Failed: " + errMsg);
                return false;
            }
        } catch (Exception e) {
            String errMsg = "Init Exception: " + e.getMessage();
            initialized = false;
            response.success = false;
            response.message = errMsg;
            last_message = errMsg;
            Logger.error(errMsg);
            return false;
        }
    }

    public void initAsync(Callback<ApiResponse> callback) {
        executor.execute(() -> {
            boolean ok = init();
            if (callback != null) {
                if (ok) callback.onSuccess(response);
                else callback.onError(response.message);
            }
        });
    }

    /**
     * Unified License Key Authentication.
     * Since license keys are used as registration vouchers in DexterAuth:
     * 1. Attempts login(licenseKey, licenseKey) for already-registered keys.
     * 2. If the user account does not exist, registers the license key via register(licenseKey, licenseKey, licenseKey, "").
     * 3. Falls back to direct /license endpoint if server-side direct license mode is configured.
     */
    public boolean license(String licenseKey) {
        if (!initialized && !init()) {
            return false;
        }

        if (licenseKey == null || licenseKey.trim().isEmpty()) {
            return handleAuthError("License key cannot be empty");
        }

        String key = licenseKey.trim();

        // 1. Try login for previously registered license key
        if (login(key, key)) {
            Logger.info("Authenticated with registered license key");
            return true;
        }

        // 2. If login failed, attempt to register new account with the license key
        String loginError = this.last_message;
        Logger.info("Attempting license registration for key: " + key);
        if (register(key, key, key, "")) {
            if (user_data.is_authenticated) {
                Logger.info("License key registered and authenticated successfully");
                return true;
            }
            // Auto-login after registration
            if (login(key, key)) {
                Logger.info("Logged in after license registration");
                return true;
            }
        }

        // 3. Fallback: try dedicated /license endpoint if supported by backend
        try {
            JSONObject payload = new JSONObject();
            payload.put("app_id", ownerid);
            payload.put("name", name);
            payload.put("version", version);
            payload.put("license", key);
            payload.put("hwid", getHWID());
            if (session_token != null && !session_token.isEmpty()) {
                payload.put("session_token", session_token);
            }

            JSONObject jsonRes = doRequest("license", payload);
            if (jsonRes != null && "success".equalsIgnoreCase(SimpleJson.getString(jsonRes, "status", ""))) {
                return handleAuthResponse(jsonRes);
            }
        } catch (Exception ignored) {}

        // If register failed, the register error message is more informative (e.g. "License invalid", "License used")
        return false;
    }

    public void licenseAsync(String licenseKey, Callback<ApiResponse> callback) {
        executor.execute(() -> {
            boolean ok = license(licenseKey);
            if (callback != null) {
                if (ok) callback.onSuccess(response);
                else callback.onError(response.message);
            }
        });
    }

    /**
     * Explicit registration with a license key.
     */
    public boolean registerWithLicense(String username, String password, String licenseKey) {
        return register(username, password, licenseKey, "");
    }

    public void registerWithLicenseAsync(String username, String password, String licenseKey, Callback<ApiResponse> callback) {
        registerAsync(username, password, licenseKey, "", callback);
    }

    /**
     * Authenticate with standard Username and Password.
     */
    public boolean login(String username, String password) {
        if (!initialized && !init()) {
            return false;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("app_id", ownerid);
            payload.put("name", name);
            payload.put("version", version);
            payload.put("username", username);
            payload.put("password", password);
            payload.put("hwid", getHWID());
            if (session_token != null && !session_token.isEmpty()) {
                payload.put("session_token", session_token);
            }

            JSONObject jsonRes = doRequest("login", payload);
            return handleAuthResponse(jsonRes);
        } catch (Exception e) {
            return handleAuthError(e.getMessage());
        }
    }

    public void loginAsync(String username, String password, Callback<ApiResponse> callback) {
        executor.execute(() -> {
            boolean ok = login(username, password);
            if (callback != null) {
                if (ok) callback.onSuccess(response);
                else callback.onError(response.message);
            }
        });
    }

    /**
     * Register a new user account with a license key.
     */
    public boolean register(String username, String password, String licenseKey, String email) {
        if (!initialized && !init()) {
            return false;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("app_id", ownerid);
            payload.put("name", name);
            payload.put("version", version);
            payload.put("username", username);
            payload.put("password", password);
            payload.put("license_key", licenseKey);
            payload.put("license", licenseKey);
            if (email != null && !email.trim().isEmpty()) {
                payload.put("email", email.trim());
            }
            payload.put("hwid", getHWID());
            if (session_token != null && !session_token.isEmpty()) {
                payload.put("session_token", session_token);
            }

            JSONObject jsonRes = doRequest("register", payload);
            return handleAuthResponse(jsonRes);
        } catch (Exception e) {
            return handleAuthError(e.getMessage());
        }
    }

    public void registerAsync(String username, String password, String licenseKey, String email, Callback<ApiResponse> callback) {
        executor.execute(() -> {
            boolean ok = register(username, password, licenseKey, email);
            if (callback != null) {
                if (ok) callback.onSuccess(response);
                else callback.onError(response.message);
            }
        });
    }

    /**
     * Upgrade existing user account with a new license key.
     */
    public boolean upgrade(String username, String licenseKey) {
        if (!initialized && !init()) {
            return false;
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("app_id", ownerid);
            payload.put("name", name);
            payload.put("version", version);
            payload.put("username", username);
            payload.put("license_key", licenseKey);
            payload.put("license", licenseKey);

            JSONObject jsonRes = doRequest("upgrade", payload);
            if (jsonRes != null && "success".equalsIgnoreCase(SimpleJson.getString(jsonRes, "status", ""))) {
                response.success = true;
                response.message = SimpleJson.getString(jsonRes, "message", "Upgraded successfully");
                last_message = response.message;
                return true;
            } else {
                String msg = jsonRes != null ? SimpleJson.getString(jsonRes, "message", "Upgrade failed") : "Network error";
                response.success = false;
                response.message = msg;
                last_message = msg;
                return false;
            }
        } catch (Exception e) {
            return handleAuthError(e.getMessage());
        }
    }

    /**
     * Retrieve remote application variable.
     */
    public String var(String varId) {
        if (!initialized && !init()) {
            return "";
        }

        try {
            JSONObject payload = new JSONObject();
            payload.put("app_id", ownerid);
            payload.put("name", name);
            payload.put("version", version);
            payload.put("session_token", session_token);
            payload.put("var_id", varId);

            JSONObject jsonRes = doRequest("var", payload);
            if (jsonRes != null && "success".equalsIgnoreCase(SimpleJson.getString(jsonRes, "status", ""))) {
                String val = SimpleJson.getString(jsonRes, "response", SimpleJson.getString(jsonRes, "value", ""));
                response.response_value = val;
                return val;
            }
        } catch (Exception ignored) {}
        return "";
    }

    /**
     * Post a log message to the DexterAuth dashboard.
     */
    public boolean log(String message) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("app_id", ownerid);
            payload.put("name", name);
            payload.put("version", version);
            payload.put("session_token", session_token);
            payload.put("message", message);

            JSONObject jsonRes = doRequest("log", payload);
            return jsonRes != null && "success".equalsIgnoreCase(SimpleJson.getString(jsonRes, "status", ""));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if user session has been revoked or banned.
     */
    public boolean check_ban() {
        try {
            JSONObject payload = new JSONObject();
            payload.put("app_id", ownerid);
            payload.put("name", name);
            payload.put("version", version);
            payload.put("session_token", session_token);
            payload.put("hwid", getHWID());

            JSONObject jsonRes = doRequest("check_ban", payload);
            if (jsonRes != null) {
                return SimpleJson.getBoolean(jsonRes, "is_banned", false) ||
                        "banned".equalsIgnoreCase(SimpleJson.getString(jsonRes, "status", ""));
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean handleAuthResponse(JSONObject jsonRes) {
        if (jsonRes != null && "success".equalsIgnoreCase(SimpleJson.getString(jsonRes, "status", ""))) {
            user_data.clear();
            user_data.is_authenticated = true;
            user_data.auth_runtime_start = System.currentTimeMillis() / 1000.0;

            JSONObject userObj = new JSONObject();
            JSONObject dataObj = SimpleJson.getObject(jsonRes, "data");
            if (dataObj.length() > 0) {
                userObj = SimpleJson.getObject(dataObj, "user");
                session_token = SimpleJson.getString(dataObj, "token", session_token);
            }

            if (userObj.length() == 0) {
                userObj = SimpleJson.getObject(jsonRes, "info");
            }
            if (userObj.length() == 0) {
                userObj = jsonRes;
            }

            user_data.username = SimpleJson.getString(userObj, "username", "");
            user_data.hwid = SimpleJson.getString(userObj, "hwid", getHWID());
            user_data.expires = SimpleJson.getString(userObj, "expiration_date",
                    SimpleJson.getString(userObj, "expires",
                    SimpleJson.getString(userObj, "expiry", "Lifetime")));
            user_data.createdate = SimpleJson.getString(userObj, "created_at",
                    SimpleJson.getString(userObj, "createdate", ""));
            user_data.lastlogin = SimpleJson.getString(userObj, "last_login",
                    SimpleJson.getString(userObj, "lastlogin", ""));
            user_data.subscription = SimpleJson.getString(userObj, "subscription_level",
                    SimpleJson.getString(userObj, "subscription", "Default"));

            JSONArray subs = SimpleJson.getArray(userObj, "subscriptions");
            for (int i = 0; i < subs.length(); i++) {
                try {
                    JSONObject subObj = subs.getJSONObject(i);
                    user_data.subscriptions.add(new SubscriptionInfo(
                            SimpleJson.getString(subObj, "subscription", ""),
                            SimpleJson.getString(subObj, "expiry", "")
                    ));
                } catch (Exception ignored) {}
            }

            session_token = SimpleJson.getString(jsonRes, "session_token", session_token);

            response.success = true;
            response.message = SimpleJson.getString(jsonRes, "message", "Authenticated successfully");
            response.session_token = session_token;
            response.user_data = user_data;
            response.raw_json = jsonRes.toString();
            last_message = response.message;
            return true;
        } else {
            String msg = jsonRes != null ? SimpleJson.getString(jsonRes, "message", "Authentication failed") : "Invalid response";
            if (jsonRes != null) {
                JSONArray errors = SimpleJson.getArray(jsonRes, "errors");
                if (errors.length() > 0) {
                    try {
                        JSONObject firstErr = errors.getJSONObject(0);
                        String errMsg = SimpleJson.getString(firstErr, "message", "");
                        JSONArray pathArr = SimpleJson.getArray(firstErr, "path");
                        if (pathArr.length() > 0) {
                            msg = pathArr.getString(0) + ": " + errMsg;
                        } else if (!errMsg.isEmpty()) {
                            msg = errMsg;
                        }
                    } catch (Exception ignored) {}
                }
            }
            return handleAuthError(msg);
        }
    }

    private boolean handleAuthError(String message) {
        user_data.is_authenticated = false;
        response.success = false;
        response.message = message;
        last_message = message;
        Logger.error("DexterAuth Error: " + message);
        return false;
    }

    /**
     * Unified network request executor with Signed Response Protocol (SRP) support.
     */
    private JSONObject doRequest(String endpoint, JSONObject payload) {
        HttpURLConnection conn = null;
        try {
            String targetUrl = api_url + endpoint;
            String requestNonce = SecurityUtils.generateNonce(32);

            URL url = new URL(targetUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "DexterAuth-SDK-Java/1.0 (" + name + ")");
            conn.setRequestProperty("X-Request-Nonce", requestNonce);

            byte[] bodyBytes = payload.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBytes);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) {
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String responseBody = sb.toString();
            String sigHeader = conn.getHeaderField("X-Response-Sig");
            String nonceHeader = conn.getHeaderField("X-Response-Nonce");

            // Verify SRP signature if secret is active
            if (secret != null && !secret.isEmpty() && sigHeader != null && nonceHeader != null) {
                boolean verified = SecurityUtils.verifyResponseSignature(responseBody, requestNonce, sigHeader, nonceHeader, secret);
                if (!verified) {
                    Logger.error("[SECURITY] Tampered response detected from " + endpoint);
                    return null;
                }
            }

            return SimpleJson.parseObject(responseBody);
        } catch (Exception e) {
            Logger.error("HTTP request error to " + endpoint + ": " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                try { conn.disconnect(); } catch (Exception ignored) {}
            }
        }
    }
}
