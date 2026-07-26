package com.hospital.ui.panels;

import com.hospital.model.BedAllocation;
import com.hospital.model.Patient;
import com.hospital.service.PatientService;
import com.hospital.service.WardService;
import com.hospital.ui.components.StatCard;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ReceptionistPanel extends JPanel {
    private final PatientService patientService = new PatientService();
    private final WardService wardService = new WardService();

    private JTable patientsTable;
    private DefaultTableModel patientsModel;

    private JTable bedsTable;
    private DefaultTableModel bedsModel;

    public ReceptionistPanel() {
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

        List<Patient> patients = patientService.getAllPatients();
        List<BedAllocation> beds = wardService.getAllBeds();
        long occupiedBeds = beds.stream().filter(b -> "OCCUPIED".equalsIgnoreCase(b.getStatus())).count();
        long availableBeds = beds.stream().filter(b -> "AVAILABLE".equalsIgnoreCase(b.getStatus())).count();

        metricsPanel.add(new StatCard("Registered Patients", String.valueOf(patients.size()), UIUtils.COLOR_PRIMARY));
        metricsPanel.add(new StatCard("Total IPD Beds", String.valueOf(beds.size()), UIUtils.COLOR_INFO));
        metricsPanel.add(new StatCard("Beds Occupied", String.valueOf(occupiedBeds), UIUtils.COLOR_WARNING));
        metricsPanel.add(new StatCard("Beds Available", String.valueOf(availableBeds), UIUtils.COLOR_SUCCESS));

        add(metricsPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);

        tabbedPane.addTab("Patient Desk & OPD Registration", createPatientDeskTab());
        tabbedPane.addTab("IPD Bed Allocation", createBedAllocationTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPatientDeskTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnRegister = UIUtils.createStyledButton("+ Register New Patient", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnRegister.addActionListener(e -> openRegisterPatientDialog());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnRegister);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Patient ID", "Name", "Age / Gender", "Type", "Blood Group", "Phone", "Emergency Contact", "Address"};
        patientsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        patientsTable = new JTable(patientsModel);
        UIUtils.styleTable(patientsTable);

        panel.add(new JScrollPane(patientsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBedAllocationTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnAllocate = UIUtils.createStyledButton("Assign Patient to Bed", UIUtils.COLOR_SUCCESS, Color.WHITE);
        JButton btnDischarge = UIUtils.createStyledButton("Discharge Bed", UIUtils.COLOR_DANGER, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh Beds", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnAllocate.addActionListener(e -> allocateBedDialog());
        btnDischarge.addActionListener(e -> dischargeBedAction());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnAllocate);
        toolbar.add(btnDischarge);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Bed ID", "Bed No", "Ward", "Status", "Patient Name", "Admission Date", "Daily Rate ($)"};
        bedsModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bedsTable = new JTable(bedsModel);
        UIUtils.styleTable(bedsTable);

        bedsTable.getColumnModel().getColumn(3).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        panel.add(new JScrollPane(bedsTable), BorderLayout.CENTER);
        return panel;
    }

    private void openRegisterPatientDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Register New Patient", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 12, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = UIUtils.createStyledTextField(15);
        JTextField txtUsername = UIUtils.createStyledTextField(15);
        JPasswordField txtPassword = UIUtils.createStyledPasswordField(15);
        JTextField txtAge = UIUtils.createStyledTextField(15);
        JComboBox<String> cbGender = UIUtils.createStyledComboBox(new String[]{"Male", "Female", "Other"});
        JComboBox<String> cbBlood = UIUtils.createStyledComboBox(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        JComboBox<String> cbType = UIUtils.createStyledComboBox(new String[]{"OPD", "IPD"});
        JTextField txtPhone = UIUtils.createStyledTextField(15);
        JTextField txtEmergency = UIUtils.createStyledTextField(15);
        JTextField txtAddress = UIUtils.createStyledTextField(15);

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; dialog.add(txtName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; dialog.add(txtUsername, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; dialog.add(txtPassword, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; dialog.add(txtAge, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1; dialog.add(cbGender, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Blood Group:"), gbc);
        gbc.gridx = 1; dialog.add(cbBlood, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Patient Type:"), gbc);
        gbc.gridx = 1; dialog.add(cbType, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; dialog.add(txtPhone, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Emergency Contact:"), gbc);
        gbc.gridx = 1; dialog.add(txtEmergency, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; dialog.add(txtAddress, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnSubmit = UIUtils.createStyledButton("Register Patient Record", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnSubmit.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String uname = txtUsername.getText().trim();
                String pwd = new String(txtPassword.getPassword());
                int age = Integer.parseInt(txtAge.getText().trim());
                String gender = (String) cbGender.getSelectedItem();
                String blood = (String) cbBlood.getSelectedItem();
                String type = (String) cbType.getSelectedItem();
                String phone = txtPhone.getText().trim();
                String emergency = txtEmergency.getText().trim();
                String addr = txtAddress.getText().trim();

                if (name.isEmpty() || uname.isEmpty() || pwd.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Required fields missing!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                patientService.registerPatient(uname, pwd, name, uname + "@patient.com", phone, age, gender, blood, addr, emergency, type);
                refreshTables();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Patient Registered Successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid age entered!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        dialog.add(btnSubmit, gbc);
        dialog.setVisible(true);
    }

    private void allocateBedDialog() {
        int sel = bedsTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select an AVAILABLE bed from the table first.", "Select Bed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bedId = (String) bedsModel.getValueAt(sel, 0);
        String bedNo = (String) bedsModel.getValueAt(sel, 1);
        String status = (String) bedsModel.getValueAt(sel, 3);

        if ("OCCUPIED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Bed " + bedNo + " is already OCCUPIED!", "Unavailable", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Patient> patients = patientService.getAllPatients();
        if (patients.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No registered patients available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] patientNames = patients.stream().map(p -> p.getFullName() + " (" + p.getId() + ")").toArray(String[]::new);
        String chosen = (String) JOptionPane.showInputDialog(this, "Select Patient for Bed " + bedNo + ":", "Allocate Bed", JOptionPane.QUESTION_MESSAGE, null, patientNames, patientNames[0]);

        if (chosen != null) {
            Patient p = patients.get(0);
            for (Patient pat : patients) {
                if (chosen.contains(pat.getId())) {
                    p = pat;
                    break;
                }
            }
            wardService.allocateBed(bedId, p.getId(), p.getFullName(), "RECEPTIONIST");
            refreshTables();
            JOptionPane.showMessageDialog(this, "Bed " + bedNo + " assigned to " + p.getFullName());
        }
    }

    private void dischargeBedAction() {
        int sel = bedsTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bed to discharge.", "Select Bed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String bedId = (String) bedsModel.getValueAt(sel, 0);
        String status = (String) bedsModel.getValueAt(sel, 3);

        if (!"OCCUPIED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Selected bed is not currently occupied.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Discharge patient from bed?", "Confirm Discharge", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            wardService.dischargeBed(bedId, "RECEPTIONIST");
            refreshTables();
        }
    }

    public void refreshTables() {
        patientsModel.setRowCount(0);
        for (Patient p : patientService.getAllPatients()) {
            patientsModel.addRow(new Object[]{p.getId(), p.getFullName(), p.getAge() + " / " + p.getGender(), p.getPatientType(), p.getBloodGroup(), p.getPhone(), p.getEmergencyContact(), p.getAddress()});
        }

        bedsModel.setRowCount(0);
        for (BedAllocation b : wardService.getAllBeds()) {
            bedsModel.addRow(new Object[]{b.getId(), b.getBedNumber(), b.getWardName(), b.getStatus(), b.getPatientName().isEmpty() ? "Unassigned" : b.getPatientName(), b.getAdmissionDate(), String.format("%.2f", b.getDailyRate())});
        }
    }
}
