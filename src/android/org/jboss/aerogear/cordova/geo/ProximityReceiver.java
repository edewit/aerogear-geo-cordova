package org.jboss.aerogear.cordova.geo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static org.jboss.aerogear.cordova.geo.GeofencingService.TAG;

/**
 * @author edewit@redhat.com
 */
public class ProximityReceiver extends BroadcastReceiver {
  public static final int NOTIFICATION_ID = 6542;

  @Override
  public void onReceive(Context context, Intent intent) {
    String id = intent.getData().getLastPathSegment();
    String status = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false) ? "entered" : "left";

    Log.d(TAG, "received proximity alert for region " + id + " with status " + status);

    GeofencingPlugin.sendNotification(id, status);
    if (!GeofencingPlugin.isInForeground()) {
      createNotification(context, id, status);
    }
  }

  public void createNotification(Context context, String id, String status) {
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    String appName = getAppName(context);

    Intent notificationIntent = new Intent(context, ProxyActivity.class);
    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    notificationIntent.putExtra("id", id);
    notificationIntent.putExtra("status", status);

    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder builder =
        new NotificationCompat.Builder(context)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(context.getApplicationInfo().icon)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(appName)
            .setTicker(appName)
            .setAutoCancel(true)
            .setContentIntent(contentIntent);

    if (GeofencingPlugin.getNotifyMessage() != null) {
      builder.setContentText(String.format(GeofencingPlugin.getNotifyMessage(), id, status));
    } else {
      builder.setContentText("You have " + status + " your point of interest");
    }

    notificationManager.notify(appName, NOTIFICATION_ID, builder.build());
  }

  private static String getAppName(Context context) {
    CharSequence appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());
    return (String) appName;
  }
}
