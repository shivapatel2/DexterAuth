package com.dexterauth.sdk;

/**
 * Top-level convenience wrapper for the DexterAuth SDK.
 */
public class DexterAuth extends api {

    public DexterAuth(String name, String ownerid, String secret, String version) {
        super(name, ownerid, secret, version);
    }

    public DexterAuth(String name, String ownerid, String secret, String version, String apiUrl) {
        super(name, ownerid, secret, version, apiUrl);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name = "";
        private String ownerId = "";
        private String secret = "";
        private String version = "1.0";
        private String apiUrl = "https://dexterauth.onrender.com/api/v2/client/";
        private String hashToCheck = "";
        private Object context = null;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setOwnerId(String ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder setHashToCheck(String hashToCheck) {
            this.hashToCheck = hashToCheck;
            return this;
        }

        public Builder setContext(Object context) {
            this.context = context;
            return this;
        }

        public DexterAuth build() {
            DexterAuth app = new DexterAuth(name, ownerId, secret, version, apiUrl);
            if (hashToCheck != null && !hashToCheck.isEmpty()) {
                app.hash_to_check = hashToCheck;
            }
            if (context != null) {
                app.setContext(context);
            }
            return app;
        }
    }
}
