package pl.sip.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LoginUser {
    @NotNull
    @Size(min = 3)
    private String userName;
    @NotNull
    private String userPassword;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

}
