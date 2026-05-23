package com.hotel.cms.hotel;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotelRepository extends JpaRepository<Hotel, String> {
    Optional<Hotel> findBySlug(String slug);
    Optional<Hotel> findBySlugAndActiveTrue(String slug);
    List<Hotel> findByActiveTrueOrderByCreatedAtDesc();
    List<Hotel> findByOwnerUserIdAndActiveTrueOrderByCreatedAtDesc(String ownerUserId);
    long countByOwnerUserIdAndActiveTrue(String ownerUserId);
}
