package com.hospital.service;

import com.hospital.model.Medicine;
import com.hospital.model.Patient;
import com.hospital.model.Prescription;
import com.hospital.model.User;
import com.hospital.repository.DataStore;

import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private final DataStore dataStore = DataStore.getInstance();
    private final BillingService billingService = new BillingService();

    public List<Medicine> getAllMedicines() {
        return new ArrayList<>(dataStore.getMedicines());
    }

    public void addOrUpdateMedicine(Medicine medicine) {
        boolean found = false;
        for (int i = 0; i < dataStore.getMedicines().size(); i++) {
            if (dataStore.getMedicines().get(i).getId().equals(medicine.getId())) {
                dataStore.getMedicines().set(i, medicine);
                found = true;
                break;
            }
        }
        if (!found) {
            dataStore.getMedicines().add(medicine);
        }
        dataStore.saveAllData();
    }

    public List<Prescription> getPendingPrescriptions() {
        List<Prescription> pending = new ArrayList<>();
        for (Prescription rx : dataStore.getPrescriptions()) {
            if ("PENDING".equalsIgnoreCase(rx.getStatus())) {
                pending.add(rx);
            }
        }
        return pending;
    }

    public synchronized boolean dispensePrescription(String prescriptionId, String pharmacistName) {
        Prescription target = null;
        for (Prescription rx : dataStore.getPrescriptions()) {
            if (rx.getId().equals(prescriptionId)) {
                target = rx;
                break;
            }
        }

        if (target == null || "DISPENSED".equalsIgnoreCase(target.getStatus())) {
            return false;
        }

        // Deduct inventory stock and calculate cost
        double totalPharmacyCost = 0.0;
        for (Prescription.PrescriptionItem item : target.getItems()) {
            for (Medicine med : dataStore.getMedicines()) {
                if (med.getName().equalsIgnoreCase(item.getMedicineName())) {
                    int newStock = Math.max(0, med.getStockQuantity() - item.getQuantity());
                    med.setStockQuantity(newStock);
                    totalPharmacyCost += (med.getUnitPrice() * item.getQuantity());
                }
            }
        }

        // Auto-billing
        Patient patient = null;
        for (User u : dataStore.getUsers()) {
            if (u instanceof Patient && u.getId().equals(target.getPatientId())) {
                patient = (Patient) u;
                break;
            }
        }

        if (patient != null && totalPharmacyCost > 0) {
            billingService.addChargeToPatientInvoice(patient, "Pharmacy Prescribed Medicines (Rx: " + target.getId() + ")", totalPharmacyCost, pharmacistName);
        }

        target.setStatus("DISPENSED");
        dataStore.saveAllData();
        dataStore.addLog(pharmacistName, "PHARMACIST", "DISPENSE_MEDICINE", "Dispensed prescription " + prescriptionId + " for patient " + target.getPatientName());
        return true;
    }
}
