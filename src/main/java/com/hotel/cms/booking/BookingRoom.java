package com.hotel.cms.booking;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hotel.cms.room.*;
import jakarta.persistence.*;
import java.math.BigDecimal; import java.util.UUID;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) @Table(name="booking_rooms")
public class BookingRoom {
 @Id @Column(length=36) private String id;
 @JsonIgnore @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="booking_id", nullable=false) private Booking booking;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="room_id", nullable=false) private Room room;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="room_type_id", nullable=false) private RoomType roomType;
 @Column(name="price_per_night", nullable=false, precision=12, scale=2) private BigDecimal pricePerNight;
 @Column(name="total_nights", nullable=false) private int totalNights;
 @Column(name="total_price", nullable=false, precision=12, scale=2) private BigDecimal totalPrice;
 @PrePersist void prePersist(){ if(id==null) id=UUID.randomUUID().toString(); }
}
