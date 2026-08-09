package com.automation.utils;

// =================================================================================================
// Imports for reading Java .properties files and handling locale-sensitive environment
// variable names. Properties is a key/value store; InputStream reads the file from the
// classpath; Locale is used to keep env-var lookups consistent across machines.
// =================================================================================================
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Centralized configuration loader for the automation framework.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Detect which environment the suite is running against
 *       ({@code dev} by default, overridable via {@code -Denv=...} or the
 *       {@code TEST_ENV} environment variable).</li>
 *   <li>Load the matching {@code config/<env>.properties} file from the classpath.</li>
 *   <li>Resolve individual configuration values using a clear precedence order so that
 *       CI pipelines and developers can override values without editing tracked files.</li>
 * </ul>
 *
 * <p>The class is {@code final} and its constructor is private: it is a pure utility
 * class and cannot be instantiated.
 */
public final class Config {

    /**
     * Active environment name (e.g. {@code "dev"}, {@code "prod"}).
     * Resolution order: {@code -Denv=...} system property, then {@code TEST_ENV}
     * environment variable, then {@code "dev"} as the safe default. The value is
     * lower-cased so file lookups stay case-insensitive.
     */
    private static final String ENVIRONMENT = System.getProperty(
            "env", System.getenv().getOrDefault("TEST_ENV", "dev")
    ).toLowerCase(Locale.ROOT);

    /** In-memory cache of all keys loaded from {@code config/<env>.properties}. */
    private static final Properties PROFILE = loadProfile();

    /** Private constructor — utility class, do not instantiate. */
    private Config() {
    }

    /**
     * Resolves a configuration value using the following precedence (highest first):
     * <ol>
     *   <li>JVM system property, e.g. {@code -Dregistration.url=https://...}</li>
     *   <li>Environment variable named {@code <NAME>_<ENV>} where {@code <NAME>} is the
     *       upper-cased key with dots replaced by underscores, e.g. {@code REGISTRATION_URL_DEV}.</li>
     *   <li>The matching key inside {@code config/<env>.properties}.</li>
     *   <li>The supplied {@code defaultValue} if none of the above produced a value.</li>
     * </ol>
     *
     * @param name         key to look up (e.g. {@code "registration.url"}).
     * @param defaultValue fallback used when the key is not configured anywhere.
     * @return trimmed value to use for the requested key.
     */
    public static String value(String name, String defaultValue) {
        // 1. Highest priority: explicit JVM system property (passed with -Dkey=value).
        String systemProperty = System.getProperty(name);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        // 2. Second priority: environment variable specific to the active profile.
        //    Example: key "registration.url" + env "dev" -> "REGISTRATION_URL_DEV".
        String environmentValue = System.getenv(name.toUpperCase(Locale.ROOT).replace('.', '_')
                + "_" + ENVIRONMENT.toUpperCase(Locale.ROOT));
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }

        // 3. Final fallback: value stored in the profile file, or the hard-coded default.
        return PROFILE.getProperty(name, defaultValue).trim();
    }

    /**
     * Convenience accessor for the application-under-test registration URL.
     *
     * @return registration page URL or an empty string if none is configured.
     */
    public static String registrationUrl() {
        return value("registration.url", "");
    }

    /**
     * @return the environment name the suite is currently configured for
     *         ({@code "dev"}, {@code "prod"}, ...).
     */
    public static String environment() {
        return ENVIRONMENT;
    }

    /**
     * Loads {@code config/<env>.properties} from the test classpath at class-load time.
     * Throws {@link IllegalStateException} with a clear message if the file is missing
     * or unreadable — failing fast beats running a broken suite.
     */
    private static Properties loadProfile() {
        Properties properties = new Properties();
        String resource = "config/" + ENVIRONMENT + ".properties";
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream(resource)) {
            // Null check protects against typos in the environment name.
            if (input == null) {
                throw new IllegalStateException("Configuration profile not found: " + resource);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            // Wrap IOException so the framework always raises the same exception type.
            throw new IllegalStateException("Unable to read configuration profile: " + resource, exception);
        }
    }
}
