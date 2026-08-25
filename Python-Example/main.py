"""
DexterAuth Python SDK — Complete Feature Example
=============================================

Demonstrates every SDK feature including all three Anti-Tamper modes.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ANTI-TAMPER SETUP (read this once)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

  SECURE MODE (recommended — no manual whitelisting EVER needed):
  ───────────────────────────────────────────────────────────────
  1. Copy your Client Secret from:
       DexterAuth Dashboard → Select App → App Info → Client Secret
  2. Paste it in APP_CLIENT_SECRET below.
  3. Compile your app with PyInstaller.
  4. Ship it. The first user login automatically registers the hash.
  5. After EVERY new build, same thing — auto-registered. No dashboard work.

  OFF MODE (no hash protection — for languages that can't protect secrets):
  ─────────────────────────────────────────────────────────────────────────
  Leave APP_CLIENT_SECRET = None.
  Auth is still protected by password bcrypt, HWID, and session tokens.

  PRODUCTION HARDENING:
  ─────────────────────
  pip install pyarmor pyinstaller
  pyarmor gen main.py          # encrypts bytecode → dist/main.py
  pyinstaller --onefile dist/main.py   # compiles → dist/main.exe

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  WINDOWS SETUP
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  pip install pywin32
  python -m pywin32_postinstall -install
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"""

import os
import sys
from dexterauth import api, others

# ═══════════════════════════════════════════════════════════════════
#   CONFIGURATION — fill in from DexterAuth Dashboard → App Info
# ═══════════════════════════════════════════════════════════════════
APP_NAME    = "MyApp"
APP_ID      = "YOUR-APP-UUID-HERE"         # ← App UUID from Dashboard
APP_VERSION = "1.0"

# ► SECURE MODE:  paste your Client Secret here
# ► OFF MODE:     leave as None
APP_CLIENT_SECRET = "YOUR-CLIENT-SECRET-HERE"   # ← Client Secret from Dashboard

# ► Development override: set to "dev-skip" only while actively editing code.
#   Remove (set to None) before compiling your final .exe for users.
HASH_OVERRIDE = None    # None = auto-compute real SHA-256 of this binary

# ═══════════════════════════════════════════════════════════════════
#   SDK INITIALISATION EXAMPLES
# ═══════════════════════════════════════════════════════════════════

# ── Example A: SECURE MODE (production recommended) ─────────────────────────
#   Automatically sends HMAC-signed hash on every login/register.
#   No manual hash whitelisting ever — even after new builds.
#   Backend blocks: wrong secret, expired timestamps, replay attacks.
#
#   dexterauthapp = api(
#       name          = APP_NAME,
#       ownerid       = APP_ID,
#       version       = APP_VERSION,
#       client_secret = APP_CLIENT_SECRET,
#   )

# ── Example B: OFF MODE (no hash protection, developer opts out) ─────────────
#   Hash field is sent but backend ignores it (no HMAC to verify with).
#   No manual whitelisting needed either.
#   Auth still protected by bcrypt password + HWID + session tokens.
#
#   dexterauthapp = api(
#       name    = APP_NAME,
#       ownerid = APP_ID,
#       version = APP_VERSION,
#       # client_secret omitted
#   )

# ── Example C: Development override (local testing, no hash check) ───────────
#   Pass a fixed string so the hash doesn't change on every file save.
#   Must also disable hash_check in your dashboard during development.
#   NEVER ship with hash_to_check="dev-skip" in production.
#
#   dexterauthapp = api(
#       name          = APP_NAME,
#       ownerid       = APP_ID,
#       version       = APP_VERSION,
#       client_secret = APP_CLIENT_SECRET,
#       hash_to_check = "dev-skip",
#   )

# ── Example D: Custom API URL (self-hosted backend) ──────────────────────────
#
#   dexterauthapp = api(
#       name          = APP_NAME,
#       ownerid       = APP_ID,
#       version       = APP_VERSION,
#       client_secret = APP_CLIENT_SECRET,
#       api_url       = "https://your-own-server.com/api/v2/client",
#   )

# ═══════════════════════════════════════════════════════════════════
#   ACTIVE INITIALISATION (change APP_CLIENT_SECRET above to switch mode)
# ═══════════════════════════════════════════════════════════════════

def clear():
    os.system("cls" if os.name == "nt" else "clear")


def init_sdk() -> api:
    """Initialise the SDK based on the configuration above."""
    print("Initialising DexterAuth security…")

    dexterauthapp = api(
        name          = APP_NAME,
        ownerid       = APP_ID,
        version       = APP_VERSION,
        client_secret = APP_CLIENT_SECRET,   # None = OFF mode, str = SECURE mode
        hash_to_check = HASH_OVERRIDE,       # None = auto-compute (production)
    )

    # Restrict all HTTP to the DexterAuth domain (host-locking anti-MITM)
    dexterauthapp.set_allowed_hosts(["dexterauth.onrender.com"])

    # Optional TLS Certificate Pinning (chain-aware)
    # Cloudflare / Google Trust Services (GTS Root R1)
    dexterauthapp.add_pinned_cert("d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf")
    # Baltimore CyberTrust Root (Cloudflare legacy fallback)
    dexterauthapp.add_pinned_cert("16af57a9f676b0ab126095aa5ebafc57b8c71b6ab4945d81b85bbd13c77148a0")

    # If APP_CLIENT_SECRET is missing, it falls back to OFF MODE (no hash check).
    mode = "SECURE (HMAC + TOFU Hash)" if APP_CLIENT_SECRET else "OFF (no hash check)"
    print(f"✓ Initialised in {mode} mode.")
    print(f"  HWID Method : {dexterauthapp.hwid_method}")
    print(f"  HWID        : {others.get_hwid(method=dexterauthapp.hwid_method)}")
    print(f"  Hash        : {dexterauthapp.hash_to_check[:24]}…")
    print()

    return dexterauthapp


# ═══════════════════════════════════════════════════════════════════
#   FEATURE EXAMPLES
# ═══════════════════════════════════════════════════════════════════

def example_login(dexterauthapp: api) -> bool:
    """
    Example: Login with username + password.

    The SDK automatically:
      • Detects the HWID using the method from the dashboard.
      • Computes the SHA-256 hash of this file/exe.
      • Mode 1: SECURE (Recommended)
      • Provide `APP_CLIENT_SECRET`.
      • SDK sends: file hash + HMAC signature + timestamp + nonce.
      • Backend verifies HMAC. If valid, the FIRST hash is auto-whitelisted (Trust On First Use).
      • IMPORTANT: For all future builds/updates, you MUST click "Reset All Hashes" in dashboard!
    """
    print("\n── LOGIN ──────────────────────────────────────────────")
    user     = input("  Username : ").strip()
    password = input("  Password : ").strip()

    # You can also pass hwid manually:
    # hwid = others.get_hwid(method="windows_user")
    # dexterauthapp.login(user, password, hwid=hwid)

    if dexterauthapp.login(user, password):
        print(f"\n  ✓ Logged in as '{dexterauthapp.user_data.username}'")
        print(f"  Subscription : {dexterauthapp.user_data.subscription or 'N/A'}")
        print(f"  Expires      : {dexterauthapp.user_data.expires or 'N/A'}")

        remaining = dexterauthapp.expiry_remaining()
        if remaining > 0:
            d, h = int(remaining // 86400), int((remaining % 86400) // 3600)
            print(f"  Time left    : {d}d {h}h")
        else:
            print("  Time left    : EXPIRED")

        # Start ban monitor — terminates process if admin bans the user mid-session
        dexterauthapp.start_ban_monitor(interval_seconds=120)
        print("  Ban monitor  : Active (120s interval)")
        return True

    print("  ✗ Login failed.")
    return False


def example_register(dexterauthapp: api):
    """
    Example: Register a new account with a license key.

    Hash + HMAC are sent automatically — same as login.
    """
    print("\n── REGISTER ────────────────────────────────────────────")
    user     = input("  Username    : ").strip()
    email    = input("  Email       : ").strip()
    password = input("  Password    : ").strip()
    key      = input("  License Key : ").strip()

    if dexterauthapp.register(user, email, password, key):
        print("  ✓ Registered! You can now log in.")
    else:
        print("  ✗ Registration failed. Check the license key.")


def example_web_login(dexterauthapp: api):
    """
    Example: Login without HWID binding.
    Includes client-side brute-force lockout (3 fails → 5 min wait).
    Hash is NOT sent in web-login (no anti-tamper for web flows).
    """
    print("\n── WEB LOGIN (no HWID) ─────────────────────────────────")
    user     = input("  Username : ").strip()
    password = input("  Password : ").strip()

    if dexterauthapp.web_login(user, password):
        print(f"  ✓ Authenticated as '{dexterauthapp.user_data.username}'")
        print(f"  Subscription : {dexterauthapp.user_data.subscription or 'N/A'}")
    else:
        if dexterauthapp.lockout_active():
            secs = dexterauthapp.lockout_remaining_ms() // 1000
            print(f"  ✗ Locked out for {secs} seconds.")
        else:
            print("  ✗ Web login failed.")


def example_register_web(dexterauthapp: api):
    """
    Example: Register without a HWID (web-flow registration).
    """
    print("\n── WEB REGISTER (no HWID) ──────────────────────────────")
    user     = input("  Username    : ").strip()
    email    = input("  Email       : ").strip()
    password = input("  Password    : ").strip()
    key      = input("  License Key : ").strip()

    if dexterauthapp.register_web(user, email, password, key):
        print("  ✓ Registered via web flow!")
    else:
        print("  ✗ Registration failed.")


def example_upgrade(dexterauthapp: api):
    """
    Example: Extend an account's subscription with another license key.
    Does NOT require a new login — can be called while logged in.
    """
    print("\n── UPGRADE ACCOUNT ─────────────────────────────────────")
    user = input("  Username    : ").strip() or dexterauthapp.user_data.username
    key  = input("  License Key : ").strip()

    if dexterauthapp.upgrade(user, key):
        print("  ✓ Account upgraded!")
    else:
        print("  ✗ Upgrade failed. Check the license key.")


def example_change_username(dexterauthapp: api):
    """
    Example: Change the logged-in user's username.
    Requires an active login session.
    """
    print("\n── CHANGE USERNAME ─────────────────────────────────────")
    new_name = input("  New Username : ").strip()

    if dexterauthapp.changeUsername(new_name):
        print(f"  ✓ Username changed to '{dexterauthapp.user_data.username}'")
    else:
        print("  ✗ Username change failed.")


def example_forgot_password(dexterauthapp: api):
    """
    Example: Reset a password using the account's bound Hardware ID.
    The server verifies the HWID matches what was stored when the account
    was first bound — so only the real user on their real machine can reset.
    """
    print("\n── FORGOT PASSWORD (HWID-verified reset) ───────────────")
    print("  Your current Hardware ID will be used to verify your identity.")
    user     = input("  Username     : ").strip()
    new_pass = input("  New Password : ").strip()

    hwid = others.get_hwid(method=dexterauthapp.hwid_method)
    print(f"  Using HWID   : {hwid[:20]}…")

    if dexterauthapp.forgot(user, new_pass, hwid=hwid):
        print("  ✓ Password reset! You can now log in with your new password.")
    else:
        print("  ✗ Reset failed. Is this HWID bound to the account?")


def example_verify_session(dexterauthapp: api):
    """
    Example: Manually check if the current session is still valid.
    Useful after network interruptions or before privileged operations.
    """
    print("\n── VERIFY SESSION ──────────────────────────────────────")
    if not dexterauthapp.session_token:
        print("  Not logged in.")
        return
    if dexterauthapp.check():
        print("  ✓ Session is valid.")
    else:
        print("  ✗ Session has expired or been revoked.")


def example_verify_token(dexterauthapp: api):
    """
    Example: Verify a standalone API token issued from the dashboard.
    This is separate from login session tokens — used for server-to-server
    or third-party integrations.
    """
    print("\n── VERIFY STANDALONE TOKEN ─────────────────────────────")
    token = input("  Token : ").strip()

    if dexterauthapp.verify_token(token):
        print("  ✓ Token is valid.")
    else:
        print("  ✗ Token is invalid or banned.")


def example_logout(dexterauthapp: api):
    """
    Example: Logout — invalidates session on server, clears local token.
    """
    print("\n── LOGOUT ──────────────────────────────────────────────")
    dexterauthapp.stop_ban_monitor()
    if dexterauthapp.logout():
        print("  ✓ Logged out successfully.")
    else:
        print("  ✗ Logout failed.")


def example_debug_info(dexterauthapp: api):
    """
    Example: Print SDK debug state snapshot.
    """
    print("\n── DEBUG INFO ──────────────────────────────────────────")
    info = dexterauthapp.debugInfo()
    for k, v in info.items():
        print(f"  {k:20} : {v}")


def example_hwid(dexterauthapp: api):
    """
    Example: Show all available HWID methods for this platform.
    """
    print("\n── HWID METHODS ────────────────────────────────────────")
    print(f"  windows_user (SID)  : {others.get_hwid('windows_user')}")
    print(f"  machine (registry)  : {others.get_hwid('machine')}")


def example_account_details(dexterauthapp: api):
    """
    Example: Display current user's account details.
    """
    print("\n── ACCOUNT DETAILS ──────────────────────────────────────")
    user = dexterauthapp.user_data
    if not user.username:
        print("  Not logged in.")
        return
        
    print(f"  Username       : {user.username}")
    print(f"  HWID Bound     : {user.hwid or 'N/A'}")
    print(f"  Subscription   : {user.subscription or 'N/A'}")
    print(f"  Expires        : {user.expires or 'N/A'}")
    print(f"  Last Login     : {user.lastlogin or 'N/A'}")
    print(f"  Created At     : {user.createdate or 'N/A'}")

    if user.subscriptions:
        print(f"  All Subs       : {user.subscriptions}")


def example_check_updates(dexterauthapp: api):
    """Example: Manually check for software updates and install them."""
    print("\n── CHECK & INSTALL UPDATES ─────────────────────────────")
    print(f"  Current Version : {dexterauthapp.version}")
    print("  Checking server for latest release...")

    info = dexterauthapp.check_for_updates()
    print(f"  Latest Version  : {info.latest_version or 'Unknown'}")

    if info.update_available:
        print(f"  ✓ Update Available! (v{info.current_version} → v{info.latest_version})")
        print(f"  Download URL    : {info.download_url}")
        choice = input("\n  Install update now? (y/N): ").strip().lower()
        if choice in ("y", "yes"):
            dexterauthapp.perform_update(info)
        else:
            print("  Update deferred by user.")
    else:
        print("  ✓ You are running the latest version.")

# ═══════════════════════════════════════════════════════════════════
#   MENUS
# ═══════════════════════════════════════════════════════════════════

def menu_main():
    print("\n  MAIN MENU")
    print("  ─────────────────────────────────────────────────────")
    print("  [1]  Login")
    print("  [2]  Register with License Key")
    print("  [3]  Web Login  (no HWID)")
    print("  [4]  Web Register  (no HWID)")
    print("  [5]  Forgot Password  (HWID-verified reset)")
    print("  [6]  Verify Standalone API Token")
    print("  [7]  Show HWID methods")
    print("  [8]  Debug Info")
    print("  [9]  Check & Install Updates")
    print("  [0]  Exit")
    return input("\n  › ").strip()


def menu_account():
    print("\n  ACCOUNT MENU")
    print("  ─────────────────────────────────────────────────────")
    print("  [1]  Account Details  (view info & expiry)")
    print("  [2]  Change Username")
    print("  [3]  Upgrade Account  (apply another license key)")
    print("  [4]  Verify Session")
    print("  [5]  Logout")
    print("  [0]  Back")
    return input("\n  › ").strip()


def banner(dexterauthapp: api):
    clear()
    mode = "SECURE 🔒" if APP_CLIENT_SECRET else "OFF ⚠"
    print("╔" + "═" * 58 + "╗")
    print(f"║  {APP_NAME}  —  Powered by DexterAuth".ljust(59) + "║")
    print(f"║  Anti-Tamper: {mode}".ljust(59) + "║")
    print("╚" + "═" * 58 + "╝")
    if dexterauthapp.user_data.username:
        print(f"\n  Logged in as: {dexterauthapp.user_data.username}")


# ═══════════════════════════════════════════════════════════════════
#   ENTRY POINT
# ═══════════════════════════════════════════════════════════════════

def main():
    dexterauthapp = init_sdk()
    logged_in = False

    while True:
        banner(dexterauthapp)

        if logged_in:
            choice = menu_account()
            if choice == "1":
                example_account_details(dexterauthapp)
            elif choice == "2":
                example_change_username(dexterauthapp)
            elif choice == "3":
                example_upgrade(dexterauthapp)
            elif choice == "4":
                example_verify_session(dexterauthapp)
            elif choice == "5":
                example_logout(dexterauthapp)
                logged_in = False
            elif choice == "0":
                break
        else:
            choice = menu_main()
            if choice == "1":
                logged_in = example_login(dexterauthapp)
            elif choice == "2":
                example_register(dexterauthapp)
            elif choice == "3":
                example_web_login(dexterauthapp)
            elif choice == "4":
                example_register_web(dexterauthapp)
            elif choice == "5":
                example_forgot_password(dexterauthapp)
            elif choice == "6":
                example_verify_token(dexterauthapp)
            elif choice == "7":
                example_hwid(dexterauthapp)
            elif choice == "8":
                example_debug_info(dexterauthapp)
            elif choice == "9":
                example_check_updates(dexterauthapp)
            elif choice == "0":
                print("\nGoodbye.")
                break

        if choice != "0":
            input("\n  Press Enter to continue…")


if __name__ == "__main__":
    main()
