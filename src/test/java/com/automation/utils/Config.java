package com.automation.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public final class Config {

    private static final String ENVIRONMENT = System.getProperty(
            "env", System.getenv().getOrDefault("TEST_ENV", "dev")
    ).toLowerCase(Locale.ROOT);
    private static final Properties PROFILE = loadProfile();

    private Config() {
    }

    public static String value(String name, String defaultValue) {
        String systemProperty = System.getProperty(name);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        String environmentValue = System.getenv(name.toUpperCase(Locale.ROOT).replace('.', '_')
                + "_" + ENVIRONMENT.toUpperCase(Locale.ROOT));
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        return PROFILE.getProperty(name, defaultValue).trim();
    }

    public static String registrationUrl() {
        return value("registration.url", "");
    }

    public static String environment() {
        return ENVIRONMENT;
    }

    private static Properties loadProfile() {
        Properties properties = new Properties();
        String resource = "config/" + ENVIRONMENT + ".properties";
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Configuration profile not found: " + resource);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read configuration profile: " + resource, exception);
        }
    }
}
