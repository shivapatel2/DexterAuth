# DexterAuth — Branded SDKs

Production SDKs for **DexterAuth** — forked from [AuthLX](https://github.com/AuthLX) and rebranded for `dexterauth.onrender.com` + `dexterauthhh.vercel.app`.

- **Backend:** https://dexterauth.onrender.com
- **Dashboard:** https://dexterauthhh.vercel.app
- **Support:** dvdtbuisness@gmail.com

## SDKs

| Language | Folder | Status |
|----------|--------|--------|
| **C#** | [`CSharp-SDK`](./CSharp-SDK) / [`CSharp-Example`](./CSharp-Example) | Available — default `https://dexterauth.onrender.com/api/v2/client` |
| **C++** | [`cpp-SDK`](./cpp-SDK) / [`CPP-Example`](./CPP-Example) | Available |
| **Python** | [`Python-SDK`](./Python-SDK) / [`Python-Example`](./Python-Example) | Available |
| **Java (Android)** | [`java-apk-sdk`](./java-apk-sdk) | Available |
| **Discord Bot** | [`Discord-Bot`](./Discord-Bot) | Available |

## Quick Start

```cs
// C# — using DexterAuth
using DexterAuth;
var api = new DexterAuth.api(
    name: "YourAppName",
    ownerid: "YOUR_APP_ID",
    version: "1.0",
    secret: "YOUR_APP_SECRET",
    apiUrl: "https://dexterauth.onrender.com/api/v2/client"
);
if (await api.LoginAsync("username", "password")) {
    // authenticated
}
```

```python
# Python — using dexterauth
from dexterauth import DexterAuth
api = DexterAuth(name="YourApp", ownerid="YOUR_APP_ID", version="1.0", secret="YOUR_SECRET", api_url="https://dexterauth.onrender.com/api/v2/client")
api.login("username", "password")
```

```cpp
// C++ — using dexterauth
#include "dexterauth/dexterauth.hpp"
DexterAuth::api api("YourApp", "YOUR_APP_ID", "1.0", "YOUR_SECRET", "", "https://dexterauth.onrender.com/api/v2/client");
api.login("username", "password");
```

## Branding Changes from AuthLX

- `AuthLX` → `DexterAuth` / `dexterauth`
- `api.dexterauth.com` / `api.authlx.com` → `dexterauth.onrender.com`
- `authlx.com` → `dexterauthhh.vercel.app`
- `support@authlx.com` → `dvdtbuisness@gmail.com`
- Package `com.authlx.sdk` → `com.dexterauth.sdk`
- Files `AuthLX.cs` → `DexterAuth.cs`, `authlx.py` → `dexterauth.py`, `authlx.hpp` → `dexterauth.hpp`

## License

MIT — see each SDK's `LICENSE`.
