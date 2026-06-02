package com.hospital.service;

import com.hospital.dto.PatientDTO;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Patient;
import com.hospital.model.Patient.PatientStatus;
import com.hospital.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private static final Logger logger = LogManager.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientDTO.Response registerPatient(PatientDTO.Request request) {
        logger.info("Registering new patient: {} {}", request.getFirstName(), request.getLastName());

        if (patientRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("A patient with phone " + request.getPhone() + " already exists.");
        }
        if (request.getEmail() != null && patientRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A patient with email " + request.getEmail() + " already exists.");
        }

        Patient patient = Patient.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .bloodGroup(request.getBloodGroup())
                .medicalHistory(request.getMedicalHistory())
                .build();

        Patient saved = patientRepository.save(patient);
        logger.info("Patient registered with id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientDTO.Response getPatientById(Long id) {
        logger.debug("Fetching patient id={}", id);
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<PatientDTO.Response> getAllPatients() {
        logger.debug("Fetching all patients");
        return patientRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientDTO.Response> searchPatients(String name) {
        logger.debug("Searching patients by name={}", name);
        return patientRepository.searchByName(name).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PatientDTO.Response> getPatientsByBloodGroup(String bloodGroup) {
        return patientRepository.findByBloodGroup(bloodGroup).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PatientDTO.Response updatePatient(Long id, PatientDTO.Request request) {
        logger.info("Updating patient id={}", id);
        Patient patient = findById(id);

        // If phone changed, check for conflicts
        if (!patient.getPhone().equals(request.getPhone()) && patientRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone " + request.getPhone() + " is already in use.");
        }

        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setPhone(request.getPhone());
        patient.setEmail(request.getEmail());
        patient.setAddress(request.getAddress());
        patient.setBloodGroup(request.getBloodGroup());
        patient.setMedicalHistory(request.getMedicalHistory());

        return toResponse(patientRepository.save(patient));
    }

    public PatientDTO.Response updateStatus(Long id, PatientStatus status) {
        logger.info("Updating status of patient id={} to {}", id, status);
        Patient patient = findById(id);
        patient.setStatus(status);
        return toResponse(patientRepository.save(patient));
    }

    public void deletePatient(Long id) {
        logger.info("Deleting patient id={}", id);
        Patient patient = findById(id);
        patientRepository.delete(patient);
    }

    // ---- helpers ----

    private Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    public PatientDTO.Response toResponse(Patient p) {
        return PatientDTO.Response.builder()
                .id(p.getId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .dateOfBirth(p.getDateOfBirth())
                .gender(p.getGender())
                .phone(p.getPhone())
                .email(p.getEmail())
                .address(p.getAddress())
                .bloodGroup(p.getBloodGroup())
                .medicalHistory(p.getMedicalHistory())
                .status(p.getStatus())
                .registeredAt(p.getRegisteredAt())
                .build();
    }
}
