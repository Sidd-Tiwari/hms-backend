package com.hotel.cms.auth;

import com.hotel.cms.auth.dto.*;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.exception.BadRequestException;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.security.JwtService;
import com.hotel.cms.user.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthenticationManager authManager,
            JwtService jwtService,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Login successful", authenticate(request, null));
    }

    @PostMapping("/super-admin-login")
    public ApiResponse<AuthResponse> superAdminLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Super admin login successful", authenticate(request, "SUPER_ADMIN"));
    }

    @PostMapping("/owner-login")
    public ApiResponse<AuthResponse> ownerLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("Hotel owner login successful", authenticate(request, "HOTEL_OWNER"));
    }

    @PostMapping("/user-login")
    public ApiResponse<AuthResponse> userLogin(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok("User login successful", authenticate(request, "USER"));
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered");
        }

        String requestedRole = request.role() == null || request.role().isBlank() ? "USER" : request.role();
        if (!requestedRole.equals("USER") && !requestedRole.equals("HOTEL_OWNER")) {
            throw new BadRequestException("Only USER or HOTEL_OWNER registration is allowed");
        }

        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new NotFoundException("Role not found: " + requestedRole));

        UserAccount user = new UserAccount();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setRoles(Set.of(role));
        user = userRepository.save(user);

        return ApiResponse.ok("Registration successful", toAuthResponse(user));
    }

    @GetMapping("/me")
    public ApiResponse<AuthResponse> me(Principal principal) {
        UserAccount user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return ApiResponse.ok("Current user", toAuthResponse(user));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok("Logout successful", null);
    }

    private AuthResponse authenticate(LoginRequest request, String requiredRole) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserAccount user = userRepository.findByEmail(request.email()).orElseThrow();

        if (requiredRole != null && user.getRoles().stream().noneMatch(role -> role.getName().equals(requiredRole))) {
            throw new BadRequestException("This login page is only for " + requiredRole);
        }

        user.setLastLogin(Instant.now());
        userRepository.save(user);
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(UserAccount user) {
        var roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        String token = jwtService.generateToken(user.getEmail(), Map.of(
                "userId", user.getId(),
                "roles", roles
        ));
        return new AuthResponse(user.getId(), user.getFullName(), user.getEmail(), roles, token);
    }
}
