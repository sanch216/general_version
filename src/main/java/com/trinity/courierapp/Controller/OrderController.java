package com.trinity.courierapp.Controller;

import com.trinity.courierapp.DTO.FindCourierRequestDto;
import com.trinity.courierapp.DTO.FindCourierResponseDto;
import com.trinity.courierapp.DTO.OrderInitRequestDto;
import com.trinity.courierapp.DTO.OrderInitResponseDto;
import com.trinity.courierapp.Entity.Order;
import com.trinity.courierapp.Repository.OrderRepository;
import com.trinity.courierapp.Service.CourierService;
import com.trinity.courierapp.Service.OrderService;
import com.trinity.courierapp.Util.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private CourierService courierService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisTemplate<String, Object> redis;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public ResponseEntity<Order> cancelOrder(Order order) {
        return ResponseEntity.ok(orderRepository.save(order));
    }

    @PostMapping("/initialize")
    public ResponseEntity<OrderInitResponseDto> initOrder(OrderInitRequestDto orderRequest) {
        OrderInitResponseDto orderResponse = new OrderInitResponseDto();
        OrderService.CalcResult calcResult = orderService.calculatePrice(orderRequest.getSrcAddress(),orderRequest.getDestAddress());
        orderResponse.setPrice(calcResult.price());
        orderResponse.setOrderType(calcResult.orderType());
        orderResponse.setDistanceMeters(orderService.getDistanceAtoBMeters(orderRequest.getSrcAddress(),orderRequest.getDestAddress()));
        orderResponse.setDurationMinutes(orderService.getDurationAtoBMinutes(orderRequest.getSrcAddress(),orderRequest.getDestAddress()));
        orderResponse.setRoute(orderService.getRouteAtoB(orderRequest.getSrcAddress(),orderRequest.getDestAddress()));

        //existing info
        orderResponse.setRecipientFullName(orderRequest.getRecipientFullName());
        orderResponse.setRecipientPhoneNumber(orderRequest.getRecipientPhoneNumber());
        orderResponse.setSrcAddress(orderRequest.getSrcAddress());
        orderResponse.setDestAddress(orderRequest.getDestAddress());
        orderResponse.setVehicleType(orderRequest.getVehicleType());

        String orderToken = UUID.randomUUID().toString();
        orderResponse.setOrderToken(orderToken);
        // store in cache
        redisCache.save("OrderInitResponse:" + orderToken, orderResponse, 600);

        //so we wait 10 minutes, then user opts to continue with the order, we wait 10 minutes, if it counts down cancel order
        //after he opts tho, we reset the timer like this don't forget to inject redistemplate
//        redis.expire("order:" + orderToken, 600, TimeUnit.SECONDS);

        return ResponseEntity.ok(orderResponse);
    }


    @GetMapping("/find_courier")
    public FindCourierResponseDto findCourier(@RequestBody FindCourierRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
        String orderToken = requestDto.getOrderToken();
        redis.expire("order:" + orderToken, 900, TimeUnit.SECONDS);
        OrderInitResponseDto responseDto = redisCache.get(requestDto.getOrderToken(), OrderInitResponseDto.class);

        courierService.findNeareastCourier(responseDto);
        return new FindCourierResponseDto();
    }

    //We could check if the pricing is 70 and make it 60 if the destination is in just 10 km away from the courier's current position






}
