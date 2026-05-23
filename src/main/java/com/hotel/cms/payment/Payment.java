package com.hotel.cms.payment;
import com.hotel.cms.booking.Booking;
import com.hotel.cms.common.BaseEntity;
import com.hotel.cms.common.enums.*;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.time.Instant;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="payments")
public class Payment extends BaseEntity {
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="booking_id", nullable=false) private Booking booking;
 @Column(name="payment_number", nullable=false, unique=true, length=40) private String paymentNumber;
 @Column(nullable=false, precision=12, scale=2) private BigDecimal amount;
 @Enumerated(EnumType.STRING) @Column(name="payment_method", nullable=false, length=40) private PaymentMethod paymentMethod;
 @Column(name="transaction_id") private String transactionId;
 @Enumerated(EnumType.STRING) @Column(name="payment_status", nullable=false, length=40) private PaymentStatus paymentStatus = PaymentStatus.PAID;
 @Column(name="paid_at") private Instant paidAt;
}
