package com.hospital.repository;

import com.hospital.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static final String DATA_DIR = "data";
    private static DataStore instance;

    private List<User> users;
    private List<Appointment> appointments;
    private List<Prescription> prescriptions;
    private List<Medicine> medicines;
    private List<VitalSign> vitals;
    private List<BedAllocation> beds;
    private List<Invoice> invoices;
    private List<SystemLog> logs;

    private DataStore() {
        ensureDataDir();
        loadAllData();
        if (users.isEmpty()) {
            seedDefaultData();
        }
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private void ensureDataDir() {
        Path path = Paths.get(DATA_DIR);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> loadList(String filename) {
        File file = new File(DATA_DIR, filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<T>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Could not load " + filename + ", initializing empty list: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private <T> void saveList(String filename, List<T> list) {
        File file = new File(DATA_DIR, filename);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void loadAllData() {
        users = loadList("users.dat");
        appointments = loadList("appointments.dat");
        prescriptions = loadList("prescriptions.dat");
        medicines = loadList("medicines.dat");
        vitals = loadList("vitals.dat");
        beds = loadList("beds.dat");
        invoices = loadList("invoices.dat");
        logs = loadList("logs.dat");
    }

    public synchronized void saveAllData() {
        saveList("users.dat", users);
        saveList("appointments.dat", appointments);
        saveList("prescriptions.dat", prescriptions);
        saveList("medicines.dat", medicines);
        saveList("vitals.dat", vitals);
        saveList("beds.dat", beds);
        saveList("invoices.dat", invoices);
        saveList("logs.dat", logs);
    }

    private void seedDefaultData() {
        System.out.println("Seeding initial default dataset...");

        // 1. Users for all 7 roles
        Admin admin = new Admin("USR-101", "admin", "admin123", "Dr. Sarah Jenkins", "admin@hospital.com", "+1-555-0101");
        Receptionist receptionist = new Receptionist("USR-102", "reception", "recep123", "Emily Watson", "reception@hospital.com", "+1-555-0102");
        
        Doctor doc1 = new Doctor("USR-103", "doctor", "doc123", "Dr. Robert Vance", "vance@hospital.com", "+1-555-0103", "Cardiology", "MD, FACC", 150.0);
        Doctor doc2 = new Doctor("USR-104", "dr_smith", "doc123", "Dr. Alice Smith", "smith@hospital.com", "+1-555-0104", "Neurology", "MD, PhD", 180.0);
        Doctor doc3 = new Doctor("USR-105", "dr_patel", "doc123", "Dr. Rajesh Patel", "patel@hospital.com", "+1-555-0105", "Pediatrics", "MD, DCH", 120.0);

        Nurse nurse = new Nurse("USR-106", "nurse", "nurse123", "Clara Oswald", "nurse@hospital.com", "+1-555-0106", "Ward A - ICU");
        Pharmacist pharmacist = new Pharmacist("USR-107", "pharma", "pharma123", "Marcus Vance", "pharma@hospital.com", "+1-555-0107");
        BillingStaff billing = new BillingStaff("USR-108", "billing", "bill123", "Helen Mirren", "billing@hospital.com", "+1-555-0108");

        Patient patient1 = new Patient("USR-109", "patient", "patient123", "John Doe", "john@example.com", "+1-555-0201", 38, "Male", "O+", "742 Evergreen Terrace", "Jane Doe (+1-555-0200)", "IPD");
        Patient patient2 = new Patient("USR-110", "patient2", "patient123", "Mary Johnson", "mary@example.com", "+1-555-0202", 45, "Female", "A+", "123 Main St", "Tom Johnson (+1-555-0209)", "OPD");
        Patient patient3 = new Patient("USR-111", "patient3", "patient123", "David Miller", "david@example.com", "+1-555-0203", 62, "Male", "B-", "456 Oak Ave", "Sarah Miller (+1-555-0210)", "IPD");

        users.add(admin);
        users.add(receptionist);
        users.add(doc1);
        users.add(doc2);
        users.add(doc3);
        users.add(nurse);
        users.add(pharmacist);
        users.add(billing);
        users.add(patient1);
        users.add(patient2);
        users.add(patient3);

        // 2. Pharmacy Stock Medicines
        medicines.add(new Medicine("MED-101", "Amoxicillin 500mg", "Antibiotic", 150, 15.50, "2027-12-31"));
        medicines.add(new Medicine("MED-102", "Paracetamol 650mg", "Analgesic", 500, 5.00, "2028-06-30"));
        medicines.add(new Medicine("MED-103", "Atorvastatin 20mg", "Cardiovascular", 80, 28.00, "2027-09-15"));
        medicines.add(new Medicine("MED-104", "Metformin 500mg", "Antidiabetic", 200, 12.00, "2027-11-20"));
        medicines.add(new Medicine("MED-105", "Ibuprofen 400mg", "NSAID", 18, 8.50, "2026-10-15")); // Low stock demo

        // 3. Bed Allocations
        beds.add(new BedAllocation("BED-101", "A-101", "Ward A (General)", "USR-109", "John Doe", "2026-07-20", "OCCUPIED", 100.0));
        beds.add(new BedAllocation("BED-102", "A-102", "Ward A (General)", "", "", "", "AVAILABLE", 100.0));
        beds.add(new BedAllocation("BED-103", "ICU-01", "ICU Ward", "USR-111", "David Miller", "2026-07-24", "OCCUPIED", 350.0));
        beds.add(new BedAllocation("BED-104", "ICU-02", "ICU Ward", "", "", "", "AVAILABLE", 350.0));
        beds.add(new BedAllocation("BED-105", "B-201", "Ward B (Private)", "", "", "", "AVAILABLE", 200.0));

        // 4. Appointments
        Appointment app1 = new Appointment("APT-101", "USR-109", "John Doe", "USR-103", "Dr. Robert Vance", "2026-07-27", "09:00 AM - 10:00 AM", "Chest pain & shortness of breath");
        app1.setStatus(AppointmentStatus.CONFIRMED);

        Appointment app2 = new Appointment("APT-102", "USR-110", "Mary Johnson", "USR-104", "Dr. Alice Smith", "2026-07-27", "10:00 AM - 11:00 AM", "Migraine headaches");
        app2.setStatus(AppointmentStatus.REQUESTED);

        Appointment app3 = new Appointment("APT-103", "USR-111", "David Miller", "USR-103", "Dr. Robert Vance", "2026-07-25", "02:00 PM - 03:00 PM", "Hypertension follow up");
        app3.setStatus(AppointmentStatus.COMPLETED);
        app3.setDiagnosis("Essential Hypertension Stage 1. Controlled.");
        app3.setPrescriptionNotes("Prescribed Atorvastatin and Paracetamol as needed.");

        appointments.add(app1);
        appointments.add(app2);
        appointments.add(app3);

        // 5. Prescriptions
        Prescription rx1 = new Prescription("RX-101", "APT-103", "USR-111", "David Miller", "USR-103", "Dr. Robert Vance", "2026-07-25", "Take medicines strictly after food.");
        rx1.addItem("Atorvastatin 20mg", "1 tablet daily at night", "30 Days", 30);
        rx1.addItem("Paracetamol 650mg", "1 tablet as needed for headache", "5 Days", 10);
        rx1.setStatus("PENDING");
        prescriptions.add(rx1);

        // 6. Vitals
        vitals.add(new VitalSign("VIT-101", "USR-109", "John Doe", "Clara Oswald", "2026-07-26 08:30 AM", "128/82 mmHg", 76, 98.6, 16, "Patient resting comfortably. BP stable."));
        vitals.add(new VitalSign("VIT-102", "USR-111", "David Miller", "Clara Oswald", "2026-07-26 09:00 AM", "142/90 mmHg", 84, 99.1, 18, "Slight hypertension observed. Doctor notified."));

        // 7. Invoices
        Invoice inv1 = new Invoice("INV-101", "USR-111", "David Miller", "2026-07-25");
        inv1.addItem("Doctor Consultation Fee (Dr. Robert Vance)", 150.0);
        inv1.addItem("ICU Ward Stay (2 Days @ $350/day)", 700.0);
        inv1.addItem("Pharmacy Medicines (Rx-101)", 45.0);
        inv1.setPaymentStatus("PAID");
        inv1.setPaymentMethod("CARD");
        invoices.add(inv1);

        // 8. System Audit Logs
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logs.add(new SystemLog(now, "SYSTEM", "SYSTEM", "DATA_SEED", "Initial seed dataset created successfully."));

        saveAllData();
    }

    // Getters and Mutators for Datastore
    public synchronized List<User> getUsers() { return users; }
    public synchronized List<Appointment> getAppointments() { return appointments; }
    public synchronized List<Prescription> getPrescriptions() { return prescriptions; }
    public synchronized List<Medicine> getMedicines() { return medicines; }
    public synchronized List<VitalSign> getVitals() { return vitals; }
    public synchronized List<BedAllocation> getBeds() { return beds; }
    public synchronized List<Invoice> getInvoices() { return invoices; }
    public synchronized List<SystemLog> getLogs() { return logs; }

    public synchronized void addLog(String username, String role, String action, String details) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        logs.add(0, new SystemLog(now, username, role, action, details));
        saveList("logs.dat", logs);
    }
}
