package com.hospital.model;

public class Patient extends User {
    private static final long serialVersionUID = 1L;

    private int age;
    private String gender;
    private String bloodGroup;
    private String address;
    private String emergencyContact;
    private String patientType; // OPD or IPD

    public Patient(String id, String username, String password, String fullName, String email, String phone,
                   int age, String gender, String bloodGroup, String address, String emergencyContact, String patientType) {
        super(id, username, password, fullName, email, phone, Role.PATIENT);
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.patientType = patientType;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getPatientType() {
        return patientType;
    }

    public void setPatientType(String patientType) {
        this.patientType = patientType;
    }
}
