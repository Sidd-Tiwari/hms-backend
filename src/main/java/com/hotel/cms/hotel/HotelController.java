package com.hotel.cms.hotel;

import com.hotel.cms.access.CurrentUserService;
import com.hotel.cms.common.ApiResponse;
import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.hotel.dto.HotelRequest;
import com.hotel.cms.user.UserAccount;
import com.hotel.cms.user.UserRepository;
import com.hotel.cms.util.SlugUtil;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hotels")
public class HotelController {
    private final HotelRepository repo;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;

    public HotelController(HotelRepository repo, CurrentUserService currentUserService, UserRepository userRepository) {
        this.repo = repo;
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<List<Hotel>> list(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        UserAccount user = currentUserService.get(principal);
        List<Hotel> hotels = currentUserService.isSuperAdmin(user)
                ? repo.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())).getContent()
                : repo.findByOwnerUserIdAndActiveTrueOrderByCreatedAtDesc(user.getId());
        return ApiResponse.ok("Hotels", hotels);
    }

    @GetMapping("/{id}")
    public ApiResponse<Hotel> get(Principal principal, @PathVariable String id) {
        UserAccount user = currentUserService.get(principal);
        Hotel hotel = repo.findById(id).orElseThrow(() -> new NotFoundException("Hotel not found"));
        currentUserService.assertHotelAccess(user, hotel);
        return ApiResponse.ok("Hotel", hotel);
    }

    @PostMapping
    public ApiResponse<Hotel> create(Principal principal, @Valid @RequestBody HotelRequest r) {
        UserAccount user = currentUserService.get(principal);
        Hotel h = new Hotel();
        apply(h, r);
        applyOwner(user, h, r);
        return ApiResponse.ok("Hotel created", repo.save(h));
    }

    @PutMapping("/{id}")
    public ApiResponse<Hotel> update(Principal principal, @PathVariable String id, @Valid @RequestBody HotelRequest r) {
        UserAccount user = currentUserService.get(principal);
        Hotel h = repo.findById(id).orElseThrow(() -> new NotFoundException("Hotel not found"));
        currentUserService.assertHotelAccess(user, h);
        apply(h, r);
        applyOwner(user, h, r);
        return ApiResponse.ok("Hotel updated", repo.save(h));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Principal principal, @PathVariable String id) {
        UserAccount user = currentUserService.get(principal);
        Hotel h = repo.findById(id).orElseThrow(() -> new NotFoundException("Hotel not found"));
        currentUserService.assertHotelAccess(user, h);
        h.setActive(false);
        repo.save(h);
        return ApiResponse.ok("Hotel disabled", null);
    }

    private void applyOwner(UserAccount currentUser, Hotel hotel, HotelRequest request) {
        if (currentUserService.isHotelOwner(currentUser)) {
            hotel.setOwnerUser(currentUser);
            return;
        }
        if (currentUserService.isSuperAdmin(currentUser)) {
            UserAccount owner = null;
            if (request.ownerUserId() != null && !request.ownerUserId().isBlank()) {
                owner = userRepository.findById(request.ownerUserId())
                        .orElseThrow(() -> new NotFoundException("Owner user not found"));
            } else if (request.ownerEmail() != null && !request.ownerEmail().isBlank()) {
                owner = userRepository.findByEmail(request.ownerEmail())
                        .orElseThrow(() -> new NotFoundException("Owner user not found"));
            }
            if (owner != null) {
                if (!currentUserService.isHotelOwner(owner)) {
                    throw new NotFoundException("Selected user is not a hotel owner");
                }
                hotel.setOwnerUser(owner);
            }
        }
    }

    private void apply(Hotel h, HotelRequest r) {
        h.setName(r.name());
        h.setSlug((r.slug() == null || r.slug().isBlank()) ? SlugUtil.toSlug(r.name()) : SlugUtil.toSlug(r.slug()));
        h.setDescription(r.description());
        h.setStarRating(r.starRating());
        h.setGstNumber(r.gstNumber());
        h.setAddressLine1(r.addressLine1());
        h.setAddressLine2(r.addressLine2());
        h.setCity(r.city());
        h.setState(r.state());
        h.setCountry(r.country() == null ? "India" : r.country());
        h.setPincode(r.pincode());
        h.setPhone(r.phone());
        h.setEmail(r.email());
        h.setWebsite(r.website());
    }
}
