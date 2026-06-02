package com.hospital.controller;

import com.hospital.dto.AppointmentDTO;
import com.hospital.model.Appointment.AppointmentStatus;
import com.hospital.service.AppointmentService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private static final Logger logger = LogManager.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** POST /api/appointments — Book a new appointment (triggers webhook) */
    @PostMapping
    public ResponseEntity<AppointmentDTO.Response> book(@Valid @RequestBody AppointmentDTO.Request request) {
        logger.debug("POST /api/appointments");
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request));
    }

    /** GET /api/appointments/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /**
     * GET /api/appointments
     * Optional: ?patientId=1  or  ?doctorId=2
     */
    @GetMapping
    public ResponseEntity<List<AppointmentDTO.Response>> getAll(
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {

        if (patientId != null) {
            return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
        }
        if (doctorId != null) {
            return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
        }
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    /** PATCH /api/appointments/{id}/status?status=CONFIRMED */
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentDTO.Response> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    /**
     * PATCH /api/appointments/{id}/reschedule
     * Body: { "newDate": "2026-05-15T10:30:00" }
     */
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentDTO.Response> reschedule(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate) {
        return ResponseEntity.ok(appointmentService.reschedule(id, newDate));
    }

    /** DELETE /api/appointments/{id} — Cancel */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
