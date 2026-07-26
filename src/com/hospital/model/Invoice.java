package com.hospital.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String patientId;
    private String patientName;
    private String issueDate;
    private List<InvoiceItem> items;
    private double totalAmount;
    private String paymentStatus; // "UNPAID", "PAID"
    private String paymentMethod; // "CASH", "CARD", "INSURANCE"

    public Invoice(String id, String patientId, String patientName, String issueDate) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.issueDate = issueDate;
        this.items = new ArrayList<>();
        this.totalAmount = 0.0;
        this.paymentStatus = "UNPAID";
        this.paymentMethod = "N/A";
    }

    public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public String getIssueDate() { return issueDate; }
    public List<InvoiceItem> getItems() { return items; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public void addItem(String description, double amount) {
        this.items.add(new InvoiceItem(description, amount));
        this.totalAmount += amount;
    }
}
