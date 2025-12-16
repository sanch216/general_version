package com.trinity.courierapp.Service;

import com.trinity.courierapp.DTO.CoordinateRecord;
import com.trinity.courierapp.DTO.OrderInitResponseDto;
import com.trinity.courierapp.Entity.Courier;
import com.trinity.courierapp.Repository.CourierRepository;
import com.trinity.courierapp.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CourierService {

    @Autowired
    private CourierRepository courierRepository;

    public findCourierResult findNeareastCourier(OrderInitResponseDto dto) {
        String destAddress = dto.getDestAddress();
        String srcAddress = dto.getSrcAddress();
        double durationMinutes = dto.getDurationMinutes();



//        String route = orderService.getRouteAtoB();







        return new findCourierResult();
    }

    public record findCourierResult(){};


//    public Courier findNearestCourierByRoute(CoordinateRecord target, List<Courier> couriers) {
//        Courier nearest = null;
//        double minDistance = Double.MAX_VALUE;
//
//        for (Courier c : couriers) {
//            // get route distance from courier to target in meters
//            double dist = getRouteDistance(c.getLat(), c.getLng(), target.lat(), target.lng());
//            if (dist < minDistance) {
//                minDistance = dist;
//                nearest = c;
//            }
//        }
//
//        return nearest;
//    }

//    public double getRouteDistance(double startLat, double startLng, double endLat, double endLng) {
//        Map<String, Object> directionsJson = doGetDirections(startLat, startLng, endLat, endLng);
//        Map<String, Object> firstLeg = ((List<Map<String, Object>>) ((List<Map<String, Object>>) directionsJson.get("routes")).get(0).get("legs")).get(0);
//        return ((Number) ((Map<String, Object>) firstLeg.get("distance")).get("value")).doubleValue();
//    }



}
