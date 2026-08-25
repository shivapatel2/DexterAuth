# DexterAuth C++ SDK Implementation Guide

This guide explains how to integrate and use the DexterAuth C++ SDK in your application.

## Prerequisites

Ensure that the `DexterAuth` folder (containing `dexterauth.hpp` and other SDK files) is included in your project's include path.

## 1. Initialization

First, include the necessary headers:

```cpp
#include "DexterAuth/dexterauth.hpp"
#include "DexterAuth/skCrypter.h" // Recommended for string encryption
```

Initialize the `DexterAuth::Api` instance with your application credentials. It is highly recommended to encrypt your string literals to protect them from reverse engineering:

```cpp
// You can use skCrypt to secure your strings in memory
std::string name = skCrypt("your_app_name").decrypt();
std::string ownerid = skCrypt("your_owner_id").decrypt();
std::string secret = skCrypt("your_app_secret").decrypt();
std::string version = skCrypt("1.0").decrypt();
std::string url = skCrypt("https://dexterauth.com/api/v1/client/").decrypt();

// Optional: Get application checksum to verify file integrity
std::string my_hash = DexterAuth::Others::get_checksum();

// Initialize the SDK
DexterAuth::Api dexterauthapp(
    name,
    ownerid,
    version,
    secret,
    my_hash, // hash_to_check (optional, pass "" if unused)
    url // api_url
);
```

## 2. Advanced Security Features

DexterAuth provides additional security features to protect your application:

**Host Locking & Public Key Pinning:**
Ensure the application only communicates with authorized servers and prevents man-in-the-middle attacks.
```cpp
dexterauthapp.set_allowed_hosts({ "dexterauth.com" });
dexterauthapp.set_pinned_public_keys({ "your_public_key_hash" }); // Optional
```

**Anti-Debugging:**
Prevent attackers from attaching debuggers to your application.
```cpp
DexterAuth::Others::anti_debug();
```

**Secure String Handling (Inbuilt):**
DexterAuth has optional built-in secure string capabilities.
```cpp
dexterauthapp.enable_secure_strings();
dexterauthapp.derive_secure_key("your_key_material");
```

## 3. Authentication

### Hardware-ID (HWID) Locked Authentication

By default, DexterAuth uses the `windows_user` method for HWID. You can fetch or change it using:
```cpp
std::string hwid = DexterAuth::Others::get_hwid("windows_user"); // or "machine"
```

**Login:**
```cpp
bool success = dexterauthapp.login("username", "password");
if (success) {
    // Successfully logged in
    
    // Start background ban monitor (e.g., checks every 120 seconds)
    dexterauthapp.start_ban_monitor(120);
}
```

**Register:**
```cpp
bool success = dexterauthapp.registerAccount("username", "email@example.com", "password", "license_key");
```

### Web Authentication (No HWID Lock)

**Web Login:**
```cpp
bool success = dexterauthapp.webLogin("username", "password");
if (!success) {
    // Check if user is rate-limited / locked out
    if (dexterauthapp.lockout_active()) {
        long long secs = dexterauthapp.lockout_remaining_ms() / 1000;
        std::cout << "Locked out for " << secs << " seconds." << std::endl;
    }
}
```

**Web Register:**
```cpp
bool success = dexterauthapp.registerWeb("username", "email@example.com", "password", "license_key");
```

## 4. Account Management

**Upgrade Account (Apply a new license key):**
```cpp
bool success = dexterauthapp.upgrade("username", "new_license_key");
```

**Change Username:**
```cpp
bool success = dexterauthapp.changeUsername("new_username");
```

**Password Reset (Requires HWID match):**
```cpp
std::string hwid = DexterAuth::Others::get_hwid(dexterauthapp.hwid_method);
bool success = dexterauthapp.forgot("username", "new_password", hwid);
```

## 5. Session & Utility Functions

**Verify Active Session:**
```cpp
if (dexterauthapp.check()) {
    // Session is valid
}
```

**Verify Standalone Token:**
```cpp
if (dexterauthapp.verifyToken("user_token")) {
    // Token is valid
}
```

**Logout:**
```cpp
// Stop the background monitor before logging out
dexterauthapp.stop_ban_monitor();
bool success = dexterauthapp.logout();
```

**Accessing User Data:**
After a successful login, you can access user information via `dexterauthapp.user_data`:
- `dexterauthapp.user_data.username`
- `dexterauthapp.user_data.hwid`
- `dexterauthapp.user_data.subscription`
- `dexterauthapp.user_data.expires`
- `dexterauthapp.has_active_subscription()`
- `dexterauthapp.expiry_remaining()` (Returns remaining time in seconds)

## 6. Debugging

If you encounter issues during implementation, you can enable debug mode or fetch debug info:
```cpp
dexterauthapp.setDebug(true);

std::map<std::string, std::string> info = dexterauthapp.debugInfo();
for (const auto& pair : info) {
    std::cout << pair.first << ": " << pair.second << std::endl;
}
```
