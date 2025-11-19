package com.ccinfoms17grp2.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for geocoding addresses using local Nominatim instance.
 */
public class GeocodingService {

    private static final String NOMINATIM_BASE_URL = "http://localhost:8081";
    private static final int TIMEOUT_MS = 5000;

    /**
     * Represents a geocoded location result.
     */
    public static class GeocodingResult {
        private final double latitude;
        private final double longitude;
        private final String displayName;

        public GeocodingResult(double latitude, double longitude, String displayName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayName = displayName;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return String.format("GeocodingResult{lat=%.6f, lon=%.6f, name='%s'}", latitude, longitude, displayName);
        }
    }

    /**
     * Geocode an address string to latitude/longitude coordinates.
     *
     * @param address The address to geocode
     * @return GeocodingResult or null if not found
     * @throws Exception if geocoding fails
     */
    public GeocodingResult geocodeAddress(String address) throws Exception {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be empty");
        }

        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString());
        String urlString = NOMINATIM_BASE_URL + "/search?q=" + encodedAddress + "&format=json&limit=1";

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "CCInfoMSHealthcareApp/1.0");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Connection", "close");

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Geocoding service returned status code: " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonArray results = JsonParser.parseString(response.toString()).getAsJsonArray();
            if (results.size() == 0) {
                return null; // No results found
            }

            JsonObject firstResult = results.get(0).getAsJsonObject();
            double lat = firstResult.get("lat").getAsDouble();
            double lon = firstResult.get("lon").getAsDouble();
            String displayName = firstResult.get("display_name").getAsString();

            return new GeocodingResult(lat, lon, displayName);
        } finally {
            conn.disconnect();
        }
    }

    public java.util.List<GeocodingResult> searchAddresses(String query, int limit) throws Exception {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query cannot be empty");
        }

        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        String urlString = NOMINATIM_BASE_URL + "/search?q=" + encodedQuery + "&format=json&limit=" + limit;

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("User-Agent", "CCInfoMSHealthcareApp/1.0");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Connection", "close");

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Search service returned status code: " + responseCode);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonArray results = JsonParser.parseString(response.toString()).getAsJsonArray();
            java.util.List<GeocodingResult> list = new java.util.ArrayList<>();
            
            for (JsonElement element : results) {
                JsonObject obj = element.getAsJsonObject();
                double lat = obj.get("lat").getAsDouble();
                double lon = obj.get("lon").getAsDouble();
                String displayName = obj.get("display_name").getAsString();
                list.add(new GeocodingResult(lat, lon, displayName));
            }
            
            return list;
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Check if the Nominatim service is reachable.
     *
     * @return true if service is available, false otherwise
     */
    public boolean isServiceAvailable() {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(NOMINATIM_BASE_URL + "/search?q=test&limit=1");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "CCInfoMSHealthcareApp/1.0");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Connection", "close");
            
            int responseCode = conn.getResponseCode();
            boolean available = responseCode == 200;
            System.out.println("[GeocodingService] Nominatim availability check: URL=" + url + ", responseCode=" + responseCode + ", available=" + available);
            return available;
        } catch (Exception e) {
            System.out.println("[GeocodingService] Nominatim availability check failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
