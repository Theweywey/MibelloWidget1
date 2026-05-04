import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'io.ionic.starter', 
  appName: 'MyBeautifulWidgets',
  webDir: 'www',
  // Borramos la línea de bundledWebRuntime
  plugins: {
    SplashScreen: {
      launchShowDuration: 3000, 
      launchAutoHide: true,
      androidSplashResourceName: "splash", 
      backgroundColor: "#1E1E1E", 
      androidScaleType: "CENTER_CROP", 
      showSpinner: true,
      spinnerColor: "#FFFFFF"
    }
  }
};

export default config;