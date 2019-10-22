package pl.sip.dao;

import pl.sip.dto.LoginUser;
import pl.sip.dto.NewUser;

public interface UserDAO {
    void createUser(NewUser user);
    boolean loginUser(LoginUser user);
    int checkUserPrivilege(LoginUser user);
}
