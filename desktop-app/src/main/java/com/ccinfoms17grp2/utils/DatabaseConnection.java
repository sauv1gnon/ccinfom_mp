package com.ccinfoms17grp2.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central place for obtaining JDBC connections using the configuration under {@code db.properties}.
 */
public final class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String PROPERTIES_FILE = "/db.properties";
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = DatabaseConnection.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Could not load " + PROPERTIES_FILE + " from classpath");
            }
            PROPERTIES.load(in);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError("Failed to read database configuration: " + ex.getMessage());
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        // final String url = Objects.requireNonNull(PROPERTIES.getProperty("db.url"), "db.url property is required");
        // final String username = PROPERTIES.getProperty("db.user", "");
        // final String password = PROPERTIES.getProperty("db.password", "");
        // TODO: migrate to staging
        final String url = "http://localhost:3306/primary_db";
        final String username = "root";
        final String password = "dudewtf12345";
        LOGGER.log(Level.FINE, () -> "Opening JDBC connection to " + url);
        return DriverManager.getConnection(url, username, password);
    }

    public static String getDatabaseName() {
        return PROPERTIES.getProperty("db.name", "queue_system");
    }
}
