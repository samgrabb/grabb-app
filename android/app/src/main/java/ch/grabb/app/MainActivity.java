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
import com.onesignal.debug.LogLevel;

public class MainActivity extends BridgeActivity {
    
    private static final String ONESIGNAL_APP_ID = "695cd630-8904-4044-962a-012f52f667ef";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Edge-to-Edge aktivieren
        setupEdgeToEdge();
        
        // OneSignal
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
    }
    
    private void setupEdgeToEdge() {
        Window window = getWindow();
        
        // Edge-to-Edge Layout aktivieren
        WindowCompat.setDecorFitsSystemWindows(window, false);
        
        // StatusBar grün mit weißen Icons
        window.setStatusBarColor(Color.parseColor("#10b981"));
        
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            // Weisse Icons (false = light icons für dunklen Hintergrund)
            controller.setAppearanceLightStatusBars(false);
        }
    }
}
