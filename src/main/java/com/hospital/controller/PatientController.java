package com.hospital.controller;

import com.hospital.dto.PatientDTO;
import com.hospital.model.Patient.PatientStatus;
import com.hospital.service.PatientService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private static final Logger logger = LogManager.getLogger(PatientController.class);

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /** POST /api/patients — Register a new patient */
    @PostMapping
    public ResponseEntity<PatientDTO.Response> register(@Valid @RequestBody PatientDTO.Request request) {
        logger.debug("POST /api/patients");
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.registerPatient(request));
    }

    /** GET /api/patients/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /** GET /api/patients */
    @GetMapping
    public ResponseEntity<List<PatientDTO.Response>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String bloodGroup) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(patientService.searchPatients(search));
        }
        if (bloodGroup != null && !bloodGroup.isBlank()) {
            return ResponseEntity.ok(patientService.getPatientsByBloodGroup(bloodGroup));
        }
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    /** PUT /api/patients/{id} — Full update */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientDTO.Request request) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    /** PATCH /api/patients/{id}/status?status=INACTIVE */
    @PatchMapping("/{id}/status")
    public ResponseEntity<PatientDTO.Response> updateStatus(
            @PathVariable Long id,
            @RequestParam PatientStatus status) {
        return ResponseEntity.ok(patientService.updateStatus(id, status));
    }

    /** DELETE /api/patients/{id} */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
