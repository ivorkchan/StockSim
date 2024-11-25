package utility;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages all the service instances for global access
 * e.g. controllers, presenters, and interactors
 * Implements the Singleton pattern to ensure only one instance exists
 */
public class ServiceManager {
    // Private static instance of the class
    private static volatile ServiceManager instance;

    // Private map to store services
    private final Map<Class<?>, Object> services;

    // Private constructor to prevent instantiation
    private ServiceManager() {
        services = new HashMap<>();
    }

    /**
     * Gets the singleton instance of ServiceManager
     * Uses double-checked locking for thread safety
     *
     * @return The singleton instance
     */
    public static ServiceManager Instance() {
        if (instance == null) {
            synchronized (ServiceManager.class) {
                if (instance == null) {
                    instance = new ServiceManager();
                }
            }
        }
        return instance;
    }

    /**
     * Registers a service instance for the given type
     *
     * @param key     The interface or class type of the service
     * @param service The service instance
     * @param <T>     The type of the service
     */
    public <T> void registerService(Class<T> key, T service) {
        services.put(key, service);
    }

    /**
     * Retrieves a service instance for the given type
     *
     * @param key The interface or class type of the service to retrieve
     * @param <T> The type of the service
     * @return The service instance
     */
    public <T> T getService(Class<T> key) {
        return key.cast(services.get(key));
    }
}
