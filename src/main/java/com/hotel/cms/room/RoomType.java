package com.hotel.cms.room;
import com.hotel.cms.common.BaseEntity;
import com.hotel.cms.hotel.Hotel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter; import lombok.Setter;
@Getter @Setter @Entity @Table(name="room_types")
public class RoomType extends BaseEntity {
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="hotel_id", nullable=false) private Hotel hotel;
 @Column(nullable=false, length=120) private String name;
 @Column(columnDefinition="TEXT") private String description;
 @Column(name="base_price", nullable=false, precision=12, scale=2) private BigDecimal basePrice;
 @Column(name="max_adults", nullable=false) private int maxAdults = 2;
 @Column(name="max_children", nullable=false) private int maxChildren = 0;
 @Column(name="max_occupancy", nullable=false) private int maxOccupancy = 2;
 @Column(name="bed_type") private String bedType;
 @Column(name="size_sqft") private Integer sizeSqft;
 @Column(name="is_active", nullable=false) private boolean active = true;
}
