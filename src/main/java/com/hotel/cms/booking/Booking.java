package com.hotel.cms.booking;
import com.hotel.cms.common.BaseEntity;
import com.hotel.cms.common.enums.*;
import com.hotel.cms.customer.Customer;
import com.hotel.cms.hotel.Hotel;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.LocalDate;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="bookings")
public class Booking extends BaseEntity {
 @Column(name="booking_number", nullable=false, unique=true, length=40) private String bookingNumber;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="customer_id", nullable=false) private Customer customer;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="hotel_id", nullable=false) private Hotel hotel;
 @Column(nullable=false, length=40) private String source = "CMS";
 @Column(name="check_in_date", nullable=false) private LocalDate checkInDate;
 @Column(name="check_out_date", nullable=false) private LocalDate checkOutDate;
 private int adults = 1;
 private int children = 0;
 @Enumerated(EnumType.STRING) @Column(name="booking_status", nullable=false, length=40) private BookingStatus bookingStatus = BookingStatus.PENDING;
 @Enumerated(EnumType.STRING) @Column(name="payment_status", nullable=false, length=40) private PaymentStatus paymentStatus = PaymentStatus.PENDING;
 @Column(nullable=false, precision=12, scale=2) private BigDecimal subtotal = BigDecimal.ZERO;
 @Column(name="discount_amount", nullable=false, precision=12, scale=2) private BigDecimal discountAmount = BigDecimal.ZERO;
 @Column(name="tax_amount", nullable=false, precision=12, scale=2) private BigDecimal taxAmount = BigDecimal.ZERO;
 @Column(name="grand_total", nullable=false, precision=12, scale=2) private BigDecimal grandTotal = BigDecimal.ZERO;
 @Column(name="special_request", columnDefinition="TEXT") private String specialRequest;
}
