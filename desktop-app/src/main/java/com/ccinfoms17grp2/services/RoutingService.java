package com.ccinfoms17grp2.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RoutingService {

    private static final String ORS_BASE_URL = "http://localhost:8080/ors/v2";
    private static final int TIMEOUT_MS = 10000;

    public static class RouteResult {
        private final double distanceKm;
        private final double durationMinutes;

        public RouteResult(double distanceKm, double durationMinutes) {
            this.distanceKm = distanceKm;
            this.durationMinutes = durationMinutes;
        }

        public double getDistanceKm() {
            return distanceKm;
        }

        public double getDurationMinutes() {
            return durationMinutes;
        }

        @Override
        public String toString() {
            return String.format("RouteResult{distance=%.2f km, duration=%.1f min}", distanceKm, durationMinutes);
        }
    }

    public RouteResult calculateRoute(double startLat, double startLon, double endLat, double endLon) throws Exception {
        String urlString = String.format("%s/directions/driving-car?start=%f,%f&end=%f,%f",
                ORS_BASE_URL, startLon, startLat, endLon, endLat);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Routing service returned status code: " + responseCode);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray routes = jsonResponse.getAsJsonArray("features");
        
        if (routes.size() == 0) {
            return null;
        }

        JsonObject firstRoute = routes.get(0).getAsJsonObject();
        JsonObject properties = firstRoute.getAsJsonObject("properties");
        JsonObject summary = properties.getAsJsonObject("summary");
        
        double distanceMeters = summary.get("distance").getAsDouble();
        double durationSeconds = summary.get("duration").getAsDouble();

        return new RouteResult(distanceMeters / 1000.0, durationSeconds / 60.0);
    }

    public List<RouteResult> calculateMultipleRoutes(double startLat, double startLon, List<double[]> destinations) {
        List<RouteResult> results = new ArrayList<>();
        for (double[] dest : destinations) {
            try {
                RouteResult route = calculateRoute(startLat, startLon, dest[0], dest[1]);
                results.add(route != null ? route : new RouteResult(999999, 999999));
            } catch (Exception e) {
                results.add(new RouteResult(999999, 999999));
            }
        }
        return results;
    }
}
