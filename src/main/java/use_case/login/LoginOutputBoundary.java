package use_case.login;

public interface LoginOutputBoundary {

  void prepareSuccessView(LoginOutputData outputData);

  void prepareValidationExceptionView();
}
