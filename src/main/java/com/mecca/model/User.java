// src/main/java/com/mecca/model/User.java
package com.mecca.model;

import com.mecca.service.AuthService;

/** Abstract base class for all users. Owner: M2. */
public abstract class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String role;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    /** Marks this user as the logged-in user. */
    public void login() { AuthService.setCurrentUser(this); }

    /** Logs this user out. */
    public void logout() { AuthService.logout(); }

    /** Role-specific dashboard behaviour (polymorphism). */
    public abstract void openDashboard();
}