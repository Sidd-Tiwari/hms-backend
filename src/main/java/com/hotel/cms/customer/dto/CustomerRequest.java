package com.hotel.cms.customer.dto;
import jakarta.validation.constraints.NotBlank;
public record CustomerRequest(@NotBlank String fullName, String email, @NotBlank String phone, String gender, String address) {}
