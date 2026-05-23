package com.hotel.cms.review;

import com.hotel.cms.booking.Booking;
import com.hotel.cms.common.BaseEntity;
import com.hotel.cms.common.enums.ReviewStatus;
import com.hotel.cms.customer.Customer;
import com.hotel.cms.hotel.Hotel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private int rating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReviewStatus status = ReviewStatus.PENDING;
}
