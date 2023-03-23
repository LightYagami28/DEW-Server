package me.pari.types;

import me.pari.Storage;
import org.hydev.logger.HyLogger;

import java.sql.SQLException;

public class UserAuth {

    private static final HyLogger LOGGER = new HyLogger("UserAuth");

    private String authToken;
    private String username;
    private Integer id;

    public UserAuth() {

    }

    public String getAuthToken() {
        return authToken;
    }

    public String getUsername() {
        return username;
    }

    public Integer getId() {
        return id;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public synchronized void resetInfo() {
        setId(null);
        setUsername(null);
        setAuthToken(null);
    }

    public boolean isAuth() {
        if (authToken == null)
            return false;
        try {
            return !Storage.getInstance().isTokenExpired(authToken);
        } catch (SQLException ex) {
            LOGGER.error("SQL Exception in isTokenValid: " + ex.getMessage());
            return false;
        }
    }

}
