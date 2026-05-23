package com.hotel.cms.promo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PromoValidationRequest(
        @NotBlank String hotelId,
        @NotBlank String code,
        @NotNull BigDecimal bookingAmount
) {}
