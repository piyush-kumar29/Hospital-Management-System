import com.hospital.repository.DataStore;
import com.hospital.service.AuthService;
import com.hospital.ui.LoginFrame;
import com.hospital.ui.DashboardFrame;
import com.hospital.model.User;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

public class ScreenshotGenerator {
    public static void main(String[] args) throws Exception {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equalsIgnoreCase(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}

        // Initialize data store
        DataStore.getInstance();
        AuthService authService = new AuthService();
        
        // 1. Capture Login Frame
        LoginFrame loginFrame = new LoginFrame();
        // Force the size to match the preferred size
        loginFrame.setSize(850, 560);
        loginFrame.setVisible(true);
        Thread.sleep(1500); // Wait for UI to render
        
        BufferedImage loginImg = new BufferedImage(loginFrame.getWidth(), loginFrame.getHeight(), BufferedImage.TYPE_INT_RGB);
        loginFrame.paint(loginImg.getGraphics());
        
        File loginFile = new File("Images/screenshots/Login.png");
        loginFile.getParentFile().mkdirs();
        ImageIO.write(loginImg, "png", loginFile);
        loginFrame.dispose();

        // 2. Capture Patient Dashboard
        Optional<User> patientOpt = authService.login("patient", "patient123");
        if (patientOpt.isPresent()) {
            DashboardFrame dashboardFrame = new DashboardFrame(patientOpt.get(), authService);
            dashboardFrame.setExtendedState(JFrame.NORMAL);
            dashboardFrame.setSize(1280, 800);
            dashboardFrame.setVisible(true);
            Thread.sleep(1500); // Wait for UI to render

            BufferedImage patientImg = new BufferedImage(dashboardFrame.getWidth(), dashboardFrame.getHeight(), BufferedImage.TYPE_INT_RGB);
            dashboardFrame.paint(patientImg.getGraphics());
            
            ImageIO.write(patientImg, "png", new File("Images/screenshots/Patient.png"));
            dashboardFrame.dispose();
            System.out.println("Patient screenshot generated successfully!");
        } else {
            System.out.println("Could not log in as patient!");
        }

        System.exit(0);
    }
}
