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
        
        // StatusBar: Sichtbar mit SCHWARZEN Icons (für weissen Hintergrund)
        setupStatusBar();
        
        // OneSignal
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID);
    }
    
    private void setupStatusBar() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        
        // StatusBar sichtbar (KEIN Fullscreen)
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        // Transparente StatusBar
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        
        // SCHWARZE Icons auf hellem Hintergrund (LIGHT = dunkle Icons)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }
}
