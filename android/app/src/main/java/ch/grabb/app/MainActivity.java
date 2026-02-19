package ch.grabb.app;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Show status bar with dark icons
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        
        // Set status bar color
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark, getTheme()));
        
        // Light status bar (dark icons)
        View decorView = getWindow().getDecorView();
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), decorView);
        controller.setAppearanceLightStatusBars(false);
    }
}
