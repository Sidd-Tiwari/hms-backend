package com.hotel.cms.booking;

import com.hotel.cms.booking.dto.BookingRequest;
import com.hotel.cms.booking.dto.UserBookingRequest;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.customer.Customer;
import com.hotel.cms.customer.CustomerRepository;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.promo.PromoCode;
import com.hotel.cms.promo.PromoCodeService;
import com.hotel.cms.room.Room;
import com.hotel.cms.room.RoomRepository;
import com.hotel.cms.user.UserAccount;
import com.hotel.cms.user.UserRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/bookings")
public class UserBookingController {
    private final BookingRepository bookingRepo;
    private final BookingRoomRepository bookingRoomRepo;
    private final BookingService bookingService;
    private final CustomerRepository customerRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;
    private final PromoCodeService promoCodeService;

    public UserBookingController(
            BookingRepository bookingRepo,
            BookingRoomRepository bookingRoomRepo,
            BookingService bookingService,
            CustomerRepository customerRepo,
            RoomRepository roomRepo,
            UserRepository userRepo,
            PromoCodeService promoCodeService
    ) {
        this.bookingRepo = bookingRepo;
        this.bookingRoomRepo = bookingRoomRepo;
        this.bookingService = bookingService;
        this.customerRepo = customerRepo;
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
        this.promoCodeService = promoCodeService;
    }

    @GetMapping
    public ApiResponse<List<MyBookingResponse>> myBookings(Principal principal) {
        UserAccount user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<MyBookingResponse> bookings = bookingRepo.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toMyBookingResponse)
                .toList();

        return ApiResponse.ok("My bookings", bookings);
    }

    @PostMapping
    public ApiResponse<MyBookingResponse> bookRoom(Principal principal, @Valid @RequestBody UserBookingRequest request) {
        UserAccount user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Customer customer = customerRepo.findByUserAccountId(user.getId())
                .orElseGet(() -> createCustomerFromUser(user));

        Room room = roomRepo.findByIdAndActiveTrue(request.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));

        long nights = ChronoUnit.DAYS.between(request.checkInDate(), request.checkOutDate());
        BigDecimal price = room.getRoomType().getBasePrice();
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(Math.max(nights, 0)));

        PromoCode promo = null;
        BigDecimal discount = BigDecimal.ZERO;
        if (request.promoCode() != null && !request.promoCode().isBlank()) {
            promo = promoCodeService.validate(room.getHotel().getId(), request.promoCode(), subtotal);
            discount = promoCodeService.calculateDiscount(promo, subtotal);
        }

        BookingRequest bookingRequest = new BookingRequest(
                customer.getId(),
                room.getHotel().getId(),
                request.checkInDate(),
                request.checkOutDate(),
                request.adults() <= 0 ? 1 : request.adults(),
                Math.max(request.children(), 0),
                discount,
                BigDecimal.ZERO,
                request.specialRequest(),
                "WEBSITE",
                List.of(new BookingRequest.BookingRoomLine(room.getId(), room.getRoomType().getId(), price))
        );

        Booking saved = bookingService.create(bookingRequest);
        promoCodeService.markUsed(promo);
        return ApiResponse.ok("Room booked successfully", toMyBookingResponse(saved));
    }

    private Customer createCustomerFromUser(UserAccount user) {
        Customer c = new Customer();
        c.setUserAccount(user);
        c.setFullName(user.getFullName());
        c.setEmail(user.getEmail());
        c.setPhone(user.getPhone() == null || user.getPhone().isBlank() ? "N/A" : user.getPhone());
        c.setActive(true);
        return customerRepo.save(c);
    }

    private MyBookingResponse toMyBookingResponse(Booking booking) {
        List<BookingRoom> lines = bookingRoomRepo.findByBookingId(booking.getId());
        List<String> roomNumbers = lines.stream().map(line -> line.getRoom().getRoomNumber()).toList();
        String roomTypeName = lines.isEmpty() ? null : lines.get(0).getRoomType().getName();
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());

        return new MyBookingResponse(
                booking.getId(),
                booking.getBookingNumber(),
                booking.getHotel().getName(),
                roomNumbers,
                roomTypeName,
                booking.getCheckInDate().toString(),
                booking.getCheckOutDate().toString(),
                (int) nights,
                booking.getAdults(),
                booking.getChildren(),
                booking.getBookingStatus().name(),
                booking.getPaymentStatus().name(),
                booking.getSubtotal(),
                booking.getDiscountAmount(),
                booking.getGrandTotal(),
                booking.getBookingStatus() == com.hotel.cms.common.enums.BookingStatus.CHECKED_OUT
        );
    }

    public record MyBookingResponse(
            String id,
            String bookingNumber,
            String hotelName,
            List<String> roomNumbers,
            String roomTypeName,
            String checkInDate,
            String checkOutDate,
            int nights,
            int adults,
            int children,
            String status,
            String paymentStatus,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            boolean reviewAllowed
    ) {}
}
