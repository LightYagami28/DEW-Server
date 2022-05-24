package me.pari.controllers.users;

import me.pari.Client;
import me.pari.controllers.Controller;
import me.pari.types.tcp.Request;
import me.pari.types.tcp.Response;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.List;

public class GetUsers extends Controller {
    @Override
    public Response execute(@NotNull Request req) {
        Response serverResponse = createOkResponse(req);

        // Get clients
        List<Client> clients = Client.getClients();

        // Put count
        serverResponse.setValue("count", Integer.toString(clients.size()));

        // Create list
        JSONArray arr = new JSONArray(clients.size());
        for (Client c : clients)
            if (c.getUsername() != null)
                arr.put(c.getUsername());

        serverResponse.setValue("list", arr.toString());

        return serverResponse;
    }
}
