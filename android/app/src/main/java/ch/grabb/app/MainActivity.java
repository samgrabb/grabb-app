package ch.grabb.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import com.onesignal.OneSignal;
import com.onesignal.Continue;
import com.onesignal.debug.LogLevel;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends BridgeActivity {
    
    private static final String ONESIGNAL_APP_ID = "695cd630-8904-4044-962a-012f52f667ef";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1002;
    
    private ValueCallback<Uri[]> filePathCallback;
    private Uri cameraPhotoUri;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // SplashScreen MUSS vor super.onCreate() installiert werden
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        
        super.onCreate(savedInstanceState);
        
        // StatusBar SOFORT konfigurieren
        forceStatusBarVisible();
        
        // Nochmal nach 500ms (falls Capacitor überschreibt)
        handler.postDelayed(this::forceStatusBarVisible, 500);
        
        // Und nochmal nach 1500ms (nach Splash)
        handler.postDelayed(this::forceStatusBarVisible, 1500);
        
        // Permissions anfragen
        requestPermissions();
        
        // WebView Setup
        setupWebView();
        
        // OneSignal
        setupOneSignal();
    }
    
    private void forceStatusBarVisible() {
        Window window = getWindow();
        View decorView = window.getDecorView();
        
        // ALLE Fullscreen-Flags entfernen
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        
        // StatusBar zeichnen
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);
        
        // Content UNTER StatusBar (nicht dahinter)
        WindowCompat.setDecorFitsSystemWindows(window, true);
        
        // System UI Flags - keine Versteck-Flags!
        int flags = decorView.getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        flags &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        flags &= ~View.SYSTEM_UI_FLAG_IMMERSIVE;
        flags &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        flags &= ~View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // Weisse Icons
        decorView.setSystemUiVisibility(flags);
        
        // Für Android 11+ (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, decorView);
            if (controller != null) {
                controller.show(WindowInsetsCompat.Type.statusBars());
                controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
                controller.setAppearanceLightStatusBars(false); // Weisse Icons
            }
        }
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
            webView.getSettings().setAllowContentAccess(true);
            
            final MainActivity activity = this;
            
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                    if (origin.contains("grabb.ch")) {
                        callback.invoke(origin, true, true);
                    } else {
                        callback.invoke(origin, false, false);
                    }
                }
                
                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    runOnUiThread(() -> request.grant(request.getResources()));
                }
                
                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback, FileChooserParams params) {
                    if (filePathCallback != null) {
                        filePathCallback.onReceiveValue(null);
                    }
                    filePathCallback = callback;
                    
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            System.err.println("Error creating image file: " + ex.getMessage());
                        }
                        
                        if (photoFile != null) {
                            cameraPhotoUri = FileProvider.getUriForFile(activity,
                                getApplicationContext().getPackageName() + ".fileprovider",
                                photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } else {
                            takePictureIntent = null;
                        }
                    }
                    
                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("image/*");
                    
                    Intent[] intentArray;
                    if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else {
                        intentArray = new Intent[0];
                    }
                    
                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "Bild auswählen");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    
                    startActivityForResult(chooserIntent, FILE_CHOOSER_REQUEST_CODE);
                    return true;
                }
            });
        } catch (Exception e) {
            System.err.println("WebView setup failed: " + e.getMessage());
        }
    }
    
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (filePathCallback == null) return;
            
            Uri[] results = null;
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    results = new Uri[]{data.getData()};
                } else if (cameraPhotoUri != null) {
                    results = new Uri[]{cameraPhotoUri};
                }
            }
            
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
            cameraPhotoUri = null;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // StatusBar bei jedem Resume erzwingen
        forceStatusBarVisible();
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Auch bei Fokus-Wechsel StatusBar erzwingen
            forceStatusBarVisible();
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
