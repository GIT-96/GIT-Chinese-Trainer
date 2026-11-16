package vn.git.chinesetrainer.remote;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;

public final class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(0, 112, 230);
    private static final int DARK = Color.rgb(9, 45, 92);
    private static final int MIC_REQUEST = 201;
    private static final int GEO_REQUEST = 202;
    private static final String PREFS = "git_chinese_tailscale_remote";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private FrameLayout root;
    private View overlay;
    private WebView web;
    private TextView statusText;
    private String endpoint = "";
    private int generation = 0;
    private int failures = 0;
    private boolean destroyed = false;
    private PermissionRequest pendingMic;
    private GeolocationPermissions.Callback pendingGeo;
    private String pendingGeoOrigin;
    private TextToSpeech tts;
    private boolean ttsReady;

    private int dp(float n) { return (int) (n * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(0, 111, 222));
        getWindow().setNavigationBarColor(Color.rgb(245, 250, 255));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(246, 250, 255));
        setContentView(root);
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int lang = tts.setLanguage(Locale.SIMPLIFIED_CHINESE);
                ttsReady = lang != TextToSpeech.LANG_MISSING_DATA && lang != TextToSpeech.LANG_NOT_SUPPORTED;
            }
        });
        String saved = prefs.getString("serve_url", "");
        if (isValidUrl(saved)) {
            endpoint = normalizeUrl(saved);
            startConnection();
        } else {
            showSetup("");
        }
    }

    private boolean isValidUrl(String value) {
        if (value == null) return false;
        try {
            Uri u = Uri.parse(value.trim());
            String host = u.getHost();
            return "https".equalsIgnoreCase(u.getScheme())
                && host != null && host.toLowerCase(Locale.US).endsWith(".ts.net")
                && host.length() > 7
                && u.getPort() <= 65535
                && u.getUserInfo() == null
                && (u.getQuery() == null || u.getQuery().isEmpty())
                && u.getFragment() == null;
        } catch (Exception ignored) { return false; }
    }

    private String normalizeUrl(String value) {
        Uri u = Uri.parse(value.trim());
        return "https://" + u.getHost().toLowerCase(Locale.US)
            + (u.getPort() > 0 ? ":" + u.getPort() : "");
    }

    private GradientDrawable surface(int color, int radius) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(color);
        result.setCornerRadius(dp(radius));
        return result;
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(color);
        t.setTextSize(size);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setLineSpacing(dp(2), 1f);
        return t;
    }

    private Button button(String name, boolean primary) {
        Button b = new Button(this);
        b.setText(name);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setTextColor(primary ? Color.WHITE : DARK);
        b.setBackground(surface(primary ? BLUE : Color.rgb(224, 241, 255), 14));
        b.setElevation(dp(2));
        return b;
    }

    private void margin(LinearLayout container, View view, int top) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(top);
        container.addView(view, p);
    }

    private LinearLayout basePanel() {
        if (overlay != null) root.removeView(overlay);
        if (web != null) web.setVisibility(View.GONE);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(36), dp(24), dp(28));
        box.setBackgroundColor(Color.rgb(246, 250, 255));
        scroll.addView(box);
        root.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
        overlay = scroll;
        return box;
    }

    private void showSetup(String error) {
        generation++;
        LinearLayout box = basePanel();
        TextView icon = label("中  ·  AI", 28, BLUE, true);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(surface(Color.rgb(218, 239, 255), 22));
        icon.setPadding(0, dp(20), 0, dp(20));
        margin(box, icon, 0);
        margin(box, label("GIT Chinese Trainer", 26, DARK, true), 25);
        margin(box, label("Kết nối an toàn qua Tailscale", 15, Color.rgb(78, 107, 142), false), 7);
        margin(box, label("Nhập một lần địa chỉ HTTPS của Tailscale Serve. Những lần mở sau ứng dụng sẽ tự kiểm tra server và kết nối lại.", 14, DARK, false), 24);
        EditText urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setTextSize(14);
        urlInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setHint("https://may-chu.tailnet.ts.net:8443");
        urlInput.setText(endpoint);
        urlInput.setPadding(dp(15), dp(15), dp(15), dp(15));
        urlInput.setBackground(surface(Color.WHITE, 12));
        margin(box, urlInput, 14);
        if (!error.isEmpty()) margin(box, label(error, 13, Color.rgb(183, 36, 43), false), 8);
        Button connect = button("Lưu và kết nối", true);
        margin(box, connect, 16);
        connect.setOnClickListener(v -> {
            String entered = urlInput.getText().toString().trim();
            if (!isValidUrl(entered)) {
                urlInput.setError("Cần HTTPS và tên miền Tailscale *.ts.net");
                return;
            }
            endpoint = normalizeUrl(entered);
            prefs.edit().putString("serve_url", endpoint).apply();
            startConnection();
        });
        Button tailscale = button("Mở ứng dụng Tailscale", false);
        margin(box, tailscale, 12);
        tailscale.setOnClickListener(v -> openTailscale());
        margin(box, label("Máy Windows phải chạy Node.js cổng 3305 và Tailscale Serve. Điện thoại cũng phải được cấp quyền vào cùng tailnet.", 12, Color.rgb(102, 126, 153), false), 25);
    }

    private void showConnecting(String message) {
        LinearLayout box = basePanel();
        box.setGravity(Gravity.CENTER_VERTICAL);
        margin(box, label("GIT Chinese Trainer AI", 25, DARK, true), 0);
        margin(box, label("Đang kết nối tới máy chủ qua Tailscale", 15, Color.rgb(89, 112, 142), false), 8);
        statusText = label(message, 14, BLUE, true);
        statusText.setPadding(dp(16), dp(16), dp(16), dp(16));
        statusText.setBackground(surface(Color.rgb(228, 244, 255), 14));
        margin(box, statusText, 22);
        margin(box, label(endpoint, 12, Color.rgb(94, 117, 143), false), 14);
        Button retry = button("Kiểm tra lại ngay", true);
        margin(box, retry, 25);
        retry.setOnClickListener(v -> { generation++; failures = 0; probe(); });
        Button vpn = button("Mở Tailscale", false);
        margin(box, vpn, 10);
        vpn.setOnClickListener(v -> openTailscale());
        Button edit = button("Đổi địa chỉ máy chủ", false);
        margin(box, edit, 10);
        edit.setOnClickListener(v -> showSetup(""));
        margin(box, label("Ứng dụng tự thử lại khi server sẵn sàng. Nếu VPN chưa bật, hãy kết nối Tailscale một lần trong ứng dụng Tailscale.", 12, Color.rgb(100, 123, 148), false), 22);
    }

    private void startConnection() {
        generation++;
        failures = 0;
        showConnecting("Đang kiểm tra /api/health ...");
        probe();
    }

    private void probe() {
        final int requestGeneration = generation;
        final String target = endpoint;
        if (statusText != null) statusText.setText("Đang kiểm tra kết nối máy chủ ...");
        new Thread(() -> {
            boolean healthy = false;
            try {
                URL url = new URL(target + "/api/health");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(6500);
                conn.setReadTimeout(6500);
                conn.setRequestMethod("GET");
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);
                if (conn.getResponseCode() == 200) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    try (InputStream in = conn.getInputStream()) {
                        byte[] bytes = new byte[256];
                        int size;
                        while ((size = in.read(bytes)) != -1 && buffer.size() < 4096) {
                            buffer.write(bytes, 0, size);
                        }
                    }
                    String response = buffer.toString("UTF-8");
                    healthy = "ok".equals(new JSONObject(response).optString("status"));
                }
                conn.disconnect();
            } catch (Exception ignored) { }
            boolean success = healthy;
            runOnUiThread(() -> {
                if (destroyed || requestGeneration != generation) return;
                if (success) {
                    failures = 0;
                    loadWebsite();
                } else {
                    failures++;
                    int seconds = failures <= 2 ? 5 : (failures <= 5 ? 10 : 20);
                    if (statusText != null) {
                        statusText.setText("Chưa kết nối được. Tự thử lại sau " + seconds + " giây.\nKiểm tra Tailscale và Node.js trên Windows.");
                    }
                    handler.postDelayed(() -> {
                        if (!destroyed && requestGeneration == generation) probe();
                    }, seconds * 1000L);
                }
            });
        }, "GitChinese-HealthCheck").start();
    }

    private boolean isCurrentOrigin(Uri uri) {
        if (uri == null || !isValidUrl(endpoint)) return false;
        Uri allowed = Uri.parse(endpoint);
        return "https".equalsIgnoreCase(uri.getScheme())
            && allowed.getHost().equalsIgnoreCase(uri.getHost())
            && allowed.getPort() == uri.getPort();
    }

    private void ensureWeb() {
        if (web != null) return;
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(246, 250, 255));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        CookieManager.getInstance().setAcceptCookie(true);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                if (isCurrentOrigin(url) || "about".equals(url.getScheme())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, url)); } catch (Exception ignored) { }
                return true;
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) runOnUiThread(() -> {
                    if (!destroyed) startConnection();
                });
            }
            @Override public void onReceivedHttpError(WebView view, WebResourceRequest request, android.webkit.WebResourceResponse response) {
                if (request.isForMainFrame() && response.getStatusCode() >= 500) {
                    runOnUiThread(() -> { if (!destroyed) startConnection(); });
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    if (!isCurrentOrigin(request.getOrigin())) { request.deny(); return; }
                    boolean wantsAudio = false;
                    for (String resource : request.getResources()) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) wantsAudio = true;
                    }
                    if (!wantsAudio) { request.deny(); return; }
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        request.grant(new String[] {PermissionRequest.RESOURCE_AUDIO_CAPTURE});
                    } else {
                        pendingMic = request;
                        requestPermissions(new String[] {Manifest.permission.RECORD_AUDIO}, MIC_REQUEST);
                    }
                });
            }
            @Override public void onPermissionRequestCanceled(PermissionRequest request) {
                if (pendingMic == request) pendingMic = null;
            }
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (!isCurrentOrigin(Uri.parse(origin))) { callback.invoke(origin, false, false); return; }
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false);
                } else {
                    pendingGeo = callback;
                    pendingGeoOrigin = origin;
                    requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, GEO_REQUEST);
                }
            }
        });
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface public void speak(String text, double rate) {
                if (text == null || text.length() > 500) return;
                runOnUiThread(() -> {
                    if (web == null || !isCurrentOrigin(Uri.parse(web.getUrl()))) return;
                    if (ttsReady) {
                        tts.setSpeechRate((float) Math.max(0.55, Math.min(1.3, rate)));
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mandarin");
                    }
                });
            }
        }, "GITNativeTTS");
        root.addView(web, 0, new FrameLayout.LayoutParams(-1, -1));
    }

    private void loadWebsite() {
        ensureWeb();
        if (overlay != null) { root.removeView(overlay); overlay = null; }
        statusText = null;
        web.setVisibility(View.VISIBLE);
        Uri current = web.getUrl() == null ? null : Uri.parse(web.getUrl());
        if (isCurrentOrigin(current) && !current.toString().equals("about:blank")) {
            web.reload();
        } else {
            web.loadUrl(endpoint + "/");
        }
    }

    private void openTailscale() {
        Intent launch = getPackageManager().getLaunchIntentForPackage("com.tailscale.ipn");
        try {
            if (launch != null) startActivity(launch);
            else startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://tailscale.com/download/android")));
        } catch (Exception ignored) {
            Toast.makeText(this, "Hãy cài và bật Tailscale trên điện thoại", Toast.LENGTH_LONG).show();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allowed = false;
        for (int result : grantResults) if (result == PackageManager.PERMISSION_GRANTED) allowed = true;
        if (requestCode == MIC_REQUEST && pendingMic != null) {
            if (allowed) pendingMic.grant(new String[] {PermissionRequest.RESOURCE_AUDIO_CAPTURE});
            else pendingMic.deny();
            pendingMic = null;
        }
        if (requestCode == GEO_REQUEST && pendingGeo != null) {
            pendingGeo.invoke(pendingGeoOrigin, allowed, false);
            pendingGeo = null;
        }
    }

    @Override public void onBackPressed() {
        if (web != null && web.getVisibility() == View.VISIBLE && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        generation++;
        handler.removeCallbacksAndMessages(null);
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (web != null) { web.destroy(); web = null; }
        super.onDestroy();
    }
}
