package com.trinity.courierapp.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payment_details")
public class PaymentDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_details_id_gen")
    @SequenceGenerator(name = "payment_details_id_gen", sequenceName = "payment_details_payment_id_seq", allocationSize = 1)
    @Column(name = "payment_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Size(max = 255)
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Size(max = 255)
    @Column(name = "stripe_payment_method_id")
    private String stripePaymentMethodId;

    @ColumnDefault("now()")
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;


    public PaymentDetail() {}

    public PaymentDetail(User user, String stripeCustomerId, String stripePaymentMethodId) {
        this.user = user;
        this.stripeCustomerId = stripeCustomerId;
        this.stripePaymentMethodId = stripePaymentMethodId;
    }


}