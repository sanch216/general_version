package com.trinity.courierapp.Repository;

import com.trinity.courierapp.Entity.PaymentDetail;
import com.trinity.courierapp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface PaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {
    Optional<PaymentDetail> findByUser(User user);
    List<PaymentDetail> findAllByUser(User user);

}
