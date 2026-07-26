package com.hospital.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Prescription implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String appointmentId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String date;
    private List<PrescriptionItem> items;
    private String status; // "PENDING", "DISPENSED"
    private String clinicalNotes;
    private List<String> administrationLogs;

    public static class PrescriptionItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String medicineName;
        private String dosage;
        private String duration;
        private int quantity;

        public PrescriptionItem(String medicineName, String dosage, String duration, int quantity) {
            this.medicineName = medicineName;
            this.dosage = dosage;
            this.duration = duration;
            this.quantity = quantity;
        }

        public String getMedicineName() { return medicineName; }
        public String getDosage() { return dosage; }
        public String getDuration() { return duration; }
        public int getQuantity() { return quantity; }
    }

    public Prescription(String id, String appointmentId, String patientId, String patientName,
                        String doctorId, String doctorName, String date, String clinicalNotes) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.date = date;
        this.clinicalNotes = clinicalNotes;
        this.items = new ArrayList<>();
        this.status = "PENDING";
        this.administrationLogs = new ArrayList<>();
    }

    public String getId() { return id; }
    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public List<PrescriptionItem> getItems() { return items; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getClinicalNotes() { return clinicalNotes; }
    public List<String> getAdministrationLogs() { return administrationLogs; }

    public void addAdministrationLog(String log) {
        if (this.administrationLogs == null) {
            this.administrationLogs = new ArrayList<>();
        }
        this.administrationLogs.add(log);
    }

    public void addItem(String medicineName, String dosage, String duration, int quantity) {
        this.items.add(new PrescriptionItem(medicineName, dosage, duration, quantity));
    }
}
