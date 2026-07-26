package com.hospital.ui;

import com.hospital.model.User;
import com.hospital.service.AuthService;
import com.hospital.service.PatientService;
import com.hospital.ui.components.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;

public class LoginFrame extends JFrame {
    private final AuthService authService = new AuthService();
    private final PatientService patientService = new PatientService();

    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("Hospital Management System - Standalone JDK Login");
        setSize(850, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);

        initUI();
    }

    private void initUI() {
        JPanel rootPanel = new JPanel(new GridLayout(1, 2));

        // Left Branding Panel
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIUtils.COLOR_SIDEBAR, getWidth(), getHeight(), new Color(30, 58, 138));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel title1 = new JLabel("🏥 HOSPICARE");
        title1.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title1.setForeground(Color.WHITE);

        JLabel title2 = new JLabel("Standalone Enterprise HMS");
        title2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        title2.setForeground(new Color(148, 163, 184));

        JLabel subtitle = new JLabel("<html><br>• 100% Pure Standard Java JDK<br>• Zero External Dependencies<br>• Standalone File Serialization<br>• Complete 7-Role RBAC Workflow</html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(203, 213, 225));

        leftPanel.add(title1);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(title2);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(subtitle);

        rootPanel.add(leftPanel);

        // Right Login Panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel loginHeader = new JLabel("Account Login");
        loginHeader.setFont(UIUtils.FONT_TITLE);
        loginHeader.setForeground(UIUtils.COLOR_TEXT_MAIN);

        txtUsername = UIUtils.createStyledTextField(20);
        txtUsername.setText("admin");

        txtPassword = UIUtils.createStyledPasswordField(20);
        txtPassword.setText("admin123");

        JButton btnLogin = UIUtils.createStyledButton("Sign In to Portal", UIUtils.COLOR_PRIMARY, Color.WHITE);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnLogin.addActionListener(e -> performLogin(txtUsername.getText().trim(), new String(txtPassword.getPassword())));

        // Quick Demo Role Buttons Box
        JPanel demoBox = new JPanel(new GridLayout(4, 2, 6, 6));
        demoBox.setOpaque(false);

        JLabel demoHeader = new JLabel("⚡ Quick Demo Presets (1-Click Login):");
        demoHeader.setFont(UIUtils.FONT_BOLD);
        demoHeader.setForeground(UIUtils.COLOR_TEXT_MUTED);

        demoBox.add(createDemoBtn("Admin", "admin", "admin123"));
        demoBox.add(createDemoBtn("Reception", "reception", "recep123"));
        demoBox.add(createDemoBtn("Doctor", "doctor", "doc123"));
        demoBox.add(createDemoBtn("Nurse", "nurse", "nurse123"));
        demoBox.add(createDemoBtn("Pharmacist", "pharma", "pharma123"));
        demoBox.add(createDemoBtn("Billing", "billing", "bill123"));
        demoBox.add(createDemoBtn("Patient", "patient", "patient123"));

        rightPanel.add(loginHeader);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(new JLabel("Username:"));
        rightPanel.add(txtUsername);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(new JLabel("Password:"));
        rightPanel.add(txtPassword);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(btnLogin);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(demoHeader);
        rightPanel.add(Box.createVerticalStrut(8));
        rightPanel.add(demoBox);

        rootPanel.add(rightPanel);
        add(rootPanel);
    }

    private JButton createDemoBtn(String label, String uname, String pwd) {
        JButton btn = new JButton(label);
        btn.setFont(UIUtils.FONT_SMALL);
        btn.setBackground(new Color(241, 245, 249));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            txtUsername.setText(uname);
            txtPassword.setText(pwd);
            performLogin(uname, pwd);
        });
        return btn;
    }

    private void performLogin(String username, String password) {
        Optional<User> userOpt = authService.login(username, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            dispose();
            SwingUtilities.invokeLater(() -> new DashboardFrame(user, authService).setVisible(true));
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Authentication Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
