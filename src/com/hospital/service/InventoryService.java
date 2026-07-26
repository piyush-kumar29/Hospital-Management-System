package com.hospital.service;

import com.hospital.model.Medicine;
import com.hospital.model.Prescription;
import com.hospital.repository.DataStore;

import java.util.ArrayList;
import java.util.List;

public class InventoryService {
    private final DataStore dataStore = DataStore.getInstance();

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

        // Deduct inventory stock
        for (Prescription.PrescriptionItem item : target.getItems()) {
            for (Medicine med : dataStore.getMedicines()) {
                if (med.getName().equalsIgnoreCase(item.getMedicineName())) {
                    int newStock = Math.max(0, med.getStockQuantity() - item.getQuantity());
                    med.setStockQuantity(newStock);
                }
            }
        }

        target.setStatus("DISPENSED");
        dataStore.saveAllData();
        dataStore.addLog(pharmacistName, "PHARMACIST", "DISPENSE_MEDICINE", "Dispensed prescription " + prescriptionId + " for patient " + target.getPatientName());
        return true;
    }
}
