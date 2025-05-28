package nwes.mywebsite;

public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String additionalPassword;

    // Required: no-arg constructor
    public RegisterRequest() {}

    // Getters & Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAdditionalPassword() { return additionalPassword; }
    public void setAdditionalPassword(String additionalPassword) { this.additionalPassword = additionalPassword; }
}
