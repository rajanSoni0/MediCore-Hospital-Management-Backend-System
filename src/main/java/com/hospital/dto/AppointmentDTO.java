package com.hospital.dto;

import com.hospital.model.Appointment.AppointmentStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

public class AppointmentDTO {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotNull(message = "Patient ID is required")
        private Long patientId;

        @NotNull(message = "Doctor ID is required")
        private Long doctorId;

        @NotNull(message = "Appointment date is required")
        @Future(message = "Appointment must be in the future")
        private LocalDateTime appointmentDate;

        private String reason;
        private String notes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long patientId;
        private String patientName;
        private Long doctorId;
        private String doctorName;
        private String doctorSpecialization;
        private LocalDateTime appointmentDate;
        private String reason;
        private String notes;
        private AppointmentStatus status;
        private Boolean webhookNotified;
        private LocalDateTime createdAt;
    }
}
