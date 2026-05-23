package com.hotel.cms.payment;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByBookingId(String bookingId);
    List<Payment> findByBookingHotelOwnerUserIdOrderByCreatedAtDesc(String ownerUserId);

    @Query("select p from Payment p order by p.createdAt desc")
    List<Payment> findAllOrderByCreatedAtDesc();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.booking.hotel.ownerUser.id = :ownerUserId")
    java.math.BigDecimal sumAmountByOwnerUserId(@Param("ownerUserId") String ownerUserId);
}
