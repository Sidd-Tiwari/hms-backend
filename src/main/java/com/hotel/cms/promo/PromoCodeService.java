package com.hotel.cms.promo;

import com.hotel.cms.common.enums.DiscountType;
import com.hotel.cms.exception.BadRequestException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class PromoCodeService {
    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        this.promoCodeRepository = promoCodeRepository;
    }

    public PromoCode validate(String hotelId, String code, BigDecimal bookingAmount) {
        if (code == null || code.isBlank()) {
            return null;
        }
        PromoCode promo = promoCodeRepository.findByCodeIgnoreCaseAndHotelIdAndActiveTrue(code.trim(), hotelId)
                .orElseThrow(() -> new BadRequestException("Invalid coupon code"));

        LocalDate today = LocalDate.now();
        if (promo.getStartDate() != null && promo.getStartDate().isAfter(today)) {
            throw new BadRequestException("Coupon is not active yet");
        }
        if (promo.getEndDate() != null && promo.getEndDate().isBefore(today)) {
            throw new BadRequestException("Coupon has expired");
        }
        if (promo.getUsageLimit() != null && promo.getUsedCount() >= promo.getUsageLimit()) {
            throw new BadRequestException("Coupon usage limit exceeded");
        }
        if (promo.getMinBookingAmount() != null && bookingAmount.compareTo(promo.getMinBookingAmount()) < 0) {
            throw new BadRequestException("Minimum booking amount not met for coupon");
        }
        return promo;
    }

    public BigDecimal calculateDiscount(PromoCode promo, BigDecimal bookingAmount) {
        if (promo == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount;
        if (promo.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = bookingAmount.multiply(promo.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            discount = promo.getDiscountValue();
        }
        if (promo.getMaxDiscountAmount() != null && discount.compareTo(promo.getMaxDiscountAmount()) > 0) {
            discount = promo.getMaxDiscountAmount();
        }
        if (discount.compareTo(bookingAmount) > 0) {
            discount = bookingAmount;
        }
        return discount.max(BigDecimal.ZERO);
    }

    public void markUsed(PromoCode promo) {
        if (promo == null) {
            return;
        }
        promo.setUsedCount(promo.getUsedCount() + 1);
        promoCodeRepository.save(promo);
    }
}
