package com.hospital.model;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    public Admin(String id, String username, String password, String fullName, String email, String phone) {
        super(id, username, password, fullName, email, phone, Role.SYSTEM_ADMIN);
    }
}
