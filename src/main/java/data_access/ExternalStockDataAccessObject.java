package data_access;

import entity.Stock;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import utility.ServiceManager;
import utility.exceptions.RateLimitExceededException;

/** A DataAccessObject that retrieves real time stock data */
public class ExternalStockDataAccessObject implements StockDataAccessInterface {

    private static final String BASE_URL = "https://finnhub.io/api/v1";
    private static final String TICKERS_FILE = "/config/tickers.txt";
    // Rate limit exceeded error code provided by Finnhub documentation
    private static final int LIMIT_EXCEED_ERROR_CODE = 429;

    private final OkHttpClient client;
    private final String apiKey;
    private final List<String> tickers;

    public ExternalStockDataAccessObject() {
        this.client = new OkHttpClient();
        this.tickers = new ArrayList<>();

        // Load .env.local file and get API token
        Dotenv dotenv = Dotenv.configure().filename(".env.local").load();
        this.apiKey = dotenv.get("STOCK_API_KEY");

        // Fetch ticker data from resource file and store in list
        try (InputStream inputStream = getClass().getResourceAsStream(TICKERS_FILE)) {
            if (inputStream == null) {
                throw new RuntimeException("Unable to find configuration file: " + TICKERS_FILE);
            } else {
                // Reads content in config/tickers text file
                try (Scanner scanner = new Scanner(inputStream)) {
                    while (scanner.hasNextLine()) {
                        String ticker = scanner.nextLine().trim();
                        // Stores ticker in list containing all tickers
                        tickers.add(ticker);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading configuration file", e);
        }

        ServiceManager.Instance().registerService(StockDataAccessInterface.class, this);
    }

    /**
     * Get all stock information
     *
     * @return a hashmap with the stock ticker as the key and the Stock entity as the value. It should contain all
     *     stocks in the database.
     */
    @Override
    public Map<String, Stock> getStocks() throws RateLimitExceededException {
        System.out.println("Fetching all stocks data...");
        HashMap<String, Stock> stocks = new HashMap<>();

        for (String ticker : tickers) {
            System.out.println("\nProcessing ticker: " + ticker);
            // Information to create a new default stock
            String company = "Unknown Company Name";
            String industry = "Unknown Industry";

            try {
                // Retrieves ticker current market price
                System.out.println("Getting market price for " + ticker);
                double price = getMarketPrice(ticker);

                // Profile2 api call to get company name and industry
                System.out.println("Getting company profile for " + ticker);

                // Creates new request with Profile2 url
                String profileUrl = String.format("%s/stock/profile2?symbol=%s&token=%s", BASE_URL, ticker, apiKey);
                Request profileRequest = new Request.Builder().url(profileUrl).build();

                System.out.println("Making API call to: " + profileUrl.replace(apiKey, "API_KEY_HIDDEN"));

                // Initiates API call request
                try (Response profileResponse = client.newCall(profileRequest).execute()) {
                    System.out.println("Profile response code: " + profileResponse.code());

                    if (profileResponse.isSuccessful()) {
                        String responseBody = profileResponse.body().string();
                        System.out.println("Profile response body: " + responseBody);

                        JSONObject jsonObject = new JSONObject(responseBody);
                        company = jsonObject.optString("name", company);
                        industry = jsonObject.optString("finnhubIndustry", industry);

                        System.out.println(
                                "Successfully fetched profile for " + ticker + ": " + company + " (" + industry + ")");
                    } else if (profileResponse.code() == LIMIT_EXCEED_ERROR_CODE) {
                        System.err.println("Rate limit exceeded while fetching profile for: " + ticker);
                        throw new RateLimitExceededException();
                    } else {
                        System.err.println("Failed to fetch profile for " + ticker);
                        System.err.println("Response code: " + profileResponse.code());
                        System.err.println(
                                "Response body: " + profileResponse.body().string());
                    }
                }

                // Check if the stock already exists in the map
                if (stocks.containsKey(ticker)) {
                    System.out.println("Updating existing stock: " + ticker);
                    stocks.get(ticker).updatePrice(price);
                } else {
                    System.out.println("Creating new stock: " + ticker);
                    Stock stock = new Stock(ticker, company, industry, price);
                    stocks.put(ticker, stock);
                }

            } catch (IOException e) {
                System.err.println("IOException while processing " + ticker);
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected error while processing " + ticker);
                e.printStackTrace();
            }
        }

        System.out.println("\nFinished fetching all stocks. Total stocks: " + stocks.size());
        return Collections.unmodifiableMap(stocks);
    }

    /**
     * Get updated market price for all stocks
     *
     * @return a hashmap with the stock ticker as the key and the updated market price as the value.
     */
    @Override
    public Map<String, Double> getUpdatedPrices() throws RateLimitExceededException {
        HashMap<String, Double> updatedPrices = new HashMap<>();

        // Calls helper function to get current market price for each ticker and stores it in updatedPrice
        for (String ticker : tickers) {
            updatedPrices.put(ticker, getMarketPrice(ticker));
        }

        // Return an unmodifiable view of the updated prices map to prevent external modifications
        return Collections.unmodifiableMap(updatedPrices);
    }

    /**
     * Helper function to retrieve the current market price of the given ticker
     *
     * @param ticker the stock ticker
     * @return the updated price
     */
    private double getMarketPrice(String ticker) throws RateLimitExceededException {
        System.out.println("Fetching market price for ticker: " + ticker);

        // Quote api call to get current market price
        try {
            // Creates new request with Quote url
            String quoteUrl = String.format("%s/quote?symbol=%s&token=%s", BASE_URL, ticker, apiKey);
            Request quoteRequest = new Request.Builder().url(quoteUrl).build();

            System.out.println("Making API call to: " + quoteUrl.replace(apiKey, "API_KEY_HIDDEN"));

            // Initiates API call request
            try (Response quoteResponse = client.newCall(quoteRequest).execute()) {
                System.out.println("Response code: " + quoteResponse.code());

                if (quoteResponse.isSuccessful()) {
                    String responseBody = quoteResponse.body().string();
                    System.out.println("Response body: " + responseBody);

                    JSONObject jsonObject = new JSONObject(responseBody);
                    double price = jsonObject.getDouble("c");
                    System.out.println("Successfully fetched price for " + ticker + ": $" + price);
                    return price;
                } else if (quoteResponse.code() == LIMIT_EXCEED_ERROR_CODE) {
                    System.err.println("Rate limit exceeded for ticker: " + ticker);
                    throw new RateLimitExceededException();
                } else {
                    System.err.println("Failed to fetch quote data for ticker: " + ticker);
                    System.err.println("Response code: " + quoteResponse.code());
                    System.err.println("Response body: " + quoteResponse.body().string());
                }
            }
        } catch (IOException e) {
            System.err.println("IOException while fetching price for " + ticker);
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error while fetching price for " + ticker);
            e.printStackTrace();
        }

        System.out.println("Returning default price (0.0) for " + ticker);
        return 0.0;
    }
}
