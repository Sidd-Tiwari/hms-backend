package com.hotel.cms.dashboard;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.booking.BookingRepository;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.common.enums.BookingStatus;
import com.hotel.cms.common.enums.RoomStatus;
import com.hotel.cms.customer.CustomerRepository;
import com.hotel.cms.hotel.HotelRepository;
import com.hotel.cms.payment.PaymentRepository;
import com.hotel.cms.room.RoomRepository;
import com.hotel.cms.user.Role;
import com.hotel.cms.user.UserAccount;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final HotelRepository hotelRepo;
    private final RoomRepository roomRepo;
    private final BookingRepository bookingRepo;
    private final CustomerRepository customerRepo;
    private final PaymentRepository paymentRepo;
    private final CurrentUserService currentUserService;

    public DashboardController(
            HotelRepository h,
            RoomRepository r,
            BookingRepository b,
            CustomerRepository c,
            PaymentRepository p,
            CurrentUserService currentUserService
    ) {
        hotelRepo = h;
        roomRepo = r;
        bookingRepo = b;
        customerRepo = c;
        paymentRepo = p;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(Principal principal) {
        UserAccount user = currentUserService.get(principal);
        boolean superAdmin = currentUserService.isSuperAdmin(user);

        BigDecimal revenue = superAdmin
                ? paymentRepo.findAll().stream().map(x -> x.getAmount() == null ? BigDecimal.ZERO : x.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add)
                : paymentRepo.sumAmountByOwnerUserId(user.getId());

        Map<String, Object> summary = superAdmin
                ? Map.of(
                    "currentUser", currentUser(user),
                    "scope", "ALL_HOTELS",
                    "hotels", hotelRepo.count(),
                    "rooms", roomRepo.count(),
                    "availableRooms", roomRepo.countByStatus(RoomStatus.AVAILABLE),
                    "occupiedRooms", roomRepo.countByStatus(RoomStatus.OCCUPIED),
                    "bookings", bookingRepo.count(),
                    "confirmedBookings", bookingRepo.countByBookingStatus(BookingStatus.CONFIRMED),
                    "customers", customerRepo.count(),
                    "revenue", revenue
                )
                : Map.of(
                    "currentUser", currentUser(user),
                    "scope", "OWNER_HOTELS",
                    "hotels", hotelRepo.countByOwnerUserIdAndActiveTrue(user.getId()),
                    "rooms", roomRepo.countActiveByOwnerUserId(user.getId()),
                    "availableRooms", roomRepo.countByOwnerUserIdAndStatus(user.getId(), RoomStatus.AVAILABLE),
                    "occupiedRooms", roomRepo.countByOwnerUserIdAndStatus(user.getId(), RoomStatus.OCCUPIED),
                    "bookings", bookingRepo.countByHotelOwnerUserId(user.getId()),
                    "confirmedBookings", bookingRepo.countByHotelOwnerUserIdAndBookingStatus(user.getId(), BookingStatus.CONFIRMED),
                    "customers", customerRepo.countCustomersByOwnerUserId(user.getId()),
                    "revenue", revenue == null ? BigDecimal.ZERO : revenue
                );

        return ApiResponse.ok("Dashboard summary", summary);
    }

    private Map<String, Object> currentUser(UserAccount user) {
        return Map.of(
                "userId", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}
