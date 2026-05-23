package com.hotel.cms.room.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal;
public record RoomTypeRequest(@NotBlank String hotelId, @NotBlank String name, String description, @NotNull BigDecimal basePrice, int maxAdults, int maxChildren, int maxOccupancy, String bedType, Integer sizeSqft) {}
