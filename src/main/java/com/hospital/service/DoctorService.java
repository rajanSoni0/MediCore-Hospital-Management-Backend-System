package com.hospital.service;

import com.hospital.dto.DoctorDTO;
import com.hospital.exception.DuplicateResourceException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Doctor;
import com.hospital.model.Doctor.DoctorStatus;
import com.hospital.repository.DoctorRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorService {

    private static final Logger logger = LogManager.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public DoctorDTO.Response registerDoctor(DoctorDTO.Request request) {
        logger.info("Registering doctor: {} {}", request.getFirstName(), request.getLastName());

        if (doctorRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new DuplicateResourceException("License number " + request.getLicenseNumber() + " is already registered.");
        }
        if (request.getEmail() != null && doctorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email " + request.getEmail() + " is already in use.");
        }

        Doctor doctor = Doctor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialization(request.getSpecialization())
                .licenseNumber(request.getLicenseNumber())
                .email(request.getEmail())
                .phone(request.getPhone())
                .yearsOfExperience(request.getYearsOfExperience())
                .consultationFee(request.getConsultationFee())
                .availableDays(request.getAvailableDays())
                .shiftStart(request.getShiftStart())
                .shiftEnd(request.getShiftEnd())
                .build();

        Doctor saved = doctorRepository.save(doctor);
        logger.info("Doctor registered with id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public DoctorDTO.Response getDoctorById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO.Response> getAllDoctors() {
        return doctorRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO.Response> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO.Response> getAvailableDoctors(String specialization) {
        if (specialization != null && !specialization.isBlank()) {
            return doctorRepository.findBySpecializationAndStatus(specialization, DoctorStatus.AVAILABLE)
                    .stream().map(this::toResponse).collect(Collectors.toList());
        }
        return doctorRepository.findByStatus(DoctorStatus.AVAILABLE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO.Response> searchDoctors(String name) {
        return doctorRepository.searchByName(name).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public DoctorDTO.Response updateDoctor(Long id, DoctorDTO.Request request) {
        logger.info("Updating doctor id={}", id);
        Doctor doctor = findById(id);

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        doctor.setYearsOfExperience(request.getYearsOfExperience());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setAvailableDays(request.getAvailableDays());
        doctor.setShiftStart(request.getShiftStart());
        doctor.setShiftEnd(request.getShiftEnd());

        return toResponse(doctorRepository.save(doctor));
    }

    public DoctorDTO.Response updateStatus(Long id, DoctorStatus status) {
        logger.info("Updating status of doctor id={} to {}", id, status);
        Doctor doctor = findById(id);
        doctor.setStatus(status);
        return toResponse(doctorRepository.save(doctor));
    }

    public void deleteDoctor(Long id) {
        logger.info("Deleting doctor id={}", id);
        doctorRepository.delete(findById(id));
    }

    // ---- helpers ----

    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    public DoctorDTO.Response toResponse(Doctor d) {
        return DoctorDTO.Response.builder()
                .id(d.getId())
                .firstName(d.getFirstName())
                .lastName(d.getLastName())
                .specialization(d.getSpecialization())
                .licenseNumber(d.getLicenseNumber())
                .email(d.getEmail())
                .phone(d.getPhone())
                .yearsOfExperience(d.getYearsOfExperience())
                .consultationFee(d.getConsultationFee())
                .availableDays(d.getAvailableDays())
                .shiftStart(d.getShiftStart())
                .shiftEnd(d.getShiftEnd())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
