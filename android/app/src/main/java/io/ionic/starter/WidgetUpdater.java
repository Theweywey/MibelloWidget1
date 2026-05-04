package io.ionic.starter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "WidgetUpdater")
public class WidgetUpdater extends Plugin {

  @PluginMethod
  public void update(PluginCall call) {
    Context context = getContext();

    // 1. Recibimos el ID del juego desde Angular
    String gameId = call.getString("gameId", null);

    // 2. Si recibimos un ID, lo guardamos en una memoria nativa exclusiva para los widgets
    if (gameId != null) {
      SharedPreferences prefs = context.getSharedPreferences("WidgetStorage", Context.MODE_PRIVATE);
      prefs.edit().putString("favoriteGame", gameId).apply();
    }

    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

    // 3. Actualizamos el TaskWidget
    ComponentName taskWidget = new ComponentName(context, TaskWidget.class);
    int[] taskWidgetIds = appWidgetManager.getAppWidgetIds(taskWidget);
    for (int appWidgetId : taskWidgetIds) {
      TaskWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    // 4. Actualizamos el GameWidget
    ComponentName gameWidget = new ComponentName(context, GameWidget.class);
    int[] gameWidgetIds = appWidgetManager.getAppWidgetIds(gameWidget);
    for (int appWidgetId : gameWidgetIds) {
      GameWidget.updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    call.resolve();
  }
}
