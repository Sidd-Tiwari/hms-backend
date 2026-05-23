package com.hotel.cms.review;

import com.hotel.cms.common.enums.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, String> {
    boolean existsByBookingIdAndCustomerId(String bookingId, String customerId);
    Optional<Review> findByBookingIdAndCustomerId(String bookingId, String customerId);
    List<Review> findByCustomerUserAccountIdOrderByCreatedAtDesc(String userAccountId);
    List<Review> findByHotelOwnerUserIdOrderByCreatedAtDesc(String ownerUserId);
    List<Review> findByHotelSlugAndStatusOrderByCreatedAtDesc(String slug, ReviewStatus status);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.hotel.id = :hotelId and r.status = com.hotel.cms.common.enums.ReviewStatus.APPROVED")
    Double averageRatingByHotelId(@Param("hotelId") String hotelId);

    @Query("select count(r) from Review r where r.hotel.id = :hotelId and r.status = com.hotel.cms.common.enums.ReviewStatus.APPROVED")
    long approvedCountByHotelId(@Param("hotelId") String hotelId);
}
