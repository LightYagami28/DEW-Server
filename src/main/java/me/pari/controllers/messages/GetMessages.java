package me.pari.controllers.messages;

import me.pari.controllers.Controller;
import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import org.jetbrains.annotations.NotNull;

public class GetMessages extends Controller {
    @Override
    public Response execute(@NotNull Request r) {
        // TODO: Retrieve last messages from database
        return null;
    }
}
