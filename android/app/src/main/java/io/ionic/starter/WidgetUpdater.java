package io.ionic.starter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "WidgetUpdater")
public class WidgetUpdater extends Plugin {

  @PluginMethod
  public void update(PluginCall call) {
    android.util.Log.d("WidgetUpdater", "Recibida petición de actualización desde TypeScript");
    Context context = getContext();
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

    // 1. ACTUALIZAMOS EL TASK WIDGET
    ComponentName taskWidget = new ComponentName(context, TaskWidget.class);
    int[] taskWidgetIds = appWidgetManager.getAppWidgetIds(taskWidget);
    for (int appWidgetId : taskWidgetIds) {
      TaskWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    // 2. ACTUALIZAMOS EL GAME WIDGET (¡Esto era lo que faltaba!)
    ComponentName gameWidget = new ComponentName(context, GameWidget.class);
    int[] gameWidgetIds = appWidgetManager.getAppWidgetIds(gameWidget);
    for (int appWidgetId : gameWidgetIds) {
      GameWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    call.resolve();
  }
}
