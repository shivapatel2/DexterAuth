# DexterAuth Java & Android SDK

[![Java Version](https://img.shields.io/badge/Java-8%2B-blue.svg?style=flat-square)](https://www.oracle.com/java/)
[![Android API](https://img.shields.io/badge/Android-API%2019%2B-green.svg?style=flat-square)](https://developer.android.com/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20JVM-lightgrey.svg?style=flat-square)](#)
[![License](https://img.shields.io/badge/License-MIT-brightgreen.svg?style=flat-square)](LICENSE)
[![Dependencies](https://img.shields.io/badge/Dependencies-Zero-orange.svg?style=flat-square)](#)

Official, lightweight, zero-dependency Java and Android SDK for **[DexterAuth](https://dexterauth.com)** — a modern authentication, licensing, and security platform. Engineered for seamless integration into Android APKs, desktop Java applications, and backend JVM microservices.

---

## 📑 Table of Contents
- [Features](#-features)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
  - [Installation](#installation)
  - [SDK Initialization](#sdk-initialization)
- [Authentication Guide](#-authentication-guide)
  - [1. Single License Key (Voucher Flow)](#1-single-license-key-voucher-flow)
  - [2. Username & Password Login](#2-username--password-login)
  - [3. User Registration with License](#3-user-registration-with-license)
  - [4. Account Upgrade](#4-account-upgrade)
- [Security Features](#-security-features)
  - [Signed Response Protocol (SRP)](#signed-response-protocol-srp)
  - [Hardware ID (HWID) Binding](#hardware-id-hwid-binding)
  - [In-Memory Credential Protection (`SecureVault`)](#in-memory-credential-protection-securevault)
  - [Anti-Debugging Protection](#anti-debugging-protection)
- [Remote Features & Variables](#-remote-features--variables)
- [Data Models Reference](#-data-models-reference)
- [License](#-license)

---

## 🌟 Features

* **Zero External Dependencies**: Self-contained with no heavy runtime dependencies; works on standard Android frameworks and JVM out of the box.
* **Signed Response Protocol (SRP)**: HMAC-SHA256 response signature verification with cryptographic request nonces to defeat HTTP proxy tampering, replay attacks, and response spoofing.
* **Multi-Platform HWID Engine**: Automatic extraction of Android ID and hardware fingerprints on Android, with automatic fallback for desktop JVMs.
* **Flexible Authentication**: Supports single-field license keys, username/password pairs, account registration, and subscription upgrades.
* **Thread-Safe Structured Logger**: Integrated logging with Logcat forwarding on Android and stdout/file logging on desktop JVM.
* **Bytecode Decompilation Protection (`SecureVault`)**: In-memory rolling XOR string encryption to block static credential extraction from `.dex` / `.class` binaries.

---

## 📁 Project Structure

```
DexterAuth-java-apk-sdk/
├── src/main/java/com/dexterauth/sdk/
│   ├── api.java                 # Core SDK engine (init, login, license, register, etc.)
│   ├── DexterAuth.java              # Fluent builder & wrapper
│   ├── Callback.java            # Asynchronous callback interface
│   ├── models/
│   │   ├── ApiResponse.java     # Unified response container
│   │   ├── AppInfo.java         # Remote application metadata
│   │   ├── SubscriptionInfo.java# Subscription details
│   │   ├── UpdateInfo.java      # Application updater metadata
│   │   └── UserData.java        # Authenticated user & subscription model
│   ├── security/
│   │   ├── HwidManager.java     # Multi-platform HWID extraction
│   │   ├── SecureVault.java     # In-memory credential decryptor & tool
│   │   └── SecurityUtils.java   # Cryptographic routines (HMAC-SHA256, SRP, Nonces)
│   └── utils/
│       ├── Logger.java          # Thread-safe dual logger (Logcat + File/Console)
│       └── SimpleJson.java      # Lightweight zero-dependency JSON helper
├── examples/
│   ├── BasicExample.java        # Console JVM usage example
│   └── AndroidActivityExample.java # Android Activity integration
├── build.gradle
├── settings.gradle
└── LICENSE
```

---

## 🚀 Getting Started

### Installation

#### Option 1: Direct Source Copy (Recommended for Android APKs)
Copy the `com/dexterauth/sdk/` folder directly into your project's `src/main/java/` directory.

#### Option 2: Gradle Library Dependency
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.dexterauth:DexterAuth-java-apk-sdk:1.0.0'
}
```

---

### SDK Initialization

Create a global `api` or `DexterAuth` instance and initialize it:

```java
import com.dexterauth.sdk.api;
import com.dexterauth.sdk.Callback;
import com.dexterauth.sdk.models.ApiResponse;

public class MainApplication {

    // Global SDK instance
    public static api DexterAuthApp = new api(
        "YOUR_APP_NAME",
        "YOUR_OWNER_ID",
        "YOUR_APP_SECRET",
        "1.0",
        "https://dexterauth.onrender.com/api/v2/client/"
    );

    public static void main(String[] args) {
        // Asynchronous initialization
        DexterAuthApp.initAsync(new Callback<ApiResponse>() {
            @Override
            public void onSuccess(ApiResponse result) {
                System.out.println("[+] DexterAuth connected: " + result.getMessage());
            }

            @Override
            public void onError(String error) {
                System.err.println("[-] Init failed: " + error);
            }
        });
    }
}
```

> **Android Tip:** In your `Activity.onCreate()`, invoke `DexterAuthApp.setContext(this)` so the SDK can access the Android `ContentResolver` for hardware identification.

---

## 🔑 Authentication Guide

### 1. Single License Key (Voucher Flow)
Ideal for single-field login interfaces. If the key was already registered, it logs in directly. If it is new, it automatically registers and activates the subscription:

```java
DexterAuthApp.licenseAsync("AIMKILL-XXXX-XXXX-XXXX", new Callback<ApiResponse>() {
    @Override
    public void onSuccess(ApiResponse result) {
        System.out.println("[+] Logged In!");
        System.out.println("    Expires: " + DexterAuthApp.user_data.getExpires());
        System.out.println("    Tier:    " + DexterAuthApp.user_data.getSubscription());
        System.out.println("    HWID:    " + DexterAuthApp.user_data.getHwid());
    }

    @Override
    public void onError(String error) {
        System.err.println("[-] License Authentication Failed: " + error);
    }
});
```

### 2. Username & Password Login
```java
DexterAuthApp.loginAsync("john_doe", "password123", new Callback<ApiResponse>() {
    @Override
    public void onSuccess(ApiResponse result) {
        System.out.println("[+] Welcome, " + DexterAuthApp.user_data.getUsername());
    }

    @Override
    public void onError(String error) {
        System.err.println("[-] Login Failed: " + error);
    }
});
```

### 3. User Registration with License
```java
DexterAuthApp.registerWithLicenseAsync("new_user", "password123", "KEY-XXXX", new Callback<ApiResponse>() {
    @Override
    public void onSuccess(ApiResponse result) {
        System.out.println("[+] User successfully registered and subscription activated!");
    }

    @Override
    public void onError(String error) {
        System.err.println("[-] Registration Error: " + error);
    }
});
```

### 4. Account Upgrade
```java
DexterAuthApp.upgrade("existing_user", "UPGRADE-KEY-XXXX");
```

---

## 🛡️ Security Features

### Signed Response Protocol (SRP)
Every request generates a cryptographically secure 32-character nonce (`X-Request-Nonce`). The DexterAuth server returns the nonce and computes `X-Response-Sig = HMAC-SHA256(canonical_body + ":" + nonce, secret)`.

The SDK automatically verifies this signature before parsing any response data. If a reverse engineer uses a debugging proxy (Charles, Fiddler, Burp) to tamper with the JSON response, verification hard-fails.

### Hardware ID (HWID) Binding
The SDK generates a deterministic SHA-256 fingerprint combining the device's `android_id`, hardware board, manufacturer, model, and package signature, preventing multiple devices from sharing the same license.

### In-Memory Credential Protection (`SecureVault`)
To stop decompilers (JADX, Apktool) from discovering your App Secret and App ID via simple string searches:

```java
public static api DexterAuthApp = new api(
    SecureVault.getAppName(),
    SecureVault.getOwnerId(),
    SecureVault.getAppSecret(),
    SecureVault.getAppVersion(),
    SecureVault.getApiUrl()
);
```

---

## 📡 Remote Features & Variables

### Fetch Application Variable
Retrieve dynamic strings, configs, or feature flags stored on the DexterAuth server:
```java
String remoteConfig = DexterAuthApp.var("my_remote_variable");
```

### Audit Logging
Send telemetry or security alert logs directly to your DexterAuth web panel:
```java
DexterAuthApp.log("User triggered critical action");
```

### Check Session / Ban Status
Verify if the current user session or device HWID has been revoked:
```java
if (DexterAuthApp.check_ban()) {
    System.err.println("Device/Account has been banned.");
    System.exit(0);
}
```

---

## 📊 Data Models Reference

### `UserData`
| Field | Type | Description |
|---|---|---|
| `username` | `String` | Authenticated username or license identifier |
| `hwid` | `String` | Bound Hardware ID |
| `expires` | `String` | Subscription expiration timestamp or "Lifetime" |
| `createdate` | `String` | Account creation timestamp |
| `lastlogin` | `String` | Timestamp of last recorded login |
| `subscription` | `String` | Active subscription level name |
| `subscriptions` | `List<SubscriptionInfo>` | List of all active subscription plans |
| `is_authenticated` | `boolean` | Authentication status flag |

---

## 📄 License
This project is licensed under the [MIT License](LICENSE).
