package com.hospital.model;

public class LabTech extends User {
    private static final long serialVersionUID = 1L;
    
    private String department; // e.g., Pathology, Radiology

    public LabTech(String id, String username, String password, String fullName, String email, String phone, String department) {
        super(id, username, password, fullName, email, phone, Role.LAB_TECH);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
