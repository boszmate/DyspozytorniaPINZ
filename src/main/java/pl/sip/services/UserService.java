package pl.sip.services;

import pl.sip.dto.LoginUser;
import pl.sip.dto.NewUser;

public interface UserService {
    void createUser(NewUser user);
    boolean loginUser(LoginUser user);
    int checkUserPrivilege(LoginUser user);
}
