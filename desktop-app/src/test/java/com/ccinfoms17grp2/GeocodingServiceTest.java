package com.ccinfoms17grp2;

import com.ccinfoms17grp2.services.GeocodingService;

public class GeocodingServiceTest {
    public static void main(String[] args) {
        GeocodingService service = new GeocodingService();
        
        System.out.println("Testing Nominatim service availability...");
        boolean available = service.isServiceAvailable();
        System.out.println("Service available: " + available);
        
        if (available) {
            System.out.println("\nTesting geocoding address: Manila...");
            try {
                GeocodingService.GeocodingResult result = service.geocodeAddress("Manila");
                if (result != null) {
                    System.out.println("Success! Location: " + result.getDisplayName());
                    System.out.println("Lat: " + result.getLatitude() + ", Lon: " + result.getLongitude());
                } else {
                    System.out.println("No results found");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Service is not available");
        }
    }
}
