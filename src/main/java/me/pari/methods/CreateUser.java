package me.pari.methods;

import me.pari.Utils;
import me.pari.connection.Request;
import me.pari.connection.Response;
import me.pari.connection.Status;
import me.pari.Storage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class CreateUser extends Method {

    private final static Pattern USERNAME_PATTERN = Pattern.compile(
            "^(?=.{3,20}$)(?![\\d_.])(?!.*[_.]{2})[a-zA-Z\\d._]+(?<![_.])$"
    );

    private final static Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,32}$"
    );

    @Override
    public Response execute(Request r) {
        HashMap<String, String> params = r.getParams();

        // No params provided
        if (params == null)
            return new Response(r.getId(), Status.BAD_REQUEST, "Params are empty");

        // Extract username and password
        String username = params.get("username");
        String password = params.get("password");

        // Username not provided or empty
        if (username == null || username.isBlank())
            return new Response(r.getId(), Status.BAD_REQUEST, "Username is empty");

        username = username.toLowerCase();

        // Password not provided or empty
        if (password == null || password.isBlank())
            return new Response(r.getId(), Status.BAD_REQUEST, "Password is empty");

        Storage db = Storage.getInstance();

        // Already taken username
        try {
            db.getUserId(username);
            return new Response(r.getId(), Status.BAD_REQUEST, "Username already taken.");
        } catch (SQLException ignored) {}

        // Invalid username
        if (!USERNAME_PATTERN.matcher(username).matches())
            return new Response(r.getId(), Status.BAD_REQUEST, "Username is invalid. It should be 3-20 characters and contain only numbers, letters, underscore and dot.");

        // Invalid password
        if (!PASSWORD_PATTERN.matcher(password).matches())
            return new Response(r.getId(), Status.BAD_REQUEST, "Password is invalid. It should be 8-32 characters and contain at least one uppercase letter, one lowercase letter, one number and one special character.");

        String authToken;

        // Sign up user into database and retrieve first authToken
        try {
            authToken = db.addUser(username, Utils.hashPassword(password));

        } catch (SQLException ex) {
            return new Response(r.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
        }

        // Build response
        HashMap<String, String> values = new HashMap<>();
        values.put("authToken", authToken);
        Response serverResponse = new Response(r.getId(), Status.OK);
        serverResponse.setValues(values);
        return serverResponse;
    }

    @Override
    public boolean isNeededAuth() {
        return false;
    }
}
