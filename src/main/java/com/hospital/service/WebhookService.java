package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.model.Appointment;
import com.hospital.repository.AppointmentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookService {

    private static final Logger logger = LogManager.getLogger(WebhookService.class);

    private final RestTemplate webhookRestTemplate;
    private final AppointmentRepository appointmentRepository;

    @Value("${webhook.appointment.url}")
    private String webhookUrl;

    @Value("${webhook.enabled:true}")
    private boolean webhookEnabled;

    public WebhookService(
            @Qualifier("webhookRestTemplate") RestTemplate webhookRestTemplate,
            AppointmentRepository appointmentRepository) {
        this.webhookRestTemplate = webhookRestTemplate;
        this.appointmentRepository = appointmentRepository;
    }

    /**
     * Fire-and-forget: notifies the external system asynchronously.
     * Runs in a separate thread so the API response is not delayed.
     */
    @Async
    public void notifyAppointmentCreated(Appointment appointment) {
        if (!webhookEnabled) {
            logger.info("Webhook disabled — skipping notification for appointment id={}", appointment.getId());
            return;
        }

        logger.info("Sending webhook for appointment id={}, patient={} {}, doctor={} {}",
                appointment.getId(),
                appointment.getPatient().getFirstName(),
                appointment.getPatient().getLastName(),
                appointment.getDoctor().getFirstName(),
                appointment.getDoctor().getLastName());

        try {
            Map<String, Object> payload = buildPayload(appointment);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Hospital-Event", "APPOINTMENT_CREATED");
            headers.set("X-Appointment-Id", String.valueOf(appointment.getId()));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = webhookRestTemplate.postForEntity(
                    webhookUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Webhook delivered successfully for appointment id={}. HTTP {}",
                        appointment.getId(), response.getStatusCode().value());
                markNotified(appointment.getId());
            } else {
                logger.warn("Webhook returned non-2xx for appointment id={}. HTTP {}",
                        appointment.getId(), response.getStatusCode().value());
            }

        } catch (RestClientException ex) {
            logger.error("Webhook delivery failed for appointment id={}: {}",
                    appointment.getId(), ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error during webhook for appointment id={}: ",
                    appointment.getId(), ex);
        }
    }

    private Map<String, Object> buildPayload(Appointment appointment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "APPOINTMENT_CREATED");
        payload.put("appointmentId", appointment.getId());
        payload.put("patientId", appointment.getPatient().getId());
        payload.put("patientName",
                appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName());
        payload.put("doctorId", appointment.getDoctor().getId());
        payload.put("doctorName",
                appointment.getDoctor().getFirstName() + " " + appointment.getDoctor().getLastName());
        payload.put("doctorSpecialization", appointment.getDoctor().getSpecialization());
        payload.put("appointmentDate", appointment.getAppointmentDate().toString());
        payload.put("reason", appointment.getReason());
        payload.put("status", appointment.getStatus().name());
        return payload;
    }

    private void markNotified(Long appointmentId) {
        appointmentRepository.findById(appointmentId).ifPresent(a -> {
            a.setWebhookNotified(true);
            appointmentRepository.save(a);
            logger.debug("Marked appointment id={} as webhook-notified", appointmentId);
        });
    }
}
