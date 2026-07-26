package com.hospital.service;

import com.hospital.model.Patient;
import com.hospital.model.User;
import com.hospital.model.VitalSign;
import com.hospital.repository.DataStore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PatientService {
    private final DataStore dataStore = DataStore.getInstance();

    public Patient registerPatient(String username, String password, String fullName, String email, String phone,
                                   int age, String gender, String bloodGroup, String address, String emergencyContact, String patientType, String insuranceProvider, boolean isEmergency, boolean isMLC) {
        
        int patientCount = 0;
        for (User u : dataStore.getUsers()) {
            if (u instanceof Patient) patientCount++;
        }
        
        String prefix = isEmergency ? "EMG-2026-" : "PARAS-2026-";
        String id = prefix + String.format("%04d", patientCount + 1);
        Patient patient = new Patient(id, username, password, fullName, email, phone, age, gender, bloodGroup, address, emergencyContact, patientType, insuranceProvider);
        patient.setMLC(isMLC);
        dataStore.getUsers().add(patient);
        dataStore.saveAllData();
        dataStore.addLog("SYSTEM", "STAFF", "REGISTER_PATIENT", "Registered new patient: " + fullName + " (" + id + ")");
        return patient;
    }

    public List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        for (User u : dataStore.getUsers()) {
            if (u instanceof Patient) {
                list.add((Patient) u);
            }
        }
        return list;
    }

    public VitalSign recordVitals(String patientId, String patientName, String nurseName,
                                  String bp, int heartRate, double temp, int respRate, String notes) {
        String id = "VIT-" + (dataStore.getVitals().size() + 101);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm a"));
        VitalSign vitals = new VitalSign(id, patientId, patientName, nurseName, timestamp, bp, heartRate, temp, respRate, notes);
        dataStore.getVitals().add(0, vitals);
        dataStore.saveAllData();
        dataStore.addLog(nurseName, "NURSE", "RECORD_VITALS", "Recorded vitals for patient: " + patientName);
        return vitals;
    }

    public List<VitalSign> getVitalsForPatient(String patientId) {
        List<VitalSign> list = new ArrayList<>();
        for (VitalSign v : dataStore.getVitals()) {
            if (v.getPatientId().equals(patientId)) {
                list.add(v);
            }
        }
        return list;
    }
}
