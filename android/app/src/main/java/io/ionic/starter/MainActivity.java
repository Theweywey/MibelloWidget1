package io.ionic.starter;

import android.os.Bundle;
import androidx.core.splashscreen.SplashScreen;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    SplashScreen.installSplashScreen(this);
    // Registramos nuestro timbre nativo
    registerPlugin(WidgetUpdater.class);
    super.onCreate(savedInstanceState);
  }
}
