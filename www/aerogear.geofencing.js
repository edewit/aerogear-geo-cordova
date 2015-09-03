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
var exec = require('cordova/exec');

/**
 the global geofencing object is the entry point for all geofencing methods
 @status Experimental
 @class
 @returns {object} geofencing - The geofencing api
 */
var geofencing = (function() {

  return {

    /**
     * register a callback to get called when the geofence is entered or left ( a geofence is a radius around a geo coordinate )
     * use addRegion to add a geofence.
     * @param {Function} callback - callback to be executed if a geofencing is entered or left
     * @returns {void}
     */
    register: function (callback) {
      exec(callback, null, 'Geofencing', 'register', []);
    },

    /**
     * Add a geofence for a specific region. The fenceId (fid) needs to be a unique string, because this is passed when the
     * notification callback is called.
     * @param {Function} successCallback - callback to be executed when successful added the specified geofence
     * @param {Function} errorCallback - callback to be executed when there was an error
     * @param {Object} params - objects that must have the following properties:
     * @param {String} params.fid - the fence identifier a string to identify this fence later
     * @param {String} params.latitude - the latitude of the fence
     * @param {String} params.longitude - the longitude of the fence
     * @param {String} params.radius - the radius of the fence
     * @param {String} params.message - the message to show the user the format is: message [left text| entered text]
     *  example: You have {0} your point of interest [left|entered] you could also create a message without left and entered 
     * @returns {void}
     */
    addRegion: function (successCallback, errorCallback, params) {
      exec(successCallback, errorCallback, 'Geofencing', 'addRegion', [params]);
    },

    /**
     * Remove a watched region for entering and leaving events by it's unique fenceId
     * @param {String} fid - the fence identifier of the fence to remove
     * @returns {void}
     */
    removeRegion: function (fid) {
      exec(null, null, 'Geofencing', 'removeRegion', [
        {fid: fid}
      ]);
    },

    /**
     * SuccessCallback will get called with all the fenceIds that are currently being watched/monitored
     * @param {Function} successCallback - called with the list of watched fences
     * @param {Function} errorCallback - called if there was an error fetching the fences
     * @returns {void}
     */
    getWatchedRegionIds: function (successCallback, errorCallback) {
      exec(successCallback, errorCallback, 'Geofencing', 'getWatchedRegionIds', []);
    }
  }
}());

module.exports = geofencing;
