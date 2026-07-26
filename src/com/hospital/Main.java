package com.hospital;

import com.hospital.repository.DataStore;
import com.hospital.ui.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set Look and Feel to System / Modern Swing
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equalsIgnoreCase(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Initialize Data Repository (loads or seeds .dat files)
        DataStore.getInstance();

        // Register Shutdown hook for serializing data on application exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Saving application state to local data storage...");
            DataStore.getInstance().saveAllData();
        }));

        // Launch UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
