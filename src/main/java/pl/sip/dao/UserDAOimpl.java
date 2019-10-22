package pl.sip.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import pl.sip.dto.LoginUser;
import pl.sip.dto.NewUser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserDAOimpl implements UserDAO {

    private final DataSource dataSource;

    @Autowired
    public UserDAOimpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createUser(NewUser user) {
        String sql = "Insert into Users (StoreId, UserName, FirstName, LastName, Email, UserPassword) " +
                "values(?,?,?,?,?,?)";
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, storeNameToId(user.getStoreLabel()));
            preparedStatement.setString(2, user.getUserName());
            preparedStatement.setString(3, user.getFirstName());
            preparedStatement.setString(4, user.getLastName());
            preparedStatement.setString(5, user.getEmail());
            preparedStatement.setString(6, user.getPassword());
            preparedStatement.execute();
            connection.close();
        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e){
                    System.out.print("Exception in closing connection!");
                }
            }
        }
    }

    public boolean loginUser(LoginUser user) {
        boolean userExist=false;
        String sql = "select UserName, UserPassword from Users where UserName = ? and UserPassword = ?";
        Connection connection = null;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getUserPassword());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                userExist = true;
            }
            connection.close();
        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e){
                    System.out.print("Exception in closing connection!");
                }
            }
        }

        return userExist;
    }

    public int checkUserPrivilege(LoginUser user) {
        int privilege = 0;
        String sql = "select Privileges from Users where UserName=?";
        Connection connection=null;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, user.getUserName());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                privilege = resultSet.getInt("Privileges");
            }
            connection.close();
        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e){
                    System.out.print("Exception in closing connection!");
                }
            }
        }

        return privilege;
    }

    private String storeNameToId(String storeName){
        String id = "";

        String sql = "select StoreId from Stores where Name=?";
        Connection connection=null;

        try {
            connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, storeName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                id = resultSet.getString("StoreId");
            }
            connection.close();
        } catch (SQLException e){
            throw new RuntimeException(e);
        } finally {
            if(connection != null){
                try {
                    connection.close();
                } catch (SQLException e){
                    System.out.print("Exception in closing connection!");
                }
            }
        }
        return id;
    }
}
