package examples;

import com.dexterauth.sdk.api;
import com.dexterauth.sdk.Callback;
import com.dexterauth.sdk.models.ApiResponse;
import com.dexterauth.sdk.models.UserData;

public class BasicExample {

    public static void main(String[] args) {
        // 1. Instantiate the SDK
        api authApp = new api(
            "YOUR_APP_NAME",
            "YOUR_OWNER_ID",
            "YOUR_APP_SECRET",
            "1.0",
            "https://dexterauth.onrender.com/api/v2/client/"
        );

        // 2. Initialize
        System.out.println("[*] Initializing DexterAuth...");
        authApp.initAsync(new Callback<ApiResponse>() {
            @Override
            public void onSuccess(ApiResponse result) {
                System.out.println("[+] Initialized successfully: " + result.getMessage());

                // 3. Authenticate with License Key
                String licenseKey = "XXXXX-XXXXX-XXXXX";
                authApp.licenseAsync(licenseKey, new Callback<ApiResponse>() {
                    @Override
                    public void onSuccess(ApiResponse authResult) {
                        UserData user = authApp.user_data;
                        System.out.println("[+] Login Successful!");
                        System.out.println("    Username:     " + user.getUsername());
                        System.out.println("    HWID:         " + user.getHwid());
                        System.out.println("    Subscription: " + user.getSubscription());
                        System.out.println("    Expires:      " + user.getExpires());
                    }

                    @Override
                    public void onError(String error) {
                        System.err.println("[-] License Authentication Failed: " + error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                System.err.println("[-] Initialization Failed: " + error);
            }
        });
    }
}
