package com.hotel.cms.hotel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hotel.cms.common.BaseEntity;
import com.hotel.cms.user.UserAccount;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "hotels")
public class Hotel extends BaseEntity {
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private UserAccount ownerUser;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "star_rating", precision = 3, scale = 2)
    private BigDecimal starRating;

    @Column(name = "gst_number", length = 40)
    private String gstNumber;

    @Column(name = "address_line_1")
    private String addressLine1;

    @Column(name = "address_line_2")
    private String addressLine2;

    @Column(nullable = false)
    private String city;

    private String state;
    private String country = "India";
    private String pincode;
    private String phone;
    private String email;
    private String website;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
