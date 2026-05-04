package io.ionic.starter;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences; // Necesario para leer los datos
import android.widget.RemoteViews;

import org.json.JSONObject; // Necesario para procesar el JSON de Capacitor

import io.ionic.starter.R;

/**
 * Implementation of App Widget functionality.
 */
public class TaskWidget extends AppWidgetProvider {

  // Método reemplazado para leer datos desde CapacitorStorage
  public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                              int appWidgetId) {

    // 1. Apuntamos al archivo de memoria nativa que crea Capacitor
    SharedPreferences prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);

    // 2. Buscamos la llave "current" que guardamos desde Ionic
    String currentTaskJson = prefs.getString("current", null);
    String taskText = "Sin tarea seleccionada"; // Texto por defecto

    // 3. Si encontramos datos, extraemos la descripción usando nuestra clase o JSON
    if (currentTaskJson != null) {
      try {
        // Capacitor guarda el valor en formato JSON, así que lo parseamos
        JSONObject jsonObject = new JSONObject(currentTaskJson);
        taskText = jsonObject.getString("description");
      } catch (Exception e) {
        // Si hay un error en el formato, imprimimos el error en la consola de depuración
        e.printStackTrace();
      }
    }

    // 4. Inyectamos el texto en el diseño XML del Widget
    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.task_widget);
    views.setTextViewText(R.id.appwidget_text, taskText);

    // 5. Le decimos al sistema Android que actualice la pantalla
    appWidgetManager.updateAppWidget(appWidgetId, views);
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    // Se actualizan todos los widgets activos en la pantalla de inicio
    for (int appWidgetId : appWidgetIds) {
      updateAppWidget(context, appWidgetManager, appWidgetId);
    }
  }

  @Override
  public void onEnabled(Context context) {
    // Funcionalidad cuando se crea el primer widget
  }

  @Override
  public void onDisabled(Context context) {
    // Funcionalidad cuando se elimina el último widget
  }
}
