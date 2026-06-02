package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.exception.BadRequestException;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Appointment;
import com.hospital.model.Appointment.AppointmentStatus;
import com.hospital.model.Doctor;
import com.hospital.model.Patient;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private static final Logger logger = LogManager.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final WebhookService webhookService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorService doctorService,
            WebhookService webhookService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.webhookService = webhookService;
    }

    public AppointmentDTO.Response createAppointment(AppointmentDTO.Request request) {
        logger.info("Creating appointment: patientId={}, doctorId={}, date={}",
                request.getPatientId(), request.getDoctorId(), request.getAppointmentDate());

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.getPatientId()));

        Doctor doctor = doctorService.findById(request.getDoctorId());

        // Conflict check: same doctor within 30-minute window
        LocalDateTime start = request.getAppointmentDate().minusMinutes(29);
        LocalDateTime end   = request.getAppointmentDate().plusMinutes(29);
        List<Appointment> conflicts = appointmentRepository
                .findDoctorAppointmentsInRange(doctor.getId(), start, end);

        if (!conflicts.isEmpty()) {
            logger.warn("Scheduling conflict for doctor id={} at {}", doctor.getId(), request.getAppointmentDate());
            throw new BadRequestException(
                    "Dr. " + doctor.getFirstName() + " " + doctor.getLastName() +
                    " already has an appointment near " + request.getAppointmentDate() +
                    ". Please choose a different time slot.");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(request.getAppointmentDate())
                .reason(request.getReason())
                .notes(request.getNotes())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        logger.info("Appointment created with id={}", saved.getId());

        // Trigger webhook asynchronously — does not block the HTTP response
        webhookService.notifyAppointmentCreated(saved);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public AppointmentDTO.Response getAppointmentById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO.Response> getAllAppointments() {
        return appointmentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO.Response> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO.Response> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AppointmentDTO.Response updateStatus(Long id, AppointmentStatus newStatus) {
        logger.info("Updating appointment id={} status to {}", id, newStatus);
        Appointment appointment = findById(id);

        // Business rule: cannot change a COMPLETED or CANCELLED appointment
        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot change status of a " + appointment.getStatus() + " appointment.");
        }

        appointment.setStatus(newStatus);
        return toResponse(appointmentRepository.save(appointment));
    }

    public AppointmentDTO.Response reschedule(Long id, LocalDateTime newDate) {
        logger.info("Rescheduling appointment id={} to {}", id, newDate);
        Appointment appointment = findById(id);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot reschedule a cancelled appointment.");
        }
        if (newDate.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("New appointment date must be in the future.");
        }

        appointment.setAppointmentDate(newDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        return toResponse(appointmentRepository.save(appointment));
    }

    public void cancelAppointment(Long id) {
        logger.info("Cancelling appointment id={}", id);
        Appointment appointment = findById(id);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    // ---- helpers ----

    private Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    private AppointmentDTO.Response toResponse(Appointment a) {
        return AppointmentDTO.Response.builder()
                .id(a.getId())
                .patientId(a.getPatient().getId())
                .patientName(a.getPatient().getFirstName() + " " + a.getPatient().getLastName())
                .doctorId(a.getDoctor().getId())
                .doctorName(a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName())
                .doctorSpecialization(a.getDoctor().getSpecialization())
                .appointmentDate(a.getAppointmentDate())
                .reason(a.getReason())
                .notes(a.getNotes())
                .status(a.getStatus())
                .webhookNotified(a.getWebhookNotified())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
