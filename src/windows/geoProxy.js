cordova.define("aerogear-cordova-geo.geofencingProxy", function(require, exports, module) { /* AeroGear Cordova Plugin
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
module.exports = {
    register: function (onNotification) {
        //function requestLocationAccess() {
        //    Windows.Devices.Geolocation.Geolocator.requestAccessAsync().done(
        //    function (accessStatus) {
        //        switch (accessStatus) {
        //            case Windows.Devices.Geolocation.GeolocationAccessStatus.allowed:

        //                // register for geofence state change events
        //                geofenceMonitor.addEventListener("geofencestatechanged", onGeofenceStateChanged);
        //                geofenceMonitor.addEventListener("statuschanged", onGeofenceStatusChanged);
        //                break;

        //            case Windows.Devices.Geolocation.GeolocationAccessStatus.denied:
        //                WinJS.log && WinJS.log("Access to location is denied.", "sample", "error");
        //                break;

        //            case Windows.Devices.Geolocation.GeolocationAccessStatus.unspecified:
        //                WinJS.log && WinJS.log("Unspecified error!", "sample", "error");
        //                break;
        //        }
        //    },
        //    function (err) {
        //        WinJS.log && WinJS.log(err, "sample", "error");
        //    });
        //}

        //requestLocationAccess();

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

            var dwellTimeSpan = 36000;
            var durationTimeSpan = new Number(0); // duration needs to be set since start time is set below
            var startDateTime = new Date(); // if you don't set start time in JavaScript the start time defaults to 1/1/1601

            var geofenceMonitor = Windows.Devices.Geolocation.Geofencing.GeofenceMonitor.current;
            geofence = new Windows.Devices.Geolocation.Geofencing.Geofence(fenceKey, geocircle, mask, singleUse, dwellTimeSpan, startDateTime, durationTimeSpan);
            geofenceMonitor.geofences.push(geofence);
        } catch (ex) {
            WinJS.log && WinJS.log(ex.toString(), "sample", "error");
        }
    }
};

require("cordova/exec/proxy").add("Geofencing", module.exports);
});
