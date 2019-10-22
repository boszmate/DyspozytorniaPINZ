package pl.sip.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.sip.dao.UserDAO;
import pl.sip.dto.LoginUser;
import pl.sip.dto.NewUser;

@Service
public class UserServiceImpl implements UserService {

    private UserDAO userDAO;

    @Autowired
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void createUser(NewUser user) {
        this.userDAO.createUser(user);
    }

    public boolean loginUser(LoginUser user) {
        return this.userDAO.loginUser(user);
    }

    public int checkUserPrivilege(LoginUser user) {
        return this.userDAO.checkUserPrivilege(user);
    }
}
