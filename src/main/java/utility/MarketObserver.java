package utility;

import data_access.UserDataAccessInterface;
import entity.User;
import utility.exceptions.ValidationException;
import view.ViewManager;
import view.view_events.UpdateAssetEvent;

public class MarketObserver {

    private static volatile MarketObserver instance = null;
    private boolean initialized = false;
    private UserDataAccessInterface dataAccess;

    private MarketObserver() {}

    public static MarketObserver Instance() {
        if (instance == null) {
            synchronized (MarketObserver.class) {
                if (instance == null) {
                    instance = new MarketObserver();
                }
            }
        }
        return instance;
    }

    public synchronized void initialize(UserDataAccessInterface dataAccess) {
        if (this.initialized) {
            throw new IllegalStateException("MarketTracker is already initialized.");
        }
        this.dataAccess = dataAccess;
        this.initialized = true;
    }

    public void onMarketUpdate() {
        try {
            User user = dataAccess.getUserWithCredential(
                    ClientSessionManager.Instance().getCredential());

            // Update portfolio stock prices from MarketTracker
            user.getPortfolio().getAllUserStocks().forEach(userStock -> {
                MarketTracker.Instance()
                        .getStock(userStock.getStock().getTicker())
                        .ifPresent(updatedStock -> {
                            userStock.getStock().updatePrice(updatedStock.getMarketPrice());
                        });
            });

            try {
                // Save updated portfolio back to database
                dataAccess.updateUserData(user);
            } catch (java.rmi.ServerException e) {
                System.err.println("Failed to update user data: " + e.getMessage());
            }

            System.out.println("Current user: " + user.getUsername());
            ViewManager.Instance().broadcastEvent(new UpdateAssetEvent(user.getPortfolio(), user.getBalance()));
        } catch (ValidationException e) {
            System.out.println("Failed to find current user.");
        }
    }
}
