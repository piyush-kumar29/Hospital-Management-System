package com.hospital.model;

public class Patient extends User {
    private static final long serialVersionUID = 1L;

    private int age;
    private String gender;
    private String bloodGroup;
    private String address;
    private String emergencyContact;
    private String patientType; // OPD or IPD
    private String insuranceProvider; // e.g., "Cash", "TPA/BlueCross"
    
    // New Workflows (Phase 3)
    private boolean isMLC;
    private boolean clinicalDischarge;
    private boolean financialDischarge;
    private String gatePassId;
    private java.util.List<String> crossConsultDoctorIds;

    public Patient(String id, String username, String password, String fullName, String email, String phone,
                   int age, String gender, String bloodGroup, String address, String emergencyContact, String patientType, String insuranceProvider) {
        super(id, username, password, fullName, email, phone, Role.PATIENT);
        this.age = age;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.emergencyContact = emergencyContact;
        this.patientType = patientType;
        this.insuranceProvider = insuranceProvider;
        this.isMLC = false;
        this.clinicalDischarge = false;
        this.financialDischarge = false;
        this.gatePassId = "";
        this.crossConsultDoctorIds = new java.util.ArrayList<>();
    }

    public boolean isMLC() { return isMLC; }
    public void setMLC(boolean isMLC) { this.isMLC = isMLC; }
    public boolean isClinicalDischarge() { return clinicalDischarge; }
    public void setClinicalDischarge(boolean clinicalDischarge) { this.clinicalDischarge = clinicalDischarge; }
    public boolean isFinancialDischarge() { return financialDischarge; }
    public void setFinancialDischarge(boolean financialDischarge) { this.financialDischarge = financialDischarge; }
    public String getGatePassId() { return gatePassId; }
    public void setGatePassId(String gatePassId) { this.gatePassId = gatePassId; }
    public java.util.List<String> getCrossConsultDoctorIds() { return crossConsultDoctorIds; }
    public void addCrossConsultDoctor(String docId) {
        if (!this.crossConsultDoctorIds.contains(docId)) {
            this.crossConsultDoctorIds.add(docId);
        }
    }

    public String getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(String insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
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
