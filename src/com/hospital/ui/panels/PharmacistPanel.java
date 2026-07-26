package com.hospital.ui.panels;

import com.hospital.model.Medicine;
import com.hospital.model.Pharmacist;
import com.hospital.model.Prescription;
import com.hospital.service.InventoryService;
import com.hospital.ui.components.StatCard;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PharmacistPanel extends JPanel {
    private final Pharmacist pharmacist;
    private final InventoryService inventoryService = new InventoryService();

    private JTable rxTable;
    private DefaultTableModel rxModel;

    private JTable stockTable;
    private DefaultTableModel stockModel;

    private JTable returnTable;
    private DefaultTableModel returnModel;

    public PharmacistPanel(Pharmacist pharmacist) {
        this.pharmacist = pharmacist;
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

        List<Prescription> pendingRx = inventoryService.getPendingPrescriptions();
        List<Medicine> medicines = inventoryService.getAllMedicines();
        long lowStockCount = medicines.stream().filter(m -> m.getStockQuantity() < 20).count();

        metricsPanel.add(new StatCard("Pending Prescriptions", String.valueOf(pendingRx.size()), UIUtils.COLOR_WARNING));
        metricsPanel.add(new StatCard("Total Inventory Items", String.valueOf(medicines.size()), UIUtils.COLOR_PRIMARY));
        metricsPanel.add(new StatCard("Low Stock Alerts (< 20)", String.valueOf(lowStockCount), UIUtils.COLOR_DANGER));
        metricsPanel.add(new StatCard("Pharmacy Status", "Operational", UIUtils.COLOR_SUCCESS));

        add(metricsPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);

        tabbedPane.addTab("Prescription Dispensing Queue", createDispensingTab());
        tabbedPane.addTab("Medicine Inventory Stock", createInventoryTab());
        tabbedPane.addTab("Returns & Refunds (IPD)", createReturnsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createDispensingTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnDispense = UIUtils.createStyledButton("Fulfill & Dispense Prescription", UIUtils.COLOR_SUCCESS, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh Queue", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnDispense.addActionListener(e -> dispensePrescriptionAction());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnDispense);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Rx ID", "Date", "Patient Name", "Doctor Name", "Prescribed Items", "Status"};
        rxModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        rxTable = new JTable(rxModel);
        UIUtils.styleTable(rxTable);

        rxTable.getColumnModel().getColumn(5).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        panel.add(new JScrollPane(rxTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnAddMed = UIUtils.createStyledButton("+ Add Medicine / Restock", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh Stock", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnAddMed.addActionListener(e -> openAddMedicineDialog());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnAddMed);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Medicine ID", "Name", "Category", "Stock Qty", "Unit Price ($)", "Expiry Date"};
        stockModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        stockTable = new JTable(stockModel);
        UIUtils.styleTable(stockTable);

        panel.add(new JScrollPane(stockTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReturnsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnReturn = UIUtils.createStyledButton("Process Unused Drug Return", UIUtils.COLOR_WARNING, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnReturn.addActionListener(e -> processReturnAction());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnReturn);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Rx ID", "Patient Name", "Status", "Items"};
        returnModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        returnTable = new JTable(returnModel);
        UIUtils.styleTable(returnTable);

        panel.add(new JScrollPane(returnTable), BorderLayout.CENTER);
        return panel;
    }

    private void dispensePrescriptionAction() {
        int sel = rxTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a pending prescription from the queue.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rxId = (String) rxModel.getValueAt(sel, 0);
        boolean success = inventoryService.dispensePrescription(rxId, pharmacist.getFullName());
        if (success) {
            refreshTables();
            JOptionPane.showMessageDialog(this, "Prescription " + rxId + " dispensed successfully! Inventory updated.");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to dispense. Prescription may already be fulfilled.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddMedicineDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add / Restock Medicine", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = UIUtils.createStyledTextField(15);
        JTextField txtCat = UIUtils.createStyledTextField(15);
        JTextField txtQty = UIUtils.createStyledTextField(15);
        JTextField txtPrice = UIUtils.createStyledTextField(15);
        JTextField txtExpiry = UIUtils.createStyledTextField(15); txtExpiry.setText("2028-12-31");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Medicine Name:"), gbc);
        gbc.gridx = 1; dialog.add(txtName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1; dialog.add(txtCat, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Stock Quantity:"), gbc);
        gbc.gridx = 1; dialog.add(txtQty, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Unit Price ($):"), gbc);
        gbc.gridx = 1; dialog.add(txtPrice, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Expiry Date:"), gbc);
        gbc.gridx = 1; dialog.add(txtExpiry, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnSubmit = UIUtils.createStyledButton("Save to Inventory", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnSubmit.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String cat = txtCat.getText().trim();
                int qty = Integer.parseInt(txtQty.getText().trim());
                double price = Double.parseDouble(txtPrice.getText().trim());
                String exp = txtExpiry.getText().trim();

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter medicine name!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String medId = "MED-" + (inventoryService.getAllMedicines().size() + 101);
                Medicine med = new Medicine(medId, name, cat, qty, price, exp);
                inventoryService.addOrUpdateMedicine(med);

                refreshTables();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Inventory Stock Updated!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid numbers entered for Stock or Price!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(btnSubmit, gbc);
        dialog.setVisible(true);
    }

    private void processReturnAction() {
        int sel = returnTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a dispensed prescription to process return.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rxId = (String) returnModel.getValueAt(sel, 0);
        String status = (String) returnModel.getValueAt(sel, 2);

        if (!"DISPENSED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only dispensed prescriptions can have items returned.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        com.hospital.model.Prescription rx = com.hospital.repository.DataStore.getInstance().getPrescriptions().stream().filter(p -> p.getId().equals(rxId)).findFirst().orElse(null);
        if (rx == null) return;

        String[] items = rx.getItems().stream().map(i -> i.getMedicineName() + " (Qty: " + i.getQuantity() + ")").toArray(String[]::new);
        String itemStr = (String) JOptionPane.showInputDialog(this, "Select Item to Return:", "Process Return", JOptionPane.QUESTION_MESSAGE, null, items, items[0]);

        if (itemStr != null) {
            String medName = itemStr.split("\\(")[0].trim();
            com.hospital.model.Prescription.PrescriptionItem targetItem = rx.getItems().stream().filter(i -> i.getMedicineName().equals(medName)).findFirst().orElse(null);
            
            if (targetItem != null) {
                String qtyStr = JOptionPane.showInputDialog(this, "Enter Quantity to Return (Max " + targetItem.getQuantity() + "):", "1");
                try {
                    int qty = Integer.parseInt(qtyStr.trim());
                    if (qty <= 0 || qty > targetItem.getQuantity()) {
                        JOptionPane.showMessageDialog(this, "Invalid quantity.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    com.hospital.service.BillingService billingService = new com.hospital.service.BillingService();
                    com.hospital.model.Patient patient = (com.hospital.model.Patient) com.hospital.repository.DataStore.getInstance().getUsers().stream().filter(u -> u.getId().equals(rx.getPatientId())).findFirst().orElse(null);
                    
                    com.hospital.model.Medicine med = inventoryService.getAllMedicines().stream().filter(m -> m.getName().equalsIgnoreCase(medName)).findFirst().orElse(null);
                    if (med != null && patient != null) {
                        double refundAmount = qty * med.getUnitPrice();
                        
                        // Increment stock
                        med.setStockQuantity(med.getStockQuantity() + qty);
                        inventoryService.addOrUpdateMedicine(med); // Saves DataStore
                        
                        // Add negative charge to invoice
                        billingService.addChargeToPatientInvoice(patient, "Refund: Unused " + medName + " (Qty: " + qty + ")", -refundAmount, pharmacist.getFullName());
                        
                        JOptionPane.showMessageDialog(this, "Processed return for " + qty + " of " + medName + ". Refunded $" + refundAmount);
                        refreshTables();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public void refreshTables() {
        rxModel.setRowCount(0);
        for (Prescription rx : inventoryService.getPendingPrescriptions()) {
            StringBuilder items = new StringBuilder();
            for (Prescription.PrescriptionItem item : rx.getItems()) {
                items.append(item.getMedicineName()).append(" (x").append(item.getQuantity()).append("); ");
            }
            rxModel.addRow(new Object[]{rx.getId(), rx.getDate(), rx.getPatientName(), rx.getDoctorName(), items.toString(), rx.getStatus()});
        }

        stockModel.setRowCount(0);
        for (Medicine m : inventoryService.getAllMedicines()) {
            stockModel.addRow(new Object[]{m.getId(), m.getName(), m.getCategory(), m.getStockQuantity(), String.format("%.2f", m.getUnitPrice()), m.getExpiryDate()});
        }

        if (returnModel != null) {
            returnModel.setRowCount(0);
            for (com.hospital.model.Prescription rx : com.hospital.repository.DataStore.getInstance().getPrescriptions()) {
                if ("DISPENSED".equals(rx.getStatus())) {
                    StringBuilder items = new StringBuilder();
                    for (com.hospital.model.Prescription.PrescriptionItem item : rx.getItems()) {
                        items.append(item.getMedicineName()).append(" (x").append(item.getQuantity()).append("); ");
                    }
                    returnModel.addRow(new Object[]{rx.getId(), rx.getPatientName(), rx.getStatus(), items.toString()});
                }
            }
        }
    }
}
