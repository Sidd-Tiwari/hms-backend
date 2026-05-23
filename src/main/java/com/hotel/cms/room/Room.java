package com.hotel.cms.room;
import com.hotel.cms.common.BaseEntity;
import com.hotel.cms.common.enums.RoomStatus;
import com.hotel.cms.hotel.Hotel;
import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="rooms")
public class Room extends BaseEntity {
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="hotel_id", nullable=false) private Hotel hotel;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="room_type_id", nullable=false) private RoomType roomType;
 @Column(name="room_number", nullable=false, length=40) private String roomNumber;
 @Column(name="floor_number", length=40) private String floorNumber;
 @Enumerated(EnumType.STRING) @Column(nullable=false, length=40) private RoomStatus status = RoomStatus.AVAILABLE;
 @Column(name="is_active", nullable=false) private boolean active = true;
}
