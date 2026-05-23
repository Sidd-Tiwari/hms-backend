package com.hotel.cms.payment.dto;
import com.hotel.cms.common.enums.*; import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record PaymentRequest(@NotBlank String bookingId, @NotNull BigDecimal amount, @NotNull PaymentMethod paymentMethod, String transactionId, PaymentStatus paymentStatus) {}
