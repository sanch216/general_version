package com.trinity.courierapp.Service;

import com.trinity.courierapp.Entity.RefreshToken;
import com.trinity.courierapp.Entity.User;
import com.trinity.courierapp.Repository.RefreshTokensRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refresh.expiration}")
    private Long refreshExpirationMs;

    @Autowired
    private RefreshTokensRepository repository;

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        repository.deleteByUser(user);

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setRefreshToken(UUID.randomUUID().toString());
        token.setRefreshTokenExpiry(Instant.now().plusMillis(refreshExpirationMs));

        repository.save(token);
        return token;
    }

    @Transactional
    public RefreshToken verify(String token) {
        RefreshToken refreshToken = repository.findByRefreshToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid Refresh Token"));

        if (refreshToken.getRefreshTokenExpiry().isBefore(Instant.now())) {
            repository.delete(refreshToken);
            throw new RuntimeException("Refresh Token Expired");
        }

        return refreshToken;
    }
}
