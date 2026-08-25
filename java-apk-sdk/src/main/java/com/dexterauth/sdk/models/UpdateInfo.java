package com.dexterauth.sdk.models;

/**
 * Encapsulates application update details.
 */
public class UpdateInfo {
    public boolean update_available = false;
    public String current_version = "";
    public String latest_version = "";
    public String download_url = "";
    public String file_name = "";
    public String release_notes = "";

    public UpdateInfo() {}

    public boolean isUpdateAvailable() {
        return update_available;
    }

    public String getCurrentVersion() {
        return current_version;
    }

    public String getLatestVersion() {
        return latest_version;
    }

    public String getDownloadUrl() {
        return download_url;
    }

    public String getFileName() {
        return file_name;
    }

    public String getReleaseNotes() {
        return release_notes;
    }
}
