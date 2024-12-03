package data_access;

import entity.User;
import java.rmi.ServerException;
import utility.exceptions.ValidationException;

public interface UserDataAccessInterface {

    User getUserWithCredential(String credential) throws ValidationException;

    void updateUserData(User user) throws ServerException;
}
