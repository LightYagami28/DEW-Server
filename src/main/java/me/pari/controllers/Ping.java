package me.pari.controllers;

import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import org.jetbrains.annotations.NotNull;

public class Ping extends Controller {
    @Override
    public Response execute(@NotNull Request req) {
        Response r = createOkResponse(req);
        r.setValue("Version", "1");
        return r;
    }
}
