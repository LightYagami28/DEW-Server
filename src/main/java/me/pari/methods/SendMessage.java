package me.pari.methods;

import me.pari.connection.Request;
import me.pari.connection.Response;
import me.pari.connection.Status;

import java.sql.SQLException;
import java.util.HashMap;

public class SendMessage extends Method {

    @Override
    public Response execute(Request r) {
        HashMap<String, String> params = r.getParams();

        // No params provided
        if (params == null)
            return new Response(r.getId(), 400, "Params are empty");

        String text = params.get("text");

        // Message not provided
        if (text == null)
            return new Response(r.getId(), Status.BAD_REQUEST, "Message text not provided");

        // Text too much long
        if (text.length() > 1024)
            return new Response(r.getId(), Status.BAD_REQUEST, "Message text too long");

        // Text not provided
        if (text.isBlank())
            return new Response(r.getId(), Status.BAD_REQUEST, "Message text empty");

        // Send message to all clients
        try {
            r.getClient().sendMessageBroadcast(text);
        } catch (SQLException e) {
            return new Response(r.getId(), Status.INTERNAL_ERROR, "SQL Error during sending message: " + e.getMessage());
        }

        return new Response(r.getId(), Status.OK, "Message sent");
    }

}
