package com.hospital.model;

import java.io.Serializable;

public class SystemLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private String timestamp;
    private String username;
    private String role;
    private String action;
    private String details;

    public SystemLog(String timestamp, String username, String role, String action, String details) {
        this.timestamp = timestamp;
        this.username = username;
        this.role = role;
        this.action = action;
        this.details = details;
    }

    public String getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
}
