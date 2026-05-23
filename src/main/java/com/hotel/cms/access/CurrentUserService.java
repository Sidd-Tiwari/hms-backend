package com.hotel.cms.access;

import com.hotel.cms.exception.NotFoundException;
import com.hotel.cms.hotel.Hotel;
import com.hotel.cms.hotel.HotelRepository;
import com.hotel.cms.user.UserAccount;
import com.hotel.cms.user.UserRepository;
import java.security.Principal;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;

    public CurrentUserService(UserRepository userRepository, HotelRepository hotelRepository) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
    }

    public UserAccount get(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new NotFoundException("Logged-in user not found");
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new NotFoundException("Logged-in user not found"));
    }

    public boolean hasRole(UserAccount user, String roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getName().equals(roleName));
    }

    public boolean isSuperAdmin(UserAccount user) {
        return hasRole(user, "SUPER_ADMIN");
    }

    public boolean isHotelOwner(UserAccount user) {
        return hasRole(user, "HOTEL_OWNER");
    }

    public boolean isUser(UserAccount user) {
        return hasRole(user, "USER");
    }

    public void assertHotelAccess(UserAccount user, Hotel hotel) {
        if (isSuperAdmin(user)) {
            return;
        }
        if (isHotelOwner(user)
                && hotel.getOwnerUser() != null
                && hotel.getOwnerUser().getId().equals(user.getId())) {
            return;
        }
        throw new NotFoundException("Hotel not found for current account");
    }

    public Hotel getHotelForCurrentOwnerOrAdmin(UserAccount user, String hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found"));
        assertHotelAccess(user, hotel);
        return hotel;
    }
}
