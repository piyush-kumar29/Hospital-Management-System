package com.hospital.model;

public class Pharmacist extends User {
    private static final long serialVersionUID = 1L;

    public Pharmacist(String id, String username, String password, String fullName, String email, String phone) {
        super(id, username, password, fullName, email, phone, Role.PHARMACIST);
    }
}
