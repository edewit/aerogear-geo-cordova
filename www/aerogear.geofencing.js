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
 geofencing is used to provide various geofencing methods
 @status Experimental
 @class
 @returns {object} geofencing - The geofencing api
 */
var geofencing = {

  /**
   * register you app to receive geofencing alerts
   * @param {Function} [params.callback] - callback to be executed if a geofencing is entered or left
   * @param {String} [params.notifyMessage] - Message to be used for the alert defaults to 'You have {left/entered} your point of interest'
   * @returns {void}
   */
  register: function (params) {
    exec(null, null, 'Geofencing', 'register', [params]);
  },

  /**
   * @param {Function} successCallback - callback to be executed when successful added the specified geofence
   * @param {Function} errorCallback - callback to be executed when there was an error
   * @param {Object} params - objects that must have the following properties:
   * @param {String} params.fid - the fence identifier a string to identify this fence later
   * @param {String} params.latitude - the latitude of the fence
   * @param {String} params.longitude - the longitude of the fence
   * @param {String} params.radius - the radius of the fence
   * @returns {void}
   */
  addRegion: function (successCallback, errorCallback, params) {
    exec(successCallback, errorCallback, 'Geofencing', 'addRegion', [params]);
  },

  /**
   * @param {String} fid - the fence identifier of the fence to remove
   * @returns {void}
   */
  removeRegion: function (fid) {
    exec(null, null, 'Geofencing', 'removeRegion', [
      {fid: fid}
    ]);
  },

  /**
   * @param {Function} successCallback - called with the list of watched fences
   * @param {Function} errorCallback - called if there was an error fetching the fences
   * @returns {void}
   */
  getWatchedRegionIds: function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'Geofencing', 'getWatchedRegionIds', []);
  }
};

module.exports = geofencing;