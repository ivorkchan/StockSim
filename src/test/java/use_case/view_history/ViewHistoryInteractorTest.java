package use_case.view_history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import entity.Transaction;
import entity.TransactionHistory;
import entity.User;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utility.exceptions.ValidationException;

class ViewHistoryInteractorTest {

    private ViewHistoryDataAccessInterface dataAccess;
    private ViewHistoryOutputBoundary outputPresenter;

    @BeforeEach
    void setUp() {
        // Create mock data access and presenter for ViewHistory
        dataAccess = Mockito.mock(ViewHistoryDataAccessInterface.class);
        outputPresenter = Mockito.mock(ViewHistoryOutputBoundary.class);
    }

    @Test
    void successTest() throws ValidationException {
        // Create a mockUser and it's mockCredential
        User mockUser = new User("testUser", "password");
        TransactionHistory mockUserTransactionHistory = mockUser.getTransactionHistory();
        when(dataAccess.getUserWithCredential("dummy")).thenReturn(mockUser);

        // Set up the interactor and input data for this mockUser
        ViewHistoryInputData inputData = new ViewHistoryInputData("dummy");
        ViewHistoryInteractor interactor = new ViewHistoryInteractor(dataAccess, outputPresenter);

        // Execute the interactor with the inputData from the mockUser
        interactor.execute(inputData);

        // Check if the view is correct
        verify(outputPresenter).prepareSuccessView(new ViewHistoryOutputData(mockUserTransactionHistory));

        // Check the initial empty state of TransactionHistory
        assertTrue(
                mockUserTransactionHistory.getTransactions().isEmpty(),
                "The transaction History of a new user should be empty");

        // Create a mock buy transaction
        Date mockBuyTimestamp = new Date(2024 - 1900, 11, 22, 13, 5, 55);
        Transaction mockBuyTransaction = new Transaction(mockBuyTimestamp, "XXXX", 10, 100.0, "BUY");
        mockUserTransactionHistory.addTransaction(mockBuyTransaction);

        // Check if the TransactionHistory contains the mock buy transaction
        assertEquals(
                List.of(mockBuyTransaction),
                mockUserTransactionHistory.getTransactions(),
                "Transaction History should contain the mockBuyTransaction");

        // Create a mock sell transaction
        Date mockSellTimestamp = new Date(2024 - 1900, 11, 23, 3, 5, 25);
        Transaction mockSellTransaction = new Transaction(mockSellTimestamp, "XXXX", 5, 100.0, "SELL");
        mockUserTransactionHistory.addTransaction(mockSellTransaction);

        // Check if the TransactionHistory contains both the mock buy and mock sell transaction
        List<Transaction> expectedTransactions = List.of(mockBuyTransaction, mockSellTransaction);
        assertEquals(
                expectedTransactions,
                mockUserTransactionHistory.getTransactions(),
                "Transaction History should contain both the mockBuyTransaction and the mockSellTransaction");
    }

    @Test
    void validationExceptionTest() throws ValidationException {
        // Simulate a validation error to throw a ValidationException
        when(dataAccess.getUserWithCredential("invalidUser")).thenThrow(new ValidationException());

        // Set up the interactor and input data for this ValidationException case
        ViewHistoryInputData inputData = new ViewHistoryInputData("invalidUser");
        ViewHistoryInteractor interactor = new ViewHistoryInteractor(dataAccess, outputPresenter);

        // Execute the interactor with the inputData
        interactor.execute(inputData);

        // Check if the view correctly handles the validation error
        verify(outputPresenter).prepareValidationExceptionView();
    }
}
