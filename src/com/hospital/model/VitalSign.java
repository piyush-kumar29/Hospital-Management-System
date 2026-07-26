package com.hospital.model;

import java.io.Serializable;

public class VitalSign implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String patientId;
    private String patientName;
    private String recordedByNurse;
    private String timestamp;
    private String bloodPressure;
    private int heartRate;
    private double temperature;
    private int respiratoryRate;
    private String notes;

    public VitalSign(String id, String patientId, String patientName, String recordedByNurse, String timestamp,
                     String bloodPressure, int heartRate, double temperature, int respiratoryRate, String notes) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.recordedByNurse = recordedByNurse;
        this.timestamp = timestamp;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.temperature = temperature;
        this.respiratoryRate = respiratoryRate;
        this.notes = notes;
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getRecordedByNurse() { return recordedByNurse; }
    public String getTimestamp() { return timestamp; }
    public String getBloodPressure() { return bloodPressure; }
    public int getHeartRate() { return heartRate; }
    public double getTemperature() { return temperature; }
    public int getRespiratoryRate() { return respiratoryRate; }
    public String getNotes() { return notes; }
}
