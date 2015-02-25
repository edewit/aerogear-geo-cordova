AeroGear Geo Plugin
===================

|                 | Project Info  |
| --------------- | ------------- |
| License:        | Apache License, Version 2.0  |
| Build:          | Cordova Plugin  |
| Documentation:  | https://aerogear.org/docs/specs/aerogear-cordova/  |
| Issue tracker:  | https://issues.jboss.org/browse/AGCORDOVA  |
| Mailing lists:  | [aerogear-users](http://aerogear-users.1116366.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-users))  |
|                 | [aerogear-dev](http://aerogear-dev.1069024.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-dev))  |

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
```

## Documentation

For more details about the current release, please consult [our documentation](https://aerogear.org/docs/specs/aerogear-cordova/).

## Development

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AGCORDOVA) with some steps to reproduce it.
