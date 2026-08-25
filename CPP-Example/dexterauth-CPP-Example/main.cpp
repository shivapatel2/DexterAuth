#include <Windows.h>
#include "DexterAuth/dexterauth.hpp"
#include <iostream>
#include <string>
#include "DexterAuth/skCrypter.h"
#include <algorithm>

std::string name = skCrypt("your_application_name").decrypt();
std::string ownerid = skCrypt("your_application_owner_id_from_dashboard").decrypt();
std::string secret = skCrypt("your_application_secret_key_from_dashboard").decrypt();
std::string version = skCrypt("1.0").decrypt();
// v2: direct to dexterauth.onrender.com — no 307 redirect, SRP-secured endpoint
// Old v1 URL (keep for legacy builds): "https://dexterauth.com/api/v1/client/"
std::string url = skCrypt("https://dexterauth.onrender.com/api/v2/client/").decrypt();

void clear() {
#ifdef _WIN32
    system("cls");
#else
    system("clear");
#endif
}

std::string get_input(const std::string& prompt) {
    std::cout << prompt;
    std::string value;
    std::getline(std::cin, value);
    // Trim whitespace
    value.erase(0, value.find_first_not_of(" \t\r\n"));
    value.erase(value.find_last_not_of(" \t\r\n") + 1);
    return value;
}

void banner(DexterAuth::Api& dexterauthapp) {
    clear();
    std::string mode = dexterauthapp.client_secret.empty() ? "OFF ⚠" : "SECURE 🔒";
    std::cout << "╔" << std::string(58, '=') << "╗" << std::endl;
    
    std::string title = "║  " + dexterauthapp.name + "  —  Powered by DexterAuth";
    if (title.length() < 60) {
        title += std::string(60 - title.length(), ' ');
    }
    title += "║";
    std::cout << title << std::endl;

    std::string mode_line = "║  Anti-Tamper: " + mode;
    if (mode_line.length() < 60) {
        mode_line += std::string(60 - mode_line.length(), ' ');
    }
    mode_line += "║";
    std::cout << mode_line << std::endl;

    std::cout << "╚" << std::string(58, '=') << "╝" << std::endl;

    if (dexterauthapp.user_data.is_authenticated && !dexterauthapp.user_data.username.empty()) {
        std::cout << "\n  Logged in as: " << dexterauthapp.user_data.username << std::endl;
    }
}

std::string menu_main() {
    std::cout << "\n  MAIN MENU" << std::endl;
    std::cout << "  " << std::string(53, '-') << std::endl;
    std::cout << "  [1]  Login" << std::endl;
    std::cout << "  [2]  Register with License Key" << std::endl;
    std::cout << "  [3]  Web Login  (no HWID)" << std::endl;
    std::cout << "  [4]  Web Register  (no HWID)" << std::endl;
    std::cout << "  [5]  Forgot Password  (HWID-verified reset)" << std::endl;
    std::cout << "  [6]  Verify Standalone API Token" << std::endl;
    std::cout << "  [7]  Show HWID methods" << std::endl;
    std::cout << "  [8]  Debug Info" << std::endl;
    std::cout << "  [9]  Check & Install Updates" << std::endl;
    std::cout << "  [0]  Exit" << std::endl;
    return get_input("\n  › ");
}

std::string menu_account() {
    std::cout << "\n  ACCOUNT MENU" << std::endl;
    std::cout << "  " << std::string(53, '-') << std::endl;
    std::cout << "  [1]  Account Details  (view info & expiry)" << std::endl;
    std::cout << "  [2]  Change Username" << std::endl;
    std::cout << "  [3]  Upgrade Account  (apply another license key)" << std::endl;
    std::cout << "  [4]  Verify Session" << std::endl;
    std::cout << "  [5]  Logout" << std::endl;
    std::cout << "  [0]  Back" << std::endl;
    return get_input("\n  › ");
}

bool example_login(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── LOGIN ──────────────────────────────────────────────" << std::endl;
    std::string user = get_input("  Username : ");
    std::string password = get_input("  Password : ");

    if (dexterauthapp.login(user, password)) {
        std::cout << "\n  ✓ Logged in as '" << dexterauthapp.user_data.username << "'" << std::endl;
        std::cout << "  Subscription : " << (dexterauthapp.user_data.subscription.empty() ? "N/A" : dexterauthapp.user_data.subscription) << std::endl;
        std::cout << "  Expires      : " << (dexterauthapp.user_data.expires.empty() ? "N/A" : dexterauthapp.user_data.expires) << std::endl;
        std::cout << "  Last Login   : " << (dexterauthapp.user_data.lastlogin.empty() ? "N/A" : dexterauthapp.user_data.lastlogin) << std::endl;

        double remaining = dexterauthapp.expiry_remaining();
        if (remaining > 0.0) {
            int d = (int)(remaining / 86400.0);
            int h = (int)(((long long)remaining % 86400) / 3600);
            std::cout << "  Time left    : " << d << "d " << h << "h" << std::endl;
        } else {
            std::cout << "  Time left    : EXPIRED" << std::endl;
        }

        dexterauthapp.start_ban_monitor(120);
        std::cout << "  Ban monitor  : Active (120s interval)" << std::endl;
        return true;
    }

    std::cout << "  ✗ Login failed." << std::endl;
    return false;
}

void example_register(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── REGISTER ────────────────────────────────────────────" << std::endl;
    std::string user = get_input("  Username    : ");
    std::string email = get_input("  Email       : ");
    std::string password = get_input("  Password    : ");
    std::string key = get_input("  License Key : ");

    if (dexterauthapp.registerAccount(user, email, password, key)) {
        std::cout << "  ✓ Registered! You can now log in." << std::endl;
    } else {
        std::cout << "  ✗ Registration failed. Check the license key." << std::endl;
    }
}

bool example_web_login(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── WEB LOGIN (no HWID) ─────────────────────────────────" << std::endl;
    std::string user = get_input("  Username : ");
    std::string password = get_input("  Password : ");

    if (dexterauthapp.webLogin(user, password)) {
        std::cout << "\n  ✓ Authenticated as '" << dexterauthapp.user_data.username << "'" << std::endl;
        std::cout << "  Subscription : " << (dexterauthapp.user_data.subscription.empty() ? "N/A" : dexterauthapp.user_data.subscription) << std::endl;
        std::cout << "  Expires      : " << (dexterauthapp.user_data.expires.empty() ? "N/A" : dexterauthapp.user_data.expires) << std::endl;
        std::cout << "  Last Login   : " << (dexterauthapp.user_data.lastlogin.empty() ? "N/A" : dexterauthapp.user_data.lastlogin) << std::endl;

        dexterauthapp.start_ban_monitor(120);
        std::cout << "  Ban monitor  : Active (120s interval)" << std::endl;
        return true;
    } else {
        if (dexterauthapp.lockout_active()) {
            long long secs = dexterauthapp.lockout_remaining_ms() / 1000;
            std::cout << "  ✗ Locked out for " << secs << " seconds." << std::endl;
        } else {
            std::cout << "  ✗ Web login failed." << std::endl;
        }
        return false;
    }
}

void example_register_web(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── WEB REGISTER (no HWID) ──────────────────────────────" << std::endl;
    std::string user = get_input("  Username    : ");
    std::string email = get_input("  Email       : ");
    std::string password = get_input("  Password    : ");
    std::string key = get_input("  License Key : ");

    if (dexterauthapp.registerWeb(user, email, password, key)) {
        std::cout << "  ✓ Registered via web flow!" << std::endl;
    } else {
        std::cout << "  ✗ Registration failed." << std::endl;
    }
}

void example_upgrade(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── UPGRADE ACCOUNT ─────────────────────────────────────" << std::endl;
    std::string user = get_input("  Username    : ");
    if (user.empty()) {
        user = dexterauthapp.user_data.username;
    }
    std::string key = get_input("  License Key : ");

    if (dexterauthapp.upgrade(user, key)) {
        std::cout << "  ✓ Account upgraded!" << std::endl;
    } else {
        std::cout << "  ✗ Upgrade failed. Check the license key." << std::endl;
    }
}

void example_change_username(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── CHANGE USERNAME ─────────────────────────────────────" << std::endl;
    std::string new_name = get_input("  New Username : ");

    if (dexterauthapp.changeUsername(new_name)) {
        std::cout << "  ✓ Username changed to '" << dexterauthapp.user_data.username << "'" << std::endl;
    } else {
        std::cout << "  ✗ Username change failed." << std::endl;
    }
}

void example_forgot_password(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── FORGOT PASSWORD (HWID-verified reset) ───────────────" << std::endl;
    std::cout << "  Your current Hardware ID will be used to verify your identity." << std::endl;
    std::string user = get_input("  Username     : ");
    std::string new_pass = get_input("  New Password : ");

    std::string hwid = DexterAuth::Others::get_hwid(dexterauthapp.hwid_method);
    std::cout << "  Using HWID   : " << (hwid.length() > 20 ? hwid.substr(0, 20) + "..." : hwid) << std::endl;

    if (dexterauthapp.forgot(user, new_pass, hwid)) {
        std::cout << "  ✓ Password reset! You can now log in with your new password." << std::endl;
    } else {
        std::cout << "  ✗ Reset failed. Is this HWID bound to the account?" << std::endl;
    }
}

void example_verify_session(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── VERIFY SESSION ──────────────────────────────────────" << std::endl;
    if (dexterauthapp.session_token.empty()) {
        std::cout << "  Not logged in." << std::endl;
        return;
    }
    if (dexterauthapp.check()) {
        std::cout << "  ✓ Session is valid." << std::endl;
    } else {
        std::cout << "  ✗ Session has expired or been revoked." << std::endl;
    }
}

void example_verify_token(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── VERIFY STANDALONE TOKEN ─────────────────────────────" << std::endl;
    std::string token = get_input("  Token : ");

    if (dexterauthapp.verifyToken(token)) {
        std::cout << "  ✓ Token is valid." << std::endl;
    } else {
        std::cout << "  ✗ Token is invalid or banned." << std::endl;
    }
}

void example_logout(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── LOGOUT ──────────────────────────────────────────────" << std::endl;
    dexterauthapp.stop_ban_monitor();
    if (dexterauthapp.logout()) {
        std::cout << "  ✓ Logged out successfully." << std::endl;
    } else {
        std::cout << "  ✗ Logout failed." << std::endl;
    }
}

void example_debug_info(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── DEBUG INFO ──────────────────────────────────────────" << std::endl;
    auto info = dexterauthapp.debugInfo();
    for (auto const& pair : info) {
        const std::string& key = pair.first;
        const std::string& val = pair.second;
        int spaces = 20 - (int)key.length();
        if (spaces < 1) spaces = 1;
        std::cout << "  " << key << std::string(spaces, ' ') << " : " << val << std::endl;
    }
}

void example_hwid(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── HWID METHODS ────────────────────────────────────────" << std::endl;
    std::cout << "  windows_user (SID)  : " << DexterAuth::Others::get_hwid("windows_user") << std::endl;
    std::cout << "  machine (registry)  : " << DexterAuth::Others::get_hwid("machine") << std::endl;
}

void example_account_details(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── ACCOUNT DETAILS ──────────────────────────────────────" << std::endl;
    if (dexterauthapp.user_data.username.empty()) {
        std::cout << "  Not logged in." << std::endl;
        return;
    }

    std::cout << "  Username       : " << dexterauthapp.user_data.username << std::endl;
    std::cout << "  HWID Bound     : " << dexterauthapp.user_data.hwid << std::endl;
    std::cout << "  Subscription   : " << (dexterauthapp.user_data.subscription.empty() ? "N/A" : dexterauthapp.user_data.subscription) << std::endl;
    std::cout << "  Expires        : " << (dexterauthapp.user_data.expires.empty() ? "N/A" : dexterauthapp.user_data.expires) << std::endl;
    std::cout << "  Last Login     : " << (dexterauthapp.user_data.lastlogin.empty() ? "N/A" : dexterauthapp.user_data.lastlogin) << std::endl;
    std::cout << "  Created At     : " << (dexterauthapp.user_data.createdate.empty() ? "N/A" : dexterauthapp.user_data.createdate) << std::endl;

    if (!dexterauthapp.user_data.subscriptions.empty()) {
        std::cout << "  All Subs       : [";
        for (size_t i = 0; i < dexterauthapp.user_data.subscriptions.size(); ++i) {
            std::cout << "{'" << dexterauthapp.user_data.subscriptions[i].subscription << "', expiry: '" << dexterauthapp.user_data.subscriptions[i].expiry << "'}";
            if (i + 1 < dexterauthapp.user_data.subscriptions.size()) {
                std::cout << ", ";
            }
        }
        std::cout << "]" << std::endl;
    }
}

void example_check_updates(DexterAuth::Api& dexterauthapp) {
    std::cout << "\n── CHECK & INSTALL UPDATES ─────────────────────────────" << std::endl;
    std::cout << "  Current Version : " << dexterauthapp.version << std::endl;
    std::cout << "  Checking server for latest release..." << std::endl;

    DexterAuth::UpdateInfo info = dexterauthapp.check_for_updates();
    std::cout << "  Latest Version  : " << (info.latest_version.empty() ? "Unknown" : info.latest_version) << std::endl;

    if (info.update_available) {
        std::cout << "  ✓ Update Available! (v" << info.current_version << " → v" << info.latest_version << ")" << std::endl;
        std::cout << "  Download URL    : " << info.download_url << std::endl;
        std::string choice = get_input("\n  Install update now? (y/N): ");
        if (choice == "y" || choice == "Y") {
            dexterauthapp.perform_update(info);
        } else {
            std::cout << "  Update deferred by user." << std::endl;
        }
    } else {
        std::cout << "  ✓ You are running the latest version." << std::endl;
    }
}

int main() {
#ifdef _WIN32
    SetConsoleOutputCP(CP_UTF8);
    SetConsoleCP(CP_UTF8);
#endif
    std::cout << "Initialising DexterAuth security..." << std::endl;
    
    DexterAuth::Api dexterauthapp(
        name,
        ownerid,
        version,
        secret,
        "", // hash_to_check
        url // api_url
    );

    // Optional host locking whitelist
    dexterauthapp.set_allowed_hosts({ "dexterauth.onrender.com" });

    // Optional TLS Certificate Pinning (Chain-Aware)
    // Cloudflare / Google Trust Services (GTS Root R1)
    dexterauthapp.add_pinned_cert("d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf");
    // Baltimore CyberTrust Root (Cloudflare legacy fallback)
    dexterauthapp.add_pinned_cert("16af57a9f676b0ab126095aa5ebafc57b8c71b6ab4945d81b85bbd13c77148a0");

    std::cout << "✓ Initialised in " << (secret.empty() ? "OFF" : "SECURE") << " mode." << std::endl;
    std::cout << "  HWID Method : " << dexterauthapp.hwid_method << std::endl;
    std::cout << "  HWID        : " << DexterAuth::Others::get_hwid(dexterauthapp.hwid_method) << std::endl;
    std::cout << "  Hash        : " << dexterauthapp.hash_to_check.substr(0, 24) << "..." << std::endl;
    std::cout << std::endl;
    
    get_input("  Press Enter to continue...");

    bool logged_in = false;

    while (true) {
        if (logged_in && !dexterauthapp.user_data.is_authenticated) {
            std::cout << "\n[SECURITY] Session revoked or account banned at runtime. Logging out..." << std::endl;
            dexterauthapp.stop_ban_monitor();
            logged_in = false;
            get_input("\n  Press Enter to continue...");
            continue;
        }

        banner(dexterauthapp);

        std::string choice;
        if (logged_in) {
            choice = menu_account();
            if (choice == "1") {
                example_account_details(dexterauthapp);
            } else if (choice == "2") {
                example_change_username(dexterauthapp);
            } else if (choice == "3") {
                example_upgrade(dexterauthapp);
            } else if (choice == "4") {
                example_verify_session(dexterauthapp);
            } else if (choice == "5") {
                example_logout(dexterauthapp);
                logged_in = false;
            } else if (choice == "0") {
                break;
            }
        } else {
            choice = menu_main();
            if (choice == "1") {
                logged_in = example_login(dexterauthapp);
            } else if (choice == "2") {
                example_register(dexterauthapp);
            } else if (choice == "3") {
                logged_in = example_web_login(dexterauthapp);
            } else if (choice == "4") {
                example_register_web(dexterauthapp);
            } else if (choice == "5") {
                example_forgot_password(dexterauthapp);
            } else if (choice == "6") {
                example_verify_token(dexterauthapp);
            } else if (choice == "7") {
                example_hwid(dexterauthapp);
            } else if (choice == "8") {
                example_debug_info(dexterauthapp);
            } else if (choice == "9") {
                example_check_updates(dexterauthapp);
            } else if (choice == "0") {
                std::cout << "\nGoodbye." << std::endl;
                break;
            }
        }

        if (choice != "0") {
            get_input("\n  Press Enter to continue...");
        }
    }

    return 0;
}
