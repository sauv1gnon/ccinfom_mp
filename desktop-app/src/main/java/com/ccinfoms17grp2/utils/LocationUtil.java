package com.ccinfoms17grp2.utils;

/**
 * Utility class for calculating geographical distances using the Haversine formula.
 */
public class LocationUtil {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculate the distance between two geographical points using the Haversine formula.
     *
     * @param lat1 Latitude of the first point
     * @param lon1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lon2 Longitude of the second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Validate latitude value.
     *
     * @param latitude The latitude to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * Validate longitude value.
     *
     * @param longitude The longitude to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }
}
