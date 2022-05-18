package me.pari.methods;

import me.pari.Utils;
import me.pari.connection.Request;
import me.pari.connection.Response;
import me.pari.types.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class AuthUser extends Method {

    @Override
    public Response execute(@NotNull Request r) {
        HashMap<String, String> params = r.getParams();

        // No params provided
        if (params == null)
            return new Response(r.getId(), 400, "Params are empty");

        // Need to update token
        if (params.get("authToken") != null) {
            HashMap<String, String> values = new HashMap<>();
            String newToken = Utils.generateToken();

            // TODO: Update newToken on database
            Storage db = Storage.getInstance();
            int userId = db.getUserIdByToken(params.get("authToken"));
            db.updateUserToken(userId, newToken);

            values.put("authToken", newToken);
            Response serverResponse = new Response(r.getId(), 200, "ok");
            serverResponse.setValues(values);
            return serverResponse;
        }

        // Need to authenticate username and password

        // Username not provided or empty
        if (params.get("username") == null || params.get("username").isBlank())
            return new Response(r.getId(), 400, "Username is empty");

        // Password not provided or empty
        if (params.get("username") == null || params.get("username").isBlank())
            return new Response(r.getId(), 400, "Password is empty");

        return null;
    }

    @Override
    public boolean isNeededAuth() {
        return false;
    }
}
