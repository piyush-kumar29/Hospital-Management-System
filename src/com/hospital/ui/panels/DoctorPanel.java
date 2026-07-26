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
import java.util.List;

public class DoctorPanel extends JPanel {
    private final Doctor doctor;
    private final AppointmentService appointmentService = new AppointmentService();
    private final BillingService billingService = new BillingService();
    private final DataStore dataStore = DataStore.getInstance();

    private JTable aptTable;
    private DefaultTableModel aptModel;

    private JTable labTable;
    private DefaultTableModel labModel;

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

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);
        
        tabbedPane.addTab("Consultations & Appointments", createConsultationsTab());
        tabbedPane.addTab("Lab Tests & Reports", createLabReportsTab());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createConsultationsTab() {
        JPanel mainContent = new JPanel(new BorderLayout(12, 12));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnConfirm = UIUtils.createStyledButton("Accept & Confirm", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnCancel = UIUtils.createStyledButton("Cancel Request", UIUtils.COLOR_DANGER, Color.WHITE);
        JButton btnComplete = UIUtils.createStyledButton("Complete & Prescribe", UIUtils.COLOR_SUCCESS, Color.WHITE);
        JButton btnHistory = UIUtils.createStyledButton("View Patient History", UIUtils.COLOR_WARNING, Color.WHITE);
        JButton btnAdmit = UIUtils.createStyledButton("Order IPD Admission", UIUtils.COLOR_DANGER, Color.WHITE);
        JButton btnDischarge = UIUtils.createStyledButton("Clinical Discharge", UIUtils.COLOR_INFO, Color.WHITE);
        JButton btnCrossConsult = UIUtils.createStyledButton("Cross-Consult", UIUtils.COLOR_SECONDARY, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnConfirm.addActionListener(e -> changeStatus(AppointmentStatus.CONFIRMED));
        btnCancel.addActionListener(e -> changeStatus(AppointmentStatus.CANCELED));
        btnComplete.addActionListener(e -> openCompleteConsultationDialog());
        btnHistory.addActionListener(e -> viewPatientHistory());
        btnAdmit.addActionListener(e -> orderIPDAdmission());
        btnDischarge.addActionListener(e -> clinicalDischarge());
        btnCrossConsult.addActionListener(e -> crossConsultation());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnConfirm);
        toolbar.add(btnCancel);
        toolbar.add(btnComplete);
        toolbar.add(btnHistory);
        toolbar.add(btnAdmit);
        toolbar.add(btnDischarge);
        toolbar.add(btnCrossConsult);
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
        return mainContent;
    }

    private JPanel createLabReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnRequest = UIUtils.createStyledButton("+ Request Lab Test", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnView = UIUtils.createStyledButton("View Lab Result", UIUtils.COLOR_INFO, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnRequest.addActionListener(e -> requestLabTestDialog());
        btnView.addActionListener(e -> viewLabResultDialog());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnRequest);
        toolbar.add(btnView);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Test ID", "Patient Name", "Test Name", "Date Requested", "Status", "Result / Notes"};
        labModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        labTable = new JTable(labModel);
        UIUtils.styleTable(labTable);
        labTable.getColumnModel().getColumn(4).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        panel.add(new JScrollPane(labTable), BorderLayout.CENTER);
        return panel;
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
        dialog.setSize(650, 650);
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

        // Medicine Builder
        JPanel rxBuilder = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JComboBox<String> cbMedicine = UIUtils.createStyledComboBox(
                dataStore.getMedicines().stream().map(Medicine::getName).toArray(String[]::new)
        );
        JTextField txtDosage = UIUtils.createStyledTextField(12);
        txtDosage.setText("1x Daily");
        JTextField txtQty = UIUtils.createStyledTextField(5);
        txtQty.setText("10");
        JButton btnAddMed = UIUtils.createStyledButton("+ Add", UIUtils.COLOR_INFO, Color.WHITE);
        
        rxBuilder.add(new JLabel("Med:"));
        rxBuilder.add(cbMedicine);
        rxBuilder.add(new JLabel("Dosage:"));
        rxBuilder.add(txtDosage);
        rxBuilder.add(new JLabel("Qty:"));
        rxBuilder.add(txtQty);
        rxBuilder.add(btnAddMed);
        body.add(rxBuilder);

        String[] rxCols = {"Medicine Name", "Dosage", "Quantity"};
        DefaultTableModel tempRxModel = new DefaultTableModel(rxCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tempRxTable = new JTable(tempRxModel);
        UIUtils.styleTable(tempRxTable);
        JScrollPane scrollRx = new JScrollPane(tempRxTable);
        scrollRx.setPreferredSize(new Dimension(600, 150));
        body.add(scrollRx);
        body.add(Box.createVerticalStrut(10));

        btnAddMed.addActionListener(e -> {
            String med = (String) cbMedicine.getSelectedItem();
            String dosage = txtDosage.getText().trim();
            String qty = txtQty.getText().trim();
            if (dosage.isEmpty() || qty.isEmpty()) return;
            try { Integer.parseInt(qty); } catch (Exception ex) { return; }
            tempRxModel.addRow(new Object[]{med, dosage, qty});
        });

        JButton btnSubmit = UIUtils.createStyledButton("Save Diagnosis, Prescribe & Bill", UIUtils.COLOR_SUCCESS, Color.WHITE);
        final Appointment finalTarget = target;
        btnSubmit.addActionListener(e -> {
            String diag = txtDiagnosis.getText().trim();
            finalTarget.setDiagnosis(diag);
            finalTarget.setStatus(AppointmentStatus.COMPLETED);

            // Create prescription record
            String rxId = "RX-" + (dataStore.getPrescriptions().size() + 101);
            Prescription rx = new Prescription(rxId, finalTarget.getId(), finalTarget.getPatientId(), finalTarget.getPatientName(), doctor.getId(), doctor.getFullName(), finalTarget.getAppointmentDate(), diag);
            
            for (int i = 0; i < tempRxModel.getRowCount(); i++) {
                String medName = (String) tempRxModel.getValueAt(i, 0);
                String dosage = (String) tempRxModel.getValueAt(i, 1);
                int qty = Integer.parseInt((String) tempRxModel.getValueAt(i, 2));
                
                rx.addItem(medName, dosage, "Standard", qty);
            }
            dataStore.getPrescriptions().add(rx);

            // Generate Invoice
            Patient patient = null;
            for (User u : dataStore.getUsers()) {
                if (u instanceof Patient && u.getId().equals(finalTarget.getPatientId())) {
                    patient = (Patient) u;
                    break;
                }
            }
            
            if (patient != null) {
                billingService.addChargeToPatientInvoice(patient, "Doctor Consultation Fee (Dr. " + doctor.getFullName() + ")", doctor.getConsultationFee(), doctor.getFullName());
            }

            dataStore.saveAllData();
            dataStore.addLog(doctor.getFullName(), "DOCTOR", "COMPLETE_CONSULT", "Completed consultation for " + finalTarget.getPatientName() + " and added consult fee.");

            refreshTables();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Consultation Completed, Prescription Issued & Consultation Fee Billed!");
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

    private void orderIPDAdmission() {
        int sel = aptTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient's appointment to order IPD admission.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aptId = (String) aptModel.getValueAt(sel, 0);
        Appointment target = appointmentService.getAppointmentsByDoctor(doctor.getId()).stream()
                .filter(a -> a.getId().equals(aptId)).findFirst().orElse(null);
        if (target == null) return;

        String[] wards = {"Ward A (General)", "Ward B (Private)", "ICU Ward", "Emergency"};
        String ward = (String) JOptionPane.showInputDialog(this, "Select Target Ward for Admission:", "Order IPD Admission", JOptionPane.QUESTION_MESSAGE, null, wards, wards[0]);
        
        if (ward != null) {
            String reqId = "IPD-" + (dataStore.getIpdAdmissionRequests().size() + 101);
            String date = java.time.LocalDate.now().toString();
            IPDAdmissionRequest req = new IPDAdmissionRequest(reqId, target.getPatientId(), target.getPatientName(), doctor.getId(), doctor.getFullName(), ward, date);
            dataStore.getIpdAdmissionRequests().add(req);
            dataStore.saveAllData();
            JOptionPane.showMessageDialog(this, "IPD Admission ordered for " + target.getPatientName() + " in " + ward + ". Sent to Front Desk.");
        }
    }

    private void clinicalDischarge() {
        int sel = aptTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient's appointment to process discharge.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aptId = (String) aptModel.getValueAt(sel, 0);
        Appointment target = appointmentService.getAppointmentsByDoctor(doctor.getId()).stream()
                .filter(a -> a.getId().equals(aptId)).findFirst().orElse(null);
        if (target == null) return;

        Patient patient = (Patient) dataStore.getUsers().stream().filter(u -> u.getId().equals(target.getPatientId())).findFirst().orElse(null);
        if (patient != null) {
            if (patient.isClinicalDischarge()) {
                JOptionPane.showMessageDialog(this, "Patient is already clinically discharged.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clinically discharge " + patient.getFullName() + "? This will allow billing to process final settlement.", "Confirm Clinical Discharge", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                patient.setClinicalDischarge(true);
                dataStore.saveAllData();
                JOptionPane.showMessageDialog(this, "Patient " + patient.getFullName() + " clinically discharged.");
            }
        }
    }

    private void crossConsultation() {
        int sel = aptTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a patient's appointment to request cross-consultation.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String aptId = (String) aptModel.getValueAt(sel, 0);
        Appointment target = appointmentService.getAppointmentsByDoctor(doctor.getId()).stream()
                .filter(a -> a.getId().equals(aptId)).findFirst().orElse(null);
        if (target == null) return;

        Patient patient = (Patient) dataStore.getUsers().stream().filter(u -> u.getId().equals(target.getPatientId())).findFirst().orElse(null);
        if (patient == null) return;

        List<User> doctors = new java.util.ArrayList<>();
        for (User u : dataStore.getUsers()) {
            if (u instanceof Doctor && !u.getId().equals(doctor.getId())) {
                doctors.add(u);
            }
        }
        String[] docNames = doctors.stream().map(u -> u.getFullName() + " (" + ((Doctor)u).getSpecialty() + ")").toArray(String[]::new);
        
        String chosen = (String) JOptionPane.showInputDialog(this, "Select Doctor for Cross-Consultation:", "Cross Consult", JOptionPane.QUESTION_MESSAGE, null, docNames, docNames[0]);
        if (chosen != null) {
            Doctor chosenDoc = (Doctor) doctors.stream().filter(u -> chosen.contains(u.getFullName())).findFirst().orElse(null);
            if (chosenDoc != null) {
                patient.addCrossConsultDoctor(chosenDoc.getId());
                dataStore.saveAllData();
                JOptionPane.showMessageDialog(this, "Cross-consultation requested with " + chosenDoc.getFullName() + " for patient " + patient.getFullName() + ".");
            }
        }
    }

    private void requestLabTestDialog() {
        int sel = aptTable.getSelectedRow();
        String defaultPatientName = "";
        String defaultPatientId = "";
        if (sel != -1) {
            Appointment apt = appointmentService.getAppointmentsByDoctor(doctor.getId()).get(sel);
            defaultPatientName = apt.getPatientName();
            defaultPatientId = apt.getPatientId();
        }

        String[] tests = {"Complete Blood Count (CBC)", "Lipid Profile", "Liver Function Test (LFT)", "X-Ray Chest", "MRI Scan", "Urine Analysis", "COVID-19 RT-PCR"};
        String testName = (String) JOptionPane.showInputDialog(this, "Select Lab Test to Request for " + (defaultPatientName.isEmpty() ? "Patient" : defaultPatientName) + ":", "Request Lab Test", JOptionPane.QUESTION_MESSAGE, null, tests, tests[0]);
        
        if (testName != null && !defaultPatientId.isEmpty()) {
            String date = java.time.LocalDate.now().toString();
            String testId = "LAB-" + (dataStore.getLabTests().size() + 101);
            double price = 50.0;
            if (testName.contains("MRI")) price = 500.0;
            else if (testName.contains("X-Ray")) price = 150.0;
            else if (testName.contains("COVID")) price = 75.0;
            
            LabTest test = new LabTest(testId, defaultPatientId, defaultPatientName, doctor.getId(), doctor.getFullName(), testName, date, price);
            dataStore.getLabTests().add(test);
            dataStore.saveAllData();
            refreshTables();
            JOptionPane.showMessageDialog(this, "Requested " + testName + " for " + defaultPatientName);
        } else if (testName != null) {
            JOptionPane.showMessageDialog(this, "Please select an appointment from the consultations tab first to infer the patient.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void viewLabResultDialog() {
        int sel = labTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Select a lab test from the list.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String testId = (String) labModel.getValueAt(sel, 0);
        LabTest test = dataStore.getLabTests().stream().filter(t -> t.getId().equals(testId)).findFirst().orElse(null);
        if (test == null) return;

        if (!"REPORT_GENERATED".equals(test.getStatus())) {
            JOptionPane.showMessageDialog(this, "The lab report has not been generated yet. Status is: " + test.getStatus(), "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JTextArea txtNotes = new JTextArea(10, 40);
        txtNotes.setText(test.getResultNotes());
        txtNotes.setEditable(false);
        txtNotes.setLineWrap(true);
        JOptionPane.showMessageDialog(this, new JScrollPane(txtNotes), "Lab Result for " + test.getTestName(), JOptionPane.INFORMATION_MESSAGE);
    }

    public void refreshTables() {
        aptModel.setRowCount(0);
        for (Appointment a : appointmentService.getAppointmentsByDoctor(doctor.getId())) {
            aptModel.addRow(new Object[]{a.getId(), a.getPatientName(), a.getAppointmentDate(), a.getTimeSlot(), a.getSymptoms(), a.getStatus(), a.getDiagnosis()});
        }
        
        if (labModel != null) {
            labModel.setRowCount(0);
            for (LabTest lt : dataStore.getLabTests()) {
                if (lt.getDoctorId().equals(doctor.getId())) {
                    labModel.addRow(new Object[]{lt.getId(), lt.getPatientName(), lt.getTestName(), lt.getDateRequested(), lt.getStatus(), lt.getResultNotes()});
                }
            }
        }
    }
}
