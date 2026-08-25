package com.dexterauth.sdk.models;

/**
 * Standard unified response object returned by DexterAuth API operations.
 */
public class ApiResponse {
    public boolean success = false;
    public String message = "";
    public String session_token = "";
    public UserData user_data = new UserData();
    public AppInfo app_info = new AppInfo();
    public String response_value = "";
    public String raw_json = "";

    public ApiResponse() {}

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message != null ? message : "";
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionToken() {
        return session_token;
    }

    public UserData getUserData() {
        return user_data;
    }

    public AppInfo getAppInfo() {
        return app_info;
    }

    public String getResponseValue() {
        return response_value;
    }

    public String getRawJson() {
        return raw_json;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", sessionToken='" + (session_token != null && !session_token.isEmpty() ? "***" : "") + '\'' +
                '}';
    }
}
