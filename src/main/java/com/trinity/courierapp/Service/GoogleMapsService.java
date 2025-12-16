package com.trinity.courierapp.Service;

import com.trinity.courierapp.DTO.GeocodingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GoogleMapsService {



    @Value("${google.maps.api.key}")
    public String apiKey;

    private final RestTemplate restTemplate;
    public GoogleMapsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public GeocodingResult geocodeAddress(String address) {
        Map<String, Object> json = doGeocode(address);

        List<Map<String, Object>> results = (List<Map<String, Object>>) json.get("results");
        if (results == null || results.isEmpty()) {
            throw new RuntimeException("No geocode results");
        }

        Map<String, Object> first = results.get(0);

        // --- coordinates ---
        Map<String, Object> geometry = (Map<String, Object>) first.get("geometry");
        Map<String, Object> location = (Map<String, Object>) geometry.get("location");
        double lat = ((Number) location.get("lat")).doubleValue();
        double lng = ((Number) location.get("lng")).doubleValue();

        // --- region (administrative_area_level_1) ---
        List<Map<String, Object>> components =
                (List<Map<String, Object>>) first.get("address_components");

        String region = null;
        for (Map<String, Object> c : components) {
            List<String> types = (List<String>) c.get("types");
            if (types.contains("administrative_area_level_1")) {
                region = (String) c.get("long_name");
            }
        }

        return new GeocodingResult(lat, lng, region);
    }


    public Map<String,Object> doGeocode(String address) {
        try {
            String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" +
                    UriUtils.encode(address, StandardCharsets.UTF_8) +
                    "&key=" + apiKey;

            Map<String, Object> body = restTemplate.getForObject(url, Map.class);

            if (body == null) throw new RuntimeException("No response from Google Geocoding API");

            return body;
        } catch (Exception e) {
            throw new RuntimeException("Geocode failed: " + e.getMessage(), e);
        }
    }


    public Map<String, Object> doGetDirections(double startLat, double startLng, double endLat, double endLng) {
        String url = String.format(
                "https://maps.googleapis.com/maps/api/directions/json?origin=%f,%f&destination=%f,%f&key=%s",
                startLat, startLng, endLat, endLng, apiKey
        );
        return restTemplate.getForObject(url, Map.class);
    }

    public String extractPolyline(Map<String, Object> directionsJson) {
        Map<String, Object> firstRoute = ((List<Map<String, Object>>) directionsJson.get("routes")).get(0);
        return (String) ((Map<String, Object>) firstRoute.get("overview_polyline")).get("points");
    }

    public double extractDistance(Map<String, Object> directionsJson) {
        Map<String, Object> firstLeg = ((List<Map<String, Object>>) ((List<Map<String, Object>>) directionsJson.get("routes")).get(0).get("legs")).get(0);
        return ((Number) ((Map<String, Object>) firstLeg.get("distance")).get("value")).doubleValue();
    }

    public double extractDuration(Map<String, Object> directionsJson) {
        Map<String, Object> firstLeg = ((List<Map<String, Object>>) ((List<Map<String, Object>>) directionsJson.get("routes")).get(0).get("legs")).get(0);
        return ((Number) ((Map<String, Object>) firstLeg.get("duration")).get("value")).doubleValue();
    }



}
