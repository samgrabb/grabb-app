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
        // StatusBar ZUERST setzen, VOR super.onCreate()
        setupStatusBarBeforeWebView();
        
        super.onCreate(savedInstanceState);
        
        // OneSignal
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
    }
    
    private void setupStatusBarBeforeWebView() {
        Window window = getWindow();
        
        // Flags setzen
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        
        // GRÜNE StatusBar mit SCHWARZEN Icons
        window.setStatusBarColor(Color.parseColor("#10b981"));
        
        // SCHWARZE Icons (LIGHT_STATUS_BAR = dunkle Icons für hellen Hintergrund)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // SETZEN, nicht entfernen!
            decorView.setSystemUiVisibility(flags);
        }
    }
}
