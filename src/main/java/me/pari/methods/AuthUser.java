package me.pari.methods;

import me.pari.Client;
import me.pari.Utils;
import me.pari.connection.Request;
import me.pari.connection.Response;
import me.pari.connection.Status;
import me.pari.Storage;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;

public class AuthUser extends Method {

    @Override
    public Response execute(@NotNull Request r) {
        HashMap<String, String> params = r.getParams();

        // No params provided
        if (params == null)
            return new Response(r.getId(), Status.BAD_REQUEST, "Params are empty");

        // Get database instance
        Storage db = Storage.getInstance();
        Client c = r.getClient();

        // Need to update token
        if (params.get("authToken") != null) {
            HashMap<String, String> values = new HashMap<>();

            // Generate new token
            String newToken = Utils.generateToken();

            int userId;

            try {
                // Check if the token is valid
                if (db.isTokenExpired(params.get("authToken")))
                    throw new SQLException();

                // Get userId associated to the token
                userId = db.getUserIdByToken(params.get("authToken"));

            } catch (SQLException ex) {
                return new Response(r.getId(), Status.BAD_REQUEST, "AuthToken invalid");
            }

            // Try to update new token in database
            try {
                db.updateUserToken(userId, newToken);
            } catch (SQLException ex) {
                return new Response(r.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
            }

            // Set new token in cache
            c.setAuthToken(newToken);
            c.setUserId(userId);
            try {
                c.setUsername(db.getUsernameByUserId(userId));
            } catch (SQLException ex) {
                return new Response(r.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
            }

            // Send new token in serverResponse
            values.put("authToken", newToken);
            Response serverResponse = new Response(r.getId(), Status.OK, "ok");
            serverResponse.setValues(values);
            return serverResponse;
        }

        // Need to authenticate username and password

        // Username not provided or empty
        if (params.get("username") == null || params.get("username").isBlank())
            return new Response(r.getId(), Status.BAD_REQUEST, "Username is empty");

        // Password not provided or empty
        if (params.get("password") == null || params.get("password").isBlank())
            return new Response(r.getId(), Status.BAD_REQUEST, "Password is empty");

        int userId;

        // Hashing password
        String realPasswordHash;
        String passwordHash = Utils.hashPassword(params.get("password"));
        try {
            userId = db.getUserId(params.get("username").toLowerCase());
            realPasswordHash = db.getUserPassword(userId);
        } catch (SQLException ex) {
            return new Response(r.getId(), Status.BAD_REQUEST, "Username not exists");
        }

        // Password is wrong
        if (!realPasswordHash.equals(passwordHash))
            return new Response(r.getId(), Status.BAD_REQUEST, "Wrong password");

        // Generate new token
        String newToken = Utils.generateToken();

        // Try to update new token in database
        try {
            db.updateUserToken(userId, newToken);
        } catch (SQLException ex) {
            return new Response(r.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
        }

        // Set new info in cache
        c.setAuthToken(newToken);
        c.setUserId(userId);
        c.setUsername(params.get("username"));

        // Build response
        HashMap<String, String> values = new HashMap<>();
        values.put("authToken", newToken);
        Response serverResponse = new Response(r.getId(), Status.OK);
        serverResponse.setValues(values);
        return serverResponse;
    }

    @Override
    public boolean isNeededAuth() {
        return false;
    }
}
