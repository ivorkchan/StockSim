package utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for loading and accessing configuration properties.
 *
 * <p>Configuration is dynamically loaded from properties files under resources/config.
 */
public class ConfigLoader {

    // Cache to store loaded configurations

    private static final Map<String, Properties> configCache = new ConcurrentHashMap<>();

    /**
     * Loads the configuration file dynamically.
     *
     * @param configFilePath The relative path of the configuration file (e.g., "config/market-tracker-config.txt").
     * @throws RuntimeException if the configuration file is not found or cannot be loaded.
     */
    public static void loadConfig(String configFilePath) {

        if (configCache.containsKey(configFilePath)) {

            return; // Already loaded
        }

        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(configFilePath)) {

            if (input == null) {

                throw new RuntimeException("Unable to find configuration file: " + configFilePath);
            }

            Properties properties = new Properties();

            properties.load(input);

            configCache.put(configFilePath, properties);

        } catch (IOException e) {

            throw new RuntimeException("Error loading configuration file: " + configFilePath, e);
        }
    }

    /**
     * Retrieves the value of a property from a loaded configuration.
     *
     * @param configFilePath The relative path of the configuration file (e.g., "config/market-tracker-config.txt").
     * @param key The property key to retrieve.
     * @return The value of the property, or {@code null} if the key does not exist.
     * @throws RuntimeException if the configuration file has not been loaded yet.
     */
    public static String getProperty(String configFilePath, String key) {

        Properties properties = configCache.get(configFilePath);

        if (properties == null) {

            throw new RuntimeException("Configuration file not loaded: " + configFilePath);
        }

        return properties.getProperty(key);
    }

    /**
     * Checks if a configuration file is loaded.
     *
     * @param configFilePath The relative path of the configuration file.
     * @return {@code true} if the configuration file is loaded, {@code false} otherwise.
     */
    public static boolean isConfigLoaded(String configFilePath) {

        return configCache.containsKey(configFilePath);
    }

    static {
        ConfigLoader.loadConfig("config/market-tracker-config.txt");

        ConfigLoader.loadConfig("config/database-config.txt");
    }
}
