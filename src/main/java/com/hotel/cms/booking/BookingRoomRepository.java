package com.hotel.cms.booking;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRoomRepository extends JpaRepository<BookingRoom, String> {
    java.util.List<BookingRoom> findByBookingId(String bookingId);
}
