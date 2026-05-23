package com.hotel.cms.auth.dto;
import java.util.Set;
public record AuthResponse(String userId, String fullName, String email, Set<String> roles, String accessToken) {}
