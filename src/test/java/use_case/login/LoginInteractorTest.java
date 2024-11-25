package use_case.login;

import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import utility.ClientSessionManager;
import utility.SessionManager;
import utility.exceptions.ValidationException;

import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

class LoginInteractorTest {

    private LoginDataAccessInterface dataAccess;
    private LoginOutputBoundary outputPresenter;
    private LoginInteractor interactor;
    private SessionManager sessionManagerMock;
    private ClientSessionManager clientSessionManagerMock;

    @BeforeEach
    void setUp() {
        dataAccess = mock(LoginDataAccessInterface.class);
        outputPresenter = mock(LoginOutputBoundary.class);
        sessionManagerMock = mock(SessionManager.class);
        clientSessionManagerMock = mock(ClientSessionManager.class);

        // Create a synchronous executor for testing
        Executor directExecutor = Runnable::run;
        interactor = new LoginInteractor(dataAccess, outputPresenter);
    }

    @Test
    void successfulLoginTest() throws Exception {
        // Arrange
        User mockUser = new User("testUser", "password");
        String credential = "session123";

        when(dataAccess.getUserWithPassword("testUser", "password")).thenReturn(mockUser);

        try (MockedStatic<SessionManager> sessionManagerMockedStatic = Mockito.mockStatic(SessionManager.class);
             MockedStatic<ClientSessionManager> clientSessionManagerMockedStatic = Mockito.mockStatic(ClientSessionManager.class)) {

            sessionManagerMockedStatic.when(SessionManager::Instance).thenReturn(sessionManagerMock);
            clientSessionManagerMockedStatic.when(ClientSessionManager::Instance).thenReturn(clientSessionManagerMock);
            when(sessionManagerMock.createSession("testUser")).thenReturn(credential);

            // Act
            LoginInputData inputData = new LoginInputData("testUser", "password");
            interactor.execute(inputData);

            // Assert
            verify(outputPresenter).prepareSuccessView(argThat(output ->
                    output.user().getUsername().equals(mockUser.getUsername())
            ));
            verify(clientSessionManagerMock).setCredential(credential);
        }
    }

    @Test
    void validationErrorTest() throws Exception {
        // Arrange
        when(dataAccess.getUserWithPassword("invalidUser", "wrongPassword"))
                .thenThrow(new ValidationException());

        try (MockedStatic<SessionManager> sessionManagerMockedStatic = Mockito.mockStatic(SessionManager.class);
             MockedStatic<ClientSessionManager> clientSessionManagerMockedStatic = Mockito.mockStatic(ClientSessionManager.class)) {

            sessionManagerMockedStatic.when(SessionManager::Instance).thenReturn(sessionManagerMock);
            clientSessionManagerMockedStatic.when(ClientSessionManager::Instance).thenReturn(clientSessionManagerMock);

            // Act
            LoginInputData inputData = new LoginInputData("invalidUser", "wrongPassword");
            interactor.execute(inputData);

            // Assert
            verify(outputPresenter).prepareValidationExceptionView();
            verifyNoInteractions(clientSessionManagerMock);
        }
    }
}