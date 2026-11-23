package com.numberone.manager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;

public class MainActivity extends Activity {
    private WebView webView;
    private static final String CHANNEL_ID = "number_one_alerts";
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(9,10,14));
        getWindow().setNavigationBarColor(Color.rgb(9,10,14));
        createChannel();
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(9,10,14));
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        webView.addJavascriptInterface(new AlarmBridge(this), "AndroidBridge");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        setContentView(webView);
        if (savedInstanceState == null) webView.loadUrl("file:///android_asset/index.html");
        else webView.restoreState(savedInstanceState);
    }
    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "تنبيهات جلسات NUMBER ONE", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("تنبيه قبل انتهاء جلسة اللعب وعند انتهائها");
            channel.enableVibration(true);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }
    public static class AlarmBridge {
        private final Context context;
        AlarmBridge(Context c) { context = c.getApplicationContext(); }
        @JavascriptInterface public void scheduleSession(int device, long start, int durationMinutes) {
            long warning = start + durationMinutes * 60000L - 300000L;
            long expiry = start + durationMinutes * 60000L;
            if (warning > System.currentTimeMillis()) schedule(device * 2, warning, device, "باقي 5 دقائق", "الجلسة على جهاز " + (device + 1) + " ستنتهي بعد خمس دقائق.");
            schedule(device * 2 + 1, expiry, device, "انتهى الوقت", "انتهى الوقت المحدد لجهاز " + (device + 1) + ".");
        }
        private void schedule(int request, long at, int device, String title, String body) {
            Intent i = new Intent(context, AlarmReceiver.class).putExtra("title", title).putExtra("body", body).putExtra("notificationId", request + 100);
            PendingIntent pi = PendingIntent.getBroadcast(context, request, i, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (am != null) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
        @JavascriptInterface public void cancelSession(int device) {
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            for (int request = device * 2; request <= device * 2 + 1; request++) {
                Intent i = new Intent(context, AlarmReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(context, request, i, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
                if (am != null) am.cancel(pi);
                pi.cancel();
            }
        }
    }
    @Override protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); if (webView != null) webView.saveState(outState); }
    @Override public void onBackPressed() { if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}