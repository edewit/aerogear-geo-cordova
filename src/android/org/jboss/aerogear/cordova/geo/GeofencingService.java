/*
 * JBoss, Home of Professional Open Source.
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.cordova.geo;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Service that monitors the Geofences.
 * @author edewit@redhat.com
 */
public class GeofencingService extends Service {
  static final String TAG = GeofencingService.class.getSimpleName();

  static final String PROXIMITY_ALERT_INTENT = "geoFencingProximityAlert";

  private LocationManager locationManager;
  private final IBinder binder = new LocalBinder();
  private GeofenceStore geofenceStore;

  public class LocalBinder extends Binder {
    GeofencingService getService() {
      return GeofencingService.this;
    }
  }

  @Override
  public void onCreate() {
    locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

    geofenceStore = new GeofenceStore(getApplicationContext());
    for (String id : geofenceStore.getGeofences()) {
      Geofence fence = geofenceStore.getGeofence(id);
      addFence(id, fence);
    }
  }

  public void addRegion(String id, double latitude, double longitude, float radius) {
    Geofence geofence = new Geofence(id, latitude, longitude, radius);
    geofenceStore.setGeofence(id, geofence);
    addFence(id, geofence);
  }

  private void addFence(String id, Geofence geofence) {
    PendingIntent proximityIntent = createIntent(id);
    locationManager.addProximityAlert(geofence.getLatitude(), geofence.getLongitude(),
        geofence.getRadius(), geofence.getExpirationDuration(), proximityIntent);
  }

  public void removeRegion(String id) {
    geofenceStore.clearGeofence(id);
    PendingIntent proximityIntent = createIntent(id);
    locationManager.removeProximityAlert(proximityIntent);
  }

  private PendingIntent createIntent(String id) {
    Intent intent = new Intent(PROXIMITY_ALERT_INTENT);
    Uri uri = new Uri.Builder().appendPath("proximity").appendPath(id).build();
    intent.setDataAndType(uri, "vnd.geofencing.region/update");
    return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
  }

  public Set<String> getWachedRegionIds() {
    return new HashSet<String>(geofenceStore.getGeofences());
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }
}
