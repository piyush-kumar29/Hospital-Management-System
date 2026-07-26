package com.hospital.model;

import java.io.Serializable;

public enum Role implements Serializable {
    SYSTEM_ADMIN("System Admin"),
    RECEPTIONIST("Receptionist / Front Desk"),
    DOCTOR("Doctor"),
    NURSE("Nurse / Ward Staff"),
    PHARMACIST("Pharmacist"),
    BILLING("Billing & Accounts"),
    PATIENT("Patient");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
