package com.hospital.ui;

import com.hospital.model.*;
import com.hospital.service.AuthService;
import com.hospital.ui.components.UIUtils;
import com.hospital.ui.panels.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardFrame extends JFrame {
    private final User currentUser;
    private final AuthService authService;
    private JPanel mainContentContainer;

    public DashboardFrame(User currentUser, AuthService authService) {
        this.currentUser = currentUser;
        this.authService = authService;

        setTitle("Hospital Management System - Standalone JDK Desktop App");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.COLOR_SIDEBAR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // App Logo & Title
        JLabel logoLabel = new JLabel("🏥 HOSPICARE | Management System");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoLabel.setForeground(Color.WHITE);
        headerPanel.add(logoLabel, BorderLayout.WEST);

        // Right side: User Profile info & Logout
        JPanel userBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userBox.setOpaque(false);

        JLabel userLabel = new JLabel("Logged in as: " + currentUser.getFullName() + " [" + currentUser.getRole().getDisplayName() + "]");
        userLabel.setFont(UIUtils.FONT_BOLD);
        userLabel.setForeground(new Color(226, 232, 240));

        JButton btnLogout = UIUtils.createStyledButton("Logout", UIUtils.COLOR_DANGER, Color.WHITE);
        btnLogout.addActionListener(e -> {
            authService.logout();
            dispose();
            new LoginFrame().setVisible(true);
        });

        userBox.add(userLabel);
        userBox.add(btnLogout);
        headerPanel.add(userBox, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Center Content Area
        mainContentContainer = new JPanel(new BorderLayout());
        mainContentContainer.setBackground(UIUtils.COLOR_BG);

        // Load role specific panel
        JPanel rolePanel = createRolePanel(currentUser);
        mainContentContainer.add(rolePanel, BorderLayout.CENTER);

        add(mainContentContainer, BorderLayout.CENTER);
    }

    private JPanel createRolePanel(User user) {
        switch (user.getRole()) {
            case SYSTEM_ADMIN:
                return new AdminPanel();
            case RECEPTIONIST:
                return new ReceptionistPanel();
            case DOCTOR:
                return new DoctorPanel((Doctor) user);
            case NURSE:
                return new NursePanel((Nurse) user);
            case PHARMACIST:
                return new PharmacistPanel((Pharmacist) user);
            case BILLING:
                return new BillingPanel((BillingStaff) user);
            case PATIENT:
                return new PatientPanel((Patient) user);
            default:
                JPanel p = new JPanel();
                p.add(new JLabel("No panel configured for role: " + user.getRole()));
                return p;
        }
    }
}
