package com.hospital.ui.panels;

import com.hospital.model.Nurse;
import com.hospital.model.Patient;
import com.hospital.model.VitalSign;
import com.hospital.repository.DataStore;
import com.hospital.service.PatientService;
import com.hospital.ui.components.StatCard;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NursePanel extends JPanel {
    private final Nurse nurse;
    private final PatientService patientService = new PatientService();
    private final DataStore dataStore = DataStore.getInstance();

    private JTable vitalsTable;
    private DefaultTableModel vitalsModel;
    
    private JTable marTable;
    private DefaultTableModel marModel;

    public NursePanel(Nurse nurse) {
        this.nurse = nurse;
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

        List<VitalSign> allVitals = dataStore.getVitals();
        List<Patient> patients = patientService.getAllPatients();

        metricsPanel.add(new StatCard("Assigned Station", nurse.getAssignedWard(), UIUtils.COLOR_PRIMARY));
        metricsPanel.add(new StatCard("IPD / OPD Patients", String.valueOf(patients.size()), UIUtils.COLOR_INFO));
        metricsPanel.add(new StatCard("Vitals Logged Today", String.valueOf(allVitals.size()), UIUtils.COLOR_SUCCESS));
        metricsPanel.add(new StatCard("Shift Status", "Active / On Duty", UIUtils.COLOR_WARNING));

        add(metricsPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);
        tabbedPane.addTab("Patient Vitals Log", createVitalsTab());
        tabbedPane.addTab("Medication Administration (MAR)", createMARTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createVitalsTab() {
        JPanel mainContent = new JPanel(new BorderLayout(12, 12));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnRecord = UIUtils.createStyledButton("+ Log Daily Patient Vitals", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh Log", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnRecord.addActionListener(e -> openRecordVitalsDialog());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnRecord);
        toolbar.add(btnRefresh);

        mainContent.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Log ID", "Timestamp", "Patient Name", "Blood Pressure", "Heart Rate (bpm)", "Temp (°F)", "Resp Rate", "Nurse Notes"};
        vitalsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        vitalsTable = new JTable(vitalsModel);
        UIUtils.styleTable(vitalsTable);

        mainContent.add(new JScrollPane(vitalsTable), BorderLayout.CENTER);
        return mainContent;
    }

    private JPanel createMARTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnAdminister = UIUtils.createStyledButton("Record Med Administration", UIUtils.COLOR_SUCCESS, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh Queue", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnAdminister.addActionListener(e -> recordMedAdministration());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnAdminister);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Rx ID", "Patient Name", "Doctor Name", "Medicines", "Status", "Admin Logs"};
        marModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        marTable = new JTable(marModel);
        UIUtils.styleTable(marTable);
        marTable.getColumnModel().getColumn(4).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        panel.add(new JScrollPane(marTable), BorderLayout.CENTER);
        return panel;
    }

    private void openRecordVitalsDialog() {
        List<Patient> patients = patientService.getAllPatients();
        if (patients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No registered patients found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Log Patient Vital Signs", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(460, 440);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        String[] patientNames = patients.stream().map(p -> p.getFullName() + " (" + p.getId() + ")").toArray(String[]::new);
        JComboBox<String> cbPatient = UIUtils.createStyledComboBox(patientNames);

        JTextField txtBp = UIUtils.createStyledTextField(15); txtBp.setText("120/80 mmHg");
        JTextField txtHr = UIUtils.createStyledTextField(15); txtHr.setText("72");
        JTextField txtTemp = UIUtils.createStyledTextField(15); txtTemp.setText("98.6");
        JTextField txtResp = UIUtils.createStyledTextField(15); txtResp.setText("16");
        JTextField txtNotes = UIUtils.createStyledTextField(15); txtNotes.setText("Normal resting vitals");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Select Patient:"), gbc);
        gbc.gridx = 1; dialog.add(cbPatient, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Blood Pressure (BP):"), gbc);
        gbc.gridx = 1; dialog.add(txtBp, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Heart Rate (bpm):"), gbc);
        gbc.gridx = 1; dialog.add(txtHr, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Temperature (°F):"), gbc);
        gbc.gridx = 1; dialog.add(txtTemp, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Respiratory Rate:"), gbc);
        gbc.gridx = 1; dialog.add(txtResp, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Clinical Observations:"), gbc);
        gbc.gridx = 1; dialog.add(txtNotes, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnSubmit = UIUtils.createStyledButton("Save Vitals Entry", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnSubmit.addActionListener(e -> {
            try {
                String chosen = (String) cbPatient.getSelectedItem();
                Patient pat = patients.get(cbPatient.getSelectedIndex());
                String bp = txtBp.getText().trim();
                int hr = Integer.parseInt(txtHr.getText().trim());
                double temp = Double.parseDouble(txtTemp.getText().trim());
                int resp = Integer.parseInt(txtResp.getText().trim());
                String notes = txtNotes.getText().trim();

                patientService.recordVitals(pat.getId(), pat.getFullName(), nurse.getFullName(), bp, hr, temp, resp, notes);
                refreshTables();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Vitals recorded for " + pat.getFullName());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid numbers in vitals fields!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(btnSubmit, gbc);
        dialog.setVisible(true);
    }

    private void recordMedAdministration() {
        int sel = marTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a prescription from the MAR list.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String rxId = (String) marModel.getValueAt(sel, 0);
        com.hospital.model.Prescription rx = dataStore.getPrescriptions().stream().filter(r -> r.getId().equals(rxId)).findFirst().orElse(null);
        if (rx == null) return;

        JTextArea txtNotes = new JTextArea(3, 30);
        int result = JOptionPane.showConfirmDialog(this, new JScrollPane(txtNotes), "Administer Medicines for " + rx.getPatientName() + "\nEnter Administration Notes:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            String note = txtNotes.getText().trim();
            String logEntry = timestamp + " - Administered by " + nurse.getFullName() + (note.isEmpty() ? "" : " (Notes: " + note + ")");
            rx.addAdministrationLog(logEntry);
            dataStore.addLog(nurse.getFullName(), "NURSE", "MED_ADMINISTRATION", "Administered medicines for Rx " + rxId);
            dataStore.saveAllData();
            refreshTables();
            JOptionPane.showMessageDialog(this, "Medication Administration Recorded!");
        }
    }

    public void refreshTables() {
        if (vitalsModel != null) {
            vitalsModel.setRowCount(0);
            for (VitalSign v : dataStore.getVitals()) {
                vitalsModel.addRow(new Object[]{v.getId(), v.getTimestamp(), v.getPatientName(), v.getBloodPressure(), v.getHeartRate(), v.getTemperature(), v.getRespiratoryRate(), v.getNotes()});
            }
        }
        
        if (marModel != null) {
            marModel.setRowCount(0);
            for (com.hospital.model.Prescription rx : dataStore.getPrescriptions()) {
                StringBuilder meds = new StringBuilder();
                for (com.hospital.model.Prescription.PrescriptionItem item : rx.getItems()) {
                    meds.append(item.getMedicineName()).append(" ");
                }
                
                String lastLog = "No logs yet";
                if (rx.getAdministrationLogs() != null && !rx.getAdministrationLogs().isEmpty()) {
                    lastLog = rx.getAdministrationLogs().get(rx.getAdministrationLogs().size() - 1);
                }
                
                marModel.addRow(new Object[]{rx.getId(), rx.getPatientName(), rx.getDoctorName(), meds.toString(), rx.getStatus(), lastLog});
            }
        }
    }
}
