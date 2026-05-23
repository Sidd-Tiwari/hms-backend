package com.hotel.cms.room.dto;
import com.hotel.cms.common.enums.RoomStatus; import jakarta.validation.constraints.NotBlank;
public record RoomRequest(@NotBlank String hotelId, @NotBlank String roomTypeId, @NotBlank String roomNumber, String floorNumber, RoomStatus status) {}
