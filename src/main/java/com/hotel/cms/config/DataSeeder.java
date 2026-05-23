package com.hotel.cms.config;

import com.hotel.cms.common.enums.DiscountType;
import com.hotel.cms.common.enums.RoomStatus;
import com.hotel.cms.hotel.*;
import com.hotel.cms.room.*;
import com.hotel.cms.customer.Customer;
import com.hotel.cms.customer.CustomerRepository;
import com.hotel.cms.promo.PromoCode;
import com.hotel.cms.promo.PromoCodeRepository;
import com.hotel.cms.user.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final HotelRepository hotelRepo;
    private final RoomTypeRepository roomTypeRepo;
    private final RoomRepository roomRepo;
    private final CustomerRepository customerRepo;
    private final PromoCodeRepository promoRepo;

    public DataSeeder(
            RoleRepository roleRepo,
            UserRepository userRepo,
            PasswordEncoder encoder,
            HotelRepository hotelRepo,
            RoomTypeRepository roomTypeRepo,
            RoomRepository roomRepo,
            CustomerRepository customerRepo,
            PromoCodeRepository promoRepo
    ) {
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.hotelRepo = hotelRepo;
        this.roomTypeRepo = roomTypeRepo;
        this.roomRepo = roomRepo;
        this.customerRepo = customerRepo;
        this.promoRepo = promoRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = getOrCreateRole("SUPER_ADMIN", "Full access");
        Role ownerRole = getOrCreateRole("HOTEL_OWNER", "Hotel owner access");
        Role userRole = getOrCreateRole("USER", "Website booking user access");

        getOrCreateUser("System Admin", "admin@hotel.com", "9999999999", "Admin@123", adminRole);
        UserAccount owner = getOrCreateUser("Demo Hotel Owner", "owner@hotel.com", "8888888888", "Owner@123", ownerRole);
        UserAccount demoUser = getOrCreateUser("Demo User", "user@hotel.com", "7777777777", "User@123", userRole);
        getOrCreateCustomerForUser(demoUser);
        assignExistingUnownedHotels(owner);

        if (hotelRepo.count() == 0) {
            Hotel h = new Hotel();
            h.setOwnerUser(owner);
            h.setName("Demo Grand Hotel");
            h.setSlug("demo-grand-hotel");
            h.setDescription("Demo hotel for CMS");
            h.setStarRating(new BigDecimal("4.50"));
            h.setGstNumber("09ABCDE1234F1Z5");
            h.setCity("Varanasi");
            h.setState("Uttar Pradesh");
            h.setAddressLine1("Main Road");
            h.setPhone("+91-9999999999");
            h.setEmail("info@demo.com");
            h = hotelRepo.save(h);

            RoomType rt = new RoomType();
            rt.setHotel(h);
            rt.setName("Deluxe Room");
            rt.setBasePrice(new BigDecimal("2999.00"));
            rt.setMaxAdults(2);
            rt.setMaxChildren(1);
            rt.setMaxOccupancy(3);
            rt.setBedType("King Bed");
            rt = roomTypeRepo.save(rt);

            if (promoRepo.findByCodeIgnoreCase("WELCOME10").isEmpty()) {
                PromoCode promo = new PromoCode();
                promo.setHotel(h);
                promo.setCode("WELCOME10");
                promo.setDescription("10% welcome discount for online users");
                promo.setDiscountType(DiscountType.PERCENTAGE);
                promo.setDiscountValue(new BigDecimal("10.00"));
                promo.setMinBookingAmount(new BigDecimal("1000.00"));
                promo.setMaxDiscountAmount(new BigDecimal("1000.00"));
                promo.setStartDate(LocalDate.now().minusDays(1));
                promo.setEndDate(LocalDate.now().plusMonths(6));
                promoRepo.save(promo);
            }

            for (int i = 101; i <= 106; i++) {
                Room r = new Room();
                r.setHotel(h);
                r.setRoomType(rt);
                r.setRoomNumber(String.valueOf(i));
                r.setFloorNumber("1");
                r.setStatus(RoomStatus.AVAILABLE);
                roomRepo.save(r);
            }
        }
    }

    private void assignExistingUnownedHotels(UserAccount owner) {
        hotelRepo.findAll().stream()
                .filter(Hotel::isActive)
                .filter(hotel -> hotel.getOwnerUser() == null)
                .forEach(hotel -> {
                    hotel.setOwnerUser(owner);
                    hotelRepo.save(hotel);
                });
    }

    private void getOrCreateCustomerForUser(UserAccount user) {
        customerRepo.findByUserAccountId(user.getId()).orElseGet(() -> {
            Customer c = new Customer();
            c.setUserAccount(user);
            c.setFullName(user.getFullName());
            c.setEmail(user.getEmail());
            c.setPhone(user.getPhone() == null || user.getPhone().isBlank() ? "N/A" : user.getPhone());
            c.setActive(true);
            return customerRepo.save(c);
        });
    }

    private Role getOrCreateRole(String name, String description) {
        return roleRepo.findByName(name).orElseGet(() -> {
            Role r = new Role();
            r.setName(name);
            r.setDescription(description);
            return roleRepo.save(r);
        });
    }

    private UserAccount getOrCreateUser(String fullName, String email, String phone, String password, Role role) {
        return userRepo.findByEmail(email).map(existing -> {
            if (existing.getRoles().stream().noneMatch(r -> r.getName().equals(role.getName()))) {
                existing.getRoles().add(role);
                return userRepo.save(existing);
            }
            return existing;
        }).orElseGet(() -> {
            UserAccount u = new UserAccount();
            u.setFullName(fullName);
            u.setEmail(email);
            u.setPhone(phone);
            u.setPasswordHash(encoder.encode(password));
            u.setRoles(Set.of(role));
            return userRepo.save(u);
        });
    }
}
