package com.hospital.model;

public class Nurse extends User {
    private static final long serialVersionUID = 1L;

    private String assignedWard;

    public Nurse(String id, String username, String password, String fullName, String email, String phone, String assignedWard) {
        super(id, username, password, fullName, email, phone, Role.NURSE);
        this.assignedWard = assignedWard;
    }

    public String getAssignedWard() {
        return assignedWard;
    }

    public void setAssignedWard(String assignedWard) {
        this.assignedWard = assignedWard;
    }
}
