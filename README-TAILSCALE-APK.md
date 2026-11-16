# GIT Chinese Trainer – Tailscale Auto Connect (Android)
Standalone Android WebView app: does not replace the original app module.

## Windows PC
1. Start Node.js server at http://127.0.0.1:3305. Confirm GET /api/health -> {"status":"ok"}.
2. Open PowerShell: `tailscale serve --bg --https=8443 http://127.0.0.1:3305`
3. Find your exact HTTPS URL using `tailscale serve status` (include :8443 if shown).

## Android
1. Install and sign into official Tailscale Android app on the same authorized tailnet; enable VPN.
2. Install `remoteapp-debug.apk` from the GitHub Actions artifact.
3. In GIT Chinese Remote, paste HTTPS URL (e.g. https://machine.tailnet.ts.net:8443), press Save.
4. On subsequent launches, app remembers URL; probes /api/health; loads existing SQL Server login at /; retries periodically when server unavailable.
5. If VPN is down, tap "Mở Tailscale". Consider Android Settings > Connections > More connection settings > VPN > Tailscale > Always-on VPN (menu varies by device).

## Security/limits
- VPN activation and first Tailscale authentication need user action; this APK cannot silently turn on a third-party VPN.
- The user still logs into the Chinese Trainer website with their normal SQL account; no password is embedded or saved by native app.
- Only a valid HTTPS *.ts.net host is allowed. TLS verification is not bypassed.
- Tailscale Serve access control and Node.js JWT still apply.
- This debug APK is signed with a build/debug key; do not treat as production release.
- The server must stay running; network availability cannot be guaranteed.
