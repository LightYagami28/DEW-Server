package me.pari.controllers;

import me.pari.types.Status;
import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import org.jetbrains.annotations.NotNull;

public abstract class Controller {

    public Controller() {}

    public abstract Response execute(@NotNull Request req);

    protected static Response createOkResponse(Request req) {
        return new Response(req.getId(), Status.OK);
    }

    public boolean isNeededAuth() { return true;}

}
