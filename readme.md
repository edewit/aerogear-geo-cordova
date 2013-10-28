AeroGear Geo Plugin
===================

This plugin is a collection of tools to make you live easier when using geo posistioning. It provides options to easly create maps that are based [openlayers](http://openlayers.org/) and adds some helpers to do some typical things when developing mobile applications.

For instance:

```
  var options = {
  	element: 'map', // id of the div element to render the map in, default is map 
    accuracy: true, // create a circle that indicates the accuracy of the posistion
    compass: true   // use compass to indicate the dirction the device is facing
  };
  var map = new AeroGear.Map(options);
  map.watchPosition(); // start watching the posistion of the device

```

Or you want the user to define how big the geo fence must be when he comes near his found restaurant:

```
  var map = new AeroGear.Map();

  // the location of the restaurant
  var restaurant = new OpenLayers.LonLat(5.43, 52.132).transform(
      new OpenLayers.Projection("EPSG:4326"),
      map.getProjectionObject()
  );

  // draw a marker where on the location of the restaurant
  map.drawMarker(restaurant);

  // editable geo fence with an initial radius of 1000 meters
  map.drawGeoFence(restaurant, 1000);
