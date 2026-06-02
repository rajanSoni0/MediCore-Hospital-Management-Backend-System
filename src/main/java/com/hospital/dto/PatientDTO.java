package com.hospital.dto;

import com.hospital.model.Patient.PatientStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PatientDTO {

    /** Request DTO — used for create and update */
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

        @NotNull(message = "Date of birth is required")
        private LocalDate dateOfBirth;

        @NotBlank(message = "Gender is required")
        private String gender;

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
        private String phone;

        @Email(message = "Enter a valid email")
        private String email;

        private String address;
        private String bloodGroup;
        private String medicalHistory;
    }

    /** Response DTO — returned to the client */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String gender;
        private String phone;
        private String email;
        private String address;
        private String bloodGroup;
        private String medicalHistory;
        private PatientStatus status;
        private LocalDateTime registeredAt;
    }
}
