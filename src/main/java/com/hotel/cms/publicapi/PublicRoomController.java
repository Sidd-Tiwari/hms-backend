package com.hotel.cms.publicapi;

import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.room.Room;
import com.hotel.cms.room.RoomRepository;
import java.math.BigDecimal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/rooms")
public class PublicRoomController {
    private final RoomRepository roomRepo;

    public PublicRoomController(RoomRepository roomRepo) {
        this.roomRepo = roomRepo;
    }

    @GetMapping("/{id}")
    public ApiResponse<PublicRoomResponse> getRoom(@PathVariable String id) {
        Room room = roomRepo.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Room not found"));

        return ApiResponse.ok("Public room", PublicRoomResponse.from(room));
    }

    public record PublicRoomResponse(
            String id,
            String hotelId,
            String hotelName,
            String hotelSlug,
            String roomTypeId,
            String roomTypeName,
            BigDecimal basePrice,
            String roomNumber,
            String floorNumber,
            String status
    ) {
        public static PublicRoomResponse from(Room room) {
            return new PublicRoomResponse(
                    room.getId(),
                    room.getHotel().getId(),
                    room.getHotel().getName(),
                    room.getHotel().getSlug(),
                    room.getRoomType().getId(),
                    room.getRoomType().getName(),
                    room.getRoomType().getBasePrice(),
                    room.getRoomNumber(),
                    room.getFloorNumber(),
                    room.getStatus().name()
            );
        }
    }
}
