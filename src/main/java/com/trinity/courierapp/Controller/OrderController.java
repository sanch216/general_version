package com.trinity.courierapp.Controller;

import com.trinity.courierapp.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    @Autowired
    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }
}
