package data_access;

import entity.Stock;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InMemoryStockDataAccessObject implements StockDataAccessInterface {

    private final Map<String, Stock> stocks;

    public InMemoryStockDataAccessObject() {
        this.stocks = new java.util.HashMap<>();

        // Initialize stocks in the constructor with 30 entries
        stocks.put("AAPL", new Stock("AAPL", "Apple Inc.", "Technology", 150.0));
        stocks.put("GOOGL", new Stock("GOOGL", "Alphabet Inc.", "Technology", 2800.0));
        stocks.put("MSFT", new Stock("MSFT", "Microsoft Corporation", "Technology", 320.5));
        stocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", "E-Commerce", 3200.0));
        stocks.put("META", new Stock("META", "Meta Platforms Inc.", "Technology", 330.45));
        stocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", "Automotive", 900.0));
        stocks.put("NFLX", new Stock("NFLX", "Netflix Inc.", "Entertainment", 550.2));
        stocks.put("NVDA", new Stock("NVDA", "NVIDIA Corporation", "Semiconductors", 600.0));
        stocks.put("ADBE", new Stock("ADBE", "Adobe Inc.", "Software", 500.0));
        stocks.put("ORCL", new Stock("ORCL", "Oracle Corporation", "Software", 90.0));
        stocks.put("INTC", new Stock("INTC", "Intel Corporation", "Semiconductors", 55.0));
        stocks.put("CSCO", new Stock("CSCO", "Cisco Systems", "Networking", 45.0));
        stocks.put("BABA", new Stock("BABA", "Alibaba Group", "E-Commerce", 120.0));
        stocks.put("SAP", new Stock("SAP", "SAP SE", "Software", 140.0));
        stocks.put("IBM", new Stock("IBM", "International Business Machines", "Technology", 130.0));
        stocks.put("PYPL", new Stock("PYPL", "PayPal Holdings", "Fintech", 190.0));
        stocks.put("SQ", new Stock("SQ", "Block Inc.", "Fintech", 75.0));
        stocks.put("SHOP", new Stock("SHOP", "Shopify Inc.", "E-Commerce", 1300.0));
        stocks.put("CRM", new Stock("CRM", "Salesforce Inc.", "Software", 250.0));
        stocks.put("UBER", new Stock("UBER", "Uber Technologies", "Transportation", 45.0));
        stocks.put("LYFT", new Stock("LYFT", "Lyft Inc.", "Transportation", 12.0));
        stocks.put("TWTR", new Stock("TWTR", "Twitter Inc.", "Social Media", 54.2));
        stocks.put("SNAP", new Stock("SNAP", "Snap Inc.", "Social Media", 12.3));
        stocks.put("PINS", new Stock("PINS", "Pinterest Inc.", "Social Media", 23.4));
        stocks.put("ZM", new Stock("ZM", "Zoom Video Communications", "Software", 100.0));
        stocks.put("DOCU", new Stock("DOCU", "DocuSign Inc.", "Software", 75.0));
        stocks.put("ASML", new Stock("ASML", "ASML Holding", "Semiconductors", 750.0));
        stocks.put("AMD", new Stock("AMD", "Advanced Micro Devices", "Semiconductors", 110.0));
        stocks.put("INTU", new Stock("INTU", "Intuit Inc.", "Software", 400.0));
        stocks.put("SPOT", new Stock("SPOT", "Spotify Technology", "Entertainment", 250.0));
    }

    @Override
    public Map<String, Stock> getStocks() {
        // Return an unmodifiable view of the stocks map to prevent external modifications
        return Collections.unmodifiableMap(stocks);
    }

    /**
     * Get updated market price for all stocks
     *
     * @return a hashmap with the stock ticker as the key and the updated market price as the value.
     */
    @Override
    public Map<String, Double> getUpdatedPrices() {
        Map<String, Double> updatedPrices = new HashMap<>();

        // Simulate price changes by randomly adjusting current prices within a small range
        for (Stock stock : stocks.values()) {
            double currentPrice = stock.getMarketPrice();
            // Random price change between -2% and +2%
            double priceChange = currentPrice * (Math.random() * 0.04 - 0.02);
            double newPrice = Math.max(0.01, currentPrice + priceChange); // Ensure price doesn't go below $0.01
            stock.updatePrice(newPrice); // Update the stock's price
            updatedPrices.put(stock.getTicker(), newPrice);
        }

        return Collections.unmodifiableMap(updatedPrices);
    }
}
