package com.dexterauth.sdk.models;

/**
 * Metadata about the DexterAuth application returned during initialization.
 */
public class AppInfo {
    public String name = "";
    public String version = "";
    public String hwid_method = "default";
    public String customer_panel = "";
    public String status = "ENABLED";
    public boolean developer_mode = false;
    public boolean hash_check_enabled = false;
    public boolean paused = false;

    public AppInfo() {}

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getHwidMethod() {
        return hwid_method;
    }

    public String getCustomerPanel() {
        return customer_panel;
    }

    public String getStatus() {
        return status;
    }

    public boolean isDeveloperMode() {
        return developer_mode;
    }

    public boolean isHashCheckEnabled() {
        return hash_check_enabled;
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    public String toString() {
        return "AppInfo{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", hwid_method='" + hwid_method + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
