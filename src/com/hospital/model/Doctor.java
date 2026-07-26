package com.hospital.model;

import java.util.ArrayList;
import java.util.List;

public class Doctor extends User {
    private static final long serialVersionUID = 1L;

    private String specialty;
    private String qualification;
    private double consultationFee;
    private List<String> availableDays;
    private List<String> availableSlots;

    public Doctor(String id, String username, String password, String fullName, String email, String phone,
                  String specialty, String qualification, double consultationFee) {
        super(id, username, password, fullName, email, phone, Role.DOCTOR);
        this.specialty = specialty;
        this.qualification = qualification;
        this.consultationFee = consultationFee;
        this.availableDays = new ArrayList<>();
        this.availableSlots = new ArrayList<>();
        // Default availability
        this.availableDays.add("Monday");
        this.availableDays.add("Tuesday");
        this.availableDays.add("Wednesday");
        this.availableDays.add("Thursday");
        this.availableDays.add("Friday");

        this.availableSlots.add("09:00 AM - 10:00 AM");
        this.availableSlots.add("10:00 AM - 11:00 AM");
        this.availableSlots.add("11:00 AM - 12:00 PM");
        this.availableSlots.add("02:00 PM - 03:00 PM");
        this.availableSlots.add("03:00 PM - 04:00 PM");
        this.availableSlots.add("04:00 PM - 05:00 PM");
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public List<String> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(List<String> availableDays) {
        this.availableDays = availableDays;
    }

    public List<String> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<String> availableSlots) {
        this.availableSlots = availableSlots;
    }
}
