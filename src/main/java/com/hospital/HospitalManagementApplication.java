package com.hospital;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HospitalManagementApplication {

    private static final Logger logger = LogManager.getLogger(HospitalManagementApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(HospitalManagementApplication.class, args);
        logger.info("=========================================================");
        logger.info("  Hospital Management System started successfully");
        logger.info("  API available at: http://localhost:8080/api");
        logger.info("=========================================================");
    }
}
