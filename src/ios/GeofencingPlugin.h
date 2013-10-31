//
// Created by Erik Jan de Wit on 30/10/13.
//
// To change the template use AppCode | Preferences | File Templates.
//


#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

enum CDVLocationStatus {
    PERMISSIONDENIED = 1,
    GEOFENCINGPERMISSIONDENIED = 2
};

@interface GeofencingPlugin : CDVPlugin<CLLocationManagerDelegate>
{

}
@property (nonatomic, strong) CLLocationManager* locationManager;
@property (nonatomic, copy) NSString *callbackId;
@property (nonatomic, copy) NSString *message;

- (void)register:(CDVInvokedUrlCommand*)command;
- (void)addRegion:(CDVInvokedUrlCommand *)command;
- (void)removeRegion:(CDVInvokedUrlCommand *)command;
- (void)getWatchedRegionIds:(CDVInvokedUrlCommand *)command;

@end