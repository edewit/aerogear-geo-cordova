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
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static org.jboss.aerogear.cordova.geo.GeofencingService.TAG;

/**
 * Corodova Plugin to create Geofences.
 *
 * @author edewit@redhat.com
 */
public class GeofencingPlugin extends CordovaPlugin {

  private static JSONObject cachedRegionEvent;
  private static boolean foreground;
  private static List<PluginCommand> pendingActions = new ArrayList<PluginCommand>();

  private Timer timer = new Timer();


  private GeofencingService service;
  private static CallbackContext callbackContext;
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
      return invokeService(new PluginCommand(action, data, callbackContext));
    } catch (Exception e) {
      StringWriter writer = new StringWriter();
      PrintWriter err = new PrintWriter(writer);
      e.printStackTrace(err);
      Log.e(TAG, writer.toString());
      callbackContext.error(e.getMessage());
    }

    return false;
  }


  private boolean invokeService(final PluginCommand pluginCommand) throws JSONException {
    if (service != null) {
      if ("register".equals(pluginCommand.getAction())) {
        callbackContext = pluginCommand.getCallbackContext();
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);

        if (cachedRegionEvent != null) {
          sendNotification(cachedRegionEvent);
        }

        return true;
      }
      if ("addRegion".equals(pluginCommand.getAction())) {
        JSONObject params = parseParameters(pluginCommand.getData());
        String id = params.getString("fid");
        Log.d(TAG, "adding region " + id);
        service.addRegion(id, params.getDouble("latitude"), params.getDouble("longitude"),
            (float) params.getInt("radius"), params.optString("message"));
        pluginCommand.getCallbackContext().success();
        return true;
      }
      if ("removeRegion".equals(pluginCommand.getAction())) {
        JSONObject params = parseParameters(pluginCommand.getData());
        String id = params.getString("fid");
        service.removeRegion(id);
        return true;
      }
      if ("getWatchedRegionIds".equals(pluginCommand.getAction())) {
        pluginCommand.getCallbackContext().success(new JSONArray(service.getWatchedRegionIds()));
        return true;
      }

      return false;
    } else {
      pendingActions.add(pluginCommand);
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          if (service != null) {
            for (PluginCommand args : pendingActions) {
              try {
                invokeService(args);
              } catch (JSONException e) {
                pluginCommand.callbackContext.error(e.getMessage());
              }
            }

            pendingActions.clear();
            cancel();
          }
        }
      }, 2000);

      return true;
    }
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
    if (bundle != null) {
      final boolean status = bundle.getBoolean("status");
      final String id = bundle.getString("id");
      sendNotification(createRegionEvent(id, status));
    }
  }

  public static void sendNotification(String id, boolean status) {
    sendNotification(createRegionEvent(id, status));
  }

  private static void sendNotification(JSONObject regionEvent) {
    if (callbackContext != null) {
      PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, regionEvent);
      pluginResult.setKeepCallback(true);
      callbackContext.sendPluginResult(pluginResult);
    } else {
      cachedRegionEvent = regionEvent;
    }
  }

  private static JSONObject createRegionEvent(String id, boolean status) {
    JSONObject data = new JSONObject();
    try {
      data.put("fid", id);
      data.put("status", status);
    } catch (JSONException e) {
      throw new RuntimeException("could not create json object", e);
    }
    return data;
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
    return callbackContext != null;
  }

  private static class PluginCommand {
    private final String action;
    private final JSONArray data;
    private final CallbackContext callbackContext;

    private PluginCommand(String action, JSONArray data, CallbackContext callbackContext) {
      this.action = action;
      this.data = data;
      this.callbackContext = callbackContext;
    }

    public String getAction() {
      return action;
    }

    public JSONArray getData() {
      return data;
    }

    public CallbackContext getCallbackContext() {
      return callbackContext;
    }
  }
}
