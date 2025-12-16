package com.trinity.courierapp.DTO;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SavedMethodsDto {
    private Integer id;
    private String brand;
    private String last4;
    private Long expMonth;
    private Long expYear;
    private String stripePaymentMethodId;
}
