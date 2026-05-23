package com.hotel.cms.promo.dto;

import java.math.BigDecimal;

public record PromoValidationResponse(
        boolean valid,
        String code,
        BigDecimal discountAmount,
        String message
) {}
