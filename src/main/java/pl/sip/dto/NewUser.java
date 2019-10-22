package pl.sip.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class NewUser {
    @NotNull
    @Size(min = 3, max = 254)
    private String storeLabel;
    @NotNull
    @Size(min = 3, max = 254)
    private String userName;
    @NotNull
    @Size(min = 3, max = 254)
    private String firstName;
    @NotNull
    @Size(min = 3, max = 254)
    private String lastName;
    @NotNull
    @Size(min = 5, max = 254)
    private String email;
    @NotNull
    @Size(min = 6, max = 254)
    private String password;
    @NotNull
    @Size(min = 6, max = 254)
    private String confirmPassword;

    public String getStoreLabel() {
        return storeLabel;
    }

    public void setStoreLabel(String storeLabel) {
        this.storeLabel = storeLabel;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

}
