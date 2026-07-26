package com.hospital.ui.panels;

import com.hospital.model.*;
import com.hospital.repository.DataStore;
import com.hospital.service.AppointmentService;
import com.hospital.ui.components.StatCard;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DoctorPanel extends JPanel {
    private final Doctor doctor;
    private final AppointmentService appointmentService = new AppointmentService();
    private final DataStore dataStore = DataStore.getInstance();

    private JTable aptTable;
    private DefaultTableModel aptModel;

    public DoctorPanel(Doctor doctor) {
        this.doctor = doctor;
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

        List<Appointment> docApts = appointmentService.getAppointmentsByDoctor(doctor.getId());
        long pending = docApts.stream().filter(a -> a.getStatus() == AppointmentStatus.REQUESTED).count();
        long confirmed = docApts.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED).count();
        long completed = docApts.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();

        metricsPanel.add(new StatCard("Doctor Specialty", doctor.getSpecialty(), UIUtils.COLOR_PRIMARY));
        metricsPanel.add(new StatCard("Pending Approval", String.valueOf(pending), UIUtils.COLOR_WARNING));
        metricsPanel.add(new StatCard("Confirmed Today", String.valueOf(confirmed), UIUtils.COLOR_INFO));
        metricsPanel.add(new StatCard("Completed Consults", String.valueOf(completed), UIUtils.COLOR_SUCCESS));

        add(metricsPanel, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(12, 12));
        mainContent.setBackground(UIUtils.COLOR_BG);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnConfirm = UIUtils.createStyledButton("Accept & Confirm", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnCancel = UIUtils.createStyledButton("Cancel Request", UIUtils.COLOR_DANGER, Color.WHITE);
        JButton btnComplete = UIUtils.createStyledButton("Complete & Prescribe", UIUtils.COLOR_SUCCESS, Color.WHITE);
        JButton btnHistory = UIUtils.createStyledButton("View Patient History", UIUtils.COLOR_WARNING, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnConfirm.addActionListener(e -> changeStatus(AppointmentStatus.CONFIRMED));
        btnCancel.addActionListener(e -> changeStatus(AppointmentStatus.CANCELED));
        btnComplete.addActionListener(e -> openCompleteConsultationDialog());
        btnHistory.addActionListener(e -> viewPatientHistory());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnConfirm);
        toolbar.add(btnCancel);
        toolbar.add(btnComplete);
        toolbar.add(btnHistory);
        toolbar.add(btnRefresh);

        mainContent.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Apt ID", "Patient Name", "Date", "Slot", "Symptoms / Reason", "Status", "Diagnosis"};
        aptModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        aptTable = new JTable(aptModel);
        UIUtils.styleTable(aptTable);

        aptTable.getColumnModel().getColumn(5).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        mainContent.add(new JScrollPane(aptTable), BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);
    }

    private void changeStatus(AppointmentStatus newStatus) {
        int sel = aptTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the list.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aptId = (String) aptModel.getValueAt(sel, 0);
        appointmentService.updateAppointmentStatus(aptId, newStatus, doctor.getFullName());
        refreshTables();
        JOptionPane.showMessageDialog(this, "Appointment " + aptId + " updated to " + newStatus.getDisplayName());
    }

    private void openCompleteConsultationDialog() {
        int sel = aptTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to complete.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aptId = (String) aptModel.getValueAt(sel, 0);
        Appointment target = null;
        for (Appointment a : appointmentService.getAppointmentsByDoctor(doctor.getId())) {
            if (a.getId().equals(aptId)) {
                target = a;
                break;
            }
        }

        if (target == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Clinical Consultation & Prescription (" + aptId + ")", true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setSize(520, 520);
        dialog.setLocationRelativeTo(this);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(16, 16, 16, 16));

        body.add(new JLabel("Patient: " + target.getPatientName() + " | Date: " + target.getAppointmentDate()));
        body.add(Box.createVerticalStrut(10));

        body.add(new JLabel("Clinical Diagnosis / Notes:"));
        JTextArea txtDiagnosis = new JTextArea(4, 30);
        txtDiagnosis.setLineWrap(true);
        body.add(new JScrollPane(txtDiagnosis));
        body.add(Box.createVerticalStrut(10));

        body.add(new JLabel("Prescribe Medicine (e.g. Paracetamol 650mg):"));
        JComboBox<String> cbMedicine = UIUtils.createStyledComboBox(
                dataStore.getMedicines().stream().map(Medicine::getName).toArray(String[]::new)
        );
        body.add(cbMedicine);
        body.add(Box.createVerticalStrut(6));

        body.add(new JLabel("Dosage Instructions:"));
        JTextField txtDosage = UIUtils.createStyledTextField(20);
        txtDosage.setText("1 Tablet 3x daily after food");
        body.add(txtDosage);
        body.add(Box.createVerticalStrut(6));

        body.add(new JLabel("Duration / Quantity:"));
        JTextField txtQty = UIUtils.createStyledTextField(10);
        txtQty.setText("10");
        body.add(txtQty);

        JButton btnSubmit = UIUtils.createStyledButton("Save Diagnosis & Issue Prescription", UIUtils.COLOR_SUCCESS, Color.WHITE);
        final Appointment finalTarget = target;
        btnSubmit.addActionListener(e -> {
            String diag = txtDiagnosis.getText().trim();
            String med = (String) cbMedicine.getSelectedItem();
            String dosage = txtDosage.getText().trim();
            int qty = 10;
            try { qty = Integer.parseInt(txtQty.getText().trim()); } catch (Exception ignored){}

            finalTarget.setDiagnosis(diag);
            finalTarget.setStatus(AppointmentStatus.COMPLETED);

            // Create prescription record
            String rxId = "RX-" + (dataStore.getPrescriptions().size() + 101);
            Prescription rx = new Prescription(rxId, finalTarget.getId(), finalTarget.getPatientId(), finalTarget.getPatientName(), doctor.getId(), doctor.getFullName(), finalTarget.getAppointmentDate(), diag);
            rx.addItem(med, dosage, "5 Days", qty);
            dataStore.getPrescriptions().add(rx);

            dataStore.saveAllData();
            dataStore.addLog(doctor.getFullName(), "DOCTOR", "COMPLETE_CONSULT", "Completed consultation for " + finalTarget.getPatientName() + " and issued Rx " + rxId);

            refreshTables();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Consultation Completed & Prescription Issued!");
        });

        dialog.add(body, BorderLayout.CENTER);
        dialog.add(btnSubmit, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void viewPatientHistory() {
        int sel = aptTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select an appointment to view that patient's history.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aptId = (String) aptModel.getValueAt(sel, 0);
        Appointment target = appointmentService.getAppointmentsByDoctor(doctor.getId()).stream()
                .filter(a -> a.getId().equals(aptId)).findFirst().orElse(null);
        if (target == null) return;

        StringBuilder history = new StringBuilder();
        history.append("Medical History for Patient: ").append(target.getPatientName()).append("\n\n");
        history.append("--- Past Appointments ---\n");
        for (Appointment a : appointmentService.getAppointmentsByPatient(target.getPatientId())) {
            if (a.getStatus() == AppointmentStatus.COMPLETED) {
                history.append("[").append(a.getAppointmentDate()).append("] Dr. ").append(a.getDoctorName())
                        .append(" - Diagnosis: ").append(a.getDiagnosis()).append("\n");
            }
        }
        
        history.append("\n--- Past Prescriptions ---\n");
        for (Prescription rx : dataStore.getPrescriptions()) {
            if (rx.getPatientId().equals(target.getPatientId())) {
                history.append("[").append(rx.getDate()).append("] Status: ").append(rx.getStatus()).append("\n");
                for (Prescription.PrescriptionItem item : rx.getItems()) {
                    history.append("   - ").append(item.getMedicineName()).append(" (").append(item.getDosage()).append(")\n");
                }
            }
        }
        
        JTextArea area = new JTextArea(history.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Patient Medical History", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    public void refreshTables() {
        aptModel.setRowCount(0);
        for (Appointment a : appointmentService.getAppointmentsByDoctor(doctor.getId())) {
            aptModel.addRow(new Object[]{a.getId(), a.getPatientName(), a.getAppointmentDate(), a.getTimeSlot(), a.getSymptoms(), a.getStatus(), a.getDiagnosis()});
        }
    }
}
