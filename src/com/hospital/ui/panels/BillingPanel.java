package com.hospital.ui.panels;

import com.hospital.model.BillingStaff;
import com.hospital.model.Invoice;
import com.hospital.model.InvoiceItem;
import com.hospital.model.Patient;
import com.hospital.service.BillingService;
import com.hospital.service.PatientService;
import com.hospital.ui.components.StatCard;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class BillingPanel extends JPanel {
    private final BillingStaff staff;
    private final BillingService billingService = new BillingService();
    private final PatientService patientService = new PatientService();

    private JTable invoiceTable;
    private DefaultTableModel invoiceModel;

    public BillingPanel(BillingStaff staff) {
        this.staff = staff;
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        refreshTables();
    }

    private void initUI() {
        // Metrics
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsPanel.setOpaque(false);

        List<Invoice> invoices = billingService.getAllInvoices();
        double totalRevenue = invoices.stream().filter(i -> "PAID".equalsIgnoreCase(i.getPaymentStatus())).mapToDouble(Invoice::getTotalAmount).sum();
        long unpaidCount = invoices.stream().filter(i -> "UNPAID".equalsIgnoreCase(i.getPaymentStatus())).count();

        metricsPanel.add(new StatCard("Total Invoices Issued", String.valueOf(invoices.size()), UIUtils.COLOR_PRIMARY));
        metricsPanel.add(new StatCard("Total Revenue Collected", String.format("$%.2f", totalRevenue), UIUtils.COLOR_SUCCESS));
        metricsPanel.add(new StatCard("Pending Unpaid Bills", String.valueOf(unpaidCount), UIUtils.COLOR_DANGER));
        metricsPanel.add(new StatCard("Accounts Desk", "Active", UIUtils.COLOR_INFO));

        add(metricsPanel, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(12, 12));
        mainContent.setBackground(UIUtils.COLOR_BG);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnGenerate = UIUtils.createStyledButton("+ Calculate IPD Discharge Bill", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnPay = UIUtils.createStyledButton("Process Payment", UIUtils.COLOR_SUCCESS, Color.WHITE);
        JButton btnView = UIUtils.createStyledButton("View Itemized Invoice", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnGenerate.addActionListener(e -> calculateIPDDischargeBill());
        btnPay.addActionListener(e -> processPaymentDialog());
        btnView.addActionListener(e -> viewItemizedInvoiceDialog());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnGenerate);
        toolbar.add(btnPay);
        toolbar.add(btnView);
        toolbar.add(btnRefresh);

        mainContent.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Invoice ID", "Date", "Patient Name", "Total Amount ($)", "Payment Status", "Method"};
        invoiceModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        invoiceTable = new JTable(invoiceModel);
        UIUtils.styleTable(invoiceTable);

        invoiceTable.getColumnModel().getColumn(4).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        mainContent.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    private void calculateIPDDischargeBill() {
        java.util.List<com.hospital.model.BedAllocation> occupiedBeds = new java.util.ArrayList<>();
        for (com.hospital.model.BedAllocation b : com.hospital.repository.DataStore.getInstance().getBeds()) {
            if ("OCCUPIED".equalsIgnoreCase(b.getStatus())) {
                Patient p = patientService.getAllPatients().stream().filter(pat -> pat.getId().equals(b.getPatientId())).findFirst().orElse(null);
                if (p != null && p.isClinicalDischarge()) {
                    occupiedBeds.add(b);
                }
            }
        }
        
        if (occupiedBeds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No patients currently occupying beds with a Clinical Discharge status.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = occupiedBeds.stream().map(b -> b.getPatientName() + " (" + b.getBedNumber() + ")").toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(this, "Select IPD Patient to Bill for Bed Stay:", "Calculate IPD Bill", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        
        if (chosen != null) {
            com.hospital.model.BedAllocation bed = occupiedBeds.get(java.util.Arrays.asList(options).indexOf(chosen));
            
            String daysStr = JOptionPane.showInputDialog(this, "Enter number of days stayed since " + bed.getAdmissionDate() + ":", "3");
            if (daysStr != null && !daysStr.trim().isEmpty()) {
                try {
                    int days = Integer.parseInt(daysStr.trim());
                    double totalBedCharge = days * bed.getDailyRate();
                    
                    Patient p = null;
                    for (Patient pat : patientService.getAllPatients()) {
                        if (pat.getId().equals(bed.getPatientId())) { p = pat; break; }
                    }
                    
                    if (p != null) {
                        double discount = 0.0;
                        if (!"Cash".equalsIgnoreCase(p.getInsuranceProvider())) {
                            int apply = JOptionPane.showConfirmDialog(this, "Patient has " + p.getInsuranceProvider() + " insurance. Apply 50% coverage discount?", "Apply Insurance", JOptionPane.YES_NO_OPTION);
                            if (apply == JOptionPane.YES_OPTION) {
                                discount = totalBedCharge * 0.5;
                            }
                        }
                        
                        billingService.addChargeToPatientInvoice(p, "IPD Bed Stay (" + days + " days @ $" + bed.getDailyRate() + ")", totalBedCharge, staff.getFullName());
                        if (discount > 0) {
                            billingService.addChargeToPatientInvoice(p, "Insurance Coverage Discount (" + p.getInsuranceProvider() + ")", -discount, staff.getFullName());
                        }
                        
                        refreshTables();
                        JOptionPane.showMessageDialog(this, "IPD Bed Charges successfully added to patient's master bill.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number of days.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void processPaymentDialog() {
        int sel = invoiceTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select an invoice to process payment.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String invId = (String) invoiceModel.getValueAt(sel, 0);
        String status = (String) invoiceModel.getValueAt(sel, 4);

        if ("PAID".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Invoice " + invId + " is already PAID!", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] methods = {"CASH", "CREDIT_CARD", "DEBIT_CARD", "HEALTH_INSURANCE"};
        String method = (String) JOptionPane.showInputDialog(this, "Select Payment Method:", "Process Payment - " + invId, JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);

        if (method != null) {
            billingService.recordPayment(invId, method, staff.getFullName());
            
            // Generate Gate Pass
            Invoice invoice = billingService.getAllInvoices().stream().filter(i -> i.getId().equals(invId)).findFirst().orElse(null);
            if (invoice != null) {
                Patient patient = patientService.getAllPatients().stream().filter(p -> p.getId().equals(invoice.getPatientId())).findFirst().orElse(null);
                if (patient != null) {
                    patient.setFinancialDischarge(true);
                    String gp = "GP-" + (int)(Math.random() * 100000);
                    patient.setGatePassId(gp);
                    com.hospital.repository.DataStore.getInstance().saveAllData();
                    JOptionPane.showMessageDialog(this, "Payment of " + invoiceModel.getValueAt(sel, 3) + " processed via " + method + "!\n\nSecurity Gate Pass Generated: " + gp);
                }
            }
            refreshTables();
        }
    }

    private void viewItemizedInvoiceDialog() {
        int sel = invoiceTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select an invoice to view details.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String invId = (String) invoiceModel.getValueAt(sel, 0);
        Invoice invoice = null;
        for (Invoice inv : billingService.getAllInvoices()) {
            if (inv.getId().equals(invId)) {
                invoice = inv;
                break;
            }
        }

        if (invoice == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("         HOSPITAL ITEMIZED INVOICE        \n");
        sb.append("==========================================\n");
        sb.append("Invoice ID   : ").append(invoice.getId()).append("\n");
        sb.append("Patient Name : ").append(invoice.getPatientName()).append("\n");
        sb.append("Issue Date   : ").append(invoice.getIssueDate()).append("\n");
        sb.append("Payment Status: ").append(invoice.getPaymentStatus()).append("\n");
        sb.append("Payment Method: ").append(invoice.getPaymentMethod()).append("\n");
        sb.append("------------------------------------------\n");
        sb.append("Line Items:\n");
        for (InvoiceItem item : invoice.getItems()) {
            sb.append(String.format(" - %-32s : $%8.2f\n", item.getDescription(), item.getAmount()));
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format("TOTAL AMOUNT DUE:                     $%8.2f\n", invoice.getTotalAmount()));
        sb.append("==========================================\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Itemized Receipt Preview - " + invId, JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshTables() {
        invoiceModel.setRowCount(0);
        for (Invoice i : billingService.getAllInvoices()) {
            invoiceModel.addRow(new Object[]{i.getId(), i.getIssueDate(), i.getPatientName(), String.format("%.2f", i.getTotalAmount()), i.getPaymentStatus(), i.getPaymentMethod()});
        }
    }
}
