package com.hospital.model;

import java.io.Serializable;

public class InvoiceItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String description;
    private double amount;

    public InvoiceItem(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() { return description; }
    public double getAmount() { return amount; }
}
