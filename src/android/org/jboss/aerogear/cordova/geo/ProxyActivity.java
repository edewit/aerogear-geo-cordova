package org.jboss.aerogear.cordova.geo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

/**
 * @author edewit
 */
public class ProxyActivity extends Activity {
  private static String TAG = "ProxyActivity";

  /*
   * this activity will be started if the user touches a notification that we own.
   * We send it's data off to the push plugin for processing.
   * If needed, we boot up the main activity to kickstart the application.
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v(TAG, "onCreate");

    boolean active = GeofencingPlugin.isActive();
    GeofencingPlugin.sendNotification(getIntent().getExtras());

    finish();

    if (!active) {
      forceMainActivityReload();
    }
  }

  /**
   * Forces the main activity to re-launch if it's unloaded.
   */
  private void forceMainActivityReload() {
    PackageManager pm = getPackageManager();
    Intent launchIntent = pm.getLaunchIntentForPackage(getApplicationContext().getPackageName());
    startActivity(launchIntent);
  }

}
