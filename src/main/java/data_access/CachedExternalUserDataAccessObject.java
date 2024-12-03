package data_access;

import data_transfer.cache.UserCache;
import data_transfer.parser.DocumentParsingException;
import entity.User;
import java.rmi.ServerException;
import java.util.concurrent.CompletableFuture;
import use_case.execute_buy.ExecuteBuyDataAccessInterface;
import use_case.execute_sell.ExecuteSellDataAccessInterface;
import use_case.login.LoginDataAccessInterface;
import use_case.registration.RegistrationDataAccessInterface;
import use_case.view_history.ViewHistoryDataAccessInterface;
import utility.ConfigLoader;
import utility.ServiceManager;
import utility.SessionManager;
import utility.exceptions.ValidationException;

public class CachedExternalUserDataAccessObject
        implements RegistrationDataAccessInterface,
                LoginDataAccessInterface,
                ExecuteBuyDataAccessInterface,
                ExecuteSellDataAccessInterface,
                ViewHistoryDataAccessInterface {

    private static final int USER_CACHE_MAX_CAPACITY =
            Integer.parseInt(ConfigLoader.getProperty("config/database-config.txt", "USER_CACHE_MAX_CAPACITY"));

    private final UserCache cachedUsers = new UserCache(USER_CACHE_MAX_CAPACITY);
    private final ExternalUserDataAccessObject database;

    public CachedExternalUserDataAccessObject() {
        database = new ExternalUserDataAccessObject();
        ServiceManager.Instance().registerService(CachedExternalUserDataAccessObject.class, this);
        ServiceManager.Instance().registerService(RegistrationDataAccessInterface.class, this);
        ServiceManager.Instance().registerService(LoginDataAccessInterface.class, this);
        ServiceManager.Instance().registerService(ExecuteBuyDataAccessInterface.class, this);
        ServiceManager.Instance().registerService(ExecuteSellDataAccessInterface.class, this);
        ServiceManager.Instance().registerService(ViewHistoryDataAccessInterface.class, this);
    }

    @Override
    public User getUserWithCredential(String credential) throws ValidationException {
        // get username from credential
        String username = SessionManager.Instance().getUsername(credential).orElseThrow(ValidationException::new);
        // search user from cache
        User user = cachedUsers.get(username);
        // search user from database
        if (user == null) {
            user = database.getUserWithCredential(credential);
            cachedUsers.add(user);
        }
        return user;
    }

    @Override
    public synchronized void updateUserData(User user) throws ServerException {
        // Update cache first to ensure immediate consistency
        cachedUsers.updateUser(user);

        try {
            // Update database synchronously to ensure consistency
            database.updateUserData(user);
        } catch (ServerException e) {
            // If database update fails, invalidate the cache and propagate the error
            cachedUsers.remove(user.getUsername());
            throw e;
        }
    }

    @Override
    public User getUserWithPassword(String username, String password) throws ValidationException {
        // search user from cache
        User user = cachedUsers.get(username);
        // search user from database
        if (user == null) {
            user = database.getUserWithPassword(username, password);
            cachedUsers.add(user);
        }
        return user;
    }

    @Override
    public boolean hasUsername(String username) {
        return cachedUsers.get(username) != null || database.hasUsername(username);
    }

    @Override
    public void createUser(User user) throws DocumentParsingException {
        // Asynchronously write data to the database
        CompletableFuture.runAsync(() -> {
                    try {
                        // Call the database method to update user data
                        database.createUser(user);
                        cachedUsers.add(user);
                    } catch (Exception e) {
                        // Handle any exceptions that might occur during database update
                        throw new RuntimeException("Failed to update user data", e);
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("Error occurred while updating user data: " + ex.getMessage());
                    ex.printStackTrace();
                    return null;
                });
    }
}
