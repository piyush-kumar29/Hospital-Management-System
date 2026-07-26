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

        JButton btnGenerate = UIUtils.createStyledButton("+ Generate Patient Invoice", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnPay = UIUtils.createStyledButton("Process Payment", UIUtils.COLOR_SUCCESS, Color.WHITE);
        JButton btnView = UIUtils.createStyledButton("View Itemized Invoice", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnGenerate.addActionListener(e -> openGenerateInvoiceDialog());
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

    private void openGenerateInvoiceDialog() {
        List<Patient> patients = patientService.getAllPatients();
        if (patients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No registered patients found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generate Patient Invoice", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] patientNames = patients.stream().map(p -> p.getFullName() + " (" + p.getId() + ")").toArray(String[]::new);
        JComboBox<String> cbPatient = UIUtils.createStyledComboBox(patientNames);

        JTextField txtConsult = UIUtils.createStyledTextField(15); txtConsult.setText("150.00");
        JTextField txtWard = UIUtils.createStyledTextField(15); txtWard.setText("200.00");
        JTextField txtPharma = UIUtils.createStyledTextField(15); txtPharma.setText("45.00");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Select Patient:"), gbc);
        gbc.gridx = 1; dialog.add(cbPatient, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Consultation Charges ($):"), gbc);
        gbc.gridx = 1; dialog.add(txtConsult, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Ward / Bed Stay ($):"), gbc);
        gbc.gridx = 1; dialog.add(txtWard, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Pharmacy / Lab ($):"), gbc);
        gbc.gridx = 1; dialog.add(txtPharma, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnSubmit = UIUtils.createStyledButton("Generate Invoice", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnSubmit.addActionListener(e -> {
            try {
                Patient p = patients.get(cbPatient.getSelectedIndex());
                double cFee = Double.parseDouble(txtConsult.getText().trim());
                double wFee = Double.parseDouble(txtWard.getText().trim());
                double pFee = Double.parseDouble(txtPharma.getText().trim());

                Invoice inv = billingService.generateInvoiceForPatient(p, cFee, wFee, pFee, staff.getFullName());
                refreshTables();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Invoice " + inv.getId() + " generated for total $" + String.format("%.2f", inv.getTotalAmount()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid charges entered!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(btnSubmit, gbc);
        dialog.setVisible(true);
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
            refreshTables();
            JOptionPane.showMessageDialog(this, "Payment of " + invoiceModel.getValueAt(sel, 3) + " processed via " + method + "!");
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
