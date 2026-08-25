package com.dexterauth.sdk.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the authenticated user's profile and active subscription details.
 */
public class UserData {
    public String username = "";
    public String hwid = "";
    public String expires = "";
    public String createdate = "";
    public String lastlogin = "";
    public String subscription = "";
    public List<SubscriptionInfo> subscriptions = new ArrayList<>();
    public boolean is_authenticated = false;
    public double auth_runtime_start = 0.0;

    public UserData() {}

    public String getUsername() {
        return username;
    }

    public String getHwid() {
        return hwid;
    }

    public String getExpires() {
        return expires;
    }

    public String getCreateDate() {
        return createdate;
    }

    public String getLastLogin() {
        return lastlogin;
    }

    public String getSubscription() {
        return subscription;
    }

    public List<SubscriptionInfo> getSubscriptions() {
        return subscriptions;
    }

    public boolean isAuthenticated() {
        return is_authenticated;
    }

    public double getAuthRuntimeStart() {
        return auth_runtime_start;
    }

    public void clear() {
        username = "";
        hwid = "";
        expires = "";
        createdate = "";
        lastlogin = "";
        subscription = "";
        subscriptions.clear();
        is_authenticated = false;
        auth_runtime_start = 0.0;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "username='" + username + '\'' +
                ", hwid='" + hwid + '\'' +
                ", expires='" + expires + '\'' +
                ", createdate='" + createdate + '\'' +
                ", lastlogin='" + lastlogin + '\'' +
                ", subscription='" + subscription + '\'' +
                ", subscriptionsCount=" + subscriptions.size() +
                ", isAuthenticated=" + is_authenticated +
                '}';
    }
}
