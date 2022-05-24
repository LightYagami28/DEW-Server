package me.pari.controllers.messages;

import me.pari.controllers.Controller;
import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import me.pari.types.Status;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;

public class SendMessage extends Controller {

    @Override
    public Response execute(@NotNull Request req) {
        HashMap<String, String> params = req.getParams();

        // No params provided
        if (params == null)
            return new Response(req.getId(), 400, "Params are empty");

        String text = params.get("text");

        // Message not provided
        if (text == null)
            return new Response(req.getId(), Status.BAD_REQUEST, "Message text not provided");

        // Text too much long
        if (text.length() > 1024)
            return new Response(req.getId(), Status.BAD_REQUEST, "Message text too long");

        // Text not provided
        if (text.isBlank())
            return new Response(req.getId(), Status.BAD_REQUEST, "Message text empty");

        // Send message to all clients
        try {
            req.getClient().sendMessageBroadcast(text);
        } catch (SQLException e) {
            return new Response(req.getId(), Status.INTERNAL_ERROR, "SQL Error during sending message: " + e.getMessage());
        }

        return createOkResponse(req);
    }

}
