package com.hospital.service;

import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.repository.DataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentService {
    private final DataStore dataStore = DataStore.getInstance();

    public boolean isSlotAvailable(String doctorId, String date, String timeSlot) {
        for (Appointment app : dataStore.getAppointments()) {
            if (app.getDoctorId().equals(doctorId) &&
                app.getAppointmentDate().equals(date) &&
                app.getTimeSlot().equalsIgnoreCase(timeSlot) &&
                (app.getStatus() == AppointmentStatus.REQUESTED || app.getStatus() == AppointmentStatus.CONFIRMED)) {
                return false; // Slot already booked
            }
        }
        return true;
    }

    public Appointment bookAppointment(String patientId, String patientName, String doctorId, String doctorName,
                                       String date, String timeSlot, String symptoms) throws IllegalStateException {
        if (!isSlotAvailable(doctorId, date, timeSlot)) {
            throw new IllegalStateException("Selected time slot (" + timeSlot + ") is already booked for this doctor!");
        }

        String appNo = "APT-" + (dataStore.getAppointments().size() + 101);
        Appointment newApp = new Appointment(appNo, patientId, patientName, doctorId, doctorName, date, timeSlot, symptoms);
        dataStore.getAppointments().add(newApp);
        dataStore.saveAllData();
        dataStore.addLog(patientName, "PATIENT", "BOOK_APPOINTMENT", "Booked appointment " + appNo + " with " + doctorName + " on " + date + " " + timeSlot);
        return newApp;
    }

    public void updateAppointmentStatus(String appointmentId, AppointmentStatus newStatus, String updatedBy) {
        for (Appointment app : dataStore.getAppointments()) {
            if (app.getId().equals(appointmentId)) {
                app.setStatus(newStatus);
                dataStore.saveAllData();
                dataStore.addLog(updatedBy, "STAFF", "UPDATE_APPOINTMENT", "Updated appointment " + appointmentId + " status to " + newStatus);
                break;
            }
        }
    }

    public List<Appointment> getAppointmentsByDoctor(String doctorId) {
        return dataStore.getAppointments().stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAppointmentsByPatient(String patientId) {
        return dataStore.getAppointments().stream()
                .filter(a -> a.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    public List<Appointment> getAllAppointments() {
        return new ArrayList<>(dataStore.getAppointments());
    }
}
