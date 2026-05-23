package com.hotel.cms.promo.dto;

import com.hotel.cms.common.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PromoCodeRequest(
        @NotBlank String hotelId,
        @NotBlank String code,
        String description,
        @NotNull DiscountType discountType,
        @NotNull BigDecimal discountValue,
        BigDecimal minBookingAmount,
        BigDecimal maxDiscountAmount,
        Integer usageLimit,
        LocalDate startDate,
        LocalDate endDate,
        Boolean active
) {}
