package com.hospital.model;

public class BillingStaff extends User {
    private static final long serialVersionUID = 1L;

    public BillingStaff(String id, String username, String password, String fullName, String email, String phone) {
        super(id, username, password, fullName, email, phone, Role.BILLING);
    }
}
