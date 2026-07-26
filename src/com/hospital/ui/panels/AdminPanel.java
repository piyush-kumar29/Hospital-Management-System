package com.hospital.ui.panels;

import com.hospital.model.*;
import com.hospital.repository.DataStore;
import com.hospital.service.LogService;
import com.hospital.ui.components.StatCard;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {
    private final DataStore dataStore = DataStore.getInstance();
    private final LogService logService = new LogService();

    private JTable usersTable;
    private DefaultTableModel usersTableModel;
    private JTable logsTable;
    private DefaultTableModel logsTableModel;

    public AdminPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        refreshTables();
    }

    private void initUI() {
        // Top Metrics Bar
        JPanel metricsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        metricsPanel.setOpaque(false);

        int totalUsers = dataStore.getUsers().size();
        int totalDocs = (int) dataStore.getUsers().stream().filter(u -> u instanceof Doctor).count();
        int totalPatients = (int) dataStore.getUsers().stream().filter(u -> u instanceof Patient).count();
        int totalLogs = dataStore.getLogs().size();

        metricsPanel.add(new StatCard("Total System Users", String.valueOf(totalUsers), UIUtils.COLOR_PRIMARY));
        metricsPanel.add(new StatCard("Doctors On Staff", String.valueOf(totalDocs), UIUtils.COLOR_SUCCESS));
        metricsPanel.add(new StatCard("Registered Patients", String.valueOf(totalPatients), UIUtils.COLOR_INFO));
        metricsPanel.add(new StatCard("System Audit Logs", String.valueOf(totalLogs), UIUtils.COLOR_WARNING));

        add(metricsPanel, BorderLayout.NORTH);

        // Tabbed Pane for User Management & System Logs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BOLD);

        // Tab 1: User Management
        JPanel userMgmtPanel = createUsersTab();
        tabbedPane.addTab("User Accounts & RBAC", userMgmtPanel);

        // Tab 2: System Audit Logs
        JPanel logsPanel = createLogsTab();
        tabbedPane.addTab("System Security Logs", logsPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton btnAddStaff = UIUtils.createStyledButton("+ Add Staff Member", UIUtils.COLOR_PRIMARY, Color.WHITE);
        JButton btnEditUser = UIUtils.createStyledButton("Edit User", UIUtils.COLOR_WARNING, Color.WHITE);
        JButton btnDeleteUser = UIUtils.createStyledButton("Delete Selected User", UIUtils.COLOR_DANGER, Color.WHITE);
        JButton btnRefresh = UIUtils.createStyledButton("Refresh Table", UIUtils.COLOR_SIDEBAR_HOVER, Color.WHITE);

        btnAddStaff.addActionListener(e -> openAddUserDialog());
        btnEditUser.addActionListener(e -> openEditUserDialog());
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());
        btnRefresh.addActionListener(e -> refreshTables());

        toolbar.add(btnAddStaff);
        toolbar.add(btnEditUser);
        toolbar.add(btnDeleteUser);
        toolbar.add(btnRefresh);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"User ID", "Username", "Full Name", "Role", "Email", "Phone / Details"};
        usersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        usersTable = new JTable(usersTableModel);
        UIUtils.styleTable(usersTable);

        JScrollPane scrollPane = new JScrollPane(usersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLogsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));

        String[] cols = {"Timestamp", "Username", "Role", "Action", "Details"};
        logsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        logsTable = new JTable(logsTableModel);
        UIUtils.styleTable(logsTable);

        JScrollPane scrollPane = new JScrollPane(logsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void openAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Staff User", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 420);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtUsername = UIUtils.createStyledTextField(15);
        JPasswordField txtPassword = UIUtils.createStyledPasswordField(15);
        JTextField txtFullName = UIUtils.createStyledTextField(15);
        JTextField txtEmail = UIUtils.createStyledTextField(15);
        JTextField txtPhone = UIUtils.createStyledTextField(15);

        String[] roles = {Role.RECEPTIONIST.name(), Role.DOCTOR.name(), Role.NURSE.name(), Role.PHARMACIST.name(), Role.BILLING.name(), Role.LAB_TECH.name()};
        JComboBox<String> cbRole = UIUtils.createStyledComboBox(roles);

        JTextField txtSpecialty = UIUtils.createStyledTextField(15);
        JLabel lblSpecialty = new JLabel("Specialty (If Doctor):");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; dialog.add(txtUsername, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; dialog.add(txtPassword, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; dialog.add(txtFullName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; dialog.add(cbRole, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; dialog.add(txtEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; dialog.add(txtPhone, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(lblSpecialty, gbc);
        gbc.gridx = 1; dialog.add(txtSpecialty, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnSave = UIUtils.createStyledButton("Create User Account", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnSave.addActionListener(e -> {
            String uname = txtUsername.getText().trim();
            String pwd = new String(txtPassword.getPassword());
            String fname = txtFullName.getText().trim();
            String email = txtEmail.getText().trim();
            String phone = txtPhone.getText().trim();
            String roleStr = (String) cbRole.getSelectedItem();

            if (uname.isEmpty() || pwd.isEmpty() || fname.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all required fields!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newId = "USR-" + (dataStore.getUsers().size() + 101);
            User newUser = null;
            Role roleEnum = Role.valueOf(roleStr);

            switch (roleEnum) {
                case RECEPTIONIST:
                    newUser = new Receptionist(newId, uname, pwd, fname, email, phone);
                    break;
                case DOCTOR:
                    String spec = txtSpecialty.getText().trim().isEmpty() ? "General Medicine" : txtSpecialty.getText().trim();
                    newUser = new Doctor(newId, uname, pwd, fname, email, phone, spec, "MD", 150.0);
                    break;
                case NURSE:
                    newUser = new Nurse(newId, uname, pwd, fname, email, phone, "General Ward");
                    break;
                case PHARMACIST:
                    newUser = new Pharmacist(newId, uname, pwd, fname, email, phone);
                    break;
                case BILLING:
                    newUser = new BillingStaff(newId, uname, pwd, fname, email, phone);
                    break;
                case LAB_TECH:
                    newUser = new LabTech(newId, uname, pwd, fname, email, phone, "Pathology");
                    break;
                default:
                    break;
            }

            if (newUser != null) {
                dataStore.getUsers().add(newUser);
                dataStore.saveAllData();
                dataStore.addLog("ADMIN", "SYSTEM_ADMIN", "CREATE_USER", "Created user account: " + uname + " (" + roleStr + ")");
                refreshTables();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "User Account created successfully!");
            }
        });

        dialog.add(btnSave, gbc);
        dialog.setVisible(true);
    }

    private void openEditUserDialog() {
        int sel = usersTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Select User", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String uid = (String) usersTableModel.getValueAt(sel, 0);
        User target = dataStore.getUsers().stream().filter(u -> u.getId().equals(uid)).findFirst().orElse(null);
        if (target == null) return;

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Staff User", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 250);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtFullName = UIUtils.createStyledTextField(15);
        txtFullName.setText(target.getFullName());
        JTextField txtEmail = UIUtils.createStyledTextField(15);
        txtEmail.setText(target.getEmail());
        JTextField txtPhone = UIUtils.createStyledTextField(15);
        txtPhone.setText(target.getPhone());

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; dialog.add(txtFullName, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; dialog.add(txtEmail, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; dialog.add(txtPhone, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        JButton btnSave = UIUtils.createStyledButton("Save Changes", UIUtils.COLOR_SUCCESS, Color.WHITE);
        btnSave.addActionListener(e -> {
            target.setFullName(txtFullName.getText().trim());
            target.setEmail(txtEmail.getText().trim());
            target.setPhone(txtPhone.getText().trim());
            
            dataStore.saveAllData();
            dataStore.addLog("ADMIN", "SYSTEM_ADMIN", "EDIT_USER", "Edited details for user " + target.getUsername());
            refreshTables();
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "User updated successfully!");
        });

        dialog.add(btnSave, gbc);
        dialog.setVisible(true);
    }

    private void deleteSelectedUser() {
        int sel = usersTable.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Select User", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String uid = (String) usersTableModel.getValueAt(sel, 0);
        String uname = (String) usersTableModel.getValueAt(sel, 1);

        if (uname.equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(this, "Cannot delete primary System Admin account!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user " + uname + " (" + uid + ")?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dataStore.getUsers().removeIf(u -> u.getId().equals(uid));
            dataStore.saveAllData();
            dataStore.addLog("ADMIN", "SYSTEM_ADMIN", "DELETE_USER", "Deleted user " + uname + " (" + uid + ")");
            refreshTables();
        }
    }

    public void refreshTables() {
        usersTableModel.setRowCount(0);
        for (User u : dataStore.getUsers()) {
            String details = u.getPhone();
            if (u instanceof Doctor) {
                details += " | " + ((Doctor) u).getSpecialty();
            } else if (u instanceof Patient) {
                details += " | " + ((Patient) u).getPatientType();
            }
            usersTableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole().getDisplayName(), u.getEmail(), details});
        }

        logsTableModel.setRowCount(0);
        List<SystemLog> logs = logService.getLogs();
        for (SystemLog log : logs) {
            logsTableModel.addRow(new Object[]{log.getTimestamp(), log.getUsername(), log.getRole(), log.getAction(), log.getDetails()});
        }
    }
}
