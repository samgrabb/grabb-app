package ch.grabb.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;
import com.onesignal.OneSignal;
import com.onesignal.Continue;
import com.onesignal.debug.LogLevel;

public class MainActivity extends BridgeActivity {
    
    private static final String ONESIGNAL_APP_ID = "695cd630-8904-4044-962a-012f52f667ef";
    private static final int STATUSBAR_COLOR = Color.parseColor("#059669");
    private static final int PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // StatusBar konfigurieren
        configureStatusBar();
        
        // Permissions anfragen
        requestPermissions();
        
        // WebView Setup
        setupWebView();
        
        // OneSignal
        setupOneSignal();
    }
    
    private void requestPermissions() {
        String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        };
        
        boolean needRequest = false;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }
        
        if (needRequest) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    private void setupWebView() {
        try {
            WebView webView = getBridge().getWebView();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setGeolocationEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
            webView.getSettings().setAllowFileAccess(true);
            
            webView.setWebChromeClient(new WebChromeClient() {
                // Geolocation Permission
                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    if (origin.contains("grabb.ch")) {
                        callback.invoke(origin, true, true);
                    } else {
                        callback.invoke(origin, false, false);
                    }
                }
                
                // Camera/Microphone Permission (für WebRTC und input type=file capture)
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    runOnUiThread(() -> {
                        String[] resources = request.getResources();
                        for (String resource : resources) {
                            if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource) ||
                                PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(resource)) {
                                // Kamera/Mikrofon erlauben
                                request.grant(resources);
                                return;
                            }
                        }
                        request.deny();
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("WebView setup failed: " + e.getMessage());
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        configureStatusBar();
    }
    
    private void configureStatusBar() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        
        // Flags setzen
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(STATUSBAR_COLOR);
        
        // Content unter StatusBar (nicht dahinter)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        }
        
        // Weisse Icons auf dunklem Hintergrund
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = decorView.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        }
    }
    
    private void setupOneSignal() {
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
        OneSignal.getNotifications().requestPermission(true, Continue.with(r -> {
            if (r.isSuccess()) {
                System.out.println("OneSignal: Permission " + (r.getData() ? "granted" : "denied"));
            }
        }));
    }
}
