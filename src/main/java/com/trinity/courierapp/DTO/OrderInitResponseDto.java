package com.trinity.courierapp.DTO;

import com.trinity.courierapp.Entity.OrderTypeEnum;
import com.trinity.courierapp.Entity.VehicleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OrderInitResponseDto {

    // this orderId is temporary, used for caching only, the permanent id will be set after final confirmation
    private String orderToken;

    private double price;

    private String route;

    // this is also temporary eta, after the courier is chosen, more real eta can be calculated 
    private double durationMinutes;

    private double distanceMeters;

    private OrderTypeEnum orderType;

    // the following you don't have to take in frontend, it is just for me to store in cache:
    private String recipientFullName;

    private String recipientPhoneNumber;

    private String srcAddress;

    private String destAddress;

    private VehicleTypeEnum vehicleType;

}
