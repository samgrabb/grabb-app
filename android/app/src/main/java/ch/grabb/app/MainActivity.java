package ch.grabb.app;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import com.onesignal.OneSignal;
import com.onesignal.Continue;
import com.onesignal.debug.LogLevel;

public class MainActivity extends BridgeActivity {
    
    // OneSignal App ID (same as grabb.ch web)
    private static final String ONESIGNAL_APP_ID = "695cd630-8904-4044-962a-012f52f667ef";
    
    // grabb.ch Farben
    private static final String STATUSBAR_COLOR = "#059669";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // WICHTIG: StatusBar ZUERST konfigurieren
        setupStatusBar();
        
        // OneSignal initialisieren
        setupOneSignal();
    }
    
    private void setupStatusBar() {
        Window window = getWindow();
        
        // Alle Fullscreen-Flags entfernen
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        
        // StatusBar-Flags setzen
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // StatusBar-Farbe setzen (grabb.ch grün)
        window.setStatusBarColor(Color.parseColor(STATUSBAR_COLOR));
        
        // Content NICHT unter StatusBar rendern
        WindowCompat.setDecorFitsSystemWindows(window, true);
        
        // Dunkle Icons auf heller StatusBar (DARK = dunkle Icons)
        View decorView = window.getDecorView();
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(window, decorView);
        insetsController.setAppearanceLightStatusBars(false); // false = helle Icons auf dunklem Hintergrund
    }
    
    private void setupOneSignal() {
        // Verbose logging für Debugging
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        
        // OneSignal initialisieren
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
        
        // Push-Berechtigung anfragen
        OneSignal.getNotifications().requestPermission(true, Continue.with(r -> {
            if (r.isSuccess()) {
                if (r.getData()) {
                    System.out.println("OneSignal: Push permission granted!");
                } else {
                    System.out.println("OneSignal: Push permission denied");
                }
            }
        }));
    }
}
