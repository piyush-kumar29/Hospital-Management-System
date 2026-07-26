package com.hospital.service;

import com.hospital.model.User;
import com.hospital.repository.DataStore;

import java.util.Optional;

public class AuthService {
    private final DataStore dataStore = DataStore.getInstance();
    private User currentUser;

    public Optional<User> login(String username, String password) {
        for (User u : dataStore.getUsers()) {
            if (u.getUsername().equalsIgnoreCase(username.trim()) && u.getPassword().equals(password)) {
                currentUser = u;
                dataStore.addLog(u.getUsername(), u.getRole().name(), "LOGIN", "User logged in successfully.");
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    public void logout() {
        if (currentUser != null) {
            dataStore.addLog(currentUser.getUsername(), currentUser.getRole().name(), "LOGOUT", "User logged out.");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
