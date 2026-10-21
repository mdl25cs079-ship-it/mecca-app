package com.mecca.util;

import com.mecca.model.Admin;
import com.mecca.model.Organizer;
import com.mecca.model.Student;
import com.mecca.model.User;

/** Factory pattern: builds the right User subclass for a role. Owner: M2. */
public final class UserFactory {

    private UserFactory() { }

    /** role must be STUDENT, ORGANIZER or ADMIN (case-insensitive). */
    public static User createUser(String role) {
        if (role == null) {
            throw new IllegalArgumentException("Role is required");
        }
        User user;
        switch (role.trim().toUpperCase()) {
            case "STUDENT":   user = new Student();   break;
            case "ORGANIZER": user = new Organizer(); break;
            case "ADMIN":     user = new Admin();     break;
            default: throw new IllegalArgumentException("Unknown role: " + role);
        }
        user.setRole(role.trim().toUpperCase());
        return user;
    }
}
