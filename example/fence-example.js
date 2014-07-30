/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
  // Application Constructor
  initialize: function () {
    this.bindEvents();
  },
  // Bind Event Listeners
  //
  // Bind any events that are required on startup. Common events are:
  // 'load', 'deviceready', 'offline', and 'online'.
  bindEvents: function () {
    document.addEventListener('deviceready', this.onDeviceReady, false);
    document.getElementById('get_fences').addEventListener('click', this.getFences, false);
    document.getElementById('remove_fence').addEventListener('click', this.remove, false);
  },
  // deviceready Event Handler
  //
  // The scope of 'this' is the event. In order to call the 'receivedEvent'
  // function, we must explicity call 'app.receivedEvent(...);'
  onDeviceReady: function () {
    app.receivedEvent('deviceready');

    if (geofencing) {
      app.configureGeofence();
    }
  },
  onGeofenceEvent: function (event) {
    //notification comes back here
    console.log('region event id: ' + event.fid + ' got event with status: ' + event.status);
  },
  configureGeofence: function () {
    //start geofence and set notification callback
    geofencing.register({
      callback: 'app.onGeofenceEvent'
    });

    //start adding region
    var params = {
      fid: '5',
      latitude: '52.118759',
      longitude: '5.406330',
      radius: '5'
    };

    var success = function (data) {
      console.log(data);
      alert(data);
    };

    var error = function (data) {
      console.log(data);
      alert(data);
    };

    geofencing.addRegion(success, error, params);

  },
  getFences: function () {

    var success = function (data) {
      console.log(data);
      alert(data);
    };

    var error = function (data) {
      console.log(data);
      alert(data);
    };

    geofencing.getWatchedRegionIds(success, error);

  },
  remove: function() {
    geofencing.removeRegion('5');
  },
  // Update DOM on a Received Event
  receivedEvent: function (id) {
    var parentElement = document.getElementById(id);
    var listeningElement = parentElement.querySelector('.listening');
    var receivedElement = parentElement.querySelector('.received');

    listeningElement.setAttribute('style', 'display:none;');
    receivedElement.setAttribute('style', 'display:block;');

    console.log('Received Event: ' + id);
  }
};