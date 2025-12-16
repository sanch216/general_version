package com.trinity.courierapp.Util;

import com.trinity.courierapp.DTO.CoordinateRecord;
import org.springframework.stereotype.Component;

@Component
public class CommonUtils {

    public double findDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // meters
    }

    public boolean pointInsideCity(CoordinateRecord cityGps, CoordinateRecord gpsB, int radiusMeters) {
        double dist = haversine(cityGps.lat(), cityGps.lng(), gpsB.lat(), gpsB.lng());
        return dist <= radiusMeters;
    }

    public boolean twoPointsInsideCity(CoordinateRecord cityGps, CoordinateRecord gpsA, CoordinateRecord gpsB, int radiusMeters) {
        double distA = haversine(cityGps.lat(), cityGps.lng(), gpsA.lat(), gpsA.lng());
        double distB = haversine(cityGps.lat(), cityGps.lng(), gpsB.lat(), gpsB.lng());
        return distA <= radiusMeters && distB <= radiusMeters;
    }


}
