package com.hotel.cms.promo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {
    Optional<PromoCode> findByCodeIgnoreCase(String code);
    Optional<PromoCode> findByCodeIgnoreCaseAndHotelIdAndActiveTrue(String code, String hotelId);
    List<PromoCode> findByHotelIdOrderByCreatedAtDesc(String hotelId);
    List<PromoCode> findByHotelOwnerUserIdOrderByCreatedAtDesc(String ownerUserId);

    @Query("select p from PromoCode p where p.hotel.id = :hotelId and p.active = true and (p.startDate is null or p.startDate <= :today) and (p.endDate is null or p.endDate >= :today) order by p.createdAt desc")
    List<PromoCode> findActiveForHotel(@Param("hotelId") String hotelId, @Param("today") LocalDate today);
}
