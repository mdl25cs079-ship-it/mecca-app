package com.mecca.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Singleton that owns the single shared JDBC connection to mecca.db (SQLite).
 * OWNER: M1 ONLY. Everyone else: DatabaseConnection.getInstance().getConnection()
 */
public final class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:mecca.db";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            connect();
            runSchema();
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Could not initialise database: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database connection lost: " + e.getMessage(), e);
        }
        return connection;
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection(URL);
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("PRAGMA busy_timeout = 5000"); // 5 seconds
        }
    }

    /** Runs schema.sql (all statements are CREATE TABLE IF NOT EXISTS, so it is safe every start). */
    private void runSchema() throws IOException, SQLException {
        try (InputStream in = DatabaseConnection.class.getResourceAsStream("/schema.sql")) {
            if (in == null) {
                throw new IOException("schema.sql not found on classpath");
            }
            String sql;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                sql = r.lines().filter(l -> !l.trim().startsWith("--")).collect(Collectors.joining("\n"));
            }
            try (Statement st = connection.createStatement()) {
                for (String part : sql.split(";")) {
                    if (!part.trim().isEmpty()) {
                        st.execute(part.trim());
                    }
                }
            }
        }
    }
}
