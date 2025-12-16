package com.trinity.courierapp.Controller;

import com.trinity.courierapp.DTO.AuthRequestDto;
import com.trinity.courierapp.DTO.AuthResponseDto;
import com.trinity.courierapp.DTO.ClientRegistrationRequestDto;
import com.trinity.courierapp.DTO.CourierRegistrationRequestDto;
import com.trinity.courierapp.Entity.Courier;
import com.trinity.courierapp.Entity.RefreshToken;
import com.trinity.courierapp.Entity.User;
import com.trinity.courierapp.Repository.RefreshTokensRepository;
import com.trinity.courierapp.Repository.UserRepository;
import com.trinity.courierapp.Repository.CourierRepository;
import com.trinity.courierapp.Security.JwtUtils;
import com.trinity.courierapp.Service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokensRepository refreshTokensRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public AuthResponseDto authenticateUser(@Valid @RequestBody AuthRequestDto authRequestDto) { /// bindingResult for errors (if I return request entity)
        Authentication authentication = authenticationManager.authenticate(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        authRequestDto.getEmail(),
                        authRequestDto.getPassword()));

        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtils.generateToken(userDetails.getUsername());
        User user = userRepository.findByEmail(userDetails.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        String refreshedToken = refreshToken.getRefreshToken();

        return new AuthResponseDto(accessToken, refreshedToken);
    }

    @PostMapping("/client_signup")
    public String registerClient(@Valid @RequestBody ClientRegistrationRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return "Email Already Exists";
        }
        final User newUser = new User(dto.getFullName(), dto.getPhoneNumber(),
                passwordEncoder.encode(dto.getPassword()), dto.getEmail(), dto.getUserType());
        userRepository.save(newUser);
        return "Registration Successful";
    }

    @PostMapping("/courier_signup")
    public String registerCourier(@Valid @RequestBody CourierRegistrationRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            return "Email Already Exists";
        }
        final User newUser = new User(dto.getFullName(), dto.getPhoneNumber(), dto.getPassword(), dto.getEmail(),
                dto.getUserType());
        userRepository.save(newUser);
        final Courier courier = new Courier(dto.getCourierGps(), dto.getVehicleType(), dto.getCourierStatus(),
                dto.getVehicleNumber());
        courier.setCourierUser(newUser);
        courierRepository.save(courier);
        return "Registration Successful";
    }

    @PostMapping("/logout")
    public String logout(@Valid @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email);
        refreshTokensRepository.deleteByUser(user);
        return "Logout Successful";
    }


    /// from front in json I need to receive in body: {"refreshToken": "abcd-1234-efgh-5678"}
    @PostMapping("/refresh")
    public AuthResponseDto refresh(@Valid @RequestBody Map<String,String> body) {
        String requestToken = body.get("refreshToken");

        RefreshToken verified = refreshTokenService.verify(requestToken);

        String newAccess = jwtUtils.generateToken(verified.getUser().getEmail());

        return new AuthResponseDto(newAccess, requestToken);
    }
}
