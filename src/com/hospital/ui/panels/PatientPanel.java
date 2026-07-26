package com.hospital.ui.panels;

import com.hospital.model.*;
import com.hospital.repository.DataStore;
import com.hospital.service.AppointmentService;
import com.hospital.service.BillingService;
import com.hospital.ui.components.StatCard;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PatientPanel extends JPanel {
    private final Patient patient;
    private final AppointmentService appointmentService = new AppointmentService();
    private final BillingService billingService = new BillingService();
    private final DataStore dataStore = DataStore.getInstance();

    private JTable aptTable;
    private DefaultTableModel aptModel;

    private JTable docsTable;
    private DefaultTableModel docsModel;

    private JTable billsTable;
    private DefaultTableModel billsModel;
    
    private JTable rxTable;
    private DefaultTableModel rxModel;

    public PatientPanel(Patient patient) {
        this.patient = patient;
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

        List<Appointment> myApts = appointmentService.getAppointmentsByPatient(patient.getId());
        List<Invoice> myBills = billingService.getInvoicesByPatient(patient.getId());
        long activeApts = myApts.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.REQUESTED).count();

        metricsPanel.add(new StatCard("Welcome Patient", patient.getFullName(), UIUtils.COLOR_PRIMARY));
        metricsPanel.add(new StatCard("Patient Record Type", patient.getPatientType(), UIUtils.COLOR_INFO));
        metricsPanel.add(new StatCard("Active Appointments", String.valueOf(activeApts), UIUtils.COLOR_WARNING));
        metricsPanel.add(new StatCard("Billing Accounts", String.valueOf(myBills.size()) + " Invoices", UIUtils.COLOR_SUCCESS));

        add(metricsPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);

        tabbedPane.addTab("My Appointments", createAppointmentsTab());
        tabbedPane.addTab("Book Doctor Appointment", createBookTab());
        tabbedPane.addTab("My Invoices & Prescriptions", createMedicalRecordsTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createAppointmentsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnCancel = UIUtils.createStyledButton("Cancel Selected Appointment", UIUtils.COLOR_DANGER, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnCancel.addActionListener(e -> cancelAppointmentAction());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnCancel);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Doctor Name", "Date", "Slot", "Status", "Symptoms / Notes", "Doctor Diagnosis"};
        aptModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        aptTable = new JTable(aptModel);
        UIUtils.styleTable(aptTable);

        aptTable.getColumnModel().getColumn(4).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        panel.add(new JScrollPane(aptTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBookTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnBook = UIUtils.createStyledButton("Book Slot with Selected Doctor", UIUtils.COLOR_SUCCESS, Color.WHITE);
        btnBook.addActionListener(e -> openBookSlotDialog());

        toolbar.add(btnBook);
        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Doctor ID", "Doctor Name", "Specialty", "Qualification", "Consultation Fee ($)", "Available Slots"};
        docsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        docsTable = new JTable(docsModel);
        UIUtils.styleTable(docsTable);

        panel.add(new JScrollPane(docsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMedicalRecordsTab() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Bills Panel
        JPanel billsPanel = new JPanel(new BorderLayout());
        billsPanel.setOpaque(false);
        billsPanel.add(new JLabel("My Invoices & Billing"), BorderLayout.NORTH);
        
        String[] bCols = {"Invoice ID", "Date", "Total Fee ($)", "Payment Status", "Payment Method"};
        billsModel = new DefaultTableModel(bCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        billsTable = new JTable(billsModel);
        UIUtils.styleTable(billsTable);
        billsTable.getColumnModel().getColumn(3).setCellRenderer(new UIUtils.StatusBadgeRenderer());
        billsPanel.add(new JScrollPane(billsTable), BorderLayout.CENTER);
        
        // Prescriptions Panel
        JPanel rxPanel = new JPanel(new BorderLayout());
        rxPanel.setOpaque(false);
        rxPanel.add(new JLabel("My Prescriptions"), BorderLayout.NORTH);
        
        String[] rCols = {"Rx ID", "Date", "Doctor Name", "Medicines Prescribed", "Status"};
        rxModel = new DefaultTableModel(rCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        rxTable = new JTable(rxModel);
        UIUtils.styleTable(rxTable);
        rxTable.getColumnModel().getColumn(4).setCellRenderer(new UIUtils.StatusBadgeRenderer());
        rxPanel.add(new JScrollPane(rxTable), BorderLayout.CENTER);

        panel.add(billsPanel);
        panel.add(rxPanel);
        
        return panel;
    }

    private void openBookSlotDialog() {
        int sel = docsTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a doctor from the list first.", "Select Doctor", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String docId = (String) docsModel.getValueAt(sel, 0);
        String docName = (String) docsModel.getValueAt(sel, 1);

        Doctor selectedDoc = null;
        for (User u : dataStore.getUsers()) {
            if (u instanceof Doctor && u.getId().equals(docId)) {
                selectedDoc = (Doctor) u;
                break;
            }
        }

        if (selectedDoc == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Book Appointment with " + docName, true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(460, 400);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String defaultDate = LocalDate.now().plusDays(1).toString();
        JTextField txtDate = UIUtils.createStyledTextField(15); txtDate.setText(defaultDate);

        String[] slots = selectedDoc.getAvailableSlots().toArray(new String[0]);
        JComboBox<String> cbSlots = UIUtils.createStyledComboBox(slots);

        JTextField txtSymptoms = UIUtils.createStyledTextField(15); txtSymptoms.setText("Routine Consultation");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 1; dialog.add(new JLabel(docName + " (" + selectedDoc.getSpecialty() + ")"), gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Appointment Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; dialog.add(txtDate, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Select Time Slot:"), gbc);
        gbc.gridx = 1; dialog.add(cbSlots, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Symptoms / Medical Concerns:"), gbc);
        gbc.gridx = 1; dialog.add(txtSymptoms, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnSubmit = UIUtils.createStyledButton("Confirm Booking Request", UIUtils.COLOR_SUCCESS, Color.WHITE);
        final Doctor finalDoc = selectedDoc;
        btnSubmit.addActionListener(e -> {
            try {
                String date = txtDate.getText().trim();
                String slot = (String) cbSlots.getSelectedItem();
                String symptoms = txtSymptoms.getText().trim();

                Appointment app = appointmentService.bookAppointment(patient.getId(), patient.getFullName(), finalDoc.getId(), finalDoc.getFullName(), date, slot, symptoms);
                refreshTables();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Appointment Requested Successfully!\nStatus: REQUESTED (Awaiting Doctor Confirmation)");
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Slot Unavailable", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(btnSubmit, gbc);
        dialog.setVisible(true);
    }

    private void cancelAppointmentAction() {
        int sel = aptTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select an appointment to cancel.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aptId = (String) aptModel.getValueAt(sel, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel appointment " + aptId + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            appointmentService.updateAppointmentStatus(aptId, AppointmentStatus.CANCELED, patient.getFullName());
            refreshTables();
        }
    }

    public void refreshTables() {
        aptModel.setRowCount(0);
        for (Appointment a : appointmentService.getAppointmentsByPatient(patient.getId())) {
            aptModel.addRow(new Object[]{a.getId(), a.getDoctorName(), a.getAppointmentDate(), a.getTimeSlot(), a.getStatus(), a.getSymptoms(), a.getDiagnosis().isEmpty() ? "Pending" : a.getDiagnosis()});
        }

        docsModel.setRowCount(0);
        List<Doctor> doctors = dataStore.getUsers().stream().filter(u -> u instanceof Doctor).map(u -> (Doctor) u).collect(Collectors.toList());
        for (Doctor d : doctors) {
            docsModel.addRow(new Object[]{d.getId(), d.getFullName(), d.getSpecialty(), d.getQualification(), String.format("%.2f", d.getConsultationFee()), d.getAvailableSlots().size() + " Daily Slots"});
        }

        billsModel.setRowCount(0);
        for (Invoice inv : billingService.getInvoicesByPatient(patient.getId())) {
            billsModel.addRow(new Object[]{inv.getId(), inv.getIssueDate(), String.format("%.2f", inv.getTotalAmount()), inv.getPaymentStatus(), inv.getPaymentMethod()});
        }
        
        rxModel.setRowCount(0);
        for (Prescription rx : dataStore.getPrescriptions()) {
            if (rx.getPatientId().equals(patient.getId())) {
                StringBuilder items = new StringBuilder();
                for (Prescription.PrescriptionItem item : rx.getItems()) {
                    items.append(item.getMedicineName()).append(" (x").append(item.getQuantity()).append("); ");
                }
                rxModel.addRow(new Object[]{rx.getId(), rx.getDate(), rx.getDoctorName(), items.toString(), rx.getStatus()});
            }
        }
    }
}
