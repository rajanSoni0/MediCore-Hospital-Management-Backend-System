package com.hospital.dto;

import com.hospital.model.Doctor.DoctorStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class DoctorDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Specialization is required")
        private String specialization;

        @NotBlank(message = "License number is required")
        private String licenseNumber;

        @Email(message = "Enter a valid email")
        private String email;

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit mobile number")
        private String phone;

        private Integer yearsOfExperience;
        private Double consultationFee;
        private String availableDays;
        private LocalTime shiftStart;
        private LocalTime shiftEnd;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private String specialization;
        private String licenseNumber;
        private String email;
        private String phone;
        private Integer yearsOfExperience;
        private Double consultationFee;
        private String availableDays;
        private LocalTime shiftStart;
        private LocalTime shiftEnd;
        private DoctorStatus status;
        private LocalDateTime createdAt;
    }
}
