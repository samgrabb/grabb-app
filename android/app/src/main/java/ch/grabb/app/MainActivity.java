package ch.grabb.app;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class MainActivity extends BridgeActivity {
    
    private static final String ONESIGNAL_APP_ID = "695cd630-8904-4044-962a-012f52f667ef";
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // StatusBar erzwingen
        forceStatusBarSettings();
        
        // OneSignal
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
        
        // WebView Listener für Page Load
        setupWebViewListener();
        
        // Periodisch StatusBar erzwingen (alle 500ms für 10 Sekunden)
        startStatusBarEnforcer();
    }
    
    private void setupWebViewListener() {
        // Nach dem Bridge-Setup den WebView holen
        handler.postDelayed(() -> {
            try {
                WebView webView = getBridge().getWebView();
                if (webView != null) {
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public void onPageFinished(WebView view, String url) {
                            super.onPageFinished(view, url);
                            // StatusBar NACH Page Load nochmal setzen
                            forceStatusBarSettings();
                            // Und nochmal nach 500ms (für JavaScript-Verzögerung)
                            handler.postDelayed(() -> forceStatusBarSettings(), 500);
                            handler.postDelayed(() -> forceStatusBarSettings(), 1000);
                            handler.postDelayed(() -> forceStatusBarSettings(), 2000);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }
    
    private void startStatusBarEnforcer() {
        // Für die ersten 10 Sekunden: alle 500ms StatusBar erzwingen
        for (int i = 0; i < 20; i++) {
            handler.postDelayed(() -> forceStatusBarSettings(), i * 500);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        forceStatusBarSettings();
        // Auch bei Resume mehrmals setzen
        handler.postDelayed(() -> forceStatusBarSettings(), 500);
        handler.postDelayed(() -> forceStatusBarSettings(), 1000);
    }
    
    private void forceStatusBarSettings() {
        runOnUiThread(() -> {
            Window window = getWindow();
            
            // Alle Transparenz-Flags entfernen
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            
            // StatusBar zeichnen
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            
            // GRÜN (nicht rot - jetzt wissen wir dass der Code läuft)
            window.setStatusBarColor(Color.parseColor("#10b981"));
            
            // SCHWARZE Icons
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(flags);
            }
            
            // WebView unter StatusBar (nicht dahinter)
            WindowCompat.setDecorFitsSystemWindows(window, true);
        });
    }
}
