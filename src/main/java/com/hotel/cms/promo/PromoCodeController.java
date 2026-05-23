package com.hotel.cms.promo;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.hotel.Hotel;
import com.hotel.cms.hotel.HotelRepository;
import com.hotel.cms.promo.dto.PromoCodeRequest;
import com.hotel.cms.promo.dto.PromoValidationRequest;
import com.hotel.cms.promo.dto.PromoValidationResponse;
import com.hotel.cms.user.UserAccount;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promo-codes")
public class PromoCodeController {
    private final PromoCodeRepository promoRepo;
    private final PromoCodeService promoService;
    private final HotelRepository hotelRepo;
    private final CurrentUserService currentUserService;

    public PromoCodeController(
            PromoCodeRepository promoRepo,
            PromoCodeService promoService,
            HotelRepository hotelRepo,
            CurrentUserService currentUserService
    ) {
        this.promoRepo = promoRepo;
        this.promoService = promoService;
        this.hotelRepo = hotelRepo;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public ApiResponse<List<PromoCode>> list(Principal principal, @RequestParam(required = false) String hotelId) {
        UserAccount user = currentUserService.get(principal);
        List<PromoCode> data;
        if (hotelId != null && !hotelId.isBlank()) {
            currentUserService.getHotelForCurrentOwnerOrAdmin(user, hotelId);
            data = promoRepo.findByHotelIdOrderByCreatedAtDesc(hotelId);
        } else if (currentUserService.isSuperAdmin(user)) {
            data = promoRepo.findAll();
        } else {
            data = promoRepo.findByHotelOwnerUserIdOrderByCreatedAtDesc(user.getId());
        }
        return ApiResponse.ok("Promo codes", data);
    }

    @PostMapping
    public ApiResponse<PromoCode> create(Principal principal, @Valid @RequestBody PromoCodeRequest request) {
        UserAccount user = currentUserService.get(principal);
        PromoCode promo = new PromoCode();
        apply(user, promo, request);
        return ApiResponse.ok("Promo code created", promoRepo.save(promo));
    }

    @PutMapping("/{id}")
    public ApiResponse<PromoCode> update(Principal principal, @PathVariable String id, @Valid @RequestBody PromoCodeRequest request) {
        UserAccount user = currentUserService.get(principal);
        PromoCode promo = promoRepo.findById(id).orElseThrow(() -> new NotFoundException("Promo code not found"));
        currentUserService.assertHotelAccess(user, promo.getHotel());
        apply(user, promo, request);
        return ApiResponse.ok("Promo code updated", promoRepo.save(promo));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Principal principal, @PathVariable String id) {
        UserAccount user = currentUserService.get(principal);
        PromoCode promo = promoRepo.findById(id).orElseThrow(() -> new NotFoundException("Promo code not found"));
        currentUserService.assertHotelAccess(user, promo.getHotel());
        promo.setActive(false);
        promoRepo.save(promo);
        return ApiResponse.ok("Promo code disabled", null);
    }

    @PostMapping("/validate")
    public ApiResponse<PromoValidationResponse> validate(Principal principal, @Valid @RequestBody PromoValidationRequest request) {
        UserAccount user = currentUserService.get(principal);
        if (!currentUserService.isUser(user)) {
            throw new NotFoundException("Coupon validation is available for users only");
        }
        PromoCode promo = promoService.validate(request.hotelId(), request.code(), request.bookingAmount());
        BigDecimal discount = promoService.calculateDiscount(promo, request.bookingAmount());
        return ApiResponse.ok("Coupon valid", new PromoValidationResponse(true, promo.getCode(), discount, "Coupon applied"));
    }

    private void apply(UserAccount user, PromoCode promo, PromoCodeRequest request) {
        Hotel hotel = hotelRepo.findById(request.hotelId()).orElseThrow(() -> new NotFoundException("Hotel not found"));
        currentUserService.assertHotelAccess(user, hotel);
        promo.setHotel(hotel);
        promo.setCode(request.code().trim().toUpperCase());
        promo.setDescription(request.description());
        promo.setDiscountType(request.discountType());
        promo.setDiscountValue(request.discountValue());
        promo.setMinBookingAmount(request.minBookingAmount() == null ? BigDecimal.ZERO : request.minBookingAmount());
        promo.setMaxDiscountAmount(request.maxDiscountAmount());
        promo.setUsageLimit(request.usageLimit());
        promo.setStartDate(request.startDate());
        promo.setEndDate(request.endDate());
        promo.setActive(request.active() == null || request.active());
    }
}
