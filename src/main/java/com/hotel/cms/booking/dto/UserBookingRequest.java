package com.hotel.cms.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserBookingRequest(
        @NotBlank String roomId,
        @NotNull @FutureOrPresent LocalDate checkInDate,
        @NotNull @FutureOrPresent LocalDate checkOutDate,
        @Min(1) int adults,
        @Min(0) int children,
        String promoCode,
        String specialRequest
) {}
