package com.trinity.courierapp.Service;

import com.trinity.courierapp.DTO.CoordinateRecord;
import com.trinity.courierapp.DTO.GeocodingResult;
import com.trinity.courierapp.Entity.OrderTypeEnum;
import com.trinity.courierapp.Repository.CourierRepository;
import com.trinity.courierapp.Repository.OrderRepository;
import com.trinity.courierapp.Util.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private GoogleMapsService googleMapsService;

    @Autowired
    private CommonUtils commonUtils;

    // public Order createOrder(OrderInitRequestDto orderInitRequestDto) {}

    public CalcResult calculatePrice(String srcAddress, String destAddress) {

        CoordinateRecord bishkekGps = new CoordinateRecord(42.871374, 74.582327);
        CoordinateRecord oshGps = new CoordinateRecord(40.526464, 72.806236);

        GeocodingResult srcGeocode = googleMapsService.geocodeAddress(srcAddress);
        GeocodingResult destGeocode = googleMapsService.geocodeAddress(destAddress);
        OrderTypeEnum currentOrderType = OrderTypeEnum.INTER_REGION;
        double km = commonUtils.findDistanceKm(srcGeocode.lat(), srcGeocode.lng(), destGeocode.lat(),
                destGeocode.lng());
        CoordinateRecord destGps = new CoordinateRecord(destGeocode.lat(), destGeocode.lng());
        CoordinateRecord srcGps = new CoordinateRecord(srcGeocode.lat(), srcGeocode.lng());
        Map<String, Object> directions = googleMapsService.doGetDirections(srcGeocode.lat(), srcGeocode.lng(),
                destGeocode.lat(), destGeocode.lng());
        double distanceAtoBMeters = googleMapsService.extractDistance(directions);
        double price = 0;
        int cityRadInMeters = 15000;
        int localOrderDistance = 15000;
        double priceKmRate = 0;

        if ((Objects.equals(srcGeocode.region(), "Bishkek") && Objects.equals(destGeocode.region(), "Bishkek"))
                ||
                (Objects.equals(srcGeocode.region(), "Osh City") && Objects.equals(destGeocode.region(), "Osh City"))
                ||
                commonUtils.twoPointsInsideCity(bishkekGps, srcGps, destGps, cityRadInMeters)
                ||
                commonUtils.twoPointsInsideCity(oshGps, srcGps, destGps, cityRadInMeters)) {
            priceKmRate = 50;
            price = priceKmRate * distanceAtoBMeters;
            currentOrderType = OrderTypeEnum.LOCAL;
        } else if (distanceAtoBMeters > localOrderDistance
                && (destGeocode.region().equals("Bishkek") || destGeocode.region().equals("Osh City"))) {
            priceKmRate = 60;
            price = priceKmRate * distanceAtoBMeters;
        } else if (distanceAtoBMeters > localOrderDistance) {
            priceKmRate = 70;
            price = priceKmRate * distanceAtoBMeters;
        } else if (distanceAtoBMeters < localOrderDistance) {
            priceKmRate = 55;
            price = priceKmRate * distanceAtoBMeters;
            currentOrderType = OrderTypeEnum.LOCAL;
        } else {
            price = 1;
            return new CalcResult(price, currentOrderType, priceKmRate);
        }

        return new CalcResult(price, currentOrderType, priceKmRate);
    }

    public double getDistanceAtoBMeters(String pointA, String pointB) {
        GeocodingResult srcGeocode = googleMapsService.geocodeAddress(pointA);
        GeocodingResult destGeocode = googleMapsService.geocodeAddress(pointB);
        Map<String, Object> directions = googleMapsService.doGetDirections(srcGeocode.lat(), srcGeocode.lng(),
                destGeocode.lat(), destGeocode.lng());
        return googleMapsService.extractDistance(directions);
    }

    public double getDurationAtoBMinutes(String pointA, String pointB) {
        GeocodingResult srcGeocode = googleMapsService.geocodeAddress(pointA);
        GeocodingResult destGeocode = googleMapsService.geocodeAddress(pointB);
        Map<String, Object> directions = googleMapsService.doGetDirections(srcGeocode.lat(), srcGeocode.lng(),
                destGeocode.lat(), destGeocode.lng());
        double durationInSec = googleMapsService.extractDuration(directions);
        double durationInMinutes = Math.round(durationInSec / 60);
        return durationInMinutes;
    }

    public String getRouteAtoB(String pointA, String pointB) {
        GeocodingResult srcGeocode = googleMapsService.geocodeAddress(pointA);
        GeocodingResult destGeocode = googleMapsService.geocodeAddress(pointB);
        Map<String, Object> directions = googleMapsService.doGetDirections(srcGeocode.lat(), srcGeocode.lng(),
                destGeocode.lat(), destGeocode.lng());
        return googleMapsService.extractPolyline(directions);
    }

    public String getRouteAtoB(CoordinateRecord srcCoordinates, String pointB) {
        GeocodingResult destGeocode = googleMapsService.geocodeAddress(pointB);
        Map<String, Object> directions = googleMapsService.doGetDirections(srcCoordinates.lat(), srcCoordinates.lng(),
                destGeocode.lat(), destGeocode.lng());
        return googleMapsService.extractPolyline(directions);
    }

    // public String getEta(){
    //
    // for courier destination
    // Map<String, Object> courierToADirections = googleMapsService.
    // doGetDirections(courier , , srcGeocode.lat(), srcGeocode.lng());
    // double distanceCourierToA =
    // googleMapsService.extractDistance(courierToADirections);
    //
    // Map<String, Object> courierToBDirections = googleMapsService.
    // doGetDirections(srcGeocode.lat(), srcGeocode.lng(), destGeocode.lat(),
    // destGeocode.lng());
    // double distanceCourierToB =
    // googleMapsService.extractDistance(courierToBDirections);

    //// always add the from a to b and from courier to a
    // }

    public record CalcResult(double price, OrderTypeEnum orderType, double priceRate) {
    }
}