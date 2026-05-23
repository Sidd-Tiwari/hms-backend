package com.hotel.cms.review;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.booking.Booking;
import com.hotel.cms.booking.BookingRepository;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.common.enums.BookingStatus;
import com.hotel.cms.common.enums.ReviewStatus;
import com.hotel.cms.customer.Customer;
import com.hotel.cms.customer.CustomerRepository;
import com.hotel.cms.exception.BadRequestException;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.review.dto.ReviewRequest;
import com.hotel.cms.user.UserAccount;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {
    private final ReviewRepository reviewRepo;
    private final BookingRepository bookingRepo;
    private final CustomerRepository customerRepo;
    private final CurrentUserService currentUserService;

    public ReviewController(
            ReviewRepository reviewRepo,
            BookingRepository bookingRepo,
            CustomerRepository customerRepo,
            CurrentUserService currentUserService
    ) {
        this.reviewRepo = reviewRepo;
        this.bookingRepo = bookingRepo;
        this.customerRepo = customerRepo;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/reviews")
    public ApiResponse<List<Review>> adminReviews(Principal principal) {
        UserAccount user = currentUserService.get(principal);
        List<Review> reviews = currentUserService.isSuperAdmin(user)
                ? reviewRepo.findAll()
                : reviewRepo.findByHotelOwnerUserIdOrderByCreatedAtDesc(user.getId());
        return ApiResponse.ok("Reviews", reviews);
    }

    @PatchMapping("/reviews/{id}/status")
    public ApiResponse<Review> updateStatus(Principal principal, @PathVariable String id, @RequestParam ReviewStatus status) {
        UserAccount user = currentUserService.get(principal);
        Review review = reviewRepo.findById(id).orElseThrow(() -> new NotFoundException("Review not found"));
        currentUserService.assertHotelAccess(user, review.getHotel());
        review.setStatus(status);
        return ApiResponse.ok("Review status updated", reviewRepo.save(review));
    }

    @GetMapping("/user/reviews")
    public ApiResponse<List<Review>> myReviews(Principal principal) {
        UserAccount user = currentUserService.get(principal);
        return ApiResponse.ok("My reviews", reviewRepo.findByCustomerUserAccountIdOrderByCreatedAtDesc(user.getId()));
    }

    @PostMapping("/user/reviews")
    public ApiResponse<Review> createReview(Principal principal, @Valid @RequestBody ReviewRequest request) {
        UserAccount user = currentUserService.get(principal);
        Customer customer = customerRepo.findByUserAccountId(user.getId())
                .orElseThrow(() -> new BadRequestException("Customer profile not found"));
        Booking booking = bookingRepo.findById(request.bookingId())
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (!booking.getCustomer().getId().equals(customer.getId())) {
            throw new NotFoundException("Booking not found");
        }
        if (booking.getBookingStatus() != BookingStatus.CHECKED_OUT) {
            throw new BadRequestException("You can rate the hotel after checkout only");
        }
        if (reviewRepo.existsByBookingIdAndCustomerId(booking.getId(), customer.getId())) {
            throw new BadRequestException("Review already submitted for this booking");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setCustomer(customer);
        review.setHotel(booking.getHotel());
        review.setRating(request.rating());
        review.setFeedback(request.feedback());
        review.setStatus(ReviewStatus.PENDING);
        return ApiResponse.ok("Review submitted", reviewRepo.save(review));
    }

    @GetMapping("/public/hotels/{slug}/reviews")
    public ApiResponse<List<Review>> publicHotelReviews(@PathVariable String slug) {
        return ApiResponse.ok("Hotel reviews", reviewRepo.findByHotelSlugAndStatusOrderByCreatedAtDesc(slug, ReviewStatus.APPROVED));
    }
}
