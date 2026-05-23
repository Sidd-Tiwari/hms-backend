package com.hotel.cms.room;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.common.enums.RoomStatus;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.hotel.Hotel;
import com.hotel.cms.hotel.HotelRepository;
import com.hotel.cms.room.dto.RoomRequest;
import com.hotel.cms.user.UserAccount;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {
    private final RoomRepository repo;
    private final RoomTypeRepository typeRepo;
    private final HotelRepository hotelRepo;
    private final CurrentUserService currentUserService;

    public RoomController(RoomRepository repo, RoomTypeRepository typeRepo, HotelRepository hotelRepo, CurrentUserService currentUserService) {
        this.repo = repo;
        this.typeRepo = typeRepo;
        this.hotelRepo = hotelRepo;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<Room>> list(Principal principal, @RequestParam(required = false) String hotelId) {
        UserAccount user = currentUserService.get(principal);
        List<Room> rooms;
        if (hotelId != null && !hotelId.isBlank()) {
            Hotel hotel = currentUserService.getHotelForCurrentOwnerOrAdmin(user, hotelId);
            rooms = repo.findByHotelId(hotel.getId());
        } else if (currentUserService.isSuperAdmin(user)) {
            rooms = repo.findAll(Sort.by("roomNumber"));
        } else {
            rooms = repo.findByHotelOwnerUserIdAndActiveTrueOrderByRoomNumberAsc(user.getId());
        }
        return ApiResponse.ok("Rooms", rooms);
    }

    @PostMapping
    public ApiResponse<Room> create(Principal principal, @Valid @RequestBody RoomRequest r) {
        UserAccount user = currentUserService.get(principal);
        Room room = new Room();
        apply(user, room, r);
        return ApiResponse.ok("Room created", repo.save(room));
    }

    @PutMapping("/{id}")
    public ApiResponse<Room> update(Principal principal, @PathVariable String id, @Valid @RequestBody RoomRequest r) {
        UserAccount user = currentUserService.get(principal);
        Room room = repo.findById(id).orElseThrow(() -> new NotFoundException("Room not found"));
        currentUserService.assertHotelAccess(user, room.getHotel());
        apply(user, room, r);
        return ApiResponse.ok("Room updated", repo.save(room));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Room> status(Principal principal, @PathVariable String id, @RequestParam RoomStatus status) {
        UserAccount user = currentUserService.get(principal);
        Room room = repo.findById(id).orElseThrow(() -> new NotFoundException("Room not found"));
        currentUserService.assertHotelAccess(user, room.getHotel());
        room.setStatus(status);
        return ApiResponse.ok("Room status updated", repo.save(room));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Room> delete(Principal principal, @PathVariable String id) {
        UserAccount user = currentUserService.get(principal);
        Room room = repo.findById(id).orElseThrow(() -> new NotFoundException("Room not found"));
        currentUserService.assertHotelAccess(user, room.getHotel());
        room.setActive(false);
        room.setStatus(RoomStatus.INACTIVE);
        return ApiResponse.ok("Room disabled", repo.save(room));
    }

    private void apply(UserAccount user, Room room, RoomRequest r) {
        Hotel hotel = hotelRepo.findById(r.hotelId()).orElseThrow(() -> new NotFoundException("Hotel not found"));
        currentUserService.assertHotelAccess(user, hotel);
        RoomType type = typeRepo.findById(r.roomTypeId()).orElseThrow(() -> new NotFoundException("Room type not found"));
        if (!type.getHotel().getId().equals(hotel.getId())) {
            throw new NotFoundException("Room type not found for this hotel");
        }
        room.setHotel(hotel);
        room.setRoomType(type);
        room.setRoomNumber(r.roomNumber());
        room.setFloorNumber(r.floorNumber());
        if (r.status() != null) room.setStatus(r.status());
    }
}
