package com.hotel.cms.publicapi;

import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.hotel.Hotel;
import com.hotel.cms.hotel.HotelRepository;
import com.hotel.cms.promo.PromoCode;
import com.hotel.cms.promo.PromoCodeRepository;
import com.hotel.cms.review.ReviewRepository;
import com.hotel.cms.room.Room;
import com.hotel.cms.room.RoomRepository;
import com.hotel.cms.room.RoomType;
import com.hotel.cms.room.RoomTypeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/hotels")
public class PublicHotelController {

    private final HotelRepository hotelRepo;
    private final RoomTypeRepository roomTypeRepo;
    private final RoomRepository roomRepo;
    private final PromoCodeRepository promoRepo;
    private final ReviewRepository reviewRepo;

    public PublicHotelController(
            HotelRepository hotelRepo,
            RoomTypeRepository roomTypeRepo,
            RoomRepository roomRepo,
            PromoCodeRepository promoRepo,
            ReviewRepository reviewRepo
    ) {
        this.hotelRepo = hotelRepo;
        this.roomTypeRepo = roomTypeRepo;
        this.roomRepo = roomRepo;
        this.promoRepo = promoRepo;
        this.reviewRepo = reviewRepo;
    }

    @GetMapping
    public ApiResponse<List<PublicHotelListResponse>> listActiveHotels() {
        List<PublicHotelListResponse> hotels = hotelRepo.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(PublicHotelListResponse::from)
                .toList();

        return ApiResponse.ok("Public hotels", hotels);
    }

    @GetMapping("/{slug}")
    public ApiResponse<PublicHotelDetailResponse> getHotelDetail(@PathVariable String slug) {
        Hotel hotel = hotelRepo.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new NotFoundException("Hotel not found"));

        List<RoomTypeResponse> roomTypes = roomTypeRepo.findByHotelIdAndActiveTrue(hotel.getId())
                .stream()
                .map(RoomTypeResponse::from)
                .toList();

        List<RoomResponse> rooms = roomRepo.findByHotelIdAndActiveTrue(hotel.getId())
                .stream()
                .map(RoomResponse::from)
                .toList();

        List<PromoCodeResponse> activePromos = promoRepo.findActiveForHotel(hotel.getId(), LocalDate.now())
                .stream()
                .map(PromoCodeResponse::from)
                .toList();

        RatingSummary ratingSummary = new RatingSummary(
                reviewRepo.averageRatingByHotelId(hotel.getId()),
                reviewRepo.approvedCountByHotelId(hotel.getId())
        );

        return ApiResponse.ok(
                "Public hotel detail",
                PublicHotelDetailResponse.from(hotel, roomTypes, rooms, activePromos, ratingSummary)
        );
    }

    public record PublicHotelListResponse(
            String id,
            String name,
            String slug,
            String description,
            BigDecimal starRating,
            String city,
            String state,
            String country,
            String phone,
            String email,
            String website
    ) {
        public static PublicHotelListResponse from(Hotel h) {
            return new PublicHotelListResponse(
                    h.getId(),
                    h.getName(),
                    h.getSlug(),
                    h.getDescription(),
                    h.getStarRating(),
                    h.getCity(),
                    h.getState(),
                    h.getCountry(),
                    h.getPhone(),
                    h.getEmail(),
                    h.getWebsite()
            );
        }
    }

    public record PublicHotelDetailResponse(
            String id,
            String name,
            String slug,
            String description,
            BigDecimal starRating,
            String gstNumber,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String country,
            String pincode,
            String phone,
            String email,
            String website,
            List<RoomTypeResponse> roomTypes,
            List<RoomResponse> rooms,
            List<PromoCodeResponse> activePromos,
            RatingSummary ratingSummary
    ) {
        public static PublicHotelDetailResponse from(
                Hotel h,
                List<RoomTypeResponse> roomTypes,
                List<RoomResponse> rooms,
                List<PromoCodeResponse> activePromos,
                RatingSummary ratingSummary
        ) {
            return new PublicHotelDetailResponse(
                    h.getId(),
                    h.getName(),
                    h.getSlug(),
                    h.getDescription(),
                    h.getStarRating(),
                    h.getGstNumber(),
                    h.getAddressLine1(),
                    h.getAddressLine2(),
                    h.getCity(),
                    h.getState(),
                    h.getCountry(),
                    h.getPincode(),
                    h.getPhone(),
                    h.getEmail(),
                    h.getWebsite(),
                    roomTypes,
                    rooms,
                    activePromos,
                    ratingSummary
            );
        }
    }

    public record RoomTypeResponse(
            String id,
            String name,
            String description,
            BigDecimal basePrice,
            int maxAdults,
            int maxChildren,
            int maxOccupancy,
            String bedType,
            Integer sizeSqft
    ) {
        public static RoomTypeResponse from(RoomType rt) {
            return new RoomTypeResponse(
                    rt.getId(),
                    rt.getName(),
                    rt.getDescription(),
                    rt.getBasePrice(),
                    rt.getMaxAdults(),
                    rt.getMaxChildren(),
                    rt.getMaxOccupancy(),
                    rt.getBedType(),
                    rt.getSizeSqft()
            );
        }
    }

    public record RoomResponse(
            String id,
            String roomNumber,
            String floorNumber,
            String roomTypeName,
            BigDecimal basePrice,
            String status
    ) {
        public static RoomResponse from(Room r) {
            return new RoomResponse(
                    r.getId(),
                    r.getRoomNumber(),
                    r.getFloorNumber(),
                    r.getRoomType().getName(),
                    r.getRoomType().getBasePrice(),
                    r.getStatus().name()
            );
        }
    }

    public record PromoCodeResponse(
            String code,
            String description,
            String discountType,
            BigDecimal discountValue,
            BigDecimal minBookingAmount,
            BigDecimal maxDiscountAmount,
            LocalDate endDate
    ) {
        public static PromoCodeResponse from(PromoCode p) {
            return new PromoCodeResponse(
                    p.getCode(),
                    p.getDescription(),
                    p.getDiscountType().name(),
                    p.getDiscountValue(),
                    p.getMinBookingAmount(),
                    p.getMaxDiscountAmount(),
                    p.getEndDate()
            );
        }
    }

    public record RatingSummary(Double averageRating, long totalReviews) {}
}
