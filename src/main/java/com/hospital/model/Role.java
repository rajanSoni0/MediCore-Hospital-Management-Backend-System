package com.hospital.model;

/**
 * Application roles.
 * Stored as a string in the DB via @Enumerated(EnumType.STRING) on the User entity.
 *
 * Spring Security expects roles to be prefixed with "ROLE_" when used with
 * hasRole(). We keep the enum clean and add the prefix in UserDetails.getAuthorities().
 */
public enum Role {
    PATIENT,
    DOCTOR,
    ADMIN
}
