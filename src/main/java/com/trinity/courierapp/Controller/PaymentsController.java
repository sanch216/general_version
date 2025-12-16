package com.trinity.courierapp.Controller;

import com.stripe.exception.StripeException;
import com.trinity.courierapp.DTO.PaymentIntentResponse;
import com.trinity.courierapp.DTO.SavedMethodsDto;
import com.trinity.courierapp.Entity.User;
import com.trinity.courierapp.Repository.UserRepository;
import com.trinity.courierapp.Service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/payments")
public class PaymentsController {


    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public PaymentsController(PaymentService paymentService, UserRepository userRepository) {
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }


    @PostMapping("/intent")
    public ResponseEntity<?> createIntent(@RequestParam Long amount, @RequestParam String paymentMethodId) {
        try {
            String response = paymentService.createIntentAndPayWithSavedMethod(amount, paymentMethodId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating payment"+e.getMessage());
        }
    }

    @PostMapping("/save_method") /// send me the paymentmethodId that you should generate in the frontend after getting the key
    public ResponseEntity<?> savePaymentMethod(@RequestParam String paymentMethodId, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            User user = userRepository.findByEmail(email);
            paymentService.saveStripeCustomerAndMethod(user, paymentMethodId);
            return ResponseEntity.ok("Saved successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving payment method");
        }
    }

    @GetMapping("/methods") /// just send me the jwt token in the headers that's all, nothing in the body
    public List<SavedMethodsDto> getSavedMethods(@AuthenticationPrincipal UserDetails userDetails) throws StripeException {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email);
        return paymentService.getSavedMethods(user);
    }


}






//@PostMapping("/intent")
//public ResponseEntity<?> createIntent(@RequestParam Long amount) {
//    try {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//        String clientSecret = paymentService.createIntentAndPayWithSavedMethod(amount, email);
//
//        return ResponseEntity.ok(clientSecret);
//    } catch (Exception e) {
//        return ResponseEntity.status(500).body("Error creating payment");
//    }
//}