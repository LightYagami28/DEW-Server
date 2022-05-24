package me.pari.controllers.auth;

import me.pari.Client;
import me.pari.Storage;
import me.pari.controllers.Controller;
import me.pari.types.Status;
import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class SignOut extends Controller {

    @Override
    public Response execute(@NotNull Request req) {

        // Create server response
        Response serverResponse = createOkResponse(req);
        Client c = req.getClient();

        // Check if the connection is signed in
        if (!c.isTokenValid() || c.getUserId() != 0)
            return new Response(req.getId(), Status.BAD_REQUEST, "You are not Signed In");

        // Invalidate current authToken
        try {
            Storage.getInstance().removeUserToken(c.getUserId());
        } catch (SQLException ex) {
            return new Response(req.getId(), Status.INTERNAL_ERROR, "SQL Error: " + ex.getMessage());
        }

        // Reset cached connection info
        c.resetInfo();

        return serverResponse;
    }
}
