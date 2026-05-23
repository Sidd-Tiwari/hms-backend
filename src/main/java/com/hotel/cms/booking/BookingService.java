package com.hotel.cms.booking;

import com.hotel.cms.booking.dto.BookingRequest;
import com.hotel.cms.common.enums.*;
import com.hotel.cms.customer.CustomerRepository;
import com.hotel.cms.exception.*;
import com.hotel.cms.hotel.HotelRepository;
import com.hotel.cms.room.*;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final BookingRoomRepository bookingRoomRepo;
    private final CustomerRepository customerRepo;
    private final HotelRepository hotelRepo;
    private final RoomRepository roomRepo;
    private final RoomTypeRepository typeRepo;

    public BookingService(
            BookingRepository b,
            BookingRoomRepository br,
            CustomerRepository c,
            HotelRepository h,
            RoomRepository r,
            RoomTypeRepository t
    ) {
        bookingRepo = b;
        bookingRoomRepo = br;
        customerRepo = c;
        hotelRepo = h;
        roomRepo = r;
        typeRepo = t;
    }

    @Transactional
    public Booking create(BookingRequest req) {
        if (!req.checkOutDate().isAfter(req.checkInDate())) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }
        if (req.rooms() == null || req.rooms().isEmpty()) {
            throw new BadRequestException("At least one room is required");
        }

        long nights = ChronoUnit.DAYS.between(req.checkInDate(), req.checkOutDate());

        Booking b = new Booking();
        b.setBookingNumber("BK" + System.currentTimeMillis());
        b.setCustomer(customerRepo.findById(req.customerId()).orElseThrow(() -> new NotFoundException("Customer not found")));
        b.setHotel(hotelRepo.findById(req.hotelId()).orElseThrow(() -> new NotFoundException("Hotel not found")));
        b.setCheckInDate(req.checkInDate());
        b.setCheckOutDate(req.checkOutDate());
        b.setAdults(req.adults() <= 0 ? 1 : req.adults());
        b.setChildren(Math.max(req.children(), 0));
        b.setSpecialRequest(req.specialRequest());
        if (req.source() != null && !req.source().isBlank()) {
            b.setSource(req.source());
        }
        b = bookingRepo.save(b);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (var line : req.rooms()) {
            Room room = roomRepo.findByIdAndActiveTrue(line.roomId())
                    .orElseThrow(() -> new NotFoundException("Room not found"));
            RoomType type = typeRepo.findById(line.roomTypeId())
                    .orElseThrow(() -> new NotFoundException("Room type not found"));

            if (!room.getHotel().getId().equals(req.hotelId())) {
                throw new BadRequestException("Selected room does not belong to selected hotel");
            }
            if (!room.getRoomType().getId().equals(type.getId())) {
                throw new BadRequestException("Room type does not match selected room");
            }
            if (room.getStatus() != RoomStatus.AVAILABLE) {
                throw new BadRequestException("Room " + room.getRoomNumber() + " is not available");
            }

            BigDecimal price = line.pricePerNight() == null ? type.getBasePrice() : line.pricePerNight();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(nights));

            BookingRoom br = new BookingRoom();
            br.setBooking(b);
            br.setRoom(room);
            br.setRoomType(type);
            br.setPricePerNight(price);
            br.setTotalNights((int) nights);
            br.setTotalPrice(lineTotal);
            bookingRoomRepo.save(br);

            subtotal = subtotal.add(lineTotal);
            room.setStatus(RoomStatus.RESERVED);
            roomRepo.save(room);
        }

        BigDecimal discount = req.discountAmount() == null ? BigDecimal.ZERO : req.discountAmount();
        BigDecimal tax = req.taxAmount() == null ? BigDecimal.ZERO : req.taxAmount();

        b.setSubtotal(subtotal);
        b.setDiscountAmount(discount);
        b.setTaxAmount(tax);
        b.setGrandTotal(subtotal.subtract(discount).add(tax));
        b.setBookingStatus(BookingStatus.CONFIRMED);
        return bookingRepo.save(b);
    }

    @Transactional
    public Booking status(String id, BookingStatus status) {
        Booking b = bookingRepo.findById(id).orElseThrow(() -> new NotFoundException("Booking not found"));
        b.setBookingStatus(status);
        var rooms = bookingRoomRepo.findByBookingId(id);
        for (var br : rooms) {
            if (status == BookingStatus.CHECKED_IN) br.getRoom().setStatus(RoomStatus.OCCUPIED);
            if (status == BookingStatus.CHECKED_OUT || status == BookingStatus.CANCELLED || status == BookingStatus.NO_SHOW) {
                br.getRoom().setStatus(RoomStatus.AVAILABLE);
            }
            roomRepo.save(br.getRoom());
        }
        return bookingRepo.save(b);
    }

    @Transactional
    public Booking cancel(String id) {
        return status(id, BookingStatus.CANCELLED);
    }
}
