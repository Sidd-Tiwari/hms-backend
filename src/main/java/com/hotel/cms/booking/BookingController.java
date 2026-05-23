package com.hotel.cms.booking;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.booking.dto.BookingRequest;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.common.enums.BookingStatus;
import com.hotel.cms.user.UserAccount;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingRepository repo;
    private final BookingService service;
    private final CurrentUserService currentUserService;

    public BookingController(BookingRepository repo, BookingService service, CurrentUserService currentUserService) {
        this.repo = repo;
        this.service = service;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<Booking>> list(Principal principal) {
        UserAccount user = currentUserService.get(principal);
        List<Booking> bookings = currentUserService.isSuperAdmin(user)
                ? repo.findAll()
                : repo.findByHotelOwnerUserIdOrderByCreatedAtDesc(user.getId());
        return ApiResponse.ok("Bookings", bookings);
    }

    @PostMapping
    public ApiResponse<Booking> create(Principal principal, @Valid @RequestBody BookingRequest r) {
        UserAccount user = currentUserService.get(principal);
        currentUserService.getHotelForCurrentOwnerOrAdmin(user, r.hotelId());
        return ApiResponse.ok("Booking created", service.create(r));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Booking> status(Principal principal, @PathVariable String id, @RequestParam BookingStatus status) {
        UserAccount user = currentUserService.get(principal);
        Booking booking = repo.findById(id).orElseThrow(() -> new com.hotel.cms.exception.NotFoundException("Booking not found"));
        currentUserService.assertHotelAccess(user, booking.getHotel());
        return ApiResponse.ok("Booking status updated", service.status(id, status));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Booking> delete(Principal principal, @PathVariable String id) {
        UserAccount user = currentUserService.get(principal);
        Booking booking = repo.findById(id).orElseThrow(() -> new com.hotel.cms.exception.NotFoundException("Booking not found"));
        currentUserService.assertHotelAccess(user, booking.getHotel());
        return ApiResponse.ok("Booking cancelled", service.cancel(id));
    }
}
