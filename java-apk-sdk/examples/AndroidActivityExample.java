package examples;

import com.dexterauth.sdk.api;
import com.dexterauth.sdk.Callback;
import com.dexterauth.sdk.models.ApiResponse;
import com.dexterauth.sdk.security.SecureVault;

/*
// On Android, use inside an Activity:
public class LoginActivity extends android.app.Activity {

    // Globally accessible DexterAuth client instance (using SecureVault to hide credentials)
    public static api DexterAuthApp = new api(
        SecureVault.getAppName(),
        SecureVault.getOwnerId(),
        SecureVault.getAppSecret(),
        SecureVault.getAppVersion(),
        SecureVault.getApiUrl()
    );

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind Context for automatic Android HWID extraction
        DexterAuthApp.setContext(this);

        // Initialize SDK asynchronously
        DexterAuthApp.initAsync(new Callback<ApiResponse>() {
            @Override
            public void onSuccess(ApiResponse result) {
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(LoginActivity.this, "Connected to DexterAuth", android.widget.Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(LoginActivity.this, "DexterAuth Init Error: " + error, android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public void onLoginButtonClicked(String licenseKey) {
        DexterAuthApp.licenseAsync(licenseKey, new Callback<ApiResponse>() {
            @Override
            public void onSuccess(ApiResponse result) {
                runOnUiThread(() -> {
                    // Navigate to main application menu
                    // startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    // finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(LoginActivity.this, error, android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
*/
