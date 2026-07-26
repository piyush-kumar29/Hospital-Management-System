package com.hospital.service;

import com.hospital.model.*;
import com.hospital.repository.DataStore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillingService {
    private final DataStore dataStore = DataStore.getInstance();

    public List<Invoice> getAllInvoices() {
        return new ArrayList<>(dataStore.getInvoices());
    }

    public List<Invoice> getInvoicesByPatient(String patientId) {
        List<Invoice> result = new ArrayList<>();
        for (Invoice inv : dataStore.getInvoices()) {
            if (inv.getPatientId().equals(patientId)) {
                result.add(inv);
            }
        }
        return result;
    }

    public Invoice generateInvoiceForPatient(Patient patient, double consultationFee, double wardCharges, double pharmacyCharges, String createdBy) {
        String id = "INV-" + (dataStore.getInvoices().size() + 101);
        String today = LocalDate.now().toString();

        Invoice invoice = new Invoice(id, patient.getId(), patient.getFullName(), today);
        if (consultationFee > 0) {
            invoice.addItem("Doctor Consultation Fee", consultationFee);
        }
        if (wardCharges > 0) {
            invoice.addItem("Ward Stay / IPD Facility Charges", wardCharges);
        }
        if (pharmacyCharges > 0) {
            invoice.addItem("Pharmacy Prescribed Medicines", pharmacyCharges);
        }

        dataStore.getInvoices().add(invoice);
        dataStore.saveAllData();
        dataStore.addLog(createdBy, "BILLING", "GENERATE_INVOICE", "Generated invoice " + id + " for $" + invoice.getTotalAmount() + " (Patient: " + patient.getFullName() + ")");
        return invoice;
    }

    public boolean recordPayment(String invoiceId, String paymentMethod, String processedBy) {
        for (Invoice inv : dataStore.getInvoices()) {
            if (inv.getId().equals(invoiceId)) {
                inv.setPaymentStatus("PAID");
                inv.setPaymentMethod(paymentMethod);
                dataStore.saveAllData();
                dataStore.addLog(processedBy, "BILLING", "RECORD_PAYMENT", "Recorded payment for invoice " + invoiceId + " via " + paymentMethod);
                return true;
            }
        }
        return false;
    }
}
