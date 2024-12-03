package use_case.execute_sell;

import entity.*;
import java.rmi.ServerException;
import java.util.Date;
import utility.MarketTracker;
import utility.ServiceManager;
import utility.exceptions.ValidationException;

public class ExecuteSellInteractor implements ExecuteSellInputBoundary {
    private final ExecuteSellDataAccessInterface dataAccess;
    private final ExecuteSellOutputBoundary outputPresenter;

    public ExecuteSellInteractor(ExecuteSellDataAccessInterface dataAccess, ExecuteSellOutputBoundary outputPresenter) {
        this.dataAccess = dataAccess;
        this.outputPresenter = outputPresenter;
        ServiceManager.Instance().registerService(ExecuteSellInputBoundary.class, this);
    }

    @Override
    public void execute(ExecuteSellInputData data) {
        try {
            User currentUser = dataAccess.getUserWithCredential(data.credential());
            Stock stock = MarketTracker.Instance().getStock(data.ticker()).orElseThrow(StockNotFoundException::new);
            Portfolio portfolio = currentUser.getPortfolio();

            double currentPrice = stock.getMarketPrice();
            double totalValue = currentPrice * data.quantity();

            // Get current stock position if exists
            int currentQuantity = portfolio
                    .getUserStock(data.ticker())
                    .map(UserStock::getQuantity)
                    .orElse(0);

            // If user doesn't have the stock or doesn't have enough quantity
            // they need margin to short sell
            if (currentQuantity < data.quantity()) {
                if (currentUser.getBalance() < totalValue) {
                    throw new InsufficientMarginCallException();
                }
            }

            currentUser.addBalance(totalValue);
            portfolio.updatePortfolio(stock, -data.quantity(), currentPrice);

            Transaction transaction = new Transaction(new Date(), data.ticker(), data.quantity(), currentPrice, "SELL");
            currentUser.getTransactionHistory().addTransaction(transaction);

            dataAccess.updateUserData(currentUser);

            outputPresenter.prepareSuccessView(new ExecuteSellOutputData(
                    currentUser.getBalance(), currentUser.getPortfolio(), currentUser.getTransactionHistory()));
        } catch (ValidationException e) {
            outputPresenter.prepareValidationExceptionView();
        } catch (StockNotFoundException e) {
            outputPresenter.prepareStockNotFoundExceptionView();
        } catch (InsufficientMarginCallException e) {
            outputPresenter.prepareInsufficientMarginCallExceptionView();
        } catch (ServerException e) {
            outputPresenter.prepareServerErrorView();
        }
    }

    static class InsufficientMarginCallException extends Exception {}

    static class StockNotFoundException extends Exception {}
}
