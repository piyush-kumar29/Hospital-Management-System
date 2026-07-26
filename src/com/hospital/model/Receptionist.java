package com.hospital.model;

public class Receptionist extends User {
    private static final long serialVersionUID = 1L;

    public Receptionist(String id, String username, String password, String fullName, String email, String phone) {
        super(id, username, password, fullName, email, phone, Role.RECEPTIONIST);
    }
}
