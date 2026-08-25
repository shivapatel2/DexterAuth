package com.dexterauth.sdk.models;

/**
 * Represents a single subscription associated with an DexterAuth user account.
 */
public class SubscriptionInfo {
    public String subscription;
    public String expiry;

    public SubscriptionInfo() {
        this.subscription = "";
        this.expiry = "";
    }

    public SubscriptionInfo(String subscription, String expiry) {
        this.subscription = subscription != null ? subscription : "";
        this.expiry = expiry != null ? expiry : "";
    }

    public String getSubscription() {
        return subscription;
    }

    public String getExpiry() {
        return expiry;
    }

    @Override
    public String toString() {
        return "SubscriptionInfo{" +
                "subscription='" + subscription + '\'' +
                ", expiry='" + expiry + '\'' +
                '}';
    }
}
