package com.hotel.cms.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record HotelRequest(
        @NotBlank String name,
        String slug,
        String description,
        BigDecimal starRating,
        String gstNumber,
        String addressLine1,
        String addressLine2,
        @NotBlank String city,
        String state,
        String country,
        String pincode,
        String phone,
        String email,
        String website,
        String ownerUserId,
        String ownerEmail
) {}
