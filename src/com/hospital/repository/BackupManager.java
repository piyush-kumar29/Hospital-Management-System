package com.hospital.repository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class BackupManager {
    public static void backup(File file) {
        if (!file.exists()) {
            return;
        }
        try {
            Path source = file.toPath();
            Path backup = Paths.get(file.getAbsolutePath() + ".bak");
            Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("Failed to create backup for " + file.getName() + ": " + e.getMessage());
        }
    }
}
