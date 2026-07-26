package com.hospital.model;

import java.io.Serializable;

public class LabTest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String testName;
    private String dateRequested;
    private String status; // e.g., REQUESTED, COMPLETED, SAMPLE_REJECTED
    private String resultNotes;
    private double price;
    private String rejectionReason;

    public LabTest(String id, String patientId, String patientName, String doctorId, String doctorName, String testName, String dateRequested, double price) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.testName = testName;
        this.dateRequested = dateRequested;
        this.status = "REQUESTED";
        this.resultNotes = "";
        this.price = price;
        this.rejectionReason = "";
    }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getDateRequested() { return dateRequested; }
    public void setDateRequested(String dateRequested) { this.dateRequested = dateRequested; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResultNotes() { return resultNotes; }
    public void setResultNotes(String resultNotes) { this.resultNotes = resultNotes; }
}
