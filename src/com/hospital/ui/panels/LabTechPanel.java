package com.hospital.ui.panels;

import com.hospital.model.LabTech;
import com.hospital.model.LabTest;
import com.hospital.model.Patient;
import com.hospital.model.User;
import com.hospital.repository.DataStore;
import com.hospital.service.BillingService;
import com.hospital.ui.components.StatCard;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LabTechPanel extends JPanel {
    private final LabTech labTech;
    private final DataStore dataStore = DataStore.getInstance();
    private final BillingService billingService = new BillingService();

    private JTable queueTable;
    private DefaultTableModel queueModel;

    public LabTechPanel(LabTech labTech) {
        this.labTech = labTech;
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        refreshTables();
    }

    private void initUI() {
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsPanel.setOpaque(false);

        long pending = dataStore.getLabTests().stream().filter(t -> "REQUESTED".equals(t.getStatus())).count();
        long collected = dataStore.getLabTests().stream().filter(t -> "SAMPLE_COLLECTED".equals(t.getStatus())).count();
        long completed = dataStore.getLabTests().stream().filter(t -> "REPORT_GENERATED".equals(t.getStatus())).count();

        metricsPanel.add(new StatCard("Pending Tests", String.valueOf(pending), UIUtils.COLOR_WARNING));
        metricsPanel.add(new StatCard("Samples Collected", String.valueOf(collected), UIUtils.COLOR_INFO));
        metricsPanel.add(new StatCard("Reports Generated", String.valueOf(completed), UIUtils.COLOR_SUCCESS));
        metricsPanel.add(new StatCard("Department", labTech.getDepartment(), UIUtils.COLOR_PRIMARY));

        add(metricsPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(UIUtils.COLOR_BG);
        
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnCollect = UIUtils.createStyledButton("Collect Sample & Bill Patient", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnReject = UIUtils.createStyledButton("Reject Sample", UIUtils.COLOR_DANGER, Color.WHITE);
        JButton btnReport = UIUtils.createStyledButton("Enter Results & Publish Report", UIUtils.COLOR_SUCCESS, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh Queue", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnCollect.addActionListener(e -> collectSampleAction());
        btnReject.addActionListener(e -> rejectSampleAction());
        btnReport.addActionListener(e -> enterResultsAction());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnCollect);
        toolbar.add(btnReject);
        toolbar.add(btnReport);
        toolbar.add(btnRefresh);

        mainPanel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Test ID", "Patient Name", "Doctor Name", "Test Name", "Price ($)", "Status", "Date Requested"};
        queueModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        queueTable = new JTable(queueModel);
        UIUtils.styleTable(queueTable);
        queueTable.getColumnModel().getColumn(5).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        mainPanel.add(new JScrollPane(queueTable), BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }

    private void collectSampleAction() {
        int sel = queueTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a REQUESTED test to collect sample.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String testId = (String) queueModel.getValueAt(sel, 0);
        String status = (String) queueModel.getValueAt(sel, 5);
        
        if (!"REQUESTED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Only REQUESTED tests can be marked as Sample Collected.", "Invalid Action", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (LabTest t : dataStore.getLabTests()) {
            if (t.getId().equals(testId)) {
                t.setStatus("SAMPLE_COLLECTED");
                
                // Auto-bill
                Patient patient = null;
                for (User u : dataStore.getUsers()) {
                    if (u instanceof Patient && u.getId().equals(t.getPatientId())) {
                        patient = (Patient) u;
                        break;
                    }
                }
                if (patient != null && t.getPrice() > 0) {
                    billingService.addChargeToPatientInvoice(patient, "Lab Test: " + t.getTestName() + " (ID: " + testId + ")", t.getPrice(), labTech.getFullName());
                }
                
                dataStore.addLog(labTech.getFullName(), "LAB_TECH", "SAMPLE_COLLECTED", "Collected sample for test " + testId + " and billed patient.");
                break;
            }
        }
        dataStore.saveAllData();
        refreshTables();
        JOptionPane.showMessageDialog(this, "Sample Collected and Patient Billed Successfully!");
    }

    private void rejectSampleAction() {
        int sel = queueTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a test to reject.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String testId = (String) queueModel.getValueAt(sel, 0);
        String status = (String) queueModel.getValueAt(sel, 5);

        if (!"REQUESTED".equals(status) && !"SAMPLE_COLLECTED".equals(status)) {
            JOptionPane.showMessageDialog(this, "Cannot reject a test that is already completed or rejected.", "Invalid Action", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] reasons = {"Hemolyzed Sample", "Insufficient Quantity", "Improper Labeling", "Clotted Sample", "Contaminated"};
        String reason = (String) JOptionPane.showInputDialog(this, "Select Rejection Reason:", "Reject Sample", JOptionPane.QUESTION_MESSAGE, null, reasons, reasons[0]);
        
        if (reason != null) {
            for (LabTest t : dataStore.getLabTests()) {
                if (t.getId().equals(testId)) {
                    t.setStatus("SAMPLE_REJECTED");
                    t.setRejectionReason(reason);
                    dataStore.addLog(labTech.getFullName(), "LAB_TECH", "SAMPLE_REJECTED", "Rejected sample for test " + testId + " - " + reason);
                    break;
                }
            }
            dataStore.saveAllData();
            refreshTables();
            JOptionPane.showMessageDialog(this, "Sample has been rejected. Notification sent to Ward/Doctor.");
        }
    }

    private void enterResultsAction() {
        int sel = queueTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a SAMPLE_COLLECTED test to enter results.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String testId = (String) queueModel.getValueAt(sel, 0);
        String status = (String) queueModel.getValueAt(sel, 5);
        
        if (!"SAMPLE_COLLECTED".equals(status)) {
            JOptionPane.showMessageDialog(this, "You must collect the sample first before entering results.", "Invalid Action", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LabTest target = null;
        for (LabTest t : dataStore.getLabTests()) {
            if (t.getId().equals(testId)) {
                target = t;
                break;
            }
        }
        
        if (target == null) return;

        JTextArea txtResults = new JTextArea(10, 40);
        JScrollPane scroll = new JScrollPane(txtResults);
        
        int confirm = JOptionPane.showConfirmDialog(this, scroll, "Enter Results for " + target.getTestName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (confirm == JOptionPane.OK_OPTION) {
            String res = txtResults.getText().trim();
            if (res.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Results cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            target.setResultNotes(res);
            target.setStatus("REPORT_GENERATED");
            dataStore.addLog(labTech.getFullName(), "LAB_TECH", "REPORT_GENERATED", "Generated report for test " + testId);
            dataStore.saveAllData();
            refreshTables();
            JOptionPane.showMessageDialog(this, "Report Published Successfully!");
        }
    }

    public void refreshTables() {
        queueModel.setRowCount(0);
        for (LabTest t : dataStore.getLabTests()) {
            queueModel.addRow(new Object[]{
                t.getId(), t.getPatientName(), t.getDoctorName(), t.getTestName(), 
                String.format("%.2f", t.getPrice()), t.getStatus(), t.getDateRequested()
            });
        }
    }
}
