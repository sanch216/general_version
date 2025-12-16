package com.trinity.courierapp.Repository;

import com.trinity.courierapp.Entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourierRepository extends JpaRepository<Courier, Integer> {
    Optional<Courier> findByCourierUserFullName(String fullName);
    //for now all, but for future using an area with radius of a 100 km around the srcAddress
//    List<Courier> findByAvailableTrue(); error some
}
