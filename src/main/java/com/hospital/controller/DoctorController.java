package com.hospital.controller;

import com.hospital.dto.DoctorDTO;
import com.hospital.model.Doctor.DoctorStatus;
import com.hospital.service.DoctorService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private static final Logger logger = LogManager.getLogger(DoctorController.class);

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    /** POST /api/doctors */
    @PostMapping
    public ResponseEntity<DoctorDTO.Response> register(@Valid @RequestBody DoctorDTO.Request request) {
        logger.debug("POST /api/doctors");
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.registerDoctor(request));
    }

    /** GET /api/doctors/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    /**
     * GET /api/doctors
     * Optional query params:
     *   ?specialization=Cardiology
     *   ?available=true&specialization=Cardiology
     *   ?search=sharma
     */
    @GetMapping
    public ResponseEntity<List<DoctorDTO.Response>> getAll(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "false") boolean available) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(doctorService.searchDoctors(search));
        }
        if (available) {
            return ResponseEntity.ok(doctorService.getAvailableDoctors(specialization));
        }
        if (specialization != null && !specialization.isBlank()) {
            return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(specialization));
        }
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    /** PUT /api/doctors/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody DoctorDTO.Request request) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    /** PATCH /api/doctors/{id}/status?status=ON_LEAVE */
    @PatchMapping("/{id}/status")
    public ResponseEntity<DoctorDTO.Response> updateStatus(
            @PathVariable Long id,
            @RequestParam DoctorStatus status) {
        return ResponseEntity.ok(doctorService.updateStatus(id, status));
    }

    /** DELETE /api/doctors/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
