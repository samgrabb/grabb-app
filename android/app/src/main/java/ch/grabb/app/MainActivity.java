package ch.grabb.app;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.getcapacitor.BridgeActivity;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class MainActivity extends BridgeActivity {
    
    private static final String ONESIGNAL_APP_ID = "695cd630-8904-4044-962a-012f52f667ef";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // StatusBar konfigurieren
        setupStatusBar();
        
        // OneSignal
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
    }
    
    private void setupStatusBar() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        
        // StatusBar-Hintergrund zeichnen lassen
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        
        // GRÜNER Hintergrund für StatusBar (passend zu grabb.ch Header)
        window.setStatusBarColor(Color.parseColor("#10b981"));
        
        // WEISSE Icons (standard = helle Icons für dunklen Hintergrund)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.setSystemUiVisibility(0); // KEINE LIGHT_STATUS_BAR = weisse Icons
        }
    }
}
