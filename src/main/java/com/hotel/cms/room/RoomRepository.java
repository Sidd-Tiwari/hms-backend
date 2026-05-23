package com.hotel.cms.room;

import com.hotel.cms.common.enums.RoomStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, String> {
    long countByStatus(RoomStatus status);
    long countByActiveTrue();
    long countByStatusAndActiveTrue(RoomStatus status);
    List<Room> findByHotelId(String hotelId);
    List<Room> findByHotelIdAndActiveTrue(String hotelId);
    List<Room> findByHotelSlugAndActiveTrue(String slug);
    List<Room> findByHotelOwnerUserIdAndActiveTrueOrderByRoomNumberAsc(String ownerUserId);
    Optional<Room> findByIdAndActiveTrue(String id);

    @Query("select count(r) from Room r where r.hotel.ownerUser.id = :ownerUserId and r.active = true")
    long countActiveByOwnerUserId(@Param("ownerUserId") String ownerUserId);

    @Query("select count(r) from Room r where r.hotel.ownerUser.id = :ownerUserId and r.active = true and r.status = :status")
    long countByOwnerUserIdAndStatus(@Param("ownerUserId") String ownerUserId, @Param("status") RoomStatus status);
}
