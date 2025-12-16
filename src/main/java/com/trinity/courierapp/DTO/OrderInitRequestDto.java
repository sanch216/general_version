package com.trinity.courierapp.DTO;

import com.trinity.courierapp.Entity.OrderStatusEnum;
import com.trinity.courierapp.Entity.VehicleTypeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderInitRequestDto {

    private String recipientFullName;

    private String recipientPhoneNumber;

    private String srcAddress;

    private String destAddress;

    private VehicleTypeEnum vehicleType;

}
