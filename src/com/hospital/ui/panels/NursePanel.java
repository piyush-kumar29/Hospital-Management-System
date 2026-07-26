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
import java.util.List;

public class NursePanel extends JPanel {
    private final Nurse nurse;
    private final PatientService patientService = new PatientService();
    private final DataStore dataStore = DataStore.getInstance();

    private JTable vitalsTable;
    private DefaultTableModel vitalsModel;

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

        JPanel mainContent = new JPanel(new BorderLayout(12, 12));
        mainContent.setBackground(UIUtils.COLOR_BG);

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

        add(mainContent, BorderLayout.CENTER);
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

    public void refreshTables() {
        vitalsModel.setRowCount(0);
        for (VitalSign v : dataStore.getVitals()) {
            vitalsModel.addRow(new Object[]{v.getId(), v.getTimestamp(), v.getPatientName(), v.getBloodPressure(), v.getHeartRate(), v.getTemperature(), v.getRespiratoryRate(), v.getNotes()});
        }
    }
}
