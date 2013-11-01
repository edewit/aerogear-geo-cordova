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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.jboss.aerogear.cordova.geo.GeofencingService.TAG;

/**
 * Corodova Plugin to create Geofences.
 * @author edewit@redhat.com
 */
public class GeofencingPlugin extends CordovaPlugin {

  private static String callback;
  private static String cachedRegionEvent;
  private static boolean foreground;
  private static String notifyMessage;
  private static CordovaWebView gWebView;

  public GeofencingService service;
  private ServiceConnection connection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      GeofencingService.LocalBinder binder = (GeofencingService.LocalBinder) service;
      GeofencingPlugin.this.service = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
    }
  };

  @Override
  public void onNewIntent(Intent intent) {
    fireRegionChangedEvent(intent);
  }

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Intent intent = new Intent(cordova.getActivity(), GeofencingService.class);
    cordova.getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
  }

  @Override
  public void onDestroy() {
    cordova.getActivity().unbindService(connection);
  }

  @Override
  public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
    try {
      if ("register".equals(action)) {
        gWebView = this.webView;
        JSONObject params = parseParameters(data);
        callback = (String) params.get("callback");
        if (params.has("notifyMessage")) {
          notifyMessage = (String) params.get("notifyMessage");
        }

        if (cachedRegionEvent != null) {
          sendNotification(cachedRegionEvent);
        }
      }

      if ("addRegion".equals(action)) {
        JSONObject params = parseParameters(data);
        String id = params.getString("fid");
        Log.d(TAG, "adding region " + id);
        service.addRegion(id, params.getDouble("latitude"), params.getDouble("longitude"),
            (float) params.getInt("radius"));
        callbackContext.success();
        return true;
      }
      if ("removeRegion".equals(action)) {
        JSONObject params = parseParameters(data);
        String id = params.getString("fid");
        service.removeRegion(id);
        return true;
      }
      if ("getWatchedRegionIds".equals(action)) {
        callbackContext.success(new JSONArray(service.getWachedRegionIds()));
        return true;
      }
    } catch (Exception e) {
      StringWriter writer = new StringWriter();
      PrintWriter err = new PrintWriter(writer);
      e.printStackTrace(err);
      Log.e(TAG, writer.toString());
      callbackContext.error(e.getMessage());
    }

    return false;
  }

  @Override
  public void onPause(boolean multitasking) {
    super.onPause(multitasking);
    foreground = false;
  }

  @Override
  public void onResume(boolean multitasking) {
    super.onResume(multitasking);
    foreground = true;
  }

  void fireRegionChangedEvent(final Intent intent) {
    cordova.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        sendNotification(intent.getExtras());
      }
    });
  }

  public static void sendNotification(Bundle bundle) {
    final String status = bundle.getString("status");
    final String id = bundle.getString("id");
    sendNotification(createRegionEvent(id, status));
  }

  public static void sendNotification(String id, String status) {
    sendNotification(createRegionEvent(id, status));
  }

  private static void sendNotification(String regionEvent) {
    if (callback != null && gWebView != null) {
      gWebView.sendJavascript("javascript:" + callback + "(" + regionEvent + ")");
    } else {
      cachedRegionEvent = regionEvent;
    }
  }

  private static String createRegionEvent(String id, String status) {
    return "{fid:" + id + ",status:\"" + status + "\"}";
  }

  private JSONObject parseParameters(JSONArray data) throws JSONException {
    if (data.length() == 1 && !data.isNull(0)) {
      return (JSONObject) data.get(0);
    } else {
      throw new IllegalArgumentException("Invalid arguments specified!");
    }
  }

  public static boolean isInForeground() {
    return foreground;
  }

  public static boolean isActive() {
    return gWebView != null;
  }

  public static String getNotifyMessage() {
    return notifyMessage;
  }
}
