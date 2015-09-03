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

#import <CoreLocation/CoreLocation.h>
#import "GeofencingPlugin.h"

@implementation GeofencingPlugin

@synthesize callbackId;
@synthesize locationManager;

- (CDVPlugin*)initWithWebView:(UIWebView*)theWebView {
    self = (GeofencingPlugin*) [super initWithWebView:theWebView];
    if (self) {
        self.locationManager = [[CLLocationManager alloc] init];
        self.locationManager.delegate = self; // Tells the location manager to send updates to this object
        self.locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    }

    [self.locationManager startUpdatingLocation];
    return self;
}

- (void)register:(CDVInvokedUrlCommand *)command {
    self.callbackId = command.callbackId;
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)addRegion:(CDVInvokedUrlCommand *)command {
    [self checkMonitoringStatus];

    NSMutableDictionary *options = [self parseParameters:command];
    NSString *regionId = options[@"fid"];
    NSString *latitude = options[@"latitude"];
    NSString *longitude = options[@"longitude"];
    NSString *message = options[@"message"];
    double radius = [options[@"radius"] doubleValue];
    if (radius > locationManager.maximumRegionMonitoringDistance) {
        radius = locationManager.maximumRegionMonitoringDistance;
    }

    NSString *identifier = [NSString stringWithFormat:@"cordovaGeofencing:%@|%@", regionId, message ? message : @"You have {0} your point of interest [left|entered]"];
    CLLocationCoordinate2D coordinate2D = CLLocationCoordinate2DMake([latitude doubleValue], [longitude doubleValue]);
    CLRegion * region = [[CLRegion alloc] initCircularRegionWithCenter:coordinate2D radius:radius identifier:identifier];

    [self.locationManager startMonitoringForRegion:region];
    [self returnStatusOk:command];
}

- (void)removeRegion:(CDVInvokedUrlCommand *)command {
    NSString *regionId = [[self parseParameters:command] objectForKey:@"fid"];

    for (CLRegion *region in [locationManager monitoredRegions]) {
        if ([regionId isEqualToString:[self getRegionId:region]]) {
            [locationManager stopMonitoringForRegion:region];
        }
    }

    [self returnStatusOk:command];
}

- (void)getWatchedRegionIds:(CDVInvokedUrlCommand *)command {
    NSSet *regions = [locationManager monitoredRegions];
    NSMutableArray *watchedRegions = [NSMutableArray array];
    for (CLRegion *region in regions) {
        [watchedRegions addObject:[self getRegionId:region]];
    }

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:watchedRegions];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (NSString *)getRegionId:(CLRegion *)region {
    NSRegularExpression *idExpression = [NSRegularExpression regularExpressionWithPattern:@":(\\d+?)\\|" options:0 error:nil];
    NSString *identifier = region.identifier;
    NSTextCheckingResult* idMatch = [idExpression matchesInString:identifier options:0 range:NSMakeRange(0, [identifier length])][0];

    return [identifier substringWithRange:[idMatch rangeAtIndex:1]];
}

- (void)locationManager:(CLLocationManager *)manager didEnterRegion:(CLRegion *)region {
    [self notify:region withStatus:@YES];
}

- (void)locationManager:(CLLocationManager *)manager didExitRegion:(CLRegion *)region {
    [self notify:region withStatus:@NO];
}

- (void)notify:(CLRegion *)region withStatus:(NSNumber*)status{
    NSString *regionId = [self getRegionId:region];

    NSMutableDictionary *data = [[NSMutableDictionary alloc] init];
    data[@"fid"] = regionId;
    data[@"status"] = status;

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:data];
    [result setKeepCallback:@YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];

    //Local notification if the app is in the background
    NSString *statusMessage = [self parseMessage:region status:status];
    UILocalNotification *notification = [[UILocalNotification alloc] init];
    notification.fireDate = [NSDate date];
    NSTimeZone* timezone = [NSTimeZone defaultTimeZone];
    notification.timeZone = timezone;
    notification.alertBody = statusMessage;
    notification.alertAction = @"Show";
    notification.soundName = UILocalNotificationDefaultSoundName;
    [[UIApplication sharedApplication] scheduleLocalNotification:notification];
}

- (NSString *)parseMessage:(CLRegion *)region status:(NSNumber *)status {
    NSString* identifier = region.identifier;
    NSRegularExpression *messageParams = [NSRegularExpression regularExpressionWithPattern:@"\\[(.+?)\\|(.+?)\\]" options:0 error:nil];
    NSArray* matches = [messageParams matchesInString:identifier options:0 range:NSMakeRange(0, [identifier length])];
    NSUInteger index = [identifier rangeOfString:@"|"].location;
    NSString* statusMessage;
    if (matches) {
        NSTextCheckingResult* match = matches[0];
        NSString* exit = [identifier substringWithRange:[match rangeAtIndex:1]];
        NSString* enter = [identifier substringWithRange:[match rangeAtIndex:2]];
        NSRange range = NSMakeRange(index + 1, [match range].location - index);
        NSMutableString* template = [identifier substringWithRange:range].mutableCopy;
        NSRegularExpression *templateParam = [NSRegularExpression regularExpressionWithPattern:@"\\{0\\}" options:0 error:nil];
        [templateParam replaceMatchesInString:template options:0 range:NSMakeRange(0, template.length) withTemplate:(status ? enter : exit)];
        statusMessage = template;
    } else {
        statusMessage = [identifier substringWithRange:NSMakeRange(index, identifier.length)];
    }
    return statusMessage;
}

- (void)locationManager:(CLLocationManager *)manager didStartMonitoringForRegion:(CLRegion *)region {
    NSLog(@"starting monitoring region: %@", region);
}

- (void)locationManager:(CLLocationManager *)manager monitoringDidFailForRegion:(CLRegion *)region withError:(NSError *)error {
    NSLog(@"Region monitoring failed with error: %@", [error localizedDescription]);
}

- (void)checkMonitoringStatus {
    if (![self isLocationServicesEnabled]) {
        [self returnLocationError:PERMISSIONDENIED withMessage:@"Location services are not enabled."];
        return;
    }

    if ([self.locationManager respondsToSelector:@selector(requestAlwaysAuthorization)]) {
        [self.locationManager requestAlwaysAuthorization];
    }

    if (![self isAuthorized]) {
        NSString *errorMessage = nil;
        BOOL authStatusAvailable = [CLLocationManager respondsToSelector:@selector(authorizationStatus)]; // iOS 4.2+

        if (authStatusAvailable) {
            NSUInteger code = [CLLocationManager authorizationStatus];
            if (code == kCLAuthorizationStatusNotDetermined) {
                // could return POSITION_UNAVAILABLE but need to coordinate with other platforms
                errorMessage = @"User undecided on application's use of location services.";
            } else if (code == kCLAuthorizationStatusRestricted) {
                errorMessage = @"Application's use of location services is restricted.";
            }
        }
        // PERMISSIONDENIED is only PositionError that makes sense when authorization denied
        [self returnLocationError:PERMISSIONDENIED withMessage:errorMessage];

        return;
    }

    if (![self isRegionMonitoringEnabled]) {
        [self returnLocationError:GEOFENCINGPERMISSIONDENIED withMessage:@"Geofencing services are not authorized."];
    }
}

- (BOOL)isAuthorized {
    BOOL authorizationStatusClassPropertyAvailable = [CLLocationManager respondsToSelector:@selector(authorizationStatus)]; // iOS 4.2+

    if (authorizationStatusClassPropertyAvailable) {
        NSUInteger authStatus = [CLLocationManager authorizationStatus];
        return (authStatus == kCLAuthorizationStatusAuthorized) || (authStatus == kCLAuthorizationStatusNotDetermined);
    }

    // by default, assume YES (for iOS < 4.2)
    return YES;
}

- (BOOL)isLocationServicesEnabled {
    BOOL locationServicesEnabledInstancePropertyAvailable = [self.locationManager respondsToSelector:@selector(locationServicesEnabled)]; // iOS 3.x
    BOOL locationServicesEnabledClassPropertyAvailable = [CLLocationManager respondsToSelector:@selector(locationServicesEnabled)]; // iOS 4.x

    if (locationServicesEnabledClassPropertyAvailable) { // iOS 4.x
        return [CLLocationManager locationServicesEnabled];
    } else if (locationServicesEnabledInstancePropertyAvailable) { // iOS 2.x, iOS 3.x
        return [(id) self.locationManager locationServicesEnabled];
    } else {
        return NO;
    }
}

- (BOOL)isRegionMonitoringEnabled {
    if ([CLLocationManager respondsToSelector:@selector(regionMonitoringEnabled)]) {
        return [CLLocationManager regionMonitoringEnabled];
    }
    if ([CLLocationManager respondsToSelector:@selector(regionMonitoringAvailable)]) {
        return [CLLocationManager regionMonitoringAvailable];
    }

    // by default, assume NO
    return NO;
}

- (void)returnStatusOk:(CDVInvokedUrlCommand *)command {
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)returnLocationError:(NSUInteger)errorCode withMessage:(NSString *)errorMessage {
    NSMutableDictionary *posError = [NSMutableDictionary dictionaryWithCapacity:2];

    posError[@"code"] = @(errorCode);
    posError[@"message"] = errorMessage ? errorMessage : @"";
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsDictionary:posError];

    [self.commandDelegate sendPluginResult:result callbackId:callbackId];
}

- (id)parseParameters:(CDVInvokedUrlCommand*)command {
    NSArray *data = [command arguments];
    if (data.count == 1) {
        return data[0];
    }
    return Nil;
}

@end
