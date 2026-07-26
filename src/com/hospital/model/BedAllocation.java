package com.hospital.model;

import java.io.Serializable;

public class BedAllocation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String bedNumber;
    private String wardName;
    private String patientId;
    private String patientName;
    private String admissionDate;
    private String dischargeDate;
    private String status; // "AVAILABLE", "OCCUPIED", "DISCHARGED"
    private double dailyRate;

    public BedAllocation(String id, String bedNumber, String wardName, String patientId, String patientName,
                         String admissionDate, String status, double dailyRate) {
        this.id = id;
        this.bedNumber = bedNumber;
        this.wardName = wardName;
        this.patientId = patientId;
        this.patientName = patientName;
        this.admissionDate = admissionDate;
        this.dischargeDate = "";
        this.status = status;
        this.dailyRate = dailyRate;
    }

    public String getId() { return id; }
    public String getBedNumber() { return bedNumber; }
    public String getWardName() { return wardName; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getAdmissionDate() { return admissionDate; }
    public void setAdmissionDate(String admissionDate) { this.admissionDate = admissionDate; }
    public String getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(String dischargeDate) { this.dischargeDate = dischargeDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getDailyRate() { return dailyRate; }
}
