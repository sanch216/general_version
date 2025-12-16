package com.trinity.courierapp.Repository;

import com.trinity.courierapp.Entity.RefreshToken;
import com.trinity.courierapp.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokensRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByRefreshToken(String token);
    void deleteByUser(User user);
}
