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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // StatusBar VOR super.onCreate() konfigurieren!
        configureStatusBar();
        
        super.onCreate(savedInstanceState);
        
        // Nochmal nach super.onCreate() setzen (falls Capacitor es überschreibt)
        configureStatusBar();
        
        setupOneSignal();
        requestLocationPermission();
        setupWebViewGeolocation();
    }
    
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST);
        }
    }
    
    private void setupWebViewGeolocation() {
        // WebView für Geolocation konfigurieren
        try {
            WebView webView = getBridge().getWebView();
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setGeolocationEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    // Automatisch Geolocation für grabb.ch erlauben
                    if (origin.contains("grabb.ch")) {
                        callback.invoke(origin, true, true);
                    } else {
                        callback.invoke(origin, false, false);
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("WebView Geolocation setup failed: " + e.getMessage());
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Bei jedem Resume sicherstellen dass StatusBar korrekt ist
        configureStatusBar();
    }
    
    private void configureStatusBar() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        
        // ALLE Fullscreen/Immersive Flags entfernen
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        
        // StatusBar zeichnen
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(STATUSBAR_COLOR);
        
        // Wichtig: Content UNTER (nicht hinter) der StatusBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        } else {
            // Legacy: SystemUiVisibility zurücksetzen
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
        
        // Helle Icons auf dunklem Hintergrund (API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = decorView.getSystemUiVisibility();
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // Entferne LIGHT flag = weisse Icons
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
