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
import java.util.regex.Pattern;

public class SignUp extends Controller {

    public final static Pattern USERNAME_PATTERN = Pattern.compile(
            "^(?=.{3,20}$)(?![\\d_.])(?!.*[_.]{2})[a-zA-Z\\d._]+(?<![_.])$"
    );

    public final static Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,32}$"
    );

    @Override
    public Response execute(@NotNull Request req) {

        // Create ok response and get params
        Response serverResponse = createOkResponse(req);
        HashMap<String, String> params = req.getParams();

        // No params provided
        if (params == null)
            return new Response(req.getId(), Status.BAD_REQUEST, "Params are empty");

        // Extract username and password
        String username = params.get("username");
        String password = params.get("password");

        // Username not provided or empty
        if (username == null || username.isBlank())
            return new Response(req.getId(), Status.BAD_REQUEST, "Username is empty");

        username = username.toLowerCase();

        // Password not provided or empty
        if (password == null || password.isBlank())
            return new Response(req.getId(), Status.BAD_REQUEST, "Password is empty");

        Storage db = Storage.getInstance();

        // Already taken username
        try {
            db.getUserId(username);
            return new Response(req.getId(), Status.BAD_REQUEST, "Username already taken.");
        } catch (SQLException ignored) {}

        // Invalid username
        if (!USERNAME_PATTERN.matcher(username).matches())
            return new Response(req.getId(), Status.BAD_REQUEST, "Username is invalid. It should be 3-20 characters and contain only numbers, letters, underscore and dot.");

        // Invalid password
        if (!PASSWORD_PATTERN.matcher(password).matches())
            return new Response(req.getId(), Status.BAD_REQUEST, "Password is invalid. It should be 8-32 characters and contain at least one uppercase letter, one lowercase letter, one number and one special character.");

        String authToken = Token.genNext();
        Client c = req.getClient();

        // Sign up user into database and update the authToken
        try {
            c.setUserId(db.addUser(username, Password.hash(password)));
            db.updateUserToken(c.getUserId(), authToken);

        } catch (SQLException ex) {
            return new Response(req.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
        }

        serverResponse.setValue("authToken", authToken);
        return serverResponse;
    }

    @Override
    public boolean isNeededAuth() {
        return false;
    }
}
