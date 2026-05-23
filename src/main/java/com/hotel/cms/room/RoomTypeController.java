package com.hotel.cms.room;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.hotel.Hotel;
import com.hotel.cms.hotel.HotelRepository;
import com.hotel.cms.room.dto.RoomTypeRequest;
import com.hotel.cms.user.UserAccount;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/room-types")
public class RoomTypeController {
    private final RoomTypeRepository repo;
    private final HotelRepository hotelRepo;
    private final CurrentUserService currentUserService;

    public RoomTypeController(RoomTypeRepository repo, HotelRepository hotelRepo, CurrentUserService currentUserService) {
        this.repo = repo;
        this.hotelRepo = hotelRepo;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<RoomType>> list(Principal principal, @RequestParam(required = false) String hotelId) {
        UserAccount user = currentUserService.get(principal);
        List<RoomType> roomTypes;
        if (hotelId != null && !hotelId.isBlank()) {
            Hotel hotel = currentUserService.getHotelForCurrentOwnerOrAdmin(user, hotelId);
            roomTypes = repo.findByHotelId(hotel.getId());
        } else if (currentUserService.isSuperAdmin(user)) {
            roomTypes = repo.findAll(PageRequest.of(0, 100, Sort.by("createdAt").descending())).getContent();
        } else {
            roomTypes = repo.findByHotelOwnerUserIdAndActiveTrueOrderByCreatedAtDesc(user.getId());
        }
        return ApiResponse.ok("Room types", roomTypes);
    }

    @PostMapping
    public ApiResponse<RoomType> create(Principal principal, @Valid @RequestBody RoomTypeRequest r) {
        UserAccount user = currentUserService.get(principal);
        RoomType t = new RoomType();
        apply(user, t, r);
        return ApiResponse.ok("Room type created", repo.save(t));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomType> update(Principal principal, @PathVariable String id, @Valid @RequestBody RoomTypeRequest r) {
        UserAccount user = currentUserService.get(principal);
        RoomType t = repo.findById(id).orElseThrow(() -> new NotFoundException("Room type not found"));
        currentUserService.assertHotelAccess(user, t.getHotel());
        apply(user, t, r);
        return ApiResponse.ok("Room type updated", repo.save(t));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<RoomType> delete(Principal principal, @PathVariable String id) {
        UserAccount user = currentUserService.get(principal);
        RoomType t = repo.findById(id).orElseThrow(() -> new NotFoundException("Room type not found"));
        currentUserService.assertHotelAccess(user, t.getHotel());
        t.setActive(false);
        return ApiResponse.ok("Room type disabled", repo.save(t));
    }

    private void apply(UserAccount user, RoomType t, RoomTypeRequest r) {
        Hotel hotel = hotelRepo.findById(r.hotelId()).orElseThrow(() -> new NotFoundException("Hotel not found"));
        currentUserService.assertHotelAccess(user, hotel);
        t.setHotel(hotel);
        t.setName(r.name());
        t.setDescription(r.description());
        t.setBasePrice(r.basePrice());
        t.setMaxAdults(r.maxAdults() <= 0 ? 2 : r.maxAdults());
        t.setMaxChildren(Math.max(r.maxChildren(), 0));
        t.setMaxOccupancy(r.maxOccupancy() <= 0 ? 2 : r.maxOccupancy());
        t.setBedType(r.bedType());
        t.setSizeSqft(r.sizeSqft());
    }
}
