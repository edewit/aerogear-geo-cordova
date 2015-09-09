/* AeroGear Cordova Plugin
 * https://github.com/aerogear/aerogear-pushplugin-cordova
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

var Background = Windows.ApplicationModel.Background;
var geofenceTask;
var geolocator = new Windows.Devices.Geolocation.Geolocator();
var getGeopositionPromise;
var taskName = "AeroGearGeofencingTask";
var taskEntryPoint = "plugins/aerogear-cordova-geo/geofencebackgroundtask.js";
var geofenceEventsData;
var geofenceEventsListView;
var disposed;

function getGeopositionAsync() {
    var geolocator = new Windows.Devices.Geolocation.Geolocator();

    getGeopositionPromise = geolocator.getGeopositionAsync();
    getGeopositionPromise.done(
        function (pos) {
            var coord = pos.coordinate;

        },
        function (err) {
            if (!disposed) {
            }
        }
    );
}

function registerBackgroundTask(fail) {
    try {
        Background.BackgroundExecutionManager.requestAccessAsync().done(
            function (backgroundAccessStatus) {
                var builder = new Windows.ApplicationModel.Background.BackgroundTaskBuilder();

                builder.name = taskName;
                builder.taskEntryPoint = taskEntryPoint;
                builder.setTrigger(new Windows.ApplicationModel.Background.TimeTrigger(15, false));

                geofenceTask = builder.register();

                //geolocTask.addEventListener("completed", onCompleted);

                switch (backgroundAccessStatus) {
                    case Background.BackgroundAccessStatus.unspecified:
                    case Background.BackgroundAccessStatus.denied:
                        fail("This application must be added to the lock screen before the background task will run.");
                        break;

                    default:
                        getGeopositionAsync();
                        break;
                }
            },
            function (e) {
                fail(e.toString());
            }
        );
    } catch (ex) {
        // HRESULT_FROM_WIN32(ERROR_NOT_SUPPORTED) === -2147024846
        if (ex.number === -2147024846) {
           fail("Location Simulator not supported.  Could not get permission to add application to the lock screen, this application must be added to the lock screen before the background task will run.");
        } else {
            fail(ex.toString());
        }
    }
}

module.exports = {
    register: function (onNotification, fail) {

        var iter = Windows.ApplicationModel.Background.BackgroundTaskRegistration.allTasks.first();
        var hascur = iter.hasCurrent;
        while (hascur) {
            var cur = iter.current.value;
            if (cur.name === taskName) {
                geofenceTask = cur;
                break;
            }
            hascur = iter.moveNext();
        }

        if (geofenceTask) {
            try {
                var backgroundAccessStatus = Background.BackgroundExecutionManager.getAccessStatus();
                switch (backgroundAccessStatus) {
                    case Background.BackgroundAccessStatus.unspecified:
                    case Background.BackgroundAccessStatus.denied:
                        fail("This application must be added to the lock screen before the background task will run.");
                        break;

                    default:
                        break;
                }
            } catch (ex) {

                // HRESULT_FROM_WIN32(ERROR_NOT_SUPPORTED) === -2147024846
                if (ex.number === -2147024846) {
                    fail("Location Simulator not supported. Could not determine lock screen status, be sure that the application is added to the lock screen.");
                } else {
                    fail(ex.toString());
                }
            }
            registerBackgroundTask(fail);
        } else {
            registerBackgroundTask(fail);
        }

        var geofenceMonitor = Windows.Devices.Geolocation.Geofencing.GeofenceMonitor.current;
        geofenceMonitor.addEventListener("geofencestatechanged", function () {
            args.target.readReports().forEach(function processReport(report) {
                var state = report.newState;
                var geofence = report.geofence;
                var eventDescription = getTimeStampedMessage(geofence.id);

                if (state === Geolocation.Geofencing.GeofenceState.removed) {
                    var reason = report.removalReason;

                    if (reason === Geolocation.Geofencing.GeofenceRemovalReason.expired) {
                        eventDescription += " (Removed/Expired)";
                    } else if (reason === Geolocation.Geofencing.GeofenceRemovalReason.used) {
                        eventDescription += " (Removed/Used)";
                    }

                    // remove the geofence from the client side geofences collection
                    removeGeofence(geofence);

                    // empty the registered geofence listbox and repopulate
                    repopulateRegisteredGeofenceData();
                } else if (state === Geolocation.Geofencing.GeofenceState.entered) {

                    // NOTE: You might want to write your app to take particular
                    // action based on whether the app has internet connectivity.
                    eventDescription += " (Entered)";
                } else if (state === Geolocation.Geofencing.GeofenceState.exited) {
                    eventDescription += " (Exited)";
                } else {
                    eventDescription += " (Unknown)";
                }

                onNotification(eventDescription);
            });
        });
    },
    addRegion: function (successCallback, errorCallback, params) {
        var geofence = null;

        params = params[0];
        try {
            var fenceKey = params.fid;

            var decimalFormatter = new Windows.Globalization.NumberFormatting.DecimalFormatter();
            var position = {
                latitude: decimalFormatter.parseDouble(params.latitude),
                longitude: decimalFormatter.parseDouble(params.longitude),
                altitude: 0
            };
            var radiusValue = decimalFormatter.parseDouble(params.radius);

            // the geofence is a circular region
            var geocircle = new Windows.Devices.Geolocation.Geocircle(position, radiusValue);

            var singleUse = false;

            // want to listen for enter geofence, exit geofence and remove geofence events
            var mask = 0;

            mask = mask | Windows.Devices.Geolocation.Geofencing.MonitoredGeofenceStates.entered;
            mask = mask | Windows.Devices.Geolocation.Geofencing.MonitoredGeofenceStates.exited;
            mask = mask | Windows.Devices.Geolocation.Geofencing.MonitoredGeofenceStates.removed;

            var dwellTimeSpan = 3000;
            var durationTimeSpan = new Number(0); // duration needs to be set since start time is set below
            var startDateTime = new Date(); // if you don't set start time in JavaScript the start time defaults to 1/1/1601

            var geofenceMonitor = Windows.Devices.Geolocation.Geofencing.GeofenceMonitor.current;
            geofence = new Windows.Devices.Geolocation.Geofencing.Geofence(fenceKey, geocircle, mask, singleUse, dwellTimeSpan, startDateTime, durationTimeSpan);
            geofenceMonitor.geofences.push(geofence);
        } catch (ex) {
            errorCallback(ex.toString());
        }
    }
};

require("cordova/exec/proxy").add("Geofencing", module.exports);
