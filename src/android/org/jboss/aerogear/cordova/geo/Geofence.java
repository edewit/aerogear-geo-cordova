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
package org.jboss.aerogear.cordova.geo;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pojo holding the geofence info
 */
public class Geofence {
    public static final String DEFAULT_MESSAGE = "You have {0} your point of interest [left|entered]";
    private static final Pattern REGEX = Pattern.compile("\\[(.+?)\\|(.+?)\\]");

    private final String id;
    private final double latitude;
    private final double longitude;
    private final float radius;
    private final String message;
    private long expirationDuration;
    private int transitionType;

    /**
     * @param geofenceId The Geofence's request ID
     * @param latitude   Latitude of the Geofence's center. The value is not checked for validity.
     * @param longitude  Longitude of the Geofence's center. The value is not checked for validity.
     * @param radius     Radius of the geofence circle. The value is not checked for validity
     * @param expiration Geofence expiration duration in milliseconds The value is not checked for
     *                   validity.
     * @param transition Type of Geofence transition. The value is not checked for validity.
     */
    public Geofence(
            String geofenceId,
            double latitude,
            double longitude,
            float radius,
            long expiration,
            int transition,
            String message) {
        this.id = geofenceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.expirationDuration = expiration;
        this.transitionType = transition;
        this.message = message;
    }

    public Geofence(String id, double latitude, double longitude, float radius) {
        this(id, latitude, longitude, radius, -1, 3, DEFAULT_MESSAGE);
    }

    public Geofence(String id, double latitude, double longitude, float radius, String message) {
        this(id, latitude, longitude, radius, -1, 3, message == null || "".equals(message) ? DEFAULT_MESSAGE : message);
    }

    // Instance field getters

    /**
     * Get the geofence ID
     *
     * @return A Geofence ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the geofence latitude
     *
     * @return A latitude value
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Get the geofence longitude
     *
     * @return A longitude value
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Get the geofence radius
     *
     * @return A radius value
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Get the geofence expiration duration
     *
     * @return Expiration duration in milliseconds
     */
    public long getExpirationDuration() {
        return expirationDuration;
    }

    /**
     * Get the geofence transition type
     *
     * @return Transition type (see Geofence)
     */
    public int getTransitionType() {
        return transitionType;
    }

    /**
     * Get the message to be used to notify the user.
     *
     * @return the message for notification
     */
    public String getMessage() {
        return message;
    }

    public String getMessage(boolean entering) {
        Matcher matcher = REGEX.matcher(message);
        if (matcher.find()) {
            return MessageFormat.format(message.substring(0, matcher.start()), matcher.group(entering ? 2 : 1));
        } else {
            return message;
        }
    }
    
}
