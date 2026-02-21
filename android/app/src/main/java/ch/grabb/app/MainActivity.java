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
import com.getcapacitor.Plugin;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

public class MainActivity extends BridgeActivity {
    
    private static final String ONESIGNAL_APP_ID = "695cd630-8904-4044-962a-012f52f667ef";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // StatusBar erzwingen
        forceStatusBarSettings();
        
        // OneSignal
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Bei jedem Resume nochmal setzen
        forceStatusBarSettings();
    }
    
    private void forceStatusBarSettings() {
        runOnUiThread(() -> {
            Window window = getWindow();
            
            // Alle Transparenz-Flags entfernen
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            
            // StatusBar zeichnen
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            
            // GRÜN und UNDURCHSICHTIG
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
