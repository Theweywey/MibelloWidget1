package io.ionic.starter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class GameWidget extends AppWidgetProvider {

  // CACHÉ EN MEMORIA: Evita descargar la lista de tiendas cada vez
  private static final HashMap<String, String> storeCache = new HashMap<>();

  static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
    // CAMBIADO: Ahora lee de WidgetStorage
    SharedPreferences prefs = context.getSharedPreferences("WidgetStorage", Context.MODE_PRIVATE);
    String favoriteGameId = prefs.getString("favoriteGame", null);

    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.game_widget);
    setupClickIntent(context, views, favoriteGameId);

    if (favoriteGameId != null && !favoriteGameId.isEmpty()) {
      views.setTextViewText(R.id.widget_game_title, "Sincronizando...");
      appWidgetManager.updateAppWidget(appWidgetId, views);
      fetchGameData(context, appWidgetManager, appWidgetId, favoriteGameId);
    } else {
      views.setTextViewText(R.id.widget_game_title, "Elige un favorito");
      views.removeAllViews(R.id.widget_store_flipper);
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }

  private static void fetchGameData(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String gameId) {
    new Thread(() -> {
      try {
        // 1. OBTENER TIENDAS (Súper rápido gracias al caché estático)
        if (storeCache.isEmpty()) {
          try {
            URL storesUrl = new URL("https://www.cheapshark.com/api/1.0/stores");
            HttpURLConnection storeConn = (HttpURLConnection) storesUrl.openConnection();
            storeConn.setRequestProperty("User-Agent", "CheapSharkWidget/1.0");
            storeConn.setConnectTimeout(3000); // Límite de 3 segundos

            BufferedReader sReader = new BufferedReader(new InputStreamReader(storeConn.getInputStream()));
            StringBuilder sRes = new StringBuilder();
            String sLine;
            while ((sLine = sReader.readLine()) != null) sRes.append(sLine);
            sReader.close();

            JSONArray sArr = new JSONArray(sRes.toString());
            for (int i = 0; i < sArr.length(); i++) {
              JSONObject st = sArr.getJSONObject(i);
              storeCache.put(st.getString("storeID"), st.getString("storeName"));
            }
          } catch (Exception e) { Log.e("GameWidget", "Error tiendas"); }
        }

        // 2. DESCARGAR DATOS DEL JUEGO
        URL url = new URL("https://www.cheapshark.com/api/1.0/games?id=" + gameId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "CheapSharkWidget/1.0");
        conn.setConnectTimeout(3000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) result.append(line);
        reader.close();

        JSONObject response = new JSONObject(result.toString());
        JSONObject info = response.getJSONObject("info");
        String title = info.getString("title");
        String thumbUrl = info.getString("thumb");

        // 3. DESCARGAR IMAGEN (Con User-Agent para que el servidor no nos haga esperar)
        Bitmap bitmap = null;
        try {
          URL imgUrl = new URL(thumbUrl);
          HttpURLConnection imgConn = (HttpURLConnection) imgUrl.openConnection();
          imgConn.setRequestProperty("User-Agent", "CheapSharkWidget/1.0");
          imgConn.setConnectTimeout(3000);
          InputStream in = imgConn.getInputStream();
          bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) { Log.e("GameWidget", "Error imagen"); }

        // 4. ACTUALIZAR INTERFAZ AL INSTANTE
        JSONArray deals = response.getJSONArray("deals");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.game_widget);
        setupClickIntent(context, views, gameId);

        if (bitmap != null) {
          views.setImageViewBitmap(R.id.widget_game_thumb, bitmap);
        }
        views.setTextViewText(R.id.widget_game_title, title);
        views.removeAllViews(R.id.widget_store_flipper);

        if (deals.length() > 0) {
          int max = Math.min(deals.length(), 6);
          for (int i = 0; i < max; i++) {
            JSONObject d = deals.getJSONObject(i);
            String sPrice = d.getString("price");
            String rPrice = d.getString("retailPrice");
            String sID = d.getString("storeID");

            // Leemos directo desde la memoria caché
            String sName = storeCache.containsKey(sID) ? storeCache.get(sID) : "Tienda " + sID;

            RemoteViews dView = new RemoteViews(context.getPackageName(), R.layout.deal_item);
            dView.setTextViewText(R.id.deal_price, "$" + sPrice);
            dView.setTextViewText(R.id.deal_store_name, sName);

            if (!sPrice.equals(rPrice)) {
              String rText = "<s>Antes: $" + rPrice + "</s>";
              dView.setTextViewText(R.id.deal_retail_price, Html.fromHtml(rText));
            } else {
              dView.setTextViewText(R.id.deal_retail_price, "");
            }
            views.addView(R.id.widget_store_flipper, dView);
          }
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);

      } catch (Exception e) { Log.e("GameWidget", "Error fetch", e); }
    }).start();
  }

  private static void setupClickIntent(Context context, RemoteViews views, String gameId) {
    Intent intent = new Intent(context, GameWidget.class);
    intent.setAction("OPEN_GAME_MODAL");
    if (gameId != null) intent.putExtra("GAME_ID", gameId);

    PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
      PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    views.setOnClickPendingIntent(R.id.widget_root, pi);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    super.onReceive(context, intent);
    if ("OPEN_GAME_MODAL".equals(intent.getAction())) {
      String id = intent.getStringExtra("GAME_ID");
      if (id != null) {
        SharedPreferences prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);
        prefs.edit().putString("trigger_modal_id", id).apply();
      }
      Intent launch = new Intent(context, MainActivity.class);
      launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      context.startActivity(launch);
    }
  }

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for (int id : appWidgetIds) updateAppWidget(context, appWidgetManager, id);
  }
}
