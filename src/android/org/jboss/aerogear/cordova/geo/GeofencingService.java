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
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.google.android.gms.location.Geofence.Builder;

/**
 * Service that monitors the Geofences.
 *
 * @author edewit@redhat.com
 */
public class GeofencingService extends Service implements GoogleApiClient.ConnectionCallbacks {
  static final String TAG = GeofencingService.class.getSimpleName();

  static final String PROXIMITY_ALERT_INTENT = "geoFencingProximityAlert";

  private final IBinder binder = new LocalBinder();
  private GeofenceStore geofenceStore;
  private GoogleApiClient googleClient;

  @Override
  public void onConnected(Bundle bundle) {

    for (String id : geofenceStore.getGeofences()) {
      Geofence geofence = geofenceStore.getGeofence(id);
      addFence(id, geofence);
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
  }

  public class LocalBinder extends Binder {
    GeofencingService getService() {
      return GeofencingService.this;
    }
  }

  @Override
  public void onCreate() {
    buildGoogleApiClient();
    geofenceStore = new GeofenceStore(getApplicationContext());
  }

  protected synchronized void buildGoogleApiClient() {
    googleClient = new GoogleApiClient.Builder(this)
        .addApi(LocationServices.API)
        .addConnectionCallbacks(this)
        .build();
    googleClient.connect();
  }

  public void addRegion(String id, double latitude, double longitude, float radius, String message) {
    Geofence geofence = new Geofence(id, latitude, longitude, radius, message);
    geofenceStore.setGeofence(id, geofence);
    addFence(id, geofence);
  }

  private void addFence(String id, final Geofence geofence) {
    if (googleClient.isConnected()) {
      PendingIntent proximityIntent = createIntent(id);

      LocationServices.GeofencingApi.addGeofences(googleClient, Arrays.asList(
          new Builder().setRequestId(id)
              .setCircularRegion(
                  geofence.getLatitude(),
                  geofence.getLongitude(),
                  geofence.getRadius()
              )
              .setExpirationDuration(geofence.getExpirationDuration())
              .setTransitionTypes(geofence.getTransitionType())
              .build()
      ), proximityIntent);
    }
  }


  public void removeRegion(String id) {
    geofenceStore.clearGeofence(id);
    PendingIntent proximityIntent = createIntent(id);
    if (googleClient.isConnected()) {
      LocationServices.GeofencingApi.removeGeofences(googleClient, proximityIntent);
    }
  }

  private PendingIntent createIntent(String id) {
    Intent intent = new Intent(PROXIMITY_ALERT_INTENT);
    Uri uri = new Uri.Builder().appendPath("proximity").appendPath(id).build();
    intent.setDataAndType(uri, "vnd.geofencing.region/update");
    return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
  }

  public Set<String> getWatchedRegionIds() {
    return new HashSet<String>(geofenceStore.getGeofences());
  }

  @Override
  public IBinder onBind(Intent intent) {
    return binder;
  }
}
