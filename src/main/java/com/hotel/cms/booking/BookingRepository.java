package com.hotel.cms.booking;

import com.hotel.cms.common.enums.BookingStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, String> {
    long countByBookingStatus(BookingStatus status);

    @Query("select b from Booking b where b.customer.userAccount.id = :userId order by b.createdAt desc")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    List<Booking> findByHotelOwnerUserIdOrderByCreatedAtDesc(String ownerUserId);
    List<Booking> findByHotelIdOrderByCreatedAtDesc(String hotelId);
    long countByHotelOwnerUserId(String ownerUserId);
    long countByHotelOwnerUserIdAndBookingStatus(String ownerUserId, BookingStatus status);
}
