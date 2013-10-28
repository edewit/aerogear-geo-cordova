//var exec = require('cordova.exec');

var geofencing = {

    register: function(params) {
        return cordova.exec(null, null, 'Geofencing', 'register', [params]);
    },

	/*
	Params:
	#define KEY_REGION_ID       @"fid"
	#define KEY_REGION_LAT      @"latitude"
    #define KEY_REGION_LNG      @"longitude"
    #define KEY_REGION_RADIUS   @"radius"
    #define KEY_REGION_ACCURACY @"accuracy"
	*/
     addRegion: function(successCallback, errorCallback, params) {
          return cordova.exec(successCallback, errorCallback, "Geofencing", "addRegion", [params]);
     },

     /*
	Params:
	#define KEY_REGION_ID      @"fid"
	#define KEY_REGION_LAT     @"latitude"
    #define KEY_REGION_LNG     @"longitude"
	*/
     removeRegion: function(id) {
          return cordova.exec(null, null, "Geofencing", "removeRegion", [id]);
     },

     /*
	Params:
	NONE
	*/
	getWatchedRegionIds: function(success, fail) {
		return cordova.exec(success, fail, "Geofencing", "getWatchedRegionIds", []);
	},
	
	/*
	Params:
	NONE
	*/
	getPendingRegionUpdates: function(success, fail) {
		return cordova.exec(success, fail, "Geofencing", "getPendingRegionUpdates", []);
	}
};