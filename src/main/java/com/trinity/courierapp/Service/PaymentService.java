package com.trinity.courierapp.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.trinity.courierapp.DTO.SavedMethodsDto;
import com.trinity.courierapp.Entity.PaymentDetail;
import com.trinity.courierapp.Entity.User;
import com.trinity.courierapp.Repository.PaymentDetailRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentDetailRepository paymentDetailRepository;

    public PaymentService(PaymentDetailRepository paymentDetailRepository) {
        this.paymentDetailRepository = paymentDetailRepository;
    }

    public String createIntentAndPayWithSavedMethod(Long amount, String paymentMethodId) throws StripeException {
        Long amountInCents = amount * 100;
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("kgs")
                        .setPaymentMethod(paymentMethodId)
                        .setConfirm(true)
                        .setOffSession(true)
                        .build();
        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getStatus();
//        following is for getting the stripe customer payment history
//        PaymentIntent intent = PaymentIntent.retrieve(intentId);
//        System.out.println(intent.getAmount());
//        System.out.println(intent.getStatus());
//        System.out.println(intent.getPaymentMethod());
    }


    public void saveStripeCustomerAndMethod(User user, String paymentMethodId) throws StripeException {
        // Check if user already has a Stripe customer in PaymentDetail table
        Optional<PaymentDetail> existing = paymentDetailRepository.findByUser(user);

        String stripeCustomerId;

        if (existing.isPresent()) {
            stripeCustomerId = existing.get().getStripeCustomerId();
        } else {
            // Create Stripe customer
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setMetadata(Map.of("userId", String.valueOf(user.getId())))
                    .build();
            Customer customer = Customer.create(params);
            stripeCustomerId = customer.getId();
        }

        // Attach the payment method to the Stripe customer
        PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
        pm.attach(PaymentMethodAttachParams.builder()
                .setCustomer(stripeCustomerId)
                .build());

        // Save or update PaymentDetail
        PaymentDetail paymentDetail = existing.orElseGet(PaymentDetail::new);
        paymentDetail.setUser(user);
        paymentDetail.setStripeCustomerId(stripeCustomerId);
        paymentDetail.setStripePaymentMethodId(paymentMethodId);

        paymentDetailRepository.save(paymentDetail);
    }


    public List<SavedMethodsDto> getSavedMethods(User user) throws StripeException {
        List<PaymentDetail> methods = paymentDetailRepository.findAllByUser(user);

        List<SavedMethodsDto> dtos = new ArrayList<>();


        for (PaymentDetail pd : methods) {
            PaymentMethod pm = PaymentMethod.retrieve(pd.getStripePaymentMethodId());

            SavedMethodsDto dto = new SavedMethodsDto();
            dto.setId(pd.getId());
            dto.setStripePaymentMethodId(pd.getStripePaymentMethodId());
            dto.setBrand(pm.getCard().getBrand());
            dto.setLast4(pm.getCard().getLast4());
            dto.setExpMonth(pm.getCard().getExpMonth());
            dto.setExpYear(pm.getCard().getExpYear());
            dtos.add(dto);
        }
        return dtos;
    }

}