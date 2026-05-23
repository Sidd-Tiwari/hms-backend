package com.hotel.cms.booking.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record BookingRequest(
        @NotBlank String customerId,
        @NotBlank String hotelId,
        @NotNull LocalDate checkInDate,
        @NotNull LocalDate checkOutDate,
        int adults,
        int children,
        BigDecimal discountAmount,
        BigDecimal taxAmount,
        String specialRequest,
        String source,
        List<BookingRoomLine> rooms
) {
    public record BookingRoomLine(
            @NotBlank String roomId,
            @NotBlank String roomTypeId,
            BigDecimal pricePerNight
    ) {}
}
