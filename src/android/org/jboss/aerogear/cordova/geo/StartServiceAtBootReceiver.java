package org.jboss.aerogear.cordova.geo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by edewit on 9/2/13.
 */
public class StartServiceAtBootReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {
        Intent serviceLauncher = new Intent(context, GeofencingService.class);
        context.startService(serviceLauncher);
    }
}
