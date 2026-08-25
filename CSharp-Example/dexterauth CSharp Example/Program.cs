using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using DexterAuth;

namespace DexterAuth_CSharp_Example
{
    internal class Program
    {
        public static api DexterAuthApp = new api(
    name: "fakelag", 
    ownerid: "0980ac9c-586d-44d0-8719-3dd9fbcec374",
    secret: "744d4489c4edeea10be71f7f92e94cd0103ca2b44b3cb24d975304288e7454ad",
    version: "1.3"
);

        private static void ClearConsole()
        {
            Console.Clear();
        }

        private static string GetInput(string prompt)
        {
            Console.Write(prompt);
            string val = Console.ReadLine();
            return val != null ? val.Trim() : "";
        }

        private static void ShowBanner(api dexterauthapp)
        {
            ClearConsole();
            string mode = string.IsNullOrEmpty(dexterauthapp.client_secret) ? "OFF ⚠" : "SECURE 🔒";
            Console.WriteLine("╔" + new string('=', 58) + "╗");

            string title = "║  " + dexterauthapp.name + "  —  Powered by DexterAuth";
            if (title.Length < 60)
            {
                title += new string(' ', 60 - title.Length);
            }
            title += "║";
            Console.WriteLine(title);

            string modeLine = "║  Anti-Tamper: " + mode;
            if (modeLine.Length < 60)
            {
                modeLine += new string(' ', 60 - modeLine.Length);
            }
            modeLine += "║";
            Console.WriteLine(modeLine);

            Console.WriteLine("╚" + new string('=', 58) + "╝");

            if (dexterauthapp.user_data.is_authenticated && !string.IsNullOrEmpty(dexterauthapp.user_data.username))
            {
                Console.WriteLine($"\n  Logged in as: {dexterauthapp.user_data.username}");
            }
        }

        private static string ShowMainMenu()
        {
            Console.WriteLine("\n  MAIN MENU");
            Console.WriteLine("  " + new string('-', 53));
            Console.WriteLine("  [1]  Login");
            Console.WriteLine("  [2]  Register with License Key");
            Console.WriteLine("  [3]  Web Login  (no HWID)");
            Console.WriteLine("  [4]  Web Register  (no HWID)");
            Console.WriteLine("  [5]  Forgot Password  (HWID-verified reset)");
            Console.WriteLine("  [6]  Verify Standalone API Token");
            Console.WriteLine("  [7]  Show HWID methods");
            Console.WriteLine("  [8]  Debug Info");
            Console.WriteLine("  [9]  Check & Install Updates");
            Console.WriteLine("  [0]  Exit");
            return GetInput("\n  › ");
        }

        private static string ShowAccountMenu()
        {
            Console.WriteLine("\n  ACCOUNT MENU");
            Console.WriteLine("  " + new string('-', 53));
            Console.WriteLine("  [1]  Account Details  (view info & expiry)");
            Console.WriteLine("  [2]  Change Username");
            Console.WriteLine("  [3]  Upgrade Account  (apply another license key)");
            Console.WriteLine("  [4]  Verify Session");
            Console.WriteLine("  [5]  Logout");
            Console.WriteLine("  [0]  Back");
            return GetInput("\n  › ");
        }

        private static bool ExampleLogin(api dexterauthapp)
        {
            Console.WriteLine("\n── LOGIN ──────────────────────────────────────────────");
            string user = GetInput("  Username : ");
            string password = GetInput("  Password : ");

            if (dexterauthapp.login(user, password))
            {
                Console.WriteLine($"\n  ✓ Logged in as '{dexterauthapp.user_data.username}'");
                Console.WriteLine($"  Subscription : {(string.IsNullOrEmpty(dexterauthapp.user_data.subscription) ? "N/A" : dexterauthapp.user_data.subscription)}");
                Console.WriteLine($"  Expires      : {(string.IsNullOrEmpty(dexterauthapp.user_data.expires) ? "N/A" : dexterauthapp.user_data.expires)}");
                Console.WriteLine($"  Last Login   : {(string.IsNullOrEmpty(dexterauthapp.user_data.lastlogin) ? "N/A" : dexterauthapp.user_data.lastlogin)}");

                double remaining = dexterauthapp.expiry_remaining();
                if (remaining > 0.0)
                {
                    int d = (int)(remaining / 86400.0);
                    int h = (int)((remaining % 86400.0) / 3600.0);
                    Console.WriteLine($"  Time left    : {d}d {h}h");
                }
                else
                {
                    Console.WriteLine("  Time left    : EXPIRED");
                }

                dexterauthapp.start_ban_monitor(120);
                Console.WriteLine("  Ban monitor  : Active (120s interval)");
                return true;
            }

            Console.WriteLine("  ✗ Login failed.");
            return false;
        }

        private static void ExampleRegister(api dexterauthapp)
        {
            Console.WriteLine("\n── REGISTER ────────────────────────────────────────────");
            string user = GetInput("  Username    : ");
            string email = GetInput("  Email       : ");
            string password = GetInput("  Password    : ");
            string key = GetInput("  License Key : ");

            if (dexterauthapp.registerAccount(user, email, password, key))
            {
                Console.WriteLine("  ✓ Registered! You can now log in.");
            }
            else
            {
                Console.WriteLine("  ✗ Registration failed. Check the license key.");
            }
        }

        private static bool ExampleWebLogin(api dexterauthapp)
        {
            Console.WriteLine("\n── WEB LOGIN (no HWID) ─────────────────────────────────");
            string user = GetInput("  Username : ");
            string password = GetInput("  Password : ");

            if (dexterauthapp.webLogin(user, password))
            {
                Console.WriteLine($"\n  ✓ Authenticated as '{dexterauthapp.user_data.username}'");
                Console.WriteLine($"  Subscription : {(string.IsNullOrEmpty(dexterauthapp.user_data.subscription) ? "N/A" : dexterauthapp.user_data.subscription)}");
                Console.WriteLine($"  Expires      : {(string.IsNullOrEmpty(dexterauthapp.user_data.expires) ? "N/A" : dexterauthapp.user_data.expires)}");
                Console.WriteLine($"  Last Login   : {(string.IsNullOrEmpty(dexterauthapp.user_data.lastlogin) ? "N/A" : dexterauthapp.user_data.lastlogin)}");

                dexterauthapp.start_ban_monitor(120);
                Console.WriteLine("  Ban monitor  : Active (120s interval)");
                return true;
            }
            else
            {
                if (dexterauthapp.lockout_active())
                {
                    long secs = dexterauthapp.lockout_remaining_ms() / 1000;
                    Console.WriteLine($"  ✗ Locked out for {secs} seconds.");
                }
                else
                {
                    Console.WriteLine("  ✗ Web login failed.");
                }
                return false;
            }
        }

        private static void ExampleRegisterWeb(api dexterauthapp)
        {
            Console.WriteLine("\n── WEB REGISTER (no HWID) ──────────────────────────────");
            string user = GetInput("  Username    : ");
            string email = GetInput("  Email       : ");
            string password = GetInput("  Password    : ");
            string key = GetInput("  License Key : ");

            if (dexterauthapp.registerWeb(user, email, password, key))
            {
                Console.WriteLine("  ✓ Registered via web flow!");
            }
            else
            {
                Console.WriteLine("  ✗ Registration failed.");
            }
        }

        private static void ExampleUpgrade(api dexterauthapp)
        {
            Console.WriteLine("\n── UPGRADE ACCOUNT ─────────────────────────────────────");
            string user = GetInput("  Username    : ");
            if (string.IsNullOrEmpty(user))
            {
                user = dexterauthapp.user_data.username;
            }
            string key = GetInput("  License Key : ");

            if (dexterauthapp.upgrade(user, key))
            {
                Console.WriteLine("  ✓ Account upgraded!");
            }
            else
            {
                Console.WriteLine("  ✗ Upgrade failed. Check the license key.");
            }
        }

        private static void ExampleChangeUsername(api dexterauthapp)
        {
            Console.WriteLine("\n── CHANGE USERNAME ─────────────────────────────────────");
            string newName = GetInput("  New Username : ");

            if (dexterauthapp.changeUsername(newName))
            {
                Console.WriteLine($"  ✓ Username changed to '{dexterauthapp.user_data.username}'");
            }
            else
            {
                Console.WriteLine("  ✗ Username change failed.");
            }
        }

        private static void ExampleForgotPassword(api dexterauthapp)
        {
            Console.WriteLine("\n── FORGOT PASSWORD (HWID-verified reset) ───────────────");
            Console.WriteLine("  Your current Hardware ID will be used to verify your identity.");
            string user = GetInput("  Username     : ");
            string newPass = GetInput("  New Password : ");

            string hwid = Others.GetHWID(dexterauthapp.hwid_method);
            string displayHwid = hwid.Length > 20 ? hwid.Substring(0, 20) + "..." : hwid;
            Console.WriteLine($"  Using HWID   : {displayHwid}");

            if (dexterauthapp.forgot(user, newPass, hwid))
            {
                Console.WriteLine("  ✓ Password reset! You can now log in with your new password.");
            }
            else
            {
                Console.WriteLine("  ✗ Reset failed. Is this HWID bound to the account?");
            }
        }

        private static void ExampleVerifySession(api dexterauthapp)
        {
            Console.WriteLine("\n── VERIFY SESSION ──────────────────────────────────────");
            if (string.IsNullOrEmpty(dexterauthapp.session_token))
            {
                Console.WriteLine("  Not logged in.");
                return;
            }
            if (dexterauthapp.check())
            {
                Console.WriteLine("  ✓ Session is valid.");
            }
            else
            {
                Console.WriteLine("  ✗ Session has expired or been revoked.");
            }
        }

        private static void ExampleVerifyToken(api dexterauthapp)
        {
            Console.WriteLine("\n── VERIFY STANDALONE TOKEN ─────────────────────────────");
            string token = GetInput("  Token : ");

            if (dexterauthapp.verifyToken(token))
            {
                Console.WriteLine("  ✓ Token is valid.");
            }
            else
            {
                Console.WriteLine("  ✗ Token is invalid or banned.");
            }
        }

        private static void ExampleLogout(api dexterauthapp)
        {
            Console.WriteLine("\n── LOGOUT ──────────────────────────────────────────────");
            dexterauthapp.stop_ban_monitor();
            if (dexterauthapp.logout())
            {
                Console.WriteLine("  ✓ Logged out successfully.");
            }
            else
            {
                Console.WriteLine("  ✗ Logout failed.");
            }
        }

        private static void ExampleDebugInfo(api dexterauthapp)
        {
            Console.WriteLine("\n── DEBUG INFO ──────────────────────────────────────────");
            var info = dexterauthapp.debugInfo();
            foreach (var pair in info)
            {
                string key = pair.Key;
                string val = pair.Value;
                int spaces = 20 - key.Length;
                if (spaces < 1) spaces = 1;
                Console.WriteLine($"  {key}{new string(' ', spaces)} : {val}");
            }
        }

        private static void ExampleHwid(api dexterauthapp)
        {
            Console.WriteLine("\n── HWID METHODS ────────────────────────────────────────");
            Console.WriteLine($"  windows_user (SID)  : {Others.GetHWID("windows_user")}");
            Console.WriteLine($"  machine (registry)  : {Others.GetHWID("machine")}");
        }

        private static void ExampleAccountDetails(api dexterauthapp)
        {
            Console.WriteLine("\n── ACCOUNT DETAILS ──────────────────────────────────────");
            if (string.IsNullOrEmpty(dexterauthapp.user_data.username))
            {
                Console.WriteLine("  Not logged in.");
                return;
            }

            Console.WriteLine($"  Username       : {dexterauthapp.user_data.username}");
            Console.WriteLine($"  HWID Bound     : {dexterauthapp.user_data.hwid}");
            Console.WriteLine($"  Subscription   : {(string.IsNullOrEmpty(dexterauthapp.user_data.subscription) ? "N/A" : dexterauthapp.user_data.subscription)}");
            Console.WriteLine($"  Expires        : {(string.IsNullOrEmpty(dexterauthapp.user_data.expires) ? "N/A" : dexterauthapp.user_data.expires)}");
            Console.WriteLine($"  Last Login     : {(string.IsNullOrEmpty(dexterauthapp.user_data.lastlogin) ? "N/A" : dexterauthapp.user_data.lastlogin)}");
            Console.WriteLine($"  Created At     : {(string.IsNullOrEmpty(dexterauthapp.user_data.createdate) ? "N/A" : dexterauthapp.user_data.createdate)}");

            if (dexterauthapp.user_data.subscriptions.Count > 0)
            {
                Console.Write("  All Subs       : [");
                for (int i = 0; i < dexterauthapp.user_data.subscriptions.Count; ++i)
                {
                    Console.Write($"{{'{dexterauthapp.user_data.subscriptions[i].subscription}', expiry: '{dexterauthapp.user_data.subscriptions[i].expiry}'}}");
                    if (i + 1 < dexterauthapp.user_data.subscriptions.Count)
                    {
                        Console.Write(", ");
                    }
                }
                Console.WriteLine("]");
            }
        }

        private static void ExampleCheckUpdates(api dexterauthapp)
        {
            Console.WriteLine("\n── CHECK & INSTALL UPDATES ─────────────────────────────");
            Console.WriteLine($"  Current Version : {dexterauthapp.version}");
            Console.WriteLine("  Checking server for latest release...");

            UpdateInfo info = dexterauthapp.CheckForUpdates();
            Console.WriteLine($"  Latest Version  : {(string.IsNullOrEmpty(info.latest_version) ? "Unknown" : info.latest_version)}");

            if (info.update_available)
            {
                Console.WriteLine($"  ✓ Update Available! (v{info.current_version} → v{info.latest_version})");
                Console.WriteLine($"  Download URL    : {info.download_url}");
                string choice = GetInput("\n  Install update now? (y/N): ");
                if (choice.Equals("y", StringComparison.OrdinalIgnoreCase))
                {
                    dexterauthapp.PerformUpdate(info);
                }
                else
                {
                    Console.WriteLine("  Update deferred by user.");
                }
            }
            else
            {
                Console.WriteLine("  ✓ You are running the latest version.");
            }
        }

        static void Main(string[] args)
        {
            // Set console output to support unicode borders and emojis
            Console.OutputEncoding = Encoding.UTF8;

            Console.WriteLine("Initialising DexterAuth security...");

            // Initialize structured logger
            Logger.Instance.Init("logs/sdk.log", true, LogLevel.DebugLevel);

            api dexterauthapp = DexterAuthApp;

            // Configure allowed hosts locking whitelist
            dexterauthapp.set_allowed_hosts(new List<string> { "dexterauth.onrender.com" });

            // Optional TLS Certificate Pinning (Chain-Aware)
            // Cloudflare / Google Trust Services (GTS Root R1)
            // dexterauthapp.add_pinned_cert("d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf");
            // Baltimore CyberTrust Root (Cloudflare legacy fallback)
            // dexterauthapp.add_pinned_cert("16af57a9f676b0ab126095aa5ebafc57b8c71b6ab4945d81b85bbd13c77148a0");

            Console.WriteLine($"✓ Initialised in {(string.IsNullOrEmpty(dexterauthapp.client_secret) ? "OFF" : "SECURE")} mode.");
            Console.WriteLine($"  HWID Method : {dexterauthapp.hwid_method}");
            Console.WriteLine($"  HWID        : {Others.GetHWID(dexterauthapp.hwid_method)}");
            string safeHash = dexterauthapp.hash_to_check.Length > 24 ? dexterauthapp.hash_to_check.Substring(0, 24) + "..." : dexterauthapp.hash_to_check;
            Console.WriteLine($"  Hash        : {safeHash}");
            Console.WriteLine();

            GetInput("  Press Enter to continue...");

            bool loggedIn = false;

            while (true)
            {
                ShowBanner(dexterauthapp);

                if (!loggedIn)
                {
                    string opt = ShowMainMenu();
                    if (opt == "1")
                    {
                        if (ExampleLogin(dexterauthapp))
                        {
                            loggedIn = true;
                        }
                    }
                    else if (opt == "2")
                    {
                        ExampleRegister(dexterauthapp);
                    }
                    else if (opt == "3")
                    {
                        if (ExampleWebLogin(dexterauthapp))
                        {
                            loggedIn = true;
                        }
                    }
                    else if (opt == "4")
                    {
                        ExampleRegisterWeb(dexterauthapp);
                    }
                    else if (opt == "5")
                    {
                        ExampleForgotPassword(dexterauthapp);
                    }
                    else if (opt == "6")
                    {
                        ExampleVerifyToken(dexterauthapp);
                    }
                    else if (opt == "7")
                    {
                        ExampleHwid(dexterauthapp);
                    }
                    else if (opt == "8")
                    {
                        ExampleDebugInfo(dexterauthapp);
                    }
                    else if (opt == "9")
                    {
                        ExampleCheckUpdates(dexterauthapp);
                    }
                    else if (opt == "0")
                    {
                        break;
                    }
                    else
                    {
                        Console.WriteLine("  Invalid option.");
                    }
                    GetInput("\n  Press Enter to return...");
                }
                else
                {
                    string opt = ShowAccountMenu();
                    if (opt == "1")
                    {
                        ExampleAccountDetails(dexterauthapp);
                    }
                    else if (opt == "2")
                    {
                        ExampleChangeUsername(dexterauthapp);
                    }
                    else if (opt == "3")
                    {
                        ExampleUpgrade(dexterauthapp);
                    }
                    else if (opt == "4")
                    {
                        ExampleVerifySession(dexterauthapp);
                    }
                    else if (opt == "5")
                    {
                        ExampleLogout(dexterauthapp);
                        loggedIn = false;
                    }
                    else if (opt == "0")
                    {
                        loggedIn = false;
                    }
                    else
                    {
                        Console.WriteLine("  Invalid option.");
                    }
                    GetInput("\n  Press Enter to return...");
                }
            }

            Console.WriteLine("Exiting...");
        }
    }
}
