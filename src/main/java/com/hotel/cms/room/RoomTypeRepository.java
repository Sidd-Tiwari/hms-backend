package com.hotel.cms.room;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomTypeRepository extends JpaRepository<RoomType, String> {
    List<RoomType> findByHotelId(String hotelId);
    List<RoomType> findByHotelIdAndActiveTrue(String hotelId);
    List<RoomType> findByHotelOwnerUserIdAndActiveTrueOrderByCreatedAtDesc(String ownerUserId);

    @Query("select count(rt) from RoomType rt where rt.hotel.ownerUser.id = :ownerUserId and rt.active = true")
    long countActiveByOwnerUserId(@Param("ownerUserId") String ownerUserId);
}
