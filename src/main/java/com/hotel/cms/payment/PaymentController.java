package com.hotel.cms.payment;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.booking.Booking;
import com.hotel.cms.booking.BookingRepository;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.common.enums.PaymentStatus;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.payment.dto.PaymentRequest;
import com.hotel.cms.user.UserAccount;
import jakarta.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentRepository repo;
    private final BookingRepository bookingRepo;
    private final CurrentUserService currentUserService;

    public PaymentController(PaymentRepository repo, BookingRepository bookingRepo, CurrentUserService currentUserService) {
        this.repo = repo;
        this.bookingRepo = bookingRepo;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<Payment>> list(Principal principal) {
        UserAccount user = currentUserService.get(principal);
        List<Payment> payments = currentUserService.isSuperAdmin(user)
                ? repo.findAllOrderByCreatedAtDesc()
                : repo.findByBookingHotelOwnerUserIdOrderByCreatedAtDesc(user.getId());
        return ApiResponse.ok("Payments", payments);
    }

    @PostMapping
    public ApiResponse<Payment> create(Principal principal, @Valid @RequestBody PaymentRequest r) {
        UserAccount user = currentUserService.get(principal);
        Booking b = bookingRepo.findById(r.bookingId()).orElseThrow(() -> new NotFoundException("Booking not found"));
        currentUserService.assertHotelAccess(user, b.getHotel());
        Payment p = new Payment();
        p.setPaymentNumber("PAY" + System.currentTimeMillis());
        p.setBooking(b);
        p.setAmount(r.amount());
        p.setPaymentMethod(r.paymentMethod());
        p.setTransactionId(r.transactionId());
        p.setPaymentStatus(r.paymentStatus() == null ? PaymentStatus.PAID : r.paymentStatus());
        p.setPaidAt(Instant.now());
        if (r.amount().compareTo(b.getGrandTotal()) >= 0) b.setPaymentStatus(PaymentStatus.PAID);
        else b.setPaymentStatus(PaymentStatus.PARTIAL);
        bookingRepo.save(b);
        return ApiResponse.ok("Payment saved", repo.save(p));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Principal principal, @PathVariable String id) {
        UserAccount user = currentUserService.get(principal);
        Payment payment = repo.findById(id).orElseThrow(() -> new NotFoundException("Payment not found"));
        currentUserService.assertHotelAccess(user, payment.getBooking().getHotel());
        repo.deleteById(id);
        return ApiResponse.ok("Payment deleted", null);
    }
}
