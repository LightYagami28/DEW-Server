package me.pari.controllers.auth;

import me.pari.Client;
import me.pari.Utils;
import me.pari.controllers.Controller;
import me.pari.security.Password;
import me.pari.security.Token;
import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import me.pari.types.Status;
import me.pari.Storage;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;

public class SignIn extends Controller {

    @Override
    public Response execute(@NotNull Request req) {

        // Create server response and get params
        Response serverResponse = createOkResponse(req);
        HashMap<String, String> params = req.getParams();

        // No params provided
        if (params == null)
            return new Response(req.getId(), Status.BAD_REQUEST, "Params are empty");

        // Get database instance
        Storage db = Storage.getInstance();
        Client c = req.getClient();

        // SignIn via authToken (or reloading)
        if (params.get("authToken") != null) {

            // Generate new token
            String newToken = Token.genNext();

            try {
                // Check if the token is valid
                if (db.isTokenExpired(params.get("authToken")))
                    throw new SQLException();

                // Get userId associated to the token
                c.setUserId(db.getUserIdByToken(params.get("authToken")));

            } catch (SQLException ex) {
                return new Response(req.getId(), Status.BAD_REQUEST, "AuthToken invalid");
            }

            // Try to update new token in database
            try {
                db.updateUserToken(c.getUserId(), newToken);
            } catch (SQLException ex) {
                return new Response(req.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
            }

            // Set new token in cache
            c.setAuthToken(newToken);

            // Set username (if not already set)
            if (c.getUsername() == null)
                try {
                    c.setUsername(db.getUsernameByUserId(c.getUserId()));
                } catch (SQLException ex) {
                    return new Response(req.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
                }

            // Send new token in serverResponse
            serverResponse.setValue("authToken", newToken);
            return serverResponse;
        }

        // SignIn via username and password
        String username = params.get("username");
        String password = params.get("password");

        // Username not provided or empty
        if (username == null || username.isBlank())
            return new Response(req.getId(), Status.BAD_REQUEST, "Username is empty");

        // Password not provided or empty
        if (password == null || password.isBlank())
            return new Response(req.getId(), Status.BAD_REQUEST, "Password is empty");

        // Check username and password via regex

        if (!SignUp.USERNAME_PATTERN.matcher(username).matches())
            return new Response(req.getId(), Status.BAD_REQUEST, "Username is invalid.");
        if (!SignUp.PASSWORD_PATTERN.matcher(password).matches())
            return new Response(req.getId(), Status.BAD_REQUEST, "Password is invalid.");

        String databasePassword;

        // Get userId and db password
        try {
            c.setUserId(db.getUserId(username.toLowerCase()));
            databasePassword = db.getUserPassword(c.getUserId());
        } catch (SQLException ex) {
            return new Response(req.getId(), Status.BAD_REQUEST, "Username not exists");
        }

        // Password is wrong
        if (!Password.check(password, databasePassword))
            return new Response(req.getId(), Status.BAD_REQUEST, "Wrong password");

        // Generate new token
        String newToken = Token.genNext();

        // Try to update new token in database
        try {
            db.updateUserToken(c.getUserId(), newToken);
        } catch (SQLException ex) {
            return new Response(req.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
        }

        // Set new info in cache
        c.setAuthToken(newToken);
        c.setUsername(params.get("username"));

        // Build response
        serverResponse.setValue("authToken", newToken);

        return serverResponse;
    }

    @Override
    public boolean isNeededAuth() {
        return false;
    }
}
