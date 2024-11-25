package utility;

public class ClientSessionManager {

  // singleton instance
  private static volatile ClientSessionManager instance;

  // credential for the session
  private String credential;

  // singleton constructor
  private ClientSessionManager() {}

  public static synchronized ClientSessionManager Instance() {
    if (instance == null) {
      instance = new ClientSessionManager();
    }
    return instance;
  }

  public String getCredential() {
    return credential;
  }

  public void setCredential(String credential) {
    this.credential = credential;
  }
}
