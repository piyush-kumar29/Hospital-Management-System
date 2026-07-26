package com.hospital.model;

import java.io.Serializable;

public class IPDAdmissionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String patientId;
    private String patientName;
    private String requestingDoctorId;
    private String requestingDoctorName;
    private String targetWard;
    private String requestDate;
    private String status; // "PENDING", "ADMITTED"

    public IPDAdmissionRequest(String id, String patientId, String patientName, String requestingDoctorId, String requestingDoctorName, String targetWard, String requestDate) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.requestingDoctorId = requestingDoctorId;
        this.requestingDoctorName = requestingDoctorName;
        this.targetWard = targetWard;
        this.requestDate = requestDate;
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getRequestingDoctorId() { return requestingDoctorId; }
    public String getRequestingDoctorName() { return requestingDoctorName; }
    public String getTargetWard() { return targetWard; }
    public String getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
