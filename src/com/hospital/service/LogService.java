package com.hospital.service;

import com.hospital.model.SystemLog;
import com.hospital.repository.DataStore;

import java.util.ArrayList;
import java.util.List;

public class LogService {
    private final DataStore dataStore = DataStore.getInstance();

    public List<SystemLog> getLogs() {
        return new ArrayList<>(dataStore.getLogs());
    }

    public void log(String username, String role, String action, String details) {
        dataStore.addLog(username, role, action, details);
    }
}
