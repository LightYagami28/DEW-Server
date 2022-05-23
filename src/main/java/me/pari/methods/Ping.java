package me.pari.methods;

import me.pari.connection.Request;
import me.pari.connection.Response;
import me.pari.connection.Status;

public class Ping extends Method {
    @Override
    public Response execute(Request r) {
        return new Response(r.getId(), Status.OK);
    }
}
