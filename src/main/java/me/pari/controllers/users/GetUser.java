package me.pari.controllers.users;

import me.pari.Storage;
import me.pari.controllers.Controller;
import me.pari.types.Status;
import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;

public class GetUser extends Controller {

    @Override
    public Response execute(@NotNull Request req) {
        HashMap<String, String> params = req.getParams();

        // No params provided
        if (params == null)
            return new Response(req.getId(), Status.BAD_REQUEST, "Params are empty");

        String user = params.get("user_id");

        if (user == null || user.isBlank())
            return new Response(req.getId(), Status.BAD_REQUEST, "user_id is empty");

        Storage db = Storage.getInstance();
        int userId;

        // Get the user id by username
        try {
            userId = Integer.parseInt(user);
            if (!db.hasChatWith(userId))
                return new Response(req.getId(), Status.FORBIDDEN, "You can't get an user info that you never talked with by ID");
        } catch (NumberFormatException ignored) {
            try {
                userId = db.getUserId(user);
            } catch (SQLException _ignored) {
                return new Response(req.getId(), Status.BAD_REQUEST, "Username not found")
            }
        }

        User userInfo;

        Response r = createOkResponse(req);
        r.setValue("user", userInfo);

    }
}
