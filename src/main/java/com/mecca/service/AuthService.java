package com.mecca.service;

import com.mecca.model.User;

import java.util.Optional;

/** STUB. Owner: M2. Keep method names/signatures EXACTLY. All methods are static. */
public class AuthService {
    private static User currentUser;

    public static Optional<User> login(String email, String password) { return Optional.empty(); } // TODO M2
    public static void logout() { currentUser = null; }
    public static User getCurrentUser() { return currentUser; }
    public static void setCurrentUser(User u) { currentUser = u; }
    public static String hashPassword(String plain) { return plain; } // TODO M2: SHA-256
}
